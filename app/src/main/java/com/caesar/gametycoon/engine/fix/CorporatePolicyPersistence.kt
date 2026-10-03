package com.caesar.gametycoon.engine.fix

import android.content.Context
import android.content.SharedPreferences
import com.example.corporate.model.MergedDividendPolicy
import com.example.corporate.model.StandaloneDividendPolicy
import com.example.corporate.repository.CorporatePolicyRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Phase 2 Fix: CorporatePolicyPersistence
 *
 * Persists customized dividend allocation policies and profit split sliders
 * to SharedPreferences so player-configured corporate financial policies
 * survive app restarts and process recreation.
 */
object CorporatePolicyPersistence {

    private const val PREFS_NAME = "corporate_policy_prefs"
    private const val KEY_STANDALONE_POLICIES = "standalone_dividend_policies_json"
    private const val KEY_HOLDING_POLICIES = "holding_dividend_policies_json"

    private val gson = Gson()

    data class LoadedPolicies(
        val standalonePolicies: Map<String, StandaloneDividendPolicy>,
        val holdingPolicies: Map<String, MergedDividendPolicy>
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves current dividend policies to local storage asynchronously.
     */
    suspend fun savePolicies(
        context: Context,
        standalonePolicies: Map<String, StandaloneDividendPolicy>,
        holdingPolicies: Map<String, MergedDividendPolicy>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) = withContext(dispatcher) {
        try {
            val standaloneJson = gson.toJson(standalonePolicies)
            val holdingJson = gson.toJson(holdingPolicies)

            getPrefs(context).edit()
                .putString(KEY_STANDALONE_POLICIES, standaloneJson)
                .putString(KEY_HOLDING_POLICIES, holdingJson)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("PolicyPersistence", "Error saving corporate policies: ${e.message}")
        }
    }

    /**
     * Loads saved dividend policies from local storage.
     */
    suspend fun loadPolicies(
        context: Context,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): LoadedPolicies = withContext(dispatcher) {
        try {
            val prefs = getPrefs(context)
            val standaloneJson = prefs.getString(KEY_STANDALONE_POLICIES, null)
            val holdingJson = prefs.getString(KEY_HOLDING_POLICIES, null)

            val standaloneMap: Map<String, StandaloneDividendPolicy> = if (!standaloneJson.isNullOrBlank()) {
                val type = object : TypeToken<Map<String, StandaloneDividendPolicy>>() {}.type
                gson.fromJson(standaloneJson, type) ?: emptyMap()
            } else {
                emptyMap()
            }

            val holdingMap: Map<String, MergedDividendPolicy> = if (!holdingJson.isNullOrBlank()) {
                val type = object : TypeToken<Map<String, MergedDividendPolicy>>() {}.type
                gson.fromJson(holdingJson, type) ?: emptyMap()
            } else {
                emptyMap()
            }

            LoadedPolicies(
                standalonePolicies = standaloneMap,
                holdingPolicies = holdingMap
            )
        } catch (e: Exception) {
            android.util.Log.e("PolicyPersistence", "Error loading corporate policies: ${e.message}")
            LoadedPolicies(emptyMap(), emptyMap())
        }
    }

    /**
     * Initializes the [CorporatePolicyRepository] singleton with saved policies on startup.
     */
    suspend fun restoreToRepository(
        context: Context,
        repo: CorporatePolicyRepository = CorporatePolicyRepository.getInstance()
    ) {
        val loaded = loadPolicies(context)
        loaded.standalonePolicies.forEach { (id, policy) ->
            repo.setStandalonePolicy(id, policy)
        }
        loaded.holdingPolicies.forEach { (id, policy) ->
            repo.setHoldingPolicy(id, policy)
        }
    }

    /**
     * Flushes [CorporatePolicyRepository] active state to disk.
     */
    suspend fun persistFromRepository(
        context: Context,
        repo: CorporatePolicyRepository = CorporatePolicyRepository.getInstance()
    ) {
        savePolicies(
            context = context,
            standalonePolicies = repo.standalonePolicies.value,
            holdingPolicies = repo.holdingPolicies.value
        )
    }
}
