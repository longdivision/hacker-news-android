package com.hexforhn.hex.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.hexforhn.hex.R;
import com.hexforhn.hex.fragment.SettingsFragment;
import com.hexforhn.hex.util.ThemeHelper;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_settings);

        setupToolbar();

        getFragmentManager().beginTransaction()
                .replace(R.id.settingsContent, new SettingsFragment())
                .commit();
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
            actionBar.setTitle(R.string.settingsTitle);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
