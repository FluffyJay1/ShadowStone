package server;

import java.util.ArrayList;
import java.util.LinkedList;

import server.card.BoardObject;
import server.card.Leader;
import server.card.Minion;
import server.event.*;

public class Board {
    Player player1, player2;
    protected ArrayList<BoardObject> player1side;
    protected ArrayList<BoardObject> player2side;
    public LinkedList<Event> eventlist;

    public Board() {
	player1side = new ArrayList<BoardObject>();
	player2side = new ArrayList<BoardObject>();
	eventlist = new LinkedList<Event>();
	player1side.add(new Leader(this, "Rowen", "The Memer", 1));
	player2side.add(new Leader(this, "Nower", "Remem eht", -1));
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
	bo.team = position > 0 ? 1 : -1;
	relevantSide.add(pos, bo);
	for (int i = pos + 1; i < relevantSide.size(); i++) {
	    relevantSide.get(i).position++;
	}
    }

    public void addBoardObjectToSide(BoardObject bo, int side) {
	bo.team = side;
	if (side > 0) {
	    addBoardObject(bo, player1side.size() + 1);
	}
	if (side < 0) {
	    addBoardObject(bo, -player2side.size() - 1);
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
		relevantSide.get(i).position += position > 0 ? -1 : 1;
	    }
	}
    }

    public void updatePositions() {
	for (int i = 0; i < player1side.size(); i++) {
	    player1side.get(i).position = i + 1;
	}
	for (int i = 0; i < player2side.size(); i++) {
	    player2side.get(i).position = -i - 1;
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
	    String eventstring = eventlist.getFirst().resolve(eventlist, false);
	    if (!eventstring.isEmpty()) {
		System.out.println(eventstring);
		System.out.println(this.stateToString());
	    }
	    eventlist.removeFirst();
	}
    }
}
