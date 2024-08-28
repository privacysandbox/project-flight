/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sspsdk

import android.adservices.adselection.AdSelectionConfig
import android.adservices.adselection.AdSelectionManager
import android.adservices.adselection.AdSelectionOutcome
import android.adservices.adselection.ReportEventRequest
import android.adservices.adselection.ReportEventRequest.FLAG_REPORTING_DESTINATION_BUYER
import android.adservices.adselection.ReportEventRequest.FLAG_REPORTING_DESTINATION_SELLER
import android.adservices.adselection.ReportImpressionRequest
import android.adservices.common.AdSelectionSignals
import android.adservices.common.AdTechIdentifier
import android.adservices.measurement.MeasurementManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.OutcomeReceiver
import android.os.ext.SdkExtensions
import android.util.Log
import android.view.MotionEvent
import androidx.concurrent.futures.CallbackToFutureAdapter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.common.util.concurrent.ListenableFuture
import java.lang.IllegalStateException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.stream.Collectors

@Suppress("NewApi")
class SspSdkImpl constructor(
    context: Context
) {
    /*
     * SHARED
     */
    private val logTag = "Flight SSP"
    private val topLevelDomain = "example.com"

    /*
     * PROTECTED AUDIENCES
     */
    private val adSelectionManager: AdSelectionManager
    private val adSelectionConfig: AdSelectionConfig
    // Use your own scoring logic and scoring trusted URLs
    private val scoringLogicUri =
        Uri.parse("https://$topLevelDomain/protected-audience/Logic/ScoringLogic.js")
    private val scoringTrustedUri=
        Uri.parse("https://$topLevelDomain/protected-audience/Functions/ScoringTrusted.js")

    // TODO: Replace with URI of buyer (can also pull this list from server)
    private val buyers = listOf(AdTechIdentifier.fromString(topLevelDomain))

    /*
     * ATTRIBUTION REPORTING API
     */
    private val measurementManager: MeasurementManager
    // Use your own register source URL
    private val registerSourceUrl = "https://$topLevelDomain/attribution/source"
    private val registerSourceIdentifier = "?attribution_id="

    init {
        // Check for Privacy Sandbox version
        if (!canUsePrivacySandbox(context)) {
            // If the version is too low, can not initialize the customAudienceManager
            // and measurementManager
            throw IllegalStateException("Can not use Privacy Sandbox, version too low")
        }

        adSelectionManager = context.getSystemService(
            AdSelectionManager::class.java
        )

        measurementManager = context.getSystemService(
            MeasurementManager::class.java
        )

        adSelectionConfig = AdSelectionConfig.Builder()
            .setSeller(AdTechIdentifier.fromString(topLevelDomain))
            .setCustomAudienceBuyers(buyers)
            .setDecisionLogicUri(scoringLogicUri)
            .setAdSelectionSignals(AdSelectionSignals.EMPTY)
            .setPerBuyerSignals(
                buyers.stream().collect(Collectors.toMap(
                    { buyer: AdTechIdentifier -> buyer },
                    { AdSelectionSignals.EMPTY }
                )))
            .setTrustedScoringSignalsUri(scoringTrustedUri)
            .build()
    }

    /*
    * Returns whether the device and AdServices Extension and Google Play Services versions
    * are at the minimum level or higher to use the Privacy Sandbox.
    */
    fun canUsePrivacySandbox(context: Context) : Boolean {
        // Only needed for Beta releases
        val isCorrectBuildVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        val isCorrectSdkExtensionVersion =
            SdkExtensions.getExtensionVersion(SdkExtensions.AD_SERVICES) >= 4

        // Needed for both Developer Preview and Beta releases
        val isGooglePlayServicesAvailable = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        return isCorrectBuildVersion && isCorrectSdkExtensionVersion && isGooglePlayServicesAvailable
    }

    fun runAdSelection() : ListenableFuture<AdSelectionOutcome?> {
        val executor: Executor = Executors.newCachedThreadPool()
        return CallbackToFutureAdapter.getFuture { completer: CallbackToFutureAdapter.Completer<AdSelectionOutcome?> ->
            try {
                adSelectionManager.selectAds(
                    adSelectionConfig,
                    executor,
                    object : OutcomeReceiver<AdSelectionOutcome, Exception> {
                        override fun onResult(result: AdSelectionOutcome) {
                            Log.i(logTag, "Ad selection succeeded")
                            completer.set(
                                AdSelectionOutcome.Builder()
                                    .setAdSelectionId(result.adSelectionId)
                                    .setRenderUri(result.renderUri)
                                    .build()
                            )
                        }

                        override fun onError(error: Exception) {
                            Log.e(logTag, "Error when running ad selection", error)

                            // Return null to indicate no ad was selected.
                            // To be replaced with contextual ad in the future
                            completer.set(null)
                        }
                    }
                )
            } catch (e: IllegalStateException) {
                Log.e(logTag, "Error when running ad selection", e)
                completer.set(null)
            }
        }
    }

    fun reportImpression(adSelectionId: Long) : ListenableFuture<Any?> {
        val executor: Executor = Executors.newCachedThreadPool()
        val reportImpressionRequest = ReportImpressionRequest(adSelectionId, adSelectionConfig)
        return CallbackToFutureAdapter.getFuture { completer: CallbackToFutureAdapter.Completer<Any?> ->
            try {
                adSelectionManager.reportImpression(
                    reportImpressionRequest,
                    executor,
                    object : OutcomeReceiver<Any, Exception> {
                        override fun onResult(result: Any) {
                            Log.i(logTag, "Report impression succeeded")
                            completer.set(
                                Object()
                            )
                        }

                        override fun onError(error: Exception) {
                            Log.e(logTag, "Error during impression reporting", error)

                            // Return null to indicate no impression was reported.
                            completer.set(null)
                        }
                    }
                )
            } catch (e: IllegalStateException) {
                Log.e(logTag, "Error when reporting impression", e)
                completer.set(null)
            }
        }
    }

    fun reportEvent(adSelectionId: Long, key: String, data: String) : ListenableFuture<Any?> {
        val executor: Executor = Executors.newCachedThreadPool()
        val reportingDestinations =
            FLAG_REPORTING_DESTINATION_BUYER or FLAG_REPORTING_DESTINATION_SELLER
        val reportEventRequest = ReportEventRequest.Builder(
            adSelectionId,
            key,
            data,
            reportingDestinations
        ).build()
        return CallbackToFutureAdapter.getFuture { completer: CallbackToFutureAdapter.Completer<Any?> ->
            try {
                adSelectionManager.reportEvent(
                    reportEventRequest,
                    executor,
                    object : OutcomeReceiver<Any, Exception> {
                        override fun onResult(result: Any) {
                            Log.i(logTag, "Report event succeeded")
                            completer.set(
                                Object()
                            )
                        }

                        override fun onError(error: Exception) {
                            Log.e(logTag, "Error during event reporting", error)

                            // Return null to indicate no event was reported.
                            completer.set(null)
                        }
                    }
                )
            } catch (e: IllegalStateException) {
                Log.e(logTag, "Error when reporting event", e)
                completer.set(null)
            }
        }
    }

    /*
     * Registers a source.
     */
    fun registerSource(identifier: String, inputEvent: MotionEvent) : ListenableFuture<String> {
        val executor = Executors.newCachedThreadPool()
        var registerSourceUri = Uri.parse("$registerSourceUrl$registerSourceIdentifier$identifier")
        if (inputEvent.action == MotionEvent.ACTION_DOWN) {
            registerSourceUri = registerSourceUri.buildUpon().appendQueryParameter("type", "click").build()
        }

        Log.d(logTag, "registerSource called")
        Log.d(logTag, "registerSource URL is = $registerSourceUri")

        val successMsg = "registerSource succeeded"
        val failMsg = "registerSource failed"

        return CallbackToFutureAdapter.getFuture { completer: CallbackToFutureAdapter.Completer<String> ->
            try {
                measurementManager.registerSource(
                    registerSourceUri,
                    inputEvent,
                    executor,
                    object : OutcomeReceiver<Any, Exception> {
                        override fun onResult(result: Any) {
                            Log.i(logTag, successMsg)
                            completer.set(successMsg)
                        }

                        override fun onError(error: Exception) {
                            Log.e(logTag, failMsg, error)
                            completer.setException(error)
                        }
                    })
            } catch (e: Exception) {
                Log.e(logTag, failMsg, e)
            }
        }
    }
}