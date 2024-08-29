package coco.cheese.ide.console

import coco.cheese.ide.utils.IconsUtils
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
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.LocalFileSystem
import java.awt.BorderLayout
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
                return "愿作鸳鸯不羡仙"
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
} //public class CustomExecutor implements Disposable {
//
//    private static ConsoleView consoleView = null;
//
//    private Project project = null;
//
//    private Runnable rerunAction;
//    private Runnable stopAction;
//
//    private Computable<Boolean> stopEnabled;
//
//    public CustomExecutor withReturn(Runnable returnAction) {
//        this.rerunAction = returnAction;
//        return this;
//    }
//
//    public CustomExecutor withStop(Runnable stopAction, Computable<Boolean> stopEnabled) {
//        this.stopAction = stopAction;
//        this.stopEnabled = stopEnabled;
//        return this;
//    }
//
//    public CustomExecutor(@NotNull Project project) {
//        this.project = project;
//        consoleView = createConsoleView(project);
//    }
//
//    private ConsoleView createConsoleView(Project project) {
//        TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
////        consoleBuilder.addFilter(new MyBatisSQLFilter());
////        Filter[] filters = new MyBatisSQLFilter().getDefaultFilters(project);
////        for (Filter filter : filters) {
////            consoleBuilder.addFilter(filter);
////        }
//        return consoleBuilder.getConsole();
//    }
//
//    public static void printToConsole(String text, ConsoleViewContentType contentType) {
//        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(text);
//        if (file != null) {
//            consoleView.print(String.format("file:///%s", file.getPath().replace("\\", "/")) + "\n", contentType);
//        } else {
//            consoleView.print(text+ "\n", contentType);
//        }
//
//    }
//
//    @Override
//    public void dispose() {
//        Disposer.dispose(this);
//    }
//
//    public void run() {
//        if (project.isDisposed()) {
//            return;
//        }
//
//        Executor executor = CustomRunExecutor.getRunExecutorInstance();
//        if (executor == null) {
//            return;
//        }
//
//        final RunnerLayoutUi.Factory factory = RunnerLayoutUi.Factory.getInstance(project);
//        RunnerLayoutUi layoutUi = factory.create("runnerId", "runnerTitle", "sessionName", project);
//        final JPanel consolePanel = createConsolePanel(consoleView);
//
//        RunContentDescriptor descriptor = new RunContentDescriptor(new RunProfile() {
//            @Nullable
//            @Override
//            public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
//                return null;
//            }
//
//            @NotNull
//            @Override
//            public String getName() {
//                return "愿作鸳鸯不羡仙";
//            }
//
//            @Nullable
//            @Override
//            public Icon getIcon() {
//                return null;
//            }
//        }, new DefaultExecutionResult(), layoutUi);
//        descriptor.setExecutionId(System.nanoTime());
//
//        final Content content = layoutUi.createContent("contentId", consolePanel, "Cheese Console", AllIcons.Debugger.Console, consolePanel);
//        content.setCloseable(false);
//        layoutUi.addContent(content);
//        layoutUi.getOptions().setLeftToolbar(createActionToolbar(consolePanel, consoleView, layoutUi, descriptor, executor), "RunnerToolbar");
//
//
//        Disposer.register(descriptor,this);
//
//        Disposer.register(content, consoleView);
//        if (stopAction != null) {
//            Disposer.register(consoleView, () -> stopAction.run());
//        }
//        RunContentManager runContentManager = RunContentManager.getInstance(project);
//        runContentManager.showRunContent(executor, descriptor);
////        ExecutionManager.getInstance(project).getContentManager().showRunContent(executor, descriptor);
//    }
//
//    private ActionGroup createActionToolbar(JPanel consolePanel, ConsoleView consoleView, RunnerLayoutUi layoutUi, RunContentDescriptor descriptor, Executor executor) {
//        final DefaultActionGroup actionGroup = new DefaultActionGroup();
//        actionGroup.add(new RerunAction(consolePanel, consoleView));
//        actionGroup.add(new StopAction());
//        actionGroup.add(consoleView.createConsoleActions()[2]);
//        actionGroup.add(consoleView.createConsoleActions()[3]);
//        actionGroup.add(consoleView.createConsoleActions()[5]);
//        actionGroup.add(new CustomAction("custom action", "custom action", ICONS.Companion.getImage("cheese.svg")));
//        return actionGroup;
//    }
//
//    private JPanel createConsolePanel(ConsoleView consoleView) {
//        JPanel panel = new JPanel(new BorderLayout());
//
//        // 将 ConsoleView 的组件添加到 panel 的中心位置
//        panel.add(consoleView.getComponent(), BorderLayout.CENTER);
//
//        // 不设置 JPanel 和 ConsoleView 的首选大小，以便它们自动适应父容器的大小
//        // panel.setPreferredSize(new Dimension(800, 600)); // 删除这行
//        // consoleView.getComponent().setPreferredSize(new Dimension(800, 600)); // 删除这行
//
//        // 返回创建的面板
//        return panel;
//    }
//
//    private class RerunAction extends AnAction implements DumbAware {
//        private final ConsoleView consoleView;
//
//        public RerunAction(JComponent consolePanel, ConsoleView consoleView) {
//            super("Rerun", "Rerun", AllIcons.Actions.Restart);
//            this.consoleView = consoleView;
//            registerCustomShortcutSet(CommonShortcuts.getRerun(), consolePanel);
//        }
//
//        @Override
//        public void actionPerformed(AnActionEvent e) {
//            Disposer.dispose(consoleView);
//            rerunAction.run();
//        }
//
//        @Override
//        public void update(AnActionEvent e) {
//            e.getPresentation().setVisible(rerunAction != null);
//            e.getPresentation().setIcon(AllIcons.Actions.Restart);
//        }
//        @Override
//        public @NotNull ActionUpdateThread getActionUpdateThread() {
//            // 指定在 EDT（事件调度线程）上执行
//            return ActionUpdateThread.EDT;
//        }
//    }
//
//    private class StopAction extends AnAction implements DumbAware {
//        public StopAction() {
//            super("Stop", "Stop", AllIcons.Actions.Suspend);
//        }
//
//        @Override
//        public void actionPerformed(AnActionEvent e) {
//            stopAction.run();
//        }
//
//        @Override
//        public void update(AnActionEvent e) {
//            e.getPresentation().setVisible(stopAction != null);
//            e.getPresentation().setEnabled(stopEnabled != null && stopEnabled.compute());
//        }
//        @Override
//        public @NotNull ActionUpdateThread getActionUpdateThread() {
//            // 指定在 EDT（事件调度线程）上执行
//            return ActionUpdateThread.EDT;
//        }
//    }
//}