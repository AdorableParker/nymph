/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:29
 */

package com.example.nymph_TB_DLC


import com.example.nymph_TB_DLC.MirrorWorldConfig.ExchangeRate
import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.nextEvent


object DLC : KotlinPlugin(
    JvmPluginDescription(
        id = "MCP.TB_DLC",
        version = "0.0.1",
        name = "TB_DLC-MirrorWorld"
    ) { dependsOn("MCP.navigatorTB_Nymph", "0.15.0") }) {

    override fun onEnable() {
        // 从数据库自动读
        MirrorWorldUser.reload()
        MirrorWorldAssets.reload()

        PermanentInfo.register()  //用户信息
        PermanentBuild.register() //建立角色
        APAllot.register()        //分配点数
    }

    override fun onDisable() {
        DLC.cancel()
    }
}

private suspend fun MemberCommandSenderOnMessage.yesOrNo(theme: String, no: String): Boolean {
    sendMessage("[10秒/3次]$theme<是/否>")
    for (i in 0..2) {
        when (nextEvent<GroupMessageEvent>(10_000) { it.sender == this.user }.message.contentToString()) {
            "否" -> {
                sendMessage(no)
                return true
            }
            "是" -> break
            else -> if (i > 0) sendMessage("[20秒/${i}次]$theme<是/否>")
        }
    }
    return false
}

class DLCPerm {
    fun inquire(group: Group): Boolean = DLCPermData.dlc_permList.contains(group)
    fun _enable_DLC(group: Group) = DLCPermData.dlc_permList.add(group)
    fun _disable_DLC(group: Group): Boolean = DLCPermData.dlc_permList.remove(group)
}


object PermanentInfo : SimpleCommand(
    DLC, "我的信息",
    description = "用户信息"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        sendMessage("测试通过")
        val userData = MirrorWorldUser.userPermanent.getOrPut(user.id) {
            PermanentData()
        }
        sendMessage(userData.outInfo())
    }
}

object PermanentBuild : SimpleCommand(
    DLC, "建立角色",
    description = "玩家角色建立"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        sendMessage("测试开始")
        val userData = MirrorWorldUser.userPermanent.getOrPut(user.id) {
            PermanentData()
        }
        if (userData.pc != null) {
            if (yesOrNo("创建新角色？\n清除存档,不可恢复", "角色建立取消")) return
        }
        userData.pc = PlayerCharacter()
        sendMessage(
            """角色已建立,可以开始分配属性点
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
}

object APAllot : CompositeCommand(
    DLC, "属性点",
    description = "分配属性点"
) {
    @SubCommand("分配")
    suspend fun MemberCommandSenderOnMessage.main() {
        sendMessage("测试开始")
        val userData = MirrorWorldUser.userPermanent.getOrPut(user.id) {
            PermanentData()
        }
        // 判断角色是否存在
        if (userData.pc == null) {
            sendMessage("角色不存在，请建立角色后操作")
            return
        }
        // 判断是否可分配点数
        val ap = userData.pc!!.showAP()
        if (ap == 0) {
            sendMessage("无可分配点数")
            return
        }
        // 开始分配点数
        sendMessage("[30秒]请按照以下顺序输入点数分配方案(各项之间使用冒号分割)：\n力量：法力：智力:体质：速度：运气")
        val lt = arrayOf(6)
        val enter = false
        GetPlan@ while (!enter) {
            // 获取用户输入
            val plan = nextEvent<GroupMessageEvent>(30_000) { it.sender == user }.message.contentToString()
                .split(":", "：", limit = 7)
            // 判断输入数量
            if (plan.size < 6) {
                sendMessage("[30秒]参数数量不匹配,请重新输入")
                continue
            }
            // 按顺序分配
            for (i in 0..5) {
                when (val apv = plan[i].toIntOrNull()) { //检查输入合法性
                    null -> {
                        sendMessage("属性值必须为整数")
                        continue@GetPlan
                    }
                    in 0..7 -> lt[i] = apv
                    else -> {
                        sendMessage("[30秒]每项最多为其分配7点,请重新输入")
                        continue@GetPlan
                    }
                }
            }
            // 检查点数用量
            if (ap < lt.sum()) {
                sendMessage("该方案所用点数超出可用点数总量,请重新输入")
            }
            // 输出汇报
            val mod = Tool(lt)
            sendMessage(
                """方案有效,生成角色属性预览如下
                等级:\t1
                HP:${mod.draftHP()}\tMP:${mod.draftMP()}
                ATK:${mod.draftATK()}\tMAT:${mod.draftMAT()}
                ${mod.show6D()}
                """.trimIndent()
            )
        }
        // 用户确定
        if (yesOrNo(
                "确定当前方案？（剩余未分配的属性点将会以1属性点:${ExchangeRate}金币的比例自动转化为金币值）\n确定属性后,不可再次更改",
                "点数分配取消"
            )
        ) return

        userData.pc!!.set6D(lt)
        sendMessage("点数设定完成,可进行特性/技能学习")
    }
}

