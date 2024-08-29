package coco.cheese.ide.action.pm;

import coco.cheese.ide.Env;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

public class ConfigUtil {

    public static void setRunning(Project project, boolean value) {
        PropertiesComponent.getInstance(project).setValue(Env.RUNNING_KEY, value);
    }

    public static boolean getRunning(Project project){
        return PropertiesComponent.getInstance(project).getBoolean(Env.RUNNING_KEY);
    }
}