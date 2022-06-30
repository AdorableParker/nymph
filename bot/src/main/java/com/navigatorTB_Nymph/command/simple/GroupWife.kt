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
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.MessageChain
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
    private val solitaryMap = mutableMapOf<Long, MutableList<Long>>()
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

        val groupWifeList = wifeGroupMap.getOrPut(group.id) { mutableListOf() }
        val solitaryList = solitaryMap.getOrPut(group.id) { mutableListOf() }

        val index = groupWifeList.indexOfFirst { it.id == user.id }

        when (index % 2) {
            -1 -> { //没找到
                val wifeList = List(groupWifeList.size) { i -> groupWifeList[i].id }
                val cache = group.members.filter {
                    it.id !in wifeList && it.id !in solitaryList
                }.sortedByDescending(NormalMember::lastSpeakTimestamp)
                val wife = (if (cache.size >= 10) cache.subList(0, 10) else cache).random()
                if (wife.id == user.id) {
                    sendMessage("今天你没有老婆哒")
                    solitaryList.add(user.id)
                    return
                } else sendMessage(getChain(group, groupWifeList.addPair(user, wife)))
            }
            1 -> sendMessage(getChain(group, groupWifeList[index - 1]))
            else -> sendMessage(getChain(group, groupWifeList[index + 1]))
        }
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

        val ntrList = ntrMap.getOrPut(group.id) { mutableListOf() }
        if (user.id in ntrList) {
            sendMessage("今天你已经牛过了")
            return
        }
        val solitaryList = solitaryMap.getOrPut(group.id) { mutableListOf() }
        val wifeList = wifeGroupMap.getOrPut(group.id) { mutableListOf() }

        when {
            user.id == beau.id -> sendMessage("你娶你自己?")
            user.id in List(wifeList.size) { wifeList[it].id } -> sendMessage("喂,你家里还有个吃白饭的呢")
            user.id !in solitaryList -> main()
            beau.id == bot.id -> sendMessage("笨蛋！不准娶我！哼唧！")
            beau.id in solitaryList -> sendMessage("你无法牛一个没有老婆的人")
            else -> sendMessage(doNTR(group, user, beau, ntrList, wifeList, solitaryList))
        }
    }

    private suspend fun doNTR(
        group: Group,
        user: Member,
        beau: Member,
        ntrList: MutableList<Long>,
        groupWifeList: MutableList<GroupUser>,
        solitaryList: MutableList<Long>
    ): MessageChain {
        ntrList.add(user.id)
        val chain = MessageChainBuilder()
        if (1 == (1..5).random()) {
            groupWifeList.delPair(user, beau)
            groupWifeList.addPair(user, beau)
            solitaryList.remove(user.id)
            chain.add("你成功的把")
            chain.add(
                (withContext(Dispatchers.IO) {
                    URL(beau.avatarUrl).openConnection().getInputStream()
                }).uploadAsImage(group)
            )
            chain.add("${beau.nameCardOrNick}(${beau.id})骗到了手,现在是你的老婆")
        } else {
            chain.add("你试图把")
            chain.add(
                (withContext(Dispatchers.IO) {
                    URL(beau.avatarUrl).openConnection().getInputStream()
                }).uploadAsImage(group)
            )
            chain.add("${beau.nameCardOrNick}(${beau.id})骗做老婆,但是失败了,你还是没有老婆")
        }
        return chain.build()
    }

    private suspend fun getChain(group: Group, wife: GroupUser): MessageChain {
        val chain = MessageChainBuilder()
        chain.add("今天你的群老婆是")
        chain.add(
            (withContext(Dispatchers.IO) {
                URL(wife.avatarUrl).openConnection().getInputStream()
            }).uploadAsImage(group)
        )
        chain.add("${wife.nameCardOrNick}(${wife.id})哒")
        return chain.build()
    }

    private fun MutableList<GroupUser>.addPair(user: Member, beau: Member): GroupUser {
        add(GroupUser(user))
        return GroupUser(beau).also(::add)
    }

    private fun MutableList<GroupUser>.delPair(user: Member, beau: Member) {
        removeIf { it.id == user.id || it.id == beau.id }
    }

    private fun cleanList() {
        groupWifeUpdate = LocalDateTime.now().dayOfYear
        wifeGroupMap.forEach { (_, it) -> it.clear() }
        ntrMap.forEach { (_, it) -> it.clear() }
        byNtrMap.forEach { (_, it) -> it.clear() }
    }
}