package com.splicr.app.viewModel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.splicr.app.R
import com.splicr.app.utils.MediaConfigurationUtil.formatTimestamp
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SubscriptionViewModel(private val application: Application) : AndroidViewModel(application),
    PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    val subscriptionStatus = MutableLiveData<SubscriptionStatus>()
    private val renewalDate = MutableLiveData<String?>()
    val purchaseResult = MutableLiveData<Result<Unit>>()
    val monthlyProductDetails = MutableLiveData<ProductDetails?>()
    val yearlyProductDetails = MutableLiveData<ProductDetails?>()

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient =
            BillingClient.newBuilder(getApplication()).setListener(this).enablePendingPurchases()
                .build()
        startBillingConnection()
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                } else {
                    purchaseResult.postValue(Result.failure(Exception("Billing Setup Failed")))
                }
            }

            override fun onBillingServiceDisconnected() {
                purchaseResult.postValue(Result.failure(Exception("Billing Service Disconnected")))
            }
        })
    }

    private fun queryProductDetails() {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(
            listOf(
                QueryProductDetailsParams.Product.newBuilder().setProductId("monthly_premium")
                    .setProductType(BillingClient.ProductType.SUBS).build(),
                QueryProductDetailsParams.Product.newBuilder().setProductId("yearly_premium")
                    .setProductType(BillingClient.ProductType.SUBS).build()
            )
        ).build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach { productDetails ->
                    when (productDetails.productId) {
                        "monthly_premium" -> monthlyProductDetails.postValue(productDetails)
                        "yearly_premium" -> yearlyProductDetails.postValue(productDetails)
                    }
                }
            } else {
                monthlyProductDetails.postValue(null)
                yearlyProductDetails.postValue(null)
            }
        }
    }

    fun startSubscriptionPurchase(
        activity: Activity, productDetails: ProductDetails, basePlanId: String
    ): Result<Unit> {
        val offerDetails = productDetails.subscriptionOfferDetails?.find {
            it.basePlanId == basePlanId
        }

        return if (offerDetails != null) {
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails).setOfferToken(offerDetails.offerToken).build()

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams)).build()

            val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        activity.getString(
                            R.string.purchase_failed, billingResult.debugMessage
                        )
                    )
                )
            }
        } else {
            Result.failure(
                Exception(
                    activity.getString(
                        R.string.offer_details_not_found_for_baseplanid, basePlanId
                    )
                )
            )
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                    purchaseResult.postValue(Result.success(Unit))
                } else {
                    purchaseResult.postValue(
                        Result.failure(
                            Exception(
                                application.applicationContext.getString(
                                    R.string.no_purchases_found
                                )
                            )
                        )
                    )
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {

            }

            else -> {
                purchaseResult.postValue(Result.failure(Exception(billingResult.debugMessage.ifEmpty {
                    application.applicationContext.getString(
                        R.string.an_unknown_error_occurred
                    )
                })))
            }
        }
    }

    private fun handlePurchase(purchase: Purchase): Result<Unit> {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val productId = purchase.products.firstOrNull()
            val isSubscribed = purchase.isAutoRenewing

            return try {
                // Check if the purchase is acknowledged before proceeding
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken).build()
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            updateSubscriptionStatus(productId, isSubscribed, purchase.purchaseTime)
                        } else {
                            throw Exception("Failed to acknowledge purchase: ${billingResult.debugMessage}")
                        }
                    }
                } else {
                    updateSubscriptionStatus(productId, isSubscribed, purchase.purchaseTime)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            return Result.failure(Exception("Purchase state is not PURCHASED"))
        }
    }

    private fun updateSubscriptionStatus(
        productId: String?, isSubscribed: Boolean, purchaseTime: Long
    ) {
        val subscriptionStatus2 = when (productId) {
            "monthly_premium" -> {
                if (isSubscribed) SubscriptionStatus.MONTHLY_SUBSCRIPTION else SubscriptionStatus.NONE
            }

            "yearly_premium" -> {
                if (isSubscribed) SubscriptionStatus.YEARLY_SUBSCRIPTION else SubscriptionStatus.NONE
            }

            else -> SubscriptionStatus.NONE
        }

        // Query for offer details (to check if there's a free trial)
        productId?.let {
            billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder().setProductId(it)
                            .setProductType(BillingClient.ProductType.SUBS).build()
                    )
                ).build()
            ) { _, productDetailsList ->
                val productDetail =
                    productDetailsList.firstOrNull { productDetails -> productDetails.productId == productId }
                val hasFreeTrial = productDetail?.subscriptionOfferDetails?.any { offer ->
                    offer.offerTags.contains("monthly-free-trial") || offer.offerTags.contains("yearly-free-trial")
                } ?: false

                if (hasFreeTrial) {
                    val freeTrialStatus = if (productId == "monthly_premium") {
                        SubscriptionStatus.MONTHLY_FREE_TRIAL
                    } else {
                        SubscriptionStatus.YEARLY_FREE_TRIAL
                    }
                    subscriptionStatus.postValue(freeTrialStatus)
                } else {
                    subscriptionStatus.postValue(subscriptionStatus2)
                }
            }
        }

        // Update renewal date
        renewalDate.postValue(formatTimestamp(purchaseTime))
    }

    suspend fun restorePurchases(context: Context): Result<Unit> {
        return suspendCoroutine { continuation ->
            val purchaseParams =
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build()

            billingClient.queryPurchasesAsync(purchaseParams) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (purchases.isEmpty()) {
                        subscriptionStatus.postValue(SubscriptionStatus.NONE)
                        continuation.resume(Result.failure(Exception(context.getString(R.string.no_purchases_found))))
                    } else {
                        try {
                            for (purchase in purchases) {
                                val result = handlePurchase(purchase)
                                if (result.isFailure) {
                                    throw result.exceptionOrNull()
                                        ?: Exception(context.getString(R.string.failed_to_handle_purchase))
                                }
                            }
                            continuation.resume(Result.success(Unit))
                        } catch (e: Exception) {
                            continuation.resume(Result.failure(e))
                        }
                    }
                } else {
                    continuation.resume(
                        Result.failure(Exception(context.getString(R.string.failed_to_restore_purchases)))
                    )
                }
            }
        }
    }

    fun cancelSubscription(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/account/subscriptions")
            setPackage("com.android.vending")
        }
        activity.startActivity(intent)
    }

    fun handleBillingError(responseCode: Int) {
        purchaseResult.postValue(Result.failure(Exception("Billing Error: $responseCode")))
    }

    override fun onCleared() {
        super.onCleared()
        billingClient.endConnection()
    }
}

enum class SubscriptionStatus {
    NONE, MONTHLY_SUBSCRIPTION, YEARLY_SUBSCRIPTION, MONTHLY_FREE_TRIAL, YEARLY_FREE_TRIAL
}