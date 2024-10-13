package me.markoutte.joker.hw1.strategies

import java.nio.ByteBuffer

class SpecialCharsStrategy : FuzzingStrategy() {
    override val defaultBufferSize: Int
        get() = 300

    private val specialChars = listOf(
        '<', '>', '/', '<', '>', '/', '<', '>', '/', '<', '>', '/', '=', '&', ';', '!', '-'
    )

    override fun generateString(buffer: ByteBuffer): String =
        String(ByteArray(
            buffer.get().toUByte().toInt()
        ) {
            specialChars[buffer.get().toUByte().toInt() % specialChars.size].code.toByte()
        })
}