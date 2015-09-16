package io.kazak.repository

import io.kazak.api.KazakApi
import io.kazak.assertThat
import io.kazak.model.*
import io.kazak.verify
import org.mockito.Mockito
import rx.subjects.BehaviorSubject
import java.util.ArrayList
import java.util.Date
import org.junit.Before as before
import org.junit.Test as test

public class KazakDataRepositoryTest {

    val talkId = Id("TestId")
    val testDate = Date()

    val mockApi: KazakApi = Mockito.mock(KazakApi::class.java)
    val mockFavoritesRepo: FavoriteSessionsRepository = Mockito.mock(FavoriteSessionsRepository::class.java)
    val scheduleObservable: BehaviorSubject<Schedule> = BehaviorSubject.create()

    val repository: DataRepository = KazakDataRepository(mockApi, mockFavoritesRepo)

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
        val talk = repository.getEvent(talkId).toBlocking().first()

        assertThat(talk).isEqualTo(testTalk())
    }

    private fun testSchedule() = Schedule(listOf(testDay()))

    private fun testDay() = Day(testDate, listOf(testTalk()))

    private fun testTalk() = Talk(talkId, "", TimeSlot(testDate, testDate), listOf(Room(Id(""), "")), testSpeakers(), null)

    private fun testSpeakers() = Speakers(testSpeakersList())

    private fun testSpeakersList(): List<Speaker> {
        val speakers = ArrayList<Speaker>(1)
        speakers + Speaker(Id(""), "", null, null, null, null)
        return speakers
    }

}
