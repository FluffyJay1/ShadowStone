package utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PendingListManagerTest {
    /**
     * | denotes where the user has queued playing cards
     */

    @Test
    void PendingPlayTest() {
        // comments indicate current state at the line
        PendingListManager<String> plm = new PendingListManager<>();
        PendingListManager.Processor<String> plmp = plm.getProducer();
        List<String> base = new ArrayList<>();
        plm.trackConsumerState(() -> base);
        base.add("a");
        // cb: a
        plmp.processOp(1, null, true);
        // cb: a b
        assertEquals(1, plm.realToPending(2));
        plmp.processOp(2, "|", true);
        // cb: a b |
        // vb: a |
        plmp.processOp(3, null, true);
        // cb: a b | c
        // vb: a |
        assertEquals(1, plm.pendingToBase(2));
        assertEquals(4, plm.baseToReal(1));
        assertEquals(4, plm.pendingToReal(2));
        plmp.processOp(3, "||", true);
        // cb: a b | c ||
        // vb: a | ||
        assertEquals(3, plm.baseToPending(1));
        plmp.processOp(2, null, false);
        // cb: a b c ||
        // vb: a | ||
        plmp.processOp(3, null, false);
        // cb: a b c
        // vb: a | ||
        assertEquals(3, plm.pendingToReal(3));
        assertEquals(List.of("a", "|", "||"), plm.getConsumerStateWithPending());
    }

    @Test
    void ConsumeOpTest() {
        PendingListManager<String> plm = new PendingListManager<>();
        PendingListManager.Processor<String> plmp = plm.getProducer();
        List<String> base = new ArrayList<>();
        plm.trackConsumerState(() -> base);
        base.add("a");
        // cb: a
        // vb: a
        // bs: a
        plmp.processOp(1, null, true);
        // cb: a b
        assertEquals(2, plm.pendingToReal(1));
        plmp.processOp(2, "|", true);
        // cb: a b |
        // vb: a |
        // bs: a
        assertEquals(1, plm.pendingToReal(1));
        assertEquals(1, plm.realToPending(2));
        plmp.processOp(2, "||", true);
        // cb: a b || |
        // vb: a || |
        // bs: a
        assertEquals(0, plm.pendingToReal(0));
        plmp.processOp(0, "|||", true);
        // cb: ||| a b || |
        // vb: ||| a || |
        // bs: a
        assertEquals(List.of("|||", "a", "||", "|"), plm.getConsumerStateWithPending());

        PendingListManager.Processor<String> plmc = plm.getConsumer();
        base.add("b");
        plmc.processOp(1, null, true);
        // cb: ||| a b || |
        // vb: ||| a b || |
        // bs: a b
        assertEquals(List.of("|||", "a", "b", "||", "|"), plm.getConsumerStateWithPending());

        base.add(2, "|");
        plmc.processOp(2, "|", true);
        // bs: a b |
        assertEquals(List.of("|||", "a", "b", "||", "|"), plm.getConsumerStateWithPending());

        base.add(2, "||");
        plmc.processOp(2, "||", true);
        // bs: a b || |
        assertEquals(List.of("|||", "a", "b", "||", "|"), plm.getConsumerStateWithPending());

        base.add(0, "|||");
        plmc.processOp(0, "|||", true);
        // bs: ||| a b || |
        assertEquals(List.of("|||", "a", "b", "||", "|"), plm.getConsumerStateWithPending());
    }

    @Test
    void PendingToRealTest() {
        PendingListManager<String> plm = new PendingListManager<>();
        PendingListManager.Processor<String> plmp = plm.getProducer();
        List<String> base = new ArrayList<>();
        plm.trackConsumerState(() -> base);
        base.add("a");
        // cb: a
        // vb: a
        plmp.processOp(1, "w", true);
        // cb: a w
        // vb: a w
        plmp.processOp(2, null, true);
        // cb: a w k
        // vb: a w
        plmp.processOp(1, null, true);
        // cb: a k w k
        // vb: a w
        // try to place vb: a | w
        // should result in a k | w k
        assertEquals(2, plm.pendingToReal(1));
        // try to place vb: a w |
        // should result in a k w k |
        assertEquals(4, plm.pendingToReal(2));
    }

    @Test
    void PendingToRealRemoveConsumeTest() {
        PendingListManager<String> plm = new PendingListManager<>();
        PendingListManager.Processor<String> plmp = plm.getProducer();
        List<String> base = new ArrayList<>();
        plm.trackConsumerState(() -> base);
        base.add("a");
        // cb: a
        // vb: a
        // bs: a
        plmp.processOp(0, null, false);
        // cb:
        // vb: a
        assertEquals(0, plm.pendingToReal(0));
        plmp.processOp(0, "|", true);
        // cb: |
        // vb: | a
        assertEquals(1, plm.pendingToReal(1));
        plmp.processOp(1, "||", true);
        // cb: | ||
        // vb: | || a
        assertEquals(List.of("|", "||", "a"), plm.getConsumerStateWithPending());

        PendingListManager.Processor<String> plmc = plm.getConsumer();
        base.remove(0);
        plmc.processOp(0, null, false);
        // cb: | ||
        // vb: | ||
        // bs:
        assertEquals(List.of("|", "||"), plm.getConsumerStateWithPending());

        base.add(0, "|");
        plmc.processOp(0, "|", true);
        // vb: | ||
        // bs: |
        assertEquals(List.of("|", "||"), plm.getConsumerStateWithPending());

        base.add(1, "||");
        plmc.processOp(1, "||", true);
        // vb: | ||
        // bs: | ||
        assertEquals(List.of("|", "||"), plm.getConsumerStateWithPending());
    }

    @Test
    void RemoveOpTest() {
        PendingListManager<String> plm = new PendingListManager<>();
        PendingListManager.Processor<String> plmp = plm.getProducer();
        List<String> base = new ArrayList<>(List.of("a", "b"));
        plm.trackConsumerState(() -> base);
        // cb: a b
        // vb: a b
        // bs: a b
        plmp.processOp(0, null, false);
        // cb: b
        // vb: a b
        // bs: a b
        plmp.processOp(0, "|", true);
        // cb: | b
        // vb: | a b
        // bs: a b
        plmp.processOp(0, "||", true);
        // cb: || | b
        // vb: || | a b
        // bs: a b
        assertEquals(List.of("||", "|", "a", "b"), plm.getConsumerStateWithPending());

        PendingListManager.Processor<String> plmc = plm.getConsumer();
        base.remove(0);
        plmc.processOp(0, null, false);
        // vb: || | b
        // bs: b
        assertEquals(List.of("||", "|", "b"), plm.getConsumerStateWithPending());
        base.add(0, "|");
        plmc.processOp(0, "|", true);
        // bs: | b
        assertEquals(List.of("||", "|", "b"), plm.getConsumerStateWithPending());
        base.add(0, "||");
        plmc.processOp(0, "||", true);
        // bs: || | b
        assertEquals(List.of("||", "|", "b"), plm.getConsumerStateWithPending());
        assertEquals(List.of("||", "|", "b"), base);
    }

    @Test
    void RemoveOpTestReverse() {
        PendingListManager<String> plm = new PendingListManager<>();
        PendingListManager.Processor<String> plmp = plm.getProducer();
        List<String> base = new ArrayList<>(List.of("a", "b"));
        plm.trackConsumerState(() -> base);
        // cb: a b
        // vb: a b
        // bs: a b
        plmp.processOp(0, null, false);
        // cb: b
        // vb: a b
        // bs: a b
        plmp.processOp(0, "|", true);
        // cb: | b
        // vb: | a b
        // bs: a b
        assertEquals(1, plm.pendingToReal(1));
        assertEquals(1, plm.pendingToReal(2));
        plmp.processOp(1, "||", true);
        // cb: | || b
        // vb: | || a b
        // bs: a b
        assertEquals(List.of("|", "||", "a", "b"), plm.getConsumerStateWithPending());

        PendingListManager.Processor<String> plmc = plm.getConsumer();
        base.remove(0);
        plmc.processOp(0, null, false);
        // vb: | || b
        // bs: b
        assertEquals(List.of("|", "||", "b"), plm.getConsumerStateWithPending());
        base.add(0, "|");
        plmc.processOp(0, "|", true);
        // bs: | b
        assertEquals(List.of("|", "||", "b"), plm.getConsumerStateWithPending());
        base.add(1, "||");
        plmc.processOp(1, "||", true);
        // bs: | || b
        assertEquals(List.of("|", "||", "b"), plm.getConsumerStateWithPending());
        assertEquals(List.of("|", "||", "b"), base);
    }
}
