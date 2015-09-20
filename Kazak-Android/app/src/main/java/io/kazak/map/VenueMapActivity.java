package io.kazak.map;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import io.kazak.BuildConfig;
import io.kazak.KazakNavDrawerActivity;
import io.kazak.R;

public class VenueMapActivity extends KazakNavDrawerActivity {

    private GoogleMap map; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_map);
        setupAppBar();
        setupMap();
    }

    private void setupAppBar() {
        getSupportAppBar().setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openNavigationDrawer();
                    }
                }
        );
    }

    private void setupMap() {
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.venue_map))
                .getMapAsync(new MapEventsListener());
    }

    private class MapEventsListener implements OnMapReadyCallback {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            setInitialMapPositionAndZoom();
        }

        private void setInitialMapPositionAndZoom() {
            LatLng latLng = new LatLng(BuildConfig.VENUE_LOCATION_LAT, BuildConfig.VENUE_LOCATION_LON);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, BuildConfig.VENUE_LOCATION_MAP_ZOOM);
            map.moveCamera(update);
        }

    }

}
