package uk.co.droidcon.kazak.repository

import org.fest.assertions.api.Assertions
import org.junit.Before as before
import org.junit.Test as test
import org.mockito.Mockito
import rx.subjects.BehaviorSubject
import uk.co.droidcon.kazak.api.DroidconApi
import uk.co.droidcon.kazak.model.*
import java.util.Date

public class DroidconDataRepositoryTest {

    val talkId = "TestId"
    val testDate = Date()

    val mockApi : DroidconApi = Mockito.mock(javaClass())
    val scheduleObservable : BehaviorSubject<Schedule> = BehaviorSubject.create()

    val repository : DataRepository = DroidconDataRepository(mockApi)

    before
    fun setup() {
        Mockito.`when`(mockApi.fetchSchedule()).thenReturn(scheduleObservable)
        scheduleObservable.onNext(testSchedule())
    }

    test
    fun itFetchesAScheduleFromApiIfMemoryCacheEmpty() {
        val schedule = repository.getSchedule().toBlocking().first();

        Mockito.verify(mockApi).fetchSchedule()
        Assertions.assertThat(schedule).isEqualTo(testSchedule())
    }

    test
    fun itFindsATalkGivenAnId() {
        val talk = repository.getTalk(talkId).toBlocking().first();

        Assertions.assertThat(talk).isEqualTo(testTalk())
    }

    private fun testSchedule() = Schedule(listOf(testDay()))

    private fun testDay() = Day(testDate, listOf(testTalk()))

    private fun testTalk() = Talk(talkId, "", TimeSlot(testDate, testDate), Room("", ""))


}
