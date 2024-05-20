package com.github.vitallium.rubylsp

import com.github.vitallium.rubylsp.settings.RubyLspSettingsConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType

@Suppress("UnstableApiUsage")
class RubyLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        if (file.fileType != RubyFileType.RUBY) return

        val rubyLspServerDescriptor = RubyLspServerDescriptor.tryCreate(project, file) ?: return
        serverStarter.ensureServerStarted(rubyLspServerDescriptor)
    }

    override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?) = LspServerWidgetItem(
        lspServer, currentFile,
        RubyLspIcons.StatusWidgetIcon, RubyLspSettingsConfigurable::class.java)
}
