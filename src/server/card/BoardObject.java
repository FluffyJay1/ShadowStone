package server.card;

import java.util.LinkedList;

import server.Board;
import server.event.Event;

public class BoardObject extends Card {
    public boolean alive;
    public int position;
    public BoardObject (Board b, int cost, String name, String text) {
	super(b, cost, name, text);
	this.alive = true;
	this.position = 0;
    }
    public LinkedList<Event> lastWords() {
	return new LinkedList<Event>();
    }
    public int getTeam() {
	if(this.position > 0) {
	    return 1;
	}
	if(this.position < 0) {
	    return -1;
	}
	return 0;
    }
    public String toString() {
	return "BoardObject " + name + " cost " + cost + " position " + position + " alive " + alive;
    }
}
