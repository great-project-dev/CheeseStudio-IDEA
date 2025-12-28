package net.codeocean.cheese.infrastructure

import net.codeocean.cheese.domain.model.vo.ProjectConfigVO


class DataState {

    private var projectConfigVO: ProjectConfigVO = ProjectConfigVO()

    fun getProjectConfigVO(): ProjectConfigVO {
        return projectConfigVO
    }

    fun setProjectConfigVO(projectConfigVO: ProjectConfigVO) {
        this.projectConfigVO = projectConfigVO
    }
}