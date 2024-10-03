package me.markoutte.joker.hw1.strategies

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.Stack
import kotlin.random.Random

class RandomHTMLTagsAttributesStrategy(rand: Random): FuzzingStrategy(rand) {
    private val maxNameLength = 10U
    private val maxGarbageLength = 50U
    private val maxAttributes = 4U

    private fun generateName(buffer: ByteBuffer): String {
        val nameLength: Int = (buffer.get().toUByte() % maxNameLength).toInt() + 1

        return String(ByteArray(nameLength) {
                ((buffer.get().toUByte() % 26U) + 'a'.code.toUInt()).toByte()
            })
    }

    private fun generateGarbage(buffer: ByteBuffer): String {
        val length: Int = (buffer.get().toUByte() % maxGarbageLength).toInt() + 1

        return String(ByteArray(length) {
            buffer.get().let { b -> when(b) {
                    in (0x0..0x1f) -> (b.toInt() + 0x40).toByte()
                    in (0x7f..0x9f) -> (b.toInt() + 0x40).toByte()
                    else -> b
                }
            }
        }, Charset.forName("iso-8859-1"))
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
                val newTagName = generateName(buffer)
                tagStack.push(newTagName)

                val attributesCount = (buffer.get().toUByte() % maxAttributes).toInt()
                val attributes = List(attributesCount) {
                    Pair(generateName(buffer), generateGarbage(buffer))
                }

                htmlString += "<$newTagName${
                    attributes.joinToString("") { " ${it.first}=\"${it.second}\"" }
                }>"
            } else {
                val tagName = tagStack.pop()
                htmlString += "</$tagName>"
            }
        }

        return htmlString
    }
}