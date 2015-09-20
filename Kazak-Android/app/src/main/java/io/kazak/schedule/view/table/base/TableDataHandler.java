package io.kazak.schedule.view.table.base;

import java.util.Comparator;

public interface TableDataHandler<ITEM, ROW, BOUND> extends Comparator<BOUND> {

    ROW getRowFor(ITEM item, int position, TableAdapterAbs<ITEM, ROW, BOUND, ?> adapter);

    BOUND getStartFor(ITEM item, int position, TableAdapterAbs<ITEM, ROW, BOUND, ?> adapter);

    BOUND getEndFor(ITEM item, int position, TableAdapterAbs<ITEM, ROW, BOUND, ?> adapter);

    boolean isPlaceholder(ITEM item, int position, TableAdapterAbs<ITEM, ROW, BOUND, ?> adapter);

    int getLength(BOUND start, BOUND end);

    BOUND sum(BOUND start, int units);

    String getLabelForRow(ROW row);

    String getLabelForBound(BOUND bound);

}
