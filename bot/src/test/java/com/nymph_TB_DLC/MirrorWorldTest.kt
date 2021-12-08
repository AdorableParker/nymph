package com.nymph_TB_DLC

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.math.roundToInt


internal class MirrorWorldTest {

    private fun getArray(a: Int, b: Int, c: Int, d: Int): Array<Int> {
        while (true) {
            val i = Array(c) {
                (a..b).random()
            }
            if (i.sum() <= d) return i
        }
    }


    @Order(1)
    @RepeatedTest(10)
    fun gamerInfo() {
        val role1 = Priest("牧师")
        val role2 = Knight("骑士")
        val a1 = arrayOf(2, 1, 1, 3, 4, 2)
        val a2 = getArray(0, 7, 6, 22)
        role1.set6D(a1)
        role2.set6D(a2)
        role1.newRole(arrayOf(2, 2, 2))
        role2.newRole(arrayOf(1, 3, 3))

        MirrorWorldUser.userRole[12357L] = role1

        val out1 = """
        拥有Pt: 0
        ==========
        玩家角色:牧师
        等级:1  金币:0枚
        HP:139/139  MP:108/108
        ATK:15    MAT:13
        TPA:5
        经验:0/1
        闲置技能点:2
        拥有特质:
        拥有技能:
        ------六维加点------
        力量:${a1[0] + 1}    法力:${a1[1] + 1}
        智力:${a1[2] + 1}    体质:${a1[3] + 1}
        速度:${a1[4] + 1}    运气:${a1[5] + 1}""".trimIndent()

        val out2 = """
        拥有Pt: 0
        ==========
        玩家角色:角色未建立""".trimIndent()
        assertAll("outInfo",
            { assertEquals(out1, MirrorWorldUser.outInfo(12357L)) },
            { assertEquals(out2, MirrorWorldUser.outInfo(1235813L)) }
        )
    }

    @Order(2)
    @RepeatedTest(10)
    fun characterCreation() {
        for (i in 0..20) {
            val role1 = Priest("牧师")
            val role2 = Knight("骑士")
            val role3 = Hunter("猎手")
            val role4 = Wizard("法师")
            val role5 = Unemployed("无职者")
            role1.set6D(getArray(0, 7, 6, 22))
            role2.set6D(getArray(0, 7, 6, 22))
            role3.set6D(getArray(0, 7, 6, 22))
            role4.set6D(getArray(0, 7, 6, 22))
            role5.set6D(getArray(0, 7, 6, 22))
            role1.newRole(getArray(1, 4, 3, 4))
            role2.newRole(getArray(1, 4, 3, 4))
            role3.newRole(getArray(1, 4, 3, 4))
            role4.newRole(getArray(1, 4, 3, 4))
            role5.newRole(getArray(1, 4, 3, 6))
        }
        val v1 = arrayOf(5, 4, 5, 3, 0, 3)
        val v2 = arrayOf(6, 1, 4, 4, 0, 0)
        val v3 = arrayOf(3, 2, 2, 0, 6, 2)
        val v4 = arrayOf(3, 6, 5, 1, 3, 1)
        val v5 = arrayOf(7, 2, 0, 6, 3, 2)
        val v6 = arrayOf(2, 7, 2, 1, 5, 4)

        val out1 = Unemployed("无职者")
        out1.set6D(v1)
        val out2 = Unemployed("无职者")
        out2.set6D(v2)
        val out3 = Unemployed("无职者")
        out3.set6D(v3)
        val out4 = Unemployed("无职者")
        out4.set6D(v4)
        val out5 = Unemployed("无职者")
        out5.set6D(v5)
        val out6 = Unemployed("无职者")
        out6.set6D(v6)

        assertEquals("方案有效,生成角色属性预览如下\n${out1.show()}", Tool(v1).show())
        assertEquals("方案有效,生成角色属性预览如下\n${out2.show()}", Tool(v2).show())
        assertEquals("方案有效,生成角色属性预览如下\n${out3.show()}", Tool(v3).show())
        assertEquals("方案有效,生成角色属性预览如下\n${out4.show()}", Tool(v4).show())
        assertEquals("方案有效,生成角色属性预览如下\n${out5.show()}", Tool(v5).show())
        assertEquals("方案有效,生成角色属性预览如下\n${out6.show()}", Tool(v6).show())
    }

    @Order(3)
    @RepeatedTest(20)
    @Timeout(2)
    fun pvp() {
        val roleList1 = arrayOf(Priest("牧师A"), Knight("骑士A"), Hunter("猎手A"), Wizard("法师A"), Unemployed("无职者A"))
        for (role in roleList1) {
            role.set6D(getArray(0, 7, 6, 22))
            role.newRole(getArray(1, 2, 3, 6))
            role.giveGold(100)
        }

        val roleList2 = arrayOf(Priest("牧师B"), Knight("骑士B"), Hunter("猎手B"), Wizard("法师B"), Unemployed("无职者B"))
        for (role in roleList2) {
            role.set6D(getArray(0, 7, 6, 22))
            role.newRole(getArray(1, 2, 3, 6))
            role.giveGold(100)
        }

        for (i in 1..4) {
            println(arrayOf("训练场", "切磋场", "死斗场", "自定义场")[i - 1])
            for (red in roleList1) {
                for (blue in roleList2) {
                    MirrorWorldUser.userData[3328L] = PermanentData()
                    MirrorWorldUser.userData[1825L] = PermanentData()
                    MirrorWorldUser.userRole[3328L] = red
                    MirrorWorldUser.userRole[1825L] = blue
                    pk(i, red, blue)

                    red.hp.treatment(9999)
                    blue.hp.treatment(9999)
                }
            }
        }
    }

    private fun pk(mods: Int, red: GameRole, blue: GameRole) {
        val logID = when (mods) {
            1 -> {
                MirrorWorld().drillBattleSequence(red, blue)
            }
            2 -> {
                MirrorWorld().battleSequence(red, blue)
            }
            3 -> {
                MirrorWorld().battleSequence(red, 114514L, blue, 1919810L)
            }
            4 -> {
                val bl = (0..90).random() * 0.01
                MirrorWorld().battleSequence(red, blue, bl)
            }
            else -> return
        }
        println("=== $logID ===")
        println(BattleRecord().readResults(logID))
        println(BattleRecord().read(logID))
        println(MirrorWorldUser.outInfo(114514L))
        println(MirrorWorldUser.outInfo(1919810L))
    }


    @AfterEach
    fun tearDown() {
        MirrorWorldUser.userData.clear()
        MirrorWorldUser.userRole.clear()
    }

    @Test
    fun transfer() {
//        val amount = (0..100).random()
        val amount = 100
        val userRole = MirrorWorldUser.userRole[114514L] ?: return
        val blueRole = MirrorWorldUser.userRole[1919810L] ?: return

//        val gold = (110..200).random()
        val gold = 120
        userRole.giveGold(gold)

        trans(userRole, blueRole, amount)

        assertAll("余额测试",
            { assertEquals(100, blueRole.showGold()) }, // 对方账户应该有amount枚金币
            { assertEquals(18, userRole.showGold()) }, // 我方账户应该有 gold- amount-v枚金币
            { assertEquals("转账成功,本次操作收取手续费1枚", trans(userRole, blueRole, 15)) },
            { assertEquals("账户金额不足,转账失败", trans(userRole, blueRole, 15)) },
            { assertEquals(false, userRole.loseGold(5)) }
        )
    }

    private fun trans(userRole: GameRole, blueRole: GameRole, amount: Int): String {
        val v = (amount * 1.01 + 1).roundToInt()
        return if (userRole.transfer(blueRole, v)) {
            blueRole.loseGold(v - amount)
            "转账成功,本次操作收取手续费${v - amount}枚"
        } else "账户金额不足,转账失败"
    }

    @BeforeEach
    fun setUp() {
        MirrorWorldUser.userData[114514L] = PermanentData()
        MirrorWorldUser.userRole[114514L] = Knight("骑士A")

        MirrorWorldUser.userData[1919810L] = PermanentData()
        MirrorWorldUser.userRole[1919810L] = Wizard("法师B")
    }
}