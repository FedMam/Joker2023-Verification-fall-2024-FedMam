package me.markoutte.joker.hw1.strategies

import kotlin.random.Random

class GarbageStrategy: FuzzingStrategy() {
    override val defaultBufferSize: Int
        get() = 300
}