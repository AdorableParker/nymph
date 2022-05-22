package com.navigatorTB_Nymph.command.dlc.data

import kotlinx.serialization.Serializable

@Serializable
/** 血条
 * @param[max]最大值
 */
data class Bar(var max: Int) {
    /**
     * 当前值
     */
    var current: Int = max

    /** 减少 */
    fun harm(v: Int): Boolean {
        return if (current >= v) {
            current -= v
            true
        } else {
            current = 0
            false
        }
    }

    /** 安全减少 */
    fun safeHarm(v: Int): Boolean {
        return if (current <= v) false else harm(v)
    }

    /** 恢复 */
    fun treatment(v: Int): Int {
        current = if (current + v < max) {
            current + v
        } else {
            max
        }
        return current
    }

    fun full() {
        current = max
    }
}