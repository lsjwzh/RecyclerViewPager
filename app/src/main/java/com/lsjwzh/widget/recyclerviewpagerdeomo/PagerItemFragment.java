package com.lsjwzh.widget.recyclerviewpagerdeomo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class PagerItemFragment extends Fragment {
    int mIndex;

    public PagerItemFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        if (savedInstanceState == null) {
            mIndex = getArguments().getInt("index");
            title.setText("index:" + mIndex);
        } else {
            mIndex = savedInstanceState.getInt("index");
            title.setText("index from savedInstanceState:" + mIndex);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("index", mIndex);
        Toast.makeText(getActivity(), "call onSaveInstanceState:" + mIndex, Toast.LENGTH_SHORT).show();
    }
}
