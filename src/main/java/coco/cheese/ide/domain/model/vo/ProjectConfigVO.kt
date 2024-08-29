package coco.cheese.ide.domain.model.vo

import javax.swing.JComboBox
import javax.swing.JTextField

open class ProjectConfigVO {
    private var projectname: String? = null
    private var pkg: String? = null
    private var type: String? = null
    private var ui: String? = null
    private var ts: Boolean? = null
    private var end: String? = null
    fun getProjectname(): String? {
        return projectname
    }

    fun setProjectname(projectname: String?) {
        this.projectname = projectname
    }

    fun getPkg(): String? {
        return pkg
    }

    fun setPkg(pkg: String?) {
        this.pkg = pkg
    }

    fun getType(): String? {
        return type
    }

    fun setType(type: String?) {
        this.type = type
    }


    fun getUi(): String? {
        return ui
    }

    fun setUi(type: String?) {
        this.ui = type
    }


    fun getTs(): Boolean? {
        return ts
    }

    fun setTs(type: Boolean?) {
        this.ts = type
    }

    fun getEnd(): String? {

        return end
    }

    fun setEnd() {
        if (this.ts == true) {
            this.end ="ts"
        }else{
            this.end = "js"
        }

    }



}