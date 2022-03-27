package com.navigatorTB_Nymph.command.composite

import com.navigatorTB_Nymph.game.duel.Gun
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.MyPluginData
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import java.time.Instant

object Duel : CompositeCommand(
    PluginMain, "Duel", "决斗",
    description = "禁言决斗，用于普通群员与普通群员之间解决冲突"
) {
    @SubCommand("发起")
    suspend fun MemberCommandSenderOnMessage.main(target: Member) {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法使用本功能")
            return
        }
        if (user == target) {
            sendMessage("请不要左右互搏")
            return
        }

        if (PluginMain.BOTH_SIDES_DUEL.containsKey(user) || PluginMain.BOTH_SIDES_DUEL.containsKey(target)) {
            sendMessage("你或对方正在决斗中，不能发起新的决斗")
            return
        }

        SQLiteJDBC(PluginMain.resolveDataPath("User.db"))

        val coolDownTime = Instant.now().epochSecond - MyPluginData.duelTime.getOrDefault(group.id, 0)
        if (coolDownTime <= 300L) {
            sendMessage("决斗场占用中，请等待${300 - coolDownTime}秒")
            return
        }
        MyPluginData.duelTime[group.id] = Instant.now().epochSecond
        sendMessage("${user.nameCardOrNick}发起了对${target.nameCardOrNick}的决斗")

        PluginMain.BOTH_SIDES_DUEL[user] = Gun(target)
        PluginMain.BOTH_SIDES_DUEL[target] = Gun(user)

        if (PluginMain.BOTH_SIDES_DUEL[user]?.shot(group) == true) {
            PluginMain.BOTH_SIDES_DUEL.remove(user)
            PluginMain.BOTH_SIDES_DUEL.remove(target)
        } else {
            sendMessage(At(target) + PlainText("轮到你了,反击！"))
        }
    }

    @SubCommand("射击")
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法使用本功能")
            return
        }

        if (PluginMain.BOTH_SIDES_DUEL.containsKey(user)) { // 判断有无进行中的决斗
            if (PluginMain.BOTH_SIDES_DUEL[user]?.shot(group) == true) {  // 进行射击 判断射击是否命中
                PluginMain.BOTH_SIDES_DUEL[user]?.let { PluginMain.BOTH_SIDES_DUEL.remove(it.adversary) }
                PluginMain.BOTH_SIDES_DUEL.remove(user)
            } else {  // 未命中
                PluginMain.BOTH_SIDES_DUEL[user]?.let {
                    sendMessage(At(it.adversary) + PlainText("轮到你了,反击！"))
                }
            }
        } else {
            sendMessage("你没有正在进行的决斗，无法射击")
        }
    }


}