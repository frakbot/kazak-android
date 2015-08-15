package io.kazak.schedule.view.table.base;

import java.util.Comparator;

public interface TableDataHandler<ITEM, ROW, BOUND> extends Comparator<BOUND> {

    ROW getRowFrom(ITEM item);

    BOUND getStartFrom(ITEM item);

    BOUND getEndFrom(ITEM item);

    int getLength(BOUND start, BOUND end);

    BOUND sum(BOUND start, int units);

    boolean isPlaceholder(ITEM item);

}
