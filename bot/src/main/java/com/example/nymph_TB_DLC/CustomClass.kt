package com.example.nymph_TB_DLC

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

// 定义插件数据
object MirrorWorldUser : AutoSavePluginData("DLC_PlayerData") { // "name" 是保存的文件名 (不带后缀)
    @ValueDescription("每月初始化")
    var initialization: Int by value(0)

    @ValueDescription("玩家数据")
    val userPermanent: MutableMap<Long, PermanentData> by value(
        mutableMapOf()
    )
}

object MirrorWorldAssets : AutoSavePluginData("DLC_AssetsData") { // "name" 是保存的文件名 (不带后缀)
    @ValueDescription("技能列表")
    val skillList: MutableMap<String, String> by value(
        mutableMapOf(
            "[招架]" to "50%的概率降低30%的物理伤害",
            "[回复]" to "每3回合恢复已损失生命值的20%",
            "[闪避]" to "20%的概率闪避任何伤害",
            "[附魔]" to "物理攻击的50%作为法术伤害计算",
            "[钞能力]" to "通过花费金币改变判定结果",
            "[皇室荣光]" to "可以终止对战或拒绝终止对战"
        )
    )

    @ValueDescription("加成特性")
    val PositiveCorrection: Map<String, MutableMap<String, Triple<Double, Double, String>>> by value(
        mapOf(
            "All" to mutableMapOf(),
            "Gold" to mutableMapOf(
                "-精打细算-" to Triple(0.5, 0.0, "金币收入+50%"),
                "-被剥削者-" to Triple(0.0, 0.6, "金币收入-60%")
            ),
            "Exp" to mutableMapOf(
                "-高人指路-" to Triple(0.2, 0.0, "经验获取+20%")
            ),
            "bonus" to mutableMapOf(
                "-被通缉者-" to Triple(0.0, 1.5, "所有惩罚+50%"),
                "-被传颂者-" to Triple(1.2, 0.8, "所有惩罚-20%,所有奖励+20%"),
                "-名人效应-" to Triple(1.5, 1.5, "所有奖励和惩罚加成+50%")
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