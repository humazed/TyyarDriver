package com.tyyar.tyyardriver.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.tyyar.tyyardriver.R;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * TODO: document your custom view class.
 */
public class NavigationView extends LinearLayout {
    private static final String TAG = NavigationView.class.getSimpleName();

    @BindView(R.id.directions_icon) ImageView mDirectionsIcon;
    @BindView(R.id.instruction_textView) TextView mInstructionTextView;
    @BindView(R.id.location_name_textView) TextView mLocationNameTextView;
    @BindView(R.id.location_address_textView) TextView mLocationAddressTextView;

    Unbinder unbinder;

    private String mInstruction = "";
    private String mLocationName = "";
    private String mLocationAddress = "";
    private LatLng mDestination;

    public NavigationView(Context context) {
        super(context);
        init(null, 0);
    }

    public NavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public NavigationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        LayoutInflater.from(getContext()).inflate(R.layout.view_navigation_directions, this);
        ButterKnife.bind(this);

        mDirectionsIcon.setOnClickListener(v -> {
            // Create a Uri from an intent string. Use the result to create an Intent.
            Uri gmmIntentUri = Uri.parse(
                    String.format(Locale.US,
                            "google.navigation:q=%f,%f", mDestination.latitude, mDestination.longitude));
            Log.d(TAG, "init " + gmmIntentUri);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Attempt to start an activity that can handle the Intent
            getContext().startActivity(mapIntent);
        });

        setInstruction(mInstruction);

        setLocationName(mLocationName);
        setLocationAddress(mLocationAddress);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // View is now detached, and about to be destroyed
        unbinder.unbind();
    }

    public String getInstruction() {
        return mInstruction;
    }

    public void setInstruction(String instruction) {
        mInstruction = instruction;
        Log.d(TAG, "setInstruction " + instruction);
        if (instruction.isEmpty())
            mInstructionTextView.setVisibility(View.GONE);
        else {
            mInstructionTextView.setVisibility(View.VISIBLE);
            mInstructionTextView.setText(instruction);
        }

    }

    public String getLocationName() {
        return mLocationName;
    }

    public void setLocationName(String locationName) {
        mLocationName = locationName;
        mLocationNameTextView.setText(locationName);
    }

    public String getLocationAddress() {
        return mLocationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        mLocationAddress = locationAddress;
        mLocationAddressTextView.setText(locationAddress);
    }

    public LatLng getDestination() {
        return mDestination;
    }

    public void setDestination(LatLng destination) {
        mDestination = destination;
    }
}
