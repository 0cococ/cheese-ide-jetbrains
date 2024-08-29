# ui类型
ui = "${ui}"
# ts支持
ts = ${ts?c}
# 依赖仓库
hub = "https://"
# ui入口文件
main = "main"

[app]# app作用域
# app版本号
version = "0.0.1"
# app包名
package = "${pkg}"
# app名
name = "${projectname}"
# 架构支持
ndk = ["x86_64", "x86", "arm64-v8a", "armeabi-v7a"]
# 权限清单
permissions = [
"android.permission.INTERNET",
"android.permission.ACCESS_NETWORK_STATE"
]

[build]# 构建配置
# android_sdk_build-tools版本
build-tools = { version = "34.0.0" }

[dependencies]# 依赖作用域
# python支持
python = { version = "0.0.1"}



