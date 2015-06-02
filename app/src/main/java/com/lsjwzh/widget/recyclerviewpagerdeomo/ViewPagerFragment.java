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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class ViewPagerFragment extends Fragment {
    private ViewPager mViewPager;
    private TextView mStateText;
    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_viewpager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();

        mToast = Toast.makeText(activity, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);

        mViewPager = (ViewPager) view.findViewById(R.id.list);
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 100;
            }


            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                final View view = LayoutInflater.from(getActivity()).inflate(R.layout.item, container, false);
                ((TextView) view.findViewById(R.id.title)).setText(position + "");
                view.setTag("" + position);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                if (object != null && object instanceof View) {
                    container.removeView((View) object);
                }
            }

            @Override
            public int getItemPosition(Object object) {
                if (object instanceof View) {
                    return Integer.valueOf(((View) object).getTag().toString());
                }
                return super.getItemPosition(object);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                updateState(state);
            }
        });

        mStateText = (TextView) view.getRootView().findViewById(R.id.state);
        updateState(RecyclerView.SCROLL_STATE_IDLE);
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
}
