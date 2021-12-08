package com.nymph_TB_DLC

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

// 定义插件数据
object MirrorWorldUser : AutoSavePluginData("DLC_PlayerData") { // "name" 是保存的文件名 (不带后缀)
    @ValueDescription("每月初始化")
    var initialization: Int by value(0)

    @ValueDescription("玩家数据")
    val userData: MutableMap<Long, PermanentData> by value(
        mutableMapOf()
    )

    @ValueDescription("玩家数据")
    val userRole: MutableMap<Long, GameRole> by value(
        mutableMapOf()
    )

    fun outInfo(uid: Long): String {
        val data = userData.getOrPut(uid) { PermanentData() }
        val role = userRole[uid]
        return """
                |拥有Pt: ${data.pt}
                |==========
                |玩家角色:${role?.info() ?: "角色未建立"}
                """.trimMargin("|")

    }
}

object MirrorWorldAssets : AutoSavePluginData("DLC_AssetsData") { // "name" 是保存的文件名 (不带后缀)
    @ValueDescription("技能列表")
    val skillList: MutableMap<String, String> by value(
        mutableMapOf(
            "[招架]" to "50%的概率降低30%的物理伤害",
            "[回复]" to "每次攻击有30%几率回复已损失生命值的10%",
            "[闪避]" to "20%的概率闪避任何伤害",
            "[附魔]" to "物理攻击的50%作为法术伤害计算",
            "[皇室荣光]" to "吸收两倍于自身等级值的伤害"
//            "[钞能力]" to "通过花费金币改变判定结果",
        )
    )

    @ValueDescription("加成特性")
    val PositiveCorrection: Pair<
            Map<String, MutableMap<String, Triple<Double, Double, String>>>,
            Map<String, MutableMap<String, Triple<Double, Double, String>>>> by value(
        Pair(
            mapOf(
                "Gold" to mutableMapOf(
                    "-被剥削者-" to Triple(0.0, 0.6, "金币收入-60%"),
                    "-精打细算-" to Triple(0.2, 0.0, "金币收入+20%"),
                    "-被传颂者-" to Triple(0.2, 0.0, "金币支出-20%,金币经验收入+20%")
                ),
                "Exp" to mutableMapOf(
                    "-天资聪颖-" to Triple(0.25, 0.0, "经验获取+25%"),
                    "-高人指路-" to Triple(0.2, 0.0, "经验获取+20%"),
                    "-被传颂者-" to Triple(0.2, 0.0, "所有支出-20%,所有收入+20%")
                ),
                "bonus" to mutableMapOf(
                    "-名人效应-" to Triple(1.5, 1.5, "所有加成影响+50%")
                )
            ),
            mapOf(
                "Gold" to mutableMapOf(
                    "-被通缉者-" to Triple(0.5, 0.0, "金币花费+50%"),
                    "-被传颂者-" to Triple(0.0, 0.2, "金币支出-20%,金币经验收入+20%"),
                ),
                "Exp" to mutableMapOf(
                    "-天生愚钝-" to Triple(0.25, 0.0, "经验获取-25%")
                ),
                "bonus" to mutableMapOf(
                    "-名人效应-" to Triple(1.5, 1.5, "所有加成影响+50%")
                )
            )
        )
    )
}

object MirrorWorldConfig : AutoSavePluginConfig("DLC_Config") {
    @ValueDescription("攻击修正系数")
    var AttackModifier: Double by value(0.06)

    @ValueDescription("属性点汇率")
    var ExchangeRate: Int by value(10)
}

object CharacterLineDictionary : AutoSavePluginConfig("DLC_CLD") {
    @ValueDescription("通用攻击台词")
    var AttackLine: Array<String> by value(
        arrayOf(
            "进攻了",
            "打了一套王八拳",
            "手一滑,武器飞了出去",
            "不知道从什么地方掏了枚臭鸡蛋丢了过去",
            "带着一道刀光冲了过去",
            "大喊一声'偷袭',并掏出了手枪",
        )
    )

    @ValueDescription("通用防御台词")
    var DefenseLine: Array<String> by value(
        arrayOf(
            "接中了",
            "吃下了这顿",
            "结结实实的挨了这一下",
            "硬吃了这次攻击",
            "没能够躲开",
            "抱头蹲防"
        )
    )

    @ValueDescription("法师攻击台词")
    var WizardAttackLine: Array<String> by value(
        arrayOf(
            "的法球发出了耀眼的闪光",
            "搓出了一发火球",
            "喊出-奥术飞弹-,却释放了落雷术",
            "丢出了一堆不稳定法力水晶",
            "高举法杖:Explosion!! ",
            "把水晶球当板砖丢了出去"
        )
    )

}