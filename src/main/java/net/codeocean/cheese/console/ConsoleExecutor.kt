package net.codeocean.cheese.console

import net.codeocean.cheese.utils.IconsUtils
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.JBColor
import java.awt.BorderLayout
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel


class ConsoleExecutor(private val project: Project) : Disposable {
    private var rerunAction: Runnable? = null
    private var stopAction: Runnable? = null
    private var stopEnabled: Computable<Boolean>? = null

    init {
        consoleViews.computeIfAbsent(
            project
        ) { project: Project? ->
            this.createConsoleView(
                project
            )
        }

        consoleViews[project]!!.addMessageFilter { line, offset ->
            // 创建 TextAttributes 对象，用于定义文本属性
            val textAttributes = TextAttributes()

            when {
                line.contains(" E ", ignoreCase = false) -> {
                    // 设置前景色为红色，表示错误
                    textAttributes.foregroundColor = JBColor.RED
                }
                line.contains(" W ", ignoreCase = false) -> {
                    // 设置前景色为黄色，表示警告
                    textAttributes.foregroundColor = JBColor.YELLOW
                }
                line.contains(" I ", ignoreCase = false) -> {
                    // 设置前景色为蓝色，表示信息
                    textAttributes.foregroundColor = JBColor.BLUE
                }
                else -> {
                    // 如果没有匹配项，默认颜色
                    textAttributes.foregroundColor = JBColor(Color(200, 200, 200), Color(150, 150, 150))
                }
            }
            return@addMessageFilter com.intellij.execution.filters.Filter.Result(
                offset - line.length,  // 起始位置
                offset,                // 结束位置
                null,                  // 可选的高亮或超链接
                textAttributes         // 应用的文本属性
            )
        }



    }

    fun withReturn(returnAction: Runnable?): ConsoleExecutor {
        this.rerunAction = returnAction
        return this
    }

    fun withStop(stopAction: Runnable?, stopEnabled: Computable<Boolean>?): ConsoleExecutor {
        this.stopAction = stopAction
        this.stopEnabled = stopEnabled
        return this
    }

    private fun createConsoleView(project: Project?): ConsoleView {
        val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project!!)
        return consoleBuilder.console
    }

    override fun dispose() {
        consoleViews.remove(project)
        Disposer.dispose(this)
    }

    fun run() {
        if (project.isDisposed) {
            return
        }

        val executor: Executor = RunExecutor.getRunExecutorInstance ?: return

        val factory = RunnerLayoutUi.Factory.getInstance(project)
        val layoutUi = factory.create("runnerId", "runnerTitle", "sessionName", project)
        val consolePanel = createConsolePanel(consoleViews[project])

        val descriptor = RunContentDescriptor(object : RunProfile {
            @Throws(ExecutionException::class)
            override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
                return null
            }

            override fun getName(): String {
                return "终不似，少年游"
            }

            override fun getIcon(): Icon? {
                return null
            }
        }, DefaultExecutionResult(), layoutUi)
        descriptor.executionId = System.nanoTime()

        val content =
            layoutUi.createContent("contentId", consolePanel, "Cheese Console", AllIcons.Debugger.Console, consolePanel)
        content.isCloseable = false
        layoutUi.addContent(content)
        layoutUi.options.setLeftToolbar(
            createActionToolbar(
                consolePanel,
                consoleViews[project]!!, layoutUi, descriptor, executor
            ), "RunnerToolbar"
        )

        Disposer.register(descriptor, this)
        Disposer.register(content, consoleViews[project]!!)
        if (stopAction != null) {
            Disposer.register(
                consoleViews[project]!!
            ) { stopAction!!.run() }
        }
        val runContentManager = RunContentManager.getInstance(project)
        runContentManager.showRunContent(executor, descriptor)
        //        ExecutionManager.getInstance(project).getContentManager().showRunContent(executor, descriptor);
    }

    private fun createActionToolbar(
        consolePanel: JPanel,
        consoleView: ConsoleView,
        layoutUi: RunnerLayoutUi,
        descriptor: RunContentDescriptor,
        executor: Executor
    ): ActionGroup {
        val actionGroup = DefaultActionGroup()
        actionGroup.add(RerunAction(consolePanel, consoleView))
        actionGroup.add(StopAction())
        actionGroup.add(consoleView.createConsoleActions()[2])
        actionGroup.add(consoleView.createConsoleActions()[3])
        actionGroup.add(consoleView.createConsoleActions()[5])
        actionGroup.add(ConsoleAnAction("custom action", "custom action", IconsUtils.getImage("cheese.svg")))
        return actionGroup
    }

    private fun createConsolePanel(consoleView: ConsoleView?): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(consoleView!!.component, BorderLayout.CENTER)
        return panel
    }

    private inner class RerunAction(consolePanel: JComponent?, private val consoleView: ConsoleView) :
        AnAction("Rerun", "Rerun", AllIcons.Actions.Restart), DumbAware {
        init {
            registerCustomShortcutSet(CommonShortcuts.getRerun(), consolePanel)
        }

        override fun actionPerformed(e: AnActionEvent) {
            Disposer.dispose(consoleView)
            rerunAction!!.run()
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isVisible = rerunAction != null
            e.presentation.icon = AllIcons.Actions.Restart
        }

        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
    }

    private inner class StopAction : AnAction("Stop", "Stop", AllIcons.Actions.Suspend), DumbAware {
        override fun actionPerformed(e: AnActionEvent) {
            stopAction!!.run()
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isVisible = stopAction != null
            e.presentation.isEnabled = stopEnabled != null && stopEnabled!!.compute()
        }

        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
    }

    companion object {
        private val consoleViews: MutableMap<Project, ConsoleView> = ConcurrentHashMap()


        private var project1: Project? = null
        fun setProject(project: Project?) {
            project1 = project
        }



        fun printToConsole(project: Project?, text: String, contentType: ConsoleViewContentType?) {
            val consoleView = consoleViews[project] ?: return
            val utf8Text = text.toByteArray(Charsets.UTF_8).toString(Charsets.UTF_8)
            val file = LocalFileSystem.getInstance().findFileByPath(text)
            if (file != null) {
                consoleView.print(
                    String.format("file:///%s", file.path.replace("\\", "/")) + "\n",
                    contentType!!
                )
            } else {
                consoleView.print(utf8Text + "\n", contentType!!)
            }
        }

        val consoleView: ConsoleView?
            get() = consoleViews[project1]

        fun getConsoleView(project: Project?): ConsoleView {
            return consoleViews[project]!!
        }

        fun printToConsole(text: String, contentType: ConsoleViewContentType?) {
            val consoleView = consoleViews[project1] ?: return

            val file = LocalFileSystem.getInstance().findFileByPath(text)
            if (file != null) {
                consoleView.print(
                    String.format("file:///%s", file.path.replace("\\", "/")) + "\n",
                    contentType!!
                )
            } else {
                consoleView.print(text + "\n", contentType!!)
            }
        }
    }
}