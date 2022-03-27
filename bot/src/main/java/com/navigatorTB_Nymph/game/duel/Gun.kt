package com.navigatorTB_Nymph.game.duel

import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText

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
            if (PluginMain.BOTH_SIDES_DUEL[adversary]?.magazine?.isNotEmpty() == true) { // 如果对方还有子弹
                group.sendMessage("你没子弹了！现在是对方的时间")
                while (PluginMain.BOTH_SIDES_DUEL[adversary]?.shot(group) == false) {
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