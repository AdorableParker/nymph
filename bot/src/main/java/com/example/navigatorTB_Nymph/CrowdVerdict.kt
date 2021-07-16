/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.time.Instant

class VoteUser {

    private var sentence: Int = 0
    private var sealingLabel: Boolean = true

    private val idList: MutableList<Long> = mutableListOf()

    private val endTime: Long = Instant.now().epochSecond + 600

    fun getSealingLabel(): Boolean = sealingLabel
    fun getBallot(): Int = idList.size
    fun getSentence(): Int = sentence
    fun countdown(): Long = endTime - Instant.now().epochSecond

    private fun poll(voter: Long, i: Int) {
        idList.add(voter)
        sentence += i
    }

    fun punch(): VoteUser {
        sealingLabel = false
        return this
    }

    fun cast(userID: Long, ticketValue: Int = 5): Boolean {
        return if (idList.indexOf(userID) == -1) {
            poll(userID, ticketValue)
            false
        } else true
    }

}

@MiraiExperimentalApi
@ConsoleExperimentalApi
object CrowdVerdict : SimpleCommand(
    PluginMain, "CrowdVerdict", "众裁",
    description = "众裁禁言"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main(target: Member) {
        record(primaryName)
        if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法使用本功能")
            return
        }

        val voteUser = PluginMain.VOTES.getOrDefault(target.id, VoteUser())

        when (potteryExile(voteUser, target.id, user.id)) {
            0 -> sendMessage(
                "${user.nameCardOrNick}发起了对${target.nameCardOrNick}的众裁\n" +
                        "达成进度(1/8) 剩余时间：${voteUser.countdown()}秒\n" +
                        "决议通过后,被裁决者将会被禁言${voteUser.getSentence()}分钟"
            )
            1 -> sendMessage("你已经投过票了")
            2 -> runCatching {
                target.mute(voteUser.getSentence() * 60)
            }.onSuccess {
                PluginMain.VOTES.remove(target.id)
                sendMessage("众裁通过,${target.nameCardOrNick} 被执行裁决")
            }.onFailure {
                sendMessage("执行失败,请检查权限")
            }
            3 -> sendMessage("达成进度(${voteUser.getBallot()}/8) 剩余时间：${voteUser.countdown()}秒\n通过后,被裁决者将会被禁言${voteUser.getSentence()}分钟")
        }
    }

    private fun potteryExile(voteUser: VoteUser, target: Long, user: Long): Int {
        if (voteUser.getSealingLabel()) {
            PluginMain.VOTES[target] = voteUser.punch()
        }
        val statusCode = if (voteUser.countdown() <= 0) 0 else 1
        if (voteUser.cast(user)) return 1
        return statusCode * if (voteUser.getBallot() >= 8) 2 else 3
    }
}