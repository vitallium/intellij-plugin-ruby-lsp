package com.github.vitallium.rubylsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
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
}
