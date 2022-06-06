package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.data.GroupUser
import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
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

    private val wifeGroupMap = mutableMapOf<Long, MutableList<GroupUser>>()
    private val ntrMap = mutableMapOf<Long, MutableList<Long>>()
    private val byNtrMap = mutableMapOf<Long, MutableList<Long>>()
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

        if (user.id in byNtrMap) {
            sendMessage("你醒啦,你的老婆被骗走了哦")
            return
        }

        val wifeList = wifeGroupMap.getOrPut(group.id) { mutableListOf() }
        val u = GroupUser(user)
        val index = wifeList.indexOf(u)
        val wife = when (index % 2) {
            -1 -> {
                val cache = group.members.filter {
                    GroupUser(it) !in wifeList
                }.sortedByDescending { it.lastSpeakTimestamp }
                val r = (if (cache.size >= 10) cache.subList(0, 10) else cache).random().let {
                    GroupUser(it.id, it.avatarUrl, it.nameCardOrNick)
                }
                wifeList.add(r)
                wifeList.add(u)
                r
            }
            1 -> wifeList[index - 1]
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

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(beau: Member) {
        UsageStatistics.record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (LocalDateTime.now().dayOfYear != groupWifeUpdate) cleanList()
        if (beau == bot) {
            sendMessage("笨蛋！不准娶我 哼唧！")
            return
        }
        if (user == beau) {
            sendMessage("你牛你自己?")
            return
        }
        val ntrList = ntrMap.getOrPut(group.id) { mutableListOf() }
        if (user.id in ntrList) {
            sendMessage("今天你已经牛过了")
            return
        }
        val wifeList = wifeGroupMap.getOrPut(group.id) { mutableListOf() }
        val a = GroupUser(user)
        val indexA = wifeList.indexOf(a)
        if (indexA == -1) {
            main()
            return
        }
        val aWifeIndex = when (indexA % 2) {
            1 -> indexA - 1
            else -> indexA + 1
        }
        val aWife = wifeList[aWifeIndex]
        val b = GroupUser(beau)
        if (aWife == b) {
            sendMessage("笨蛋~本来就是你的老婆")
            return
        }
        if (aWife != a) {
            sendMessage("喂,你家里还有个吃白饭的呢")
            return
        }
        val indexB = wifeList.indexOf(b)
        val bWifeIndex = when (indexB % 2) {
            -1 -> -1
            1 -> indexB - 1
            else -> indexB + 1
        }
        if (bWifeIndex == -1 || wifeList[bWifeIndex] == b) {
            sendMessage("你无法牛一个没有老婆的人")
            return
        }
        ntrList.add(user.id)
        val chain = MessageChainBuilder()
        if (1 == (1..5).random()) {
            byNtrMap.getOrPut(group.id) { mutableListOf() }.add(wifeList[bWifeIndex].id)
            wifeList[aWifeIndex] = b
            wifeList.removeAt(indexB)
            wifeList.removeAt(bWifeIndex)

            chain.add("你成功的把")
            chain.add(
                (withContext(Dispatchers.IO) {
                    URL(b.avatarUrl).openConnection().getInputStream()
                }).uploadAsImage(group)
            )
            chain.add("${b.nameCardOrNick}(${b.id})骗到了手,现在是你的老婆")
        } else {
            chain.add("你试图把")
            chain.add(
                (withContext(Dispatchers.IO) {
                    URL(b.avatarUrl).openConnection().getInputStream()
                }).uploadAsImage(group)
            )
            chain.add("${b.nameCardOrNick}(${b.id})骗做老婆,但是失败了,你还是没有老婆")
        }
        sendMessage(chain.build())
    }


    private fun cleanList() {
        groupWifeUpdate = LocalDateTime.now().dayOfYear
        wifeGroupMap.forEach { (_, it) -> it.clear() }
        ntrMap.forEach { (_, it) -> it.clear() }
        byNtrMap.forEach { (_, it) -> it.clear() }
    }
}