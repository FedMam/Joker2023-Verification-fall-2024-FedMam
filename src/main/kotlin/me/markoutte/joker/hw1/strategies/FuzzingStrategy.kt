package me.markoutte.joker.hw1.strategies

import java.lang.reflect.Method
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.random.Random

abstract class FuzzingStrategy(val rand: Random) {
    protected open fun generateInt(buffer: ByteBuffer): Int = buffer.get().toInt()

    protected open fun generateIntArray(buffer: ByteBuffer): IntArray =
        IntArray(buffer.get().toUByte().toInt()) {
            generateInt(buffer)
        }

    protected open fun generateString(buffer: ByteBuffer): String =
        String(ByteArray(
            buffer.get().toUByte().toInt() + 1
        ) {
            buffer.get()
        }, Charset.forName("koi8"))

    open fun generateInputValues(method: Method, data: ByteArray): Array<Any> {
        val buffer = ByteBuffer.wrap(data)
        val parameterTypes = method.parameterTypes
        return Array(parameterTypes.size) {
            when (parameterTypes[it]) {
                Int::class.java -> generateInt(buffer)
                IntArray::class.java -> generateIntArray(buffer)
                String::class.java -> generateString(buffer)
                else -> error("Cannot create value of type ${parameterTypes[it]}")
            }
        }
    }
}