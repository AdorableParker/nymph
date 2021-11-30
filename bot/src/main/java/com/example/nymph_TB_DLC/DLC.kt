/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:29
 */

package com.example.nymph_TB_DLC


import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin


object DLC : KotlinPlugin(
    JvmPluginDescription(
        id = "MCP.TB_DLC",
        version = "0.0.1",
        name = "name"
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

        sendMessage("测试通过")
        val userData = MirrorWorldUser.userPermanent.getOrPut(user.id) {
            PermanentData()
        }
        if (userData.pc == null) {
            userData.pc = PlayerCharacter()
        } else {
            sendMessage("已存在角色,是否覆盖存档？")
            val yes = true
            if (yes) {
                sendMessage("角色已销毁")
            } else {
                sendMessage("角色建立取消")
                return
            }
        }
        sendMessage(
            """角色已建立,可以开始分配属性点
        现在拥有${userData.pc!!.showAP()}点属性点可分配至
        力量：与物攻相关
        法力：与法术相关
        智力:与经验获取,战斗效率相关
        体质：与生命值相关
        速度：与行动速度相关
        运气：与事件判定相关
        六项基本属性,确定属性前随时可以查看并改变
        """.trimIndent()
        )
        //TODO:需要支持上下文命令
    }
}

object APAllot : SimpleCommand(
    DLC, "属性点分配",
    description = "分配属性点"
) {
    @Handler

    suspend fun MemberCommandSenderOnMessage.main() {

        sendMessage("测试通过")
        val userData = MirrorWorldUser.userPermanent.getOrPut(user.id) {
            PermanentData()
        }
        if (userData.pc == null) {
            sendMessage("角色不存在，请建立角色后操作")
            return
        }

        //TODO:需要支持上下文命令
    }
}

