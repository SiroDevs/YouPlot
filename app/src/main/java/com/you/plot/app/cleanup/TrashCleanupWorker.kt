package com.you.plot.app.cleanup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.you.plot.core.domain.repos.PlanRepo
import com.you.plot.core.domain.repos.RouteRepo
import com.you.plot.core.domain.repos.StartPointRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Runs once a day and permanently purges anything that has been sitting in the trash
 * bin for more than 30 days across routes, plans, and start points.
 */
@HiltWorker
class TrashCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val routeRepo: RouteRepo,
    private val planRepo: PlanRepo,
    private val startPointRepo: StartPointRepo,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = runCatching {
        val cutoff = System.currentTimeMillis() - RETENTION_MS
        routeRepo.purgeExpiredRoutes(cutoff)
        planRepo.purgeExpiredPlans(cutoff)
        startPointRepo.purgeExpired(cutoff)
        Result.success()
    }.getOrElse { Result.retry() }

    companion object {
        const val WORK_NAME = "youplot_trash_cleanup"
        private val RETENTION_MS = TimeUnit.DAYS.toMillis(30)

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TrashCleanupWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
