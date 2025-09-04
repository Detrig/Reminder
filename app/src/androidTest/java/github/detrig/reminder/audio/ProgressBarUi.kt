package github.detrig.reminder.audio

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class ProgressBarUi(
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
){

}
