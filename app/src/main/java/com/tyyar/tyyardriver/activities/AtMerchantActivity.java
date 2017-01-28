package com.tyyar.tyyardriver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.tyyar.tyyardriver.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * appears after arrving at the merchant
 */
public class AtMerchantActivity extends AppCompatActivity {

    @BindView(R.id.confirm_pickup_button) Button mConfirmPickupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_at_merchant);
        ButterKnife.bind(this);


        mConfirmPickupButton.setOnClickListener(v -> startActivity(new Intent(this, ToCustomerActivity.class)));

    }
}