package client;

import java.util.*;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import server.Board;
import server.card.BoardObject;
import server.card.Card;
import server.card.Leader;
import server.card.Minion;
import server.card.Target;
import server.event.*;
import utils.DefaultMouseListener;

public class VisualBoard extends Board implements DefaultMouseListener {
	public static final int BO_SPACING = 200;
	BoardObject selectedBO;
	Card selectedCard, draggingCard, playingCard;
	int playingX;
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
		if (this.playingCard != null) {
			Target t = this.playingCard.getNextNeededTarget();
			if (t == null) {
				this.eventlist
						.add(new EventPlayCard(this.player1, this.playingCard, XToBoardPos(this.playingX, 1) + 1));
				this.playingCard = null;
			} else {
				this.playingCard.targetpos = new Vector2f(200, 300);
				this.playingCard.scale = 1;
			}
		}
		this.resolveAll();
	}

	public void draw(Graphics g) {
		for (int i = 1; i < player1side.size(); i++) {
			BoardObject bo = player1side.get(i);
			bo.targetpos.set(boardPosToX(bo.boardpos, 1), 700);
			if (this.playingCard != null && this.playingCard.getNextNeededTarget() != null
					&& this.playingCard.getNextNeededTarget().canTarget(bo)) {
				bo.scale = 1.2;
			}
			bo.draw(g);
		}
		for (int i = 1; i < player2side.size(); i++) {
			BoardObject bo = player2side.get(i);
			bo.targetpos.set(boardPosToX((bo.boardpos), -1), 400);
			if (this.playingCard != null && this.playingCard.getNextNeededTarget() != null
					&& this.playingCard.getNextNeededTarget().canTarget(bo)) {
				bo.scale = 1.2;
			}
			bo.draw(g);
		}
		BoardObject player1leader = player1side.get(0);
		BoardObject player2leader = player2side.get(0);
		player1leader.targetpos.set(960, 950);
		player2leader.targetpos.set(960, 100);
		player1leader.draw(g);
		player2leader.draw(g);
		for (int i = 0; i < player1.hand.cards.size(); i++) {
			Card c = player1.hand.cards.get(i);
			if (c != this.playingCard && c != this.draggingCard) {
				c.targetpos.set(
						(int) (((i) - (player1.hand.cards.size()) / 2.) * 500 / player1.hand.cards.size() + 1500), 950);
				c.scale = 0.75;
			}

			c.draw(g);
		}
		if (this.playingCard != null && this.playingCard.getNextNeededTarget() != null) {
			UnicodeFont font = Game.getFont("Verdana", 24, true, false);
			font.drawString(
					this.playingCard.pos.x - font.getWidth(this.playingCard.getNextNeededTarget().description) / 2,
					this.playingCard.pos.y + 100, this.playingCard.getNextNeededTarget().description);
		}
	}

	// auxiliary function for position on board
	private int boardPosToX(int i, int team) {
		if (team == 1) {
			return (int) ((i - 2 - (player1side.size() - 2) / 2.) * BO_SPACING + Game.WINDOW_WIDTH / 2);
		}
		return (int) ((-i - 2 - (player2side.size() - 2) / 2.) * BO_SPACING + Game.WINDOW_WIDTH / 2);
	}

	private int XToBoardPos(double x, int team) {
		int pos = 0;
		if (team == 1) {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player1side.size() - 2) / 2.) + 1) + 1;
			if (pos > player1side.size()) {
				pos = player1side.size();
			}
		} else {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player2side.size() - 2) / 2.) + 1) + 1;
			if (pos > player2side.size()) {
				pos = player2side.size();
			}
		}
		if (pos < 1) {
			pos = 1;
		}
		return team == 1 ? pos : -pos;
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

	public Card cardInHandAtPos(Vector2f pos) {
		for (int i = player1.hand.cards.size() - 1; i >= 0; i--) {
			if (player1.hand.cards.get(i).isInside(pos)) {
				return player1.hand.cards.get(i);
			}
		}
		return null;
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// TODO Auto-generated method stub
		this.selectedBO = null;
		Card c = cardInHandAtPos(new Vector2f(x, y));
		if (c != null) {
			this.draggingCard = c;
			this.selectedCard = c;
			if (this.playingCard != null && this.playingCard.getNextNeededTarget() != null) {
				if (this.playingCard.getNextNeededTarget().canTarget(c)) {
					this.playingCard.getNextNeededTarget().target = c;
				} else {
					this.playingCard.resetTargets();
					this.playingCard = null;
				}
			}
		} else {
			BoardObject bo = BOAtPos(new Vector2f(x, y));
			if (bo != null) {
				if (this.playingCard != null && this.playingCard.getNextNeededTarget() != null) {
					if (this.playingCard.getNextNeededTarget().canTarget(bo)) {
						this.playingCard.getNextNeededTarget().target = bo;
					} else {
						this.playingCard.resetTargets();
						this.playingCard = null;
					}
				} else {
					if (bo instanceof Minion && bo.team == 1) {
						this.attackingMinion = (Minion) bo;
						bo.scale = 1.5;
					}
					this.selectedBO = bo;
				}
			} else { // clicked on neither handcard or boardobject
				if (this.playingCard != null && this.playingCard.getNextNeededTarget() != null) {
					this.playingCard.resetTargets();
					this.playingCard = null;
				}
			}
		}
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		// TODO Auto-generated method stub

		if (this.attackingMinion != null) {
			BoardObject target = BOAtPos(new Vector2f(x, y));
			if (target != null && (target instanceof Minion) && target.team == -1) {
				this.eventlist.add(new EventMinionAttack(this.attackingMinion, (Minion) target));
			}
			this.attackingMinion.scale = 1;
			this.attackingMinion = null;
		} else if (this.draggingCard != null) {
			if (y < 750 && this.draggingCard.conditions()) {
				this.playingCard = this.draggingCard;
				this.playingX = x;
			}
			this.draggingCard = null;
		}
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub
		if (this.draggingCard != null) {
			this.draggingCard.targetpos.add(new Vector2f(newx, newy).sub(new Vector2f(oldx, oldy)));
		}
	}
}
