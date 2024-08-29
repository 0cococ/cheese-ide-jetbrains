package coco.cheese.ide.action.pm;


import coco.cheese.ide.console.ConsoleExecutor;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

public class ConsoleViewAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        runExecutor(e.getProject());

    }

    public void runExecutor(Project project) {
        if (project == null) {
            return;
        }
        ConsoleExecutor executor = new ConsoleExecutor(project);
        // 设置restart和stop
        executor.withReturn(() -> runExecutor(project)).withStop(() -> ConfigUtil.setRunning(project,false), () ->
                ConfigUtil.getRunning(project));
        executor.run();
    }
    @Override
    public @org.jetbrains.annotations.NotNull ActionUpdateThread getActionUpdateThread() {

        return ActionUpdateThread.EDT;
    }

}