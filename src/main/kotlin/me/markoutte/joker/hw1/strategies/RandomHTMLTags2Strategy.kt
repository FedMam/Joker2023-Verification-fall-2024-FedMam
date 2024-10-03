package me.markoutte.joker.hw1.strategies

import java.nio.ByteBuffer
import java.util.Stack
import kotlin.random.Random

class RandomHTMLTags2Strategy(rand: Random): FuzzingStrategy(rand) {
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
        var htmlString = ""

        while (tagsCreated < numberOfTags) {
            val openBrace: Boolean = (buffer.get().toUByte() % 2U == 0U) || tagStack.empty()

            if (openBrace) {
                tagsCreated++
                val newTagName = generateHTMLTagName(buffer)
                tagStack.push(newTagName)
                htmlString += "<$newTagName>"
            } else {
                val tagName = tagStack.pop()
                htmlString += "</$tagName>"
            }
        }

        return htmlString
    }
}