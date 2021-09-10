package com.github.zzzj1233.checkin

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory

class YamlCheckInFactory : CheckinHandlerFactory() {

    override fun createHandler(panel: CheckinProjectPanel, ctx: CommitContext) = YamlCheckInHandler(panel, ctx)

}