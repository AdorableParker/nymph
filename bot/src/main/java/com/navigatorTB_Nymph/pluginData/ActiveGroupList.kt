package com.navigatorTB_Nymph.pluginData

import com.navigatorTB_Nymph.data.UserResponsible
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object ActiveGroupList : AutoSavePluginData("ActiveGroup") {
    var user: MutableSet<Long> by value(mutableSetOf())

    /* 每天12点 更新激活状态*/
    fun activationStatusUpdate(flag: Boolean = true) {
        user = mutableSetOf()
        SQLiteJDBC(PluginMain.resolveDataPath("User.db")).apply {
            select("Responsible", Triple("active", "!=", "-1"), "更新激活状态\nFile:ActiveGroupList.kt\tLine:17").run {
                List(size) { UserResponsible(this[it]) }.filter { it.active >= 0 }.forEach {
                    user.add(it.groupID)
                    if (flag) update(
                        "Responsible",
                        Pair("groupID", "${it.groupID}"),
                        Pair(arrayOf("active"), arrayOf("${it.active - 1}")),
                        "更新激活状态\nFile:ActiveGroupList.kt\tLine:20"
                    )
                }
            }
        }.closeDB()
    }
}