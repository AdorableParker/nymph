/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.isUser
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.utils.debug


object Request : SimpleCommand(
    PluginMain, "授权批准",
    description = "加群申请处理"
) {
    @Handler
    suspend fun CommandSenderOnMessage<MessageEvent>.main(groupID: Long) {
        if (isUser() && user.id == MySetting.AdminID) {
            MyPluginData.groupIdList[groupID] = GroupCertificate()
            sendMessage("OK")
            PluginMain.logger.debug { MyPluginData.groupIdList.keys.toString() }
        } else {
            sendMessage("权限不足")
        }
    }
}