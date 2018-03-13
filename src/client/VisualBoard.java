package client;

import java.util.*;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;

import server.Board;
import server.card.BoardObject;
import utils.DefaultMouseListener;

public class VisualBoard extends Board implements DefaultMouseListener {

	public VisualBoard() {
		super();
	}

	public void update(double frametime) {

	}

	public void draw(Graphics g) {
		for (int i = 1; i < player1side.size(); i++) {
			BoardObject bo = player1side.get(i);
			bo.pos.set((int) (((bo.position - 2) - (player1side.size() - 2) / 2.) * 300 + 960), 400);
			bo.draw(g);
		}
		for (int i = 1; i < player2side.size(); i++) {
			BoardObject bo = player2side.get(i);
			bo.pos.set((int) (((-bo.position - 2) - (player2side.size() - 2) / 2.) * 300 + 960), 700);
			bo.draw(g);
		}
		BoardObject player1leader = player1side.get(0);
		BoardObject player2leader = player2side.get(0);
		player1leader.pos.set(960, 100);
		player2leader.pos.set(960, 1000);
		player1leader.draw(g);
		player2leader.draw(g);
	}

	public void playerTurnUpdate(double frametime) {

	}
}
