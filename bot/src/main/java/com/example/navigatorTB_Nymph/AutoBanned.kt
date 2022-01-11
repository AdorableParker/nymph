/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator


object AutoBanned : SimpleCommand(
    PluginMain, "AutoBanned", "自助禁言", "睡眠套餐",
    description = "用于解决群员的自闭需求"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main(durationSeconds: Int) {
        record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法使用本功能")
            return
        }
        runCatching {
            if (durationSeconds != 0) {
                user.mute(durationSeconds)
            }
        }.onSuccess {
            sendMessage("您的套餐已到，请注意查收。")
        }.onFailure {
            sendMessage("嘤嘤嘤，在本群权限不足")
        }
    }

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(MemberTarget: Member, durationSeconds: Int) {
        record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法使用本功能")
            return
        }
        if (user.permission.isOperator()) {
            runCatching {
                if (durationSeconds != 0) {
                    MemberTarget.mute(durationSeconds)
                }
            }.onSuccess {
                sendMessage("您的套餐已到，请注意查收。")
            }.onFailure { sendMessage("嘤嘤嘤，TB在本群权限不足") }
        } else sendMessage("权限不足,爬👇")
    }
}