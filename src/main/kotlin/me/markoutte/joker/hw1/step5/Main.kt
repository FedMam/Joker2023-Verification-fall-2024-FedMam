package me.markoutte.joker.hw1.step5

import me.markoutte.joker.helpers.ComputeClassWriter
import me.markoutte.joker.hw1.strategies.*
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.objectweb.asm.*
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.URLClassLoader
import java.nio.BufferUnderflowException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.HashSet
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@ExperimentalStdlibApi
fun main(args: Array<String>) {
    val options = Options().apply {
        addOption("c", "class", true, "Java class fully qualified name")
        addOption("m", "method", true, "Method to be tested")
        addOption("cp", "classpath", true, "Classpath with libraries")
        addOption("t", "timeout", true, "Maximum time for fuzzing in seconds")
        addOption("s", "seed", true, "The source of randomness")
    }
    val parser = DefaultParser().parse(options, args)
    val className = parser.getOptionValue("class")
    val methodName = parser.getOptionValue("method")
    val classPath = parser.getOptionValue("classpath")
    val timeout = parser.getOptionValue("timeout")?.toLong() ?: 10L
    val seed = parser.getOptionValue("seed")?.toInt() ?: Random.nextInt()
    val random = Random(seed)

    println("Running: $className.$methodName) with seed = $seed")
    val errors = mutableSetOf<String>()
    val start = System.nanoTime()

    val javaMethod = try {
        loadJavaMethod(className, methodName, classPath)
    } catch (t: Throwable) {
        println("Method $className#$methodName load error: $t")
        return
    }

    val fuzzingStrategy: FuzzingStrategy = GarbageStrategy()
    val mutationStrategy = MutationStrategies::random
    val mergingStrategy = MergingStrategies::random
    val b = ByteArray(fuzzingStrategy.defaultBufferSize)

    println("Fuzzing strategy: ${fuzzingStrategy.javaClass.simpleName}")
    try {
        val dataEx = b.apply(random::nextBytes)
        val inputValuesEx = fuzzingStrategy.generateInputValues(javaMethod, dataEx)
        println("Input example: ${inputValuesEx.contentDeepToString()}")
    } catch(_: BufferUnderflowException) { }

    // здесь я реализовал генетический алгоритм
    // вспомогательная статья: https://habr.com/ru/articles/254759/

    val SAMPLES_NUM = 10000
    val CHILDREN_NUM = 2000
    val LUCKIE_NUM = 50
    val NORMAL_MUTATION_NUM = 100
    val SHAKEUP_MUTATION_NUM = 2500
    val STEPS_UNTIL_SHAKEUP = 10
    val REPORT_INTERVAL = 10

    val samples = PriorityQueue<Pair<Int, ByteArray>> { smpl1, smpl2 ->
        compareValues(
            smpl1.first,
            smpl2.first
        ) // сэмпл с меньшим значением целевой функции будет первым (чтобы его быстро удалить)
    }

    var iterations = 0
    var idleIterations = 0 // итерации, в течение которых лучший результат не менялся
    var bestResult = 0
    var bestSample = b
    var reports = 0

    fun evaluateSample(data: ByteArray): Int {
        val inputValues: Array<Any>
        try {
            inputValues = fuzzingStrategy.generateInputValues(javaMethod, data)
        } catch (e: BufferUnderflowException) {
            println("Warning: byte buffer exhausted")
            return 0
        }

        val inputValuesString = "${javaMethod.name}: ${inputValues.contentDeepToString()}"
        try {
            ExecutionResult.linesVisited = HashSet()
            javaMethod.invoke(null, *inputValues)
            val sampleRating = ExecutionResult.linesVisited.size
            if (sampleRating > bestResult) {
                bestResult = sampleRating
                bestSample = data
                idleIterations = 0
            }
            return sampleRating
        } catch (e: InvocationTargetException) {
            if (errors.add(e.targetException::class.qualifiedName!!)) {
                val errorName = e.targetException::class.simpleName
                println("New error found: $errorName")
                val path = Paths.get("report$errorName.txt")
                Files.write(path, listOf(
                    "${e.targetException.stackTraceToString()}\n",
                    "$inputValuesString\n",
                    "${data.contentToString()}\n",
                ))
                Files.write(path, data, StandardOpenOption.APPEND)
                println("Saved to: ${path.fileName}")
            }
            return 0
        }
    }

    // начальная генерация
    repeat(SAMPLES_NUM) {
        val data = b.apply(random::nextBytes)
        val rating = evaluateSample(data)
        samples.push(Pair(rating, data))
    }

    while(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(timeout)) {
        // генерация потомков
        val children = mutableListOf<Pair<Int, ByteArray>>()
        repeat(CHILDREN_NUM) {
            val sample1 = samples.getRandom(random)
            val sample2 = samples.getRandom(random)
            val chldData = mergingStrategy(random, sample1.second, sample2.second)
            val chldRating = evaluateSample(chldData)
            children.add(Pair(chldRating, chldData))
        }
        for (chld in children) {
            samples.push(chld)
        }

        // отбор "счастливчиков": сэмплов с плохим результатом, которые
        // отличаются от других и, следовательно, внесут разнообразие
        // в следующие итерации
        val luckies = mutableListOf<Pair<Int, ByteArray>>()
        repeat(LUCKIE_NUM) {
            luckies.add(samples.pollRandom(random))
        }

        // отбор сэмплов: удаление худших
        repeat(CHILDREN_NUM - LUCKIE_NUM) {
            samples.poll()
        }

        // возвращение "счастливчиков" на место
        for (luckie in luckies) {
            samples.push(luckie)
        }

        val shakeup = idleIterations >= STEPS_UNTIL_SHAKEUP
        // мутация
        // если результат не менялся в течение n итераций,
        // происходит "встряска" (интенсивная мутация)
        repeat(if (shakeup) SHAKEUP_MUTATION_NUM else NORMAL_MUTATION_NUM) {
            val sample = samples.pollRandom(random)
            val mutatedData = mutationStrategy(random, sample.second)
            val mutatedRating = evaluateSample(mutatedData)
            samples.push(Pair(mutatedRating, mutatedData))
        }

        /*
        if (shakeup && bestResult != 0 && random.nextInt(2) == 0) {
            samples.poll()
            samples.push(Pair(bestResult, bestSample))
        }
        */

        if (shakeup)
            idleIterations = 0
        else idleIterations++
        iterations++

        if (System.nanoTime() - start >= TimeUnit.SECONDS.toNanos((REPORT_INTERVAL * (reports + 1)).toLong())) {
            val currentBestSample = samples.maxBy { it.first }

            println("=== REPORT ===")
            println("Time elapsed: ${TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - start
            )} ms")
            println("Algorithm iterations: $iterations")
            println("Best result: $bestResult lines")
            println("Current best result: ${currentBestSample.first} lines")
            // println("Current best input: ${strategy.generateInputValues(javaMethod, currentBestSample.second).contentDeepToString()}")

            reports++
        }
    }

    println("=== FINISHED ===")
    println("Algorithm iterations: $iterations")
    println("Best result: $bestResult lines")
    println("Errors found: ${errors.size}")
    println("Time elapsed: ${TimeUnit.NANOSECONDS.toMillis(
        System.nanoTime() - start
    )} ms")
    println("Best sample:")
    println(fuzzingStrategy.generateInputValues(javaMethod, bestSample).contentDeepToString())
}

object ExecutionResult {
    @JvmField
    var linesVisited: MutableSet<Pair<Int, Int>> = HashSet()
}

// Я изменил инструментацию: теперь вместо того, чтобы
// формировать уникальный id, номера посещённых строк
// добавляются в set
fun loadJavaMethod(className: String, methodName: String, classPath: String): Method {
    val libraries = classPath
        .split(File.pathSeparatorChar)
        .map { File(it).toURI().toURL() }
        .toTypedArray()
    val classLoader = object : URLClassLoader(libraries) {
        override fun loadClass(name: String, resolve: Boolean): Class<*> {
            return if (name.startsWith(className.substringBeforeLast('.'))) {
                transformAndGetClass(name).apply {
                    if (resolve) resolveClass(this)
                }
            } else {
                super.loadClass(name, resolve)
            }
        }

        fun transformAndGetClass(name: String): Class<*> {
            val owner = name.replace('.', '/')
            var bytes =
                getResourceAsStream("$owner.class")!!.use { it.readBytes() }
            val reader = ClassReader(bytes)
            val cl = this
            val writer = ComputeClassWriter(
                reader, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, cl
            )
            val transformer = object : ClassVisitor(Opcodes.ASM9, writer) {
                override fun visitMethod(
                    access: Int,
                    name: String?,
                    descriptor: String?,
                    signature: String?,
                    exceptions: Array<out String>?
                ): MethodVisitor {
                    return object : MethodVisitor(
                        Opcodes.ASM9,
                        super.visitMethod(
                            access, name, descriptor, signature, exceptions
                        )
                    ) {
                        val ownerName =
                            ExecutionResult.javaClass.canonicalName.replace('.', '/')
                        val fieldName = "linesVisited"

                        override fun visitLineNumber(line: Int, start: Label?) {
                            visitFieldInsn(
                                Opcodes.GETSTATIC, ownerName, fieldName, "Ljava/util/Set;"
                            )
                            visitTypeInsn(
                                Opcodes.NEW, "kotlin/Pair"
                            )
                            visitInsn(Opcodes.DUP)
                            visitLdcInsn(owner.hashCode())
                            visitMethodInsn(
                                Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false
                            )
                            visitLdcInsn(line)
                            visitMethodInsn(
                                Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false
                            )
                            visitMethodInsn(
                                Opcodes.INVOKESPECIAL, "kotlin/Pair", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V", false
                            )
                            visitMethodInsn(
                                Opcodes.INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true
                            )
                            visitInsn(Opcodes.POP)
                            super.visitLineNumber(line, start)
                        }
                    }
                }
            }
            reader.accept(transformer, ClassReader.SKIP_FRAMES)
            bytes = writer.toByteArray()
            return defineClass(name, bytes, 0, bytes.size)
        }
    }
    val javaClass = classLoader.loadClass(className)
    val javaMethod = javaClass.declaredMethods.first {
        "${it.name}(${it.parameterTypes.joinToString(",") {
                c -> c.typeName
        }})" == methodName
    }
    return javaMethod
}