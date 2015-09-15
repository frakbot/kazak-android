package io.kazak.login;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class KazakAuthenticationService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new KazakAuthenticator(this).getIBinder();
    }
}
