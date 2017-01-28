package com.tyyar.tyyardriver.dialogs;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tyyar.tyyardriver.R;
import com.tyyar.tyyardriver.activities.ToMerchantActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewOrderDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewOrderDialogFragment extends DialogFragment {
    private static final String TAG = NewOrderDialogFragment.class.getSimpleName();

    @BindView(R.id.decline_button) Button mDeclineButton;
    @BindView(R.id.trip_summary_mapView) MapView mTripSummaryMapView;
    @BindView(R.id.accept_order_button) Button mAcceptOrderButton;

    private static final String ARG_PARAM1 = "param1";

    private String mParam1;
    private GoogleMap mMap;


    public NewOrderDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment NewOrderDialogFragment.
     */
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_order_dialog, container, false);
        ButterKnife.bind(this, view);
        mTripSummaryMapView.onCreate(savedInstanceState);

        mTripSummaryMapView.getMapAsync(this::setupMap);

        mAcceptOrderButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), ToMerchantActivity.class)));

        return view;
    }

    private void setupMap(GoogleMap map) {
        mMap = map;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(30.0448742, 31.2350013);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14));
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
}
