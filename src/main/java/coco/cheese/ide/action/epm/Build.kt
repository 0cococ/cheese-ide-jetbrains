package coco.cheese.ide.action.epm



import coco.cheese.ide.Env.OS.separator
import coco.cheese.ide.console.ConsoleExecutor.Companion.printToConsole
import coco.cheese.ide.console.ConsoleExecutor.Companion.setProject
import coco.cheese.ide.data.APKToolYml
import coco.cheese.ide.data.SettingConfig
import coco.cheese.ide.utils.*
import coco.cheese.ide.utils.FileUtils.convertPath
import coco.cheese.ide.utils.FileUtils.copyDirectory
import coco.cheese.ide.utils.FileUtils.copyFile
import coco.cheese.ide.utils.FileUtils.zipDirectory
import coco.cheese.ide.utils.TerminalUtils.executeCommand
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import org.tomlj.Toml
import org.tomlj.TomlParseError
import org.tomlj.TomlParseResult
import org.w3c.dom.Element
import java.io.File
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.function.Consumer
import javax.swing.Icon
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class Build(name: String, description: String, icon: Icon) : AnAction(name, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        if(StorageUtils.getString(SettingConfig.CHEESE_HOME).isNullOrEmpty()||StorageUtils.getString(SettingConfig.ANDROID_SDK).isNullOrEmpty()){
            ToastUtils.error("CHEESE_HOME 环境路径未设置 或 ANDROID_SDK Android SDK路径未设置 无法构建")
            return
        }



        setProject(e.project)



        class MyThread( private val num: Int,private val tim: Long) : Thread() {
            private var progress = 0
            private val updateInterval = tim // 1 second
            override fun run() {
                val startTime = System.currentTimeMillis()
                while (!currentThread().isInterrupted) {
                    // 更新进度
                    progress = (progress + 1) % 101 // Progress wraps around from 0 to 100

                    // 仅在进度小于或等于 95 时输出
                    if (progress <= num) {
                        val message = "构建处理中... $progress%"
                        printToConsole(e.project, message, ConsoleViewContentType.NORMAL_OUTPUT)
                    }

                    // 计算休眠时间以确保每秒钟更新一次
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val sleepTime = maxOf(0L, updateInterval - elapsedTime % updateInterval)

                    try {
                        sleep(sleepTime)
                    } catch (e: InterruptedException) {
                        // 处理中断异常并终止线程
                        Thread.currentThread().interrupt()
                    }
                }
            }
        }
        Thread {
            val startTime = System.currentTimeMillis()
            printToConsole(e.project,"初始构建环境", ConsoleViewContentType.USER_INPUT)
            val outputDir = File("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}apkout")
            val buildDir=  File("${e.project!!.basePath}${separator}build")
            var thread: MyThread? =null



            if(outputDir.exists()){
                if(StorageUtils.getString(SettingConfig.BUILD)=="开启"){
                    printToConsole(e.project,"解包", ConsoleViewContentType.USER_INPUT)
                    thread = MyThread(95,5000L)
                    thread.start()
                    outputDir.deleteRecursively()
                    APKToolUtils.decodeApk(
                        File("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}project${separator}cheese.apk"),
                        outputDir
                    )
                }else{
                    thread = MyThread(95,2500L)
                    thread.start()
                }
            }else{
                printToConsole(e.project,"解包", ConsoleViewContentType.USER_INPUT)
                 thread = MyThread(95,2000L)
                 thread.start()
                outputDir.deleteRecursively()
                APKToolUtils.decodeApk(
                    File("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}project${separator}cheese.apk"),
                    outputDir
                )
            }


//            ApplicationManager.getApplication().invokeLater {
//                NotificationUtil.info("解包完成")
//            }

            if(buildDir.exists()){
                buildDir.deleteRecursively()
            }

            val baseDir: @SystemIndependent @NonNls String? = e.project!!.basePath
//        val virtualFile: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
            baseDir?.let {
                val mainPath="$it${separator}src${separator}main"
                val tomlPath="$it${separator}cheese.toml"
                val buildPath="$it${separator}build"
                val node_modules ="$it${separator}node_modules"
//            val jsonString = File(tomlPath).readText()
//            val person = Json.decodeFromString<Config>(jsonString)

                val result: TomlParseResult = Toml.parse(Paths.get(tomlPath))
                result.errors().forEach(Consumer { error: TomlParseError ->
                    System.err.println(
                        error.toString()
                    )
                })


                val ts = result.getBoolean("ts")
                if (ts!!){
                    val command = "tsc" // 替换为你想要执行的命令
                    executeCommand(command, File(it)) { output ->
                        println(output)
                    }
                    copyDirectory("$buildPath${separator}js", "$buildPath${separator}release${separator}main${separator}js")
                }else{
                    copyDirectory("$mainPath${separator}js", "$buildPath${separator}release${separator}main${separator}js")
                }

                val ui = result.getString("ui")

                if (ui=="xml"){
                    copyDirectory("$mainPath${separator}ui", "$buildPath${separator}release${separator}main${separator}ui")
                }else{
                    executeCommand("npm run build",File(convertPath("$mainPath${separator}ui"))) { output ->
                        printToConsole(e.project,output, ConsoleViewContentType.USER_INPUT)
                    }
                    copyDirectory(convertPath("$mainPath${separator}ui/dist"), "$buildPath${separator}release${separator}main${separator}ui")
                }
                copyDirectory("$mainPath${separator}language", "$buildPath${separator}release${separator}main${separator}language")
                copyDirectory(mainPath + "${separator}assets",buildPath + "${separator}release${separator}main${separator}assets")
                copyDirectory(node_modules,buildPath + "${separator}release${separator}node_modules")
                copyFile(tomlPath,buildPath + "${separator}release${separator}cheese.toml")
                zipDirectory(buildPath + "${separator}release",buildPath + "${separator}release.zip")

            }

            val path = "${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}apkout${separator}apktool.yml"
            val source: Path = Paths.get("${e.project!!.basePath}${separator}cheese.toml")
            val result: TomlParseResult = Toml.parse(source)
            result.errors().forEach(Consumer { error: TomlParseError ->
                System.err.println(
                    error.toString()
                )
            })
            copyFile("$baseDir${separator}build" + "${separator}release.zip","${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}apkout${separator}assets${separator}release.zip")
            val appTable = result.getTable("app")
            val buildTable = result.getTable("build")
            val build_toolsTable= buildTable!!.getTable("build-tools")
            val apktoolYml = YamlUtils.loadYml(path) ?: return@Thread // 处理文件加载失败的情况
            rmLib(result,apktoolYml)
           apktoolYml.packageInfo!!.renameManifestPackage = appTable?.getString("package") ?:"coco.def"
            modifyStringInXmlFile( "${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}apkout${separator}res${separator}values${separator}strings.xml",
                "app_name",
                appTable?.getString("name") ?:"coco.def"
            )
            apktoolYml.resourcesAreCompressed=true
            YamlUtils.write(apktoolYml, path)

            APKToolUtils.builderApk(
                outputDir,
              File("${e.project!!.basePath}${separator}build${separator}apk${separator}app.apk"))

            val endTime = System.currentTimeMillis()
            val command = "zipalign -v -p 4 ${"${e.project!!.basePath}${separator}build${separator}apk${separator}app.apk"} ${"${e.project!!.basePath}${separator}build${separator}apk${separator}app-sign-align.apk"}" // 替换为你想要执行的命令
            printToConsole(e.project,"SO对齐", ConsoleViewContentType.USER_INPUT)
            executeCommand(command, File("${StorageUtils.getString(SettingConfig.ANDROID_SDK)}${separator}build-tools${separator}${build_toolsTable!!.getString("version")}")) { output ->

                printToConsole(e.project,output, ConsoleViewContentType.USER_INPUT)
            }
            printToConsole(e.project,"APK签名", ConsoleViewContentType.USER_INPUT)
            FileUtils.createDirectoryIfNotExists("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}jks")
            extractResourceToLocal("jks/cheese.jks",File("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}jks${separator}cheese.jks"))

            apkSing("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}jks${separator}cheese.jks","${e.project!!.basePath}${separator}build${separator}apk${separator}app-sign-align.apk")

            val elapsedTimeInSeconds = (endTime - startTime) / 1000.0
            thread.interrupt()
            printToConsole(e.project, "构建处理中... 100%", ConsoleViewContentType.NORMAL_OUTPUT)
            printToConsole(e.project,"组装完毕 耗时：${elapsedTimeInSeconds}秒", ConsoleViewContentType.USER_INPUT)

        }.start()

    }
}


fun rmLib(result: TomlParseResult, yml: APKToolYml){
    val appTable = result.getTable("app")
    val tomlArray = appTable!!.getArray("ndk")

    val targetList = listOf("x86_64", "x86", "arm64-v8a", "armeabi-v7a")
    val tomlValues = mutableSetOf<String>()
    for (i in 0 until tomlArray!!.size()) {
        val item = tomlArray[i].toString() // 确保转换为字符串
        tomlValues.add(item)

    }
    val missingValues = targetList.filter { it !in tomlValues }
    if(missingValues.isNotEmpty()){
        missingValues.forEach { missingValue ->
            when (missingValue) {
                "x86_64" ->{
                   File("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}apkout${separator}lib${separator}${missingValue}").deleteRecursively()
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libyolov8ncnn.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libpython3.8.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libopencv_java4.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libmlkit_google_ocr_pipeline.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libjavet-node-android.v.3.1.0.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libc++_shared.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libBugly_Native.so")
                }
                "x86" -> {
                    File("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}apkout${separator}lib${separator}${missingValue}").deleteRecursively()
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libyolov8ncnn.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libpython3.8.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libopencv_java4.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libmlkit_google_ocr_pipeline.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libjavet-node-android.v.3.1.0.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libc++_shared.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libBugly_Native.so")
                }
                "arm64-v8a" -> {
                    File("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}apkout${separator}lib${separator}${missingValue}").deleteRecursively()
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libyolov8ncnn.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libpython3.8.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libopencv_java4.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libmlkit_google_ocr_pipeline.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libjavet-node-android.v.3.1.0.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libc++_shared.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libBugly_Native.so")
                }
                "armeabi-v7a"->{
                    File("${StorageUtils.getString(SettingConfig.CHEESE_HOME)}${separator}work${separator}apkout${separator}lib${separator}${missingValue}").deleteRecursively()
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libyolov8ncnn.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libpython3.8.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libopencv_java4.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libmlkit_google_ocr_pipeline.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libjavet-node-android.v.3.1.0.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libc++_shared.so")
                    yml.doNotCompress.remove("lib${separator}${missingValue}${separator}libBugly_Native.so")
                }
                else -> println("no...")
            }
        }
    }

}


fun modifyStringInXmlFile(
    filePath: String,
    nameToModify: String,
    newValue: String
) {
    val xmlFile = File(filePath)
    if (!xmlFile.exists()) {
        println("文件不存在: ${xmlFile.absolutePath}")
        return
    }

    // 创建 DocumentBuilderFactory 和 DocumentBuilder
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()

    // 解析 XML 文件
    val document = builder.parse(xmlFile)

    // 获取根元素
    val root = document.documentElement

    // 修改指定 name 的 value
    val nodes = root.getElementsByTagName("string")
    var modified = false
    for (i in 0 until nodes.length) {
        val node = nodes.item(i)
        if (node is Element && node.getAttribute("name") == nameToModify) {
            node.textContent = newValue
            modified = true
            break
        }
    }

    if (!modified) {
        println("没有找到 name 为 '$nameToModify' 的元素")
        return
    }

    // 将修改后的 XML 写回文件
    val transformer = TransformerFactory.newInstance().newTransformer()
    val source = DOMSource(document)
    val result = StreamResult(xmlFile)
    transformer.transform(source, result)

}

fun apkSing(jks:String,apk:String){
    val clazz = com.android.apksigner.ApkSignerTool::class.java
    val signMethod: Method = clazz.getDeclaredMethod("sign", Array<String>::class.java)
    signMethod.isAccessible = true
    val commandLineArgs = arrayOf(
        "--ks", jks,
        "--ks-key-alias", "key0",
        "--ks-pass", "pass:14410165",
        "--key-pass", "pass:14410165ss",
        apk
    )

    try {
        signMethod.invoke(null, commandLineArgs)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getRes(path:String):String{
    val classLoader =  Build::class.java.classLoader
    val resource = classLoader.getResource(path)
   return if (resource != null) {
    resource.path
    } else {
        ""
    }

}
fun extractResourceToLocal(path: String, destination: File): String {
    val classLoader = Build::class.java.classLoader
    val resource = classLoader.getResourceAsStream(path)
    return if (resource != null) {
        // 确保目标文件所在的目录存在
        destination.parentFile?.mkdirs()

        // 使用 Files.copy 从 InputStream 复制到目标文件
        Files.copy(resource, destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        resource.close()
        println("cg")
        // 返回目标文件的绝对路径
        destination.absolutePath
    } else {
        println("sb")
        // 如果资源未找到，返回一个提示信息
        "Resource not found: $path"
    }
}


fun main(){
    val classLoader = Build::class.java.classLoader
    val resource = classLoader.getResourceAsStream("jks/cheese.jks")
    println(resource)
//
//    APKToolUtils.decodeApk(
//        File("C:\\Users\\35600\\Desktop\\CheeseHome\\project\\cheese.apk"),
//        File("C:\\Users\\35600\\Desktop\\CheeseHome\\work\\apkout"),
//    )

}
