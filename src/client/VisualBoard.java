package client;

import java.util.*;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.geom.Vector2f;

import server.Board;
import server.card.BoardObject;
import server.card.Leader;
import server.card.Minion;
import server.event.EventMinionAttack;
import utils.DefaultMouseListener;

public class VisualBoard extends Board implements DefaultMouseListener {
	BoardObject selectedBO;
	Minion attackingMinion;

	public VisualBoard() {
		super();
	}

	public void update(double frametime) {
		player1.update(frametime);
		for (BoardObject bo : player1side) {
			bo.update(frametime);
		}
		for (BoardObject bo : player2side) {
			bo.update(frametime);
		}
		this.resolveAll();
	}

	public void draw(Graphics g) {
		for (int i = 1; i < player1side.size(); i++) {
			BoardObject bo = player1side.get(i);
			bo.targetpos.set((int) (((bo.position - 2) - (player1side.size() - 2) / 2.) * 300 + 960), 700);
			bo.draw(g);
		}
		for (int i = 1; i < player2side.size(); i++) {
			BoardObject bo = player2side.get(i);
			bo.targetpos.set((int) (((-bo.position - 2) - (player2side.size() - 2) / 2.) * 300 + 960), 400);
			bo.draw(g);
		}
		BoardObject player1leader = player1side.get(0);
		BoardObject player2leader = player2side.get(0);
		player1leader.targetpos.set(960, 1000);
		player2leader.targetpos.set(960, 100);
		player1leader.draw(g);
		player2leader.draw(g);
		this.player1.draw(g);
	}

	public void playerTurnUpdate(double frametime) {

	}

	public BoardObject BOAtPos(Vector2f pos) {
		for (BoardObject bo : player1side) {
			if (bo.isInside(pos)) {
				return bo;
			}
		}
		for (BoardObject bo : player2side) {
			if (bo.isInside(pos)) {
				return bo;
			}
		}
		return null;
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// TODO Auto-generated method stub
		BoardObject bo = BOAtPos(new Vector2f(x, y));
		if (bo != null) {
			if (bo instanceof Minion && bo.team == 1) {
				this.attackingMinion = (Minion) bo;
				bo.scale = 1.5;
			}
			this.selectedBO = bo;
		}
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		// TODO Auto-generated method stub
		BoardObject target = BOAtPos(new Vector2f(x, y));
		if (this.attackingMinion != null) {
			if (target != null && (target instanceof Minion) && target.team == -1) {
				this.eventlist.add(new EventMinionAttack(this.attackingMinion, (Minion) target));
			}
			this.attackingMinion.scale = 1;
			this.attackingMinion = null;
		}
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub

	}
}
