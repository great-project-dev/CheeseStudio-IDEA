package net.codeocean.cheese.action.pm





import net.codeocean.cheese.Env
import net.codeocean.cheese.data.SettingConfig

import net.codeocean.cheese.utils.ToastUtils
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.annotations.NotNull


class VersionAction : AnAction() {

    override fun actionPerformed(@NotNull e: AnActionEvent) {
        val configValue = Env.VERSION
        if (StringUtil.isEmpty(configValue)) {
            ToastUtils.error("版本号错误")
        }
        ToastUtils.info(configValue)
    }
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

}