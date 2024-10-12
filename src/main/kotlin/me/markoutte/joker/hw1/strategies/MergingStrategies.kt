package me.markoutte.joker.hw1.strategies

import kotlin.experimental.xor
import kotlin.math.sqrt
import kotlin.random.Random

object MergingStrategies {
    fun intersperse(rand: Random, data1: ByteArray, data2: ByteArray): ByteArray {
        val size = minOf(data1.size, data2.size)

        val resData: ByteArray = ByteArray(size)
        repeat(size) { i ->
            resData[i] =
                if (rand.nextInt(2) == 0) data1[i]
                else data2[i]
        }

        return resData
    }

    fun segment(rand: Random, data1: ByteArray, data2: ByteArray): ByteArray {
        val size = minOf(data1.size, data2.size)
        val from = rand.nextInt(size - 1)
        val until = rand.nextInt(from, size)

        val resData: ByteArray = ByteArray(size)
        repeat(size) { i ->
            resData[i] =
                if (i in from..until) data2[i]
                else data1[i]
        }

        return resData
    }

    fun blend(rand: Random, data1: ByteArray, data2: ByteArray): ByteArray {
        val size = minOf(data1.size, data2.size)

        val resData: ByteArray = ByteArray(size)
        repeat(size) { i ->
            val from = minOf(data1[i].toInt(), data2[i].toInt())
            val until = maxOf(data1[i].toInt(), data2[i].toInt())
            resData[i] = rand.nextInt(from, until + 1).toByte()
        }

        return resData
    }

    fun spray(rand: Random, data1: ByteArray, data2: ByteArray): ByteArray {
        val size = minOf(data1.size, data2.size)

        val resData: ByteArray = ByteArray(size)
        val prob = 1.0 / sqrt(size.toDouble())
        repeat(size) { i ->
            resData[i] =
                if (rand.nextDouble() < prob) data2[i]
                else data1[i]
        }

        return resData
    }

    fun blendSegment(rand: Random, data1: ByteArray, data2: ByteArray): ByteArray {
        val size = minOf(data1.size, data2.size)
        val from = rand.nextInt(size - 1)
        val until = rand.nextInt(from, size)

        val resData: ByteArray = ByteArray(size)
        repeat(size) { i ->
            resData[i] =
                if (i in from..until)
                    rand.nextInt(minOf(data1[i], data2[i]).toInt(), maxOf(data1[i], data2[i]).toInt() + 1).toByte()
                else data1[i]
        }

        return resData
    }

    val allStrategies = listOf(
        ::intersperse,
        ::segment,
        ::blend,
        ::spray,
        ::blendSegment
    )

    fun random(rand: Random, data1: ByteArray, data2: ByteArray) =
        allStrategies.random(rand)(rand, data1, data2)
}