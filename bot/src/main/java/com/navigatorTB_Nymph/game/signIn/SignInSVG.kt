package com.navigatorTB_Nymph.game.signIn

import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.svg.BuildSVGTool
import org.apache.batik.anim.dom.SVGDOMImplementation

class SignInSVG : BuildSVGTool(PluginMain.resolveDataPath("signIn.svg").toString()) {
    fun runBeta(
        oneWord: Pair<String, String>,
        tarot: Map<String, String>,
        countdown: Int,
        sprint: String? = null
    ): SignInSVG {
        val themeColor = when (tarot["Brand"]) {
            "The Hanged Man(倒吊人)" -> "#69B960"
            "The Emperor(皇帝)", "Justice(正义)", "The Chariot(战车)", "The Magician(魔术师)" -> "#FF9E3E"
            "The Hierophant(教皇)", "Death(死神)", "The Sun(太阳)" -> "#C4BCB9"
            "Strength(力量)", "The Empress(女王)" -> "#E7C653"
            "The Devil(恶魔)", "The Tower(塔)" -> "#553246"
            "The Star(星星)" -> "#4C557E"
            "The High Priestess(女祭司)", "Judgement(审判)" -> "#4C92C6"
            "The Lovers(恋人)", "Wheel of Fortune(命运之轮)", "The World(世界)" -> "#4EA4C5"
            "Temperance(节制)" -> "#C2B69E"
            "The Hermit(隐者)" -> "#2B5C56"
            "The Fool(愚者)" -> "#114E61"
            "The Moon(月亮)" -> "#8A677D"
            else -> "#FFF"
        }

        doc.getElementById("main").setAttribute("fill", themeColor)
        doc.getElementById("brand").textContent = tarot["Brand"]
        doc.getElementById("tarot").setAttributeNS("http://www.w3.org/1999/xlink", "href", tarot["ImgPath"])
        for ((i, str) in tarot["word"]!!.split("、").withIndex()) {
            val text = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "text")
            text.textContent = str
            text.setAttribute("x", "20")
            text.setAttribute("y", "${95 + i * 25}")
            doc.getElementById("word").appendChild(text)
        }
        doc.getElementById("hitokoto").textContent = oneWord.first
        doc.getElementById("from").textContent = oneWord.second
        doc.getElementById("countdown").textContent = countdown.toString()

        sprint?.let { doc.getElementById("add").textContent = it }

        return this
    }
}