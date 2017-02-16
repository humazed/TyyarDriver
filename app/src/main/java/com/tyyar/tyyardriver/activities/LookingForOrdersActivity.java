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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tyyar.tyyardriver.R;
import com.tyyar.tyyardriver.dialogs.NewOrderDialogFragment;
import com.tyyar.tyyardriver.utils.BitmapUtils;
import com.tyyar.tyyardriver.utils.UiUtils;
import com.tyyar.tyyardriver.view.NavigationView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static com.tyyar.tyyardriver.utils.MapUtils.askLocationPermission;
import static com.tyyar.tyyardriver.utils.MapUtils.checkLocationPermission;
import static com.tyyar.tyyardriver.utils.MapUtils.fitThePoints;
import static com.tyyar.tyyardriver.utils.MapUtils.getCompleteAddressString;

public class LookingForOrdersActivity extends AppCompatActivity implements OnMapReadyCallback,
        RoutingListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = LookingForOrdersActivity.class.getSimpleName();
    private static final int REQ_PERMISSION = 1001;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.progress_bar) MaterialProgressBar mProgressBar;
    @BindView(R.id.navigationView) NavigationView mNavigationView;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private CompositeDisposable mCompositeDisposable;
    private LatLng mHotspot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_looking_for_orders);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        UiUtils.showDrawer(this, mToolbar, 1);
        mCompositeDisposable = new CompositeDisposable();

        mHotspot = new LatLng(30.046591, 31.238080);
        mNavigationView.setLocationAddress(
                getCompleteAddressString(this, mHotspot.latitude, mHotspot.longitude));
        mNavigationView.setDestination(mHotspot);
        mNavigationView.setLocationName("McDonald's");
        mNavigationView.setInstruction("Some Instructions");

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

        mCompositeDisposable.add(
                Observable.timer(1, TimeUnit.SECONDS, Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> showDialog()));


    }


    private void showDialog() {
        // Create the fragment and show it as a dialog.
        NewOrderDialogFragment newFragment = NewOrderDialogFragment.newInstance("");
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkLocationPermission())
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
            mMap.addMarker(new MarkerOptions().position(mHotspot).title("hotspot")
                    .icon(BitmapUtils.getBitmapDescriptor(this, R.drawable.ic_hotspot_red_36dp)));

            points.add(mHotspot);

            fitThePoints(mMap, points);

            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(driver, mHotspot)
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
                    if (checkLocationPermission())
                        mMap.setMyLocationEnabled(true);

                } else {
                    // Permission denied
                    Toast.makeText(this, "permition denyed app can't work", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }
}
