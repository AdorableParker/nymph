package com.navigatorTB_Nymph.game.simulateCardDraw

import com.navigatorTB_Nymph.data.AssetDataAzurLaneConstructTime
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.svg.BuildSVGTool


class AzleBuild(cunt: Int) :
    BuildSVGTool(PluginMain.resolveDataPath("SVG_Template/azleBuild.svg").toString()) {

    fun drawCard(result: List<AssetDataAzurLaneConstructTime>): AzleBuild {
        for ((i, v) in result.withIndex()) {
            doc.getElementById("card_$i").setAttributeNS(
                "http://www.w3.org/1999/xlink",
                "href",
                "azle/${v.originalName}.png"
            )
        }
        return this
    }
}