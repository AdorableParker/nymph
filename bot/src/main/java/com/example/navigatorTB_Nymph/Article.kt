package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object Article : AutoSavePluginData("Article") {
    var template: MutableSet<String> by value(mutableSetOf())
}