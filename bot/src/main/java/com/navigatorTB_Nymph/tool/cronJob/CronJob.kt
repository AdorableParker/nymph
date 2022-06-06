package com.navigatorTB_Nymph.tool.cronJob

import com.navigatorTB_Nymph.pluginMain.PluginMain
import kotlinx.coroutines.delay
import net.mamoe.mirai.utils.info
import java.time.LocalDateTime

object CronJob {

    private val timeAxis = mutableMapOf<Int, MutableList<PeriodicTask>>() // 任务列表时间轴

    suspend fun start() {
        while (true) {
            val nowTime = LocalDateTime.now()
            val serialNumber = nowTime.dayOfYear * 10000 + nowTime.hour * 100 + nowTime.minute //序列号
            timeAxis.filter { serialNumber >= it.key }.forEach { (key, jobList) ->
                for (job in jobList) {
                    runCatching {
                        job.run()
                    }.onSuccess {
                        PluginMain.logger.info { "任务完成" }
                    }.onFailure {
                        PluginMain.logger.info { "任务失败\n${it.message}" }
                    }
                    if (job.interval.isLoopToDo()) timeAxis.getOrPut(job.getNext(nowTime)) { mutableListOf() }.add(job)
                }
                timeAxis.remove(key)
            }
            delay(30000)
        }
    }

    fun addJob(startTime: Int, job: PeriodicTask) {
        timeAxis.getOrPut(startTime) { mutableListOf() }.add(job)
    }

    fun query(): String {
        var l = "序列号 | 任务数\n--------|---------"
        timeAxis.forEach { (serialNumber, jobList) ->
            l += "\n$serialNumber|${jobList.size}"
        }
        return l
    }

    fun query(deadline: Int): String =
        timeAxis[deadline]?.joinToString("\n", "序列${deadline}任务有:\n") { "${it.name}:${it.synopsis}" }
            ?: "序列${deadline}无任务"

}