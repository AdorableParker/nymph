package com.navigatorTB_Nymph.game.minesweeper

import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.svg.BuildSVGTool
import org.apache.batik.anim.dom.SVGDOMImplementation

class Minesweeper() : BuildSVGTool(PluginMain.resolveDataPath("minesweeper.svg").toString()) {

    private lateinit var mapAttribute: MinesweeperMapProperties         // 地图参数
    private val userMap by lazy { Array(mapAttribute.size) { true } }
    private val minesMap by lazy { mineLayer(mapAttribute) }
    private var flagCount = 0
    private var remainingMines = 0                            // 显示雷数

    constructor(level: LevelSet) : this() {
        mapAttribute = when (level) {
            LevelSet.Easy -> MinesweeperMapProperties(10, 10, 15)        // 15%
            LevelSet.General -> MinesweeperMapProperties(20, 20, 80)     // 20%
            LevelSet.Difficulty -> MinesweeperMapProperties(30, 30, 225) // 25%
        }
        remainingMines = mapAttribute.mines
        mapInit()
    }

    constructor(width: Int, height: Int, mines: Int) : this() {
        mapAttribute = MinesweeperMapProperties(width, height, mines)
        remainingMines = mapAttribute.mines
        mapInit()
    }


    /* 初始化棋盘 */
    private fun mapInit() {
        val root = doc.getElementById("root")
        root.setAttribute("width", "${60 + mapAttribute.width * 55}")
        root.setAttribute("height", "${140 + mapAttribute.height * 55}")
        val base = doc.getElementById("main")
        val cont = doc.getElementById("content")
        doc.getElementById("minesInfo").textContent = remainingMines.toString()
        doc.getElementById("flagInfo").textContent = flagCount.toString()
        for (x in 0..mapAttribute.width) {
            for (y in 0..mapAttribute.height) {
                val rect = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "rect")
                val text = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "text")

                rect.setAttribute("x", "${5 + 55 * x}")
                rect.setAttribute("y", "${5 + 55 * y}")
                rect.setAttribute("width", "50")
                rect.setAttribute("height", "50")
                rect.setAttribute("id", "r_${x}_$y")

                text.setAttribute("x", "${30 + 55 * x}")
                text.setAttribute("y", "${43 + 55 * y}")
                text.setAttribute("font-size", "40")
                text.setAttribute("id", "t_${x}_$y")

                if (x == 0 || y == 0) when (x + y) {
                    0 -> text.textContent = ""
                    x -> text.textContent = x.toString()
                    y -> text.textContent = y.toString()
                } else when (y % 2) {
                    0 -> rect.setAttribute("fill", "#85CCF0")
                    1 -> rect.setAttribute("fill", "#2B72CE")
                }
                base.appendChild(rect)
                cont.appendChild(text)
            }
        }
    }

    /* 布雷 */
    private fun mineLayer(mapProperties: MinesweeperMapProperties): Array<Boolean> {
        val minesMap = Array(mapProperties.size) { it < mapProperties.mines }
        minesMap.shuffle()
        return minesMap
    }

    /* 更新棋盘 */
    private fun update(index: Int, code: Int) {
        userMap[index] = false
        val (x, y) = mapAttribute.decoding(index)
        val colorCode = arrayOf(
            "#B4F474", // 安全
            "#0000FF", "#008000", "#FF0000", "#000080", // 1..4
            "#800000", "#008080", "#000000", "#808080", // 5..8
            "#FF0000", // 雷
            "#EDA200"  // 旗帜
        )
        when (code) {
            0 -> { // 安全
                val cont = doc.getElementById("r_${x}_$y")
                cont.setAttribute("fill", colorCode[code])
            }
            in 1..8 -> { // 附近有雷
                val text = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "text")
                text.textContent = code.toString()
                text.setAttribute("x", "${30 + 55 * x}")
                text.setAttribute("y", "${43 + 55 * y}")
                text.setAttribute("font-size", "40")
                text.setAttribute("fill", colorCode[code])
                text.setAttribute("font-family", "大签字笔体")
                doc.getElementById("content").appendChild(text)
                doc.getElementById("r_${x}_$y").setAttribute("fill", "#E9F5E6")
            }
            9 -> { //雷
                val u = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "use")
                u.setAttributeNS("http://www.w3.org/1999/xlink", "href", "#mines")
                u.setAttribute("x", "${5 + 55 * x}")
                u.setAttribute("y", "${5 + 55 * y}")
                doc.getElementById("board").appendChild(u)
            }
            10 -> { // 插旗
                val u = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "use")
                u.setAttributeNS("http://www.w3.org/1999/xlink", "href", "#flag")
                u.setAttribute("x", "${5 + 55 * x}")
                u.setAttribute("y", "${5 + 55 * y}")
                u.setAttribute("id", "f_${x}_$y")
                doc.getElementById("board").appendChild(u)
                remainingMines--
                flagCount++
                doc.getElementById("minesInfo").textContent = (remainingMines).toString()
                doc.getElementById("flagInfo").textContent = (flagCount).toString()
                doc.getElementById("r_${x}_$y").setAttribute("fill", colorCode[0])
            }
            11 -> { // 拔旗
                doc.getElementById("f_${x}_$y").let { it.parentNode.removeChild(it) }
                remainingMines++
                flagCount--
                doc.getElementById("minesInfo").textContent = (remainingMines).toString()
                doc.getElementById("flagInfo").textContent = (flagCount).toString()
                doc.getElementById("r_${x}_$y").setAttribute("fill", "#9AD163")
            }
            else -> {
                doc.getElementById("r_${x}_$y").setAttribute("fill", colorCode[9])
            }
        }


    }

    /* 结束消息 */
    private fun gameOver(str: String) {
        val over = doc.getElementById("over")
        over.setAttribute("font-size", "${mapAttribute.width * 10}")
        over.setAttribute("dy", "${mapAttribute.width * 3}")
        over.textContent = str
        for (index in 0 until mapAttribute.size) {
            if (userMap[index] && minesMap[index]) {
                update(index, 9)
            }
        }
    }

    /* 拓展挖掘区 */
    private fun expandDig(index: Int): Int {
        val mineList = mapAttribute.mapKey(index)
        val mineNumber = mineList.count { minesMap[it] }
        if (mineNumber != 0) {
            return mineNumber
        }
        if (userMap[index]) {
            userMap[index] = false
            mineList.forEach { update(it, expandDig(it)) }
        }
        return 0
    }

    /* 挖掘 */
    fun dig(x: Int, y: Int): GameState {
        val index = mapAttribute.decoding(x, y)
        if (index !in 0 until mapAttribute.size || !userMap[index]) {
            return GameState.Effective
        }
        if (minesMap[index]) {
            gameOver("Game Over")
            update(index, -1)
            return GameState.GameOver
        }
        update(index, expandDig(index))
        if (mapAttribute.conclusion(userMap.count { !it }, flagCount)) {
            gameOver("Victory")
            return GameState.GameOver
        }
        return GameState.Invalid
    }

    /* 插旗 */
    fun put(x: Int, y: Int): GameState {
        if (mapAttribute.decoding(x, y) !in 0 until mapAttribute.size) return GameState.Effective
        flagCount++
        update(mapAttribute.decoding(x, y), 10)
        return GameState.Invalid
    }

    /* 拔旗 */
    fun unplug(x: Int, y: Int): GameState {
        if (mapAttribute.decoding(x, y) !in 0 until mapAttribute.size) return GameState.Effective
        flagCount--
        update(mapAttribute.decoding(x, y), 11)
        return GameState.Invalid
    }
}