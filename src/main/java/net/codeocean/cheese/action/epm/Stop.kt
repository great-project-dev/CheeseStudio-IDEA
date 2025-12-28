package net.codeocean.cheese.action.epm

import coco.cheese.core.sendMessage
import net.codeocean.cheese.Env
import net.codeocean.cheese.utils.ToastUtils


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import javax.swing.Icon

class Stop (name: String, description: String,icon: Icon) : AnAction(name, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {

        if (!Env.cd){
            ToastUtils.error("请先启动心跳，再停止。")
            return
        }

            sendMessage("3")


    }
}