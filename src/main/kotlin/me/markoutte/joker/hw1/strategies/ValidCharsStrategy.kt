package me.markoutte.joker.hw1.strategies

import java.nio.ByteBuffer

class ValidCharsStrategy : FuzzingStrategy() {
    override val defaultBufferSize: Int
        get() = 300

    private val validChars =
        " !\"&-/0123456789;<=>ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz".toCharArray().toList()

    override fun generateString(buffer: ByteBuffer): String =
        String(ByteArray(
            buffer.get().toUByte().toInt()
        ) {
            validChars[buffer.get().toUByte().toInt() % validChars.size].code.toByte()
        })
}