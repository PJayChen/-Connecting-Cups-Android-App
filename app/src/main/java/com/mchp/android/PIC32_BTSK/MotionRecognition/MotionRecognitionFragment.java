package com.mchp.android.PIC32_BTSK.MotionRecognition;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mchp.android.PIC32_BTSK.R;

/**
 * Created by pjay on 2016/1/22.
 */
public class MotionRecognitionFragment extends Fragment {

    // UI relative members
    private TextView identifyMotionText;
    private String lastMotionStateStr;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_motion, container, false);

        identifyMotionText = (TextView) rootView.findViewById(R.id.textViewMotion);

        if (lastMotionStateStr == null) identifyMotionText.setText("---");
        else updateIdentifiedMotionText(lastMotionStateStr);

        // Return the layout for this fragment
        return rootView;

    }

    public void updateIdentifiedMotionText (String motionStr) {
        identifyMotionText.setText(motionStr);
        lastMotionStateStr = new String(motionStr);
    }
}
