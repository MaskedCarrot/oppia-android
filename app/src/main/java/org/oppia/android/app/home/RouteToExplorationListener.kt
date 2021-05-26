package org.oppia.android.app.home

import org.oppia.android.app.model.ExplorationCheckpoint

/** Listener for when an activity should route to a exploration. */
interface RouteToExplorationListener {
  fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    explorationCheckpoint: ExplorationCheckpoint
  )
}
