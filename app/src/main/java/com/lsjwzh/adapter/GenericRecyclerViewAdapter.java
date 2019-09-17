package com.lsjwzh.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public abstract class GenericRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder> extends
    RecyclerView.Adapter<VH> implements OnItemChangedListener {

  private List<T> mDataSource = new ArrayList<T>();
  private OnRecyclerViewItemClickListener<VH> mOnItemClickListener;

  public GenericRecyclerViewAdapter() {

  }

  public GenericRecyclerViewAdapter(@NonNull T[] arrays) {
    if (arrays == null) {
      throw new IllegalArgumentException("don't pass null in");
    }
    mDataSource.addAll(Arrays.asList(arrays));
  }

  public GenericRecyclerViewAdapter(@NonNull Collection<T> collection) {
    if (collection == null) {
      throw new IllegalArgumentException("don't pass null in");
    }
    mDataSource.addAll(collection);
  }

  public GenericRecyclerViewAdapter(@NonNull List<T> list) {
    if (list == null) {
      throw new IllegalArgumentException("don't pass null in");
    }
    mDataSource = list;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public int getItemCount() {
    return mDataSource.size();
  }

  public @Nullable T getItem(int position) {
    return (position < 0 || position >= mDataSource.size()) ? null : mDataSource.get(position);
  }

  @Override
  public void onBindViewHolder(final VH holder, final int position) {
    if (holder.itemView != null) {
      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, position, holder);
          }
        }
      });
    }
  }

  public int indexOf(T item) {
    return mDataSource.indexOf(item);
  }

  public GenericRecyclerViewAdapter<T, VH> add(@NonNull T item) {
    mDataSource.add(item);
    if (mDataSource.size() > 0) {
      notifyItemInserted(mDataSource.size() - 1);
    }
    return this;
  }

  public GenericRecyclerViewAdapter<T, VH> set(int index, @NonNull T item) {
    mDataSource.set(index, item);
    notifyItemChanged(index);
    return this;
  }

  public GenericRecyclerViewAdapter<T, VH> addAll(@NonNull T[] items) {
    int startIndex = mDataSource.size() > 0 ? mDataSource.size() - 1 : 0;
    mDataSource.addAll(Arrays.asList(items));
    if (startIndex >= 0) {
      notifyItemRangeInserted(startIndex, items.length);
    }
    return this;
  }

  public GenericRecyclerViewAdapter<T, VH> addAll(@NonNull Collection<T> items) {
    int startIndex = mDataSource.size() > 0 ? mDataSource.size() - 1 : 0;
    mDataSource.addAll(items);
    if (startIndex >= 0) {
      notifyItemRangeInserted(startIndex, items.size());
    }
    return this;
  }

  public GenericRecyclerViewAdapter<T, VH> add(int position, @NonNull T item) {
    mDataSource.add(position, item);
    notifyItemInserted(position);
    return this;
  }

  public GenericRecyclerViewAdapter<T, VH> remove(int position) {
    mDataSource.remove(position);
    // 规避RV在position==0时可能出现的bug
    if (position == 0) {
      notifyDataSetChanged();
    } else if (position > 0) {
      notifyItemRemoved(position);
    }
    return this;
  }

  public GenericRecyclerViewAdapter<T, VH> remove(@NonNull T item) {
    int position = mDataSource.indexOf(item);
    mDataSource.remove(item);
    // 规避RV在position==0时可能出现的bug
    if (position == 0) {
      notifyDataSetChanged();
    } else if (position > 0) {
      notifyItemRemoved(position);
    }
    return this;
  }

  public GenericRecyclerViewAdapter<T, VH> clear() {
    final int size = mDataSource.size();
    internalClear();
    notifyItemRangeRemoved(0, size);
    return this;
  }

  @Override
  public void onItemDismiss(int position) {
    remove(position);
  }

  @Override
  public boolean onItemMove(int fromPosition, int toPosition) {
    if (fromPosition < toPosition) {
      for (int i = fromPosition; i < toPosition; i++) {
        Collections.swap(mDataSource, i, i + 1);
      }
    } else {
      for (int i = fromPosition; i > toPosition; i--) {
        Collections.swap(mDataSource, i, i - 1);
      }
    }
    notifyItemMoved(fromPosition, toPosition);
    return true;
  }

  protected void internalClear() {
    mDataSource.clear();
  }

  public OnRecyclerViewItemClickListener getOnItemClickListener() {
    return mOnItemClickListener;
  }

  public GenericRecyclerViewAdapter<T, VH> setOnItemClickListener(
      OnRecyclerViewItemClickListener<VH> itemClickListener) {
    mOnItemClickListener = itemClickListener;
    return this;
  }

}
