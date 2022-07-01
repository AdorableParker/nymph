package com.navigatorTB_Nymph.data

import kotlinx.serialization.Serializable

/**角色对象 */
@Serializable
class Role(val name: String) {
    private var gold = 0                                               //金币
    private var cube = 0                                               //魔方数量
    private var favorability = 200                                     //好感度

    fun getFavorability() = favorability

    /** 获取角色信息 */
    fun info() = "$name\n|金币:${gold}枚\n|魔方:${cube}个|好感度:$favorability‰"

    fun checkInReward(): String {
        val g = (100..2000).random()
        val c = when (g) {
            in 100..300 -> 2
            in 301..1500 -> 1
            else -> 0
        }
        gold.plus(g)
        cube.plus(c)
        return "获得${g}物资和${c}个魔方"
    }

    fun putUp(light: Boolean, count: Int): String {
        val (cubeValue, goalValue) = if (light) Pair(1 * count, 50 * count) else Pair(2 * count, 150 * count)
        return if (cube >= cubeValue && gold >= goalValue) {
            cube -= cubeValue
            gold -= goalValue
            ""
        } else "魔方或物资不足"
    }

    fun nudge() {
        favorability += TB.nudge()
    }

    fun chat() {
        favorability += TB.chat()
    }

    fun play() {
        favorability += TB.play()
    }

    fun abuse() {
        favorability += TB.abuse()
    }
}


