package com.hexforhn.hex;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.test.ActivityTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.TextView;

import com.hexforhn.hex.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class FrontPageActivityTest extends ActivityTestCase {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void testToolbarTitleIsShown() {
        onView(allOf(
                withParent(isAssignableFrom(Toolbar.class)),
                isAssignableFrom(TextView.class))
            ).check(matches(withText("Front Page")));
    }
}
