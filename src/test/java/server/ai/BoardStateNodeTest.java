package server.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import utils.WeightedSampler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BoardStateNodeTest {
    @Mock
    WeightedSampler<String> mockSampler;

    @Test
    void BoardStateNodeUpdateTest() {
        when(mockSampler.sample()).thenReturn(List.of("a", "b", "c"));
        when(mockSampler.size()).thenReturn(3);
        // b1 -> b2 -> b3
        DeterministicBoardStateNode b1 = new DeterministicBoardStateNode(1, 1, "", mockSampler);
        DeterministicBoardStateNode b2 = Mockito.spy(new DeterministicBoardStateNode(1, 2, "", mockSampler));
        DeterministicBoardStateNode b3 = Mockito.spy(new DeterministicBoardStateNode(1, 3, "", mockSampler));
        b2.logEvaluation("a", b3);
        b1.logEvaluation("a", b2);
        // see if b1 is what we expect
        assertEquals(3, b1.getScore());
        // it should have queried b2 and b3
        Mockito.verify(b2).getMax();
        Mockito.verify(b3).getMax();
        // updating b1 should re-query b2, but b2 doesn't have to re-query b3
        b1.logEvaluation("b", new TerminalBoardStateNode(1, -1));
        b1.getMax();
        Mockito.verify(b2, times(2)).getMax();
        Mockito.verify(b3, times(1)).getMax();
        // updating b3 should make querying b1 eventually query b3 again
        b3.logEvaluation("b", new TerminalBoardStateNode(1, -1));
        b1.getMax();
        Mockito.verify(b3, times(2)).getMax();
    }

    @Test
    void RNGBoardStateNodeTrialTest() {
        RNGBoardStateNode rbsn = new RNGBoardStateNode(1);
        rbsn.addTrial(new TerminalBoardStateNode(1, 10));
        assertEquals(10, rbsn.getScore());
        rbsn.addTrial(new TerminalBoardStateNode(-1, -20));
        assertEquals(15, rbsn.getScore());
        rbsn.addTrial(new TerminalBoardStateNode(1, 30));
        assertEquals(20, rbsn.getScore());
    }
}
