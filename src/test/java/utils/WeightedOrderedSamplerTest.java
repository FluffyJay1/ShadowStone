package utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeightedOrderedSamplerTest {
    @Test
    public void OrderedSampleTest() {
        WeightedOrderedSampler<String> sampler = new WeightedOrderedSampler<>();
        sampler.add("a", 1);
        sampler.add("c", 3);
        sampler.add("b", 2);
        assertEquals(List.of("c", "b", "a"), sampler.sample());
        sampler.add("d", 4);
        assertEquals(List.of("d", "c", "b", "a"), sampler.sample());
    }
}
