package pl.droidsonrioids.toast.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Test

class DummyViewModelTest {
    @Test
    fun isTextValid() {
        assertEquals("Under Construction", DummyViewModel().text)
    }
}