package com.google.android.libraries.social.licenses;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.libraries.social.licenses.LicenseMenuFragment.LicenseSelectionListener;
import com.mp.android.apps.R;

import java.util.ArrayList;

public final class LicenseMenuActivity extends AppCompatActivity implements LicenseSelectionListener {
    static final String ARGS_LICENSE = "license";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.libraries_social_licenses_license_menu_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        FragmentManager fm = getSupportFragmentManager();
        if (!(fm.findFragmentById(R.id.license_menu_fragment_container) instanceof LicenseMenuFragment)) {
            Fragment licenseMenuFragment = new LicenseMenuFragment();
            if (getIntent().hasExtra("pluginLicensePaths")) {
                licenseMenuFragment.setArguments(getIntent().getBundleExtra("pluginLicensePaths"));
            }
            fm.beginTransaction().add(R.id.license_menu_fragment_container, licenseMenuFragment).commitNow();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    public void onLicenseSelected(License license) {
        Intent licenseIntent = new Intent(this, LicenseActivity.class);
        licenseIntent.putExtra(ARGS_LICENSE, license);
        startActivity(licenseIntent);
    }

    public static Intent createIntentWithExtraPluginPaths(Context context, ArrayList<String> extraPluginPaths) {
        Intent licenseMenuIntent = new Intent(context, LicenseMenuActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("pluginLicensePaths", extraPluginPaths);
        licenseMenuIntent.putExtra("pluginLicensePaths", bundle);
        return licenseMenuIntent;
    }
}
