package com.navigatorTB_Nymph.data

import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

data class Dynamic(
    val name: String, val timestamp: Long,
    var text: String? = null,
    var imageStream: List<InputStream>? = null
) {
    fun message2jpg(): ByteArrayInputStream {
        //计算文本区域高度
        val textList = arrayListOf<String>()
        text?.split("\n")?.forEach { textList.addAll(it.chunked(24)) }
        val textHeight = textList.size * 48 + 60
        //计算图片区域高度
        val imageList = arrayListOf<BufferedImage>()
        imageStream?.forEach { imageList.add(ImageIO.read(it)) }

        val height = if (imageList.size > 0) {
            var imageHeight = 0
            if (imageList.size == 9) {
                imageHeight = (360.0 / imageList[0].width * imageList[0].height * 3).toInt()
            } else {
                imageList.forEach { img ->
                    imageHeight += (1080.0 / img.width * img.height).toInt()
                }
            }
            imageHeight + textHeight // 计算高度
        } else textHeight
        //生成画布
        val image = BufferedImage(1080, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        // 绘制背景色
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, 1080, height)
        // 绘制配图
        var cumulativeHeight = 0
        if (imageList.size == 9) {
            for ((index, img) in imageList.withIndex()) {
                graphics.drawImage(
                    img,
                    index % 3 * 360,
                    cumulativeHeight + textHeight,
                    360,
                    (360.0 / img.width * img.height).toInt(),
                    null
                )
                if (index % 3 == 2) cumulativeHeight += img.height / 3
            }
        } else {
            for (img in imageList) {
                val h = (1080.0 / img.width * img.height).toInt()
                graphics.drawImage(img, 0, cumulativeHeight + textHeight, 1080, h, null)
                cumulativeHeight += h
            }
        }
        // 绘制透明白色遮罩
        graphics.color = Color(240, 240, 255, 192)
        graphics.fillRoundRect(5, 5, 1070, textHeight - 10, 5, 5)
//      graphics.fillArc(5, 5, 1080, textHeight-30,5,5) //会出现一个似乎效果不错的花纹 等待用别的库重构所有图片生成程序
        // 开始绘制文字
        graphics.color = Color(0, 0, 0, 192)
        graphics.font = Font("大签字笔体", Font.PLAIN, 45)
        for ((line, str) in textList.withIndex()) {
            graphics.drawString(str, 15, 60 + line * 49)
        }
        graphics.dispose()
        // 输出图片
        val os = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", os)
        return ByteArrayInputStream(os.toByteArray())
    }
}