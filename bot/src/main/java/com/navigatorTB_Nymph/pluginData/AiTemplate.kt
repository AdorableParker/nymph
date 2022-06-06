package com.navigatorTB_Nymph.pluginData

import com.navigatorTB_Nymph.data.QAPair
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object AiTemplate : AutoSavePluginData("AiTemplate") {
    @ValueDescription("各群的问答模板")
    val QASheet: MutableMap<Long, MutableSet<QAPair>> by value(
        mutableMapOf()
    )
}


