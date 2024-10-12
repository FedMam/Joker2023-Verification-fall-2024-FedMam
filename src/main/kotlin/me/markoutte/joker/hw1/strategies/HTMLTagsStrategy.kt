package me.markoutte.joker.hw1.strategies

import java.nio.ByteBuffer
import java.nio.charset.Charset

class HTMLTagsStrategy : FuzzingStrategy() {
    override val defaultBufferSize: Int
        get() = 2000

    private val maxTagNameLength = 10U

    private fun generateHTMLTag(buffer: ByteBuffer): String {
        val nextByte = buffer.get().toUByte()
        val tagNameLength: Int = (nextByte % maxTagNameLength).toInt()
        val closed: Boolean = (nextByte.and(0x80U)).toUInt() != 0U

        return "<${if (closed) "/" else ""}${
            String(ByteArray(tagNameLength) { buffer.get() }, Charset.forName("iso-8859-5"))
        }>"
    }

    override fun generateString(buffer: ByteBuffer): String {
        return List(buffer.get().toUByte().toInt() + 1) {
            generateHTMLTag(buffer)
        }.joinToString("")
    }
}