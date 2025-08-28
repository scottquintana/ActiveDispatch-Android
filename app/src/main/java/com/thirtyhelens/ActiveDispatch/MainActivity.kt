package com.thirtyhelens.ActiveDispatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.thirtyhelens.ActiveDispatch.ui.onboarding.OnboardingEntry
import com.thirtyhelens.ActiveDispatch.ui.onboarding.OnboardingViewModel

class MainActivity : ComponentActivity() {
    private val onboardingVm: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash until the VM finishes its initial check
        splash.setKeepOnScreenCondition { !onboardingVm.isReady.value }

        setContent {
            OnboardingEntry(onboardingVm)
        }
    }
}