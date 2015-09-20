package io.kazak;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.CallSuper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import io.kazak.navigation.Navigator;

public abstract class KazakActivity extends AppCompatActivity {

    private Navigator navigator;
    private Toolbar appbar;

    @Override
    @CallSuper
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        navigator = new Navigator(this);
    }

    @Override
    @CallSuper
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        findAppbar();
    }

    @Override
    @CallSuper
    public void setContentView(View view) {
        super.setContentView(view);
        findAppbar();
    }

    @Override
    @CallSuper
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        findAppbar();
    }

    private void findAppbar() {
        appbar = (Toolbar) findViewById(R.id.appbar);
        if (appbar != null) {
            setSupportActionBar(appbar);
        }
    }

    protected final Navigator navigate() {
        return navigator;
    }

    protected final Toolbar getSupportAppBar() {
        return appbar;
    }

}
