package org.oppia.android.app.resumeexploration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import javax.inject.Inject
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.State
import org.oppia.android.app.player.exploration.ExplorationActivity

class ResumeExplorationActivity :
  InjectableAppCompatActivity(),
  RouteToExplorationListener {

  @Inject
  lateinit var resumeExplorationActivityPresenter: ResumeExplorationActivityPresenter
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private lateinit var state: State
  private var backflowScreen: Int? = null
  private lateinit var explorationCheckpoint: ExplorationCheckpoint

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(RESUME_EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, -1)
    topicId = intent.getStringExtra(RESUME_EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)
    storyId = intent.getStringExtra(RESUME_EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY)
    explorationId = intent.getStringExtra(RESUME_EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY)
    backflowScreen = intent.getIntExtra(RESUME_EXPLORATION_ACTIVITY_BACKFLOW_SCREEN_KEY, -1)
    explorationCheckpoint = ExplorationCheckpoint.parseFrom(
      intent.getByteArrayExtra(RESUME_EXPLORATION_ACTIVITY_EXPLORATION_CHECKPOINT_KEY)
    )
    resumeExplorationActivityPresenter.handleOnCreate(
      this,
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen,
      explorationCheckpoint
    )
  }

  // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
  companion object {
    /** Returns a new [Intent] to route to [ExplorationActivity] for a specified exploration. */

    const val RESUME_EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY =
      "ResumeExplorationActivity.profile_id"
    const val RESUME_EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY =
      "ResumeExplorationActivity.topic_id"
    const val RESUME_EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY =
      "ResumeExplorationActivity.story_id"
    const val RESUME_EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY =
      "ResumeExplorationActivity.exploration_id"
    const val RESUME_EXPLORATION_ACTIVITY_BACKFLOW_SCREEN_KEY =
      "ResumeExplorationActivity.backflow_screen"
    const val RESUME_EXPLORATION_ACTIVITY_EXPLORATION_CHECKPOINT_KEY =
      "ResumeExplorationActivity.exploration_checkpoint"

    fun createResumeExplorationActivityIntent(
      context: Context,
      profileId: Int,
      topicId: String,
      storyId: String,
      explorationId: String,
      backflowScreen: Int?,
      explorationCheckpoint: ExplorationCheckpoint
    ): Intent {
      val intent = Intent(context, ResumeExplorationActivity::class.java)
      intent.putExtra(RESUME_EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, profileId)
      intent.putExtra(RESUME_EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
      intent.putExtra(RESUME_EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
      intent.putExtra(RESUME_EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY, explorationId)
      intent.putExtra(RESUME_EXPLORATION_ACTIVITY_BACKFLOW_SCREEN_KEY, backflowScreen)
      intent.putExtra(
        RESUME_EXPLORATION_ACTIVITY_EXPLORATION_CHECKPOINT_KEY,
        explorationCheckpoint.toByteArray()
      )
      return intent
    }
  }

  override fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen,
        explorationCheckpoint
      )
    )
  }
}