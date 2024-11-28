package com.github.vitallium.rubylsp

import com.github.vitallium.rubylsp.settings.RubyLspSettingsStateComponent
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider

class RubyLspLanguageServer(
    commands: List<String>,
    workingDirectory: String?,
    environmentVariables: Map<String, String>
) : ProcessStreamConnectionProvider(commands, workingDirectory, environmentVariables) {
    companion object {
        fun create(project: Project): RubyLspLanguageServer {
            val commands: List<String> = listOf("ruby-lsp")
            val workingDirectory = project.basePath

            val environmentVariables = mapOf<String, String>()

            return RubyLspLanguageServer(commands, workingDirectory, environmentVariables)
        }
    }
}
