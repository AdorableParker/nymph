package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.MiraiExperimentalApi

data class Gun(val adversary: Member, val magazine: MutableList<Int>)

@MiraiExperimentalApi
@ConsoleExperimentalApi
object Duel : CompositeCommand(
    PluginMain, "Duel", "决斗",
    description = "禁言决斗"
) {
    @SubCommand("发起")
    suspend fun MemberCommandSenderOnMessage.main(target: Member) {
//        bot.eventChannel.subscribeAlways<> {  }
        if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法使用本功能")
            return
        }
        if (PluginMain.BothSidesDuel.containsKey(user) || PluginMain.BothSidesDuel.containsKey(target)) {
            sendMessage("你或对方正在决斗中，不能发起新的决斗")
            return
        }

        sendMessage("${user.nameCardOrNick}发起了对${target.nameCardOrNick}的决斗")

        PluginMain.BothSidesDuel[user] = Gun(target, reload())
        PluginMain.BothSidesDuel[target] = Gun(user, reload())

        if (PluginMain.BothSidesDuel[user]?.let { shot(it, group) } == true) {
            PluginMain.BothSidesDuel.remove(user)
            PluginMain.BothSidesDuel.remove(target)
        } else {
            sendMessage(At(target) + PlainText("轮到你了,反击！"))
        }
    }

    @SubCommand("反击")
    suspend fun MemberCommandSenderOnMessage.main() {
        if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法使用本功能")
            return
        }
//        for (i in PluginMain.BothSidesDuel) {
//            PluginMain.logger.info {
//                "${i.key},${i.value.adversary}"
//            }
//        }
        if (PluginMain.BothSidesDuel.containsKey(user)) {
            if (PluginMain.BothSidesDuel[user]?.let { shot(it, group) } == true) {
                PluginMain.BothSidesDuel.remove(user)
                PluginMain.BothSidesDuel[user]?.let { PluginMain.BothSidesDuel.remove(it.adversary) }
            } else {
                PluginMain.BothSidesDuel[user]?.let {
                    sendMessage(At(it.adversary) + PlainText("轮到你了,反击！"))
                }
            }
        } else {
            sendMessage("你没有正在进行的决斗，无法反击")
        }
    }


    private suspend fun shot(revolver: Gun, group: Group): Boolean {
        if (revolver.magazine.isNotEmpty()) {
            if (revolver.magazine[0] == 0) {
                group.sendMessage("这是颗哑弹!")
            } else {
                if ((0..2).random() != 0) {
                    group.sendMessage("未命中！")
                } else {
                    runCatching {
                        val bonusModifier = if (revolver.magazine.size == 6) {
                            group.sendMessage("首发!")
                            300
                        } else 0

                        if (revolver.magazine[0] == 1) {
                            group.sendMessage("常规弹命中！")
                            revolver.adversary.mute(300 / revolver.magazine.size + bonusModifier)  // 回合数越久，禁言时间越长
                        } else {
                            group.sendMessage("特装弹命中！")
                            revolver.adversary.mute(600 / revolver.magazine.size + bonusModifier)  // 回合数越久，禁言时间越长
                        }
                    }.onSuccess {
                        group.sendMessage(At(revolver.adversary) + PlainText("不要停下来啊！"))
                    }.onFailure { group.sendMessage("嘤嘤嘤，TB在本群权限不足") }
                    return true
                }
            }
            revolver.magazine.removeFirst()
        } else {
            if (PluginMain.BothSidesDuel[revolver.adversary]?.magazine?.isNotEmpty() == true) { // 如果对方还有子弹
                group.sendMessage("你没子弹了！现在是对方的时间")
                while (PluginMain.BothSidesDuel[revolver.adversary]?.let { shot(it, group) } == false) {
                    // 直到命中或是弹药用尽
                }
            } else {
                group.sendMessage("弹药用尽，双方停火，决斗结束")
                return true
            }
        }
        return false
    }

    private fun reload(): MutableList<Int> {
        val magazine = Array(6) {
            when ((0..it).random()) {
                0 -> 0
                5 -> 2
                else -> 1
            }
        }
        magazine.shuffle()
        return magazine.toMutableList()
    }
}