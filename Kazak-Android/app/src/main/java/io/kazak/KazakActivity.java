package io.kazak;

import android.os.Bundle;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigator = new Navigator(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        findAppbar();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        findAppbar();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        findAppbar();
    }

    protected final void findAppbar() {
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
