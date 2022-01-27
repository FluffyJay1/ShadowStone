package server.card.target;

import java.util.ArrayList;
import java.util.List;

public class ModalTargetList extends TargetList<Integer> {
    public ModalTargetList(List<Integer> targeted) {
        this.targeted = targeted;
    }

    @Override
    public TargetList<Integer> clone() {
        return new ModalTargetList(new ArrayList<>(this.targeted));
    }
}
