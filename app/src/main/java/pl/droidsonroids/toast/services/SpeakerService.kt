package pl.droidsonroids.toast.services

import io.reactivex.Single
import pl.droidsonroids.toast.data.api.speaker.SpeakersResponse
import pl.droidsonroids.toast.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Query

interface SpeakerService {
    @GET("speakers")
    fun getSpeakers(@Query("per_page") pageSize: Int = Constants.PAGE_SIZE, @Query("page") pageNumber: Int = Constants.FIRST_PAGE): Single<SpeakersResponse>
}