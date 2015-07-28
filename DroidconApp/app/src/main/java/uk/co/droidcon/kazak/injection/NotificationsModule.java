package uk.co.droidcon.kazak.injection;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import uk.co.droidcon.kazak.notifications.NotificationCreator;
import uk.co.droidcon.kazak.notifications.Notifier;

@Module
public class NotificationsModule {

    @Provides
    NotificationCreator providesNotificationCreator(Context context) {
        return new NotificationCreator(context);
    }

    @Provides
    Notifier providesNotifier(Context context) {
        return Notifier.from(context);
    }

}
