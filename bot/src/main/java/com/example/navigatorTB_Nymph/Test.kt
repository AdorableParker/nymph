/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.isUser
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiExperimentalApi

@Suppress("UNUSED_PARAMETER") // 忽略未使用的参数警告
@MiraiExperimentalApi
@ConsoleExperimentalApi
object Test : SimpleCommand(
    PluginMain, "Test", "测试",
    description = "功能测试命令"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (isUser() && user.id == MySetting.AdminID) {
            sendMessage("Hi,Administration")
        } else {
            sendMessage("权限不足")
        }
    }
}

