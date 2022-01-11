/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph


import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object Birthday : SimpleCommand(
    PluginMain, "舰娘生日", "历史今天",
    description = "历史今日下水舰船"
) {
    override val usage: String = "${CommandManager.commandPrefix}舰娘生日"

    @MiraiExperimentalApi
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("M月d日"))

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
        val r = dbObject.select("ShipBirthday", "LaunchDay", today)
        dbObject.closeDB()
        if (r.isEmpty()) {
            sendMessage("今天生日的舰娘没有记载哦")
            return
        }
        for (i in r) {
            if (i["Annotate"].toString().isBlank()) {
                sendMessage("${i["LaunchYear"]}年的今天,${i["Nationality"]}${i["ShipType"]}${i["Name"]}下水于${i["Annotate"]}")
            } else {
                sendMessage("${i["LaunchYear"]}年的今天,${i["Nationality"]}${i["ShipType"]}${i["Name"]}下水")
            }
        }
    }
}