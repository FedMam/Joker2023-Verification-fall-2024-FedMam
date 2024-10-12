package me.markoutte.joker.hw1.strategies.gbf

class GrammarExpansion(vararg cntnt: GrammarNode) {
    val content = cntnt.toList()
    val isTerminal = content.all { it is Terminal }
}