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
package io.kazak.session.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.model.Session;
import io.kazak.model.Speaker;
import io.kazak.model.Speakers;
import io.kazak.model.Talk;

public class SessionContentView extends LinearLayout {

    private LinearLayout speakerContainer;
    private TextView topics;
    private TextView description;
    private LayoutInflater layoutInflater;

    public SessionContentView(Context context) {
        this(context, null);
    }

    public SessionContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        topics = (TextView) findViewById(R.id.session_topics);
        description = (TextView) findViewById(R.id.session_description);

        layoutInflater = LayoutInflater.from(getContext());
    }

    public void updateWith(Session session) {
        final Speakers speakers;

        switch (session.type()) {
            case CEREMONY:
            case PLACEHOLDER:
                speakers = null;
                break;

            case TALK:
                speakers = ((Talk) session).getSpeakers();
                break;

            default:
                throw new DeveloperError("Session type not supported: "+session.type());
        }

        updateWithSpeakers(speakers);
        topics.setText(session.name());
        description.setText(session.description());
    }

    private void updateWithSpeakers(@Nullable Speakers speakersObj){
        // Remove speaker views if any.
        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (getChildAt(i) instanceof SessionSpeakerView) {
                removeViewAt(i);
            }
        }

        if (speakersObj == null) {
            return;
        }

        // Add speaker views if any.
        final List<Speaker> speakers = speakersObj.getSpeakers();
        for (int i = 0; i < speakers.size(); i++) {
            SessionSpeakerView speakerDetailsView =
                    (SessionSpeakerView) layoutInflater.inflate(
                            R.layout.view_session_speaker, this, false);
            speakerDetailsView.updateWith(speakers.get(i));
            // Add the speakers in order at the top of this LinearLayout.
            addView(speakerDetailsView, i);
        }
    }
}
