package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand


object RollDice : SimpleCommand(
    PluginMain, "rollDice", "判定",
    description = "简易骰娘"
) {
    override val usage: String = "${CommandManager.commandPrefix}判定 [P] <R> <D> <V>\n" +
            "Pip\t成功目标\n" +
            "Roll\t掷数-默认：1\n" +
            "Dice\t骰面数-默认：6\n" +
            "Verify\t检定计算方式-默认：细\n" +
            "检定计算方式可选参数:\n" +
            "总|统计点数和\n" +
            "均|统计点数平均\n" +
            "和|统计满足点数和\n" +
            "数|统计满足骰子数\n" +
            "细|提供掷骰细则自行计算"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(pip: Int, roll: Int = 1, dice: Int = 6, verify: String = "细") {
        record(primaryName)
        if (group.botMuteRemaining > 0) return

        if (roll <= 0 || dice <= 0) {
            sendMessage("参数错误：无法执行R${roll}D${dice}")
            return
        }
        val l = List(roll) { (1..dice).random() }
        sendMessage(when (verify) {
            "总" -> "P$pip,R${roll}D$dice=${l.sum()}(${if (l.sum() >= pip) "成功" else "失败"})"
            "均" -> "P$pip,R${roll}D$dice=${l.average()}(${if (l.average() >= pip) "成功" else "失败"})"
            "和" -> "P${pip}求和,R${roll}D$dice=${l.filter { it >= pip }.sum()}"
            "数" -> "P${pip}计数,R${roll}D$dice=${l.count { it >= pip }}"
            "细" -> List(roll) { if (l[it] >= pip) "=${l[it]}(成功)" else "=${l[it]}(失败)" }
                .joinToString(separator = "\n", prefix = "P$pip,R${roll}D$dice:\n")
            else -> "未知检定方法,可用检定方法：总|均|和|数|细"
        })
    }
}