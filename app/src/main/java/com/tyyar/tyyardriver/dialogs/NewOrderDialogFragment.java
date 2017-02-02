package com.tyyar.tyyardriver.dialogs;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.utils.ConvertUtils;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tyyar.tyyardriver.R;
import com.tyyar.tyyardriver.activities.ToMerchantActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewOrderDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewOrderDialogFragment extends DialogFragment implements RoutingListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = NewOrderDialogFragment.class.getSimpleName();

    @BindView(R.id.trip_summary_mapView) MapView mTripSummaryMapView;
    @BindView(R.id.decline_button) Button mDeclineButton;
    @BindView(R.id.accept_order_button) Button mAcceptOrderButton;
    @BindView(R.id.distance_textView) TextView mDistanceTextView;

    Unbinder unbinder;

    private static final int REQ_PERMISSION = 1001;
    private static final String ARG_PARAM1 = "param1";

    private String mParam1;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;


    public NewOrderDialogFragment() {
        // Required empty public constructor
    }

    public static NewOrderDialogFragment newInstance(String param1) {
        NewOrderDialogFragment fragment = new NewOrderDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_order_dialog, container, false);
        unbinder = ButterKnife.bind(this, view);
        mTripSummaryMapView.onCreate(savedInstanceState);


        mTripSummaryMapView.getMapAsync(this::setupMap);

        mAcceptOrderButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), ToMerchantActivity.class)));
        mDeclineButton.setOnClickListener(v -> getDialog().dismiss());

        return view;
    }

    private void setupMap(GoogleMap map) {
        mMap = map;
        if (checkPermission())
            mMap.setMyLocationEnabled(true);
        else askPermission();
    }


    ///////////////////////////////////////////////////////////////////////////
    // GoogleApiClient.ConnectionCallbacks ,onConnectionFailed
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected ");
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
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
            LatLng restaurant = new LatLng(30.0438905, 31.2361031);
            mMap.addMarker(new MarkerOptions().position(restaurant).title("restaurant")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.rest_pin)));
            points.add(restaurant);
            LatLng customer = new LatLng(30.037581, 31.240961);
            mMap.addMarker(new MarkerOptions().position(customer).title("customer")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.customer_pin)));
            points.add(customer);

            fitThePoints(points);

            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(driver, restaurant, customer)
                    .build();
            routing.execute();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

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

        mDistanceTextView.setText(routes.get(shortestRouteIndex).getDistanceText());


        Toast.makeText(getContext(), "Route " + 1 + ": distance - "
                + routes.get(shortestRouteIndex).getDistanceValue() + ": duration - " +
                routes.get(shortestRouteIndex).getDurationValue(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Log.e(TAG, "onRoutingFailure: ", e);
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingCancelled() {

    }

    private void fitThePoints(ArrayList<LatLng> points) {
        //Calculate the markers to get their position
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (LatLng point : points) b.include(point);

        //Change the padding as per needed
        CameraUpdate cu = newLatLngBounds(b.build(), ConvertUtils.dp2px(100));
        mMap.moveCamera(cu);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Runtime permissions
    ///////////////////////////////////////////////////////////////////////////
    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (checkPermission())
                        mMap.setMyLocationEnabled(true);

                } else {
                    // Permission denied
                    Toast.makeText(getActivity(), "permition denyed app can't work", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTripSummaryMapView != null) {
            mTripSummaryMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mTripSummaryMapView != null) {
            mTripSummaryMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mTripSummaryMapView != null) {
            try {
                mTripSummaryMapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e(TAG, "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mTripSummaryMapView != null) {
            mTripSummaryMapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTripSummaryMapView != null) {
            mTripSummaryMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
