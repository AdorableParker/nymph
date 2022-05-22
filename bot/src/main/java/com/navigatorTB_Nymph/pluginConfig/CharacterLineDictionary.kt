package com.navigatorTB_Nymph.pluginConfig

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

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