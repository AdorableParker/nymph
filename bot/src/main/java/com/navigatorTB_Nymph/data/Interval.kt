package com.navigatorTB_Nymph.data

data class Interval(val day: Int = 0, val hour: Int = 0, val min: Int = 0) {
    fun isLoopToDo(): Boolean = (day + hour + min) != 0
}