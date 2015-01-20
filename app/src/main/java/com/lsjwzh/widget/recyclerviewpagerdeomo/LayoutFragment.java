/*
 * Copyright (C) 2014 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lsjwzh.widget.recyclerviewpagerdeomo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManagerEx;
import android.support.v7.widget.OrientationHelperEx;
import android.support.v7.widget.RecyclerViewEx;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import static android.support.v7.widget.RecyclerViewEx.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerViewEx.SCROLL_STATE_IDLE;
import static android.support.v7.widget.RecyclerViewEx.SCROLL_STATE_SETTLING;

public class LayoutFragment extends Fragment {
    private static final String ARG_LAYOUT_ID = "layout_id";

    private RecyclerViewPager mRecyclerView;
    private TextView mPositionText;
    private TextView mCountText;
    private TextView mStateText;
    private Toast mToast;

    private int mLayoutId;

    public static LayoutFragment newInstance(int layoutId) {
        LayoutFragment fragment = new LayoutFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_ID, layoutId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayoutId = getArguments().getInt(ARG_LAYOUT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(mLayoutId, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();

        mToast = Toast.makeText(activity, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);

        mRecyclerView = (RecyclerViewPager) view.findViewById(R.id.list);
        mRecyclerView.setDisplayPadding(50);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);

        mPositionText = (TextView) view.getRootView().findViewById(R.id.position);
        mCountText = (TextView) view.getRootView().findViewById(R.id.count);

        mStateText = (TextView) view.getRootView().findViewById(R.id.state);
        updateState(SCROLL_STATE_IDLE);


        mRecyclerView.setOnScrollListener(new RecyclerViewEx.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerViewEx recyclerView, int scrollState) {
                updateState(scrollState);
            }

            @Override
            public void onScrolled(RecyclerViewEx recyclerView, int i, int i2) {
//                mPositionText.setText("First: " + mRecyclerView.getFirstVisiblePosition());
                int childCount = mRecyclerView.getChildCount();
                int width = mRecyclerView.getChildAt(0).getWidth();
                int padding  = (mRecyclerView.getWidth() - width)/2;
                mCountText.setText("Count: " + childCount);
                for (int j = 0; j < childCount; j++) {
                    View v = mRecyclerView.getChildAt(j);
                    //往左 从 padding 到 -(v.getWidth()-padding) 的过程中，由大到小
                    float rate = 0;
                    if(v.getLeft()<=padding){
                        if(v.getLeft()>=padding-v.getWidth()){
                            rate = (padding - v.getLeft())*1f/v.getWidth();
                        }else {
                            rate = 1;
                        }
                        v.setScaleX(1-rate*0.1f);
                        v.setScaleY(1-rate*0.1f);
                    }else{
                        //往右 从 padding 到 recyclerView.getWidth()-padding 的过程中，由大到小
                        if(v.getLeft()<=recyclerView.getWidth()-padding){
                            rate = (recyclerView.getWidth()-padding - v.getLeft())*1f/v.getWidth();
                        }
                        v.setScaleX(0.9f+rate*0.1f);
                        v.setScaleY(0.9f+rate*0.1f);
                    }
                }
            }
        });

//        final Drawable divider = getResources().getDrawable(R.drawable.divider);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));
        mRecyclerView.setLayoutManager(new LinearLayoutManagerEx(getActivity(), OrientationHelperEx.HORIZONTAL,false));
        mRecyclerView.setAdapter(new LayoutAdapter(activity, mRecyclerView, mLayoutId));
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                if(mRecyclerView.getChildAt(1)!=null){
                    View v2 = mRecyclerView.getChildAt(1);
                    v2.setScaleX(0.9f);
                    v2.setScaleY(0.9f);
                }
            }
        });
    }

    private void updateState(int scrollState) {
        String stateName = "Undefined";
        switch(scrollState) {
            case SCROLL_STATE_IDLE:
                stateName = "Idle";
                break;

            case SCROLL_STATE_DRAGGING:
                stateName = "Dragging";
                break;

            case SCROLL_STATE_SETTLING:
                stateName = "Flinging";
                break;
        }

        mStateText.setText(stateName);
    }

    public int getLayoutId() {
        return getArguments().getInt(ARG_LAYOUT_ID);
    }
}
