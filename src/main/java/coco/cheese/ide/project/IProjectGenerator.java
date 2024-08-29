package coco.cheese.ide.project;


import coco.cheese.ide.domain.model.vo.ProjectConfigVO;
import com.intellij.openapi.project.Project;

public interface IProjectGenerator {

    void doGenerator(Project project, String entryPath, ProjectConfigVO projectConfig);

}
