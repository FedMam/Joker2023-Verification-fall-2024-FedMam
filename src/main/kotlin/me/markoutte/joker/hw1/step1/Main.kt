package me.markoutte.joker.hw1.step1

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.URLClassLoader
import java.nio.BufferUnderflowException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import me.markoutte.joker.hw1.strategies.*

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
    val b = ByteArray(20000)
    val start = System.nanoTime()

    val javaMethod = try {
        loadJavaMethod(className, methodName, classPath)
    } catch (t: Throwable) {
        println("Method $className#$methodName is not found")
        return
    }

    val strategy: FuzzingStrategy = RandomHTMLTagsAttributesStrategy(random)
    println("Fuzzing strategy: ${strategy.javaClass.simpleName}")
    try {
        val dataEx = b.apply(random::nextBytes)
        val inputValuesEx = strategy.generateInputValues(javaMethod, dataEx)
        println("Input example: ${inputValuesEx.contentDeepToString()}")
    } catch(_: BufferUnderflowException) { }

    var iterations = 0

    while(System.nanoTime() - start < TimeUnit.SECONDS.toNanos(timeout)) {
        val data = b.apply(random::nextBytes)
        val inputValues: Array<Any>
        try {
            inputValues = strategy.generateInputValues(javaMethod, data)
        } catch (e: BufferUnderflowException) {
            println("Warning: byte buffer exhausted")
            continue
        }
        iterations++
        val inputValuesString = "${javaMethod.name}: ${inputValues.contentDeepToString()}"
        try {
            javaMethod.invoke(null, *inputValues)
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