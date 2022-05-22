package com.nymph_TB_DLC

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class BattleRecordTest {


    @BeforeEach
    fun write() {
        BattleRecord().write("0D00", "12345")
        BattleRecord().write("0D00", "67890")
        BattleRecord().write("0D00", Pair("测试A", "测试B"), Pair(10, 20), Pair(30, 40))
    }

    @Test
    fun read() {
        val out1 = "第1回合:12345,67890。\n"
        val out2 = "无战斗记录"
        assertAll("读取测试",
            { assertEquals(out1, BattleRecord().read("0D00")) },
            { assertEquals(out2, BattleRecord().read("0721")) }
        )
    }

    @Test
    fun readResults() {
        val out1 = "测试A取得本次战斗胜利,获得10经验,获得30金币\n测试B获得20经验,失去40金币"
        val out2 = "无战果记录"
        assertAll("读取测试",
            { assertEquals(out1, BattleRecord().readResults("0D00")) },
            { assertEquals(out2, BattleRecord().readResults("0721")) }
        )
    }
}