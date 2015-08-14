package io.kazak.schedule.view.table.base;

import java.util.Comparator;

public interface TableDataHandler<I, R, C> extends Comparator<C> {

    R getRowFrom(I item);

    C getStartFrom(I item);

    C getEndFrom(I item);

    int getLength(C start, C end);

    C sum(C start, int units);

    boolean isPlaceholder(I item);

}
