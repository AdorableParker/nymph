package com.navigatorTB_Nymph.pluginData

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object PushBoxLevelMap : AutoSavePluginData("PushBoxLevelMap") {
    fun upLevel(id: Long) {
        groupCompleteLevel[id] = groupCompleteLevel.getOrPut(id) { 0 } + 1
    }

    val gameMap: Array<String> by value(
        arrayOf(
            "==###=====#.#=====#-#######%-%.##.-%@#######%#=====#.#=====###==",
            "#####====#@--#====#-%%#=####-%-#=#.####-###.#=##----.#=#---#--#=#---####=#####===",
            "=########===#------#####%###----##-@-%---%-##-..#--%-####..#----#==#########="
        )
    )
    val mapSize: Array<Pair<Int, Int>> by value(arrayOf(Pair(8, 8), Pair(9, 9), Pair(11, 7)))

    val groupCompleteLevel: MutableMap<Long, Int> by value(mutableMapOf())
}