/*
 * Copyright (c) 2021.
 * 作者: AdorableParker
 * 最后编辑于: 2021/5/2 下午6:41
 */

package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.commandPrefix
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand

// 舰船经验计算器
object CalculationExp : SimpleCommand(
    PluginMain, "calculationExp", "舰船经验", "经验计算",
    description = "舰船经验计算器"
) {
    override val usage: String = "${commandPrefix}舰船经验 [当前等级] [目标等级|已有经验] <是否为决战方案>\n <是否决战方案> 参数只接受“true”及其大小写变体"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(current_level: Int, lvOrExp: Int, special: Boolean = false) {
        record(primaryName)
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        if (lvOrExp <= 125) {
            val balance = (current_level until lvOrExp).fold(0) { accExp: Int, level: Int ->
                val result = accExp + calculateParts(level, special)
                result
            }
            sendMessage("当前等级:$current_level,目标等级:$lvOrExp\n是否为决战方案:$special\n最终计算结果: 需${balance}EXP可以达成目标等级")
        } else {
            var level = current_level
            var exp = lvOrExp
            while (exp > 0 && level <= 124) {
                exp -= calculateParts(level, special)
                level++
            }
            sendMessage("当前等级:$current_level\n已有经验:$lvOrExp\n是否为决战方案:$special\n最终计算结果:当前已有经验最高可到Lv.$level")
        }
    }

    /**参数不匹配时输出提示 */
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        sendMessage("参数不匹配, 你是否想执行:\n $usage")
    }

    private fun calculateParts(target_level: Int, special: Boolean): Int {
        val totalExp = when (target_level) {
            in 0..40 -> target_level
            in 41..60 -> 2 * target_level - 40
            in 61..70 -> 3 * target_level - 100
            in 71..80 -> 4 * target_level - 170
            in 81..90 -> 5 * target_level - 250
            in 91..92 -> 10 * target_level - 700
            in 93..94 -> 20 * target_level - 1620
            in 95..95 -> 300
            in 96..97 -> 50 * target_level - 4450
            in 98..98 -> 600
            in 99..99 -> 1320
            in 100..105 -> 30 * target_level - 2500
            in 106..110 -> 60 * target_level - 5650
            in 111..115 -> 100 * target_level - 10050
            in 116..120 -> 150 * target_level - 15800
            in 120..124 -> 210 * target_level - 23000
            else -> return 3000000
        }
        return if (special) {
            totalExp * if (target_level in 90..99) 130 else 120
        } else {
            totalExp * 100
        }
    }
}