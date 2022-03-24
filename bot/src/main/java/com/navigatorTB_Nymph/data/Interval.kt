package com.navigatorTB_Nymph.data

data class Interval(val day: Int, val hour: Int = 0, val min: Int = 0) {
    fun isZero(): Boolean = day == 0 && hour == 0 && min == 0
}