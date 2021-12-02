/*
package com.example.nymph_TB_DLC

import net.mamoe.mirai.utils.error
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarInputStream


//object DLCLoadManager : SimpleCommand(
//    DLC, "DLCManager", "DLC管理器",
//    description = "DLC管理器"
//) {
//    @Handler
//    suspend fun MemberCommandSenderOnMessage.main(comd: String) {
//        if (group.botMuteRemaining > 0) return
//        when (comd) {
//            "查询" -> DLCPerm().inquire(group)
//            "启用" -> DLCPerm()._enable_DLC(group)
//            "禁用" -> DLCPerm()._disable_DLC(group)
//        }
//    }
//}


class LoadDLC(){
    //返回 JarInputStream 中类名的数组列表
    @Throws(Exception::class)
    private fun getClassNamesFromJar(jarFile: JarInputStream): ArrayList<String> {
        val classNames: ArrayList<String> = ArrayList()
        try {
            //遍历 jar 文件的内容
            while (true) {
                val jar = jarFile.nextJarEntry ?: break
                //选择扩展名为 .class 的文件
                if (jar.name.endsWith(".class")) {
                    val className = jar.name.replace("/", "\\.")
                    val myClass = className.substring(0, className.lastIndexOf('.'))
                    classNames.add(myClass)
                }
            }
        } catch (e: Exception) {
            throw Exception("从 jar 获取类名时出错", e)
        }
        return classNames
    }


    // 获取 jar 文件中所有已加载类的数组列表
    @Throws(java.lang.Exception::class)
    fun loadJarFile(filePath: String): ArrayList<Class<*>> {
        val availableClasses = ArrayList<Class<*>>()
        val classNames = getClassNamesFromJar(JarInputStream(FileInputStream(filePath)))
        val classLoader = URLClassLoader(arrayOf<URL>(File(filePath).toURI().toURL()))
        for (className in classNames) {
            try {
                classLoader.loadClass(className).run {
                    if (this.isAnnotationPresent(DLCCommand::class.java)) availableClasses.add(this)
                }
            } catch (e: ClassNotFoundException) {
                DLC.logger.error{"找不到类 $className！\n${e.message}"}
            }
        }
        return availableClasses
    }
}
*/