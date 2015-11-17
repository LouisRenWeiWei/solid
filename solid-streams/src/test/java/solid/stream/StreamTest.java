package solid.stream;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import solid.converters.AccumulateTest;
import solid.converters.FoldTest;
import solid.converters.ReduceTest;
import solid.converters.ToFirstTest;
import solid.converters.ToLastTest;
import solid.converters.ToListTest;
import solid.filters.DistinctFilterTest;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static solid.stream.Stream.stream;
import static test_utils.AssertIterableEquals.assertIterableEquals;

public class StreamTest {

    @Test
    public void testStreamOfArray() throws Exception {
        assertIterableEquals(asList(1, 2, 3), stream(asList(1, 2, 3).toArray(new Integer[3])));
        assertIterableEquals(Collections.emptyList(), stream(new Object[]{}));
        assertIterableEquals(asList(null, null), stream(new Object[]{null, null}));
    }

    @Test
    public void testStreamOfIterator() throws Exception {
        assertIterableEquals(asList(1, 2, 3), stream(asList(1, 2, 3)));
        assertIterableEquals(Collections.emptyList(), stream(Collections.emptyList()));
        assertIterableEquals(asList(null, null), stream(asList(null, null)));
    }

    @Test
    public void testOfSingle() throws Exception {
        assertIterableEquals(singletonList(1), Stream.of(1));
    }

    @Test
    public void testOfVararg() throws Exception {
        assertIterableEquals(asList(1, null, 2), Stream.of(1, null, 2));
        assertIterableEquals(emptyList(), Stream.of());
    }

    @Test
    public void testToList() throws Exception {
        assertIterableEquals(asList(1, 2, 3), stream(asList(1, 2, 3)).toList());
        assertIterableEquals(asList(1, 2, 3), stream(asList(1, 2, 3)).toList(10));
        new ToListTest().testToList();
        new ToListTest().testNewAndCall();
    }

    @Test
    public void testLift() throws Exception {
        assertIterableEquals(asList(1, 3, 6), stream(asList(1, 2, 3))
            .compose(value -> new Stream<Integer>() {
                @Override
                public Iterator<Integer> iterator() {
                    return new ReadOnlyIterator<Integer>() {

                        Iterator<Integer> source = value.iterator();
                        int count;

                        @Override
                        public boolean hasNext() {
                            return source.hasNext();
                        }

                        @Override
                        public Integer next() {
                            return count += source.next();
                        }
                    };
                }
            }));
    }

    @Test
    public void testMap() throws Exception {
        assertIterableEquals(asList("1", "2", "3"), stream(asList(1, 2, 3)).map(value -> value.toString()));
        new MapTest().testIterator();
    }

    @Test
    public void testFlatMap() throws Exception {
        assertTrue(stream(asList(1, 2, 3)).flatMap(value -> null) instanceof FlatMap);
        new FlatMapTest().testIterator();
    }

    @Test
    public void testFilter() throws Exception {
        assertIterableEquals(asList(1, 2, 3), stream(asList(1, 2, 3, 4)).filter(value -> value != 4));
        new FilterTest().testIterator();
    }

    @Test
    public void testWithout() throws Exception {
        assertIterableEquals(asList(1, 2, 3), stream(asList(1, 2, 3, 4)).without(4));
    }

    @Test
    public void testWith() throws Exception {
        assertIterableEquals(asList(1, 2, 3), stream(asList(1, 2)).with(3));
    }

    @Test
    public void testMerge() throws Exception {
        assertIterableEquals(asList(1, 2, 3, 4), stream(asList(1, 2)).merge(asList(3, 4)));
        new MergeTest().testIterator();
    }

    @Test
    public void testSeparate() throws Exception {
        assertIterableEquals(asList(1, 2, 3), stream(asList(0, 1, 4, 5, 6, 2, 3, null)).separate(asList(0, 4, 5, 6, null)));
    }

    @Test
    public void testTake() throws Exception {
        assertTrue(stream(asList(1, 2, 3)).take(2) instanceof Take);
        new TakeTest().testIterator();
    }

    @Test
    public void testSkip() throws Exception {
        assertTrue(stream(asList(1, 2, 3)).skip(2) instanceof Skip);
        new SkipTest().testIterator();
    }

    @Test
    public void testDistinct() throws Exception {
        assertIterableEquals(asList(1, 2, 3), stream(asList(1, 2, 3, 3, 3)).distinct());
        new DistinctFilterTest().testCall();
    }

    @Test
    public void testSort() throws Exception {
        assertIterableEquals(asList(1, 2, 3), stream(asList(3, 2, 1)).sorted((lhs, rhs) -> lhs < rhs ? -1 : (lhs.equals(rhs) ? 0 : 1)));
        assertIterableEquals(asList(1, 2, 3), Stream.of(3, 2, 1).sorted((lhs, rhs) -> lhs < rhs ? -1 : (lhs.equals(rhs) ? 0 : 1)));
        assertIterableEquals(asList(null, null), Stream.of(null, null).sorted((lhs, rhs) -> 0));
        assertIterableEquals(emptyList(), Stream.of().sorted((lhs, rhs) -> 0));
    }

    @Test
    public void testReverse() throws Exception {
        assertIterableEquals(asList(1, 2, 3), Stream.of(3, 2, 1).reverse());
        assertIterableEquals(asList(null, null), Stream.of(null, null).reverse());
        assertIterableEquals(singletonList(1), Stream.of(1));
        assertIterableEquals(emptyList(), Stream.of().reverse());
    }

    @Test
    public void testCollect() throws Exception {
        final ArrayList<Integer> target = new ArrayList<>();
        ArrayList<Integer> result = stream(asList(1, 2, 3)).collect(value -> {
            for (Integer v : value)
                target.add(v);
            return target;
        });
        assertTrue(target == result);
        assertEquals(target, result);
    }

    @Test
    public void testFold() throws Exception {
        Assert.assertEquals(
            (Integer) 10,
            Stream
                .of(2, 3, 4)
                .fold(1, (value1, value2) -> value1 + value2));
        new FoldTest().all();
    }

    @Test
    public void testReduce() throws Exception {
        Assert.assertEquals(
            (Integer) 9,
            Stream
                .of(2, 3, 4)
                .reduce((value1, value2) -> value1 + value2));
        new ReduceTest().all();
    }

    @Test
    public void testAccumulate() throws Exception {
        Assert.assertEquals(
            (Integer) 109,
            Stream
                .of(2, 3, 4)
                .accumulate(100, (value1, value2) -> value1 + value2));
        new AccumulateTest().all();
    }

    @Test
    public void testFirst() throws Exception {
        assertEquals(1, (int) stream(asList(1, 2, 3)).first(10));
        new ToFirstTest().testToFirst();
    }

    @Test
    public void testLast() throws Exception {
        assertEquals(3, (int) stream(asList(1, 2, 3)).last(10));
        new ToLastTest().testToLast();
    }

    @Test
    public void testCast() throws Exception {
        List<Integer> list = asList(1, 2, 3);
        List<Number> numbers = asList((Number) 1, 2, 3);
        assertIterableEquals(numbers, stream(list).cast(Number.class));
    }

    @Test(expected = ClassCastException.class)
    public void testCastException() throws Exception {
        List<Integer> numbers = asList(1, 2, 3);
        //noinspection unchecked
        assertIterableEquals(numbers, Stream.of("1").cast(Integer.class));
    }
}