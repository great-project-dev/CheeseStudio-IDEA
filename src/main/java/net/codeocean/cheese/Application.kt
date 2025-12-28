package net.codeocean.cheese



import net.codeocean.cheese.Env.DEFAULT_HOME_PATH
import net.codeocean.cheese.Env.VERSION
import net.codeocean.cheese.Env.form
import net.codeocean.cheese.console.ConsoleExecutor
import net.codeocean.cheese.console.ConsoleExecutor.Companion.printToConsole
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.manager.SDKManager


import net.codeocean.cheese.utils.StorageUtils
import net.codeocean.cheese.utils.ToastUtils
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity


fun init(project: Project, callback: (Boolean) -> Unit){
    val sdkManager=  SDKManager(project)
    form.home.text = StorageUtils.getString(SettingConfig.CHEESE_HOME)
    if (form.home.text.isNullOrEmpty()){
        form.home.text= DEFAULT_HOME_PATH.absolutePath
        StorageUtils.save(SettingConfig.CHEESE_HOME, form.home.text)
    }
    form.gitHubProxy.text = StorageUtils.getString(SettingConfig.PROXY)
    StorageUtils.save(SettingConfig.PROXY,  form.gitHubProxy.text)

    form.port.text = StorageUtils.getString(SettingConfig.CHEESE_PORT)
    if (form.port.text.isNullOrEmpty()){
        form.port.text= "8080"
        StorageUtils.save(SettingConfig.CHEESE_PORT,form.port.text)
    }
    if (StorageUtils.getString(SettingConfig.BUILD).isNullOrEmpty()) {
        form.build.selectedItem = "关闭"
        StorageUtils.save(SettingConfig.BUILD,form.build.selectedItem)
    } else {
        form.build.selectedItem = StorageUtils.getString(SettingConfig.BUILD)
    }

    sdkManager.update(false,StorageUtils.getString(SettingConfig.CHEESE_HOME),callback)



}
class Application : ProjectActivity {
    override suspend fun execute(project: Project) {


        val executor = ConsoleExecutor(project)
        ApplicationManager.getApplication().invokeLater {
            executor.withReturn { runExecutor(project) }
                .withStop({ setRunning(project, false) }) { getRunning(project) }
            executor.run()
        }

        val text="""
    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    请您先访问（https://cheese.codeocean.net/agreement.html）详细阅读cheese系列产品和服务-用户协议。
    只有在您完全同意并遵守本用户协议的情况下，您才拥有使用本产品及相关服务的资格。
    若无法访问，请直接联系我们(邮箱：3560000009@qq.com)以获取最新协议内容。
    开始使用本产品和相关服务即表示您已阅读并同意全部协议条款。
    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        """.trimIndent()
        printToConsole(project,text, ConsoleViewContentType.USER_INPUT)
    }




    fun runExecutor(project: Project?) {
        if (project == null) {
            return
        }
        val executor = ConsoleExecutor(project)
        // 设置restart和stop
        executor.withReturn { runExecutor(project) }.withStop(
            { setRunning(project, false) },
            { getRunning(project) })
        executor.run()
    }
    fun setRunning(project: Project?, value: Boolean) {
        PropertiesComponent.getInstance(project!!).setValue(Env.RUNNING_KEY, value)
    }

    fun getRunning(project: Project?): Boolean {
        return PropertiesComponent.getInstance(project!!).getBoolean(Env.RUNNING_KEY)
    }
}

