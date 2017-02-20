package com.tyyar.tyyardriver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tyyar.tyyardriver.R;
import com.tyyar.tyyardriver.utils.UiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.max.slideview.SlideView;

/**
 * appears after arriving at the merchant
 */
public class PickupActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.confirm_pickup_slideView) SlideView mConfirmPickupSlideView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        UiUtils.showDrawer(this, mToolbar, 1);

        mConfirmPickupSlideView.setOnSlideCompleteListener(v -> startActivity(new Intent(this, ToCustomerActivity.class)));
    }
}
