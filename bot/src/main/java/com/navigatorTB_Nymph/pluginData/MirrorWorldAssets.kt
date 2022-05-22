package com.navigatorTB_Nymph.pluginData

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object MirrorWorldAssets : AutoSavePluginData("DLC_AssetsData") { // "name" 是保存的文件名 (不带后缀)
//    @ValueDescription("技能列表")
//    val skillList: MutableMap<String, String> by value(
//        mutableMapOf(
//            "[招架]" to "50%的概率降低30%的物理伤害",
//            "[回复]" to "每次攻击有30%几率回复已损失生命值的10%",
//            "[闪避]" to "20%的概率闪避任何伤害",
//            "[附魔]" to "物理攻击的50%作为法术伤害计算",
//            "[皇室荣光]" to "吸收两倍于自身等级值的伤害"
////            "[钞能力]" to "通过花费金币改变判定结果",
//        )
//    )

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
                    "-被通缉者-" to Triple(0.1, 0.0, "金币花费+20%"),
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

