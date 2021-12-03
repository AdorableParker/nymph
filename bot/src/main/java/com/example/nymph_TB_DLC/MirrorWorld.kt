package com.example.nymph_TB_DLC

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.nextEvent

class MirrorWorld {
    private suspend fun yesOrNo(subject: MemberCommandSenderOnMessage, theme: String, no: String): Boolean {
        subject.sendMessage("[10秒/3次]$theme<是/否>")
        for (i in 0..2) {
            val judge = runCatching {
                nextEvent<GroupMessageEvent>(10_000) { it.sender == subject.user }.message.contentToString()
            }.onFailure {
                subject.sendMessage("输入超时")
                return true
            }.getOrNull()

            when (judge) {
                "是" -> return true
                "否" -> {
                    subject.sendMessage(no)
                    break
                }
                else -> {
                    if (i > 0) {
                        subject.sendMessage("[10秒/${i}次]$theme<是/否>")
                    } else {
                        subject.sendMessage("次数用尽,$no")
                        break
                    }
                }
            }
        }
        return false
    }

    /** 玩家数据汇报 */
    suspend fun gamerInfo(subject: MemberCommandSenderOnMessage) {
        subject.sendMessage("测试通过")
        val userData = MirrorWorldUser.userPermanent.getOrPut(subject.user.id) {
            PermanentData()
        }
        subject.sendMessage(userData.outInfo())
    }

    /** 玩家角色建立 */
    suspend fun characterCreation(subject: MemberCommandSenderOnMessage) {
        subject.sendMessage("测试开始")
        val userData = MirrorWorldUser.userPermanent.getOrPut(subject.user.id) {
            PermanentData()
        }
        if (userData.pc != null) {
            if (yesOrNo(subject, "创建新角色？\n清除存档,不可恢复", "角色建立取消")) return
        }
        userData.pc = PlayerCharacter()
        subject.sendMessage(
            """
        角色已建立,可以开始分配属性点
        现在拥有${userData.pc!!.showAP()}点属性点可分配至
        力量：与物攻相关
        法力：与法术相关
        智力:与经验获取,战斗效率相关
        体质：与生命值和法力值相关
        速度：与行动速度相关
        运气：与事件判定相关
        六项基本属性,确定属性前随时可以查看并改变
        """.trimIndent()
        )
    }

    /** 分配属性点 */
    suspend fun apAllotted(subject: MemberCommandSenderOnMessage) {
        val userData = MirrorWorldUser.userPermanent.getOrPut(subject.user.id) {
            PermanentData()
        }
        // 判断角色是否存在
        if (userData.pc == null) {
            subject.sendMessage("角色不存在，请建立角色后操作")
            return
        }
        // 判断是否可分配点数
        val ap = userData.pc!!.showAP()
        if (ap == 0) {
            subject.sendMessage("无可分配点数")
            return
        }
        // 开始分配点数
        subject.sendMessage("[30秒]请按照以下顺序输入点数分配方案(各项之间使用冒号分割)：\n力量:法力:智力:体质:速度:运气")
        val lt = Array(6) { 0 }
        GetPlan@ while (true) {
            // 获取用户输入
            val plan = kotlin.runCatching {
                nextEvent<GroupMessageEvent>(30_000) { it.sender == subject.user }.message.contentToString()
                    .split(":", "：", limit = 7)
            }.onFailure {
                subject.sendMessage("输入超时,点数分配取消")
                return
            }.getOrDefault(listOf())
            // 判断输入数量
            if (plan.size < 6) {
                subject.sendMessage("[30秒]参数数量不匹配,请重新输入")
                continue
            }
            // 按顺序分配
            for (i in 0..5) {
                when (val apv = plan[i].toIntOrNull()) { //检查输入合法性
                    null -> {
                        subject.sendMessage("属性值必须为整数,请重新输入")
                        continue@GetPlan
                    }
                    in 0..7 -> lt[i] = apv
                    else -> {
                        subject.sendMessage("[30秒]每项最多为其分配7点,请重新输入")
                        continue@GetPlan
                    }
                }
            }
            // 检查点数用量
            if (ap < lt.sum()) subject.sendMessage("该方案所用点数超出可用点数总量,请重新输入") else break
        }
        // 输出汇报
        val mod = Tool(lt)
        subject.sendMessage(
            "方案有效,生成角色属性预览如下\n等级: 1\nHP: ${mod.draftHP()}\tMP: ${mod.draftMP()}\nATK: ${mod.draftATK()}\tMAT: ${mod.draftMAT()}\n${mod.show6D()}"
        )

        // 用户确定
        if (yesOrNo(
                subject,
                "确定当前方案？（剩余未分配的属性点将会以1属性点:${MirrorWorldConfig.ExchangeRate}金币的比例自动转化为金币值）\n确定属性后,不可再次更改",
                "点数分配取消"
            )
        ) return
        userData.pc!!.set6D(lt)
        subject.sendMessage("点数设定完成,可进行特性/技能学习")
    }
}