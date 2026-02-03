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
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;

    private final androidx.activity.result.ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION,
                        false);
                Boolean coarseLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    enableUserLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    enableUserLocation();
                } else {
                    // Default location: Madrid (Puerta del Sol) if permission denied
                    LatLng madrid = new LatLng(40.416775, -3.703790);
                    if (mMap != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 15f));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this);

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

        // Add padding to move Google Logo and Zoom Controls up
        float density = getResources().getDisplayMetrics().density;
        int bottomPadding = (int) (100 * density);
        mMap.setPadding(0, 0, 0, bottomPadding);

        mMap.getUiSettings().setZoomControlsEnabled(true);

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ||
                androidx.core.content.ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        } else {
            locationPermissionRequest.launch(new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void enableUserLocation() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ||
                androidx.core.content.ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                        } else {
                            // Fallback if location is null (e.g. new device, no last known location)
                            LatLng madrid = new LatLng(40.416775, -3.703790);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 15f));
                        }
                    });
        }
    }
}
