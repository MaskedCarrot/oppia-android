package org.oppia.android.domain.exploration

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier
annotation class ExplorationStorageDatabaseSize

/** Provider to return any constants required during the storage of exploration checkpoints. */
@Module
class ExplorationStorageModule {

  /**
   * Provides the number of records that can be stored in EventLog's cache storage.
   * The current [ExplorationStorageDatabaseSize] is set to be 2MB(2097152 Bytes) per profile.
   * Taking 20 KB per checkpoint, it is expected to store about 100 checkpoints for every profile.
   */
  @Provides
  @ExplorationStorageDatabaseSize
  fun provideExplorationStorageDatabaseSize(): Int = 2097152
}