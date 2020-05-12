package com.google.android.libraries.social.licenses;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import com.mp.android.apps.R;
public final class LicenseMenuFragment extends Fragment implements LoaderCallbacks<List<License>> {
    static final String ARGS_EXTRA_PLUGIN_PATHS = "pluginLicensePaths";
    private static final int LOADER_ID = 54321;
    private LicenseSelectionListener licenseSelectionListener;
    private ArrayAdapter<License> listAdapter;

    public interface LicenseSelectionListener {
        void onLicenseSelected(License license);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof LicenseSelectionListener) {
            this.licenseSelectionListener = (LicenseSelectionListener) parentFragment;
            return;
        }
        FragmentActivity parentActivity = getActivity();
        if (parentActivity instanceof LicenseSelectionListener) {
            this.licenseSelectionListener = (LicenseSelectionListener) parentActivity;
        }
    }

    public void onDetach() {
        super.onDetach();
        this.licenseSelectionListener = null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.libraries_social_licenses_license_menu_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity parentActivity = getActivity();
        this.listAdapter = new ArrayAdapter(parentActivity, R.layout.libraries_social_licenses_license, R.id.license, new ArrayList());
        parentActivity.getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        ListView listView = (ListView) view.findViewById(R.id.license_list);
        listView.setAdapter(this.listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                License license = (License) parent.getItemAtPosition(position);
                if (licenseSelectionListener != null) {
                    licenseSelectionListener.onLicenseSelected(license);
                }
            }
        });
    }


    public void onDestroy() {
        super.onDestroy();
        getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID);
    }

    public Loader<List<License>> onCreateLoader(int id, Bundle args) {
        Bundle arguments = getArguments();
        if (arguments == null || arguments.getStringArrayList(ARGS_EXTRA_PLUGIN_PATHS) == null || arguments.getStringArrayList(ARGS_EXTRA_PLUGIN_PATHS).isEmpty()) {
            return new LicenseLoader(getActivity());
        }
        return new LicenseLoader(getActivity(), arguments.getStringArrayList(ARGS_EXTRA_PLUGIN_PATHS));
    }

    public void onLoadFinished(Loader<List<License>> loader, List<License> licenses) {
        this.listAdapter.clear();
        this.listAdapter.addAll(licenses);
        this.listAdapter.notifyDataSetChanged();
    }

    public void onLoaderReset(Loader<List<License>> loader) {
        this.listAdapter.clear();
        this.listAdapter.notifyDataSetChanged();
    }
}
