/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:29
 */

package com.example.nymph_TB_DLC


import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiExperimentalApi

@MiraiExperimentalApi
@ConsoleExperimentalApi
object DLC : KotlinPlugin(
    JvmPluginDescription(
        id = "MCP.TB_DLC",
        version = "0.0.1",
        name = "name"
    ) { dependsOn("MCP.navigatorTB_Nymph", "0.15.0") }) {

    override fun onEnable() {
        // 从数据库自动读
        MirrorWorldUser.reload()
        MirrorWorldAssets.reload()

        MirrorWorldInfo.register()  //用户信息
        MirrorWorldBuild.register() //建立角色
    }

    override fun onDisable() {
        DLC.cancel()
    }
}

@OptIn(MiraiExperimentalApi::class, ConsoleExperimentalApi::class)
object MirrorWorldInfo : SimpleCommand(
    DLC, "我的信息",
    description = "用户信息"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        sendMessage("测试通过")
        val userData = MirrorWorldUser.userPermanent.getOrPut(user.id) {
            PermanentData()
        }
        sendMessage(userData.outInfo())
    }
}

@OptIn(MiraiExperimentalApi::class, ConsoleExperimentalApi::class)
object MirrorWorldBuild : SimpleCommand(
    DLC, "建立角色",
    description = "玩家角色建立"
) {
    @Handler

    suspend fun MemberCommandSenderOnMessage.main() {

        sendMessage("测试通过")
        val userData = MirrorWorldUser.userPermanent.getOrPut(user.id) {
            PermanentData()
        }
        if (userData.pc == null) {
            userData.pc = PlayerCharacter()
        } else {
            sendMessage("已存在角色,是否覆盖存档？")
        }
        //TODO:需要支持上下文命令
    }
}