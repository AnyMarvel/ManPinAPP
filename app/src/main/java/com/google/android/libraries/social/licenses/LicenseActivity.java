package com.google.android.libraries.social.licenses;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mp.android.apps.R;
public final class LicenseActivity extends AppCompatActivity {
    private static final String STATE_SCROLL_POS = "scroll_pos";
    private static final String TAG = "LicenseActivity";

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.libraries_social_licenses_license_activity);
        License license = (License) getIntent().getParcelableExtra("license");
        getSupportActionBar().setTitle(license.getLibraryName());
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(null);
        TextView textView = (TextView) findViewById(R.id.license_activity_textview);
        String licenseText = Licenses.getLicenseText(this, license);
        if (licenseText == null) {
            finish();
        } else {
            textView.setText(licenseText);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView textView = (TextView) findViewById(R.id.license_activity_textview);
        outState.putInt(STATE_SCROLL_POS, textView.getLayout().getLineStart(textView.getLayout().getLineForVertical(((ScrollView) findViewById(R.id.license_activity_scrollview)).getScrollY())));
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final ScrollView scrollView = (ScrollView) findViewById(R.id.license_activity_scrollview);
        final int firstVisibleChar = savedInstanceState.getInt(STATE_SCROLL_POS);
        scrollView.post(new Runnable() {
            public void run() {
                TextView textView = (TextView) LicenseActivity.this.findViewById(R.id.license_activity_textview);
                scrollView.scrollTo(0, textView.getLayout().getLineTop(textView.getLayout().getLineForOffset(firstVisibleChar)));
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }
}
