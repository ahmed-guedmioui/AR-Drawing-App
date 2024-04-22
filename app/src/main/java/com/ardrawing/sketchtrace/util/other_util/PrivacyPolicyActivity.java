package com.ardrawing.sketchtrace.util.other_util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ardrawing.sketchtrace.R;


public class PrivacyPolicyActivity extends Activity {


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AppConstants.overridePendingTransitionEnter(this);
        requestWindowFeature(1);
        setContentView(R.layout.activity_privacy_policy);


        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("ff");
    }



    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }

}
