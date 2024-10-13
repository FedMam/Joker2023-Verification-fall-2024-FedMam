package me.markoutte.joker.hw1.strategies.gbf

import me.markoutte.joker.hw1.strategies.FuzzingStrategy
import java.nio.ByteBuffer

class GrammarBasedFuzzing(val grammar: Grammar): FuzzingStrategy() {
    override val defaultBufferSize: Int
        get() = 510

    private fun ByteBuffer.getLooping(): Byte {
        if (!hasRemaining())
            rewind()
        return get()
    }

    private fun <E>getItemByBufferValue(bufferValue: Byte, lst: List<E>) =
        if (lst.isEmpty()) null
        else lst[(bufferValue.toUByte().toInt()) % lst.size]

    val MAX_NT_EXPANSIONS = 500
    val MAX_LOOP_ITERS_ALLOWED = 10000

    override fun generateString(buffer: ByteBuffer): String {
        var parseTreeZipper = LinkedNode<GrammarNode>(grammar.startNonTerminal)

        var nonTerminalsLeft = true
        var ntExpansions = MAX_NT_EXPANSIONS
        var iterations = 0
        while (nonTerminalsLeft) {
            nonTerminalsLeft = false
            iterations++

            while (true) {
                iterations++
                if (iterations > MAX_LOOP_ITERS_ALLOWED)
                    return "Error: stuck in an infinite loop"

                if (parseTreeZipper.item is Terminal &&
                    parseTreeZipper.prev?.item is Terminal) {
                    parseTreeZipper = parseTreeZipper.mergeWithPrev { node1, node2 ->
                        Terminal(node1.toString() + node2.toString())
                    }
                }

                if (parseTreeZipper.item is NonTerminal) {
                    nonTerminalsLeft = true
                    val expansion: GrammarExpansion?
                    if (ntExpansions > 0 || grammar.rules[parseTreeZipper.item]?.terminalExpansions?.isEmpty() == true) {
                        expansion = getItemByBufferValue(
                            buffer.getLooping(),
                            grammar.rules[parseTreeZipper.item]?.expansions ?: listOf()
                        )
                        ntExpansions = maxOf(0, ntExpansions - 1)
                    } else {
                        expansion = getItemByBufferValue(
                            buffer.getLooping(),
                            grammar.rules[parseTreeZipper.item]?.terminalExpansions ?: listOf()
                        )
                    }

                    if (expansion == null || expansion.content.isEmpty()) {
                        val oldNode = parseTreeZipper
                        if (parseTreeZipper.next == null) {
                            if (parseTreeZipper.prev == null)
                                return ""
                            parseTreeZipper.prev!!.next = null
                            parseTreeZipper = LinkedNode.goAllWayLeft(parseTreeZipper.prev!!)
                            break
                        }
                        parseTreeZipper = parseTreeZipper.next!!
                        oldNode.erase()
                    } else {
                        val (chainStart, chainEnd) = LinkedNode.chainTogether(
                            expansion.content.map { LinkedNode(it) }
                        )

                        val oldNode = parseTreeZipper
                        if (parseTreeZipper.next == null) {
                            parseTreeZipper.replaceChain(chainStart, chainEnd)
                            parseTreeZipper = LinkedNode.goAllWayLeft(chainStart)
                            break
                        }
                        parseTreeZipper = parseTreeZipper.next!!
                        oldNode.replaceChain(chainStart, chainEnd)
                    }
                } else {
                    if (parseTreeZipper.next == null) {
                        parseTreeZipper = LinkedNode.goAllWayLeft(parseTreeZipper)
                        break
                    }
                    parseTreeZipper = parseTreeZipper.next!!
                }
            }
        }

        var resultString = StringBuilder()
        var parseTreeNode: LinkedNode<GrammarNode>? = parseTreeZipper
        while (parseTreeNode != null) {
            val oldNode = parseTreeNode
            parseTreeNode = parseTreeNode.next
            resultString.append(oldNode.item.toString())
            oldNode.erase()
        }
        return resultString.toString()
    }
}