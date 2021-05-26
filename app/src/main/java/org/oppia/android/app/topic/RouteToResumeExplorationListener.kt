package org.oppia.android.app.topic

import org.oppia.android.app.model.ExplorationCheckpoint

interface RouteToResumeExplorationListener {
  fun routeToResumeExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    explorationCheckpoint: ExplorationCheckpoint
  )
}