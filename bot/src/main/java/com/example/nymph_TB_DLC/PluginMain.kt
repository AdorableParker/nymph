/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:29
 */

package com.example.nymph_TB_DLC


import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiExperimentalApi

@MiraiExperimentalApi
@ConsoleExperimentalApi
object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "MCP.TB_DLC",
        name = "TB_DLC-MirrorWorld",
        version = "0.0.1"
    )
) {

    override fun onEnable() {
        // 从数据库自动读
        MirrorWorldUser.reload()
        MirrorWorldAssets.reload()
    }

    override fun onDisable() {
        PluginMain.cancel()
    }
}