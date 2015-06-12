package com.lsjwzh.widget.recyclerviewpager;

import java.util.HashSet;
import java.util.Set;

import android.annotation.TargetApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Implementation of {@link android.support.v4.view.PagerAdapter} that
 * uses a {@link android.support.v4.app.Fragment} to manage each page. This class also handles
 * saving and restoring of fragment's state.
 * <p/>
 * <p>This version of the pager is more useful when there are a large number
 * of pages, working more like a list view.  When pages are not visible to
 * the user, their entire fragment may be destroyed, only keeping the saved
 * state of that fragment.  This allows the pager to hold on to much less
 * memory associated with each visited page as compared to
 * {@link android.support.v4.app.FragmentPagerAdapter} at the cost of potentially more overhead when
 * switching between pages.
 * <p/>
 * <p>When using FragmentPagerAdapter the host ViewPager must have a
 * valid ID set.</p>
 * <p/>
 * <p>Subclasses only need to implement {@link #getItem(int, Fragment.SavedState)}
 * and {@link #getItemCount()} to have a working adapter.
 */
@TargetApi(12)
public abstract class FragmentStatePagerAdapter extends RecyclerView.Adapter<FragmentStatePagerAdapter.FragmentViewHolder> {
    private static final String TAG = "FragmentStatePagerAdapter";
    private static final boolean DEBUG = false;

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;
    SparseArray<Fragment.SavedState> mStates = new SparseArray<>();
    Set<Integer> mIds = new HashSet<>();

    public FragmentStatePagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }

    @Override
    public void onViewRecycled(FragmentViewHolder holder) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        if (DEBUG) Log.v(TAG, "Removing item #");
        int tagId = genTagId(holder.getAdapterPosition());
        Fragment f = mFragmentManager.findFragmentByTag(tagId + "");
        if (f != null) {
            if (DEBUG) Log.v(TAG, "Removing fragment #");
            mStates.put(tagId, mFragmentManager.saveFragmentInstanceState(f));
            mCurTransaction.remove(f);
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
        if (holder.itemView instanceof ViewGroup) {
            ((ViewGroup) holder.itemView).removeAllViews();
        }
        super.onViewRecycled(holder);
    }

    @Override
    public final FragmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rvp_fragment_container, parent, false);
        int id = mIds.size() + 1;
        view.findViewById(R.id.rvp_fragment_container).setId(id);
        mIds.add(id);
        return new FragmentViewHolder(view);
    }

    @Override
    public final void onBindViewHolder(final FragmentViewHolder holder, int position) {
        final int tagId = genTagId(position);

        Fragment frag = mFragmentManager.findFragmentByTag(tagId + "");
        final Fragment fragmentInAdapter = getItem(position, mStates.get(tagId));
        if (!isSameFragment(frag, fragmentInAdapter)) {
            attachFragment(holder, tagId, fragmentInAdapter);
        }
    }

    private void attachFragment(final FragmentViewHolder holder, final int tagId, final Fragment fragmentInAdapter) {
        holder.itemView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                v.removeOnAttachStateChangeListener(this);
                if (mCurTransaction == null) {
                    mCurTransaction = mFragmentManager.beginTransaction();
                }
                mCurTransaction.replace(holder.itemView.getId(), fragmentInAdapter, tagId + "");
                mCurTransaction.commitAllowingStateLoss();
                mCurTransaction = null;
                mFragmentManager.executePendingTransactions();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {

            }
        });
    }

    protected int genTagId(int position) {
        // itemId must not be zero
        long itemId = getItemId(position);
        if (itemId == RecyclerView.NO_ID) {
            return position + 1;
        } else {
            return (int) itemId;
        }
    }

    /**
     * Return the Fragment associated with a specified position.
     */
    public abstract Fragment getItem(int position, Fragment.SavedState savedState);


    public boolean isSameFragment(Fragment fragmentInView, Fragment fragmentInAdapter) {
        return fragmentInView == fragmentInAdapter;
    }

    public static class FragmentViewHolder extends RecyclerView.ViewHolder {

        public FragmentViewHolder(View itemView) {
            super(itemView);
        }
    }
}
