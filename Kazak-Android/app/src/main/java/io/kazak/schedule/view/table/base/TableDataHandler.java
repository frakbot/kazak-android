package io.kazak.schedule.view.table.base;

import java.util.Comparator;

public interface TableDataHandler<ITEM, ROW, BOUND> extends Comparator<BOUND> {

    ROW getRowFor(ITEM item);

    BOUND getStartFor(ITEM item);

    BOUND getEndFor(ITEM item);

    int getLength(BOUND start, BOUND end);

    BOUND sum(BOUND start, int units);

    boolean isPlaceholder(ITEM item);

    String getLabelForRow(ROW row);

    String getLabelForBound(BOUND bound);

}
