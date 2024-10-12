package me.markoutte.joker.hw1.strategies

import java.nio.ByteBuffer

class MalformedHTMLStrategy : FuzzingStrategy() {
    override val defaultBufferSize: Int
        get() = 1500

    private fun generateGarbage(buffer: ByteBuffer): String {
        return String(ByteArray(buffer.get().toUByte().toInt()) {
            buffer.get()
        })
    }

    private fun writeOrNotRandomly(str: String, buffer: ByteBuffer): String =
        if (buffer.get().toUByte().toInt() % 2 == 0) str
        else ""

    override fun generateString(buffer: ByteBuffer): String {
        return listOf("<html ${writeOrNotRandomly(generateGarbage(buffer), buffer)}>",
            "<head ${writeOrNotRandomly(generateGarbage(buffer), buffer)}>",
            generateGarbage(buffer),
            "</head>",
            "<body ${writeOrNotRandomly(generateGarbage(buffer), buffer)}>",
            generateGarbage(buffer),
            "</body>",
            "</html>")
            .map { writeOrNotRandomly(it, buffer) }
            .joinToString("")
    }
}