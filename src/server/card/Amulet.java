package server.card;

import server.Board;

public class Amulet extends BoardObject {
	public Amulet(Board b, CardStatus status, int cost, String name, String text, String imagepath, int team, int id) {
		super(b, status, cost, name, text, imagepath, team, id);
	}
}
