package io.kazak.schedule.view.table.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class TableLayoutParams extends RecyclerView.LayoutParams {

    private boolean isFirstRow;
    private boolean isLastRow;
    private boolean startsFirst;
    private boolean endsLast;

    private boolean isPlaceholder;

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

    public boolean isFirstRow() {
        return isFirstRow;
    }

    void setIsFirstRow(boolean isFirstRow) {
        this.isFirstRow = isFirstRow;
    }

    public boolean isLastRow() {
        return isLastRow;
    }

    void setIsLastRow(boolean isLastRow) {
        this.isLastRow = isLastRow;
    }

    public boolean isStartsFirst() {
        return startsFirst;
    }

    void setStartsFirst(boolean startsFirst) {
        this.startsFirst = startsFirst;
    }

    public boolean isEndsLast() {
        return endsLast;
    }

    void setEndsLast(boolean endsLast) {
        this.endsLast = endsLast;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    void setIsPlaceholder(boolean isPlaceholder) {
        this.isPlaceholder = isPlaceholder;
    }

}
