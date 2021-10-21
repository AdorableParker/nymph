package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.MyPluginData.duelTime
import com.example.navigatorTB_Nymph.UsageStatistics.record
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
import java.time.Instant

@MiraiExperimentalApi
@ConsoleExperimentalApi
class Gun(val adversary: Member) {
    private val magazine: MutableList<Int>
    private var tracer = 0

    init {
        val ammunition = Array(6) {
            when ((0..it).random()) {
                3 -> 0
                5 -> 2
                else -> 1
            }
        }
        ammunition.shuffle()
        magazine = ammunition.toMutableList()
    }

    suspend fun shot(group: Group): Boolean {
        if (magazine.isNotEmpty()) {
            if (magazine[0] == 0) {
                group.sendMessage("这是颗哑弹!")
            } else {
                if ((0..(9 - tracer)).random() != 0) {
                    tracer++
                    group.sendMessage("未命中！")
                } else {
                    // 命中 执行禁言
                    runCatching {
                        val bonusModifier = if (magazine.size == 6) {
                            group.sendMessage("首发!")
                            300
                        } else 0

                        if (magazine[0] == 1) {
                            group.sendMessage("常规弹命中！")
                            adversary.mute(300 / magazine.size + bonusModifier)  // 回合数越久，禁言时间越长
                        } else {
                            group.sendMessage("特装弹命中！")
                            adversary.mute(600 / magazine.size + bonusModifier)  // 回合数越久，禁言时间越长
                        }
                    }.onSuccess {
                        group.sendMessage(At(adversary) + PlainText("不要停下来啊！"))
                    }.onFailure { group.sendMessage("嘤嘤嘤，TB在本群权限不足") }

                    return true
                }
            }
            magazine.removeFirst()
        } else {
            if (PluginMain.BothSidesDuel[adversary]?.magazine?.isNotEmpty() == true) { // 如果对方还有子弹
                group.sendMessage("你没子弹了！现在是对方的时间")
                while (PluginMain.BothSidesDuel[adversary]?.shot(group) == false) {
                    // 直到命中或是弹药用尽
                }
            } else {
                group.sendMessage("弹药用尽，双方停火，决斗结束")
            }
            return true // 到此处时，决斗必定已经结束
        }
        return false
    }

}

@MiraiExperimentalApi
@ConsoleExperimentalApi
object Duel : CompositeCommand(
    PluginMain, "Duel", "决斗",
    description = "禁言决斗，用于普通群员与普通群员之间解决冲突"
) {
    @SubCommand("发起")
    suspend fun MemberCommandSenderOnMessage.main(target: Member) {
        record(primaryName)
        if (group.botMuteRemaining > 0) return

//        bot.eventChannel.subscribeAlways<> {  }
        if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法使用本功能")
            return
        }
        if (user == target) {
            sendMessage("请不要左右互搏")
            return
        }

        if (PluginMain.BothSidesDuel.containsKey(user) || PluginMain.BothSidesDuel.containsKey(target)) {
            sendMessage("你或对方正在决斗中，不能发起新的决斗")
            return
        }

        SQLiteJDBC(PluginMain.resolveDataPath("User.db"))

        val coolDownTime = Instant.now().epochSecond - duelTime.getOrDefault(group.id, 0)
        if (coolDownTime <= 300L) {
            sendMessage("决斗场占用中，请等待${300 - coolDownTime}秒")
            return
        }
        duelTime[group.id] = Instant.now().epochSecond
        sendMessage("${user.nameCardOrNick}发起了对${target.nameCardOrNick}的决斗")

        PluginMain.BothSidesDuel[user] = Gun(target)
        PluginMain.BothSidesDuel[target] = Gun(user)

        if (PluginMain.BothSidesDuel[user]?.shot(group) == true) {
            PluginMain.BothSidesDuel.remove(user)
            PluginMain.BothSidesDuel.remove(target)
        } else {
            sendMessage(At(target) + PlainText("轮到你了,反击！"))
        }
    }

    @SubCommand("射击")
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return

        if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法使用本功能")
            return
        }

        if (PluginMain.BothSidesDuel.containsKey(user)) { // 判断有无进行中的决斗
            if (PluginMain.BothSidesDuel[user]?.shot(group) == true) {  // 进行射击 判断射击是否命中
                PluginMain.BothSidesDuel[user]?.let { PluginMain.BothSidesDuel.remove(it.adversary) }
                PluginMain.BothSidesDuel.remove(user)
            } else {  // 未命中
                PluginMain.BothSidesDuel[user]?.let {
                    sendMessage(At(it.adversary) + PlainText("轮到你了,反击！"))
                }
            }
        } else {
            sendMessage("你没有正在进行的决斗，无法射击")
        }
    }


}