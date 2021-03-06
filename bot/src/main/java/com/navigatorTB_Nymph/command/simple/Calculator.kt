package com.navigatorTB_Nymph.command.simple

import com.navigatorTB_Nymph.pluginData.ActiveGroupList
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.PI

object Calculator : SimpleCommand(
    PluginMain, "Calculator", "计算器", "计算",
    description = "计算器"
) {
    override val usage = "${CommandManager.commandPrefix}计算 [中缀表达式]"

    @Handler
    suspend fun MemberCommandSenderOnMessage.main(express: String) {
        if (group.botMuteRemaining > 0) return
        if (group.id !in ActiveGroupList.user) {
            sendMessage("本群授权已到期,请续费后使用")
            return
        }
        UsageStatistics.record(primaryName)
        sendMessage(analysis(express))
    }

    private fun charToNum(list: MutableList<Char>) = list.joinToString("").toBigDecimalOrNull()
    private fun comparePriority(o1: Char, o2: Char): Boolean = getPriorityValue(o1) > getPriorityValue(o2)
    private fun getPriorityValue(str: Char): Int = when (str) {
        '+', '-' -> 1
        '*', '/', '%' -> 2
        else -> 0
    }

    private fun operation(back: BigDecimal, first: BigDecimal, sign: Char) = when (sign) {
        '+' -> first.add(back)
        '-' -> first.subtract(back)
        '*' -> first.multiply(back)
        '/' -> if (back != BigDecimal(0)) first.divide(back, 24, RoundingMode.HALF_UP) else "除数不能为零"
        '%' -> first.remainder(back)
        else -> null
    }

    private fun analysis(rpn: String): String {
        val stack = Stack<Char>()
        val stackRPN = Stack<BigDecimal>()
        val list: MutableList<Char> = ArrayList()
        var flag = true
        for (i in rpn.indices) {
            when {
                (rpn[i].toString()).matches(Regex("""[\d.]""")) -> list.add(rpn[i])
                rpn[i] == '(' -> if (list.isEmpty()) stack.push(rpn[i]) else return "错误,意外的运算符 '('"
                rpn[i] == ')' -> {
                    charToNum(list)?.let { stackRPN.push(it) } ?: let { return "错误,'${list.joinToString("")}'不是有效的数字" }
                    list.clear()

                    while (stack.isNotEmpty() && '(' != stack.lastElement()) {
                        val sign = stack.pop()
                        operation(stackRPN.pop(), stackRPN.pop(), sign)?.let {
                            if (it is BigDecimal) stackRPN.push(it) else return it.toString()
                        }
                            ?: let { return "错误,意外的运算符 '$sign',预期得到'+', '-', '*', '/', '%', '(', ')'" }
                    }
                    if (stack.isEmpty()) return "错误,意外的运算符 ')'"
                    stack.pop()
                    flag = false
                }
                (rpn[i].toString()).matches(Regex("""[+\-*/%π]""")) -> {
                    if (list.isEmpty()) {
                        when {
                            rpn[i] == 'π' -> {
                                stackRPN.push(BigDecimal(PI));continue
                            }
                            rpn[i] == '-' && flag -> {
                                stackRPN.push(BigDecimal(-1.0));stack.push('*')
                                continue
                            }
                            rpn[i] == '-' && !flag -> flag = true
                            else -> {
                                stack.push(rpn[i]);continue
                            }
                        }
                    } else {
                        charToNum(list)?.let { stackRPN.push(it) }
                            ?: let { return "错误,'${list.joinToString("")}'不是有效的数字" }
                        list.clear()
                        if (rpn[i] == 'π') {
                            stackRPN.push(
                                operation(BigDecimal(PI), stackRPN.pop(), '*')?.let {
                                    if (it is BigDecimal) it else return "运算异常"
                                }
                            )
                            continue
                        }
                        if (stack.isEmpty()) {
                            stack.push(rpn[i])
                            continue
                        }
                    }
                    while (!stack.isEmpty() &&
                        stack.lastElement() != '(' &&
                        !comparePriority(rpn[i], stack.lastElement())
                    ) {
                        val sign = stack.pop()
                        operation(
                            stackRPN.pop(),
                            stackRPN.pop(),
                            sign
                        )?.let { if (it is BigDecimal) stackRPN.push(it) else return it.toString() }
                            ?: let { return "错误,意外的运算符 '$sign',预期得到'+', '-', '*', '/', '%', '(', ')'" }
                    }
                    stack.push(rpn[i])
                }
                else -> return "错误,意外的字符 '${rpn[i]}'"
            }
        }
        if (list.isNotEmpty()) {
            charToNum(list)?.let { stackRPN.push(it) } ?: let { return "Error,illegal Number" }
            list.clear()
        }

        while (stack.isNotEmpty()) {
            val sign = stack.pop()
            operation(
                stackRPN.pop(),
                stackRPN.pop(),
                sign
            )?.let { if (it is BigDecimal) stackRPN.push(it) else return it.toString() }
                ?: let { return "错误,意外的运算符 '$sign',预期得到'+', '-', '*', '/', '%', '(', ')'" }
        }
        return stackRPN.pop().toString().replaceFirst(Regex("\\.0*$|(\\.\\d*?)0+$"), "$1")
    }
}