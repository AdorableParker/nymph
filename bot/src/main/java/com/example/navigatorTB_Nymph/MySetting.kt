package com.example.navigatorTB_Nymph

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object MySetting : AutoSavePluginConfig("TB_Setting") {

    @ValueDescription("名字")
    val name by value("领航员-TB")

    @ValueDescription("Bot 账号")
    val BotID by value(123456L)

    @ValueDescription("SauceNAO 的 API Key")
    val SauceNAOKey by value("")

    @ValueDescription("超级管理员账号")
    val AdminID by value(123456L)

    @ValueDescription("手动查询动态UID简称列表")
    val DynamicNameList by value(
        mapOf(
            "碧蓝" to 233114659,
            "方舟" to 161775300,
            "FGO" to 233108841,
            "原神" to 401742377
        )
    )

    @ValueDescription("Pixiv代理域名")
    val proxy: String by value("i.pixiv.cat")
//    @ValueDescription("图床API")
//    val ImageHostingService by value("")

    @ValueDescription("违禁词")
    val prohibitedWord: List<String> by value(listOf("傻逼"))

    @ValueDescription("免打扰时间段:0-23")
    val undisturbed: List<Int> by value(listOf(-1))

    @ValueDescription("启用常驻定时任务")
    val resident: Boolean by value(false)
    //    @ValueDescription("数量") // 注释写法, 将会保存在 MySetting.yml 文件中.
//    var count by value(0)
//    val nested by value<MyNestedData>() // 嵌套类型是支持的
}