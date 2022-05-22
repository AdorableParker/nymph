package com.navigatorTB_Nymph.pluginData

import com.navigatorTB_Nymph.command.dlc.data.PermanentData
import com.navigatorTB_Nymph.command.dlc.gameRole.GameRole
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

// 定义插件数据
object MirrorWorldUser : AutoSavePluginData("DLC_PlayerData") { // "name" 是保存的文件名 (不带后缀)
//    @ValueDescription("每月初始化")
//    var initialization: Int by value(0)

    @ValueDescription("玩家数据")
    val userData: MutableMap<Long, PermanentData> by value(
        mutableMapOf()
    )

    @ValueDescription("玩家角色数据")
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