package com.navigatorTB_Nymph.miscellaneous

import com.navigatorTB_Nymph.data.DynamicInfo
import com.navigatorTB_Nymph.data.Picture
import com.navigatorTB_Nymph.pluginMain.PluginMain
import com.navigatorTB_Nymph.tool.svg.BuildSVGTool
import org.apache.batik.anim.dom.SVGDOMImplementation
import java.text.SimpleDateFormat
import java.util.*

class Dynamic(info: DynamicInfo) : BuildSVGTool(PluginMain.resolveDataPath("SVG_Template/dynamic.svg")) {
    private val time = SimpleDateFormat("yyyy-MM-dd a hh:mm E", Locale.CHINA).format(info.timestamp)
    private val name = info.name
    private val face = info.face
    private val text = info.text
    private val pictureList = info.pictureList

    //计算图片区域高度
    private var textHeight = wordWrap()
    private var imageY = textHeight + 60

    private fun wordWrap(): Int {
        var height = 0
        (Regex("#.*#").find(text)?.let {
            addTextSpan(it.value, true)
            height += 21
            text.removePrefix(it.value)
        } ?: text).split("\r\n", "\n").forEach {
            if (it.length in 0..49) {
                addTextSpan(it)
                height += 21
            } else {
                var start = 0
                do {
                    val end = it.subSequence(start, start + 49).groupingBy { char ->
                        char in ' '..'z'
                    }.eachCount().getOrDefault(true, 0).div(2.0).toInt() + start + 49

                    if (end <= it.length) {
                        addTextSpan(it.substring(start, end))
                        height += 21
                    } else break
                    start = end
                } while (start + 49 < it.length)
                addTextSpan(it.substring(start))
                height += 21
            }
        }
        return height
    }

    fun layoutDynamic() {
        doc.getElementById("time").textContent = time
        if (name.isNullOrEmpty().not()) doc.getElementById("name").textContent = name
        if (face.isNullOrEmpty().not()) doc.getElementById("face")
            .setAttributeNS(namespaceURI, "href", face)
        doc.getElementById("backdrop").setAttribute("height", "${textHeight + 50}")
        pictureList.forEach { if (it != null) addImage(it) }
        doc.getElementById("root").setAttribute("height", "${imageY + 5}")
    }

    private fun addTextSpan(content: String, topic: Boolean = false) {
        val tSpan = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "tspan")
        tSpan.setAttribute("dy", "21")
        if (topic) {
            tSpan.setAttribute("x", "68")
            tSpan.setAttribute("fill", "#178bcf")
        }
        tSpan.setAttribute("x", "${(content.length - content.trimStart().length) * 6 + 68}")
        tSpan.textContent = content.ifBlank { "　" }
        doc.getElementById("content").appendChild(tSpan)
    }

    private fun addImage(picture: Picture) {
        val image = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "image")
        image.setAttribute("y", "$imageY")

        imageY += if (picture.imgWidth > 830) {
            val imgH = picture.imgHeight * 830 / picture.imgWidth
            image.setAttribute("x", "0")
            image.setAttribute("width", "830")
            image.setAttribute("height", "$imgH")
            imgH
        } else {
            image.setAttribute("x", "${(830 - picture.imgWidth) / 2}")
            image.setAttribute("width", "${picture.imgWidth}")
            image.setAttribute("height", "${picture.imgHeight}")
            picture.imgHeight
        }
        image.setAttributeNS(namespaceURI, "href", picture.imgSrc)
        doc.getElementById("imageList").appendChild(image)
    }
}