package com.navigatorTB_Nymph.data

import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.svg.BuildSVGTool
import org.apache.batik.anim.dom.SVGDOMImplementation
import java.text.SimpleDateFormat
import java.util.*

class Dynamic(info: DynamicInfo) : BuildSVGTool(PluginMain.resolveDataPath("SVG_Template/dynamic.svg").toString()) {

    private val time = SimpleDateFormat("yyyy-MM-dd a hh:mm E", Locale.CHINA).format(info.timestamp)
    private val name = info.name
    private val face = info.face
    private val text = info.text
    private val pictureList = info.pictureList

    //计算图片区域高度
    private var imageY = text.split("\n", "。", "！").onEach { addTextSpan(it) }.size * 22 + 50
    private val height = imageY + pictureList.sumOf { it?.imgHeight ?: 0 }
    private val width = pictureList.maxOf { it?.imgWidth ?: 700 }


    fun layoutDynamic() {
        doc.getElementById("root").setAttribute("width", "$width")
        doc.getElementById("root").setAttribute("height", "$height")
        doc.getElementById("time").textContent = time
        if (name.isNullOrEmpty().not()) doc.getElementById("name").textContent = name
        if (face.isNullOrEmpty().not()) doc.getElementById("face")
            .setAttributeNS("http://www.w3.org/1999/xlink", "href", face)

        pictureList.forEach { if (it != null) addImage(it) }
    }

    private fun addTextSpan(content: String) {
        val tSpan = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "tspan")
        tSpan.setAttribute("x", "68")
        tSpan.setAttribute("dy", "22")
        if (content.isNotEmpty() && content[0] == '#') tSpan.setAttribute("fill", "#178bcf")
        tSpan.textContent = content
        doc.getElementById("content").appendChild(tSpan)
    }

    private fun addImage(picture: Picture) {
        val image = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "image")
        image.setAttribute("x", "${(width - picture.imgWidth) / 2}")
        image.setAttribute("y", "$imageY")
        image.setAttribute("width", "${picture.imgWidth}")
        image.setAttribute("height", "${picture.imgHeight}")
        image.setAttributeNS("http://www.w3.org/1999/xlink", "href", picture.imgSrc)
        doc.getElementById("imageList").appendChild(image)

        imageY += picture.imgHeight
    }
}