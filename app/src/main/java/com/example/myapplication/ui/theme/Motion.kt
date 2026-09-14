package com.example.myapplication.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

/**
 * The app's motion budget.
 *
 * One place, so a reveal, a selection and a whole screen change all move at the
 * same tempo instead of each screen inventing its own duration. The steps are
 * the Material 3 speed scale, where about 150ms is the baseline for the
 * interface answering a touch: faster reads as twitchy, slower as laggy.
 *
 * [easing] is the Material 3 standard curve (0.4, 0, 0.2, 1), chosen rather
 * than inherited: the value settles quickly while the eye is still catching
 * up, which is what makes a short transition read as precise instead of soft.
 * It happens to match `tween`'s default, so naming it changes no existing
 * motion; it stops the next animation from quietly picking a different curve.
 *
 * Reduced motion is deliberately not handled here. Compose scales every
 * duration these tokens feed by the system animation scale, so a device with
 * animations turned off already collapses them to the next frame. Spinners are
 * infinite rather than duration-driven and keep turning, which is what the
 * platform intends: the preference asks for less motion, not for a dead
 * interface.
 */
object MileLogMotion {
    /** Tactile: something was touched. */
    const val tap = 100

    /** The interface answering: a press, a small state flip. */
    const val fast = 150

    /** Small reveals and confirmations: a selection, a saved receipt. */
    const val standard = 200

    /** Overlays and reflowing lists, where the eye has more distance to track. */
    const val medium = 250

    /** A region changing shape: an expanding banner, a drawer's worth of layout. */
    const val large = 300

    /** A destination arriving. */
    const val screenEnter = 220

    /** A destination leaving. Shorter than the entrance: the user already knows it. */
    const val screenExit = 170

    /** A reading settling on the dial. Long enough to be followed, not waited on. */
    const val readout = 450

    /** The one curve. Spring-based motion is reserved for interruptible gestures. */
    val easing: Easing = FastOutSlowInEasing
}
