package com.tyyar.tyyardriver.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.blankj.utilcode.utils.ConvertUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static com.blankj.utilcode.utils.Utils.getContext;

/**
 * User: YourPc
 * Date: 2/1/2017
 */

public class MapUtils {
    private static final String TAG = MapUtils.class.getSimpleName();


    public static String getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address address = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                    strReturnedAddress.append(address.getAddressLine(i)).append(" ");

                strAdd = strReturnedAddress.toString();
                Log.w(TAG, "My Current loction address " + strReturnedAddress.toString());
                Log.w(TAG, "address: " + address);
            } else {
                Log.w(TAG, "My Current loction address " + "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "My Current loction address " + "Canont get Address!");
        }
        return strAdd;
    }

    public static void fitThePoints(GoogleMap map, ArrayList<LatLng> points) {
        //Calculate the markers to get their position
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (LatLng point : points) b.include(point);

        //Change the padding as per needed
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(b.build(), ConvertUtils.dp2px(30)));
        map.moveCamera(CameraUpdateFactory.zoomTo(map.getCameraPosition().zoom - 0.5f));
    }

    // Check for permission to access Location
    public static boolean checkLocationPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED);
    }

    // Asks for permission
    public static void askLocationPermission(Activity activity, int requestCode) {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(activity, new String[]{ACCESS_FINE_LOCATION},
                requestCode
        );
    }

    public static void movLocationButtonToTheBottom(View mapView) {
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1"))
                    .getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
    }
}
