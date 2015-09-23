package io.kazak.settings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import io.kazak.KazakActivity;
import io.kazak.R;

public class SettingsActivity extends KazakActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class InfoPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_version_info);
        }

    }

}
