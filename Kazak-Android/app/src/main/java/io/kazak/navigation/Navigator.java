package io.kazak.navigation;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

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

    public void navigateToParent() {
        Intent intent = NavUtils.getParentActivityIntent(activityContext);
        if (NavUtils.shouldUpRecreateTask(activityContext, intent)) {
            TaskStackBuilder.create(activityContext)
                    .addParentStack(activityContext)
                    .startActivities();
        } else {
            NavUtils.navigateUpTo(activityContext, intent);
        }
    }

}
