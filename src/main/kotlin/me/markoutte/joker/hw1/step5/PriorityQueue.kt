package me.markoutte.joker.hw1.step5

import java.lang.IllegalStateException
import kotlin.random.Random

// я должен был написать свой класс PriorityQueue,
// так как класс из стандартной библиотеки Java не подходил
class PriorityQueue<E>(private val comparator: Comparator<E>): Collection<E> {
    // DEBUG
    public val heap = mutableListOf<E>()

    private fun swap(index1: Int, index2: Int) {
        val temp = heap[index1]
        heap[index1] = heap[index2]
        heap[index2] = temp
    }

    private fun getPrevIndex(index: Int): Int = (index - 1) / 2

    private fun getNextIndices(index: Int): Pair<Int, Int> =
        Pair(2 * index + 1, 2 * index + 2)

    private operator fun E.compareTo(other: E): Int {
        return comparator.compare(this, other)
    }

    private fun heapifyUp(index: Int) {
        if (index == 0) return

        val prevIndex = getPrevIndex(index)
        if (heap[index] < heap[prevIndex]) {
            swap(index, prevIndex)
            heapifyUp(prevIndex)
        }
    }

    private fun heapifyDown(index: Int) {
        val (nextIndex1, nextIndex2) = getNextIndices(index)
        if (nextIndex1 >= heap.size) return
        else if (nextIndex2 >= heap.size) {
            if (heap[index] > heap[nextIndex1]) {
                swap(index, nextIndex1)
                heapifyDown(nextIndex1)
            }
        } else {
            if (heap[nextIndex1] < heap[nextIndex2]) {
                if (heap[index] > heap[nextIndex1]) {
                    swap(index, nextIndex1)
                    heapifyDown(nextIndex1)
                }
            } else {
                if (heap[index] > heap[nextIndex2]) {
                    swap(index, nextIndex2)
                    heapifyDown(nextIndex2)
                }
            }
        }
    }

    fun push(item: E) {
        val index = heap.size
        heap.add(item)
        heapifyUp(index)
    }

    fun peek(): E {
        if (heap.isEmpty())
            throw IndexOutOfBoundsException("Priority Queue is empty")

        return heap[0]
    }

    fun tryPeek(): E? = if(heap.isEmpty()) null else heap[0]

    fun poll(): E {
        if (heap.isEmpty())
            throw IndexOutOfBoundsException("Priority Queue is empty")

        val removedItem = heap[0]
        heap[0] = heap.removeLast()
        heapifyDown(0)
        return removedItem
    }

    operator fun get(index: Int): E = heap[index]

    fun removeAt(index: Int): E {
        if (index < 0 || index >= heap.size)
            throw IndexOutOfBoundsException("Priority Queue: index $index out of bounds for size ${heap.size}")

        val removedItem = heap[index]
        if (index < heap.size - 1) {
            heap[index] = heap.removeLast()
            heapifyUp(index)
            heapifyDown(index)
        } else heap.removeLast()
        return removedItem
    }

    fun getRandom(rand: Random): E {
        if (heap.isEmpty())
            throw IndexOutOfBoundsException("Priority Queue is empty")

        val randomIndex = rand.nextInt(heap.size)
        return heap[randomIndex]
    }

    fun pollRandom(rand: Random): E {
        if (heap.isEmpty())
            throw IndexOutOfBoundsException("Priority Queue is empty")

        val randomIndex = rand.nextInt(heap.size)
        return removeAt(randomIndex)
    }

    override val size: Int
        get() = heap.size

    override fun isEmpty(): Boolean = heap.isEmpty()

    override fun iterator(): Iterator<E> = heap.iterator()

    override fun containsAll(elements: Collection<E>): Boolean =
        heap.containsAll(elements)

    override fun contains(element: E): Boolean =
        heap.contains(element)

    fun validateQueue() {
        for (i in heap.indices) {
            val (n1, n2) = getNextIndices(i)
            if (n1 < heap.size) {
                if (heap[i] > heap[n1])
                    throw IllegalStateException("Priority Queue not working properly")
            }
            if (n2 < heap.size) {
                if (heap[i] > heap[n2])
                    throw IllegalStateException("Priority Queue not working properly")
            }
        }
    }
}