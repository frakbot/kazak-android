package io.kazak.repository

import io.kazak.api.KazakApi
import io.kazak.assertThat
import io.kazak.model.*
import io.kazak.verify
import org.mockito.Mockito
import rx.subjects.BehaviorSubject
import java.util.ArrayList
import java.util.Date
import org.junit.Before
import org.junit.Test

public class KazakDataRepositoryTest {

    val talkId = Id("TestId")
    val testDate = Date()

    val mockApi: KazakApi = mock()
    val mockFavoritesRepo: FavoriteSessionsRepository = mock()
    val scheduleObservable: BehaviorSubject<Schedule> = BehaviorSubject.create()

    val repository: DataRepository = KazakDataRepository(mockApi, mockFavoritesRepo)

    @Before
    fun setup() {
        Mockito.`when`(mockApi.fetchSchedule()).thenReturn(scheduleObservable)
        scheduleObservable.onNext(testSchedule())
    }

    @Test
    fun itFetchesAScheduleFromApiIfMemoryCacheEmpty() {
        val schedule = repository.getSchedule().toBlocking().first()

        verify(mockApi).fetchSchedule()
        assertThat(schedule).isEqualTo(testSchedule())
    }

    @Test
    fun itFindsATalkGivenAnId() {
        val talk = repository.getEvent(talkId).toBlocking().first()

        assertThat(talk).isEqualTo(testTalk())
    }

    private fun testSchedule() = Schedule(listOf(testDay()))

    private fun testDay() = Day(testDate, listOf(testTalk()))

    private fun testTalk() = Talk(talkId, "", "", TimeSlot(testDate, testDate), listOf(Room(Id(""), "")), testSpeakers(), null)

    private fun testSpeakers() = Speakers(testSpeakersList())

    private fun testSpeakersList(): List<Speaker> {
        val speakers = ArrayList<Speaker>(1)
        speakers + Speaker(Id(""), "", null, null, null, null)
        return speakers
    }

    private inline fun <reified T : Any> mock() = Mockito.mock(T::class.java)

}
