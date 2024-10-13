package me.markoutte.joker.hw1.strategies.gbf

class GrammarRule(
    val nonTerminal: NonTerminal,
    vararg exps: GrammarExpansion
) {
    val expansions = exps.toList()
    val terminalExpansions = exps.filter { it.isTerminal }
}