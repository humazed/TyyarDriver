package com.tyyar.tyyardriver.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tyyar.tyyardriver.R;

public class PostDelivaryDialogFragment extends DialogFragment {
    private static final String TAG = PostDelivaryDialogFragment.class.getSimpleName();

    private static final String ARG_PARAM1 = "param1";

    private String mParam1;


    public PostDelivaryDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment PostDelivaryDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostDelivaryDialogFragment newInstance(String param1) {
        PostDelivaryDialogFragment fragment = new PostDelivaryDialogFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_delivary_dialog, container, false);
    }
}
