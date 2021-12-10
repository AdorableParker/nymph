package com.nymph_TB_DLC


import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.message.data.content
import java.lang.System.currentTimeMillis
import java.time.LocalDateTime
import kotlin.math.roundToInt

class MirrorWorld {
    private suspend fun yesOrNo(subject: MemberCommandSenderOnMessage, theme: String, no: String): Boolean {
        subject.sendMessage("[30秒/3次]$theme<是/否>")
        for (i in 0..2) {
            val judge = runCatching {
                nextEvent<GroupMessageEvent>(30_000) { it.sender == subject.user }.message.content
            }.onFailure {
                subject.sendMessage("输入超时,$no")
                return true
            }.getOrNull()

            when (judge) {
                "是" -> return false
                "否" -> {
                    subject.sendMessage(no)
                    break
                }
                else -> {
                    if (i < 2) {
                        subject.sendMessage("[30秒/${2 - i}次]$theme<是/否>")
                    } else {
                        subject.sendMessage("次数用尽,$no")
                        break
                    }
                }
            }
        }
        return true
    }

    /** 用户数据汇报 */
    suspend fun gamerInfo(subject: MemberCommandSenderOnMessage) {
        subject.sendMessage(MirrorWorldUser.outInfo(subject.user.id))
    }

    /** 玩家角色建立 */
    suspend fun characterCreation(subject: MemberCommandSenderOnMessage) {
        val userData = MirrorWorldUser.userRole[subject.user.id]
        if (userData != null) {
            if (yesOrNo(subject, "创建新角色？\n清除存档,不可恢复", "角色建立取消")) return
        }
        subject.sendMessage(
            """
        角色已建立,开始分配属性点
        现在拥有 22 点属性点可分配至
        力量:与物攻相关
        法力:与法术相关
        智力:与经验获取,战斗效率相关
        体质:与生命值和法力值相关
        速度:与行动速度相关
        运气:与事件判定相关
        
        [120秒]请按照以下顺序输入点数分配方案(各项之间使用冒号分割)：
        力量:法力:智力:体质:速度:运气
        """.trimIndent()
        )
        // 点数分配
        val lt = Array(6) { 0 }
        GetAP@ while (true) {
            val plan = kotlin.runCatching {
                nextEvent<GroupMessageEvent>(120_000) { it.sender == subject.user }.message.content
                    .split(":", "：", limit = 7)
            }.onFailure {
                subject.sendMessage("输入超时,角色建立取消")
                return
            }.getOrDefault(listOf())
            // 判断输入数量
            if (plan.size < 6) {
                subject.sendMessage("[120秒]参数数量不匹配,请重新输入")
                continue
            }
            // 解析数据
            for (index in 0..5)
                when (val v = plan[index].toIntOrNull()) {
                    null -> {
                        subject.sendMessage("[120秒]属性值必须为整数,请重新输入")
                        continue@GetAP
                    }
                    in 0..7 -> lt[index] = v
                    else -> {
                        subject.sendMessage("[120秒]每项最多为其分配7点,请重新输入")
                        continue@GetAP
                    }
                }
            // 检查点数用量
            if (22 < lt.sum()) subject.sendMessage("[120秒]该方案所用点数超出可用点数总量,请重新输入") else break
            // 输出汇报
            val mod = Tool(lt)
            subject.sendMessage(
                "方案有效,生成角色属性预览如下\n${mod.show()}"
            )
            // 用户确定
            if (yesOrNo(
                    subject,
                    "确定当前方案？（剩余未分配的属性点将会以1属性点:${MirrorWorldConfig.ExchangeRate}金币的比例自动转化为金币值）\n确定属性后,不可再次更改",
                    "请重新输入方案"
                )
            ) continue
            break
        }
        //选择特质及职业
        var sp = 6
        val killList = Array(3) { 0 }
        subject.sendMessage(
            """
            [120秒]点数设定完成,请进行特性/职业选择
            现拥有 $sp SP
            请选择特性(单选)
            1:奴隶出身(+1SP)
            2:平民出身(-1SP)
            3:商贾世家(-2SP)
            4:皇室出身(-3SP)
            """.trimIndent()
        )
        while (true) {
            val k1 = runCatching {
                nextEvent<GroupMessageEvent>(120_000) { it.sender == subject.user }.message.content.toIntOrNull()
            }.onFailure {
                subject.sendMessage("输入超时,角色建立取消")
                return
            }.getOrNull()
            when (k1) {
                1 -> sp += 1
                2 -> sp -= 1
                3 -> sp -= 2
                4 -> sp -= 3
                else -> {
                    subject.sendMessage(
                        """
                        [120秒]参数错误,请重新选择特性(输入序号:单选)
                        1:奴隶出身(+1SP)
                        2:平民出身(-1SP)
                        3:商贾世家(-2SP)
                        4:皇室出身(-3SP)
                        """.trimIndent()
                    )
                    continue
                }
            }
            killList[0] = k1
            break
        }
        subject.sendMessage(
            """
            [120秒]剩余 $sp SP
            请选择特性(单选)
            1:恶名远扬(+1SP)
            2:默默无闻(-1SP)
            3:小有名气(-2SP)
            4:声名远扬(-3SP)
            """.trimIndent()
        )
        while (true) {
            val k2 = runCatching {
                nextEvent<GroupMessageEvent>(120_000) { it.sender == subject.user }.message.content.toIntOrNull()
            }.onFailure {
                subject.sendMessage("输入超时,角色建立取消")
                return
            }.getOrNull()
            when (k2) {
                1 -> sp += 1
                2 -> sp -= 1
                3 -> sp -= 2
                4 -> sp -= 3
                else -> {
                    subject.sendMessage(
                        """
                        [120秒]参数错误,请重新选择特性(输入序号:单选)
                        1:恶名远扬(+1SP)
                        2:默默无闻(-1SP)
                        3:小有名气(-2SP)
                        4:声名远扬(-3SP)
                        """.trimIndent()
                    )
                    continue
                }
            }
            killList[1] = k2
            break
        }
        val k3 = if (sp >= 2) {
            subject.sendMessage(
                """
                [120秒]现拥有 $sp SP
                请选择职业(可选)
                1:骑士(-2SP)
                2:猎手(-2SP)
                3:牧师(-2SP)
                4:法师(-2SP)
                [除选项外任意输入放弃职业选择]
                """.trimIndent()
            )
            runCatching {
                nextEvent<GroupMessageEvent>(30_000) { it.sender == subject.user }.message.content.toIntOrNull()
            }.onFailure { subject.sendMessage("输入超时,放弃职业选择") }.getOrNull()
        } else null
        val role = when (k3) {
            1 -> Knight(subject.user.nick)
            2 -> Hunter(subject.user.nick)
            3 -> Priest(subject.user.nick)
            4 -> Wizard(subject.user.nick)
            else -> Unemployed(subject.user.nick)
        }
        killList[2] = if (role is Unemployed) sp else sp - 2
        role.newRole(killList)
        role.set6D(lt)
        MirrorWorldUser.userRole[subject.user.id] = role
        subject.sendMessage("角色创建全部完成")
    }

    /** PvP */
    suspend fun pvp(subject: MemberCommandSenderOnMessage, blue: User) {
        val userRole = MirrorWorldUser.userRole[subject.user.id]
        if (userRole == null) {
            subject.sendMessage("你的角色未创建完成，请建立角色后操作")
            return
        }
        val blueRole = MirrorWorldUser.userRole[blue.id]
        if (blueRole == null) {
            subject.sendMessage("对方角色未创建完成，请等待其建立角色后操作")
            return
        }

        if (userRole.pvpUA.not()) {
            subject.sendMessage(
                """
            PvP挑战须知:\n挑战依据战败惩罚分为[练习][切磋][死斗][自定义]四类
            [练习]
            1、生命值降至0即判负
            2、对战结束后双方状态将恢复至对战前状态
            3、开启练习对战将会消耗双方金币各10枚
            4、练习场经验收益为正常的10%
            [切磋]
            1、生命值降至总生命值40%以下即判负
            2、对战结束后双方状态将会保留
            3、胜方获得败方10%的金币(加成前)
            [死斗]
            1、生命值降至0即判负
            2、败方将会进行角色清算（结算分数,删除角色）
            3、胜方获得败方25%的金币(加成前)
            [自定义]
            1、判负标准可自定
            2、无任何奖励
            3、开启自定义对战将会消耗双方金币各20枚
            """.trimIndent()
            )
            if (yesOrNo(subject, "已知上述情况？", "拒绝")) return
            userRole.pvpUA = true
        }

        subject.sendMessage("[120秒/1次]请选择模式\n1:练习\n2:切磋\n3:死斗\n4:自定义")
        val mods = runCatching {
            nextEvent<GroupMessageEvent>(120_000) { it.sender == subject.user }.message.content
                .toIntOrNull()
        }.onFailure {
            subject.sendMessage("输入超时,对战取消")
            return
        }.getOrNull()
        val logID = when (mods) {
            1 -> {
                if (userRole.showGold() < 10 || blueRole.showGold() < 10) {
                    subject.sendMessage("金币余额不足,创建对战失败")
                    return
                }
                drillBattleSequence(userRole, blueRole)
            }
            2 -> battleSequence(userRole, blueRole)
            3 -> {
                battleSequence(userRole, subject.user.id, blueRole, blue.id)
            }
            4 -> {
                if (userRole.showGold() < 20 || blueRole.showGold() < 20) {
                    subject.sendMessage("金币余额不足,创建对战失败")
                    return
                }
                val bl = getBoundaryLine(subject) ?: return
                battleSequence(userRole, blueRole, bl)
            }
            else -> {
                subject.sendMessage("未选择模式,对战取消")
                return
            }
        }
        subject.sendMessage(BattleRecord().readResults(logID))
        if (yesOrNo(subject, "是否输出战斗记录？", "战斗记录放弃输出")) return
        subject.sendMessage(BattleRecord().read(logID))
    }

    /** 转账 */
    suspend fun transfer(subject: MemberCommandSenderOnMessage, blue: User, amount: Int) {
        val userRole = MirrorWorldUser.userRole[subject.user.id]
        if (userRole == null) {
            subject.sendMessage("你的角色未创建完成，请建立角色后操作")
            return
        }
        val blueRole = MirrorWorldUser.userRole[blue.id]
        if (blueRole == null) {
            subject.sendMessage("对方角色未创建完成，请等待其建立角色后操作")
            return
        }
        val v = (amount * 1.01 + 1).roundToInt()
        subject.sendMessage(if (userRole.transfer(blueRole, v)) "转账成功,本次操作收取手续费${v - amount}枚" else "账户金额不足,转账失败")
        blueRole.loseGold(v - amount)
    }

    /** 收益 */
    fun pay(uid: Long, amount: Int): String? {
        val userRole = MirrorWorldUser.userRole[uid] ?: return null
        val userData = MirrorWorldUser.userData.getOrPut(uid) { PermanentData() }
        val today = LocalDateTime.now().dayOfYear
        return if (today != userData.signIn) {
            userData.signIn = today
            "获得${userRole.getPaid(amount)}枚金币"
        } else "你今天已经签到过了"
    }

    fun toBestow(uid: Long, amount: Int) {
        MirrorWorldUser.userRole[uid]?.giveGold(amount)
    }

    fun strip(uid: Long, amount: Int) {
        MirrorWorldUser.userRole[uid]?.snatch(amount)
    }

    fun treatment(uid: Long) = MirrorWorldUser.userRole[uid]?.treatment()

    /** 自定义场获取判定线数据 */
    private suspend fun getBoundaryLine(subject: MemberCommandSenderOnMessage): Double? {
        subject.sendMessage("[120秒/1次]请设定战败判定线(范围:0 ~ 0.9)")
        val boundaryLine = runCatching {
            nextEvent<GroupMessageEvent>(120_000) { it.sender == subject.user }.message.content
                .toDoubleOrNull()
        }.onFailure {
            subject.sendMessage("输入超时,对战取消")
            return null
        }.getOrNull()
        boundaryLine ?: return null
        if (0.0 <= boundaryLine && boundaryLine <= 0.9) return boundaryLine
        subject.sendMessage("输入非法,对战取消")
        return null
    }

    /** 练习场 */
    private fun drillBattleSequence(red: GameRole, blue: GameRole): String {
        // 支付费用
        red.loseGold(10)
        blue.loseGold(10)
        // 备份属性
        val redR = red.backup()
        val blueR = blue.backup()
        // 设定回合计数器
        var redRounds = 1
        var blueRounds = 1
        // 生成本次对战编号
        val logID = (0..1000).random().toString() + currentTimeMillis()
        while (true) {
            when {                                                              // 判断速度
                red.showTPA() * redRounds < blue.showTPA() * blueRounds -> {    //A 攻击 B
                    redRounds++
                    if (red.executeAction(blue, 0.0, logID).not()) {
                        BattleRecord().write(logID, Pair(red.name, blue.name), red.expSettlement(blue, 0.1), Pair(0, 0))
                        break
                    }
                }
                red.showTPA() * redRounds > blue.showTPA() * blueRounds -> {    //B 攻击 A
                    blueRounds++
                    if (blue.executeAction(red, 0.0, logID).not()) {
                        BattleRecord().write(logID, Pair(blue.name, red.name), blue.expSettlement(red, 0.1), Pair(0, 0))
                        break
                    }
                }// 相同再走一个短时间周期(如果时间周期相同则发起者先
                red.showTPA() < blue.showTPA() -> redRounds++
                red.showTPA() > blue.showTPA() -> blueRounds++
                else -> if ((0..1).random() == 0) redRounds++ else blueRounds++
            }
        }
        // 恢复属性
        red.recover(redR)
        blue.recover(blueR)
        return logID
    }

    /** 切磋场 */
    private fun battleSequence(red: GameRole, blue: GameRole): String {
        // 设定回合计数器
        var redRounds = 1
        var blueRounds = 1
        // 生成本次对战编号
        val logID = (0..1000).random().toString() + currentTimeMillis()
        while (true) {
            when {                                                              // 判断速度
                red.showTPA() * redRounds < blue.showTPA() * blueRounds -> {    //A 攻击 B
                    redRounds++
                    if (red.executeAction(blue, 0.4, logID).not()) {
                        BattleRecord().write(
                            logID,
                            Pair(red.name, blue.name),
                            red.expSettlement(blue, 1.0),
                            red.goldSettlement(blue, 0.1)
                        )
                        break
                    }
                }
                red.showTPA() * redRounds > blue.showTPA() * blueRounds -> {    //B 攻击 A
                    blueRounds++
                    if (blue.executeAction(red, 0.4, logID).not()) {
                        BattleRecord().write(
                            logID,
                            Pair(blue.name, red.name),
                            blue.expSettlement(red, 1.0),
                            blue.goldSettlement(red, 0.1)
                        )
                        break
                    }
                }// 相同再走一个短时间周期(如果时间周期相同则发起者先
                red.showTPA() < blue.showTPA() -> redRounds++
                red.showTPA() > blue.showTPA() -> blueRounds++
                else -> if ((0..1).random() == 0) redRounds++ else blueRounds++
            }
        }
        return logID
    }

    /** 死斗场 */
    private fun battleSequence(red: GameRole, redID: Long, blue: GameRole, blueID: Long): String {
        // 设定回合计数器
        var redRounds = 1
        var blueRounds = 1
        // 生成本次对战编号
        val logID = (0..1000).random().toString() + currentTimeMillis()
        while (true) {
            when {                                                              // 判断速度
                red.showTPA() * redRounds < blue.showTPA() * blueRounds -> {    //A 攻击 B
                    redRounds++
                    if (red.executeAction(blue, 0.0, logID).not()) {
                        BattleRecord().write(
                            logID,
                            Pair(red.name, blue.name),
                            red.expSettlement(blue, 1.0),
                            red.goldSettlement(blue, 0.25)
                        )
                        MirrorWorldUser.userData.getOrPut(blueID) { PermanentData() }.pt += blue.valuation()
                        MirrorWorldUser.userRole.remove(blueID)
                        break
                    }
                }
                red.showTPA() * redRounds > blue.showTPA() * blueRounds -> {    //B 攻击 A
                    blueRounds++
                    if (blue.executeAction(red, 0.0, logID).not()) {
                        BattleRecord().write(
                            logID,
                            Pair(blue.name, red.name),
                            blue.expSettlement(red, 1.0),
                            blue.goldSettlement(red, 0.25)
                        )
                        MirrorWorldUser.userData.getOrPut(redID) { PermanentData() }.pt += red.valuation()
                        MirrorWorldUser.userRole.remove(redID)
                        break
                    }
                }// 相同再走一个短时间周期(如果时间周期相同则发起者先
                red.showTPA() < blue.showTPA() -> redRounds++
                red.showTPA() > blue.showTPA() -> blueRounds++
                else -> if ((0..1).random() == 0) redRounds++ else blueRounds++
            }
        }
        return logID
    }

    /** 自定义场 */
    private fun battleSequence(red: GameRole, blue: GameRole, boundaryLine: Double): String {
        // 支付场地费
        red.loseGold(20)
        red.loseGold(20)
        // 设定回合计数器
        var redRounds = 1
        var blueRounds = 1
        // 生成本次对战编号
        val logID = (0..1000).random().toString() + currentTimeMillis()
        while (true) {
            when {                                                              // 判断速度
                red.showTPA() * redRounds < blue.showTPA() * blueRounds -> {      // A 进攻 B
                    redRounds++
                    if (red.executeAction(blue, boundaryLine, logID).not()) {
                        BattleRecord().write(logID, Pair(blue.name, red.name), Pair(0, 0), Pair(0, 0))
                        break
                    }
                }
                red.showTPA() * redRounds > blue.showTPA() * blueRounds -> {      // B 进攻 A
                    blueRounds++
                    if (blue.executeAction(red, boundaryLine, logID).not()) {
                        BattleRecord().write(logID, Pair(blue.name, red.name), Pair(0, 0), Pair(0, 0))
                        break
                    }
                }// 相同再走一个短时间周期(如果时间周期相同则发起者先
                red.showTPA() < blue.showTPA() -> redRounds++
                red.showTPA() > blue.showTPA() -> blueRounds++
                else -> if ((0..1).random() == 0) redRounds++ else blueRounds++
            }
        }
        return logID
    }
}
