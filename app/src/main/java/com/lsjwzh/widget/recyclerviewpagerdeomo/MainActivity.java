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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled
                (false);

        ActionBar.Tab tab = actionBar.newTab()
                .setText("Horizontal")
                .setTabListener(new TabListener(HorizontalLayoutFragment.class, "Horizontal"));
        ActionBar.Tab tab2 = actionBar.newTab()
                .setText("Vertical")
                .setTabListener(new TabListener(VerticalLayoutFragment.class, "Vertical"));
        ActionBar.Tab tab3 = actionBar.newTab()
                .setText("ViewPager")
                .setTabListener(new TabListener(ViewPagerFragment.class, "ViewPager"));
        ActionBar.Tab tab4 = actionBar.newTab()
                .setText("FragmentPager")
                .setTabListener(new TabListener(FragmentsPagerFragment.class, "FragmentPager"));
        actionBar.addTab(tab, false);
        actionBar.addTab(tab2, false);
        actionBar.addTab(tab3, false);
        actionBar.addTab(tab4, false);
        int tabIndex = getIntent().getIntExtra("tab",0);
        actionBar.selectTab(actionBar.getTabAt(tabIndex));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getIntent().putExtra("tab", getSupportActionBar().getSelectedTab().getPosition());
        super.onSaveInstanceState(outState);
    }

    public class TabListener implements ActionBar.TabListener {
        private final String mTag;
        Class<? extends Fragment> mFragClazz;

        public TabListener(Class<? extends Fragment> fragClazz, String tag) {
            mFragClazz = fragClazz;
            mTag = tag;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
            if (fragment == null) {
                ft.add(R.id.content, new HorizontalLayoutFragment(), mTag);
            } else if (!mFragClazz.isAssignableFrom(fragment.getClass())) {
                try {
                    ft.replace(R.id.content, mFragClazz.newInstance());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }
    }

}
