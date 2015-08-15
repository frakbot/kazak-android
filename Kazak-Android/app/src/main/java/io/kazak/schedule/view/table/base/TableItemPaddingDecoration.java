package io.kazak.schedule.view.table.base;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class TableItemPaddingDecoration extends RecyclerView.ItemDecoration {

    private final int horizontalPadding;
    private final int verticalPadding;

    public TableItemPaddingDecoration(int spacing) {
        this(spacing, spacing);
    }

    public TableItemPaddingDecoration(int horizontalPadding, int verticalPadding) {
        this.horizontalPadding = horizontalPadding;
        this.verticalPadding = verticalPadding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = verticalPadding;
        outRect.bottom = verticalPadding;
        outRect.left = horizontalPadding;
        outRect.right = horizontalPadding;

        TableLayoutParams tlp = TableLayoutParams.getFor(view);
        if (tlp != null && tlp.isPlaceholder()) {
            getPlaceholderOffsets(outRect, parent, tlp);
        }
    }

    private void getPlaceholderOffsets(Rect outRect, RecyclerView parent, TableLayoutParams tlp) {
        // if an item is a placeholder, it will extends from its bounds (negative padding) until the next element or the end of the parent
        outRect.left *= -1;
        outRect.right *= -1;
        if (tlp.isStartsFirst() || tlp.isEndsLast()) {
            int extraHorizontalPadding = 0;
            RecyclerView.LayoutManager lm = parent.getLayoutManager();
            if (lm instanceof TableLayoutManager) {
                TableLayoutManager tlm = (TableLayoutManager) lm;
                extraHorizontalPadding = tlm.getExtraHorizontalPadding();
            }
            boolean drawInPadding = !lm.getClipToPadding();
            if (tlp.isStartsFirst()) {
                outRect.left = -extraHorizontalPadding;
                if (drawInPadding) {
                    outRect.left -= parent.getPaddingLeft();
                }
            }
            if (tlp.isEndsLast()) {
                outRect.right = -extraHorizontalPadding;
                if (drawInPadding) {
                    outRect.left -= parent.getPaddingRight();
                }
            }
        }
    }

}
