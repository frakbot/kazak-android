package io.kazak.schedule.view.table.base;

import android.graphics.Rect;
import android.support.annotation.NonNull;

import java.util.List;

public interface Ruler {

    void onLabelsChanged(@NonNull List<String> newLabels, int newFirstIndex, int newFirstPositionPx, int newTicksSpacingPx);

    void getBoundsOnScreen(@NonNull Rect bounds);

}
