package com.github.vitallium.rubylsp.actions

import com.github.vitallium.rubylsp.RubyLspServerSupportProvider
import com.github.vitallium.rubylsp.services.RubyLspServerService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.platform.lsp.api.LspServerManager

@Suppress("UnstableApiUsage")
class RestartRubyLspServerAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project == null || project.isDefault) return

        val rubyLspServerService = project.service<RubyLspServerService>()
        rubyLspServerService.restartServer()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val project = e.project
        if (project == null || project.isDefault) return

        if (LspServerManager.getInstance(project).getServersForProvider(RubyLspServerSupportProvider::class.java)
                .isEmpty()
        ) {
            e.presentation.isEnabled = false
        } else {
            e.presentation.isEnabled = true
        }
    }


    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
