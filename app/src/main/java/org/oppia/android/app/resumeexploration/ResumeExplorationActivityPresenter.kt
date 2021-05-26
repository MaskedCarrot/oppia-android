package org.oppia.android.app.resumeexploration

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.databinding.ResumeExplorationActivityBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult

@ActivityScope
class ResumeExplorationActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val oppiaLogger: OppiaLogger
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  fun handleOnCreate(
    context: Context,
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    val binding = DataBindingUtil.setContentView<ResumeExplorationActivityBinding>(
      activity,
      R.layout.resume_exploration_activity
    )

    binding.apply {
      lifecycleOwner = activity
    }

    binding.explorationResume.setOnClickListener {
      resumeExploration(
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen,
        explorationCheckpoint
      )
    }

    binding.explorationStartOver.setOnClickListener {
      startExplorationOver(
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen,
      )
    }

  }

  private fun resumeExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    playExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen,
      explorationCheckpoint
    )
    activity.finish()
  }

  private fun startExplorationOver(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
  ) {
    playExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen,
      ExplorationCheckpoint.getDefaultInstance()
    )
  }

  private fun playExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    checkpoint: ExplorationCheckpoint
  ) {
    explorationDataController.startPlayingExploration(
      explorationId,
      checkpoint
    ).observe(
      activity,
      Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> oppiaLogger.d(
            "ResumeExplorationActivity",
            "Loading exploration"
          )
          result.isFailure() -> oppiaLogger.e(
            "ResumeExplorationActivity",
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            oppiaLogger.d(
              "ResumeExplorationActivity",
              "Successfully loaded exploration"
            )
            routeToExplorationListener.routeToExploration(
              internalProfileId,
              topicId,
              storyId,
              explorationId,
              backflowScreen,
              checkpoint
            )
          }
        }
      }
    )
  }
}