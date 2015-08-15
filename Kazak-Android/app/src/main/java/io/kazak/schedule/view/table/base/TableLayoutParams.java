package io.kazak.schedule.view.table.base;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.Contract;

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

    @Nullable
    @Contract("null->null")
    public static TableLayoutParams getFor(@Nullable View view) {
        return getFor(view, false);
    }

    @Nullable
    @Contract("null,false->null; _,true->!null")
    public static TableLayoutParams getFor(@Nullable View view, boolean createIfNull) {
        ViewGroup.LayoutParams lp = view == null ? null : view.getLayoutParams();
        if (lp instanceof TableLayoutParams) {
            return (TableLayoutParams) lp;
        } else if (createIfNull) {
            return new TableLayoutParams();
        } else {
            return null;
        }
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

    public boolean startsFirst() {
        return startsFirst;
    }

    void setStartsFirst(boolean startsFirst) {
        this.startsFirst = startsFirst;
    }

    public boolean endsLast() {
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
