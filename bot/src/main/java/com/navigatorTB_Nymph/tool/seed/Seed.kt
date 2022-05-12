package com.navigatorTB_Nymph.tool.seed

import com.navigatorTB_Nymph.tool.seed.Cycle.*
import java.time.LocalDateTime
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

object Seed {
    fun getSeed(uid: Long, cycle: Cycle): Random {
        val today = LocalDateTime.now()

        return Random(
            when (cycle) {
                Hour -> today.hour * uid.digits() + uid + today.dayOfYear
                Day -> today.dayOfYear * uid.digits() + uid
                Week -> (today.dayOfYear * 10 + (today.dayOfMonth - today.dayOfWeek.value + 6) / 7 + 1) * uid.digits() + uid
                Month -> (today.monthValue * 1000 + today.year) * uid.digits() + uid
            }
        )
    }

    private fun Long.digits() = when (this) {
        0L -> 10
        else -> 10.0.pow(log10(absoluteValue.toDouble()) + 1).toInt()
    }
}