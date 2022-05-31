package com.navigatorTB_Nymph.game.guessWord

import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.svg.BuildSVGTool

class GuessWord(private val ctAn: String) : BuildSVGTool(PluginMain.resolveDataPath("SVG_Template/wordle.svg")) {
    fun runBeta(line: Int, inStr: String): Boolean {
        var v = 0
        for (i in 0..4) {
            doc.getElementById("t_${line}${i + 1}").textContent = inStr[i].toString()
            when (ctAn.indexOf(inStr[i])) {
                -1 -> doc.getElementById("r_${line}${i + 1}").setAttribute("fill", "#7b7b7b")
                i -> {
                    v++
                    doc.getElementById("r_${line}${i + 1}").setAttribute("fill", "#7da66c")
                }
                else -> doc.getElementById("r_${line}${i + 1}").setAttribute("fill", "#c7b760")
            }
        }
        return v >= 5
    }
}