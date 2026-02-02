package com.ecocity.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.ecocity.app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnConfirmLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    LatLng target = mMap.getCameraPosition().target;
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("lat", target.latitude);
                    resultIntent.putExtra("lng", target.longitude);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Default location: Madrid (Puerta del Sol)
        LatLng madrid = new LatLng(40.416775, -3.703790);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 15f));

        // If we want to show user location, we would need to request RUNTIME
        // permissions here.
        // For MVP, we start at a default location.
        try {
            mMap.setMyLocationEnabled(false); // Can enable if permissions granted
            mMap.getUiSettings().setZoomControlsEnabled(true);

            // Add padding to move Google Logo and Zoom Controls up
            // 100dp gives enough space for the bottom button
            float density = getResources().getDisplayMetrics().density;
            int bottomPadding = (int) (100 * density);
            mMap.setPadding(0, 0, 0, bottomPadding);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
