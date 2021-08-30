package com.example.navigatorTB_Nymph

import com.example.navigatorTB_Nymph.UsageStatistics.record
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.debug
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@MiraiExperimentalApi
@ConsoleExperimentalApi
object MinesweeperGame : CompositeCommand(
    PluginMain, "MinesweeperGame", "扫雷",
    description = "扫雷游戏实现"
) {
//    override val usage: String = ""

    @SubCommand("开始", "新游戏")
    suspend fun MemberCommandSenderOnMessage.start(level: Int, punishment: Boolean = false) {
        if (punishment) if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法创建带有禁言惩罚的游戏")
            return
        } else sendMessage("警告！本局游戏开启失败禁言惩罚")

        val minesweeperGame = Minesweeper(
            when {
                level > 3 -> 3
                level < 1 -> 1
                else -> level
            }, punishment = punishment
        )
        PluginMain.GAME[group.id] = minesweeperGame
        subject.sendImage(minesweeperGame.getImage())
    }

    @SubCommand("开始", "新游戏")
    suspend fun MemberCommandSenderOnMessage.start(width: Int, high: Int, mine: Int, punishment: Boolean = false) {
        if (punishment) if (!group.botPermission.isOperator()) {
            sendMessage("TB在本群没有管理员权限，无法创建带有禁言惩罚的游戏")
            return
        } else sendMessage("警告！本局游戏开启失败禁言惩罚")

        if (width >= 40 || high >= 60 || mine >= width * high) {
            sendMessage("创建自定义游戏失败,请检查：\n1、棋盘宽不能大于40;\n2、高不能大于60;\n3、雷数不应超出地块总数;")
        } else {
            val minesweeperGame = Minesweeper(0, width, high, mine, punishment)
            PluginMain.GAME[group.id] = minesweeperGame
            subject.sendImage(minesweeperGame.getImage())
        }
    }

    @SubCommand("踩")
    suspend fun MemberCommandSenderOnMessage.dig(x: Int, y: Int) {
        val minesweeperGame = PluginMain.GAME[group.id]
        if (minesweeperGame != null) {
            if (minesweeperGame.validation(x, y)) {
                sendMessage("坐标无效,操作失败")
                return
            }
            minesweeperGame.operationsAdd()
            val gameOver = minesweeperGame.uncover(x, y)
            group.sendImage(minesweeperGame.getImage())
            if (gameOver) {
                if (minesweeperGame.punishment) runCatching {
                    user.mute(minesweeperGame.getOperationsCounter() * 30 - minesweeperGame.getRemainingPlots())
                }.onFailure {
                    group.sendMessage("嘤嘤嘤，TB在本群权限不足")
                }
                PluginMain.GAME.remove(group.id)
                sendMessage("本局游戏结束")
            }
        } else {
            sendMessage("本群无进行中游戏,请先使用开始命令创建新游戏")
        }
    }

    @SubCommand("旗")
    suspend fun MemberCommandSenderOnMessage.flag(x: Int, y: Int) {
        val minesweeperGame = PluginMain.GAME[group.id]
        if (minesweeperGame != null) {
            if (minesweeperGame.validation(x, y)) {
                sendMessage("坐标无效,操作失败")
                return
            } else {
                minesweeperGame.operationsAdd()
                minesweeperGame.flag(x, y)
                group.sendImage(minesweeperGame.getImage())
            }
        } else {
            sendMessage("本群无进行中游戏,请先使用开始命令创建新游戏")
        }
    }
}

@ConsoleExperimentalApi
@MiraiExperimentalApi
class Minesweeper(type: Int, w: Int = 0, h: Int = 0, mine: Int = 0, val punishment: Boolean = false) {
    private val width: Int
    private val high: Int
    private val gameMap: Array<Array<Boolean>>
    private val userMap: Array<Array<Int>>
    private val level = arrayOf("自定义", "简单", "常规", "困难")

    private var remainingMines: Int
    private var remainingPlots: Int
    private var operationsCounter = 0
    private var image: BufferedImage

    init {
        record(Birthday.primaryName)
        when (type) {
            1 -> {
                width = 10
                high = 10
                remainingMines = 15
            }// Easy 15%
            2 -> {
                width = 20
                high = 20
                remainingMines = 80
            } // General 20%
            3 -> {
                width = 30
                high = 30
                remainingMines = 225
            } // Difficulty 25%
            else -> {
                width = w
                high = h
                remainingMines = mine
            } // Customize
        }
        remainingPlots = width * high
        gameMap = generate(width, high, remainingMines)  // Customize
        userMap = Array(high) { Array(width) { -2 } }

        image = BufferedImage(width * 43 + 37, high * 43 + 157, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.color = Color.decode("#f9f9f9")
        graphics.fillRect(0, 0, width * 43 + 37, high * 43 + 157) // 绘制背景色
        graphics.color = Color.decode("#333333")
        graphics.fillRect(0, 120, width * 43 + 37, high * 43 + 37) // 绘制棋盘外框底色
        graphics.color = Color.decode("#DBDBDB")
        graphics.fillRect(0, 120, width * 43, high * 43) // 绘制棋盘内框边线
        graphics.color = Color.decode("#6A9FBF")
        graphics.fillRect(0, 120, width * 43 - 3, high * 43 - 3) // 绘制棋盘底色

        for (i in 0 until high) {
            for (j in 0 until width) {
                graphics.color = Color.decode("#DBDBDB")
                graphics.fillRoundRect(j * 43 + 2, i * 43 + 160, 36, 3, 1, 1)// 横向分割
                graphics.fillRoundRect(j * 43 + 40, i * 43 + 123, 3, 36, 1, 1)// 纵向分割
                if (i == high - 1) {
                    graphics.color = Color.decode("#B4F474")
                    graphics.font = Font("Zpix", Font.PLAIN, 25)
                    graphics.drawString("${j + 1}", j * 43 + 8 / j.toString().length, high * 43 + 140)  // 底部
                }
            }
            graphics.color = Color.decode("#B4F474")
            graphics.font = Font("Zpix", Font.PLAIN, 25)
            graphics.drawString("${i + 1}", width * 43 + 8 / i.toString().length, i * 43 + 150) // 侧边
        }

        printString(1, "游戏进行中,难度:${level[type]}")
        graphics.dispose()
    }

    fun getRemainingPlots(): Int = remainingPlots
    fun getOperationsCounter(): Int = operationsCounter

    private fun printMineNum(x: Int, y: Int, num: Int) {
        val graphics = image.createGraphics()
        graphics.color = Color.decode("#F9F9F9")
        graphics.fillRect((y - 1) * 43, (x - 1) * 43 + 120, 40, 40)
        if (num >= 0) {
            val colorCode = arrayOf(
                "#EDA200",
                "#0000FF",
                "#008000",
                "#FF0000",
                "#000080",
                "#800000",
                "#008080",
                "#000000",
                "#808080",
                "#FF0000"
            )
            val numString = arrayOf("！", "1", "2", "3", "4", "5", "6", "7", "8", "※")
            graphics.color = Color.decode(colorCode[num])
            graphics.font = Font("大签字笔体", Font.PLAIN, 30)
            graphics.drawString(numString[num], y * 43 - 35, x * 43 + 107)
        }
        graphics.dispose()
    }

    private fun printString(line: Int, str: String) {
        val graphics = image.createGraphics()
        graphics.color = Color.decode("#F9F9F9")

        graphics.fillRect(0, (line - 1) * 40, width * 42 + 43, 40)
        graphics.color = Color.decode("#465563")
        graphics.font = Font("Aa花瓣", Font.PLAIN, 35)

        graphics.drawString(str, 1, line * 40 - 3)
        graphics.dispose()
    }

    private fun generate(w: Int, h: Int, mine: Int): Array<Array<Boolean>> {
        val game = Array(h * w) { it < mine }
        game.shuffle()
        return Array(h) {
            game.sliceArray((it * w) until (it + 1) * w)
        }
    }

    private fun censor(x: Int, y: Int) {
        var count = 0
        val minX = if (x > 1) x - 1 else 1
        val minY = if (y > 1) y - 1 else 1
        val maxX = if (x < high) x + 1 else high
        val maxY = if (y < width) y + 1 else width

        for (i in minX..maxX) {
            for (j in minY..maxY) {
                if (gameMap[i - 1][j - 1]) {
                    count++
                }
            }
        }
        if (count == 0) {
            mark(x, y, -1)
            for (i in minX..maxX) {
                for (j in minY..maxY) {
                    if (userMap[i - 1][j - 1] == -2) {
                        censor(i, j)
                    }
                }
            }
        } else {
            mark(x, y, count)
        }
    }

    private fun downFlag(x: Int, y: Int) {
        val graphics = image.createGraphics()
        graphics.color = Color.decode("#6A9FBF")
        graphics.fillRect((y - 1) * 43, (x - 1) * 43 + 120, 40, 40)
        graphics.dispose()
    }

    private fun mark(x: Int, y: Int, mark: Int) {
        userMap[x - 1][y - 1] = mark
        printMineNum(x, y, mark)
        remainingPlots--
    }

    private fun printLook(): Boolean {
        PluginMain.logger.debug { "File:Minesweeper.kt\tLine:266\n$remainingMines,$remainingPlots" }
        if (remainingPlots == remainingMines) {
            printString(1, "Victory")
            return true
        }
        printString(2, "操作步数：$operationsCounter")
        printString(3, "剩余雷数：$remainingMines")
        return false
    }

    fun validation(x: Int, y: Int): Boolean =
        x > high || y > width || userMap[x - 1][y - 1] != -2 && userMap[x - 1][y - 1] != 0

    fun uncover(x: Int, y: Int): Boolean {
        if (gameMap[x - 1][y - 1]) {
            printString(1, "Game Over")
            var w = 0
            for (i in gameMap) {
                w++
                var h = 0
                for (j in i) {
                    h++
                    if (j) printMineNum(w, h, 9)
                }
            }
            printLook()
            return true
        }
        censor(x, y)
        return printLook()
    }

    fun flag(x: Int, y: Int) {
        when (userMap[x - 1][y - 1]) {
            -2 -> {
                printMineNum(x, y, 0)
                userMap[x - 1][y - 1] = 0
                remainingMines--
                remainingPlots--
                printLook()

            }
            0 -> {
                downFlag(x, y)
                userMap[x - 1][y - 1] = -2
                remainingMines++
                remainingPlots++
                printLook()
            }
        }
    }

    fun operationsAdd() {
        operationsCounter++
    }

    fun getImage(): ByteArrayInputStream {
        val os = ByteArrayOutputStream()
        ImageIO.write(image, "png", os)
        return ByteArrayInputStream(os.toByteArray())
    }
}