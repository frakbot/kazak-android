package io.kazak.navigation;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.support.annotation.NonNull;

import io.kazak.DebugActivity;
import io.kazak.map.VenueMapActivity;
import io.kazak.model.Id;
import io.kazak.schedule.ScheduleActivity;
import io.kazak.settings.SettingsActivity;
import io.kazak.talk.TalkDetailsActivity;

public class Navigator {

    private final Activity activityContext;

    public Navigator(@NonNull Activity activityContext) {
        this.activityContext = activityContext;
    }

    public void toSchedule() {
        Intent intent = new Intent(activityContext, ScheduleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityContext.startActivity(intent);
    }

    public void toSessionDetails(Id sessionId) {
        Intent intent = new Intent(activityContext, ScheduleActivity.class);
        intent.putExtra(TalkDetailsActivity.EXTRA_TALK_ID, sessionId.getId());
        activityContext.startActivity(intent);
    }

    public void toArrivalInfo() {
        // TODO go to arrival info activity once it's created
        /*Intent intent = new Intent(activityContext, ArrivalInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityContext.startActivity(intent);*/
    }

    public void toVenueMap() {
        Intent intent = new Intent(activityContext, VenueMapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityContext.startActivity(intent);
    }

    public void toSettings() {
        Intent intent = new Intent(activityContext, SettingsActivity.class);
        activityContext.startActivity(intent);
    }

    public void toDebug() {
        Intent intent = new Intent(activityContext, DebugActivity.class);
        activityContext.startActivity(intent);
    }

    public void upToParent() {
        Intent intent = activityContext.getParentActivityIntent();
        if (intent == null) {
            activityContext.finish();
            return;
        }

        if (activityContext.shouldUpRecreateTask(intent)) {
            TaskStackBuilder.create(activityContext)
                    .addParentStack(activityContext)
                    .startActivities();
        } else {
            activityContext.navigateUpTo(intent);
        }
    }

}
