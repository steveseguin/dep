package com.jiangdg.usb;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeviceFilterTest {
    private DeviceFilter filter(boolean exclude) {
        return new DeviceFilter(1133, 2229, 14, 2, 1,
                "maker", "camera", "serial", exclude);
    }

    private DeviceFilter wildcardFilter(boolean exclude) {
        return new DeviceFilter(-1, -1, 14, -1, -1,
                null, null, null, exclude);
    }

    @Test
    public void identicalFiltersAreEqualAndHaveEqualHashes() {
        for (boolean exclude : new boolean[] {false, true}) {
            DeviceFilter first = filter(exclude);
            DeviceFilter copy = filter(exclude);
            assertTrue(first.equals(first));
            assertTrue(first.equals(copy));
            assertTrue(copy.equals(first));
            assertEquals(first.hashCode(), copy.hashCode());
        }
    }

    @Test
    public void wildcardFiltersUseValueEqualityRatherThanDeviceMatching() {
        DeviceFilter first = wildcardFilter(false);
        DeviceFilter copy = wildcardFilter(false);
        assertTrue(first.equals(first));
        assertTrue(first.equals(copy));
        assertTrue(copy.equals(first));
        assertEquals(first.hashCode(), copy.hashCode());
        assertFalse(first.equals(filter(false)));
        assertFalse(filter(false).equals(first));
    }

    @Test
    public void everyNumericWildcardCombinationSupportsEquality() {
        int[] specified = {1133, 2229, 14, 2, 1};
        for (int mask = 0; mask < 32; mask++) {
            int[] values = specified.clone();
            for (int field = 0; field < values.length; field++) {
                if ((mask & (1 << field)) != 0) values[field] = -1;
            }
            DeviceFilter first = new DeviceFilter(values[0], values[1], values[2],
                    values[3], values[4], null, null, null);
            DeviceFilter copy = new DeviceFilter(values[0], values[1], values[2],
                    values[3], values[4], null, null, null);
            assertTrue(first.equals(copy));
            assertTrue(copy.equals(first));
            assertEquals(first.hashCode(), copy.hashCode());
        }
    }

    @Test
    public void wildcardDeviceMatchingDoesNotImplyFilterValueEquality() {
        DeviceFilter anyCamera = wildcardFilter(false);
        DeviceFilter camera = new DeviceFilter(1133, 2229, 14, 2, 1, null, null, null);
        assertTrue(anyCamera.matches(camera));
        assertFalse(anyCamera.equals(camera));
        assertFalse(camera.equals(anyCamera));
    }

    @Test
    public void includeAndExcludeFiltersAreNotEqual() {
        assertFalse(filter(false).equals(filter(true)));
        assertFalse(filter(true).equals(filter(false)));
        assertFalse(wildcardFilter(false).equals(wildcardFilter(true)));
    }

    @Test
    public void singleRemovalCanRemoveTheSameWildcardInstance() {
        DeviceFilter original = wildcardFilter(false);
        List<DeviceFilter> filters = new ArrayList<>(Collections.singletonList(original));
        assertTrue(filters.remove(original));
        assertTrue(filters.isEmpty());
    }

    @Test
    public void batchRemovalKeepsTheOppositeExclusionRule() {
        DeviceFilter include = filter(false);
        DeviceFilter exclude = filter(true);
        List<DeviceFilter> filters = new ArrayList<>(Arrays.asList(include, exclude));
        assertTrue(filters.removeAll(new ArrayList<>(Collections.singletonList(filter(false)))));
        assertEquals(1, filters.size());
        assertTrue(filters.get(0) == exclude);
    }

    @Test
    public void batchRemovalCanRemoveEquivalentWildcardFilters() {
        List<DeviceFilter> filters = new ArrayList<>(Collections.singletonList(wildcardFilter(false)));
        assertTrue(filters.removeAll(new ArrayList<>(Collections.singletonList(wildcardFilter(false)))));
        assertTrue(filters.isEmpty());
    }

    @Test
    public void batchRemovalRemovesEveryEquivalentEntry() {
        DeviceFilter keep = wildcardFilter(true);
        List<DeviceFilter> filters = new ArrayList<>(Arrays.asList(
                wildcardFilter(false), keep, wildcardFilter(false)));
        List<DeviceFilter> removed = new ArrayList<>(Collections.singletonList(wildcardFilter(false)));
        assertTrue(filters.removeAll(removed));
        assertEquals(1, filters.size());
        assertTrue(filters.get(0) == keep);
    }

    @Test
    public void differentDescriptorsAndOtherObjectsAreNotEqual() {
        DeviceFilter original = filter(false);
        for (DeviceFilter other : new DeviceFilter[] {
                new DeviceFilter(1, 2229, 14, 2, 1, "maker", "camera", "serial"),
                new DeviceFilter(1133, 1, 14, 2, 1, "maker", "camera", "serial"),
                new DeviceFilter(1133, 2229, 1, 2, 1, "maker", "camera", "serial"),
                new DeviceFilter(1133, 2229, 14, 1, 1, "maker", "camera", "serial"),
                new DeviceFilter(1133, 2229, 14, 2, 2, "maker", "camera", "serial"),
                new DeviceFilter(1133, 2229, 14, 2, 1, null, "camera", "serial"),
                new DeviceFilter(1133, 2229, 14, 2, 1, "other", "camera", "serial"),
                new DeviceFilter(1133, 2229, 14, 2, 1, "maker", null, "serial"),
                new DeviceFilter(1133, 2229, 14, 2, 1, "maker", "camera", null),
                new DeviceFilter(1133, 2229, 14, 2, 1, "maker", "other", "serial"),
                new DeviceFilter(1133, 2229, 14, 2, 1, "maker", "camera", "other")}) {
            assertFalse(original.equals(other));
            assertFalse(other.equals(original));
        }
        assertFalse(original.equals(null));
        assertFalse(original.equals("camera"));
    }
}
