package net.codeocean.cheese.action.epm

import coco.cheese.core.sendMessage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import javax.swing.Icon

class Terminal (name: String, description: String,icon: Icon) : AnAction(name, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {

        sendMessage("工具")
    }
}