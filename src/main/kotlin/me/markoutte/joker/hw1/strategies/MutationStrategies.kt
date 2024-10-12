package me.markoutte.joker.hw1.strategies

import kotlin.math.*
import kotlin.random.Random

object MutationStrategies {
    fun brush(rand: Random, buffer: ByteArray): ByteArray = buffer.clone().apply {
        val position = rand.nextInt(0, size)
        val repeat = rand.nextInt((size - position))
        val from = rand.nextInt(-128, 127)
        val until = rand.nextInt(from + 1, 128)

        repeat(repeat) { i ->
            set(position + i, rand.nextInt(from, until).toByte())
        }
    }

    fun shotgun(rand: Random, buffer: ByteArray): ByteArray = buffer.clone().apply {
        val repeat = rand.nextInt(sqrt(size.toDouble()).toInt())
        val from = rand.nextInt(-128, 127)
        val until = rand.nextInt(from + 1, 128)

        repeat(repeat) {
            set(rand.nextInt(0, size), rand.nextInt(from, until).toByte())
        }
    }

    fun spray(rand: Random, buffer: ByteArray): ByteArray = buffer.clone().apply {
        val repeat = rand.nextInt(size)
        val from = rand.nextInt(-128, 127)
        val until = rand.nextInt(from + 1, 128)

        repeat(repeat) {
            set(rand.nextInt(0, size), rand.nextInt(from, until).toByte())
        }
    }

    fun eraser(rand: Random, buffer: ByteArray): ByteArray = buffer.clone().apply {
        val position = rand.nextInt(0, size)
        val width = min(size - position, rand.nextInt(sqrt(size.toDouble()).toInt()))

        repeat(width) { i ->
            set(position + i, 0)
        }
    }

    fun solidBrush(rand: Random, buffer: ByteArray): ByteArray = buffer.clone().apply {
        val position = rand.nextInt(0, size)
        val width = rand.nextInt(size - position)
        val paint = rand.nextInt(-128, 128).toByte()

        repeat(width) { i ->
            set(position + i, paint)
        }
    }

    fun iotaBrush(rand: Random, buffer: ByteArray): ByteArray = buffer.clone().apply {
        val position = rand.nextInt(0, size)
        val width = rand.nextInt(size - position)
        val iotaStart = rand.nextInt(-128, 128).toByte()

        var iota = iotaStart
        repeat(width) { i ->
            set(position + i, iota)
            iota = ((iota.toUByte().toInt() + 1) % 256).toUByte().toByte()
        }
    }

    fun incBrush(rand: Random, buffer: ByteArray): ByteArray = buffer.clone().apply {
        val position = rand.nextInt(0, size)
        val width = rand.nextInt(size - position)
        val inc = 1

        repeat(width) { i ->
            set(position + i, ((get(position + i).toUByte().toInt() + inc) % 256).toUByte().toByte())
        }
    }

    fun plusBrush(rand: Random, buffer: ByteArray): ByteArray = buffer.clone().apply {
        val position = rand.nextInt(0, size)
        val width = rand.nextInt(size - position)
        val inc = rand.nextInt(0, 256)

        repeat(width) { i ->
            set(position + i, ((get(position + i).toUByte().toInt() + inc) % 256).toUByte().toByte())
        }
    }

    fun pierce(rand: Random, buffer: ByteArray): ByteArray = buffer.clone().apply {
        val position = rand.nextInt(0, size)
        val value = rand.nextInt(-128, 128).toByte()

        set(position, value)
    }

    val allStrategies = listOf(
        ::brush,
        ::shotgun,
        ::spray,
        ::eraser,
        ::solidBrush,
        ::iotaBrush,
        ::incBrush,
        ::plusBrush
    )

    fun random(rand: Random, buffer: ByteArray): ByteArray =
        allStrategies.random(rand)(rand, buffer)
}