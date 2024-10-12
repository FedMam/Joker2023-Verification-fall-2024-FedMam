package me.markoutte.joker.hw1.strategies.gbf

private val ntLetter = NonTerminal("letter")
private val ntCharacter = NonTerminal("character")
private val ntName = NonTerminal("name")
private val ntText = NonTerminal("text")
private val ntSpecialCharacter = NonTerminal("specialCharacter")
private val ntAttribute = NonTerminal("attribute")
private val ntAttributes = NonTerminal("attributes")
private val ntSingleTag = NonTerminal("singleTag")
private val ntSingleTagClosed = NonTerminal("singleTagClosed")
private val ntComment = NonTerminal("comment")
private val ntContent = NonTerminal("content")
private val ntTagPair = NonTerminal("tagPair")

private val nonTerminals = setOf(
    ntLetter, ntCharacter, ntName, ntText, ntSpecialCharacter, ntAttribute, ntAttributes, ntSingleTag, ntSingleTagClosed, ntComment, ntContent, ntTagPair
)

private val rules = listOf(
    GrammarRule(ntLetter, *(('a'..'z').map { GrammarExpansion(Terminal("$it")) }).toTypedArray()),
    GrammarRule(ntCharacter, *(
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.,:;-!?()"
                .toCharArray()
                .map { GrammarExpansion(Terminal("$it")) }
                .toTypedArray()
    )),
    GrammarRule(ntName,
        GrammarExpansion(ntLetter),
        GrammarExpansion(ntLetter, ntName),
    ),
    GrammarRule(ntText,
        GrammarExpansion(),
        GrammarExpansion(ntCharacter, ntText), // a little more probability to this expansion
        GrammarExpansion(ntCharacter, ntText),
        GrammarExpansion(ntCharacter, ntText),
        GrammarExpansion(ntCharacter, ntText),
    ),
    GrammarRule(ntSpecialCharacter,
        GrammarExpansion(Terminal("&"), ntName, Terminal(";"))
    ),
    GrammarRule(ntAttribute,
        GrammarExpansion(Terminal(" "), ntName, Terminal("=\""), ntText, Terminal("\""))
    ),
    GrammarRule(ntAttributes,
        GrammarExpansion(),
        GrammarExpansion(ntAttribute, ntAttributes)
    ),
    GrammarRule(ntSingleTag,
        GrammarExpansion(Terminal("<"), ntName, ntAttributes, Terminal(">"))
    ),
    GrammarRule(ntSingleTagClosed,
        GrammarExpansion(Terminal("<"), ntName, ntAttributes, Terminal("/>"))
    ),
    GrammarRule(ntComment,
        GrammarExpansion(Terminal("<!--"), ntText, Terminal("-->"))
    ),
    GrammarRule(ntContent,
        GrammarExpansion(),
        GrammarExpansion(ntText, ntContent),
        GrammarExpansion(ntSingleTag, ntContent),
        GrammarExpansion(ntSingleTagClosed, ntContent),
        GrammarExpansion(ntComment, ntContent),
        GrammarExpansion(ntTagPair, ntContent)
    ),
    // пара тегов <tagname>...</tagname> не описывается грамматикой,
    // поэтому название тега будет из конечного множества строк длины 3
    // над алфавитом {'a', 'b', 'c'}
    GrammarRule(ntTagPair,
        *((0..26)
            .map { "${('a' + it % 3)}${('a' + (it / 3) % 3)}${('a' + it / 9)}" }
            .map { GrammarExpansion(Terminal("<$it"), ntAttributes, Terminal(">"), ntContent, Terminal("</$it>")) }
            .toTypedArray())
    )
).associateBy { it.nonTerminal }

public val defaultHTMLGrammar: Grammar = Grammar(nonTerminals, ntTagPair, rules)