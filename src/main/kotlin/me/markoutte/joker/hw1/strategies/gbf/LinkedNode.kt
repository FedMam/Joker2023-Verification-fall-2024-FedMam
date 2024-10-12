package me.markoutte.joker.hw1.strategies.gbf

class LinkedNode<E>(var item: E,
    var prev: LinkedNode<E>? = null,
    var next: LinkedNode<E>? = null) {

    fun erase() {
        prev?.next = next
        next?.prev = prev
    }

    fun replace(newNode: LinkedNode<E>) {
        prev?.next = newNode
        newNode.prev = prev
        newNode.next = next
        next?.prev = newNode
    }

    fun replaceChain(chainStart: LinkedNode<E>, chainEnd: LinkedNode<E>) {
        prev?.next = chainStart
        chainStart.prev = prev
        chainEnd.next = next
        next?.prev = chainEnd
    }

    fun replaceChain(chainStartEnd: Pair<LinkedNode<E>, LinkedNode<E>>) =
        replaceChain(chainStartEnd.first, chainStartEnd.second)

    fun mergeWithPrev(mergeFunction: (E, E) -> E): LinkedNode<E> {
        if (prev == null)
            return this

        val mergedNode = LinkedNode(mergeFunction(prev!!.item, item), prev!!.prev, next)
        prev = null
        next = null
        mergedNode.next?.prev = mergedNode
        mergedNode.prev?.next = mergedNode
        return mergedNode
    }

    companion object {
        fun <E>goAllWayLeft(node: LinkedNode<E>): LinkedNode<E> {
            var _node = node
            while (_node.prev != null)
                _node = _node.prev!!
            return _node
        }

        fun <E>chainTogether(nodes: List<LinkedNode<E>>): Pair<LinkedNode<E>, LinkedNode<E>> {
            var prevNode: LinkedNode<E>? = null
            for (node in nodes) {
                node.prev = prevNode
                prevNode?.next = node
                prevNode = node
            }
            nodes.last().next = null
            return Pair(nodes.first(), nodes.last())
        }
    }
}