package me.markoutte.joker.hw1.step2

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.URLClassLoader
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import me.markoutte.joker.hw1.strategies.*

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
        println("Method $className#$methodName is not found")
        return
    }

    val fuzzingStrategy: FuzzingStrategy = GarbageStrategy()
    val mutationStrategy = MutationStrategies::random
    val b = ByteArray(fuzzingStrategy.defaultBufferSize)

    println("Fuzzing strategy: ${fuzzingStrategy.javaClass.simpleName}")
    try {
        val dataEx = b.apply(random::nextBytes)
        val inputValuesEx = fuzzingStrategy.generateInputValues(javaMethod, dataEx)
        println("Input example: ${inputValuesEx.contentDeepToString()}")
    } catch(_: BufferUnderflowException) { }

    var iterations = 0
    val seeds = mutableMapOf<Int, ByteArray>()

    while(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(timeout)) {
        val data = seeds.values.randomOrNull(random)?.let { mutationStrategy(random, it) }
            ?: b.apply(random::nextBytes)
        val inputValues: Array<Any>
        try {
            inputValues = fuzzingStrategy.generateInputValues(javaMethod, data)
        } catch (e: BufferUnderflowException) {
            println("Warning: byte buffer exhausted")
            continue
        }
        iterations++
        val inputValuesString = "${javaMethod.name}: ${inputValues.contentDeepToString()}"
        try {
            javaMethod.invoke(null, *inputValues).apply {
                val seedId = data.contentHashCode()
                if (seeds.putIfAbsent(seedId, data) == null) {
                    println("New seed added: ${seedId.toHexString()}")
                }
            }
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
        }
    }

    println("Main loop iterations: ${iterations}")
    println("Seeds found: ${seeds.size}")
    println("Errors found: ${errors.size}")
    println("Time elapsed: ${TimeUnit.NANOSECONDS.toMillis(
        System.nanoTime() - start
    )} ms")
}

fun loadJavaMethod(className: String, methodName: String, classPath: String): Method {
    val libraries = classPath
        .split(File.pathSeparatorChar)
        .map { File(it).toURI().toURL() }
        .toTypedArray()
    val classLoader = URLClassLoader(libraries)
    val javaClass = classLoader.loadClass(className)
    val javaMethod = javaClass.declaredMethods.first {
        "${it.name}(${it.parameterTypes.joinToString(",") {
                c -> c.typeName
        }})" == methodName
    }
    return javaMethod
}

fun generateInputValues(method: Method, data: ByteArray): Array<Any> {
    val buffer = ByteBuffer.wrap(data)
    val parameterTypes = method.parameterTypes
    return Array(parameterTypes.size) {
        when (parameterTypes[it]) {
            Int::class.java -> buffer.get().toInt()
            IntArray::class.java -> IntArray(buffer.get().toUByte().toInt()) {
                buffer.get().toInt()
            }
            String::class.java -> String(ByteArray(
                buffer.get().toUByte().toInt() + 1
            ) {
                buffer.get()
            }, Charset.forName("koi8"))
            else -> error("Cannot create value of type ${parameterTypes[it]}")
        }
    }
}