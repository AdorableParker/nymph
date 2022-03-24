package com.navigatorTB_Nymph.pluginData

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import java.time.LocalDateTime

object UsageStatistics : AutoSavePluginData("TB_UsageStatistics") {
    @ValueDescription("功能使用频率记录")
    private val tellTimeMode: MutableMap<Int, MutableMap<String, Int>> by value(
        mutableMapOf()
    )

    fun record(name: String) {
        val v = tellTimeMode.getOrPut(LocalDateTime.now().dayOfYear / 7) {
            mutableMapOf()
        }
        v[name] = v.getOrDefault(name, 0) + 1
    }
}