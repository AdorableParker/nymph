package com.navigatorTB_Nymph.tool.svg

import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.util.XMLResourceDescriptor
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

open class BuildSVGTool(mod: String) {

    val doc = svgInit(mod)

    private fun svgInit(mod: String): Document {
        val parser = XMLResourceDescriptor.getXMLParserClassName()
        return SAXSVGDocumentFactory(parser).createDocument(mod)
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