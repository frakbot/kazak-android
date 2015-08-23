package io.kazak.schedule.view.table.base;

import java.util.Comparator;

import io.kazak.model.ScheduleBound;
import io.kazak.model.ScheduleItem;
import io.kazak.model.ScheduleRow;

public interface TableDataHandler extends Comparator<ScheduleBound> {

    ScheduleRow getRowFor(ScheduleItem item);

    ScheduleBound getStartFor(ScheduleItem item);

    ScheduleBound getEndFor(ScheduleItem item);

    int getLength(ScheduleBound start, ScheduleBound end);

    ScheduleBound sum(ScheduleBound start, int units);

    boolean isPlaceholder(ScheduleItem item);

}
