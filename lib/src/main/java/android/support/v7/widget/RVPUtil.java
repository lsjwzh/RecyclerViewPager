package android.support.v7.widget;

public class RVPUtil {
    public static boolean isLayoutFrozen(RecyclerView recyclerView) {
        return recyclerView.mLayoutFrozen;
    }

    public static void setScrollState(RecyclerView recyclerView, int scrollState) {
        recyclerView.setScrollState(scrollState);
    }
}
