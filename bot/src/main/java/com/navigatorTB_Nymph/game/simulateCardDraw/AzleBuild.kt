package com.navigatorTB_Nymph.game.simulateCardDraw

import com.navigatorTB_Nymph.data.AssetDataAzurLaneConstructTime
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.svg.BuildSVGTool
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.util.XMLResourceDescriptor


class AzleBuild(count: Int) : BuildSVGTool() {

    init {
        val file = when (count) {
            1, 3, 5 -> PluginMain.resolveDataPath("SVG_Template/azleBuild_1.svg")
            2, 4 -> PluginMain.resolveDataPath("SVG_Template/azleBuild_2.svg")
            else -> PluginMain.resolveDataPath("SVG_Template/azleBuild_6.svg")
        }
        val parser = XMLResourceDescriptor.getXMLParserClassName()
        doc = SAXSVGDocumentFactory(parser).createDocument("$file")
    }

    fun drawCard(result: List<AssetDataAzurLaneConstructTime>): AzleBuild {
        for ((i, v) in result.withIndex()) {
            val img = if ("0" in v.type.toString())
                doc.getElementById("card_ur_$i")
            else
                doc.getElementById("card_$i")
            img.setAttributeNS(namespaceURI, "href", "azle/${v.originalName}.png")
        }
        return this
    }
}