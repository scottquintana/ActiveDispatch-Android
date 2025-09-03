package com.thirtyhelens.ActiveDispatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.maps.MapsInitializer
import com.thirtyhelens.ActiveDispatch.analytics.Analytics
import com.thirtyhelens.ActiveDispatch.ui.onboarding.OnboardingEntry
import com.thirtyhelens.ActiveDispatch.ui.onboarding.OnboardingViewModel

class MainActivity : ComponentActivity() {
    private val onboardingVm: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        Analytics.init(this)

        // Keep splash until the VM finishes its initial check
        splash.setKeepOnScreenCondition { !onboardingVm.isReady.value }
        MapsInitializer.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            OnboardingEntry(onboardingVm)
        }
    }
}