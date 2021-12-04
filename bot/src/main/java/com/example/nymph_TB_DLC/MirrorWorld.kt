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
                subject.sendMessage("输入超时,$no")
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
        val userData = MirrorWorldUser.userPermanent.getOrPut(subject.user.id) {
            PermanentData()
        }
        subject.sendMessage(userData.outInfo())
    }

    /** 玩家角色建立 */
    suspend fun characterCreation(subject: MemberCommandSenderOnMessage) {
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

    /** 选择特质及职业 */
    suspend fun chooseCareer(subject: MemberCommandSenderOnMessage) {
        val userData = MirrorWorldUser.userPermanent.getOrPut(subject.user.id) { PermanentData() }
        // 判断角色是否存在
        if (userData.pc == null) {
            subject.sendMessage("角色不存在，请建立角色后操作")
            return
        }
        // 判断是否可分配点数
        var sp = userData.pc!!.showSP()
        if (sp == 0) {
            subject.sendMessage("无可分配点数")
            return
        }
        var kill1: Int? = 0
        subject.sendMessage("[30秒/3次]现拥有 $sp SP\n请选择特性(单选)\n1:奴隶出身(+1SP)\n2：平民出身(-1SP)\n3:商贾世家(-2SP)\n4:皇室出身(-3SP)")
        for (i in 0..2) {
            kill1 = runCatching {
                nextEvent<GroupMessageEvent>(30_000) { it.sender == subject.user }.message.contentToString()
                    .toIntOrNull()
            }.onFailure {
                subject.sendMessage("输入超时,特性选择取消")
                return
            }.getOrNull()
            when (kill1) {
                1 -> sp += 1
                2 -> sp -= 1
                3 -> sp -= 2
                4 -> sp -= 3
                else -> {
                    if (i > 0) {
                        subject.sendMessage("[30秒/${i}次]请选择特性(输入序号:单选)\nA:奴隶出身(+1SP)\nB：平民出身(-1SP)\nC:商贾世家(-2SP)\nD:皇室出身(-3SP)")
                        continue
                    } else {
                        subject.sendMessage("次数用尽,特性选择取消")
                        return
                    }
                }
            }
            if (sp > 0) break
            subject.sendMessage("点数不足,请重新选择")
        }
        subject.sendMessage("[30秒/3次]现拥有 $sp SP\n请选择特性(单选)\n1:恶名远扬(+1SP)\n2：默默无闻(-1SP)\n3:小有名气(-2SP)\n4:声名远扬(-3SP)")
        var kill2: Int? = 0
        for (i in 0..2) {
            kill2 = runCatching {
                nextEvent<GroupMessageEvent>(30_000) { it.sender == subject.user }.message.contentToString()
                    .toIntOrNull()
            }.onFailure {
                subject.sendMessage("输入超时,特性选择取消")
                return
            }.getOrNull()
            when (kill2) {
                1 -> sp += 1
                2 -> sp -= 1
                3 -> sp -= 2
                4 -> sp -= 3
                else -> {
                    if (i > 0) {
                        subject.sendMessage("[30秒/${i}次]请选择特性(输入序号:单选)\n1:恶名远扬(+1SP)\n2：默默无闻(-1SP)\n3:小有名气(-2SP)\n4:声名远扬(-3SP)")
                        continue
                    } else {
                        subject.sendMessage("次数用尽,特性选择取消")
                        return
                    }
                }
            }
            if (sp > 0) break
            subject.sendMessage("点数不足,请重新选择")
        }
        if (sp >= 2) subject.sendMessage("[30秒/1次]现拥有 $sp SP\n请选择职业(可选)\n1:骑士(-2SP)\n2：猎手(-2SP)\n3牧师(-2SP)\n4:法师(-2SP)\n[除选项外任意输入放弃职业选择]")
        val kill3 = runCatching {
            nextEvent<GroupMessageEvent>(30_000) { it.sender == subject.user }.message.contentToString().toIntOrNull()
        }.onFailure { subject.sendMessage("输入超时,放弃职业选择") }.getOrNull()
        if (kill3 != null) sp -= 2
        userData.pc!!.newRole(kill1, kill2, kill3, sp)
        // 输出汇报
        subject.sendMessage("角色创建全部完成")
    }

    /** PvP */
    suspend fun pvp(subject: MemberCommandSenderOnMessage) {
        subject.sendMessage(
            """
        PvP挑战须知:\n挑战依据战败惩罚分为[练习][切磋][死斗]三类
        [练习]
        1、生命值降至0即判负
        2、对战结束后双方状态将恢复至对战前状态
        3、开启练习对战将会消耗双方金币各10枚
        4、练习场经验收益为正常的20%
        [切磋]
        1、生命值降至40%以下即判断负
        2、对战结束后双方状态将会保留
        3、败方将会付出至少50枚金币给予胜方
        [死斗]
        1、生命值降至0即判负
        2、败方将会进行角色清算（结算分数，删除角色）
        3、胜方获得败方10%的金币
        """.trimIndent()
        )
    }
}