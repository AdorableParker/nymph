package com.navigatorTB_Nymph.game.ticTacToe

import com.navigatorTB_Nymph.command.composite.TicTacToeGame
import com.navigatorTB_Nymph.pluginData.UsageStatistics
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@OptIn(MiraiExperimentalApi::class, ConsoleExperimentalApi::class)
class TicTacToe(solve: Boolean) {
    private val locate = arrayOf(
        Grid(1, 1), Grid(1, 2), Grid(1, 3),
        Grid(2, 1), Grid(2, 2), Grid(2, 3),
        Grid(3, 1), Grid(3, 2), Grid(3, 3)
    )
    private val plan = solve
    private val gameMap = arrayOf(
        arrayOf(Piece.Blank, Piece.Blank, Piece.Blank),
        arrayOf(Piece.Blank, Piece.Blank, Piece.Blank),
        arrayOf(Piece.Blank, Piece.Blank, Piece.Blank)
    )
    private val blankList = mapOf(
        "corner" to mutableListOf(1, 3, 7, 9),
        "side" to mutableListOf(2, 4, 6, 8),
        "center" to mutableListOf(5),
        "all" to (1..9).toMutableList()
    )
    private val positions = listOf(
        Grid(50, 50), Grid(156, 50), Grid(261, 50),
        Grid(50, 156), Grid(156, 156), Grid(261, 156),
        Grid(50, 261), Grid(156, 261), Grid(261, 261)
    )

    private var hold = Piece.Circle
    private var blank = mutableListOf<Int>()
    private var step = 1
    private var lastStep = Grid(0, 0)
    private var image: BufferedImage = BufferedImage(310, 310, BufferedImage.TYPE_INT_RGB)

    init {
        UsageStatistics.record(TicTacToeGame.primaryName)
        val graphics = image.createGraphics()
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, 310, 310) // 绘制背景色
        graphics.color = Color.decode("#79D2D2")
        graphics.fillRoundRect(0, 101, 310, 5, 2, 2)    // 横向分割
        graphics.fillRoundRect(0, 206, 310, 5, 2, 2)   // 横向分割
        graphics.fillRoundRect(101, 0, 5, 310, 2, 2)    // 纵向分割
        graphics.fillRoundRect(206, 0, 5, 310, 2, 2)    // 纵向分割
        graphics.font = Font("大签字笔体", Font.PLAIN, 80)
        graphics.color = Color.decode("#B7FA85")
        var v = 1
        for (i in positions) {
            graphics.drawString(v.toString(), i.column - 25, i.row + 25)
            v++
        }
        graphics.dispose()
    }

    private fun remove(index: Int) {
        when (index) {
            1, 3, 7, 9 -> blankList["corner"]
            2, 4, 6, 8 -> blankList["side"]
            else -> blankList["center"]
        }?.remove(index)
        blankList["all"]?.remove(index)
    }

    fun down(index: Int): Int {
        return if (effective(index))
            5
        else {
            if (blank.indexOf(index) != -1) blank.remove(index)
            remove(index)
            refreshInterface(index)
            when (down(locate[index - 1])) {
                -1 -> settlement(index, 1)
                -2 -> settlement(index, 2)
                -3 -> settlement(index, 3)
                -4 -> settlement(index, 4)
                -5 -> return -1
                else -> return 0
            }
            -1
        }
    }

    private fun settlement(index: Int, type: Int) {
        val graphics = image.createGraphics()
        if (hold == Piece.Circle) {
            graphics.color = Color.decode("#02B340") // 绿色连线
        } else {
            graphics.color = Color.decode("#E60039") // 红色连线
        }
        graphics.stroke = BasicStroke(5.0f)
        val kv = when (type) {
            1 -> when (index) {
                1, 2, 3 -> Pair(positions[0], positions[2])
                4, 5, 6 -> Pair(positions[3], positions[5])
                else -> Pair(positions[6], positions[8])
            }
            2 -> when (index) {
                1, 4, 7 -> Pair(positions[0], positions[6])
                2, 5, 8 -> Pair(positions[1], positions[7])
                else -> Pair(positions[2], positions[8])
            }
            3 -> Pair(positions[0], positions[8])
            else -> Pair(positions[6], positions[2])
        }
        graphics.drawLine(kv.first.column, kv.first.row, kv.second.column, kv.second.row)
        graphics.dispose()
    }

    // fillRoundRect() 圆角矩形
    // fillRect()      矩形
    // fillOval()      椭圆
    // fillArc()       圆弧
    private fun refreshInterface(index: Int) {
        val graphics = image.createGraphics()
        val position = positions[index - 1]
        if (hold == Piece.Circle) {
            graphics.color = Color.decode("#02B340") // 绿色画圈
            graphics.fillOval(position.column - 40, position.row - 40, 80, 80)
            graphics.color = Color.decode("#FFFFFF")
            graphics.fillOval(position.column - 35, position.row - 35, 70, 70)
        } else {
            graphics.color = Color.decode("#E60039") // 红色画叉
            graphics.stroke = BasicStroke(10.0f)
            graphics.drawLine(position.column - 30, position.row - 30, position.column + 30, position.row + 30)
            graphics.drawLine(position.column - 30, position.row + 30, position.column + 30, position.row - 30)
        }
        graphics.dispose()
    }

    private fun down(grid: Grid): Int {
        gameMap[grid.column - 1][grid.row - 1] = hold
        step++
        val statusCode = warn(grid)
        hold = hold.rival()
        lastStep = grid
        return statusCode
    }

    private fun effective(index: Int) = when (index) {
        1, 3, 7, 9 -> blankList["corner"]
        2, 4, 6, 8 -> blankList["side"]
        else -> blankList["center"]
    }?.indexOf(index) == -1

    private fun warn(grid: Grid): Int {
        var sign = 0
        var index = 0
        // 检查行
        for (i in 0..2) {
            when (gameMap[grid.column - 1][i]) {
                hold.rival() -> {
                    sign = 0
                    break
                }
                hold -> sign++
                else -> {
                    blank.add(grid.column * 3 + i - 2)
                    index++
                }
            }
        }
        when (sign) {
            0, 1 -> {
                for (i in 1..index) {
                    blank.removeLast()
                }
            }
            3 -> return -1
        }
        sign = 0
        index = 0
        // 检查列
        for (i in 0..2) {
            when (gameMap[i][grid.row - 1]) {
                hold.rival() -> {
                    sign = 0
                    break
                }
                hold -> sign++
                else -> {
                    blank.add((i * 3 + grid.row))
                    index++
                }
            }
        }
        when (sign) {
            0, 1 -> {
                for (i in 1..index) {
                    blank.removeLast()
                }
            }
            3 -> return -2
        }
        sign = 0
        index = 0
        // 检查斜角
        if (grid.angle()) {
            for (i in 0..2) {
                when (gameMap[i][i]) {
                    hold.rival() -> {
                        sign = 0
                        break
                    }
                    hold -> sign++
                    else -> {
                        blank.add(i * 4 + 1)
                        index++
                    }
                }
            }
            when (sign) {
                0, 1 -> {
                    for (i in 1..index) {
                        blank.removeLast()
                    }
                }
                3 -> return -3
            }
            sign = 0
            index = 0
            for (i in arrayOf(Grid(0, 2), Grid(1, 1), Grid(2, 0))) {
                when (gameMap[i.column][i.row]) {
                    hold.rival() -> {
                        sign = 0
                        break
                    }
                    hold -> sign++
                    else -> {
                        blank.add(i.column * 3 + i.row + 1)
                        index++
                    }
                }
            }
            when (sign) {
                0, 1 -> {
                    for (i in 1..index) {
                        blank.removeLast()
                    }
                }
                3 -> return -4
            }
        }
        if (step > 9) return -5
        return sign
    }

    fun aiRun(): Int {
        return if (blank.isNotEmpty()) { // 如果自己或是对方可达成三连,优先三连
            // println("三连")
            down(blank[0])
        } else {
            if (plan) {              // 否则如果启用推算
                val locate = when (step) {
                    1 -> arrayOf(1, 3, 7, 9).random()           // 先手取角
                    2 -> when (lastStep.type()) {
                        0 -> arrayOf(1, 3, 7, 9).random()       // 对方取中 则取角
                        else -> 5                               // 对方边角 则取中
                    }
                    3 -> when (lastStep.type()) {
                        0 -> blankList["corner"]!!.sum() - 10   // 对方取中 则取第一步对角
                        else -> 5                                // 对方边角 则取中
                    }
                    4 -> when (lastStep.type()) {
                        1 -> blankList["corner"]!!.random()
                        else -> blankList["side"]!!.random()
                    }
                    5 -> when ((lastStep.column - 1) * 3 + lastStep.row) {
                        1 -> 7
                        3 -> 1
                        7 -> 9
                        9 -> 3
                        else -> blankList["corner"]!!.random()
                    }
                    // 对方走
                    else -> blankList["all"]!!.random()
                }
                down(locate)
            } else {
                down(blankList["all"]!!.random())
            }
        }
    }

    fun getImage(): ByteArrayInputStream {
        val os = ByteArrayOutputStream()
        ImageIO.write(image, "png", os)
        return ByteArrayInputStream(os.toByteArray())
    }
}