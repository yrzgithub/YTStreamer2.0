package com.example.ytstreamer40;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class WebBrowserAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);

        Intent intent = getIntent();

        WebView web = findViewById(R.id.web);
        web.loadUrl(intent.getStringExtra("url"));
    }
}