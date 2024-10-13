package me.markoutte.joker.hw1.strategies.gbf

interface GrammarNode

class Terminal(val value: String): GrammarNode {
    override fun toString(): String = value
}

class NonTerminal(val name: String): GrammarNode {
    override fun toString(): String = "{$name}"
}