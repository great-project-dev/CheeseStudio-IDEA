package net.codeocean.cheese.module


import net.codeocean.cheese.Env.getDataSetting
import net.codeocean.cheese.console.ConsoleExecutor.Companion.printToConsole
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.factory.CModuleType
import net.codeocean.cheese.infrastructure.DataSetting
import net.codeocean.cheese.init

import net.codeocean.cheese.utils.StorageUtils
import net.codeocean.cheese.utils.TerminalUtils.execute
import net.codeocean.cheese.utils.ToastUtils
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.DumbAwareRunnable
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.DisposeAwareRunnable
import java.io.File
import kotlin.concurrent.thread


class CModuleBuilder() : ModuleBuilder() {


    override fun getModuleType(): ModuleType<out ModuleBuilder> {
        return CModuleType()
    }


    override fun getBuilderId(): String? {
        return javaClass.name
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
        val moduleNameLocationSettings = settingsStep.moduleNameLocationSettings
        val artifactId = getDataSetting()!!.getInstance().getProjectConfig().getProjectname()
        if (null != moduleNameLocationSettings && !StringUtil.isEmptyOrSpaces(artifactId)) {
            moduleNameLocationSettings.moduleName = artifactId!!
        }
        return super.modifySettingsStep(settingsStep)
    }

    override fun setupRootModel(model: ModifiableRootModel) {


        // 生成工程路径
        val path = FileUtil.toSystemIndependentName(contentEntryPath!!)
        File(path).mkdirs()
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
        model.addContentEntry(virtualFile!!)
        val project: Project = model.getProject()
        init(model.project) {
            if (it) {
                if (null != this.myJdk) {
                    model.sdk = this.myJdk
                } else {
                    model.inheritSdk()
                }

                val r: Runnable = DumbAwareRunnable {
                    WriteCommandAction.runWriteCommandAction(
                        project
                    ) {
                        try {
                            val CORE_JAR_PATH =
                                File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME), "lib"), "core.jar")
                            val JDK_PATH = File(File(StorageUtils.getString(SettingConfig.CHEESE_HOME), "jbr"), "bin")
                            val SDK_PATH = File(StorageUtils.getString(SettingConfig.CHEESE_HOME))

                            thread {
                                var command = ""

                                val isWindows = System.getProperty("os.name").contains("win", true)
                                if (isWindows) {
                                    command = "java -Dfile.encoding=GBK -jar $CORE_JAR_PATH -create -c {name:\"${
                                        getDataSetting()!!.getInstance().getProjectConfig().getProjectname()
                                    }\",pkg:\"${
                                        getDataSetting()!!.getInstance().getProjectConfig().getPkg()
                                    }\",platform:\"${platform}\",bindings:\"${
                                        getDataSetting()!!.getInstance().getProjectConfig().getLanguage()
                                    }\",ui:\"${
                                        getDataSetting()!!.getInstance().getProjectConfig().getUi()
                                    }\"} -sdkPath ${StorageUtils.getString(SettingConfig.CHEESE_HOME)} -o ${contentEntryPath}"
                                } else {
                                    command = """
    java -Dfile.encoding=GBK-8 -jar "$CORE_JAR_PATH" \
    -create \
    -c '{
        "name": "${getDataSetting()!!.getInstance().getProjectConfig().getProjectname()}",
        "pkg": "${getDataSetting()!!.getInstance().getProjectConfig().getPkg()}",
        "platform": "${platform}",
        "bindings": "${getDataSetting()!!.getInstance().getProjectConfig().getLanguage()}",
        "ui": "${getDataSetting()!!.getInstance().getProjectConfig().getUi()}"
    }' \
    -sdkPath "${StorageUtils.getString(SettingConfig.CHEESE_HOME)}" \
    -o "$contentEntryPath"
""".trimIndent()
                                }

                                execute(
                                    command = command,  // 执行的命令
                                    options = mapOf("workingDir" to JDK_PATH.absolutePath),
                                    onData = { output ->  // 处理输出
                                        printToConsole(project, output, ConsoleViewContentType.USER_INPUT)
                                    },
                                    onComplete = { exitCode ->
                                        if (exitCode == 0) {
                                            ToastUtils.info("项目创建成功")
                                        } else {
                                            ToastUtils.info("项目创建失败，退出码: ${exitCode}")
                                        }

                                    }
                                )
                            }


                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                        }
                    }
                }
                if (ApplicationManager.getApplication().isUnitTestMode
                    || ApplicationManager.getApplication().isHeadlessEnvironment
                ) {
                    r.run()
                    return@init
                }

                if (!project.isInitialized) {
                    StartupManager.getInstance(project)
                        .registerPostStartupActivity(DisposeAwareRunnable.create(r, project))
                    return@init
                }

                if (DumbService.isDumbAware(r)) {
                    r.run()
                } else {
                    DumbService.getInstance(project).runWhenSmart(DisposeAwareRunnable.create(r, project))
                }

            } else {
                ToastUtils.info("项目创建失败。")
            }
        }


    }

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> {
        return arrayOf(CModuleConfigStep())
    }

}