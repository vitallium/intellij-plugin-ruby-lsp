package com.github.vitallium.rubylsp

import com.github.vitallium.rubylsp.services.RubyLspServerService
import com.github.vitallium.rubylsp.settings.RubyLspSettingsStateComponent
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.util.*
import kotlin.concurrent.schedule

object ServerProcessHandler {
    fun addListeners(handler: OSProcessHandler, project: Project): OSProcessHandler {
        handler.addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
                super.processTerminated(event)

                val rubyLspServerService = project.service<RubyLspServerService>()
                if (rubyLspServerService.isRestarting) {
                    return
                }

                rubyLspServerService.setTerminatingStatus()

                val settingState = RubyLspSettingsStateComponent.getInstance(project).lspSettings
                if (!settingState.restartLspOnCrash) {
                    return
                }

                Timer().schedule(3000) {
                    rubyLspServerService.restartServer()
                }
            }

            override fun processNotStarted() {
                super.processNotStarted()

                val rubyLspServerService = project.service<RubyLspServerService>()
                rubyLspServerService.setTerminatingStatus()
            }

            override fun startNotified(event: ProcessEvent) {
                super.startNotified(event)

                val rubyLspServerService = project.service<RubyLspServerService>()
                rubyLspServerService.setRunningStatus()
            }
        })

        return handler
    }
}
