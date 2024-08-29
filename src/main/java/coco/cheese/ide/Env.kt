package coco.cheese.ide

import coco.cheese.ide.infrastructure.DataSetting
import java.io.File

object Env {

    const val RUNNING_KEY: String = "runningKey"
    var dS: DataSetting?=null
    fun getDataSetting(): DataSetting? {
        if(dS==null){
            dS=DataSetting()
        }
        return dS
    }

    object OS{
        val separator = File.separator
    }

}