package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.data.AssetDataWordle
import com.navigatorTB_Nymph.data.Role
import com.navigatorTB_Nymph.game.guessWord.GuessWord
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.MirrorWorldUser
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.syncFromEvent
import net.mamoe.mirai.message.data.content

object Wordle : SimpleCommand(
    PluginMain, "Wordle", "猜单词",
    description = "猜单词"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
        val ctAn = AssetDataWordle(dbObject.selectRandom("Wordle", "猜单词\nFile:Wordle.kt\tLine:32")).word
        dbObject.closeDB()

        val doc = GuessWord(ctAn)
        group.sendImage(doc.draw())
        var line = 1
        while (line <= 6) {
            val inStr = withTimeoutOrNull(60_000) {
                GlobalEventChannel.syncFromEvent<GroupMessageEvent, String>(EventPriority.MONITOR) {
                    if (group == it.group) {
                        it.message.content.trim().let { str ->
                            if (Regex("^[a-zA-z]{5}$").matches(str)) str.trim().uppercase() else null
                        }
                    } else null
                }
            }
            if (inStr.isNullOrEmpty()) {
                sendMessage("回答超时，游戏结束...")
                return
            }
            val dbObj = SQLiteJDBC(PluginMain.resolveDataPath("AssetData.db"))
            val r = dbObj.selectOne("Wordle", Triple("word", "=", "'$inStr'"), "猜单词\nFile:Wordle.kt\tLine:54").isEmpty()
            dbObject.closeDB()
            if (r) {
                sendMessage("有这样的单词吗?")
                continue
            }
            val pass = doc.runBeta(line, inStr)
            line++
            group.sendImage(doc.draw())
            if (pass) {
                sendMessage("你猜中了! 游戏结束...")
                MirrorWorldUser.userData.getOrPut(user.id) { Role(user.nameCardOrNick) }.play()
                return
            }
        }
        sendMessage("正确答案为:$ctAn,游戏结束...")
        MirrorWorldUser.userData.getOrPut(user.id) { Role(user.nameCardOrNick) }.play()
    }
}