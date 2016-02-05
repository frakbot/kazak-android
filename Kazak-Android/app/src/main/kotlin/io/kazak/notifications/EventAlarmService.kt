package io.kazak.notifications

import android.app.AlarmManager
import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import io.kazak.KazakApplication
import io.kazak.model.Session
import io.kazak.repository.DataRepository
import rx.Observable
import java.util.Calendar
import java.util.Date

class EventAlarmService : IntentService(EventAlarmService::class.java.name) {

    companion object {
        private val NOTIFICATION_INTERVAL_MINUTES = 10
    }

    val dataRepository: DataRepository

    init {
        dataRepository = KazakApplication.injector().dataRepository
    }

    override fun onHandleIntent(intent: Intent) {
        val notificationCreator = NotificationCreator(this)
        val notifier = Notifier.from(this)

        val calendar = Calendar.getInstance()
        val now = calendar.time
        calendar.add(Calendar.MINUTE, NOTIFICATION_INTERVAL_MINUTES)
        val notificationInterval = calendar.time

        val sessions = dataRepository.getFavorites()
                .take(1)
                .map { sortByStartingTime(it) }
                .flatMap { Observable.from(it) }

        sessions.filter { startingIn(now, notificationInterval, it) }
                .toList()
                .map { createNotifications(notificationCreator, it) }
                .subscribe { displayNotifications(it, notifier) }

        sessions.filter { startingAfter(notificationInterval, it) }
                .take(1)
                .subscribe { scheduleNextAlarm(it) }
    }

    private fun sortByStartingTime(sessions: List<Session>): List<Session> {
        return sessions.sortedBy { it.timeSlot().start }
    }

    private fun startingIn(now: Date, notificationInterval: Date, session: Session): Boolean {
        val sessionStart = session.timeSlot().start
        return now.before(sessionStart) && (sessionStart.before(notificationInterval) || sessionStart == notificationInterval)
    }

    private fun createNotifications(notificationCreator: NotificationCreator, sessions: List<Session>): List<Notification> {
        return notificationCreator.createFrom(sessions)
    }

    private fun displayNotifications(notifications: List<Notification>, notifier: Notifier) {
        notifier.showNotifications(notifications)
    }

    private fun startingAfter(notificationInterval: Date, session: Session): Boolean {
        val sessionStart = session.timeSlot().start
        return sessionStart.after(notificationInterval)
    }

    private fun scheduleNextAlarm(session: Session) {
        val talkStart = session.timeSlot().start
        val calendar = Calendar.getInstance()
        calendar.time = talkStart
        calendar.add(Calendar.MINUTE, -NOTIFICATION_INTERVAL_MINUTES)

        val serviceIntent = Intent(this@EventAlarmService, EventAlarmService::class.java)
        val pendingIntent = PendingIntent.getService(this@EventAlarmService, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

}
