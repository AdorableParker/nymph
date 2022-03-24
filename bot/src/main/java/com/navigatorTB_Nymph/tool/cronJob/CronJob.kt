package com.navigatorTB_Nymph.tool.cronJob

import com.navigatorTB_Nymph.data.Interval
import com.navigatorTB_Nymph.main.PluginMain
import kotlinx.coroutines.delay
import net.mamoe.mirai.utils.info
import java.time.LocalDateTime
import java.time.ZoneOffset

class CronJob {
    private var calibrationCountdown = 0 //校准次数
    private val timeAxis = mutableMapOf<Int, MutableList<Pair<Interval, suspend () -> Unit>>>() // 任务列表时间轴

    suspend fun start() {
        while (true) {
            val t = LocalDateTime.now()
            val serialNumber = t.dayOfYear * 10000 + t.hour * 100 + t.minute //序列号
            val jobList = timeAxis[serialNumber]
            if (jobList != null) {
                for (job in jobList) {
                    runCatching {
                        job.second()
                    }.onSuccess {
                        PluginMain.logger.info { "任务完成" }
                    }.onFailure {
                        PluginMain.logger.info { "任务失败\n${it.message}" }
                    }
                    if (job.first.isZero().not()) {
                        val next = getNext(serialNumber, t.year, job.first)
                        timeAxis.getOrPut(next) { mutableListOf() }.add(job)
                    }
                }
                timeAxis.remove(serialNumber)
            } else {
                if (calibrationCountdown >= 20) {
                    calibrationCountdown = 0
                    calibration(serialNumber)
                }
            }
            calibrationCountdown++
            delay(30000)
        }
    }

    private suspend fun calibration(nowSerialNumber: Int) {
        val l = mutableListOf<Int>()
        timeAxis.forEach { (serialNumber, _) -> if (serialNumber <= nowSerialNumber + 1) l.add(serialNumber) }
        l.forEach {
            for (job in timeAxis[it]!!) {
                val next =
                    if (job.first.isZero()) getNext(it, LocalDateTime.now().year, Interval(0, 0, 3)) else getNext(
                        it,
                        LocalDateTime.now().year,
                        job.first
                    )
                timeAxis.getOrPut(next) { mutableListOf() }.add(job)
            }
            timeAxis.remove(it)
        }

        delay(
            LocalDateTime.now().plusMinutes(1).toEpochSecond(ZoneOffset.UTC) -
                    LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000
        )
    }

    fun addJob(startTime: Int, interval: Interval, job: suspend () -> Unit) {
        if (timeAxis.containsKey(startTime)) {
            timeAxis[startTime]!!.add(Pair(interval, job))
        } else {
            timeAxis[startTime] = mutableListOf(Pair(interval, job))
        }
    }

    fun getNext(serial: Int, year: Int, interval: Interval): Int {
        var m = serial % 100
        var h = (serial - m) % 10000 / 100
        var d = (serial - h - m) / 10000
        m += interval.min
        h += interval.hour
        d += interval.day
        if (m >= 60) {
            m -= 60
            h++
        }
        if (h >= 24) {
            h -= 24
            d++
        }
        if (d >= 366) {
            if (year % 4 != 0 || d == 367) { // 如果不是闰年 或 是第367天
                d = 1
            }
        }
        return d * 10000 + h * 100 + m
    }

    fun query(deadline: Int): String {
        var l = "序列号 | 任务数\n--------|---------"
        timeAxis.forEach { (serialNumber, jobList) ->
            if (serialNumber <= deadline) {
                l += "\n$serialNumber|${jobList.size}"
            }
        }
        return l
    }
}