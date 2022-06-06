package com.navigatorTB_Nymph.defaultJob

import com.navigatorTB_Nymph.data.Interval
import com.navigatorTB_Nymph.tool.cronJob.PeriodicTask
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isOperator

class UnMute(val group: Group, val user: Member) : PeriodicTask("取消禁言", "取消对某人的禁言", Interval()) {
    override suspend fun run() {
        if (group.botPermission.isOperator()) {
            user.mute(0)
        }
    }
}
