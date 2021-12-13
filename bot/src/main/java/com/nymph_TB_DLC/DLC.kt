/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:29
 */

package com.nymph_TB_DLC


import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin


object DLC : KotlinPlugin(
    JvmPluginDescription(
        id = "MCP.TB_DLC",
        version = "0.1.0",
        name = "TB_DLC-MirrorWorld"
    ) { dependsOn("MCP.navigatorTB_Nymph", "0.16.0") }) {

    override fun onEnable() {
        MirrorWorldUser.reload()
        MirrorWorldAssets.reload()
    }

    override fun onDisable() {
        DLC.cancel()
    }
}