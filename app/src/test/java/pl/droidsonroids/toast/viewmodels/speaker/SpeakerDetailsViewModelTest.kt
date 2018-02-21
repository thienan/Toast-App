package pl.droidsonroids.toast.viewmodels.speaker

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import pl.droidsonroids.toast.RxTestBase
import pl.droidsonroids.toast.app.utils.managers.AnalyticsEventTracker
import pl.droidsonroids.toast.data.dto.speaker.SpeakerDetailsDto
import pl.droidsonroids.toast.data.mapper.toDto
import pl.droidsonroids.toast.repositories.speaker.SpeakersRepository
import pl.droidsonroids.toast.testSpeakerDetailsDto
import pl.droidsonroids.toast.utils.LoadingStatus
import pl.droidsonroids.toast.utils.NavigationRequest
import java.io.IOException

class SpeakerDetailsViewModelTest : RxTestBase() {
    private val speakerId = 0L

    @Mock
    lateinit var speakersRepository: SpeakersRepository
    @Mock
    lateinit var analyticsEventTracker: AnalyticsEventTracker
    @InjectMocks
    lateinit var speakerDetailsViewModel: SpeakerDetailsViewModel

    @Test
    fun shouldLoadSpeakerDetails() {
        mockSpeakerWith(testSpeakerDetailsDto.toSingle())

        assertSpeakerDetails()
        assertThat(speakerDetailsViewModel.loadingStatus.get(), equalTo(LoadingStatus.SUCCESS))
    }

    @Test
    fun shouldFailLoadSpeakerDetails() {
        mockSpeakerWith(Single.error(IOException()))

        assertThat(speakerDetailsViewModel.loadingStatus.get(), equalTo(LoadingStatus.ERROR))
    }


    @Test
    fun shouldRetryLoadSpeakerDetails() {
        whenever(speakersRepository.getSpeaker(speakerId)).thenReturn(Single.error(Exception()))
        speakerDetailsViewModel.init(speakerId)

        whenever(speakersRepository.getSpeaker(speakerId)).thenReturn(testSpeakerDetailsDto.toSingle())

        speakerDetailsViewModel.retryLoading()

        assertSpeakerDetails()
        assertThat(speakerDetailsViewModel.loadingStatus.get(), equalTo(LoadingStatus.SUCCESS))
    }

    @Test
    fun shouldOpenGithubPageWhenLinkIsClicked() {
        speakerDetailsViewModel.github.set(testSpeakerDetailsDto.github)
        val testObserver = speakerDetailsViewModel.navigationSubject.test()

        speakerDetailsViewModel.onGithubClick()

        testObserver
                .assertNoErrors()
                .assertValue { (it as NavigationRequest.Website).url == testSpeakerDetailsDto.github }
    }


    @Test
    fun shouldOpenWebsitePageWhenLinkIsClicked() {
        speakerDetailsViewModel.website.set(testSpeakerDetailsDto.website)
        val testObserver = speakerDetailsViewModel.navigationSubject.test()

        speakerDetailsViewModel.onWebsiteClick()

        testObserver
                .assertNoErrors()
                .assertValue { (it as NavigationRequest.Website).url == testSpeakerDetailsDto.website }
    }


    @Test
    fun shouldOpenTwitterPageWhenLinkIsClicked() {
        speakerDetailsViewModel.twitter.set(testSpeakerDetailsDto.twitter)
        val testObserver = speakerDetailsViewModel.navigationSubject.test()

        speakerDetailsViewModel.onTwitterClick()

        testObserver
                .assertNoErrors()
                .assertValue { (it as NavigationRequest.Website).url == testSpeakerDetailsDto.twitter }
    }


    @Test
    fun shouldOpenEmailPageWhenLinkIsClicked() {
        speakerDetailsViewModel.email.set(testSpeakerDetailsDto.email)
        val testObserver = speakerDetailsViewModel.navigationSubject.test()

        speakerDetailsViewModel.onEmailClick()

        testObserver
                .assertNoErrors()
                .assertValue { (it as NavigationRequest.Email).email == testSpeakerDetailsDto.email }
    }

    @Test
    fun shouldRequestNavigationToSpeakerTalkDetails() {
        mockSpeakerWith(testSpeakerDetailsDto.toSingle())
        val testObserver = speakerDetailsViewModel.navigationSubject.test()

        val speakerTalkViewModel = speakerDetailsViewModel.talksSubject.value.first()
        speakerTalkViewModel.onReadMore()

        testObserver.assertValue {
            it is NavigationRequest.SpeakerTalkDetails
                    && it.speakerTalkDto == speakerTalkViewModel.toDto()
        }
    }

    @Test
    fun shouldRequestNavigationToEventDetails() {
        mockSpeakerWith(testSpeakerDetailsDto.toSingle())
        val testObserver = speakerDetailsViewModel.navigationSubject.test()

        val eventItemViewModel = speakerDetailsViewModel.talksSubject.value.first().eventItemViewModel
        eventItemViewModel.onClick()

        testObserver.assertValue {
            it is NavigationRequest.EventDetails
                    && it.id == eventItemViewModel.id
        }
    }

    private fun mockSpeakerWith(value: Single<SpeakerDetailsDto>) {
        whenever(speakersRepository.getSpeaker(speakerId)).thenReturn(value)
        speakerDetailsViewModel.init(speakerId)
    }

    private fun assertSpeakerDetails() {
        assertThat(speakerDetailsViewModel.name.get(), equalTo(testSpeakerDetailsDto.name))
        assertThat(speakerDetailsViewModel.job.get(), equalTo(testSpeakerDetailsDto.job))
        assertThat(speakerDetailsViewModel.bio.get(), equalTo(testSpeakerDetailsDto.bio))
        assertThat(speakerDetailsViewModel.avatar.get(), equalTo(testSpeakerDetailsDto.avatar))
        assertThat(speakerDetailsViewModel.github.get(), equalTo(testSpeakerDetailsDto.github))
        assertThat(speakerDetailsViewModel.website.get(), equalTo(testSpeakerDetailsDto.website))
        assertThat(speakerDetailsViewModel.twitter.get(), equalTo(testSpeakerDetailsDto.twitter))
        assertThat(speakerDetailsViewModel.email.get(), equalTo(testSpeakerDetailsDto.email))
    }
}