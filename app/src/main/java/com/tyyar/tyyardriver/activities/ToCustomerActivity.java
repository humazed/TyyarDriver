package com.tyyar.tyyardriver.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tyyar.tyyardriver.R;
import com.tyyar.tyyardriver.dialogs.PostDeliveryDialogFragment;
import com.tyyar.tyyardriver.view.NavigationView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.max.slideview.SlideView;

import static com.tyyar.tyyardriver.utils.MapUtils.askLocationPermission;
import static com.tyyar.tyyardriver.utils.MapUtils.checkLocationPermission;
import static com.tyyar.tyyardriver.utils.MapUtils.fitThePoints;
import static com.tyyar.tyyardriver.utils.MapUtils.getCompleteAddressString;

public class ToCustomerActivity extends AppCompatActivity implements OnMapReadyCallback,
        RoutingListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = ToMerchantActivity.class.getSimpleName();

    private static final int REQ_PERMISSION = 1001;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.navigationView) NavigationView mNavigationView;
    @BindView(R.id.confirm_delivery_slideView) SlideView mConfirmDeliverySlideView;

    private GoogleMap mMap;
    private View mMapView;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng mCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_customer);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mCustomer = new LatLng(30.037581, 31.240961);
        mNavigationView.setLocationAddress(
                getCompleteAddressString(this, mCustomer.latitude, mCustomer.longitude));
        mNavigationView.setDestination(mCustomer);
        mNavigationView.setLocationName("Tahrir tower");
        mNavigationView.setInstruction("No tomatoes");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mConfirmDeliverySlideView.setOnSlideCompleteListener(v -> showDialog());
    }

    private void showDialog() {
        // Create the fragment and show it as a dialog.
        PostDeliveryDialogFragment newFragment = PostDeliveryDialogFragment.newInstance("");
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkLocationPermission(this))
            mMap.setMyLocationEnabled(true);
        else askLocationPermission(this, REQ_PERMISSION);
    }


    ///////////////////////////////////////////////////////////////////////////
    // GoogleApiClient.ConnectionCallbacks ,onConnectionFailed
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            ArrayList<LatLng> points = new ArrayList<>();

            // Add a marker in Sydney and move the camera
            LatLng driver = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            points.add(driver);
            mMap.addMarker(new MarkerOptions().position(mCustomer).title("Customer")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.customer_pin)));

            points.add(mCustomer);

            fitThePoints(mMap, points);

            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(driver, mCustomer)
                    .build();
            routing.execute();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called with: " + "i = [" + i + "]");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    ///////////////////////////////////////////////////////////////////////////
    // RoutingListener
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
        //add route(s) to the map.

        //In case of more than 5 alternative routes
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(R.color.colorAccent));
        polyOptions.width(10);
        polyOptions.addAll(routes.get(shortestRouteIndex).getPoints());
        mMap.addPolyline(polyOptions);


        Toast.makeText(this, "Route " + 1 + ": distance - "
                + routes.get(shortestRouteIndex).getDistanceValue() + ": duration - " +
                routes.get(shortestRouteIndex).getDurationValue(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Log.e(TAG, "onRoutingFailure: ", e);
    }

    @Override
    public void onRoutingStart() {
        Log.d(TAG, "onRoutingStart() called with: " + "");
    }

    @Override
    public void onRoutingCancelled() {
        Log.d(TAG, "onRoutingCancelled() called with: " + "");
    }


    ///////////////////////////////////////////////////////////////////////////
    // Runtime permissions
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (checkLocationPermission(this))
                        mMap.setMyLocationEnabled(true);

                } else {
                    // Permission denied
                    Toast.makeText(this, "permition denyed app can't work", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
