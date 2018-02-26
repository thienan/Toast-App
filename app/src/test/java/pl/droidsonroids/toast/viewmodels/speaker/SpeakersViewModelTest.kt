package pl.droidsonroids.toast.viewmodels.speaker

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.Mock
import pl.droidsonroids.toast.RxTestBase
import pl.droidsonroids.toast.app.utils.managers.AnalyticsEventTracker
import pl.droidsonroids.toast.data.Page
import pl.droidsonroids.toast.data.State
import pl.droidsonroids.toast.data.api.speaker.ApiSpeaker
import pl.droidsonroids.toast.data.mapper.toDto
import pl.droidsonroids.toast.repositories.speaker.SpeakersRepository
import pl.droidsonroids.toast.testSpeaker
import pl.droidsonroids.toast.testSpeakers
import pl.droidsonroids.toast.testSpeakersPage
import pl.droidsonroids.toast.utils.LoadingStatus
import pl.droidsonroids.toast.utils.NavigationRequest
import pl.droidsonroids.toast.utils.SortingType
import java.io.IOException

class SpeakersViewModelTest : RxTestBase() {

    @Mock
    lateinit var speakersRepository: SpeakersRepository
    @Mock
    lateinit var analyticsEventTracker: AnalyticsEventTracker

    @Mock
    lateinit var clock: Clock

    lateinit var speakersViewModel: SpeakersViewModel

    @Test
    fun shouldLoadFirstPage() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(testSpeakersPage))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)

        checkIsFirstPageLoaded()
    }

    private fun checkIsFirstPageLoaded() {
        val speakerItemViewModelList = speakersViewModel.speakersSubject.value
        val testSpeaker = testSpeakers.first()
        val resultSpeakerViewModel = (speakerItemViewModelList.first() as? State.Item)?.item

        assertSpeaker(resultSpeakerViewModel, testSpeaker)
        assertThat(speakersViewModel.loadingStatus.get(), equalTo(LoadingStatus.SUCCESS))
    }

    private fun assertSpeaker(resultSpeakerViewModel: SpeakerItemViewModel?, testSpeaker: ApiSpeaker) {
        assertThat(resultSpeakerViewModel?.id, equalTo(testSpeaker.id))
        assertThat(resultSpeakerViewModel?.name, equalTo(testSpeaker.name))
        assertThat(resultSpeakerViewModel?.avatar, equalTo(testSpeaker.avatar.toDto()))
    }


    @Test
    fun shouldFailLoadFirstPage() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.error(IOException()))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)

        val speakerItemViewModelList: List<State<SpeakerItemViewModel>> = speakersViewModel.speakersSubject.value

        assertThat(speakerItemViewModelList.isEmpty(), equalTo(true))
        assertThat(speakersViewModel.loadingStatus.get(), equalTo(LoadingStatus.ERROR))
    }

    @Test
    fun shouldLoadFirstPageAfterRetry() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.error(IOException()))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)

        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(testSpeakersPage))

        speakersViewModel.retryLoading()

        checkIsFirstPageLoaded()
    }

    @Test
    fun shouldHaveLoadingItemWhenNextPageAvailable() {
        val testSpeakersPageWithNextPageAvailable = testSpeakersPage.copy(allPagesCount = 2)
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(testSpeakersPageWithNextPageAvailable))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)

        val speakerItemViewModelList = speakersViewModel.speakersSubject.value
        checkIsFirstPageLoaded()
        assertThat(speakerItemViewModelList.last(), equalTo(State.Loading as State<SpeakerItemViewModel>))
    }

    @Test
    fun shouldHaveErrorItemWhenNextPageLoadFailed() {
        val testSpeakersPageWithNextPageAvailable = testSpeakersPage.copy(allPagesCount = 2)
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(testSpeakersPageWithNextPageAvailable))
        whenever(speakersRepository.getSpeakersPage(eq(2), any())).thenReturn(Single.error(IOException()))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)

        speakersViewModel.loadNextPage()

        val speakerItemViewModelList = speakersViewModel.speakersSubject.value
        assertThat(speakerItemViewModelList.last() is State.Error, equalTo(true))
    }

    @Test
    fun shouldLoadNextPage() {
        val itemsCountOnAllPages = 2
        val testSpeakersPageWithNextPageAvailable = testSpeakersPage.copy(allPagesCount = 2)
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(testSpeakersPageWithNextPageAvailable))
        val secondSpeakersPage = testSpeakersPage.copy(pageNumber = 2, allPagesCount = 2)
        whenever(speakersRepository.getSpeakersPage(eq(2), any())).thenReturn(Single.just(secondSpeakersPage))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)

        speakersViewModel.loadNextPage()

        val speakerItemViewModelList = speakersViewModel.speakersSubject.value
        assertThat(speakerItemViewModelList.size, equalTo(itemsCountOnAllPages))
        assertSpeaker((speakerItemViewModelList[1] as? State.Item)?.item, testSpeakers.first())
    }

    @Test
    fun shouldRequestNavigationToSpeakerDetails() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(testSpeakersPage))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)
        val speakerItemViewModelList = speakersViewModel.speakersSubject.value
        val speakerItemViewModel = (speakerItemViewModelList.first() as? State.Item)?.item
        val testObserver = speakersViewModel.navigationSubject.test()

        speakerItemViewModel?.onClick()

        testObserver.assertValue {
            it is NavigationRequest.SpeakerDetails
                    && it.id == testSpeaker.id
        }
    }

    @Test
    fun shouldSortSpeakersAlphabetical() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.error(IOException()))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)

        whenever(speakersRepository.getSpeakersPage(sortingQuery = SortingType.ALPHABETICAL.toQuery())).thenReturn(Single.just(testSpeakersPage))

        speakersViewModel.onAlphabeticalSortingClick()
        assertThat(speakersViewModel.sortingType.get(), equalTo(SortingType.ALPHABETICAL))
    }

    @Test
    fun shouldSortSpeakersByDate() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.error(IOException()))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)

        whenever(speakersRepository.getSpeakersPage(sortingQuery = SortingType.DATE.toQuery())).thenReturn(Single.just(testSpeakersPage))

        speakersViewModel.onDateSortingClick()
        assertThat(speakersViewModel.sortingType.get(), equalTo(SortingType.DATE))
    }

    @Test
    fun shouldLoadFirstPageAfterDateSorting() {
        whenever(speakersRepository.getSpeakersPage(sortingQuery = SortingType.DATE.toQuery())).thenReturn(Single.just(testSpeakersPage))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)

        speakersViewModel.onDateSortingClick()

        checkIsFirstPageLoaded()
    }

    @Test
    fun shouldLoadFirstPageAfterAlphabeticalSorting() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.error(IOException()))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)

        whenever(speakersRepository.getSpeakersPage(pageNumber = any(), sortingQuery = eq(SortingType.ALPHABETICAL.toQuery()))).thenReturn(Single.just(testSpeakersPage))

        speakersViewModel.onAlphabeticalSortingClick()
        checkIsFirstPageLoaded()
    }

    @Test
    fun shouldRefreshData() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(Page(items = emptyList(), pageNumber = 1, allPagesCount = 1)))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(testSpeakersPage))

        speakersViewModel.refresh()

        checkIsFirstPageLoaded()
    }

    @Test
    fun shouldHideSwipeRefreshLoaderWhenDataRefreshed() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(Page(items = emptyList(), pageNumber = 1, allPagesCount = 1)))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(testSpeakersPage))
        val testObserver = speakersViewModel.isSwipeRefreshLoaderVisibleSubject.test()

        speakersViewModel.refresh()

        testObserver.assertValue { !it }
    }

    @Test
    fun shouldRetainDataWhenRefreshFailed() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(testSpeakersPage))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.error(IOException()))

        speakersViewModel.refresh()

        checkIsFirstPageLoaded()
    }

    @Test
    fun shouldRequestSnackBarWhenRefreshFailed() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(testSpeakersPage))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.error(IOException()))
        val testObserver = speakersViewModel.navigationSubject.test()

        speakersViewModel.refresh()

        testObserver.assertValue { it is NavigationRequest.SnackBar }
    }


    @Test
    fun shouldHideSwipeRefreshLoaderWhenRefreshFailed() {
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.just(Page(items = emptyList(), pageNumber = 1, allPagesCount = 1)))
        speakersViewModel = SpeakersViewModel(speakersRepository, analyticsEventTracker, clock)
        whenever(speakersRepository.getSpeakersPage(any(), any())).thenReturn(Single.error(IOException()))
        val testObserver = speakersViewModel.isSwipeRefreshLoaderVisibleSubject.test()

        speakersViewModel.refresh()

        testObserver.assertValue { !it }
    }
}