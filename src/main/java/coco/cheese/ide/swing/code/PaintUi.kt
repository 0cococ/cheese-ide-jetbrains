package coco.cheese.ide.swing.code

import coco.cheese.ide.InteractionServer.instruction
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min



object PaintUi {
    var imagePanel: PaintUi.ImagePanel? = null
    fun createTransparentImageIcon(width: Int, height: Int): ImageIcon {
        // 创建一个具有透明背景的 BufferedImage
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        // 使用 BufferedImage 创建 ImageIcon
        return ImageIcon(bufferedImage)
    }

    fun getFrame(): JFrame {
        // 创建主窗口
        val frame = JFrame("Cheese 图色工具").apply {
            defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            setSize(700, 500) // 调整窗口大小以适应需要

            // 检查图像文件是否存在
            val imagePath = createTransparentImageIcon(700, 500)
            imagePanel = ImagePanel(imagePath)

            // 创建按钮面板
            val buttonPanel = createButtonPanel(imagePanel as ImagePanel)

            // 创建一个 JSplitPane 用于分隔图片面板和按钮面板
            val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, JScrollPane(imagePanel), buttonPanel).apply {
                resizeWeight = 0.8 // 初始图片面板占 80% 的宽度
//            isOneTouchExpandable = true // 显示展开/折叠按钮
                dividerSize = 5 // 设置分隔条的大小
            }

            // 将 JSplitPane 添加到主窗口的内容面板
            contentPane.add(splitPane)

            // 不设置 isVisible = true
        }
        return frame
    }

    private fun createButtonPanel(imagePanel: ImagePanel): JPanel {
        // 创建按钮面板
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS) // 使用垂直方向的BoxLayout
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10) // 添加边距
            alignmentX = Component.CENTER_ALIGNMENT // 居中对齐
        }

        // 设置中文字体
        var font = Font("宋体", Font.PLAIN, 14) // 使用宋体字体，可以根据需要修改为其他合适的中文字体

        // 保存按钮
        val anotherButton4 = JButton("保存图片").apply {
            preferredSize = Dimension(150, 40) // 设置按钮的大小
            font = font // 设置字体
            addActionListener { saveImage(imagePanel) }
        }

        // 其他按钮
        val anotherButton3 = JButton("还原").apply {
            preferredSize = Dimension(150, 40)
            font = font // 设置字体
            addActionListener { imagePanel.restoreImage() }
        }

        val anotherButton2 = JButton("载入").apply {
            preferredSize = Dimension(150, 40)
            font = font // 设置字体
            addActionListener { /* 在此处添加按钮2的功能 */ }
        }

        val anotherButton1 = JButton("截图").apply {
            preferredSize = Dimension(150, 40)
            font = font // 设置字体
            addActionListener {
                instruction = 4

            }
        }

        // 将按钮添加到面板
        panel.add(Box.createVerticalStrut(10)) // 添加垂直间距
        panel.add(anotherButton1)
        panel.add(Box.createVerticalStrut(10))
        panel.add(anotherButton2)
        panel.add(Box.createVerticalStrut(10))
        panel.add(anotherButton3)
        panel.add(Box.createVerticalStrut(10))
        panel.add(anotherButton4)

        return panel
    }
    private fun saveImage(imagePanel: ImagePanel) {
        val chooser = JFileChooser().apply {
            dialogTitle = "保存图片"
            fileFilter = FileNameExtensionFilter("PNG 图片", "png")
            selectedFile = File("image.png")
        }

        // 显示保存对话框
        val userSelection = chooser.showSaveDialog(null)
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            var fileToSave = chooser.selectedFile

            // 检查文件是否有扩展名，如果没有，则加上 .png
            if (!fileToSave.name.lowercase().endsWith(".png")) {
                fileToSave = File(fileToSave.parentFile, "${fileToSave.name}.png")
            }

            // 如果文件存在，提示用户确认是否覆盖
            if (fileToSave.exists()) {
                val response = JOptionPane.showConfirmDialog(
                    null,
                    "文件已存在，是否覆盖？",
                    "确认",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                )
                if (response != JOptionPane.YES_OPTION) {
                    return
                }
            }

            // 尝试保存图像
            try {
                ImageIO.write(imagePanel.image, "png", fileToSave)
                JOptionPane.showMessageDialog(null, "图片保存成功: ${fileToSave.absolutePath}")
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(null, "图片保存失败: ${e.message}", "错误", JOptionPane.ERROR_MESSAGE)
            }
        }
    }


    class ImagePanel(imageIcon: ImageIcon) : JPanel() {
        var image: BufferedImage = createBufferedImage(imageIcon)
        private var originalImage: BufferedImage = image // 保存原始图像
        private var scale = 1.0
        private var mousePoint: Point? = null
        private var selectionRect: Rectangle? = null
        private var selecting = false
        private var x = 0
        private var y = 0
        private var dragged = false // 长按阈值，单位：毫秒

        // 常量定义
        private val ZOOM_SIZE = 9
        private val MAGNIFICATION = 20
        private val MAGNIFIER_OFFSET_X = 30
        private val MAGNIFIER_OFFSET_Y = 60
        private val GRID_COLOR = Color.BLACK
        private val HIGHLIGHT_COLOR = Color.RED
        private val INFO_BOX_COLOR = Color.WHITE
        private val INFO_TEXT_COLOR = Color.BLACK

        init {
            val mouseAdapter = object : MouseAdapter() {
                private var startX = 0
                private var startY = 0

                override fun mousePressed(e: MouseEvent) {
                    startX = e.x
                    startY = e.y
                }

                override fun mouseDragged(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        if (!selecting) {
                            selecting = true
                            originalImage = image
                            selectionRect = Rectangle(startX, startY, 0, 0)
                        }
                        updateSelectionRect(e)
                        mousePoint = e.point
                        repaint()

                        dragged = true
                    } else if (SwingUtilities.isLeftMouseButton(e)) {

                        handleDragging(e)
                        dragged = true
                    }
                }

                override fun mouseReleased(e: MouseEvent) {
                    if (!dragged) {
                        if (SwingUtilities.isRightMouseButton(e)) {

                            showPopupMenu(e.point)
                        } else if (SwingUtilities.isLeftMouseButton(e)) {

                        }
                    }

                    if (selecting) {
//                    finalizeSelection()
//                    selecting = false
//                    selectionRect = null
                    } else {
                        printImageCoordinates(e)
                    }
                    dragged = false
                }

                override fun mouseMoved(e: MouseEvent) {
                    mousePoint = e.point
                    repaint()
                }

                private fun updateSelectionRect(e: MouseEvent) {
                    val x1 = min(startX, e.x)
                    val y1 = min(startY, e.y)
                    val width = abs(startX - e.x)
                    val height = abs(startY - e.y)
                    selectionRect?.setBounds(x1, y1, width, height)
                }

                private fun handleDragging(e: MouseEvent) {
                    val viewport = parent as? JViewport ?: return
                    val viewPosition = viewport.viewPosition
                    viewPosition.translate(startX - e.x, startY - e.y)
                    scrollRectToVisible(Rectangle(viewPosition, viewport.size))
                    startX = e.x
                    startY = e.y
                }





                private fun printImageCoordinates(e: MouseEvent) {
                    val originalX = ((e.x - x) / scale).toInt()
                    val originalY = ((e.y - y) / scale).toInt()
                println("Original Image Coordinates: ($originalX, $originalY)")
                }
            }

            addMouseListener(mouseAdapter)
            addMouseMotionListener(mouseAdapter)
            addMouseWheelListener {
                scale *= if (it.preciseWheelRotation < 0) 1.1 else 0.9
                revalidate()
                repaint()
            }
        }
        private fun finalizeSelection() {
            val scaledRect = getScaledRectangle(selectionRect ?: return)

            // 获取原始图像的宽度和高度
            val imageWidth = image.width
            val imageHeight = image.height

            // 确保子图像区域在原始图像范围内
            var x = scaledRect.x
            var y = scaledRect.y
            var width = scaledRect.width
            var height = scaledRect.height

            if (x < 0) x = 0
            if (y < 0) y = 0
            if (x + width > imageWidth) width = imageWidth - x
            if (y + height > imageHeight) height = imageHeight - y

            if (width > 0 && height > 0) {
                val selectedImage = image.getSubimage(x, y, width, height)
                image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().apply {
                        drawImage(selectedImage, 0, 0, null)
                        dispose()
                    }
                }
                revalidate()
                repaint()
            }
        }

        private fun getScaledRectangle(rect: Rectangle): Rectangle {
            val startX = ((rect.x - x) / scale).toInt()
            val startY = ((rect.y - y) / scale).toInt()
            val endX = ((rect.x + rect.width - x) / scale).toInt()
            val endY = ((rect.y + rect.height - y) / scale).toInt()
            return Rectangle(startX, startY, endX - startX, endY - startY)
        }
        fun restoreImage() {
            image = originalImage
            revalidate()
            repaint()
        }

        fun loadImage(imagePath: String) {
            if (File(imagePath).exists()) {
                image = createBufferedImage(ImageIcon(imagePath))
                revalidate()
                repaint()
            }

        }


        private fun createBufferedImage(imageIcon: ImageIcon): BufferedImage {
            return BufferedImage(imageIcon.iconWidth, imageIcon.iconHeight, BufferedImage.TYPE_INT_ARGB).apply {
                createGraphics().apply {
                    imageIcon.paintIcon(null, this, 0, 0)
                    dispose()
                }
            }
        }

        private fun showPopupMenu(point: Point) {
            val popupMenu = JPopupMenu().apply {
                // 设置菜单项的字体和大小
                val font = Font("宋体", Font.PLAIN, 14)

                // 创建菜单项并添加到弹出菜单中
                add(createMenuItem("复制坐标", font) { copyCoordinates() })
                add(createMenuItem("复制范围", font) {

                    val ra=copyRange()
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection("${ra?.left}, ${ra?.top}, ${ra?.right}, ${ra?.bottom}"), null)
                    revalidate()
                    repaint()
                    selecting = false
                    selectionRect = null

                })
                add(createMenuItem("复制RGB", font) { copyRGB() })
                add(createMenuItem("剪切图片", font) { cutImage() })
                addSeparator() // 添加分隔线
                add(createMenuItem("取消", font) {
                    isVisible = false
                    selecting = false

                    selectionRect = null
                })
            }
            popupMenu.show(this, point.x, point.y)
        }

        private fun createMenuItem(text: String, font: Font, action: () -> Unit): JMenuItem {
            return JMenuItem(text).apply {
                this.font = font
                addActionListener { action() }
            }
        }
        private fun cutImage(){
            finalizeSelection()
            selecting = false
            selectionRect = null
        }
        private fun copyRange(): Range? {
            val scaledRect = getScaledRectangle(selectionRect ?: return null)

            // 获取原始图像的宽度和高度
            val imageWidth = image.width
            val imageHeight = image.height

            // 确保子图像区域在原始图像范围内
            var x = scaledRect.x
            var y = scaledRect.y
            var width = scaledRect.width
            var height = scaledRect.height

            if (x < 0) x = 0
            if (y < 0) y = 0
            if (x + width > imageWidth) width = imageWidth - x
            if (y + height > imageHeight) height = imageHeight - y
            val top = y
            val left = x
            val bottom = y + height
            val right = x + width

            return Range(left = left, top = top, bottom = bottom, right = right)
        }
        data class Range(val left: Int, val top: Int, val bottom: Int, val right: Int)
        private fun copyCoordinates() {
            mousePoint?.let {
                val text = formatCoordinates(it)
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
            }
        }

        private fun copyRGB() {
            mousePoint?.let {
                val text = getRGBText(it)
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
            }
        }

        private fun getRGBText(point: Point): String {
            val x = ((point.x - this.x) / scale).toInt()
            val y = ((point.y - this.y) / scale).toInt()
            return if (isInBounds(x, y)) {
                val rgb = image.getRGB(x, y)
                val color = Color(rgb)
                "RGB: (${color.red}, ${color.green}, ${color.blue})"
            } else {
                "RGB: Out of bounds"
            }
        }

        private fun formatCoordinates(point: Point): String {
            val x = ((point.x - this.x) / scale).toInt()
            val y = ((point.y - this.y) / scale).toInt()
            return "$x, $y"
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2d = g as Graphics2D
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            val at = AffineTransform().apply {
                translate(this@ImagePanel.x.toDouble(), this@ImagePanel.y.toDouble())
                scale(scale, scale)
            }
            g2d.drawImage(image, at, this)

            mousePoint?.let {
                drawMagnifier(g2d)
            }

            selectionRect?.let {
                g2d.color = Color.RED
                g2d.draw(it)

            }
        }

        private fun drawMagnifier(g2d: Graphics2D) {
            // 计算鼠标在图像中的坐标
            val mouseXInImage = ((mousePoint!!.x - x) / scale).toInt()
            val mouseYInImage = ((mousePoint!!.y - y) / scale).toInt()

            // 放大镜的宽度和高度
            val zoomedWidth = ZOOM_SIZE * MAGNIFICATION
            val zoomedHeight = ZOOM_SIZE * MAGNIFICATION

            // 创建放大镜图像
            val zoomedImage = createZoomedImage(mouseXInImage, mouseYInImage)

            // 计算放大镜的默认位置
            var xOffset = mousePoint!!.x + MAGNIFIER_OFFSET_X
            var yOffset = mousePoint!!.y + MAGNIFIER_OFFSET_Y

            // 获取图像的实际显示区域
            val imageDisplayRect = getVisibleImageAreaRect()

            // 确保鼠标位置在图像显示区域内
            if (!printMouseCoordinatesInVisibleRange()) {
                // 鼠标位置不在图像显示区域内，不显示放大镜

                return
            }
            val scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane::class.java, this) as? JScrollPane
            val scrollOffsetX = scrollPane?.horizontalScrollBar?.value ?: 0
            val scrollOffsetY = scrollPane?.verticalScrollBar?.value ?: 0
            // 将鼠标的屏幕坐标转换为相对于当前可见区域的坐标
            val mouseXInVisibleImage = mousePoint!!.x - scrollOffsetX
            val mouseYInVisibleImage = mousePoint!!.y - scrollOffsetY


            // 确保放大镜不会超出图像显示区域的边界

            // 如果放大镜超出图像显示区域底部，将其移动到鼠标上方
            val (horizontalOffsets, verticalOffsets) = calculateOffsets(imageDisplayRect.width, imageDisplayRect.height, 50)
            val (left, right) = horizontalOffsets
            val (top, bottom) = verticalOffsets


            if (mouseYInVisibleImage > bottom) {
                yOffset = mousePoint!!.y - zoomedHeight - MAGNIFIER_OFFSET_Y
            }

            if (mouseYInVisibleImage < top) {
                yOffset = mousePoint!!.y + MAGNIFIER_OFFSET_Y
            }

//        // 如果放大镜超出图像显示区域右边界，将其移动到鼠标左侧
            if (mouseXInVisibleImage > right) {
                xOffset = mousePoint!!.x - zoomedWidth - MAGNIFIER_OFFSET_X
            }


            // 如果放大镜超出图像显示区域左边界，将其移动到鼠标右侧
            if (mouseXInVisibleImage < left) {
                xOffset = mousePoint!!.x + MAGNIFIER_OFFSET_X
            }


            // 绘制放大镜
            g2d.drawImage(zoomedImage, xOffset, yOffset, this)
            g2d.color = HIGHLIGHT_COLOR
            g2d.drawRect(xOffset, yOffset, zoomedWidth, zoomedHeight)

            drawGrid(g2d, xOffset, yOffset)
            drawHighlight(g2d, xOffset, yOffset)
            drawInfoBox(g2d, xOffset, yOffset + zoomedHeight + 10)
        }

        fun calculateOffsets(width: Int, height: Int, offset: Int): Pair<Pair<Int, Int>, Pair<Int, Int>> {
            val leftOffset = offset
            val rightOffset = width - offset
            val topOffset = offset
            val bottomOffset = height - offset

            val horizontalOffsets = Pair(leftOffset, rightOffset)
            val verticalOffsets = Pair(topOffset, bottomOffset)

            return Pair(horizontalOffsets, verticalOffsets)
        }


        private fun printMouseCoordinatesInVisibleRange(): Boolean {
            // 获取图像的实际显示区域
            val imageDisplayRect = getVisibleImageAreaRect()

            // 获取滚动条的偏移量
            val scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane::class.java, this) as? JScrollPane
            val scrollOffsetX = scrollPane?.horizontalScrollBar?.value ?: 0
            val scrollOffsetY = scrollPane?.verticalScrollBar?.value ?: 0
            // 将鼠标的屏幕坐标转换为相对于当前可见区域的坐标
            val mouseXInVisibleImage = mousePoint!!.x - scrollOffsetX
            val mouseYInVisibleImage = mousePoint!!.y - scrollOffsetY

            // 检查鼠标的可见图像坐标是否在图像的可见区域内
            val isInsideVisibleRange = imageDisplayRect.contains(mouseXInVisibleImage, mouseYInVisibleImage)
            return isInsideVisibleRange
        }


        private fun getVisibleImageAreaRect(): Rectangle {
            // 面板的宽度和高度
            val panelWidth = parent.width
            val panelHeight = parent.height

            // 缩放后的图像宽度和高度
            val scaledImageWidth = (image.width * scale).toInt()
            val scaledImageHeight = (image.height * scale).toInt()

            // 图像在面板上的位置偏移量
            val imageX = x.toInt()
            val imageY = y.toInt()

            // 计算图像在面板上的显示区域的左上角坐标
            val visibleX = max(0, imageX)
            val visibleY = max(0, imageY)

            // 计算图像在面板上的可见区域的宽度和高度
            val visibleWidth = min(panelWidth - visibleX, scaledImageWidth)
            val visibleHeight = min(panelHeight - visibleY, scaledImageHeight)

            return Rectangle(visibleX, visibleY, visibleWidth, visibleHeight)
        }



        private fun createZoomedImage(mouseX: Int, mouseY: Int): BufferedImage {
            val zoomedWidth = ZOOM_SIZE * MAGNIFICATION
            val zoomedHeight = ZOOM_SIZE * MAGNIFICATION
            return BufferedImage(zoomedWidth, zoomedHeight, BufferedImage.TYPE_INT_ARGB).apply {
                for (i in 0 until ZOOM_SIZE) {
                    for (j in 0 until ZOOM_SIZE) {
                        val srcX = mouseX - ZOOM_SIZE / 2 + i
                        val srcY = mouseY - ZOOM_SIZE / 2 + j
                        if (isInBounds(srcX, srcY)) {
                            val rgb = image.getRGB(srcX, srcY)
                            for (k in 0 until MAGNIFICATION) {
                                for (l in 0 until MAGNIFICATION) {
                                    setRGB(i * MAGNIFICATION + k, j * MAGNIFICATION + l, rgb)
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun isInBounds(x: Int, y: Int): Boolean {
            return x >= 0 && x < image.width && y >= 0 && y < image.height
        }

        private fun drawGrid(g2d: Graphics2D, x: Int, y: Int) {
            g2d.color = GRID_COLOR
            for (i in 0..ZOOM_SIZE) {
                val gridX = x + i * MAGNIFICATION
                g2d.drawLine(gridX, y, gridX, y + ZOOM_SIZE * MAGNIFICATION)
            }
            for (j in 0..ZOOM_SIZE) {
                val gridY = y + j * MAGNIFICATION
                g2d.drawLine(x, gridY, x + ZOOM_SIZE * MAGNIFICATION, gridY)
            }
        }

        private fun drawHighlight(g2d: Graphics2D, x: Int, y: Int) {
            val highlightX = (ZOOM_SIZE / 2) * MAGNIFICATION
            val highlightY = (ZOOM_SIZE / 2) * MAGNIFICATION
            g2d.color = HIGHLIGHT_COLOR
            g2d.drawRect(x + highlightX, y + highlightY, MAGNIFICATION, MAGNIFICATION)
        }

        private fun drawInfoBox(g2d: Graphics2D, x: Int, y: Int) {
            val infoWidth = 180
            val infoHeight = 50
            g2d.color = INFO_BOX_COLOR
            g2d.fillRect(x, y, infoWidth, infoHeight)
            g2d.color = INFO_TEXT_COLOR
            g2d.drawRect(x, y, infoWidth, infoHeight)

            mousePoint?.let {
                val coordinatesText = formatCoordinates(it)
                val rgbText = getRGBText(it)
                val fontMetrics = g2d.fontMetrics
                val lineHeight = fontMetrics.height

                // 绘制坐标信息
                g2d.drawString(coordinatesText, x + 10, y + 20)
                // 绘制 RGB 信息
                g2d.drawString(rgbText, x + 10, y + 20 + lineHeight)
            }
        }


        override fun getPreferredSize(): Dimension {
            return Dimension((image.width * scale).toInt(), (image.height * scale).toInt())
        }
    }
}