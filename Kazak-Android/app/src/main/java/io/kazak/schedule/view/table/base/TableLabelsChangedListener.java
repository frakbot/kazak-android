package io.kazak.schedule.view.table.base;

import android.support.annotation.NonNull;

import java.util.List;

public interface TableLabelsChangedListener {

    // flat model, can't be changed
    @SuppressWarnings("checkstyle:parameternumber")
    void onTableLabelsChanged(
            @NonNull List<String> rowsLabels, int firstRowIndex, int firstRowPositionY, int rowHeight,
            @NonNull List<String> boundsLabels, int firstBoundIndex, int firstBoundPositionX, int boundsSpacing);

}
