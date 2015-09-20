package io.kazak.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import io.kazak.R;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.appbar);
        setupAppBar();
    }

    private void setupAppBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigateToParent();
                    }
                }
        );
    }

    private void navigateToParent() {
        // TODO use navigator
        Intent intent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, intent)) {
            TaskStackBuilder.create(this)
                    .addParentStack(this)
                    .startActivities();
        } else {
            NavUtils.navigateUpTo(this, intent);
        }
    }

    public static class InfoPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_version_info);
        }

    }

}
