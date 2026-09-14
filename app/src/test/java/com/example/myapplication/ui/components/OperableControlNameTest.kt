package com.example.myapplication.ui.components

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.R
import com.example.myapplication.ui.navigation.MileLogBottomBar
import com.example.myapplication.ui.navigation.MileLogRoutes
import com.example.myapplication.ui.theme.MileLogTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Guards the rule that anything a user can operate says what it is.
 *
 * A name may come either from the label a control draws or from a description
 * set on it, but it has to reach the semantics tree: that tree is what a screen
 * reader reads, and a button without a name is announced as nothing at all.
 * These run on the JVM, so a regression is caught without a device.
 */
@RunWith(AndroidJUnit4::class)
// Pinned to the API level the app targets. Left to itself the runner picks the
// compile SDK, whose runtime cannot be instrumented yet.
@Config(sdk = [36])
class OperableControlNameTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    /**
     * An operable node carrying a name, whether that name is drawn text or a
     * description. Read off the merged tree, which is the shape a screen reader
     * is handed.
     */
    private fun namedOperable(matcher: SemanticsMatcher = SemanticsMatcher("any") { true }) =
        hasClickAction() and matcher and hasAccessibleName()

    private fun hasAccessibleName() = SemanticsMatcher("has a non-blank accessible name") { node ->
        val described = node.config.getOrNull(SemanticsProperties.ContentDescription)
            ?.any { it.isNotBlank() } == true
        val labelled = node.config.getOrNull(SemanticsProperties.Text)
            ?.any { it.text.isNotBlank() } == true
        described || labelled
    }

    private fun names(name: String) = SemanticsMatcher("is named '$name'") { node ->
        val described = node.config.getOrNull(SemanticsProperties.ContentDescription)?.contains(name) == true
        val labelled = node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == name } == true
        described || labelled
    }

    @Test
    fun thePrimaryActionSaysWhatItDoes() {
        val label = context.getString(R.string.fab_add_label)
        composeRule.setContent {
            MileLogTheme { MileLogFab(onClick = {}) }
        }

        composeRule.onAllNodes(namedOperable(names(label))).assertCountEquals(1)
    }

    @Test
    fun everyBarDestinationSaysWhereItGoes() {
        composeRule.setContent {
            MileLogTheme {
                MileLogBottomBar(currentRoute = MileLogRoutes.DASHBOARD, onTabSelected = {})
            }
        }

        listOf(
            R.string.bottom_nav_dashboard_label,
            R.string.bottom_nav_history_label,
            R.string.bottom_nav_reports_label,
            R.string.bottom_nav_vehicles_label,
            R.string.bottom_nav_settings_label
        ).forEach { labelRes ->
            val name = context.getString(labelRes)
            composeRule.onAllNodes(namedOperable(names(name))).assertCountEquals(1)
        }
    }
}
