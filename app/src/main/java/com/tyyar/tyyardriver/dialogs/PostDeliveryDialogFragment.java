package com.tyyar.tyyardriver.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tyyar.tyyardriver.R;
import com.tyyar.tyyardriver.activities.LookingForOrdersActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PostDeliveryDialogFragment extends DialogFragment {
    private static final String TAG = PostDeliveryDialogFragment.class.getSimpleName();

    private static final String ARG_PARAM1 = "param1";
    @BindView(R.id.gear_image) ImageView mGearImage;
    @BindView(R.id.encouragement_text_view) TextView mEncouragementTextView;
    @BindView(R.id.delivery_pay_view) TextView mDeliveryPayView;
    @BindView(R.id.num_deliveries_view) TextView mNumDeliveriesView;
    @BindView(R.id.continue_button) Button mContinueButton;
    @BindView(R.id.undo_option) TextView mUndoOption;

    private Unbinder unbinder;
    private String mParam1;


    public PostDeliveryDialogFragment() {
        // Required empty public constructor
    }

    public static PostDeliveryDialogFragment newInstance(String param1) {
        PostDeliveryDialogFragment fragment = new PostDeliveryDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_post_delivary_dialog, container, false);
        unbinder = ButterKnife.bind(this, view);

        mContinueButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), LookingForOrdersActivity.class)));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
