package androidx.recyclerview.widget;

public abstract class ViewHolderDelegate {

    private ViewHolderDelegate() {
        throw new UnsupportedOperationException("no instances");
    }

    public static void setPosition(Object viewHolder, int position) {
        /*
         * accept viewHolder as object, because viewHolder is in another package when migrating
         * from android.support to androidx using Jitifier
         */
        ((RecyclerView.ViewHolder) viewHolder).mPosition = position;
    }
}
