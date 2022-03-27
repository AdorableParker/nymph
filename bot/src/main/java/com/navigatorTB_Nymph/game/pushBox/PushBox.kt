package com.navigatorTB_Nymph.game.pushBox

import com.navigatorTB_Nymph.game.pushBox.Direction.*
import com.navigatorTB_Nymph.pluginData.PushBoxLevelMap
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.svg.BuildSVGTool
import org.apache.batik.anim.dom.SVGDOMImplementation

class PushBox(private val level: Int) : BuildSVGTool(PluginMain.resolveDataPath("pushBox.svg").toString()) {

    private lateinit var reachableLocation: MutableSet<Int>

    private var manPosition = 0

    private val boxMap = mutableSetOf<Int>()
    private val goalMap = mutableSetOf<Int>()
    private val wallMap = mutableSetOf<Int>()

    private var width = 0
    private var height = 0

    init {
        PushBoxLevelMap.mapSize[level].let {
            width = it.first
            height = it.second
        }

        val root = doc.getElementById("root")
        root.setAttribute("width", "${45 + width * 40}")
        root.setAttribute("height", "${45 + height * 40}")
        generateMap()
        upDataReachableLocation()
    }

    private fun decoding(x: Int, y: Int): Int = (y - 1) * width + x - 1
    private fun decoding(index: Int): Pair<Int, Int> = Pair(index % width + 1, index / width + 1)

    private fun generateMap() {
        for ((index, it) in PushBoxLevelMap.gameMap[level].withIndex()) {
            val (x, y) = decoding(index)
            when (it) {
                '@' -> upDataMan(x, y)                           // 人
                '=' -> addRect(x, y, "none")       // 地图外
                '#' -> {                                        // 墙
                    wallMap.add(index)
                    addRect(x, y, "wall")
                }
                '.' -> {                                       // 目标点
                    goalMap.add(index)
                    addRect(x, y, "goal")
                }
                '+' -> {
                    goalMap.add(index)
                    addRect(x, y, "goal")
                    upDataMan(x, y)
                }
                '%' -> {                                        // 箱子
                    boxMap.add(index)
                    addBox(x, y, "box")
                }
                '*' -> {
                    boxMap.add(index)
                    goalMap.add(index)
                    addRect(x, y, "goal")
                    addBox(x, y, "box")
                    addBox(x, y, "okBox")
                }
            }
        }
    }

    private fun upDataMan(x: Int, y: Int) {
        manPosition = decoding(x, y)
        val m = doc.getElementById("man")
        m.setAttribute("cx", "${25 + 40 * x}")
        m.setAttribute("cy", "${25 + 40 * y}")
    }

    private fun upDataMan() {
        val (x, y) = decoding(manPosition)
        val m = doc.getElementById("man")
        m.setAttribute("cx", "${25 + 40 * x}")
        m.setAttribute("cy", "${25 + 40 * y}")
    }

    private fun addBox(x: Int, y: Int, parentNodeID: String) {
        val rect = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "rect")
        rect.setAttribute("id", "${parentNodeID}_${x}_$y")
        rect.setAttribute("width", "30")
        rect.setAttribute("height", "30")
        rect.setAttribute("x", "${10 + 40 * x}")
        rect.setAttribute("y", "${10 + 40 * y}")
        doc.getElementById(parentNodeID).appendChild(rect)
    }

    private fun addRect(x: Int, y: Int, parentNodeID: String) {
        val rect = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "rect")
        rect.setAttribute("width", "40")
        rect.setAttribute("height", "40")
        rect.setAttribute("x", "${5 + 40 * x}")
        rect.setAttribute("y", "${5 + 40 * y}")
        doc.getElementById(parentNodeID).appendChild(rect)
    }

    private fun upDataReachableLocation() {
        reachableLocation = mutableSetOf(manPosition)
        setOf(
            manPosition - width,
            manPosition - 1,
            manPosition + 1,
            manPosition + width
        ).filter { it !in wallMap && it !in boxMap }.forEach { upDataReachableLocation(it) }
    }

    private fun upDataReachableLocation(index: Int) {
        reachableLocation += index
        setOf(
            index - width,
            index - 1,
            index + 1,
            index + width
        ).filter { it !in reachableLocation && it !in wallMap && it !in boxMap }.forEach(::upDataReachableLocation)
    }

    private fun moveBox(boxID: String, boxFinalPosition: Int) {
        val box = doc.getElementById(boxID)
        val (x, y) = decoding(boxFinalPosition)
        box.setAttribute("id", "box_${x}_$y")
        box.setAttribute("x", "${10 + 40 * x}")
        box.setAttribute("y", "${10 + 40 * y}")
    }

    fun tryTo(x: Int, y: Int) = if (decoding(x, y) in reachableLocation) {
        upDataMan(x, y)
        true
    } else false

    fun tryPush(type: Direction, step: Int): Boolean? {
        val d = when (type) {
            Up -> Array(step + 1) { manPosition - (it + 1) * width }
            Down -> Array(step + 1) { manPosition + (it + 1) * width }
            Left -> Array(step + 1) { manPosition - (it + 1) }
            Right -> Array(step + 1) { manPosition + (it + 1) }
        }
        if (d[0] !in boxMap) return null

        val boxID = decoding(d[0]).let { (x, y) -> "box_${x}_$y" }
        var boxFinalPosition = d[0]
        for (index in d.drop(1)) {
            if (index in wallMap || index in boxMap) break
            manPosition = boxFinalPosition
            boxFinalPosition = index
        }
        if (boxFinalPosition == d[0]) return null

        boxMap.remove(d[0])
        boxMap.add(boxFinalPosition)

        upDataMan()
        upDataReachableLocation()
        moveBox(boxID, boxFinalPosition)
        return goalMap.subtract(boxMap).isEmpty()
    }

    fun restart() {
        wallMap.clear()
        goalMap.clear()
        boxMap.clear()
        generateMap()
        upDataReachableLocation()
    }

//    fun debug() {
//        reachableLocation.forEach {
//            val circle = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "circle")
//            val (x, y) = decoding(it)
//            circle.setAttribute("r", "5")
//            circle.setAttribute("cx", "${25 + 40 * x}")
//            circle.setAttribute("cy", "${25 + 40 * y}")
//            doc.getElementById("debug").appendChild(circle)
//        }
//    }
}