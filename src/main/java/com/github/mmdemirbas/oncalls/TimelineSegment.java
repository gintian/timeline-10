package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

import static com.github.mmdemirbas.oncalls.Utils.reduce;
import static com.github.mmdemirbas.oncalls.Utils.sorted;
import static java.util.Collections.emptyList;

/**
 * Represent a finite interval of a {@link Timeline}. Used to merge multiple timelines and query details.
 */
public interface TimelineSegment<C extends Comparable<? super C>, V> {
    /**
     * Merges this and other {@code segments} into one {@link TimelineSegment} using the provided {@code mergeFunction}.
     *
     * @param <A> value type of the other segments
     */
    default <A> TimelineSegment<C, V> mergeWith(List<TimelineSegment<C, A>> segments, Reducer<V, A> mergeFunction) {
        return reduce(this, segments, (acc, segment) -> {
            List<ValuedRange<C, V>> valuedRanges = new ArrayList<>();
            List<V>                 values       = emptyList();
            C                       start        = null;
            C                       end          = null;

            for (C point : sorted(acc.getKeyPoints(), segment.getKeyPoints())) {
                end = point;
                List<V> mergedValues = mergeFunction.merge(acc.findCurrentValues(point),
                                                           segment.findCurrentValues(point));
                if (!values.equals(mergedValues)) {
                    if (!values.isEmpty()) {
                        Range<C> range = Range.of(start, end);
                        values.forEach(value -> valuedRanges.add(new ValuedRange<>(range, value)));
                    }
                    values = mergedValues;
                    start = end;
                }
            }

            if (!values.isEmpty()) {
                Range<C> range = Range.of(start, end);
                values.forEach(value -> valuedRanges.add(new ValuedRange<>(range, value)));
            }
            return newSegment(valuedRanges);
        });
    }

    /**
     * Creates a new {@link TimelineSegment} instance from the given {@code valuedRanges}.
     */
    TimelineSegment<C, V> newSegment(List<ValuedRange<C, V>> valuedRanges);

    /**
     * Returns a set of change points. In other words all start and end points of sub-intervals.
     */
    Set<C> getKeyPoints();

    /**
     * Returns values of the interval containing the specified {@code point}.
     */
    List<V> findCurrentValues(C point);

    /**
     * Returns the interval containing the specified {@code point}.
     */
    ValuedRange<C, List<V>> findCurrentInterval(C point);

    /**
     * Returns the interval coming just after the interval containing the specified {@code point}.
     */
    ValuedRange<C, List<V>> findNextInterval(C point);

    /**
     * Returns an interval map representation.
     */
    NavigableMap<C, List<V>> toIntervalMap();
}
