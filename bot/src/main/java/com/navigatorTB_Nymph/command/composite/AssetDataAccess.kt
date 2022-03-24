package com.navigatorTB_Nymph.command.composite

import com.navigatorTB_Nymph.main.PluginMain
import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.isUser
import net.mamoe.mirai.event.events.MessageEvent

object AssetDataAccess : CompositeCommand(
    PluginMain, "AssetDataAccess", "写入资产",
    description = "资产数据库写入操作"
) {
    override val usage: String = "${CommandManager.commandPrefix}写入资产 [建造时间|和谐名]"

    @SubCommand("建造时间")
    suspend fun CommandSenderOnMessage<MessageEvent>.main(
        shipName: String,
        alias: String,
        time: String,
        limitedTime: Boolean
    ) {
        if (isUser() && user.id == MySetting.AdminID) {
            val limited = if (limitedTime) "1.0" else "0.0"
            val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
            dbObject.insert(
                "AzurLane_construct_time",
                arrayOf("OriginalName", "Alias", "Time", "LimitedTime"),
                arrayOf("\"$shipName\"", "\"$alias\"", "\"$time\"", limited)
            )
            dbObject.closeDB()
            sendMessage("写入完成")
        } else {
            sendMessage("权限不足")
        }
    }

    @SubCommand("和谐名")
    suspend fun CommandSenderOnMessage<MessageEvent>.main(shipName: String, alias: String) {
        if (isUser() && user.id == MySetting.AdminID) {
            val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
            dbObject.insert(
                "Roster",
                arrayOf("code", "name"),
                arrayOf("\"$shipName\"", "\"$alias\"")
            )
            dbObject.closeDB()
            sendMessage("写入完成")
        } else {
            sendMessage("权限不足")
        }
    }
}