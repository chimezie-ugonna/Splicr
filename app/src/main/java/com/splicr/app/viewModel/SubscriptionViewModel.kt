package com.splicr.app.viewModel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.splicr.app.utils.MediaConfigurationUtil.formatTimestamp

class SubscriptionViewModel(application: Application) : AndroidViewModel(application),
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
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry logic can go here
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
                // Separate product details by product ID
                productDetailsList.forEach { productDetails ->
                    when (productDetails.productId) {
                        "monthly_premium" -> monthlyProductDetails.postValue(productDetails)
                        "yearly_premium" -> yearlyProductDetails.postValue(productDetails)
                    }
                }
            }
        }
    }

    fun startSubscriptionPurchase(
        activity: Activity, productDetails: ProductDetails, basePlanId: String
    ) {
        val offerDetails = productDetails.subscriptionOfferDetails?.find {
            it.basePlanId == basePlanId
        }

        offerDetails?.let {
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails).setOfferToken(it.offerToken).build()

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams)).build()

            val result = billingClient.launchBillingFlow(activity, billingFlowParams)
            // Handle result if needed
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
                purchaseResult.postValue(Result.success(Unit))
            }
        } else {
            // Handle failure or user cancellation
            purchaseResult.postValue(Result.failure(Exception("Purchase failed: ${billingResult.debugMessage}")))
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val productId = purchase.products.firstOrNull()
            val isSubscribed = purchase.isAutoRenewing

            val subscriptionStatus2 = when (productId) {
                "monthly_premium" -> {
                    if (isSubscribed) SubscriptionStatus.MONTHLY_SUBSCRIPTION else SubscriptionStatus.NONE
                }

                "yearly_premium" -> {
                    if (isSubscribed) SubscriptionStatus.YEARLY_SUBSCRIPTION else SubscriptionStatus.NONE
                }

                else -> SubscriptionStatus.NONE
            }
            subscriptionStatus.postValue(subscriptionStatus2)

            // Check if the purchase includes a free trial
            val productDetails = productId?.let { it ->
                billingClient.queryProductDetailsAsync(
                    QueryProductDetailsParams.newBuilder().setProductList(
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder().setProductId(it)
                                .setProductType(BillingClient.ProductType.SUBS).build()
                        )
                    ).build()
                ) { _, productDetailsList ->
                    val productDetail = productDetailsList.firstOrNull { it.productId == productId }
                    val offerDetails = productDetail?.subscriptionOfferDetails?.firstOrNull()

                    val hasFreeTrial =
                        offerDetails?.offerTags?.contains("monthly-free-trial") == true || offerDetails?.offerTags?.contains(
                            "yearly-free-trial"
                        ) == true

                    when {
                        productId == "monthly_premium" && hasFreeTrial -> {
                            subscriptionStatus.postValue(SubscriptionStatus.MONTHLY_FREE_TRIAL)
                        }

                        productId == "yearly_premium" && hasFreeTrial -> {
                            subscriptionStatus.postValue(SubscriptionStatus.YEARLY_FREE_TRIAL)
                        }

                        else -> {
                            // Handle case where there is no free trial
                            subscriptionStatus.postValue(subscriptionStatus2)
                        }
                    }
                }
            }

            renewalDate.postValue(formatTimestamp(purchase.purchaseTime))
        }
    }

    fun restorePurchases() {
        val queryPurchaseParams =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()

        billingClient.queryPurchasesAsync(queryPurchaseParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases.isNotEmpty()) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
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
        // Implement error handling based on response codes
    }

    override fun onCleared() {
        super.onCleared()
        billingClient.endConnection()
    }
}

enum class SubscriptionStatus {
    NONE,                      // No active subscription
    MONTHLY_SUBSCRIPTION,      // Monthly subscription
    YEARLY_SUBSCRIPTION,       // Yearly subscription
    MONTHLY_FREE_TRIAL,        // Monthly subscription with free trial
    YEARLY_FREE_TRIAL          // Yearly subscription with free trial
}