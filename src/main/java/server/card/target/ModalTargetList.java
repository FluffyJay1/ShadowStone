package server.card.target;

import server.Board;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ModalTargetList extends TargetList<Integer> {
    public ModalTargetList(List<Integer> targeted) {
        this.targeted = targeted;
    }

    @Override
    public TargetList<Integer> clone() {
        return new ModalTargetList(new ArrayList<>(this.targeted));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName()).append(" ").append(this.targeted.size()).append(" ");
        for (Integer i : this.targeted) {
            sb.append(i).append(" ");
        }
        return sb.toString();
    }

    public static TargetList<Integer> fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        List<Integer> targeted = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            targeted.add(Integer.parseInt(st.nextToken()));
        }
        return new ModalTargetList(targeted);
    }
}
