package client;

import java.util.*;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.ui.Text;
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
	public static final double CARD_SCALE_DEFAULT = 1, CARD_SCALE_HAND = 0.75, CARD_SCALE_HAND_EXPAND = 1.2,
			CARD_SCALE_BOARD = 1, CARD_SCALE_ABILITY = 1.5, CARD_SCALE_TARGET = 1.25, CARD_SCALE_ATTACK = 1.5;
	UI ui;
	public Board realBoard;
	public Card selectedCard, draggingCard, playingCard;
	ArrayList<Card> targetedCards = new ArrayList<Card>();
	int playingX;
	public Minion attackingMinion, unleashingMinion;
	CardSelectPanel cardSelectPanel;
	EndTurnButton endTurnButton;
	Text targetText;
	double animationtimer = 0;
	Event currentEvent;

	public boolean disableInput = false;
	boolean expandHand = false;

	public VisualBoard() {
		super();
		this.ui = new UI();
		this.isClient = true;
		this.realBoard = new Board();
		this.realBoard.isClient = true;
		this.cardSelectPanel = new CardSelectPanel(this.ui, this);
		this.ui.addUIElementParent(this.cardSelectPanel);
		this.cardSelectPanel.hide = true;
		this.endTurnButton = new EndTurnButton(this.ui, this);
		this.ui.addUIElementParent(this.endTurnButton);
		this.targetText = new Text(ui, new Vector2f(), "Target", 400, 24, "Verdana", 30, 0, -1);
		this.ui.addUIElementParent(this.targetText);
		// this.cardSelectPanel.draggable = true;
	}

	public void update(double frametime) {
		this.updateEventAnimation(frametime);
		ui.update(frametime);
		player1.update(frametime);
		player2.update(frametime);
		for (BoardObject bo : player1side) {
			bo.update(frametime);
		}
		for (BoardObject bo : player2side) {
			bo.update(frametime);
		}
		String eventstring = this.realBoard.retrieveEventString();
		if (!eventstring.isEmpty()) {
			this.parseEventString(eventstring);
		}
	}

	public void draw(Graphics g) {
		for (int i = 1; i < player1side.size(); i++) {
			BoardObject bo = player1side.get(i);
			bo.targetpos.set(boardPosToX(bo.cardpos, 1), 700);
			bo.draw(g);
		}
		for (int i = 1; i < player2side.size(); i++) {
			BoardObject bo = player2side.get(i);
			bo.targetpos.set(boardPosToX((bo.cardpos), -1), 400);
			bo.draw(g);
		}
		if (!player1side.isEmpty()) {
			BoardObject player1leader = player1side.get(0);
			player1leader.targetpos.set(960, 950);
			player1leader.draw(g);
			String manastring = this.player1.mana + "/" + this.player1.maxmana;
			UnicodeFont font = Game.getFont("Verdana", 24, true, false);
			font.drawString(player1leader.pos.x - font.getWidth(manastring) / 2, player1leader.pos.y - 100, manastring);
		}
		if (!player2side.isEmpty()) {
			BoardObject player2leader = player2side.get(0);
			player2leader.targetpos.set(960, 100);
			player2leader.draw(g);
			String manastring = this.player2.mana + "/" + this.player2.maxmana;
			UnicodeFont font = Game.getFont("Verdana", 24, true, false);
			font.drawString(player2leader.pos.x - font.getWidth(manastring) / 2, player2leader.pos.y - 100, manastring);
		}
		for (int i = 0; i < player1.hand.cards.size(); i++) {
			Card c = player1.hand.cards.get(i);
			if (c != this.playingCard && c != this.draggingCard) {
				c.targetpos.set((int) (((i) - (player1.hand.cards.size()) / 2.)
						* (this.expandHand ? (800 + player1.hand.cards.size() * 40) : 500) / player1.hand.cards.size()
						+ (this.expandHand ? (1400 - player1.hand.cards.size() * 20) : 1500)),
						(this.expandHand ? 900 : 950));
				c.scale = this.expandHand ? CARD_SCALE_HAND_EXPAND : CARD_SCALE_HAND;
			}

			c.draw(g);
		}
		for (int i = 0; i < player2.hand.cards.size(); i++) {
			Card c = player2.hand.cards.get(i);
			c.targetpos.set((int) (((i) - (player2.hand.cards.size()) / 2.) * 500 / player2.hand.cards.size() + 1500),
					100);
			c.scale = CARD_SCALE_HAND;

			c.draw(g);
		}
		for (Card c : this.targetedCards) {
			g.setColor(org.newdawn.slick.Color.red);
			g.drawRect((float) (c.pos.x - Card.CARD_DIMENSIONS.x * c.scale / 2 * 0.9),
					(float) (c.pos.y - Card.CARD_DIMENSIONS.y * c.scale / 2 * 0.9),
					(float) (Card.CARD_DIMENSIONS.x * c.scale * 0.9), (float) (Card.CARD_DIMENSIONS.y * c.scale * 0.9));
			g.setColor(org.newdawn.slick.Color.white);
		}
		this.targetText.hide = false;
		if (this.playingCard != null && this.playingCard.getNextNeededBattlecryTarget() != null) {
			this.targetText.setText(this.playingCard.getNextNeededBattlecryTarget().description);
			this.targetText.setPos(new Vector2f(this.playingCard.pos.x, this.playingCard.pos.y + 100), 1);
		} else if (this.unleashingMinion != null && this.unleashingMinion.getNextNeededUnleashTarget() != null) {
			this.targetText.setText(this.unleashingMinion.getNextNeededUnleashTarget().description);
			this.targetText.setPos(new Vector2f(this.unleashingMinion.pos.x, this.unleashingMinion.pos.y + 100), 1);
		} else {
			this.targetText.hide = true;
		}
		ui.draw(g);
		if (this.draggingCard != null && this.draggingCard instanceof BoardObject && this.ui.lastmousepos.y < 750) {
			g.drawLine(this.playBoardPosToX(this.XToPlayBoardPos(this.ui.lastmousepos.x, 1), 1), 600,
					this.playBoardPosToX(this.XToPlayBoardPos(this.ui.lastmousepos.x, 1), 1), 800);
		}
		this.drawEventAnimation(g);
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
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player1side.size() - 2) / 2.) + 0.5) + 1;
			if (pos >= player1side.size()) {
				pos = player1side.size() - 1;
			}
			if (pos < 1) {
				pos = 1;
			}
		} else {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player2side.size() - 2) / 2.) + 0.5) + 1;
			if (pos >= player2side.size()) {
				pos = player2side.size() - 1;
			}
			if (pos < 1) {
				pos = 1;
			}
		}
		return pos;
	}

	private int playBoardPosToX(int i, int team) {
		if (team == 1) {
			return (int) ((i - 1.5 - (player1side.size() - 2) / 2.) * BO_SPACING + Game.WINDOW_WIDTH / 2);
		}
		return (int) ((i - 1.5 - (player2side.size() - 2) / 2.) * BO_SPACING + Game.WINDOW_WIDTH / 2);
	}

	private int XToPlayBoardPos(double x, int team) {
		int pos = 0;
		if (team == 1) {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player1side.size() - 2) / 2.) + 1) + 1;
			if (pos > player1side.size()) {
				pos = player1side.size();
			}
			if (pos < 1) {
				pos = 1;
			}
		} else {
			pos = (int) (((x - Game.WINDOW_WIDTH / 2) / BO_SPACING) + ((player2side.size() - 2) / 2.) + 1) + 1;
			if (pos > player2side.size()) {
				pos = player2side.size();
			}
			if (pos < 1) {
				pos = 1;
			}
		}
		return pos;
	}

	@Override
	public void resolveAll(LinkedList<Event> eventlist, boolean loopprotection) {
		this.realBoard.resolveAll(eventlist, loopprotection); // gottem

	}

	public void updateEventAnimation(double frametime) {
		if (this.animationtimer > 0) {
			this.animationtimer -= frametime;
		}
		if (this.animationtimer <= 0) {
			if (!this.inputeventliststrings.isEmpty()) {
				StringTokenizer st = new StringTokenizer(this.inputeventliststrings.remove(0));
				this.currentEvent = Event.createFromString(this, st);
				if (this.currentEvent != null && this.currentEvent.conditions()) {
					// System.out.println(this.currentEvent.toString());
					LinkedList<Event> lmao = new LinkedList<Event>();
					this.currentEvent.resolve(lmao, false);

					if (this.currentEvent instanceof EventMinionAttack) {
						this.animationtimer = 0.2;
					} else if (this.currentEvent instanceof EventMinionAttackDamage) {
						this.animationtimer = 0.5;
					} else if (this.currentEvent instanceof EventDamage) {
						this.animationtimer = 0.5;
					} else if (this.currentEvent instanceof EventMinionDamage) {
						this.animationtimer = 0.25;
					} else if (this.currentEvent instanceof EventUnleash) {
						this.animationtimer = 1;
					} else if (this.currentEvent instanceof EventTurnStart) {
						this.animationtimer = 1;
						if (((EventTurnStart) this.currentEvent).p.team == -1) {
							this.realBoard.AIThink();
						} else {
							this.disableInput = false;
						}
					} else if (!(this.currentEvent instanceof EventCreateCard)) {
						this.animationtimer = 0.2;
					}

				} else {
					this.currentEvent = null;
				}
			} else {
				this.currentEvent = null;
			}
		}
	}

	public void drawEventAnimation(Graphics g) {
		if (this.currentEvent != null) {
			UnicodeFont font = Game.getFont("Verdana", 60, true, false);
			g.setFont(font);
			String debugevent = this.currentEvent.getClass().getName();
			g.drawString(debugevent, 20, 20);
		}
		if (this.currentEvent != null) {
			if (this.currentEvent instanceof EventMinionAttack) {
				EventMinionAttack e = (EventMinionAttack) this.currentEvent;
				Vector2f pos = e.m1.pos.copy().sub(e.m2.pos).scale((float) (this.animationtimer / 0.2)).add(e.m2.pos);
				g.fillOval(pos.x - 5, pos.y - 5, 10, 10);
			} else if (this.currentEvent instanceof EventMinionAttackDamage) {
				EventMinionAttackDamage e = (EventMinionAttackDamage) this.currentEvent;
				Vector2f pos = e.m1.pos.copy().sub(e.m2.pos).scale((float) (this.animationtimer / 0.5)).add(e.m2.pos);
				Vector2f pos2 = e.m1.pos.copy().sub(e.m2.pos).scale(1 - (float) (this.animationtimer / 0.5))
						.add(e.m2.pos);
				g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
				g.fillOval(pos2.x - 20, pos2.y - 20, 40, 40);
			} else if (this.currentEvent instanceof EventDamage) {
				EventDamage e = (EventDamage) this.currentEvent;
				g.setColor(Color.red);
				UnicodeFont font = Game.getFont("Verdana", 80, true, false);
				g.setFont(font);
				float yoff = (float) (Math.pow(this.animationtimer / 0.5 - 0.5, 2) * 300) - 37.5f;
				for (int i = 0; i < e.t.size(); i++) {
					String dstring = e.damage.get(i) + "";
					for (Card c : e.t.get(i).getTargets()) {
						if (c != null) {
							g.drawString(dstring, c.pos.x - font.getWidth(dstring) / 2,
									c.pos.y - font.getHeight(dstring) + yoff);
						}
					}
				}
				g.setColor(Color.white);
			} else if (this.currentEvent instanceof EventMinionDamage) {
				g.setColor(Color.red);
				EventMinionDamage e = (EventMinionDamage) this.currentEvent;
				for (int i = 0; i < e.m2.size(); i++) {
					for (Card c : e.m2.get(i).getTargets()) {
						if (c != null) {
							Vector2f pos = e.m1.pos.copy().sub(c.pos).scale((float) (this.animationtimer / 0.25))
									.add(c.pos);
							g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
						}
					}
				}
				g.setColor(Color.white);
			} else if (this.currentEvent instanceof EventUnleash) {
				EventUnleash e = (EventUnleash) this.currentEvent;
				Vector2f pos = this.getBoardObject(e.p.team, 0).pos.copy().sub(e.m.pos)
						.scale((float) (this.animationtimer / 1)).add(e.m.pos);
				g.setColor(Color.yellow);
				g.fillOval(pos.x - 40, pos.y - 40, 80, 80);
				g.setColor(Color.white);
			} else if (this.currentEvent instanceof EventTurnStart) {
				EventTurnStart e = (EventTurnStart) this.currentEvent;
				UnicodeFont font = Game.getFont("Verdana", 80, true, false);
				String dstring = "TURN START";
				switch (e.p.team) {
				case 1:
					g.setColor(Color.cyan);
					dstring = "YOUR TURN";
					break;
				case -1:
					g.setColor(Color.red);
					dstring = "OPPONENT'S TURN";
					break;
				}
				g.setFont(font);
				g.drawString(dstring, Game.WINDOW_WIDTH / 2 - font.getWidth(dstring) / 2,
						Game.WINDOW_HEIGHT / 2 - font.getHeight(dstring));
				g.setColor(Color.white);
			}
		}
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
		if (!this.ui.mousePressed(button, x, y)) { // if we didn't click on
													// anything in the ui
			this.selectedCard = null;
			Card c = cardInHandAtPos(new Vector2f(x, y));
			if (c != null) {
				if (!this.handleTargeting(c)) {
					if (this.realBoard.player1.canPlayCard(c.realCard) && !this.disableInput && this.expandHand) {
						this.draggingCard = c;
					}
					this.selectedCard = c;
					this.expandHand = true;
				}
			} else {
				this.expandHand = x > 1000 && y > 850;
				BoardObject bo = BOAtPos(new Vector2f(x, y));
				if (!this.handleTargeting(bo)) {
					if (bo != null && bo instanceof Minion && bo.realCard.team == 1
							&& ((Minion) bo.realCard).canAttack() && !this.disableInput) {
						this.attackingMinion = (Minion) bo;
						for (BoardObject b : this.getBoardObjects(-1)) {
							if (b instanceof Minion && ((Minion) this.attackingMinion.realCard).getAttackableTargets()
									.contains((Minion) b.realCard)) {
								b.scale = CARD_SCALE_TARGET;
							}
						}
						bo.scale = CARD_SCALE_ATTACK;
					}
					this.selectedCard = bo;
				}
			}
		}
		System.out.println("REAL BOARD:");
		System.out.println(this.realBoard.stateToString());
		this.realBoard.player1.printHand();
		this.realBoard.player2.printHand();
		System.out.println("VISUAL BOARD");
		this.player1.printHand();
		this.player2.printHand();
		System.out.println(this.stateToString());

	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		// TODO Auto-generated method stub
		this.ui.mouseReleased(button, x, y);
		if (this.attackingMinion != null) {
			BoardObject target = BOAtPos(new Vector2f(x, y));
			if (target != null && (target instanceof Minion) && target.team == -1
					&& ((Minion) this.attackingMinion.realCard).getAttackableTargets().contains(target.realCard)) {
				// TODO: SERVER COMMUNICATION
				this.realBoard.playerOrderAttack((Minion) this.attackingMinion.realCard, (Minion) target.realCard);
				target.scale = CARD_SCALE_BOARD;
			}
			for (BoardObject b : this.getBoardObjects(-1)) {
				if (b instanceof Minion && ((Minion) this.attackingMinion.realCard).getAttackableTargets()
						.contains((Minion) b.realCard)) {
					b.scale = CARD_SCALE_BOARD;
				}
			}
			this.attackingMinion.scale = CARD_SCALE_BOARD;
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

		if (this.playingCard != null) {
			Target t = this.playingCard.getNextNeededBattlecryTarget();
			if (t == null) {
				this.realBoard.playerPlayCard(this.realBoard.player1, this.playingCard.realCard,
						XToPlayBoardPos(this.playingX, 1), this.playingCard.battlecryTargetsToString());
				this.playingCard = null;
			} else {
				this.playingCard.targetpos = new Vector2f(200, 300);
				this.playingCard.scale = CARD_SCALE_ABILITY * CARD_SCALE_HAND;
			}
		} else if (this.unleashingMinion != null) {
			Target t = this.unleashingMinion.getNextNeededUnleashTarget();
			if (t == null) {
				this.realBoard.playerUnleashMinion(this.realBoard.player1, (Minion) this.unleashingMinion.realCard,
						this.unleashingMinion.unleashTargetsToString());
				this.unleashingMinion.scale = CARD_SCALE_BOARD;
				this.unleashingMinion = null;
			} else {
				this.unleashingMinion.scale = CARD_SCALE_ABILITY;
			}
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

	public boolean handleTargeting(Card c) {
		if (this.disableInput) {
			return false;
		}
		if (this.playingCard != null && this.playingCard.getNextNeededBattlecryTarget() != null) {
			if (c != null && c.realCard.alive
					&& this.playingCard.realCard.getNextNeededBattlecryTarget().canTarget(c.realCard)) {
				if (this.targetedCards.contains(c)) {
					this.targetedCards.remove(c);
				} else {
					this.targetedCards.add(c);
					// whether max targets have been selected or all selectable
					// targets have been selected
					if (this.targetedCards.size() >= this.playingCard.getNextNeededBattlecryTarget().maxtargets
							|| this.targetedCards.size() == this
									.getTargetableCards(this.playingCard.getNextNeededBattlecryTarget()).size()) {
						this.animateBattlecryTargets(false);
						this.playingCard.getNextNeededBattlecryTarget().setTargets(this.targetedCards);
						this.targetedCards.clear();
						this.resolveNoBattlecryTarget();
						this.animateBattlecryTargets(true);
					}
				}

				return true;
			} else {
				this.animateBattlecryTargets(false);
				this.playingCard.scale = this.expandHand ? CARD_SCALE_HAND_EXPAND : CARD_SCALE_HAND;
				this.playingCard.resetBattlecryTargets();
				this.targetedCards.clear();
				this.playingCard = null;
			}
		} else if (this.unleashingMinion != null && this.unleashingMinion.getNextNeededUnleashTarget() != null) {
			if (c != null && c.realCard.alive
					&& ((Minion) this.unleashingMinion.realCard).getNextNeededUnleashTarget().canTarget(c.realCard)) {
				if (this.targetedCards.contains(c)) {
					this.targetedCards.remove(c);
				} else {
					this.targetedCards.add(c);
					// whether max targets have been selected or all selectable
					// targets have been selected
					if (this.targetedCards.size() >= this.unleashingMinion.getNextNeededUnleashTarget().maxtargets
							|| this.targetedCards.size() == this
									.getTargetableCards(this.unleashingMinion.getNextNeededUnleashTarget()).size()) {
						this.animateUnleashTargets(false);
						this.unleashingMinion.getNextNeededUnleashTarget().setTarget(c);
						this.targetedCards.clear();
						this.resolveNoUnleashTarget();
						this.animateUnleashTargets(true);
					}
				}
				return true;
			} else {
				this.animateUnleashTargets(false);
				this.unleashingMinion.scale = CARD_SCALE_BOARD;
				this.unleashingMinion.resetUnleashTargets();
				this.targetedCards.clear();
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
