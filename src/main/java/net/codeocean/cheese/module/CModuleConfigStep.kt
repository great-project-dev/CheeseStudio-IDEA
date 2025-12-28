package net.codeocean.cheese.module


import net.codeocean.cheese.Env.getDataSetting
import net.codeocean.cheese.data.SettingConfig
import net.codeocean.cheese.domain.model.vo.ProjectConfigVO
import net.codeocean.cheese.ui.ProjectConfigUI
import net.codeocean.cheese.ui.ProjectConfigUIIOS
import net.codeocean.cheese.utils.StorageUtils

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import java.awt.Graphics
import javax.swing.JComponent
var platform:String=""
class CModuleConfigStep(): ModuleWizardStep() {
    private var projectConfigUI: ProjectConfigUI
    private var projectConfigUIIOS: ProjectConfigUIIOS

    init {
        // 在构造方法中初始化一些东西
        this.projectConfigUI = ProjectConfigUI()
        this.projectConfigUIIOS = ProjectConfigUIIOS()
    }



    override fun getComponent(): JComponent {

        if (platform=="ios"){
            return  projectConfigUIIOS.component;
        }
        return  projectConfigUI.component;
    }

    override fun updateDataModel() {
        // todo: 根据 UI 更新模型
    }


    override fun validate(): Boolean {
        // 获取配置信息，写入到 DataSetting
        val projectConfig: ProjectConfigVO = getDataSetting()!!.getInstance().getProjectConfig()
        if (platform=="ios"){
            projectConfig.setProjectname(projectConfigUIIOS.name.text)
            projectConfig.setPkg(projectConfigUIIOS.pkg.text)
            projectConfig.setLanguage(projectConfigUIIOS.language.selectedItem as String)
            projectConfig.setUi(projectConfigUIIOS.ui.selectedItem as String)
        }else{
            projectConfig.setProjectname(projectConfigUI.name.text)
            projectConfig.setPkg(projectConfigUI.pkg.text)
            projectConfig.setLanguage(projectConfigUI.language.selectedItem as String)
            val xui=projectConfigUI.ui.selectedItem as String
            val index = xui.indexOf('[')
            val cleanedString = if (index != -1) {
                xui.substring(0, index)
            } else {
                xui
            }
            projectConfig.setUi(cleanedString)
        }


        projectConfig.setTs(projectConfig.getLanguage().equals("ts"))
        projectConfig.setEnd()
        return super.validate()
    }
}