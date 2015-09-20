package io.kazak.talk;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import io.kazak.BuildConfig;
import io.kazak.KazakActivity;

import static io.kazak.base.BasePackage.safeTrim;

public class TalkDetailsActivity extends KazakActivity {

    public static final String EXTRA_TALK_ID = BuildConfig.APPLICATION_ID + ".extra.TALK_ID";

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        String talkId = extractTalkIdFrom(getIntent());
        updateWith(talkId);
    }

    private String extractTalkIdFrom(Intent intent) {
        return safeTrim(intent.getStringExtra(EXTRA_TALK_ID));
    }

    private void updateWith(String talkId) {
        // TODO bind data
    }

}
