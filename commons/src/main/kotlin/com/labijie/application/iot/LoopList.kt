package com.labijie.application.iot

import kotlin.random.Random

enum class LoopPolicy {
    Sequence,
    Random
}

class LoopList<T : Any>(private val list: List<T>, private val loopPolicy: LoopPolicy = LoopPolicy.Sequence) {
    private var currentIndex: Int = -1
    private val syncRoot = Any()

    fun next(): T? {
        if (list.isEmpty()) {
            return null
        }

        if (list.size == 1) {
            currentIndex = 0
            return list[0]
        }

        var nextIndex = -1
        if (currentIndex == -1) {
            synchronized(syncRoot) {
                if (currentIndex == -1) {
                    nextIndex = if (loopPolicy == LoopPolicy.Sequence) {
                        0
                    } else {
                        Random.nextInt(0, list.size)
                    }
                    currentIndex = nextIndex
                }
            }
        } else {
            synchronized(syncRoot) {
                nextIndex = ++currentIndex
                currentIndex = if (nextIndex < list.size) {
                    nextIndex
                } else {
                    nextIndex = 0
                    nextIndex
                }
            }
        }


        return list[nextIndex]
    }

    fun current(): T? {
        if (list.isEmpty() || this.currentIndex < 0) {
            return this.next()
        }

        if (list.size == 1) {
            return list[0]
        }

        synchronized(syncRoot) {
            return list[currentIndex]
        }
    }

    fun <R> loop(action: (item: T) -> R?): R? {
        if (list.isNotEmpty()) {
            repeat(list.size){
                val item = this.next()
                if (item == null) {
                    return null
                } else {
                    val r = action.invoke(item)
                    if (r != null) {
                        return r
                    }
                }
            }
        }
        return null
    }
}