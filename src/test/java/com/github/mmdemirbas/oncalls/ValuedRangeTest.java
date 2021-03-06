package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.mmdemirbas.oncalls.TestUtils.mapOf;
import static com.github.mmdemirbas.oncalls.TestUtils.pair;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ValuedRangeTest {
    @Test
    void buildIntervalMap_NoEvents() {
        assertIntervalMap(mapOf(), asList());
    }

    @Test
    void buildIntervalMap_SingleEvent_Empty() {
        assertIntervalMap(mapOf(), asList(interval(1, 1, "a")));
    }

    @Test
    void buildIntervalMap_SingleEvent_NonEmpty() {
        assertIntervalMap(mapOf(pair(1, asList("a")), pair(3, asList())), asList(interval(1, 3, "a")));
    }

    @Test
    void buildIntervalMap_DisjointEvents() {
        assertIntervalMap(mapOf(pair(1, asList("a")), pair(3, asList()), pair(5, asList("b")), pair(7, asList())),
                          asList(interval(1, 3, "a"), interval(5, 7, "b")));
    }

    @Test
    void buildIntervalMap_SuccessiveEvents() {
        assertIntervalMap(mapOf(pair(1, asList("a")), pair(3, asList("b")), pair(5, asList())),
                          asList(interval(1, 3, "a"), interval(3, 5, "b")));
    }

    @Test
    void buildIntervalMap_IntersectingEvents() {
        assertIntervalMap(mapOf(pair(1, asList("a")),
                                pair(3, asList("a", "b")),
                                pair(5, asList("b")),
                                pair(7, asList())), asList(interval(1, 5, "a"), interval(3, 7, "b")));
    }

    @Test
    void buildIntervalMap_OverlappingEvents() {
        assertIntervalMap(mapOf(pair(1, asList("a")),
                                pair(3, asList("a", "b")),
                                pair(5, asList("a")),
                                pair(7, asList())), asList(interval(1, 7, "a"), interval(3, 5, "b")));
    }

    @Test
    void buildIntervalMap_SuccessiveEventsWithSameValue() {
        assertIntervalMap(mapOf(pair(1, asList("a")), pair(7, asList())),
                          asList(interval(1, 3, "a"), interval(3, 5, "a"), interval(5, 7, "a")));
    }

    @Test
    void buildIntervalMap_SuccessiveEventsWithDifferentValue() {
        assertIntervalMap(mapOf(pair(1, asList("a")), pair(3, asList("b")), pair(5, asList("c")), pair(7, asList())),
                          asList(interval(1, 3, "a"), interval(3, 5, "b"), interval(5, 7, "c")));
    }

    private static void assertIntervalMap(Map<Integer, List<String>> expected,
                                          Collection<ValuedRange<Integer, String>> intervals) {
        assertEquals(expected, ValuedRange.buildIntervalMap(intervals));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static <C extends Comparable<? super C>, V> ValuedRange<C, V> interval(C startInclusive,
                                                                                   C endExclusive,
                                                                                   V value) {
        return ValuedRange.of(Range.of(startInclusive, endExclusive), value);
    }
}