package com.example.nymph_TB_DLC

import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand


object DLCLoadManager : SimpleCommand(
    DLC, "DLCManager", "DLC管理器",
    description = "DLC管理器"
) {
    @Handler
    suspend fun MemberCommandSenderOnMessage.main(comd: String) {
        if (group.botMuteRemaining > 0) return
        when (comd) {
            "查询" -> DLCPerm().inquire(group)
            "启用" -> DLCPerm()._enable_DLC(group)
            "禁用" -> DLCPerm()._disable_DLC(group)
        }
    }
}
