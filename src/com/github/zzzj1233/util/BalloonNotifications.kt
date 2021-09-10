package com.github.zzzj1233.util

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager


object BalloonNotifications {

    private const val ID = "Intellij IDEA Yaml checker"

    private const val TITLE = "Yaml checker"

    private val balloonGroup = NotificationGroup(ID, NotificationDisplayType.BALLOON, true)

    fun showSuccessNotification(message: String, title: String = TITLE, project: Project? = ProjectManager.getInstance().defaultProject) {
        showNotification(message, title, NotificationType.INFORMATION, project)
    }

    fun showWarningNotification(message: String, title: String = TITLE, project: Project? = ProjectManager.getInstance().defaultProject) {
        showNotification(message, title, NotificationType.WARNING, project)
    }

    fun showErrorNotification(message: String, title: String = TITLE, project: Project? = ProjectManager.getInstance().defaultProject) {
        showNotification(message, title, NotificationType.ERROR, project)
    }

    private fun showNotification(message: String, title: String, type: NotificationType, project: Project?) {
        balloonGroup.createNotification(title, message, type).notify(project)
    }

}
