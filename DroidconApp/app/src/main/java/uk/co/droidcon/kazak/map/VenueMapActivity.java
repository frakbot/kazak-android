package uk.co.droidcon.kazak.map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import uk.co.droidcon.kazak.BuildConfig;
import uk.co.droidcon.kazak.R;

public class VenueMapActivity extends FragmentActivity {

    private GoogleMap map; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_map);
        setupMap();
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
