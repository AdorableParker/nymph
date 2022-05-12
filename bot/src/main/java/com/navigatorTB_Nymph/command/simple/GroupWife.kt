package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.seed.Cycle
import com.navigatorTB_Nymph.tool.seed.Seed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.net.URL
import java.time.LocalDateTime

object GroupWife : SimpleCommand(
    PluginMain, "GroupWife", "今日老婆",
    description = "每天一个群老婆"
) {
    override val usage: String = "${CommandManager.commandPrefix}今日老婆"

    private val wifeGroupMap = mutableMapOf<Long, MutableList<Member>>()
    private var groupWifeUpdate = 0

    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }

        if (LocalDateTime.now().dayOfYear != groupWifeUpdate) cleanList()

        val wifeList = wifeGroupMap.getOrPut(group.id) { mutableListOf() }
        val index = wifeList.indexOf(user)
        val wife = when {
            index == -1 -> {
                val cache = group.members.filter {
                    it !in wifeList
                }.sortedByDescending { it.lastSpeakTimestamp }
                val r = (if (cache.size >= 10) cache.subList(0, 10) else cache).random(Seed.getSeed(user.id, Cycle.Day))
                wifeList.add(r)
                wifeList.add(user)
                r
            }
            index % 2 == 1 -> wifeList[index - 1]
            else -> wifeList[index + 1]
        }

        val chain = MessageChainBuilder()
        chain.add("今天你的群老婆是")
        chain.add(
            (withContext(Dispatchers.IO) {
                URL(wife.avatarUrl).openConnection().getInputStream()
            }).uploadAsImage(group)
        )
        chain.add("${wife.nameCardOrNick}(${wife.id})哒")
        sendMessage(chain.build())
    }

    private fun cleanList() {
        groupWifeUpdate = LocalDateTime.now().dayOfYear
        wifeGroupMap.forEach { (_, it) -> it.clear() }
    }
}