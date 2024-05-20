package com.github.vitallium.rubylsp.services

import com.github.vitallium.rubylsp.RubyLspBundle
import com.github.vitallium.rubylsp.RubyLspServerSupportProvider
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerManager

@Service(Service.Level.PROJECT)
@Suppress("UnstableApiUsage")
class RubyLspServerService(private val project: Project) {
    private var status = Status.TERMINATING

    val isRestarting get() = status == Status.RESTARTING

    fun setRunningStatus() {
        status = Status.RUNNING
    }

    fun setTerminatingStatus() {
        status = Status.TERMINATING
        notifyTerminating()
    }

    fun restartServer() {
        LspServerManager.getInstance(project)
            .stopAndRestartIfNeeded(RubyLspServerSupportProvider::class.java)

        status = Status.RESTARTING
        notifyRestart()
    }

    private fun notifyRestart() {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("RubyLsp")
            .createNotification(
                RubyLspBundle.message("language.server.restarted"),
                "",
                NotificationType.INFORMATION
            )
            .notify(project)
    }

    private fun notifyTerminating() {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("RubyLsp")
            .createNotification(
                RubyLspBundle.message("language.server.is.stopped"),
                "",
                NotificationType.WARNING
            )
            .notify(project)
    }

    private enum class Status {
        RUNNING,
        RESTARTING,
        TERMINATING
    }
}
