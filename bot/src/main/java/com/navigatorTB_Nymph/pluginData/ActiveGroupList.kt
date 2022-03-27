package com.navigatorTB_Nymph.pluginData

import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.sql.SQLiteJDBC
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object ActiveGroupList : AutoSavePluginData("ActiveGroup") {
    var user:MutableSet<Long> by value(mutableSetOf())

    /* 每天12点 更新激活状态*/
    fun activationStatusUpdate(flag: Boolean = true) {
        user = mutableSetOf()
        val dbObject = SQLiteJDBC(PluginMain.resolveDataPath("User.db"))
        dbObject.select("Responsible", "active", -1, 5).forEach {
            val uid = (it["group_id"] as Int).toLong()
            val active = it["active"] as Int
            if (active >= 0) {
                user.add(uid)
                if (flag) dbObject.update("Responsible", "group_id", uid, "active", active - 1)
            }
        }
        dbObject.closeDB()
    }
}