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

package com.example.mmpsdk

import android.adservices.common.AdData
import android.adservices.common.AdSelectionSignals
import android.adservices.common.AdTechIdentifier
import android.adservices.customaudience.CustomAudience
import android.adservices.customaudience.CustomAudienceManager
import android.adservices.customaudience.JoinCustomAudienceRequest
import android.adservices.customaudience.TrustedBiddingData
import android.adservices.measurement.MeasurementManager
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.OutcomeReceiver
import android.os.ext.SdkExtensions
import android.util.Log
import java.util.concurrent.Executors;
import com.google.common.util.concurrent.ListenableFuture
import androidx.concurrent.futures.CallbackToFutureAdapter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.json.JSONObject
import java.lang.IllegalStateException
import java.time.Duration
import java.time.Instant

/*
 * Implementation of an MMP SDK. This SDK will join custom audiences on behalf of a DSP.
 */
@SuppressLint("NewApi")
class MmpSdkImpl constructor(
    context: Context
  ) {
    /*
     * SHARED
     */
    private val logTag = "Flight MMP"
    private val topLevelDomain = "example.com"

    /*
     * PROTECTED AUDIENCES
     */
    private val customAudienceManager: CustomAudienceManager
    private val locationsWithAds = listOf("athens", "berlin", "cairo")
    // Use your own bidding logic, bidding daily, and bidding trusted URLs
    private val biddingLogicUri = Uri.parse("https://$topLevelDomain/protected-audience/Logic/BiddingLogic.js")
    private val biddingDailyUri = Uri.parse("https://$topLevelDomain/protected-audience/Functions/BiddingDaily.html")
    private val biddingTrustedUri = Uri.parse("https://$topLevelDomain/protected-audience/Functions/BiddingTrusted.js")

    /*
     * ATTRIBUTION REPORTING
     */
    private val measurementManager: MeasurementManager
    // Use your own register trigger URL
    private val registerTriggerUrl = "https://$topLevelDomain/attribution/trigger"
    private val registerTriggerIdentifier = "?attribution_id="

    init {
        // Check for Privacy Sandbox version
        if (!canUsePrivacySandbox(context)) {
            // If the version is too low, can not initialize the customAudienceManager
            // and measurementManager
            throw IllegalStateException("Can not use Privacy Sandbox, version too low")
        }

        customAudienceManager = context.getSystemService(
            CustomAudienceManager::class.java
        )

        measurementManager = context.getSystemService(
            MeasurementManager::class.java
        )
    }

    /*
    * Returns whether the device, AdServices Extension versions, and Google Play Services
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

    /*
     * Joins a test custom audience using hard coded values. To be replaced by reading values
     * from DSP "server."
     */
    fun joinCustomAudience(customAudienceName: String) {
        joinCustomAudience(
            customAudienceName,
            topLevelDomain,
            biddingLogicUri,
            biddingDailyUri,
            biddingTrustedUri,
            Instant.now(),
            Instant.now().plus(Duration.ofDays(7))
        )
    }

    /*
     * Joins a custom audience.
     */

    fun joinCustomAudience(
        name: String,
        buyer: String,
        biddingLogicUri: Uri,
        dailyUpdateUri: Uri,
        trustedBiddingUri: Uri,
        activationTime: Instant,
        expiry: Instant
    ) {
        val customAudience = CustomAudience.Builder()
            .setName(name)
            .setBuyer(AdTechIdentifier.fromString(buyer))
            .setDailyUpdateUri(dailyUpdateUri)
            .setBiddingLogicUri(biddingLogicUri)
            .setActivationTime(activationTime)
            .setExpirationTime(expiry)
            .setTrustedBiddingData(TrustedBiddingData.Builder()
                .setTrustedBiddingKeys(listOf("\"valid_signals\": true"))
                .setTrustedBiddingUri(trustedBiddingUri)
                .build())
            .setAds(
                listOf(
                    AdData.Builder()
                        .setRenderUri(getRenderUriForAudience(name))
                        .setMetadata(JSONObject().toString())
                        .build()
                )
            )
            .setUserBiddingSignals(AdSelectionSignals.EMPTY)
            .build()

        try {
            joinCustomAudience(customAudience)
            Log.i(logTag, "Successfully joined custom audience: $name")
        } catch (e: Exception) {
            Log.e(logTag, "joinCustomAudience exception: ", e)
        }
    }

    private fun joinCustomAudience(customAudience: CustomAudience?) : ListenableFuture<Void?> {
        val executor = Executors.newCachedThreadPool()
        return CallbackToFutureAdapter.getFuture { completer: CallbackToFutureAdapter.Completer<Void?> ->
            val request = JoinCustomAudienceRequest.Builder()
                .setCustomAudience(customAudience!!)
                .build()

            customAudienceManager.joinCustomAudience(
                request,
                executor,
                object : NullableOutcomeReceiver<Any, java.lang.Exception?> {
                    override fun onResult(result: Any) {
                        completer.set(null)
                    }

                    override fun onError(error: java.lang.Exception?) {
                        Log.e(logTag, "joinCustomAudience exception: ", error)
                        completer.setException(error!!)
                    }
                })
            "joinCustomAudience"
        }
    }

    // Lazily compute the render URI for an audience. We have a limited
    // number of specific "ads", so will fall back to generic ad if the destination
    // does not have a specific ad.
    private fun getRenderUriForAudience(name: String) : Uri {
        var renderParam = name
        if (!locationsWithAds.contains(name)) {
            renderParam = "generic"
        }
        return Uri.parse("https://$topLevelDomain/render/${renderParam}.jpg")
    }

    /*
     * Registers a trigger.
     */
    fun registerTrigger(identifier: String) : ListenableFuture<String> {
        val executor = Executors.newCachedThreadPool()
        val registerTriggerUri = Uri.parse("$registerTriggerUrl$registerTriggerIdentifier$identifier")

        Log.d(logTag, "registerTrigger called")
        Log.d(logTag, "registerTrigger URL is = $registerTriggerUri")

        val successMsg = "registerTrigger succeeded"
        val failMsg = "registerTrigger failed"

        return CallbackToFutureAdapter.getFuture {
                completer: CallbackToFutureAdapter.Completer<String> ->
                    try {
                        measurementManager.registerTrigger(
                            registerTriggerUri,
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