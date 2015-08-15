package io.kazak.schedule.view.table.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class TableLayoutParams extends RecyclerView.LayoutParams {

    boolean isFirstRow;
    boolean isLastRow;
    boolean startsFirst;
    boolean endsLast;

    boolean isPlaceholder;

    public TableLayoutParams() {
        super(WRAP_CONTENT, WRAP_CONTENT);
    }

    public TableLayoutParams(ViewGroup.LayoutParams lp) {
        this();
        if (lp instanceof TableLayoutParams) {
            TableLayoutParams tlp = (TableLayoutParams) lp;
            startsFirst = tlp.startsFirst;
            isFirstRow = tlp.isFirstRow;
            endsLast = tlp.endsLast;
            isLastRow = tlp.isLastRow;
            isPlaceholder = tlp.isPlaceholder;
        }
    }

    @NonNull
    public static TableLayoutParams getOrCreateFor(@Nullable View view) {
        TableLayoutParams lp = getFor(view);
        return lp != null ? lp : new TableLayoutParams();
    }

    @Nullable
    public static TableLayoutParams getFor(@Nullable View view) {
        ViewGroup.LayoutParams lp = view == null ? null : view.getLayoutParams();
        return !(lp instanceof TableLayoutParams) ? null : (TableLayoutParams) lp;
    }

}
