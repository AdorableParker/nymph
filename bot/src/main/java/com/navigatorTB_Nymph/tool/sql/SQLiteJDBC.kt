package com.navigatorTB_Nymph.tool.sql

import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.warning
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class SQLiteJDBC(DbPath: Path) {
    private val connection: Connection? = runCatching {
        DriverManager.getConnection("jdbc:sqlite:$DbPath")
    }.onFailure {
        PluginMain.logger.error { "申请数据库失败,异常信息:\n${it.message}" }
    }.getOrNull()

    /**
     * 用于执行非查询语句
     */
    private fun executeUpdateSQL(sql: String, log: String): Int {
        if (connection == null) return -1
        val statement = connection.createStatement()
        return runCatching {
            val r = statement.executeUpdate(sql)
            statement.close()
            r
        }.onFailure {
            statement.close()
            PluginMain.logger.warning { "$log\n${it.message}" }
            return -1
        }.getOrDefault(-1)
    }

    /**
     * 创建表
     * 以[sql]作为SQL语句创建表
     */
    fun createTable(sql: String, log: String) {
        if (executeUpdateSQL(sql, "File:SQLiteJBDC.kt\tLine:45") < 0) {
            PluginMain.logger.warning { "$log\n执行SQL创建表操作异常" }
        }
    }

    /**
     * 插入
     * 以[column]作为目标字段名，[value]作为目标值插入目标表[table]内
     */
    fun insert(table: String, column: Array<String>, value: Array<String>, log: String) {
        val sql = "INSERT INTO $table " +
                "${column.joinToString(",", "(", ")")} VALUES " +
                "${value.joinToString(",", "(", ")")};"
        if (executeUpdateSQL(sql, "File:SQLiteJBDC.kt\tLine:58") < 0) {
            PluginMain.logger.warning { "$log\nSQL:$sql\n执行SQL插入操作异常" }
        }
    }

    /**
     * 更改
     * 将目标表[table]内的所有记录中符合条件[limitWord]的记录更改为[newData]
     *
     * UPDATE $table SET $Array.key = $Array.data WHERE $limitWord.A = $limitWord.B;
     **/
    fun update(
        table: String,
        limitWord: Pair<String, String>,
        newData: Pair<Array<String>, Array<String>>,
        log: String
    ) {
        val sql = "UPDATE $table SET " +
                "${newData.first.joinToString(",", "(", ")")} = " +
                "${newData.second.joinToString(",", "(", ")")} WHERE " +
                "${limitWord.first} = ${limitWord.second};"
        if (executeUpdateSQL(sql, "File:SQLiteJBDC.kt\tLine:74") < 0) {
            PluginMain.logger.warning { "$log\nSQL:$sql\n执行SQL更改操作异常" }
        }
    }

    /**
     * 删除
     * 删除目标表[table]内符合条件[limitWord]的记录
     */
    fun delete(table: String, limitWord: Pair<String, String>, log: String) {
        val sql = "DELETE FROM $table WHERE ${limitWord.first} = ${limitWord.second};"
        if (executeUpdateSQL(sql, "File:SQLiteJBDC.kt\tLine:93") < 0) {
            PluginMain.logger.warning { "$log\nSQL:$sql\n执行SQL删除操作异常" }
        }
    }

//    fun delete(table: String, column: List<String>, value: List<String>, conjunction: String) {
//        val valueIterator = value.iterator()
//        val determiner: MutableList<String> = ArrayList()
//        column.forEach { determiner.add("$it = ${valueIterator.next()}") }
//        val sql = "DELETE FROM $table WHERE ${determiner.joinToString(" $conjunction ")};"
//        if (executeSQL(sql) < 0) {
//            PluginMain.logger.warning { "File:SQLiteJBDC.kt\tLine:110\n执行SQL删除操作异常" }
//        }
//    }

    /**
     * 单条返回操作
     */
    fun selectOne(table: String, limitWord: Triple<String, String, String>, log: String): MutableMap<String, Any?> {
        val row = mutableMapOf<String, Any?>()
        if (connection == null) return row
        val statement = connection.createStatement()
        runCatching {
            statement.executeQuery("SELECT * FROM $table WHERE ${limitWord.first} ${limitWord.second} ${limitWord.third};")
                .apply {
                    if (next()) (1..metaData.columnCount).forEach { row[metaData.getColumnName(it)] = getObject(it) }
                }.close()
        }.onFailure { PluginMain.logger.error { "$log\n${it.message}" } }
        statement.close()
        return row
    }

    /**
     * 多个条件之间使用连接词[conjunction]连接
     * @param [table] 目标表名
     * @param [limitWord] [key, infix ,value]
     * @param [conjunction] 连接词
     */
    fun selectOne(
        table: String,
        limitWord: Triple<Array<String>, Array<String>, Array<String>>,
        conjunction: String,
        log: String
    ): MutableMap<String, Any?> {
        val limit =
            List(limitWord.first.size) { "${limitWord.first[it]} ${limitWord.second[it]} ${limitWord.third[it]}" }
        val row = mutableMapOf<String, Any?>()
        if (connection == null) return row
        val statement = connection.createStatement()
        runCatching {
            statement.executeQuery("SELECT * FROM $table WHERE ${limit.joinToString(" $conjunction ")};").apply {
                if (next()) (1..metaData.columnCount).forEach { row[metaData.getColumnName(it)] = getObject(it) }
            }.close()
        }.onFailure { PluginMain.logger.error { "$log\n${it.message}" } }
        statement.close()
        return row
    }

    fun selectRandom(table: String, log: String): MutableMap<String, Any?> {
        val row = mutableMapOf<String, Any?>()
        if (connection == null) return row
        val statement = connection.createStatement()
        runCatching {
            statement.executeQuery("SELECT * FROM $table ORDER BY RANDOM() LIMIT 1").apply {
                while (next()) (1..metaData.columnCount).forEach { row[metaData.getColumnName(it)] = getObject(it) }
            }.close()
        }.onFailure { PluginMain.logger.warning { "$log\n${it.message}" } }
        statement.close()
        return row
    }

    /**
     * limitWord[key, infix ,value]
     */
    fun select(
        table: String,
        limitWord: Triple<String, String, String>,
        log: String
    ): MutableList<MutableMap<String, Any?>> {
        val sql = "SELECT * FROM $table WHERE ${limitWord.first} ${limitWord.second} ${limitWord.third};"
        return executeQuerySQL(sql, "$log\nSQL:$sql\n执行SQL查询操作异常")
    }


    /**
     * 多个条件之间使用连接词[conjunction]连接
     * @param [table] 目标表名
     * @param [limitWord] [key, infix ,value]
     * @param [conjunction] 连接词
     */
    fun select(
        table: String,
        limitWord: Triple<Array<String>, Array<String>, Array<String>>,
        conjunction: String,
        log: String
    ): MutableList<MutableMap<String, Any?>> {
        val limit =
            List(limitWord.first.size) { "${limitWord.first[it]} ${limitWord.second[it]} ${limitWord.third[it]}" }
        val sql = "SELECT * FROM $table WHERE ${limit.joinToString(" $conjunction ")};"
        return executeQuerySQL(sql, "$log\nSQL:$sql\n执行SQL查询操作异常")
    }

    /**
     * 执行给定的查询语句
     */
    fun executeQuerySQL(sql: String, log: String): MutableList<MutableMap<String, Any?>> {
        val resultList: MutableList<MutableMap<String, Any?>> = ArrayList()
        if (connection == null) return resultList
        val statement = connection.createStatement()
        runCatching {
            statement.executeQuery(sql).apply {
                while (next()) {
                    val row = mutableMapOf<String, Any?>()
                    (1..metaData.columnCount).forEach { row[metaData.getColumnName(it)] = getObject(it) }
                    resultList.add(row)
                }
            }.close()
        }.onFailure { PluginMain.logger.warning { "$log\n${it.message}" } }
        statement.close()
        return resultList
    }

    /**
     * 关闭数据库
     */
    fun closeDB() {
        connection?.close()
    }
}