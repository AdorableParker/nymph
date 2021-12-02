package com.example.navigatorTB_Nymph

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
        text?.split("\n")?.forEach { textList.addAll(it.chunked(23)) }
        val textHeight = textList.size * 45
        //计算图片区域高度
        var imageWidth = 0
        var imageHeight = 0
        val imageList = arrayListOf<BufferedImage>()
        imageStream?.forEach { imageList.add(ImageIO.read(it)) }
        for ((index, img) in imageList.withIndex()) {
            imageWidth = img.width
            when (index % 3) {
                0 -> imageHeight += img.height
                1 -> imageHeight -= img.height / 2
                2 -> imageHeight -= img.height / 6
            }
        }
        //生成画布
        val height = (1080.0 / imageWidth * imageHeight + textHeight).toInt()// 计算高度
        val image = BufferedImage(1080, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        // 绘制背景色
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, 1080, height)
        // 绘制配图
        for ((index, img) in imageList.withIndex()) {
            val w = if (imageList.size / 3 * 3 >= index + 1) 360 else 1080 / (imageList.size % 3)
            val h = (w * 1.0 / img.width * img.height).toInt()
            graphics.drawImage(img, index % 3 * w, (index / 3.0 * h + textHeight).toInt(), w, h, null)
        }
        // 绘制透明白色遮罩
        graphics.color = Color(240, 240, 255, 192)
        graphics.fillRoundRect(5, 5, 1060, textHeight - 30, 5, 5)
//      graphics.fillArc(5, 5, 1080, textHeight-30,5,5) //会出现一个似乎效果不错的花纹 等待用别的库重构所有图片生成程序
        // 开始绘制文字
        graphics.color = Color(0, 0, 0, 192)
        graphics.font = Font("大签字笔体", Font.PLAIN, 45)
        for ((line, str) in textList.withIndex()) {
            graphics.drawString(str, 30, 60 + line * 45)
        }
        graphics.dispose()
        // 输出图片
        val os = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", os)
        return ByteArrayInputStream(os.toByteArray())
    }
}