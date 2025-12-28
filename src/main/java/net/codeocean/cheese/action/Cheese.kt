package net.codeocean.cheese.action

import net.codeocean.cheese.action.epm.Build
import net.codeocean.cheese.action.epm.CheckSDK
import net.codeocean.cheese.action.epm.Docs
import net.codeocean.cheese.action.epm.FixSDK
import net.codeocean.cheese.action.epm.HOT
import net.codeocean.cheese.action.epm.PIP
import net.codeocean.cheese.action.epm.Run
import net.codeocean.cheese.action.epm.Runx
import net.codeocean.cheese.action.epm.Stop
import net.codeocean.cheese.action.epm.Tools
import net.codeocean.cheese.action.epm.Ui
import net.codeocean.cheese.action.epm.UpdateProject
import net.codeocean.cheese.action.epm.Wifi
import net.codeocean.cheese.utils.IconsUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup

class Cheese : DefaultActionGroup("Cheese", "Cheese工具集合", IconsUtils.Companion.getImage("cheese.svg")) {

    init {

        addSeparator()
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return arrayOf(
            Wifi("启动心跳", "", IconsUtils.Companion.getImage("wifi.svg")),
            Run("运行", "", IconsUtils.Companion.getImage("run.svg")),
            Runx("运行选中", "", IconsUtils.Companion.getImage("runx.svg")),
            Ui("预览Ui", "", IconsUtils.Companion.getImage("ui.svg")),
            Stop("停止", "", IconsUtils.Companion.getImage("stop.svg")),
            Tools("抓抓", "", IconsUtils.Companion.getImage("zz.svg")),
            Build("构建", "", IconsUtils.Companion.getImage("build.svg")),
            HOT("构建热更新包", "", IconsUtils.Companion.getImage("hot.svg")),
            PIP("配置pypi库", "", IconsUtils.Companion.getImage("pip.svg")),
            Docs("文档", "", IconsUtils.Companion.getImage("doc.svg")),
            FixSDK("修复SDK", "", IconsUtils.Companion.getImage("fix.svg")),
            CheckSDK("检查更新SDK", "", IconsUtils.Companion.getImage("check.svg")),

            UpdateProject("更新项目", "", IconsUtils.Companion.getImage("up.svg")),

        )
    }


}