package io.kazak;

import android.app.Activity;
import android.app.Notification;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.kazak.model.Room;
import io.kazak.model.Speaker;
import io.kazak.model.Speakers;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import io.kazak.model.Track;
import io.kazak.notifications.NotificationCreator;
import io.kazak.notifications.Notifier;


public class DebugActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        Button buttonSingleNotification = (Button) findViewById(R.id.button_test_single_notification);
        buttonSingleNotification.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        testSingleNotification();
                    }
                });
    }

    private void testSingleNotification() {
        NotificationCreator notificationCreator = new NotificationCreator(this);
        Talk talk = createTestTalk();
        Notification singleNotification = notificationCreator.createFrom(talk);

        Notifier notifier = Notifier.from(this);
        notifier.showNotification(singleNotification);
    }

    @NonNull
    private Talk createTestTalk() {
        return new Talk(
                "12345",
                "A very interesting talk",
                createTalkTimeSlot(),
                createTalkRoom(),
                createTalkSpeakers(),
                createTalkTrack()
        );
    }

    private TimeSlot createTalkTimeSlot() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.HOUR, 1);
        Date endDate = calendar.getTime();

        return new TimeSlot(startDate, endDate);
    }

    @NonNull
    private Room createTalkRoom() {
        return new Room(
                "45678",
                "Room 1"
        );
    }

    @NonNull
    private Speakers createTalkSpeakers() {
        List<Speaker> speakers = new ArrayList<>(2);
        speakers.add(new Speaker("0", "Awesome Speaker"));
        speakers.add(new Speaker("1", "Meh Guy"));
        return new Speakers(speakers);
    }

    @NonNull
    private Track createTalkTrack() {
        return new Track("dummy", "Track", 0xFFBF0D7B);
    }

}
