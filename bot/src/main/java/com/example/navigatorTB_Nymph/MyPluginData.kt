package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object MyPluginData : AutoSavePluginData("TB_Data") { // "name" 是保存的文件名 (不带后缀)
    @ValueDescription("初始化状态,如果为True则会初始化重置所有用户数据")
    var initialization: Boolean by value(true)

    @ValueDescription("历史动态时间戳")
    val timeStampOfDynamic: MutableMap<Int, Long> by value(
        mutableMapOf(
            233114659 to 1L,
            161775300 to 1L,
            233108841 to 1L,
            401742377 to 1L
        )
    )

    @ValueDescription("UID对照表")
    val nameOfDynamic: MutableMap<Int, String> by value(
        mutableMapOf(
            233114659 to "AzurLane",
            161775300 to "ArKnights",
            233108841 to "FateGrandOrder",
            401742377 to "GenShin"
        )
    )

    @ValueDescription("报时模式对照表")
    val tellTimeMode: MutableMap<Int, String> by value(
        mutableMapOf(
            0 to "关闭",
            -1 to "标准",
            1 to "舰队Collection-中文",
            3 to "舰队Collection-日文",
            5 to "明日方舟",
            7 to "碧蓝航线",
            2 to "舰队Collection-音频",
            4 to "千恋*万花-音频(芳乃/茉子/丛雨/蕾娜)-音频"
        )
    )

    @ValueDescription("对决功能状态")
    val duelTime: MutableMap<Long, Long> by value(
        mutableMapOf()
    )

    @ValueDescription("随机图片功能状态")
    val AcgImageRun: MutableSet<Long> by value(
        mutableSetOf()
    )
//    var long: Long by value(0L) // 允许 var
//    var int by value(0) // 可以使用类型推断, 但更推荐使用 `var long: Long by value(0)` 这种定义方式.

//     带默认值的非空 map.
//     notnullMap[1] 的返回值总是非 null 的 MutableMap<Int, String>
//    var notnullMap by value<MutableMap<Int, MutableMap<Int, String>>>().withEmptyDefault()

//     可将 MutableMap<Long, Long> 映射到 MutableMap<Bot, Long>.
//    val botToLongMap: MutableMap<Bot, Long> by value<MutableMap<Long, Long>>().mapKeys(Bot::getInstance, Bot::id)
}