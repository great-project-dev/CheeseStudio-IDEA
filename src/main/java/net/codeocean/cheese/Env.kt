package net.codeocean.cheese

import net.codeocean.cheese.infrastructure.DataSetting
import net.codeocean.cheese.ui.ConfigSettingUi

import java.io.File

object Env {
    const val RUNNING_KEY: String = "runningKey"
    var dS: DataSetting? = null
    var cd:Boolean = false //连接状态

    // 定义路径常量
    val DEFAULT_HOME_PATH = File(System.getProperty("user.home"), "CheeseSDK")
    val form = ConfigSettingUi()
    val VERSION="1.1.12"
    fun getDataSetting(): DataSetting? {
        if (dS == null) {
            dS = DataSetting()
        }
        return dS
    }


}