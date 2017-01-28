package com.tyyar.tyyardriver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tyyar.tyyardriver.R;
import com.tyyar.tyyardriver.utils.UiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ToMerchantActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.at_pickup_button) Button mAtPickupButton;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_merchant);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        UiUtils.showDrawer(this, mToolbar).setSelection(1, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);

        mAtPickupButton.setOnClickListener(v -> startActivity(new Intent(this, AtMerchantActivity.class)));

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(30.0448742, 31.2350013);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14));
    }
}
