package io.kazak.schedule.view.table.base;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

public class RowsRuler extends Ruler {

    private static final float VERTICALLY_COUNTER_CLOCKWISE = -90f;

    public RowsRuler(Context context) {
        super(context);
    }

    public RowsRuler(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RowsRuler(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RowsRuler(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        RecyclerViewWrapper recyclerViewWrapper = getRecyclerViewWrapper();
        if (recyclerViewWrapper != null) {
            canvas.save();
            int rowHeight = recyclerViewWrapper.getRowHeight();
            float halRowHeight = (float) rowHeight / 2f;
            float halfWidth = (float) getWidth() / 2f;
            canvas.translate(halfWidth, halRowHeight + recyclerViewWrapper.getFirstRowPositionY());
            canvas.rotate(VERTICALLY_COUNTER_CLOCKWISE);
            for (String rowLabel : recyclerViewWrapper.getRowsLabels()) {
                drawVerticallyCenteredLabel(canvas, rowLabel);
                canvas.translate(-rowHeight, 0);
            }
            canvas.restore();
        }
    }

}
