package me.markoutte.joker.hw1.strategies.gbf

class Grammar(
    val nonTerminals: Set<NonTerminal>,
    val startNonTerminal: NonTerminal,
    val rules: Map<NonTerminal, GrammarRule>
)