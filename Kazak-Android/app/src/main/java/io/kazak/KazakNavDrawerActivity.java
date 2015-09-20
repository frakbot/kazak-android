package io.kazak;

import android.annotation.SuppressLint;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import io.kazak.base.DeveloperError;

public class KazakNavDrawerActivity extends KazakActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ViewGroup rootContainer;

    @Override
    @CallSuper
    public void setContentView(@LayoutRes int layoutResID) {
        ensureDrawerLayout();
        rootContainer.removeAllViews();
        getLayoutInflater().inflate(layoutResID, rootContainer, true);
        findAppbar();
    }

    @Override
    @CallSuper
    public void setContentView(View view) {
        setContentView(view, null);
    }

    @Override
    @CallSuper
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        ensureDrawerLayout();
        rootContainer.removeAllViews();
        if (params == null) {
            rootContainer.addView(view);
        } else {
            rootContainer.addView(view, params);
        }
        findAppbar();
    }

    @SuppressLint("InflateParams")
    private void ensureDrawerLayout() {
        if (drawerLayout != null) {
            return;
        }

        Window window = getWindow();
        drawerLayout = (DrawerLayout) window.getLayoutInflater().inflate(R.layout.base_activity_navigation_drawer, null);
        rootContainer = (ViewGroup) drawerLayout.findViewById(R.id.root_container);
        navigationView = (NavigationView) drawerLayout.findViewById(R.id.drawer_menu);

        window.setContentView(drawerLayout);
        setupNavigationDrawer();
    }

    private void setupNavigationDrawer() {
        if (BuildConfig.DEBUG) {
            navigationView.inflateMenu(R.menu.drawer_debug);
        }
        navigationView.setCheckedItem(R.id.menu_nav_schedule);
        navigationView.setNavigationItemSelectedListener(this);
        hackToHideNavDrawerHeaderRipple();
    }

    private void hackToHideNavDrawerHeaderRipple() {
        // TODO remove this when the issue is fixed
        // See https://code.google.com/p/android/issues/detail?id=176400
        View navigationHeader = findViewById(R.id.navigation_header);
        ((View) navigationHeader.getParent()).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do nothing
                    }
                }
        );
    }

    @Override
    @CallSuper
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_nav_schedule:
                // Do nothing: we're already there
                break;
            case R.id.menu_nav_arrival_info:
                navigate().toArrivalInfo();
                break;
            case R.id.menu_nav_venue_map:
                navigate().toVenueMap();
                break;
            case R.id.menu_nav_settings:
                navigate().toSettings();
                break;
            case R.id.menu_nav_debug:
                navigate().toDebug();
                break;
            default:
                throw new DeveloperError("Menu item " + item + " not supported");
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    protected final void openNavigationDrawer() {
        drawerLayout.openDrawer(navigationView);
    }

}
