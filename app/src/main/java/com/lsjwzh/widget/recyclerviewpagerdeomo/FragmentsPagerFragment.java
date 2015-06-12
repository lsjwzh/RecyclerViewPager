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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lsjwzh.widget.recyclerviewpager.FragmentStatePagerAdapter;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;


public class FragmentsPagerFragment extends Fragment {
    private View mViewRoot;
    private RecyclerViewPager mRecyclerView;
    private TextView mStateText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mViewRoot == null) {
            mViewRoot = inflater.inflate(R.layout.layout_horizontal, container, false);
        } else if (mViewRoot.getParent() != null) {
            ((ViewGroup) mViewRoot.getParent()).removeView(mViewRoot);
        }
        return mViewRoot;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerViewPager) view.findViewById(R.id.list);

        LinearLayoutManager layout = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setAdapter(new FragmentsAdapter(getChildFragmentManager()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);

        mStateText = (TextView) view.getRootView().findViewById(R.id.state);
        updateState(RecyclerView.SCROLL_STATE_IDLE);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                updateState(newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });
    }


    private void updateState(int scrollState) {
        String stateName = "Undefined";
        switch (scrollState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                stateName = "Idle";
                break;

            case RecyclerView.SCROLL_STATE_DRAGGING:
                stateName = "Dragging";
                break;

            case RecyclerView.SCROLL_STATE_SETTLING:
                stateName = "Flinging";
                break;
        }

        mStateText.setText(stateName);
    }

    class FragmentsAdapter extends FragmentStatePagerAdapter {

        public FragmentsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position, SavedState savedState) {
            Fragment f = new PagerItemFragment();
            if (savedState == null) {
                Bundle bundle = new Bundle();
                bundle.putInt("index", position);
                f.setArguments(bundle);
            }
            f.setInitialSavedState(savedState);
            return f;
        }

        @Override
        public void onDestroyItem(int position, Fragment fragment) {
            // onDestroyItem
        }

        @Override
        public int getItemCount() {
            return 100;
        }
    }
}
