package com.hexforhn.hex.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import com.hexforhn.hex.R;
import com.hexforhn.hex.util.ThemeHelper;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_about);
        setupToolbar();

        TextView aboutText = (TextView) findViewById(R.id.aboutText);
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());

        TextView feedbackText = (TextView) findViewById(R.id.feedbackText);
        feedbackText.setMovementMethod(LinkMovementMethod.getInstance());

        TextView contributingText = (TextView) findViewById(R.id.contributingText);
        contributingText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onResume() {
        super.onResume();
        ThemeHelper.updateTheme(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.aboutTitle);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
