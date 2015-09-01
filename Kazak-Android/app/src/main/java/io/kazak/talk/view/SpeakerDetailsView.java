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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.kazak.R;
import io.kazak.model.Speaker;

public class SpeakerDetailsView extends RelativeLayout {

    private ImageView avatar;
    private TextView name;
    private TextView jobTitle;


    public SpeakerDetailsView(Context context) {
        this(context, null);
    }

    public SpeakerDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        avatar = (ImageView) findViewById(R.id.avatar);
        name = (TextView) findViewById(R.id.name);
        jobTitle = (TextView) findViewById(R.id.jobtitle);
    }

    public void updateWith(Speaker speaker) {
        // TODO: download image avatar.
        avatar.setImageResource(R.drawable.ic_nav_schedule);
        name.setText(speaker.getName());
        // TODO: set job title.
        jobTitle.setText("My job title.");
    }
}
