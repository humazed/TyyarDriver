package com.tyyar.tyyardriver.activities;

import android.Manifest;
import android.content.Intent;
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
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.microsoft.windowsazure.notifications.NotificationsManager;
import com.tyyar.tyyardriver.R;
import com.tyyar.tyyardriver.dialogs.NewOrderDialogFragment;
import com.tyyar.tyyardriver.notification.MyHandler;
import com.tyyar.tyyardriver.notification.NotificationSettings;
import com.tyyar.tyyardriver.notification.RegistrationIntentService;
import com.tyyar.tyyardriver.utils.BitmapUtils;
import com.tyyar.tyyardriver.utils.UiUtils;
import com.tyyar.tyyardriver.view.NavigationView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
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


    public static LookingForOrdersActivity mainActivity;
    public static Boolean isVisible = false;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_looking_for_orders);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        UiUtils.showDrawer(this, mToolbar, 1);
        setupNotification();
        mCompositeDisposable = new CompositeDisposable();

        mHotspot = new LatLng(30.0246552,30.9755531);
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

//        mCompositeDisposable.add(
//                Observable.timer(5, TimeUnit.SECONDS, Schedulers.io())
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(aLong -> showDialog()));


    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.");
                ToastNotify("This device is not supported by Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void registerWithNotificationHubs() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with FCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(() -> {
//            Toast.makeText(LookingForOrdersActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
//            TextView helloText = (TextView) findViewById(R.id.text_hello);
//            helloText.setText(notificationMessage);
//            Toast.makeText(LookingForOrdersActivity.this, "notificationMessage: " + notificationMessage, Toast.LENGTH_LONG).show();
            Log.d(TAG, "ToastNotify " + "notificationMessage: " + notificationMessage);
            showDialog();
        });
    }

    private void setupNotification() {
        mainActivity = this;
        NotificationsManager.handleNotifications(this, NotificationSettings.SenderId, MyHandler.class);
        registerWithNotificationHubs();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    private void showDialog() {
        // Create the fragment and show it as a dialog.
        NewOrderDialogFragment newFragment = NewOrderDialogFragment.newInstance("");
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
        isVisible = true;
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        isVisible = false;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }
}
