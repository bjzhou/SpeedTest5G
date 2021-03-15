package com.hinnka.speedtest5g.ext

import android.content.res.Resources
import androidx.compose.ui.unit.TextUnit
import com.hinnka.speedtest5g.App

val Int.i18n: String
    get() = App.instance.getString(this)

val TextUnit.px: Float
    get() = value * Resources.getSystem().displayMetrics.density