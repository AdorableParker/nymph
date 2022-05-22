package com.navigatorTB_Nymph.pluginData

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Alchemy : AutoSavePluginData("DLC_AlchemyData") { // "name" 是保存的文件名 (不带后缀)
    /** 配方库数据
     *
     * 物品id: 配方原料
     */
    @ValueDescription("配方库")
    val formula: MutableMap<String, MutableList<String>> by value(
        mutableMapOf()
    )
}