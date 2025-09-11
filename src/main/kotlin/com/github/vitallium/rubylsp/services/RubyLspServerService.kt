package com.github.vitallium.rubylsp.services

import com.github.vitallium.rubylsp.RubyLspServerSupportProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerManager

@Service(Service.Level.PROJECT)
class RubyLspServerService(private val project: Project) {
    fun restartServer() {
        if (project.isDefault) return

        LspServerManager.getInstance(project)
            .stopAndRestartIfNeeded(RubyLspServerSupportProvider::class.java)
    }
}
