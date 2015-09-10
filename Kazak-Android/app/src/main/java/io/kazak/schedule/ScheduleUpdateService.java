package io.kazak.schedule;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import javax.inject.Inject;

import io.kazak.KazakApplication;
import io.kazak.repository.DataRepository;
import io.kazak.repository.event.SyncEvent;
import io.kazak.repository.event.SyncState;

public class ScheduleUpdateService extends GcmTaskService {

    @Inject
    DataRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        KazakApplication.injector().inject(this);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        SyncEvent result = repository.refreshSchedule()
                .toBlocking()
                .first();

        if (result.getState() == SyncState.ERROR) {
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

}
