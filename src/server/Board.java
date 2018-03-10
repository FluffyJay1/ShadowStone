package server;

import java.util.ArrayList;
import java.util.LinkedList;

import server.card.BoardObject;
import server.card.minion.Leader;
import server.card.minion.Minion;
import server.event.*;

public class Board {
    ArrayList<BoardObject> player1side;
    ArrayList<BoardObject> player2side;
    LinkedList<Event> eventlist;

    public Board() {
	player1side = new ArrayList<BoardObject>();
	player2side = new ArrayList<BoardObject>();
	eventlist = new LinkedList<Event>();
	player1side.add(new Leader(this, "Rowen", "The Memer"));
	player2side.add(new Leader(this, "Nower", "Remem eht"));
    }

    public BoardObject getBoardObject(int position) {
	ArrayList<BoardObject> relevantSide = position > 0 ? player1side : player2side;
	int pos = position > 0 ? position - 1 : -position - 1;
	if (pos >= relevantSide.size()) {
	    return null;
	}
	return relevantSide.get(pos);
    }

    public void addBoardObject(BoardObject bo, int position) {
	ArrayList<BoardObject> relevantSide = position > 0 ? player1side : player2side;
	int pos = position > 0 ? position - 1 : -position - 1;
	if (pos > relevantSide.size()) {
	    pos = relevantSide.size();
	}
	if (pos == 0) { // top notch error handling
	    pos = 1;
	}
	bo.position = position;
	relevantSide.add(pos, bo);
	for (int i = pos + 1; i < relevantSide.size(); i++) {
	    relevantSide.get(i).position++;
	}
    }

    public void removeBoardObject(int position) {
	BoardObject bo;
	ArrayList<BoardObject> relevantSide = position > 0 ? player1side : player2side;
	int pos = position > 0 ? position - 1 : -position - 1;
	if (pos >= relevantSide.size()) {
	    return;
	}
	if (pos == 0) { // leader dies

	} else {
	    bo = relevantSide.remove(pos);
	    for (int i = pos; i < relevantSide.size(); i++) {
		relevantSide.get(i).position--;
	    }
	}
    }

    public void updatePositions() {
	for (int i = 0; i < player1side.size(); i++) {
	    player1side.get(i).position = i;
	}
	for (int i = 0; i < player2side.size(); i++) {
	    player2side.get(i).position = i;
	}
    }

    public String stateToString() {
	String ret = "State----------------------------+\nPlayer 1:\n";
	for (BoardObject b : player1side) {
	    ret += b.toString() + "\n";
	}
	ret += "\nPlayer 2:\n";
	for (BoardObject b : player2side) {
	    ret += b.toString() + "\n";
	}
	ret += "---------------------------------+\n";
	return ret;
    }

    public void resolveAll() {
	while (!eventlist.isEmpty()) {
	    eventlist.getFirst().resolve(eventlist, false);
	    String str = eventlist.getFirst().toString();
	    if (!str.isEmpty()) {
		System.out.println(str);
		System.out.println(this.stateToString());
	    }
	    eventlist.removeFirst();
	}
    }

    public static void main(String args[]) {
	Board b = new Board();
	Minion p1 = new Minion(b, 0, 3, 1, 5, "Gemstone Carapace", "");
	Minion p2 = new Minion(b, 0, 3, 1, 5, "Gemstone Carapace", "");
	b.addBoardObject(p1, 2);
	b.addBoardObject(p2, -2);
	System.out.println(b.stateToString());
	Event e = new EventMinionAttack(p1, p2);
	b.eventlist.add(e);
	b.resolveAll();
    }
}
