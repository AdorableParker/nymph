package com.navigatorTB_Nymph.command.dlc.tool

/** 战斗记录 */
class BattleRecordTool {
    companion object {
        val combatRecord = mutableMapOf<String, ArrayList<String>>()
        val resultsReport = mutableMapOf<String, String>()
    }

    /** 写入战斗记录*/
    fun write(id: String, t: String) {
        val log = combatRecord.getOrPut(id) { arrayListOf() }
        log.add(t)
    }

    /** 写入战果记录*/
    fun write(id: String, name: Pair<String, String>, exp: Pair<Int, Int>, gold: Pair<Int, Int>) {
        var r1 = "${name.first}取得本次战斗胜利"
        var r2 = "\n${name.second}"
        if (exp.first != 0) {
            r1 += ",获得${exp.first}经验"
            r2 += "获得${exp.second}经验,"
        }
        if (gold.first != 0) {
            r1 += ",获得${gold.first}金币"
            r2 += "失去${gold.second}金币"
        }
        resultsReport[id] = r1 + r2
    }

    /** 读取战斗记录*/
    fun read(id: String): String {
        val log = combatRecord.getOrElse(id) { return "无战斗记录" }
        val outLog = StringBuilder()
        for ((i, t) in log.withIndex()) outLog.append(if (i % 2 == 0) "第${i / 2 + 1}回合:$t," else "$t。\n")
        combatRecord.remove(id)
        return outLog.toString()
    }

    /** 读取战果记录 */
    fun readResults(id: String) = resultsReport.remove(id) ?: "无战果记录"

}