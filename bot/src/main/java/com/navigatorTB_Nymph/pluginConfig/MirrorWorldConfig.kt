package com.navigatorTB_Nymph.pluginConfig

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object MirrorWorldConfig : AutoSavePluginConfig("DLC_Config") {
    @ValueDescription("攻击修正系数")
    var AttackModifier: Double by value(0.06)

    @ValueDescription("属性点汇率")
    var ExchangeRate: Int by value(10)
}