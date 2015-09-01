/**
 * Copyright (c) 2015, Facebook, Inc.  All rights reserved.
 *
 * Facebook, Inc. (“Facebook”) owns all right, title and interest, including all intellectual
 * property and other proprietary rights, in and to its contributions to the Droidcon App software
 * (the “Contributions”). Subject to your compliance with these terms, you are hereby granted a
 * non-exclusive, worldwide, royalty-free copyright license to (1) use and copy the Contributions;
 * (2) reproduce and distribute the Contributions as part of your own software (“Your Software”),
 * provided Your Software does not consist solely of the Contributions; and
 * (3) modify the Contributions for your own internal use.
 * Facebook reserves all rights not expressly granted to you in this license agreement.
 *
 * THE CONTRIBUTIONS AND DOCUMENTATION, IF ANY, ARE PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL FACEBOOK OR ITS AFFILIATES, OFFICERS,
 * DIRECTORS OR EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THE CONTRIBUTIONS, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.kazak.talk;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import javax.inject.Inject;

import io.kazak.BuildConfig;
import io.kazak.KazakApplication;
import io.kazak.R;
import io.kazak.model.Talk;
import io.kazak.repository.DataRepository;
import io.kazak.repository.event.SyncEvent;
import io.kazak.talk.view.TalkDetailsView;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static io.kazak.base.BasePackage.safeTrim;

public class TalkDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TALK_ID = BuildConfig.APPLICATION_ID + ".extra.TALK_ID";

    private final CompositeSubscription subscriptions;

    @Inject
    DataRepository dataRepository;

    private String talkId;
    private TalkDetailsView talkDetailsView;

    public TalkDetailsActivity() {
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        KazakApplication.injector().inject(this);
        setContentView(R.layout.activity_talk);

        talkDetailsView = (TalkDetailsView) findViewById(R.id.talk_details);

        talkId = extractTalkIdFrom(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribeToTalk(talkId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.clear();
    }

    private String extractTalkIdFrom(Intent intent) {
        return safeTrim(intent.getStringExtra(EXTRA_TALK_ID));
    }

    private void updateWith(Talk talk) {
        talkDetailsView.updateWith(talk);
    }

    private void subscribeToTalk(String talkId) {
        subscriptions.add(
                dataRepository.getTalk(talkId)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new TalkObserver())
        );
        subscriptions.add(
                dataRepository.getScheduleSyncEvents()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SyncEventObserver())
        );
    }

    private class TalkObserver implements Observer<Talk> {

        @Override
        public void onCompleted() {
            // No-op
        }

        @Override
        public void onError(Throwable e) {
            throw new IllegalStateException(e);
        }

        @Override
        public void onNext(Talk talk) {
            updateWith(talk);
        }
    }

    private class SyncEventObserver implements Observer<SyncEvent> {

        @Override
        public void onCompleted() {
            // Ignore
        }

        @Override
        public void onError(Throwable e) {
            throw new IllegalStateException(e);
        }

        @Override
        public void onNext(SyncEvent syncEvent) {
            switch (syncEvent.getState()) {
                case ERROR:
                    Snackbar.make(talkDetailsView, R.string.error_loading_schedule, Snackbar.LENGTH_LONG)
                            .setAction(
                                    R.string.action_retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            subscribeToTalk(talkId);
                                        }
                                    })
                            .show();
                    break;
                case IDLE:
                    //Display empty screen if no data
                    break;
                case LOADING:
                    //Display loading screen
                    break;
            }
        }
    }
}
