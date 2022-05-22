package com.nymph_TB_DLC

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class BarTest {

    @Test
    fun harm() {
        val a = Bar(100)

        assertAll(
            "伤害测试",
            { assertEquals(true, a.harm(70)) },
            { assertEquals(false, a.harm(60)) }
        )
    }

    @Test
    fun treatment() {
        val a = Bar(100)
        a.current = 15
        assertAll(
            "伤害测试",
            { assertEquals(85, a.treatment(70)) },
            { assertEquals(100, a.treatment(60)) }
        )
    }
}