package github.detrig.reminder.audio

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not

open class BaseUi(
    protected val interaction: ViewInteraction
) {
    fun click() {
        interaction.perform(androidx.test.espresso.action.ViewActions.click())
    }

    fun assertVisible() {
        interaction.check(matches(isDisplayed()))
    }

    fun assertNotVisible() {
        interaction.check(matches(not(isDisplayed())))
    }

    fun assertDoesNotExist() {
        interaction.check(doesNotExist())
    }

    fun waitUntilVisible(timeout: Long = 5000) {
        val endTime = System.currentTimeMillis() + timeout
        while (System.currentTimeMillis() < endTime) {
            try {
                assertVisible()
                return
            } catch (e: Exception) {
                Thread.sleep(100)
            }
        }
        throw AssertionError("Element did not become visible in $timeout ms")
    }

    fun waitUntilNotVisible(timeout: Long = 5000) {
        val endTime = System.currentTimeMillis() + timeout
        while (System.currentTimeMillis() < endTime) {
            try {
                assertNotVisible()
                return
            } catch (e: Exception) {
                Thread.sleep(100)
            }
        }
        throw AssertionError("Element did not become invisible in $timeout ms")
    }

}

fun createBaseUi(
    id: Int,
    containerIdMatcher: Matcher<View>,
    containerClassTypeMatcher: Matcher<View>
): BaseUi {
    return BaseUi(
        onView(
            allOf(
                containerIdMatcher,
                containerClassTypeMatcher,
                withId(id)
            )
        )
    )
}

fun createTextViewUi(
    id: Int,
    textResId: Int,
    containerIdMatcher: Matcher<View>,
    containerClassTypeMatcher: Matcher<View>
): BaseUi {
    return BaseUi(
        onView(
            allOf(
                containerIdMatcher,
                containerClassTypeMatcher,
                withId(id),
                withText(textResId)
            )
        )
    )
}