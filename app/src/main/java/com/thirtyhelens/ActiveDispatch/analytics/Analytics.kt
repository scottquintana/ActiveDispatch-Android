package com.thirtyhelens.ActiveDispatch.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object Analytics {

    private var fa: FirebaseAnalytics? = null

    fun init(context: Context) {
        if (fa == null) {
            fa = FirebaseAnalytics.getInstance(context.applicationContext)
        }
    }

    fun logIncidentTypeTapped(incidentType: String) {
        val bundle = Bundle().apply {
            putString("incident_id", incidentType)
        }
        fa?.logEvent("incident_tapped", bundle)
    }

    fun logScreen(screenName: String, screenClass: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        fa?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logCustomEvent(name: String, params: Map<String, String> = emptyMap()) {
        val bundle = Bundle()
        params.forEach { (k, v) -> bundle.putString(k, v) }
        fa?.logEvent(name, bundle)
    }
}