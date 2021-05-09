package com.labijie.application.io.testing

import com.labijie.application.iot.LoopList
import com.labijie.application.iot.LoopPolicy
import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class LoopListTester {
    @Test
    fun testLoopSequence() {
        val list = LoopList(listOf(1, 2, 3, 4, 5))
        val array = arrayOfNulls<Int>(12)

        repeat(array.size) {
            array[it] = list.next()
        }

        Assertions.assertArrayEquals(arrayOf(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2), array)
    }


    @Test
    fun testLoopRandom() {
        val list = LoopList(listOf(1, 2, 3, 4, 5), LoopPolicy.Random)
        val array = arrayOfNulls<Int>(12)

        repeat(array.size) {
            array[it] = list.next()
        }

        val first = array.first()
        Assertions.assertTrue(first in 1..5)

        array.reduce { acc, i ->
            val prev = i!! - 1
            Assertions.assertTrue(acc == prev || (i == 1 && acc == 5))
            i
        }
    }
}