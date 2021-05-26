package org.oppia.android.domain.exploration

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ExplorationCheckpointDatabase
import org.oppia.android.app.model.OrderedExploration
import org.oppia.android.app.model.ProfileId
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync

private const val CACHE_NAME = "exploration_checkpoint_database"
private const val RETRIEVE_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID =
  "retrieve_exploration_checkpoint_provider_id"
private const val RETRIEVE_ORDERED_CHECKPOINT_DATA_PROVIDER_ID =
  "retrieve_exploration_checkpoint_provider_id"
private const val RECORD_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID =
  "record_exploration_checkpoint_provider_id"
private const val DELETE_OLDEST_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID =
  "delete_oldest_exploration_checkpoint_provider_id"

@Singleton
class ExplorationCheckpointController @Inject constructor(
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val oppiaLogger: OppiaLogger,
  @ExplorationStorageDatabaseSize private val explorationCheckpointDatabaseSize: Int
) {

  class ExplorationCheckpointDatabaseSizeLimitExceeded(message: String) : Exception(message)

  private enum class ExplorationCheckpointActionStatus {
    CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_NOT_EXCEEDED,
    CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_EXCEEDED,
    SUCCESS
  }

  private val cacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<ExplorationCheckpointDatabase>>()

  fun recordExplorationCheckpoint(
    profileId: ProfileId,
    explorationId: String,
    explorationName: String,
    explorationCheckpoint: ExplorationCheckpoint
  ): DataProvider<Any?> {
    val deferred =
      retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
        updateInMemoryCache = true
      ) {

        val explorationCheckpointDatabaseBuilder = it.toBuilder()

        explorationCheckpointDatabaseBuilder
          .putExplorationCheckpoint(explorationId, explorationCheckpoint)
          .addOrderedExploration(
            OrderedExploration.newBuilder()
              .setExplorationId(explorationId)
              .setExplorationName(explorationName)
          )

        val explorationCheckpointDatabase = explorationCheckpointDatabaseBuilder.build()

        if (explorationCheckpointDatabase.serializedSize <= explorationCheckpointDatabaseSize)
          Pair(
            explorationCheckpointDatabase,
            ExplorationCheckpointActionStatus.CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_NOT_EXCEEDED
          )
        else
          Pair(
            explorationCheckpointDatabase,
            ExplorationCheckpointActionStatus.CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_EXCEEDED
          )
      }
    return dataProviders.createInMemoryDataProviderAsync(
      RECORD_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(deferred)
    }
  }

  fun retrieveExplorationCheckpoint(
    profileId: ProfileId,
    explorationId: String
  ): DataProvider<ExplorationCheckpoint> {
    return retrieveCacheStore(profileId)
      .transformAsync(RETRIEVE_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID) { explorationCheckpointDatabase ->
        AsyncResult.success(
          explorationCheckpointDatabase.explorationCheckpointMap[explorationId]
            ?: ExplorationCheckpoint.getDefaultInstance()
        )
      }
  }

  fun retrieveOldestSavedExplorationCheckpoint(
    profileId: ProfileId
  ): DataProvider<OrderedExploration> {
    return retrieveCacheStore(profileId)
      .transformAsync(RETRIEVE_ORDERED_CHECKPOINT_DATA_PROVIDER_ID) { explorationCheckpointDatabase ->
        AsyncResult.success(
          explorationCheckpointDatabase.orderedExplorationList[0]
            ?: OrderedExploration.getDefaultInstance()
        )
      }
  }

  fun deleteOldestSavedExplorationCheckpoint(
    profileId: ProfileId
  ): DataProvider<Any?> {
    val deferred = retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) { explorationCheckpointDatabase ->
      val explorationCheckpointDatabaseBuilder = explorationCheckpointDatabase.toBuilder()
      val oldestExplorationCheckpoint = explorationCheckpointDatabase.orderedExplorationList[0]
        ?: OrderedExploration.getDefaultInstance()

      // TODO("handle case when ordered exploration is of default instance")

      explorationCheckpointDatabaseBuilder
        .removeExplorationCheckpoint(oldestExplorationCheckpoint.explorationId)
        .removeOrderedExploration(0)

      Pair(
        explorationCheckpointDatabaseBuilder.build(),
        ExplorationCheckpointActionStatus.SUCCESS
      )
    }
    return dataProviders.createInMemoryDataProviderAsync(
      DELETE_OLDEST_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(deferred)
    }
  }

  private suspend fun getDeferredResult(
    deferred: Deferred<ExplorationCheckpointActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      ExplorationCheckpointActionStatus.CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_EXCEEDED ->
        AsyncResult.success(null)
      ExplorationCheckpointActionStatus.CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_NOT_EXCEEDED ->
        AsyncResult.success(
          ExplorationCheckpointDatabaseSizeLimitExceeded(
            "Exploration checkpoint Database size limit exceeded."
          )
        )
      ExplorationCheckpointActionStatus.SUCCESS ->
        AsyncResult.success(null)
    }
  }

  private fun retrieveCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<ExplorationCheckpointDatabase> {
    val cacheStore = if (profileId in cacheStoreMap) {
      cacheStoreMap[profileId]!!
    } else {
      val cacheStore =
        cacheStoreFactory.createPerProfile(
          CACHE_NAME,
          ExplorationCheckpointDatabase.getDefaultInstance(),
          profileId
        )
      cacheStoreMap[profileId] = cacheStore
      cacheStore
    }

    cacheStore.primeCacheAsync().invokeOnCompletion { throwable ->
      throwable?.let {
        oppiaLogger.e(
          "ExplorationCheckpointController",
          "Failed to prime cache ahead of LiveData conversion for ExplorationCheckpointController.",
          it
        )
      }
    }
    return cacheStore
  }
}
