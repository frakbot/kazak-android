package uk.co.droidcon.kazak.repository

import org.mockito.Mockito
import rx.subjects.BehaviorSubject
import uk.co.droidcon.kazak.api.DroidconApi
import uk.co.droidcon.kazak.assertThat
import uk.co.droidcon.kazak.model.*
import uk.co.droidcon.kazak.verify
import java.util.*
import org.junit.Before as before
import org.junit.Test as test

public class DroidconDataRepositoryTest {

    val talkId = "TestId"
    val testDate = Date()

    val mockApi: DroidconApi = Mockito.mock(javaClass())
    val scheduleObservable: BehaviorSubject<Schedule> = BehaviorSubject.create()

    val repository: DataRepository = DroidconDataRepository(mockApi)

    before
    fun setup() {
        Mockito.`when`(mockApi.fetchSchedule()).thenReturn(scheduleObservable)
        scheduleObservable.onNext(testSchedule())
    }

    test
    fun itFetchesAScheduleFromApiIfMemoryCacheEmpty() {
        val schedule = repository.getSchedule().toBlocking().first()

        verify(mockApi).fetchSchedule()
        assertThat(schedule).isEqualTo(testSchedule())
    }

    test
    fun itFindsATalkGivenAnId() {
        val talk = repository.getTalk(talkId).toBlocking().first()

        assertThat(talk).isEqualTo(testTalk())
    }

    private fun testSchedule() = Schedule(listOf(testDay()))

    private fun testDay() = Day(testDate, listOf(testTalk()))

    private fun testTalk() = Talk(talkId, "", TimeSlot(testDate, testDate), Room("", ""), testSpeakers())

    private fun testSpeakers() = Speakers(testSpeakersList())

    private fun testSpeakersList(): List<Speaker> {
        val speakers = ArrayList<Speaker>(1)
        speakers + Speaker("", "")
        return speakers
    }

}
