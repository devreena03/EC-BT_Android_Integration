package bt.paypal.com.ecbt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PayPalRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.models.PostalAddress;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements PaymentMethodNonceCreatedListener {

    private String clientToken="";
    private String nonce;
    private BraintreeFragment mBraintreeFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("enter  ","onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://bt-direct.herokuapp.com/payments/client_token", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                Log.i("DONE clientToken",response);
                clientToken = response;
            }
        });
        Log.e("exit  ","onCreate");
    }
    public void payNow(View v){
        Log.e("enter  ","payNow");
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, clientToken);
            Log.i("mBraintreeFragment", "done");
        } catch (InvalidArgumentException e) {
            // There was an issue with your authorization string.
        }
        setupBraintreeAndStartExpressCheckout();
        final TextView tvOut = (TextView) findViewById(R.id.textView);
        tvOut.setText("pay success");
        Log.e("exit  ","payNow");
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        Log.e("enter  ","onPaymentMethodNonceCreated");
        // Send nonce to server
        String nonce = paymentMethodNonce.getNonce();
        if (paymentMethodNonce instanceof PayPalAccountNonce) {
            PayPalAccountNonce payPalAccountNonce = (PayPalAccountNonce)paymentMethodNonce;

            // Access additional information
            String email = payPalAccountNonce.getEmail();
            String firstName = payPalAccountNonce.getFirstName();
            String lastName = payPalAccountNonce.getLastName();
            String phone = payPalAccountNonce.getPhone();

            // See PostalAddress.java for details
            PostalAddress billingAddress = payPalAccountNonce.getBillingAddress();
            PostalAddress shippingAddress = payPalAccountNonce.getShippingAddress();
        }
        postNonceToServer(nonce);
        Log.e("exit  ","onPaymentMethodNonceCreated");
    }

    private void postNonceToServer(String nonce) {
        Log.e("enter  ","postNonceToServer");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("nonce", nonce);
        params.put("amount", "20");
        Log.e("nonce  ",nonce);
        client.post("https://bt-direct.herokuapp.com/payments/checkout", params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String body = new String(responseBody);
                        Log.d("payment  ",body);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e("checkout  ",error.getMessage());
                    }
                    // Your implementation here
                }
        );
        Log.e("exit  ","postNonceToServer");
    }

    private void setupBraintreeAndStartExpressCheckout() {
        Log.e("enter  ","setupBraintreeAndStartExpressCheckout");
        PostalAddress add = new PostalAddress();
        add.recipientName("S2S store");
        add.streetAddress("test street");
        add.locality("city");
        add.region("state");
        add.postalCode("600044");
        add.countryCodeAlpha2("IN");


        PayPalRequest request = new PayPalRequest("20")
                .currencyCode("INR")
                .intent(PayPalRequest.INTENT_SALE)
                .userAction(PayPalRequest.USER_ACTION_COMMIT)
                .shippingAddressRequired(true)
                .shippingAddressOverride(add);
        PayPal.requestOneTimePayment(mBraintreeFragment, request);
        Log.e("exit  ","setupBraintreeAndStartExpressCheckout");
    }

}
