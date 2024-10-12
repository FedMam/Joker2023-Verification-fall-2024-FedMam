package me.markoutte.joker.hw1.strategies

import java.nio.ByteBuffer
import java.util.Stack
import kotlin.math.sqrt

class HTMLTagsMixedStrategy : FuzzingStrategy() {
    override val defaultBufferSize: Int
        get() = 8000

    private val maxTagNameLength = 10U

    private fun generateHTMLTagName(buffer: ByteBuffer): String {
        val tagNameLength: Int = (buffer.get().toUByte() % maxTagNameLength).toInt() + 1

        return String(ByteArray(tagNameLength) {
                ((buffer.get().toUByte() % 26U) + 'a'.code.toUInt()).toByte()
            })
    }

    override fun generateString(buffer: ByteBuffer): String {
        val numberOfTags = buffer.get().toUByte().toInt() + 1
        val tagStack = Stack<String>()
        var tagsCreated = 0
        var tagStringsList = mutableListOf<String>()

        while (tagsCreated < numberOfTags || !tagStack.empty()) {
            val openBrace: Boolean = ((buffer.get().toUByte() % 2U == 0U) || tagStack.empty()) && tagsCreated < numberOfTags

            if (openBrace) {
                tagsCreated++
                val newTagName = generateHTMLTagName(buffer)
                tagStack.push(newTagName)
                tagStringsList.add("<$newTagName>")
            } else {
                val tagName = tagStack.pop()
                tagStringsList.add("</$tagName>")
            }
        }

        repeat(sqrt(numberOfTags.toDouble()).toInt() + 1) { i ->
            val k = buffer.get().toUByte().toInt() % numberOfTags
            val l = buffer.get().toUByte().toInt() % numberOfTags

            if (k == l)
                tagStringsList.removeAt(k)
            else {
                val temp = tagStringsList[k]
                tagStringsList[k] = tagStringsList[l]
                tagStringsList[l] = temp
            }
        }

        return tagStringsList.joinToString("")
    }
}