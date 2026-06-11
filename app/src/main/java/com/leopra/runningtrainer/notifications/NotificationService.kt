package com.leopra.runningtrainer.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.WorkoutType
import java.time.LocalTime
import java.time.ZoneId

class NotificationService(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Cancels all existing workout alarms then schedules one per future non-rest,
     * non-completed workout at [hour]:[minute] local time.
     * ID scheme mirrors Flutter: weekIndex * 7 + dayOfWeek.
     */
    fun scheduleForPlan(plan: TrainingPlan, hour: Int, minute: Int) {
        cancelRange(plan.weeks.size)

        val now = System.currentTimeMillis()

        for (wi in plan.weeks.indices) {
            for (workout in plan.weeks[wi].workouts) {
                if (workout.type == WorkoutType.rest) continue
                if (workout.isCompleted) continue

                val workoutDate = plan.startDate
                    .plusDays((wi * 7 + workout.dayOfWeek - 1).toLong())

                val triggerMillis = workoutDate
                    .atTime(LocalTime.of(hour, minute))
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                if (triggerMillis <= now) continue

                val id = wi * 7 + workout.dayOfWeek
                val km = workout.distanceKm?.let { " · ${"%.1f".format(it)} km" }.orEmpty()
                val pending = buildPendingIntent(id, "Time to run!", "${workout.title}$km")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
                }
            }
        }
    }

    fun cancelAll(totalWeeks: Int) = cancelRange(totalWeeks)

    private fun cancelRange(weeks: Int) {
        val maxId = weeks * 7 + 7
        for (id in 1..maxId) {
            val intent = Intent(context, WorkoutAlarmReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context, id, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pending?.let { alarmManager.cancel(it) }
        }
    }

    private fun buildPendingIntent(id: Int, title: String, body: String): PendingIntent {
        val intent = Intent(context, WorkoutAlarmReceiver::class.java).apply {
            putExtra(WorkoutAlarmReceiver.EXTRA_ID, id)
            putExtra(WorkoutAlarmReceiver.EXTRA_TITLE, title)
            putExtra(WorkoutAlarmReceiver.EXTRA_BODY, body)
        }
        return PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
