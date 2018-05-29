package client;

import java.util.*;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.ui.UI;
import client.ui.game.CardSelectPanel;
import client.ui.game.EndTurnButton;
import client.ui.game.UnleashButton;
import server.Board;
import server.card.BoardObject;
import server.card.Card;
import server.card.Leader;
import server.card.Minion;
import server.card.Target;
import server.card.effect.EffectStats;
import server.event.*;
import utils.DefaultMouseListener;

public class VisualBoard extends Board implements DefaultMouseListener {
	public static final int BO_SPACING = 200;
	UI ui;
	public Card selectedCard, draggingCard, playingCard;
	LinkedList<Card> targetableCards = new LinkedList<Card>();
	int playingX;
	public Minion attackingMinion, unleashingMinion;
	CardSelectPanel cardSelectPanel;
	EndTurnButton endTurnButton;

	public VisualBoard() {
		super();
		this.ui = new UI();
		this.cardSelectPanel = new CardSelectPanel(this.ui, this);
		this.ui.addUIElementParent(this.cardSelectPanel);
		this.cardSelectPanel.hide = true;
		this.endTurnButton = new EndTurnButton(this.ui, this);
		this.ui.addUIElementParent(this.endTurnButton);
		// this.cardSelectPanel.draggable = true;
	}

	public void update(double frametime) {
		ui.update(frametime);
		player1.update(frametime);
		for (BoardObject bo : player1side) {
			bo.update(frametime);
		}
		for (BoardObject bo : player2side) {
			bo.update(frametime);
		}
		if (this.playingCard != null) {
			Target t = this.playingCard.getNextNeededBattlecryTarget();
			if (t == null) {
				this.eventlist
						.add(new EventPlayCard(this.player1, this.playingCard, XToBoardPos(this.playingX, 1) + 1));
				this.playingCard = null;
			} else {
				this.playingCard.targetpos = new Vector2f(200, 300);
				this.playingCard.scale = 1;
			}
		} else if (this.unleashingMinion != null) {
			Target t = this.unleashingMinion.getNextNeededUnleashTarget();
			if (t == null) {
				this.eventlist.add(new EventUnleash(this.player1, this.unleashingMinion));
				this.unleashingMinion = null;
			} else {
				this.unleashingMinion.scale = 1.5;
			}
		}
		if (this.currentplayerturn == -1) {
			this.AIThink();
		}
		this.resolveAll();
	}

	public void draw(Graphics g) {
		for (int i = 1; i < player1side.size(); i++) {
			BoardObject bo = player1side.get(i);
			bo.targetpos.set(boardPosToX(bo.boardpos, 1), 700);
			bo.draw(g);
		}
		for (int i = 1; i < player2side.size(); i++) {
			BoardObject bo = player2side.get(i);
			bo.targetpos.set(boardPosToX((bo.boardpos), -1), 400);
			bo.draw(g);
		}
		BoardObject player1leader = player1side.get(0);
		BoardObject player2leader = player2side.get(0);
		player1leader.targetpos.set(960, 950);
		player2leader.targetpos.set(960, 100);
		player1leader.draw(g);
		player2leader.draw(g);
		{
			String manastring = this.player1.mana + "/" + this.player1.maxmana;
			UnicodeFont font = Game.getFont("Verdana", 24, true, false);
			font.drawString(player1leader.pos.x - font.getWidth(manastring) / 2, player1leader.pos.y - 100, manastring);
		}
		for (int i = 0; i < player1.hand.cards.size(); i++) {
			Card c = player1.hand.cards.get(i);
			if (c != this.playingCard && c != this.draggingCard) {
				c.targetpos.set(
						(int) (((i) - (player1.hand.cards.size()) / 2.) * 500 / player1.hand.cards.size() + 1500), 950);
				c.scale = 0.75;
			}

			c.draw(g);
		}
		if (this.playingCard != null && this.playingCard.getNextNeededBattlecryTarget() != null) {
			UnicodeFont font = Game.getFont("Verdana", 24, true, false);
			font.drawString(
					this.playingCard.pos.x
							- font.getWidth(this.playingCard.getNextNeededBattlecryTarget().description) / 2,
					this.playingCard.pos.y + 100, this.playingCard.getNextNeededBattlecryTarget().description);
		} else if (this.unleashingMinion != null && this.unleashingMinion.getNextNeededUnleashTarget() != null) {
			UnicodeFont font = Game.getFont("Verdana", 24, true, false);
			font.drawString(
					this.unleashingMinion.pos.x
							- font.getWidth(this.unleashingMinion.getNextNeededUnleashTarget().description) / 2,
					this.unleashingMinion.pos.y + 100, this.unleashingMinion.getNextNeededUnleashTarget().description);
		}
		ui.draw(g);
	}

	// auxiliary function for position on board
	private int boardPosToX(int i, int team) {
		if (team == 1) {
			return (int) ((i - 1 - (player1side.size() - 2) / 2.) * BO_SPACING + Game.WINDOW_WIDTH / 2);
		}
		return (int) ((i - 1 - (player2side.size() - 2) / 2.) * BO_SPACING + Game.WINDOW_WIDTH / 2);
	}

	private int XToBoardPos(double x, int team) {
		int pos = 0;
		if (team == 1) {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player1side.size() - 2) / 2.)) + 1;
			if (pos > player1side.size()) {
				pos = player1side.size();
			}
		} else {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player2side.size() - 2) / 2.)) + 1;
			if (pos > player2side.size()) {
				pos = player2side.size();
			}
		}
		return pos;
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
	public void endCurrentPlayerTurn() {
		this.handleTargeting(null);
		super.endCurrentPlayerTurn();
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// TODO Auto-generated method stub
		if (!this.ui.mousePressed(button, x, y)) { // if we didn't click on
													// anything in the ui
			this.selectedCard = null;
			Card c = cardInHandAtPos(new Vector2f(x, y));
			if (c != null) {
				if (!this.handleTargeting(c)) {
					if (this.player1.canPlayCard(c)) {
						this.draggingCard = c;
					}
					this.selectedCard = c;
				}
			} else {
				BoardObject bo = BOAtPos(new Vector2f(x, y));
				if (bo != null) {
					if (!this.handleTargeting(bo)) {
						if (bo instanceof Minion && bo.team == 1 && ((Minion) bo).canAttack()) {
							this.attackingMinion = (Minion) bo;
							for (BoardObject b : this.attackingMinion.getAttackableTargets()) {
								b.scale = 1.25;
							}
							bo.scale = 1.5;
						}
						this.selectedCard = bo;
					}
				} else { // clicked on neither handcard or boardobject
					if (this.playingCard != null && this.playingCard.getNextNeededBattlecryTarget() != null) {
						this.animateBattlecryTargets(false);
						this.playingCard.resetBattlecryTargets();
						this.playingCard = null;
					}
					if (this.unleashingMinion != null && this.unleashingMinion.getNextNeededUnleashTarget() != null) {
						this.animateUnleashTargets(false);
						this.unleashingMinion.resetUnleashTargets();
						this.unleashingMinion = null;
					}
				}
			}
		}
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		// TODO Auto-generated method stub
		this.ui.mouseReleased(button, x, y);
		if (this.attackingMinion != null) {
			BoardObject target = BOAtPos(new Vector2f(x, y));
			if (target != null && (target instanceof Minion) && target.team == -1) {
				this.eventlist.add(new EventMinionAttack(this.attackingMinion, (Minion) target));
			}
			for (BoardObject b : this.attackingMinion.getAttackableTargets()) {
				b.scale = 1;
			}
			this.attackingMinion.scale = 1;
			this.attackingMinion = null;
		} else if (this.draggingCard != null) {
			if (y < 750 && this.player1.canPlayCard(this.draggingCard)) {
				this.playingCard = this.draggingCard;
				this.selectedCard = null;
				this.resolveNoBattlecryTarget();
				this.animateBattlecryTargets(true);
				this.playingX = x;
			}
			this.draggingCard = null;
		}
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub
		this.ui.mouseMoved(oldx, oldy, newx, newy);
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub
		this.ui.mouseDragged(oldx, oldy, newx, newy);
		if (this.draggingCard != null) {
			this.draggingCard.targetpos.add(new Vector2f(newx, newy).sub(new Vector2f(oldx, oldy)));
		}
	}

	@Override
	public void mouseWheelMoved(int change) {
		this.ui.mouseWheelMoved(change);
	}

	private boolean handleTargeting(Card c) {
		if (this.playingCard != null && this.playingCard.getNextNeededBattlecryTarget() != null) {
			if (c != null && this.playingCard.getNextNeededBattlecryTarget().canTarget(c)) {
				this.animateBattlecryTargets(false);
				this.playingCard.getNextNeededBattlecryTarget().setTarget(c);
				this.resolveNoBattlecryTarget();
				this.animateBattlecryTargets(true);
				return true;
			} else {
				this.animateBattlecryTargets(false);
				this.playingCard.resetBattlecryTargets();
				this.playingCard = null;
			}
		} else if (this.unleashingMinion != null && this.unleashingMinion.getNextNeededUnleashTarget() != null) {
			if (c != null && this.unleashingMinion.getNextNeededUnleashTarget().canTarget(c)) {
				this.animateUnleashTargets(false);
				this.unleashingMinion.getNextNeededUnleashTarget().setTarget(c);
				this.resolveNoUnleashTarget();
				this.animateUnleashTargets(true);
				return true;
			} else {
				this.animateUnleashTargets(false);
				this.unleashingMinion.resetUnleashTargets();
				this.unleashingMinion = null;
			}
		}
		return false;
	}

	public void animateBattlecryTargets(boolean activate) {
		LinkedList<Card> tc = this.getTargetableCards(this.playingCard.getNextNeededBattlecryTarget());
		for (Card c : tc) {
			c.scale = activate ? 1.25 : 1;
		}
	}

	public void animateUnleashTargets(boolean activate) {
		LinkedList<Card> tc = this.getTargetableCards(this.unleashingMinion.getNextNeededUnleashTarget());
		for (Card c : tc) {
			c.scale = activate ? 1.25 : 1;
		}
	}

	public void resolveNoBattlecryTarget() {
		LinkedList<Card> tc = this.getTargetableCards(this.playingCard.getNextNeededBattlecryTarget());
		while (tc.isEmpty() && this.playingCard.getNextNeededBattlecryTarget() != null) {
			this.playingCard.getNextNeededBattlecryTarget().setTarget(null);
			tc = this.getTargetableCards(this.playingCard.getNextNeededBattlecryTarget());
		}
	}

	public void resolveNoUnleashTarget() {
		LinkedList<Card> tc = this.getTargetableCards(this.unleashingMinion.getNextNeededUnleashTarget());

		while (tc.isEmpty() && this.unleashingMinion.getNextNeededUnleashTarget() != null) {
			this.unleashingMinion.getNextNeededUnleashTarget().setTarget(null);
			tc = this.getTargetableCards(this.unleashingMinion.getNextNeededUnleashTarget());
		}
	}
}
