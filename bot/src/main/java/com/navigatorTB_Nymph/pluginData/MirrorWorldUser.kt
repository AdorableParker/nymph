package com.navigatorTB_Nymph.pluginData

import com.navigatorTB_Nymph.data.Role
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

// 定义插件数据
object MirrorWorldUser : AutoSavePluginData("DLC_PlayerData") { // "name" 是保存的文件名 (不带后缀)
//    @ValueDescription("每月初始化")
//    var initialization: Int by value(0)

    @ValueDescription("玩家数据")
    val userData: MutableMap<Long, Role> by value(
        mutableMapOf()
    )
}