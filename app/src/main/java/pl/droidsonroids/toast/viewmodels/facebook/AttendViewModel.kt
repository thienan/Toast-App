package pl.droidsonroids.toast.viewmodels.facebook

import android.databinding.ObservableField
import io.reactivex.subjects.PublishSubject
import pl.droidsonroids.toast.data.enums.AttendStatus
import pl.droidsonroids.toast.utils.NavigationRequest
import pl.droidsonroids.toast.utils.SourceAttending
import java.util.*

interface AttendViewModel {
    val attendStatus: ObservableField<AttendStatus>
    val navigationRequests: PublishSubject<NavigationRequest>
    val isPastEvent: ObservableField<Boolean>

    fun setEvent(id: String, date: Date, sourceAttending: SourceAttending)

    fun invalidateAttendState()

    fun onAttendClick()

    fun dispose()
}