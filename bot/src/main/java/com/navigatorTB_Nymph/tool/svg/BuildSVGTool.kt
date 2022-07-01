package com.navigatorTB_Nymph.tool.svg

import com.navigatorTB_Nymph.pluginConfig.MySetting
import com.navigatorTB_Nymph.pluginMain.PluginMain
import net.mamoe.mirai.utils.debug
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.util.XMLResourceDescriptor
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path


open class BuildSVGTool() {

    val namespaceURI = "http://www.w3.org/1999/xlink"
    lateinit var doc: Document

    constructor(file: Path) : this() {
        val parser = XMLResourceDescriptor.getXMLParserClassName()
        val f = if (MySetting.testMod) "file:/$file" else "$file"
        PluginMain.logger.debug { f }
        doc = SAXSVGDocumentFactory(parser).createDocument(f)
    }

    fun draw(): ByteArrayInputStream {
        val input = TranscoderInput(doc)
        val outputStream = ByteArrayOutputStream()
        val output = TranscoderOutput(outputStream)
        PNGTranscoder().transcode(input, output)
        outputStream.flush()
        outputStream.close()
        return ByteArrayInputStream(outputStream.toByteArray())
    }
}