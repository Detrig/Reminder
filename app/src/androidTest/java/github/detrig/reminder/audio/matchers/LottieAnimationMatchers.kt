package github.detrig.reminder.audio.matchers

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import com.airbnb.lottie.LottieAnimationView
import org.hamcrest.Description

class LottieAnimationStateMatcher(private val expectedPlaying: Boolean) :
    BoundedMatcher<View, LottieAnimationView>(LottieAnimationView::class.java) {

    override fun describeTo(description: Description) {
        description.appendText("Lottie animation is ${if (expectedPlaying) "playing" else "not playing"}")
    }

    override fun matchesSafely(item: LottieAnimationView): Boolean {
        return item.isAnimating == expectedPlaying
    }
}
