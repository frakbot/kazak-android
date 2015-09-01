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
package io.kazak.talk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.kazak.R;
import io.kazak.model.Speaker;
import io.kazak.model.Talk;

public class TalkDetailsView extends FrameLayout {

    private LinearLayout speakerContainer;
    private TextView topics;
    private TextView summary;

    public TalkDetailsView(Context context) {
        this(context, null);
    }

    public TalkDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        speakerContainer = (LinearLayout) findViewById(R.id.speakers_container);
        topics = (TextView) findViewById(R.id.topics);
        summary = (TextView) findViewById(R.id.summary);
    }

    public void updateWith(Talk talk) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        for (Speaker speaker : talk.getSpeakers().getSpeakers()) {
            SpeakerDetailsView speakerDetailsView =
                    (SpeakerDetailsView) layoutInflater.inflate(
                            R.layout.view_speaker_details, speakerContainer, false);
            speakerDetailsView.updateWith(speaker);
            speakerContainer.addView(speakerDetailsView);
        }

        // TODO: set proper topics and summary
        topics.setText("topic1, topic2");
        summary.setText("my talk is about... where is the topic?!?");
    }
}
