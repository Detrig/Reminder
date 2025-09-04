package github.detrig.reminder.audio

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher

class ButtonUi(
    id: Int,
    textResId: Int,
    containerIdMatcher: Matcher<View>?,
    containerClassTypeMatcher: Matcher<View>
) {
    private val interaction = onView(
        allOf(
            containerIdMatcher,
            containerClassTypeMatcher,
            withText(textResId),
            withId(id)
        )
    )

    fun assertVisible() {
        interaction.check(matches(isDisplayed()))
    }

    fun assertNotVisible() {
        interaction.check(matches(not((isDisplayed()))))
    }

    fun click() {
        interaction.perform(androidx.test.espresso.action.ViewActions.click())
    }
}
