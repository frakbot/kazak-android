package io.kazak;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.kazak.model.Id;
import io.kazak.model.Room;
import io.kazak.model.Speaker;
import io.kazak.model.Speakers;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import io.kazak.notifications.EventAlarmService;
import io.kazak.notifications.NotificationCreator;
import io.kazak.notifications.Notifier;

@SuppressWarnings("checkstyle:magicnumber")
public class DebugActivity extends KazakActivity {

    private NotificationCreator notificationCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_debug);

        setupAppBar();

        Button buttonSingleNotification = (Button) findViewById(R.id.button_test_single_notification);
        buttonSingleNotification.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        testSingleNotification();
                    }
                }
        );

        Button buttonMultipleNotifications = (Button) findViewById(R.id.button_test_multiple_notifications);
        buttonMultipleNotifications.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        testMultipleNotifications();
                    }
                }
        );

        Button buttonEventAlarmService = (Button) findViewById(R.id.button_test_event_alarm_service);
        buttonEventAlarmService.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent serviceIntent = new Intent(DebugActivity.this, EventAlarmService.class);
                        startService(serviceIntent);
                    }
                }
        );

        notificationCreator = new NotificationCreator(this);
    }

    private void setupAppBar() {
        getSupportAppBar().setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigate().upToParent();
                    }
                }
        );
    }

    private void testSingleNotification() {
        createAndNotifyTalksCount(1);
    }

    private void testMultipleNotifications() {
        createAndNotifyTalksCount(3);
    }

    private void createAndNotifyTalksCount(int count) {
        List<Talk> talks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            talks.add(createTestTalk(i));
        }
        List<Notification> notifications = notificationCreator.createFrom(talks);

        Notifier notifier = Notifier.from(this);
        notifier.showNotifications(notifications);
    }

    @NonNull
    private Talk createTestTalk(int id) {
        return new Talk(
                new Id(String.valueOf(id)),
                "A very interesting talk",
                "The talk description which will tell you things about it",
                createTalkTimeSlot(),
                createTalkRooms(id),
                createTalkSpeakers(),
                null
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
    private List<Room> createTalkRooms(int id) {
        return Collections.singletonList(
                new Room(new Id("45678" + id), "Room " + id)
        );
    }

    @NonNull
    private Speakers createTalkSpeakers() {
        List<Speaker> speakers = new ArrayList<>(2);
        speakers.add(new Speaker(new Id("0"), "Awesome Speaker", null, null, null, null));
        speakers.add(new Speaker(new Id("1"), "Meh Guy", null, null, null, null));
        return new Speakers(speakers);
    }

}
