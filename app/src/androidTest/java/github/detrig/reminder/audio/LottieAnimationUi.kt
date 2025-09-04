package github.detrig.reminder.audio

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import github.detrig.reminder.audio.matchers.LottieAnimationStateMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class LottieAnimationUi(
    id: Int,
    containerIdMatcher: Matcher<View>,
    containerClassTypeMatcher: Matcher<View>
) : BaseUi(
    onView(
        allOf(
            containerIdMatcher,
            containerClassTypeMatcher,
            withId(id)
        )
    )
) {
    fun assertPlaying() {
        interaction.check(matches(isCompletelyDisplayed()))
        interaction.check(matches(LottieAnimationStateMatcher(true)))
    }

    fun assertNotPlaying() {
        interaction.check(matches(isCompletelyDisplayed()))
        interaction.check(matches(LottieAnimationStateMatcher(false)))
    }

    fun waitUntilPlaying(timeout: Long = 5000) {
        val endTime = System.currentTimeMillis() + timeout
        while (System.currentTimeMillis() < endTime) {
            try {
                assertPlaying()
                return
            } catch (e: Exception) {
                Thread.sleep(100)
            }
        }
        throw AssertionError("Lottie animation did not start playing in $timeout ms")
    }

    fun waitUntilNotPlaying(timeout: Long = 5000) {
        val endTime = System.currentTimeMillis() + timeout
        while (System.currentTimeMillis() < endTime) {
            try {
                assertNotPlaying()
                return
            } catch (e: Exception) {
                Thread.sleep(100)
            }
        }
        throw AssertionError("Lottie animation did not stop playing in $timeout ms")
    }
}
