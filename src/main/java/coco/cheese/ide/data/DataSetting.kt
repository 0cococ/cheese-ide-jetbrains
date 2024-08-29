package coco.cheese.ide.infrastructure

import coco.cheese.ide.domain.model.vo.ProjectConfigVO
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "DataSetting", storages = [Storage("plugin.xml")])
class  DataSetting : PersistentStateComponent<DataState>{
    private var state = DataState()

    fun getInstance(): DataSetting {
        return ServiceManager.getService(DataSetting::class.java)
    }

    override fun getState(): DataState {
        return state
    }

    override fun loadState(state: DataState) {
        this.state = state
    }

    fun getProjectConfig(): ProjectConfigVO {
        return state.getProjectConfigVO()
    }

}