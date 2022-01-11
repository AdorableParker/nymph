package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object ActiveGroupList : AutoSavePluginData("ActiveGroup") {
    var user:MutableSet<Long> by value(mutableSetOf())
}