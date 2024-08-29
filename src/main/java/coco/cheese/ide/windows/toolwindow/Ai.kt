package coco.cheese.ide.windows.toolwindow

import coco.cheese.ide.swing.code.NodeUi
import coco.cheese.ide.swing.code.PaintUi
import coco.cheese.ide.utils.IconsUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefClient
import groovyjarjarantlr4.v4.runtime.misc.NotNull
import org.cef.browser.CefBrowser
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.cef.network.CefRequest
import java.awt.BorderLayout
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.swing.*

class Ai: ToolWindowFactory {
    override fun createToolWindowContent(@NotNull project: Project, @NotNull toolWindow: ToolWindow) {
        val frame = JFrame("Ai对话")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(800, 600)

        // 创建一个主面板，使用 BorderLayout
        val myPanel = JPanel(BorderLayout())
        frame.contentPane.add(myPanel)

        // 判断是否支持 JBCefBrowser
        if (JBCefApp.isSupported()) {
            // 使用 JBCefBrowser 加载网页
            val browser = JBCefBrowser()
            myPanel.add(browser.component, BorderLayout.CENTER)
            if (!p1()){
                browser.loadURL("http://49.232.248.72/")
            }else{
                browser.loadURL("http://49.232.248.72/404.html")
            }

            val infoLabel = JLabel("对话额度为20次/天/人，防止频繁恶意对话！")
            myPanel.add(infoLabel, BorderLayout.SOUTH)
            // 创建一个刷新按钮
            val refreshButton = JButton("刷新页面")
            myPanel.add(refreshButton, BorderLayout.NORTH)

            // 添加按钮的点击事件来刷新页面
            refreshButton.addActionListener {
                if (!p1()){
                    browser.loadURL("http://49.232.248.72/")
                }else{
                    browser.loadURL("http://49.232.248.72/404.html")
                }
            }


            val loadHandler = object : CefLoadHandler {
                override fun onLoadingStateChange(browser: CefBrowser, isLoading: Boolean, canGoBack: Boolean, canGoForward: Boolean) {
                    if (!isLoading) {
                        // 页面加载完成时注入 JavaScript
                        browser.executeJavaScript(
                            """
                 if (!window._fetchIntercepted) {
                    (function() {
                    
                           const originalXHROpen = XMLHttpRequest.prototype.open;
                           XMLHttpRequest.prototype.open = function(method, url) {
                               if (method.toUpperCase() === 'POST') {
                                   console.log('POST Request URL:', url);  // 打印请求的 URL
                                   window.cefQuery({
                                       request: 'url=' + encodeURIComponent(url),
                                       persistent: false
                                   });
                               }
                               return originalXHROpen.apply(this, arguments);
                           };
//                        const originalFetch = window.fetch;
//                        window.fetch = function(input, init) {
//                            const method = init && init.method ? init.method.toUpperCase() : 'GET';
//                            const url = (typeof input === 'string') ? input : input.url;
//                            if (method === 'POST') {
//                                console.log('POST Request URL:', url);  // 打印请求的 URL
//                                window.cefQuery({
//                                    request: 'url=' + encodeURIComponent(url),
//                                    persistent: false
//                                });
//                            }
//                            return originalFetch.apply(this, arguments);
//                        };
                        window._fetchIntercepted = true;
                    })();
                }
//   (function() {
//       // 拦截 XMLHttpRequest 的 open 方法
//       const originalXHROpen = XMLHttpRequest.prototype.open;
//       XMLHttpRequest.prototype.open = function(method, url) {
//           if (method.toUpperCase() === 'POST') {
//               console.log('POST Request URL:', url);  // 打印请求的 URL
//               window.cefQuery({
//                   request: 'url=' + encodeURIComponent(url),
//                   persistent: false
//               });
//           }
//           return originalXHROpen.apply(this, arguments);
//       };
//
//       // 拦截 fetch 请求
//       const originalFetch = window.fetch;
//       window.fetch = function(input, init) {
//           const method = init && init.method ? init.method.toUpperCase() : 'GET';
//           const url = (typeof input === 'string') ? input : input.url;
//           if (method === 'POST') {
//               console.log('POST Request URL:', url);  // 打印请求的 URL
//               window.cefQuery({
//                   request: 'url=' + encodeURIComponent(url),
//                   persistent: false
//               });
//           }
//           return originalFetch.apply(this, arguments);
//       };
//   })();
                """.trimIndent(),
                            browser.url, 0
                        )
                    }
                }

                override fun onLoadStart(browser: CefBrowser, frame: org.cef.browser.CefFrame, transitionType: CefRequest.TransitionType) {
                    // Handle load start if needed
                }

                override fun onLoadEnd(browser: CefBrowser, frame: org.cef.browser.CefFrame, httpStatusCode: Int) {
                    // Handle load end if needed
                }

                override fun onLoadError(browser: CefBrowser, frame: org.cef.browser.CefFrame, errorCode: CefLoadHandler.ErrorCode, errorText: String, failedUrl: String) {
                    // Handle load error if needed
                }
            }


            val client: JBCefClient = browser.jbCefClient
            client.addLoadHandler(loadHandler, browser.cefBrowser)

            val messageRouter = CefMessageRouter.create()
            messageRouter.addHandler(object : CefMessageRouterHandlerAdapter() {
                override fun onQuery(browser: CefBrowser, frame: org.cef.browser.CefFrame, queryId: Long, request: String, persistent: Boolean, callback: CefQueryCallback): Boolean {
                    if (request.startsWith("url=")) {
                        // 解析并获取URL
//                        val url = request.split("=")[1]
//                        println("POST Request URL: ${java.net.URLDecoder.decode(url, "UTF-8")}")
//                        // 在这里处理这个POST请求URL，比如调用相关业务逻辑
//                        callback.success("Success")
// 加密文件

                        if (pw()) {

                            browser.loadURL("http://49.232.248.72/404.html")
                        }

                        return true
                    }
                    return false
                }

                override fun onQueryCanceled(browser: CefBrowser, frame: org.cef.browser.CefFrame, queryId: Long) {
                    // Handle query cancellation if needed
                }
            }, true)

            client.cefClient.addMessageRouter(messageRouter)
        }

        // 获取 JFrame 的内容面板
        val contentPane = frame.contentPane as JComponent

        // 使用 ContentFactory 创建 Content 并添加到 ToolWindow
        val factory = ContentFactory.getInstance()
        val content1 = factory.createContent(contentPane, "Ai对话", false)
        toolWindow.contentManager.addContent(content1)
    }

    override val icon: Icon
        get() = IconsUtils.getImage("ai.svg")
}


fun p1(): Boolean {
    val key = generateKey()
    val userHome = System.getProperty("user.home")
    val fileName = "chai.txt"
    val filePath = File(userHome, fileName)

    val fileContent = decryptFileToString(filePath, key)
    if (fileContent.isNotEmpty()) {
        val parts = fileContent.split(" - ")
        if (parts.size == 2) {
            val fileDate = parts[0]
            val fileNumber = parts[1].toIntOrNull()
            if (fileDate == getTodayDate()) {
                if (fileNumber != null && fileNumber > 20) {
                    return true
                }
            }
        }

    }
    return false
}

fun pw(): Boolean {
    val key = generateKey()
    val userHome = System.getProperty("user.home")
    val fileName = "chai"
    val filePath = File(userHome, fileName)

    val fileContent = decryptFileToString(filePath, key)

    if (fileContent.isNotEmpty()) {
        println(fileContent)
        val parts = fileContent.split(" - ")
        if (parts.size == 2) {
            val fileDate = parts[0]
            val fileNumber = parts[1].toIntOrNull()
            var newNumber = fileNumber?.plus(1) ?: 1
            if (fileDate == getTodayDate()) {
                if (fileNumber != null && fileNumber >20) {
                    return true
                }
            }else{
                newNumber=1
            }
            encryptStringToFile("${getTodayDate()} - $newNumber", filePath, key)
        }
    }else{
        encryptStringToFile("${getTodayDate()} - 1", filePath, key)
    }
return false

}

private val FIXED_KEY = "1234567890123456" // 16字节 = 128位密钥

fun generateKey(): SecretKey {
    // 使用固定密钥字符串创建密钥对象
    val keyBytes = FIXED_KEY.toByteArray(StandardCharsets.UTF_8)
    return SecretKeySpec(keyBytes, "AES")
}
fun getTodayDate(): String {
    val today = LocalDate.now()
    return today.format(DateTimeFormatter.ISO_DATE)
}

fun decryptFileToString(inputFile: File, key: SecretKey): String {
    if (!inputFile.exists()) {
        // 文件不存在时返回空字符串
        return ""
    }

    val cipher = javax.crypto.Cipher.getInstance("AES")
    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key)

    // 读取加密字符串
    val encryptedString = inputFile.readText()
    val encryptedBytes = java.util.Base64.getDecoder().decode(encryptedString)

    // 解密数据
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return String(decryptedBytes, Charsets.UTF_8)
}

fun encryptStringToFile(inputString: String, outputFile: File, key: SecretKey) {
    val cipher = javax.crypto.Cipher.getInstance("AES")
    cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key)

    val encryptedBytes = cipher.doFinal(inputString.toByteArray(Charsets.UTF_8))
    val encryptedString = java.util.Base64.getEncoder().encodeToString(encryptedBytes)

    outputFile.writeText(encryptedString)
}