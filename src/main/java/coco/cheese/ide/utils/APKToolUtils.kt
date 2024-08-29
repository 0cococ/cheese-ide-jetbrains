package coco.cheese.ide.utils

import brut.androlib.ApkBuilder
import brut.androlib.ApkDecoder
import brut.androlib.Config
import brut.directory.ExtFile
import java.io.File

object APKToolUtils {
    fun decodeApk(apkFile: File, outputDir: File) {
        val config = Config.getDefaultConfig() // 获取默认配置
        val extFile = ExtFile(apkFile) // 将 APK 文件包装成 ExtFile
        val apkDecoder = ApkDecoder(config, extFile) // 创建 ApkDecoder 实例
        apkDecoder.decode(outputDir)
    }

    fun builderApk(apkResFile: File, outputDir: File) {
        val config = Config.getDefaultConfig()
        val extFile = ExtFile(apkResFile)
        val builder = ApkBuilder(config,extFile)
        builder.build(outputDir)
    }
}