package com.usehover.hovertest.store

import android.content.SharedPreferences

class PrefModel(
        val sharedPrefs: SharedPreferences,
        val editor: SharedPreferences.Editor
)