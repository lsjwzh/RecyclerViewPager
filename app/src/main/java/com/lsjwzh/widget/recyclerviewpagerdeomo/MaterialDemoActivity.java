package com.lsjwzh.widget.recyclerviewpagerdeomo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.lsjwzh.widget.recyclerviewpager.FragmentStatePagerAdapter;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.lsjwzh.widget.recyclerviewpager.TabLayoutSupport;

public class MaterialDemoActivity extends AppCompatActivity {
    protected RecyclerViewPager mRecyclerView;
    private FragmentsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_material);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        initViewPager();
        initTabLayout();

    }

    private void initTabLayout() {
        //给TabLayout增加Tab, 并关联ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        TabLayoutSupport.setupWithViewPager(tabLayout, mRecyclerView, mAdapter);
    }

    protected void initViewPager() {
        mRecyclerView = (RecyclerViewPager) findViewById(R.id.viewpager);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);
        mRecyclerView.setLayoutManager(layout);
        mAdapter = new FragmentsAdapter(getSupportFragmentManager());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);

        mRecyclerView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
            @Override
            public void OnPageChanged(int oldPosition, int newPosition) {
                Log.d("test", "oldPosition:" + oldPosition + " newPosition:" + newPosition);
            }
        });

    }


    class FragmentsAdapter extends FragmentStatePagerAdapter implements TabLayoutSupport.ViewPagerTabLayoutAdapter {

        public FragmentsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position, Fragment.SavedState savedState) {
            Fragment f = new CheeseListFragment();
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
        public String getPageTitle(int position) {
            return "item-" + position;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
