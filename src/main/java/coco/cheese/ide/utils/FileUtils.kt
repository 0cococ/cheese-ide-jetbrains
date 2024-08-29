package coco.cheese.ide.utils

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
object FileUtils {
    fun convertPath(path: String): String {
        val separator = File.separator
        // 替换所有的正斜杠和反斜杠为当前平台的路径分隔符
        return path.replace("/", separator).replace("\\", separator)
    }

    fun copyDirectory(sourceDir: String, targetDir: String) {
        val sourcePath = Paths.get(sourceDir)
        val targetPath = Paths.get(targetDir)
        if (!Files.exists(sourcePath)) {
           return
        }
        // 确保目标目录存在
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath)
        }

        // 遍历源目录下的所有文件和子目录
        Files.walk(sourcePath).forEach { source ->
            val target = targetPath.resolve(sourcePath.relativize(source))
            try {
                if (Files.isDirectory(source)) {
                    // 如果是目录，创建对应的目标目录
                    if (!Files.exists(target)) {
                        Files.createDirectories(target)
                    }
                } else {
                    // 如果是文件，复制到目标目录
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    fun createDirectoryIfNotExists(directoryPath: String) {
        val directory = File(directoryPath)
        if (!directory.exists()) {
            directory.mkdirs()
            println("Directory created: $directoryPath")
        } else {
            println("Directory already exists: $directoryPath")
        }
    }
    fun copyFile(sourcePath: String, destinationPath: String) {
        val sourceFile = File(sourcePath)
        val destinationFile = File(destinationPath)

        if (!sourceFile.exists()) {
            throw IOException("Source file does not exist: $sourcePath")
        }

        try {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            println("File copied successfully from $sourcePath to $destinationPath")
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }
    fun replaceCharInFile(filePath: String, oldChar: String, newChar: String) {
        val file = File(filePath)

        if (!file.exists()) {
            throw IllegalArgumentException("文件不存在: $filePath")
        }

        val tempFilePath = "$filePath.tmp"
        val tempFile = File(tempFilePath)

        file.bufferedReader().use { reader ->
            tempFile.bufferedWriter().use { writer ->
                reader.forEachLine { line ->
                    val modifiedLine = line.replace(oldChar, newChar)
                    writer.write(modifiedLine)
                    writer.newLine()
                }
            }
        }

        // 替换原文件
        Files.delete(Paths.get(filePath))
        Files.move(Paths.get(tempFilePath), Paths.get(filePath))
    }

    fun zipDirectory(sourceDirPath: String, zipFilePath: String) {
        val sourceDir = File(sourceDirPath)
        if (!sourceDir.isDirectory) {
            throw IllegalArgumentException("Source path must be a directory")
        }

        ZipOutputStream(FileOutputStream(zipFilePath)).use { zipOut ->
            sourceDir.walkTopDown().forEach { file ->
                val relativePath = sourceDir.toURI().relativize(file.toURI()).path
                if (file.isFile) {
                    val zipEntry = ZipEntry(relativePath)
                    zipOut.putNextEntry(zipEntry)
                    file.inputStream().use { input ->
                        input.copyTo(zipOut)
                    }
                    zipOut.closeEntry()
                }
            }
        }
    }

}
