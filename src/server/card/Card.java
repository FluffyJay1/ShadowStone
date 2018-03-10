package server.card;

import server.Board;

public class Card {
    public Board board;
    public int cost;
    public String name, text;
    public Card() {
	this.cost = 1;
    }
    public Card(Board board, int cost, String name, String text) {
	this.board = board;
	this.cost = cost;
	this.name = name;
	this.text = text;
    }
}
