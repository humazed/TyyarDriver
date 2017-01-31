package com.tyyar.tyyardriver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.tyyar.tyyardriver.R;
import com.tyyar.tyyardriver.dialogs.NewOrderDialogFragment;
import com.tyyar.tyyardriver.utils.UiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.button) Button mButton;
    @BindView(R.id.activity_main) RelativeLayout mActivityMain;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        UiUtils.showDrawer(this, mToolbar).setSelection(1, false);

//        mButton.setOnClickListener(v -> showDialog());
//        mButton.setOnClickListener(v -> startActivity(new Intent(this, StartNowActivity.class)));
        startActivity(new Intent(this, StartNowActivity.class));
    }

    void showDialog() {
        // Create the fragment and show it as a dialog.
        NewOrderDialogFragment newFragment = NewOrderDialogFragment.newInstance("");
        newFragment.show(getSupportFragmentManager(), "dialog");
    }
}
