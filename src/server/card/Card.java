package server.card;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import server.Board;

public class Card {
	public Board board;
	public int id, cost;
	public String name, text, imagepath;
	public Vector2f pos;
	double scale;
	Image image;

	public Card() {
		this.cost = 1;
	}

	public Card(Board board, int cost, String name, String text, String imagepath, int id) {
		this.board = board;
		this.cost = cost;
		this.name = name;
		this.text = text;
		this.image = Game.getImage(imagepath).getScaledCopy(192, 256);
		this.imagepath = imagepath;
		this.pos = new Vector2f();
		this.scale = 1;
		this.id = id;
	}

	public void draw(Graphics g) {
		Image scaledCopy = this.image.getScaledCopy((float) this.scale);
		g.drawImage(scaledCopy, this.pos.x - scaledCopy.getWidth() / 2, this.pos.y - scaledCopy.getHeight() / 2);
	}

	public boolean isInside(Vector2f p) {
		return p.x >= this.pos.x - this.image.getWidth() * this.scale
				&& p.y >= this.pos.y - this.image.getHeight() * this.scale
				&& p.x <= this.pos.x + this.image.getWidth() * this.scale
				&& p.y <= this.pos.y + this.image.getHeight() * this.scale;
	}

	public String toString() {
		return "card " + this.id;
	}
}
