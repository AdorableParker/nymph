package com.example.nymph_TB_DLC

import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.nextEvent

class MirrorWorld {
    private suspend fun yesOrNo(theme: String, no: String): Boolean {
        ConsoleCommandSender.sendMessage("[10秒/3次]$theme<是/否>")
        for (i in 0..2) {
            when (nextEvent<GroupMessageEvent>(10_000) { it.sender == ConsoleCommandSender.user }.message.contentToString()) {
                "否" -> {
                    ConsoleCommandSender.sendMessage(no)
                    return true
                }
                "是" -> break
                else -> if (i > 0) ConsoleCommandSender.sendMessage("[20秒/${i}次]$theme<是/否>")
            }
        }
        return false
    }

    /** 玩家数据汇报 */
    suspend fun gamerInfo(subject: Contact) {
        ConsoleCommandSender.sendMessage("测试通过")
        val userData = MirrorWorldUser.userPermanent.getOrPut(subject.id) {
            PermanentData()
        }
        ConsoleCommandSender.sendMessage(userData.outInfo())
    }

    /** 玩家角色建立 */
    suspend fun characterCreation(subject: Contact) {
        ConsoleCommandSender.sendMessage("测试开始")
        val userData = MirrorWorldUser.userPermanent.getOrPut(subject.id) {
            PermanentData()
        }
        if (userData.pc != null) {
            if (yesOrNo("创建新角色？\n清除存档,不可恢复", "角色建立取消")) return
        }
        userData.pc = PlayerCharacter()
        ConsoleCommandSender.sendMessage(
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
    suspend fun apAllotted(subject: Contact) {
        ConsoleCommandSender.sendMessage("测试开始")
        val userData = MirrorWorldUser.userPermanent.getOrPut(subject.id) {
            PermanentData()
        }
        // 判断角色是否存在
        if (userData.pc == null) {
            ConsoleCommandSender.sendMessage("角色不存在，请建立角色后操作")
            return
        }
        // 判断是否可分配点数
        val ap = userData.pc!!.showAP()
        if (ap == 0) {
            ConsoleCommandSender.sendMessage("无可分配点数")
            return
        }
        // 开始分配点数
        ConsoleCommandSender.sendMessage("[30秒]请按照以下顺序输入点数分配方案(各项之间使用冒号分割)：\n力量：法力：智力:体质：速度：运气")
        val lt = arrayOf(6)
        val enter = false
        GetPlan@ while (!enter) {
            // 获取用户输入
            val plan =
                nextEvent<GroupMessageEvent>(30_000) { it.sender == ConsoleCommandSender.user }.message.contentToString()
                    .split(":", "：", limit = 7)
            // 判断输入数量
            if (plan.size < 6) {
                ConsoleCommandSender.sendMessage("[30秒]参数数量不匹配,请重新输入")
                continue
            }
            // 按顺序分配
            for (i in 0..5) {
                when (val apv = plan[i].toIntOrNull()) { //检查输入合法性
                    null -> {
                        ConsoleCommandSender.sendMessage("属性值必须为整数")
                        continue@GetPlan
                    }
                    in 0..7 -> lt[i] = apv
                    else -> {
                        ConsoleCommandSender.sendMessage("[30秒]每项最多为其分配7点,请重新输入")
                        continue@GetPlan
                    }
                }
            }
            // 检查点数用量
            if (ap < lt.sum()) {
                ConsoleCommandSender.sendMessage("该方案所用点数超出可用点数总量,请重新输入")
            }
            // 输出汇报
            val mod = Tool(lt)
            ConsoleCommandSender.sendMessage(
                """
                方案有效,生成角色属性预览如下
                等级: 1
                HP:${mod.draftHP()}\tMP:${mod.draftMP()}
                ATK:${mod.draftATK()}\tMAT:${mod.draftMAT()}
                ${mod.show6D()}
                """.trimIndent()
            )
        }
        // 用户确定
        if (yesOrNo(
                "确定当前方案？（剩余未分配的属性点将会以1属性点:${MirrorWorldConfig.ExchangeRate}金币的比例自动转化为金币值）\n确定属性后,不可再次更改",
                "点数分配取消"
            )
        ) return

        userData.pc!!.set6D(lt)
        ConsoleCommandSender.sendMessage("点数设定完成,可进行特性/技能学习")
    }
}