package com.thirtyhelens.ActiveDispatch.ui.onboarding

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("first_run_prefs")

object FirstRunPrefs {
    private val KEY_TOS_ACCEPTED = booleanPreferencesKey("tos_accepted")
    private val KEY_LOCATION_FLOW_DONE = booleanPreferencesKey("location_flow_done")

    fun tosAccepted(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[KEY_TOS_ACCEPTED] ?: false }

    fun locationFlowDone(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[KEY_LOCATION_FLOW_DONE] ?: false }

    suspend fun setTosAccepted(context: Context, accepted: Boolean) {
        context.dataStore.edit { it[KEY_TOS_ACCEPTED] = accepted }
    }

    suspend fun setLocationFlowDone(context: Context, done: Boolean) {
        context.dataStore.edit { it[KEY_LOCATION_FLOW_DONE] = done }
    }
}