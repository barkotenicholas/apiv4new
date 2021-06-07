package recipes.bestreipes.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    private static final List<String> LIST_OF_SKUS = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add("mango");
                add("mine");
                add("pineapple");
            }});
    public Button button;
    public BillingClient billingClient;
    public MutableLiveData<Map<String, SkuDetails>> skusWithSkuDetails = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpBilling();

        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
            }
        });
    }

    private void setUpBilling() {

        billingClient = BillingClient.newBuilder(MainActivity.this)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        if (!billingClient.isReady()) {
            Log.d(TAG, "BillingClient: Start connection...");
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingServiceDisconnected() {
                    Log.d(TAG, "onBillingServiceDisconnected");

                }

                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    int responseCode = billingResult.getResponseCode();
                    String debugMessage = billingResult.getDebugMessage();
                    Log.d(TAG, "onBillingSetupFinished: " + responseCode + " " + debugMessage);
                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        // The billing client is ready. You can query purchases here.
                        querySkuDetails();
                        queryPurchase();
                    }
                }
            });
        }
    }

    private void queryPurchase() {
        if (!billingClient.isReady()) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready");
        }
        Log.d(TAG, "queryPurchases: SUBS");
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                processPurchases(list);

            }
        });
    }

    private void processPurchases(List<Purchase> list) {


    }
    public void subscribe(){
        if(billingClient.isReady()){
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(LIST_OF_SKUS)
                    .setType(BillingClient.SkuType.SUBS)
                    .build();

            Log.d(TAG, "subscribe:  subscribed");

            billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                    Log.d(TAG, "onSkuDetailsResponse: Begin sss");
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                        Log.d(TAG, "onSkuDetailsResponse: everything is ok");

                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(list.get(0))
                                .build();
                        Log.d(TAG, "onSkuDetailsResponse: start billing");

                        int response = billingClient.launchBillingFlow(MainActivity.this,billingFlowParams)
                                .getResponseCode();
                        switch (response)
                        {
                            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                                Toast.makeText(MainActivity.this,"Billing unavilable ",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                                Toast.makeText(MainActivity.this,"item unavilable ",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                                Toast.makeText(MainActivity.this,"Developer Error  ",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                                Toast.makeText(MainActivity.this,"Feature not supported",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                                Toast.makeText(MainActivity.this,"ITEM OWNED  ",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                                Toast.makeText(MainActivity.this,"SERVICE ISCONNTETD ",Toast.LENGTH_SHORT).show();
                                break;
                            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                                Toast.makeText(MainActivity.this,"TIME OUT",Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;

                        }

                    }
                }
            });
        }

    }
    public int launchBillingFlow(Activity activity, BillingFlowParams params) {
        if (!billingClient.isReady()) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready");
        }
        BillingResult billingResult = billingClient.launchBillingFlow(activity, params);
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        Log.d(TAG, "launchBillingFlow: BillingResponse " + responseCode + " " + debugMessage);
        return responseCode;
    }

    private void querySkuDetails() {
        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.SUBS)
                .setSkusList(LIST_OF_SKUS)
                .build();
        Log.i(TAG, "querySkuDetailsAsync");

        billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                if (billingResult == null) {
                    Log.wtf(TAG, "onSkuDetailsResponse: null BillingResult");
                }

                int responseCode = billingResult.getResponseCode();
                String debugMessage = billingResult.getDebugMessage();

                switch (responseCode) {
                    case BillingClient.BillingResponseCode.OK:
                        Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        final int expectedSkuDetailsCount = LIST_OF_SKUS.size();
                        if (list == null) {
                            skusWithSkuDetails.postValue(Collections.<String, SkuDetails>emptyMap());

                            Log.e(TAG, "onSkuDetailsResponse: " +
                                    "Expected " + expectedSkuDetailsCount + ", " +
                                    "Found null SkuDetails. " +
                                    "Check to see if the SKUs you requested are correctly published " +
                                    "in the Google Play Console.");
                        } else {
                            Map<String, SkuDetails> newSkusDetailList = new HashMap<String, SkuDetails>();
                            for (SkuDetails skuDetails : list) {
                                newSkusDetailList.put(skuDetails.getSku(), skuDetails);
                            }
                            skusWithSkuDetails.postValue(newSkusDetailList);
                            int skuDetailsCount = newSkusDetailList.size();
                            if (skuDetailsCount == expectedSkuDetailsCount) {
                                Log.i(TAG, "onSkuDetailsResponse: Found " + skuDetailsCount + " SkuDetails");
                            } else {
                                Log.e(TAG, "onSkuDetailsResponse: " +
                                        "Expected " + expectedSkuDetailsCount + ", " +
                                        "Found " + skuDetailsCount + " SkuDetails. " +
                                        "Check to see if the SKUs you requested are correctly published " +
                                        "in the Google Play Console.");
                            }
                        }
                        break;
                    case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                    case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                    case BillingClient.BillingResponseCode.ERROR:
                        Log.e(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    case BillingClient.BillingResponseCode.USER_CANCELED:
                        Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    // These response codes are not expected.
                    case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                    case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                    case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                    default:
                        Log.wtf(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                }
            }
        });

    }

    @Override
    public void onPurchasesUpdated(@NonNull  BillingResult billingResult, @Nullable  List<Purchase> list) {

    }

    private boolean isUnchangedPurchaseList(List<Purchase> purchasesList) {
        // TODO: Optimize to avoid updates with identical data.
        return false;
    }
}