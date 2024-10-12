package me.markoutte.joker.hw1.strategies

import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.random.Random

class LongGarbageStrategy: FuzzingStrategy() {
    override val defaultBufferSize: Int
        get() = 300 * lengthBytes

    private val lengthBytes = 2

    override fun generateString(buffer: ByteBuffer): String {
        var length = 1
        repeat(lengthBytes) {
            length += buffer.get().toUByte().toInt()
        }

        return String(ByteArray(length) {
            buffer.get()
        }, Charset.forName("koi8"))
    }
}