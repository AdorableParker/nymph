package com.navigatorTB_Nymph.defaultJob

import com.navigatorTB_Nymph.data.Interval
import com.navigatorTB_Nymph.tool.cronJob.PeriodicTask
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.At

class AtMe(val group: Group, val user: Member) : PeriodicTask("@某人", "一个群员要求@", Interval()) {
    override suspend fun run() {
        if (group.botMuteRemaining <= 0) {
            group.sendMessage(At(user) + "时间到了")
        }
    }
}
