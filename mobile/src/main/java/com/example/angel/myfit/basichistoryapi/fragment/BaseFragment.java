package com.example.angel.myfit.basichistoryapi.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.example.angel.myfit.basichistoryapi.activity.IMainActivityCallback;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";
    protected IMainActivityCallback mCallback;

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Release the views injects by butterknife
        ButterKnife.reset(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof IMainActivityCallback) {
            mCallback = (IMainActivityCallback)activity;
            mCallback.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    /**
     * Clear callback on detach to prevent null reference errors after the view has been
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }
}