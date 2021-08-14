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
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiExperimentalApi

// 舰船经验计算器
@MiraiExperimentalApi
@ConsoleExperimentalApi
object CalculationExp : SimpleCommand(
    PluginMain, "calculationExp", "舰船经验", "经验计算",
    description = "舰船经验计算器"
) {
    override val usage: String = "${commandPrefix}舰船经验 [当前等级] [目标等级|已有经验] <是否为决战方案>\n <是否决战方案> 参数只接受“true”及其大小写变体"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(current_level: Int, lvOrExp: Int, special: Boolean = false) {
        record(primaryName)
        if (lvOrExp <= 120) {
            val balance = (current_level until lvOrExp).fold(0) { accExp: Int, level: Int ->
                val result = accExp + calculateParts(level, special)
                result
            }
            sendMessage("当前等级:$current_level,目标等级:$lvOrExp\n是否为决战方案:$special\n最终计算结果: 需${balance}EXP可以达成目标等级")
        } else {
            var level = current_level
            var exp = lvOrExp
            while (exp > 0 && level <= 119) {
                exp -= calculateParts(level, special)
                level++
            }
            sendMessage("当前等级:$current_level\n已有经验:$lvOrExp\n是否为决战方案:$special\n最终计算结果:当前已有经验最高可到Lv.${level - 1}")
        }
    }

    /**参数不匹配时输出提示 */
    @Handler
    suspend fun MemberCommandSenderOnMessage.main() {
        sendMessage("参数不匹配, 你是否想执行:\n $usage")
    }

    private fun calculateParts(target_level: Int, special: Boolean): Int {
        val totalExp = when (target_level) {
            in 0..40 -> target_level * 10
            in 41..60 -> 400 + (target_level - 40) * 20
            in 61..70 -> 800 + (target_level - 60) * 30
            in 71..80 -> 1100 + (target_level - 70) * 40
            in 81..90 -> 1500 + (target_level - 80) * 50
            in 101..104 -> 7000 + (target_level - 100) * 200
            in 106..110 -> 8500 + (target_level - 105) * 1200
            in 111..115 -> 14500 + (target_level - 110) * 1800
            in 116..119 -> 23500 + (target_level - 115) * 2100
            else -> when (target_level) {
                91 -> 2100
                92 -> 2200
                93 -> 2400
                94 -> 2600
                95 -> 3000
                96 -> 3500
                97 -> 4000
                98 -> 6000
                99 -> 13200
                100 -> 7000
                105 -> 8500
                120 -> return 3000000
                else -> 0
            }
        }
        return if (special) {
            totalExp * if (target_level in 90..99) 13 else 12
        } else {
            totalExp * 10
        }
    }
}