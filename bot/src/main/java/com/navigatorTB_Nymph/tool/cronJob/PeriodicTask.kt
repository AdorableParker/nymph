package com.navigatorTB_Nymph.tool.cronJob

import com.navigatorTB_Nymph.data.Interval
import java.time.LocalDateTime

abstract class PeriodicTask(val name: String, val synopsis: String?, val interval: Interval) {
    abstract suspend fun run()

    fun getNext(nowTime: LocalDateTime): Int {
        var m = interval.min + nowTime.minute
        var h = interval.hour + nowTime.hour
        var d = interval.day + nowTime.dayOfYear
        if (m >= 60) {
            m -= 60
            h++
        }
        if (h >= 24) {
            h -= 24
            d++
        }

        val newDay = d % if (nowTime.year % 4 == 0 && nowTime.year % 100 != 0 || nowTime.year % 400 == 0) 367 else 366
        return (if (newDay == 0) newDay + 1 else newDay) * 10000 + h * 100 + m
    }
}