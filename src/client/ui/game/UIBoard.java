package client.ui.game;

import java.util.*;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.*;
import client.ui.*;
import client.ui.game.eventanimation.*;
import server.card.*;

public class UIBoard extends UIBox {
	public static final double CLICK_DISTANCE_THRESHOLD = 5;
	public static final double CARD_SCALE_DEFAULT = 1, CARD_SCALE_HAND = 0.75, CARD_SCALE_HAND_EXPAND = 1.2,
			CARD_SCALE_BOARD = 1, CARD_SCALE_ABILITY = 1.3, CARD_SCALE_TARGET = 1.15, CARD_SCALE_ATTACK = 1.3,
			CARDS_SCALE_PLAY = 2.5;
	public static final double BO_SPACING = 0.1, BO_Y_LOCAL = 0.105, BO_Y_ENEMY = -0.115;
	public static final double LEADER_Y_LOCAL = 0.38, LEADER_Y_ENEMY = -0.4;
	public static final double UNLEASHPOWER_X = 0.07, UNLEASHPOWER_Y_LOCAL = 0.29, UNLEASHPOWER_Y_ENEMY = -0.31;
	public static final double HAND_X_LOCAL = 0.19, HAND_X_ENEMY = 0.28, HAND_X_EXPAND_LOCAL = 0.175;
	public static final double HAND_Y_LOCAL = 0.38, HAND_Y_ENEMY = -0.41, HAND_Y_EXPAND_LOCAL = 0.33;
	public static final double HAND_X_SCALE_LOCAL = 0.22, HAND_X_SCALE_ENEMY = 0.26, HAND_X_SCALE_EXPAND_LOCAL = 0.36;
	public static final double CARD_PLAY_Y = 0.2, HAND_EXPAND_Y = 0.30;
	public static final int CARD_DEFAULT_Z = 0, CARD_HAND_Z = 2, CARD_BOARD_Z = 0, CARD_ABILITY_Z = 4,
			CARD_VISUALPLAYING_Z = 4, CARD_DRAGGING_Z = 3;
	public static final Vector2f TARGETING_CARD_POS = new Vector2f(-0.4f, -0.22f);

	public VisualBoard b;
	boolean expandHand = false;
	boolean draggingUnleash = false;
	Vector2f mouseDownPos = new Vector2f();
	CardSelectPanel cardSelectPanel;
	EndTurnButton endTurnButton;
	Text targetText, player1ManaText, player2ManaText;
	public UICard preSelectedCard, selectedCard, draggingCard, playingCard, visualPlayingCard, attackingMinion,
			unleashingMinion;
	// TODO: CARD PLAYING QUEUE AND BOARD POSITIONING
	double playingX;
	List<UICard> cards, targetedCards;

	public UIBoard(UI ui, int localteam) {
		super(ui, new Vector2f(Config.WINDOW_WIDTH / 2, Config.WINDOW_HEIGHT / 2),
				new Vector2f(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT), "res/ui/uibox.png");
		this.cards = new ArrayList<UICard>();
		this.targetedCards = new LinkedList<UICard>();
		this.b = new VisualBoard(this, localteam);
		this.cardSelectPanel = new CardSelectPanel(ui, this);
		this.cardSelectPanel.setZ(10);
		this.addChild(this.cardSelectPanel);
		this.cardSelectPanel.setHide(true);
		this.endTurnButton = new EndTurnButton(ui, this);
		this.addChild(this.endTurnButton);
		this.targetText = new Text(ui, new Vector2f(), "Target", 400, 24, "Verdana", 30, 0, -1);
		this.targetText.setZ(999);
		this.player1ManaText = new Text(ui, new Vector2f(-0.25f, 0.34f), "Player 1 Mana", 400, 24, "Verdana", 30, 0, 0);
		this.player1ManaText.relpos = true;
		this.player1ManaText.setZ(1);
		this.player2ManaText = new Text(ui, new Vector2f(-0.25f, -0.44f), "Player 2 Mana", 400, 24, "Verdana", 30, 0,
				0);
		this.player2ManaText.relpos = true;
		this.player2ManaText.setZ(1);
		this.addChild(this.targetText);
		this.addChild(this.player1ManaText);
		this.addChild(this.player2ManaText);
	}

	@Override
	public void update(double frametime) {
		super.update(frametime);
		this.b.update(frametime);

		this.targetText.setHide(false);
		if (this.playingCard != null && this.playingCard.getCard().getNextNeededBattlecryTarget() != null) {
			this.targetText.setText(this.playingCard.getCard().getNextNeededBattlecryTarget().description);
			this.targetText.setPos(new Vector2f(this.playingCard.getPos().x, this.playingCard.getPos().y
					+ (float) (Card.CARD_DIMENSIONS.y * CARD_SCALE_ABILITY * CARD_SCALE_HAND / 2)), 1);
		} else if (this.unleashingMinion != null
				&& this.unleashingMinion.getMinion().getNextNeededUnleashTarget() != null) {
			this.targetText.setText(this.unleashingMinion.getMinion().getNextNeededUnleashTarget().description);
			this.targetText.setPos(new Vector2f(this.unleashingMinion.getPos().x, this.unleashingMinion.getPos().y
					+ (float) (Card.CARD_DIMENSIONS.y * CARD_SCALE_ABILITY * CARD_SCALE_BOARD / 2)), 1);
		} else {
			this.targetText.setHide(true);
		}

		this.player1ManaText
				.setText(this.b.getPlayer(this.b.localteam).mana + "/" + this.b.getPlayer(this.b.localteam).maxmana);

		this.player2ManaText.setText(
				this.b.getPlayer(this.b.localteam * -1).mana + "/" + this.b.getPlayer(this.b.localteam * -1).maxmana);

		// move the cards to their respective positions
		for (UICard c : this.cards) {
			c.setHide(false);
			switch (c.getCard().status) {
			case BOARD:
				c.draggable = false;
				if (c != this.unleashingMinion && c != this.attackingMinion) {
					c.setZ(CARD_BOARD_Z);
				}
				c.setPos(new Vector2f((float) this.boardPosToX(c.getCard().cardpos, c.getCard().team),
						(float) (c.getCard().team == this.b.localteam ? BO_Y_LOCAL : BO_Y_ENEMY)), 0.99);
				break;
			case LEADER:
				c.draggable = false;
				if (c != this.unleashingMinion && c != this.attackingMinion) {
					c.setZ(CARD_BOARD_Z);
				}
				c.setPos(
						// oh my this formatting
						new Vector2f(0,
								(float) (c.getCard().team == this.b.localteam ? LEADER_Y_LOCAL : LEADER_Y_ENEMY)),
						1);
				break;
			case UNLEASHPOWER:
				c.draggable = false;
				c.setZ(CARD_DEFAULT_Z);
				c.setPos(new Vector2f((float) UNLEASHPOWER_X,
						(float) (c.getCard().team == this.b.localteam ? UNLEASHPOWER_Y_LOCAL : UNLEASHPOWER_Y_ENEMY)),
						1);
				break;
			case HAND:
				if (c != this.draggingCard) {
					c.setZ(CARD_HAND_Z);
				}
				if (c.getCard().team == this.b.localteam && !this.b.disableInput) {
					c.draggable = true;
				}
				if (c != this.playingCard && c != this.draggingCard && c != this.visualPlayingCard) {
					// ignore the following behemoth of a statement
					// dont bother understanding it lol
					// if there's a bug then god have mercy
					c.setPos(
							new Vector2f(
									(float) (((c.getCard().cardpos)
											- (this.b.getPlayer(c.getCard().team).hand.cards.size()) / 2.)
											* (c.getCard().team == this.b.localteam ? (this.expandHand
													? (HAND_X_SCALE_EXPAND_LOCAL
															+ this.b.getPlayer(c.getCard().team).hand.cards.size()
																	* 0.02)
													: HAND_X_SCALE_LOCAL) : HAND_X_SCALE_ENEMY)
											/ this.b.getPlayer(c.getCard().team).hand.cards.size()
											+ (c.getCard().team == this.b.localteam ? (this.expandHand
													? (HAND_X_EXPAND_LOCAL
															- this.b.getPlayer(c.getCard().team).hand.cards.size()
																	* 0.01)
													: HAND_X_LOCAL) : HAND_X_ENEMY)),
									(float) (c.getCard().team == this.b.localteam
											? (this.expandHand ? HAND_Y_EXPAND_LOCAL : HAND_Y_LOCAL)
											: HAND_Y_ENEMY)),
							0.99);
					c.setScale(c.getCard().team == this.b.localteam
							? (this.expandHand ? CARD_SCALE_HAND_EXPAND : CARD_SCALE_HAND)
							: CARD_SCALE_HAND);
				}
				break;
			default:
				c.setHide(true);
				break;
			}
		}
		if (this.playingCard != null) {
			this.playingCard.setPos(TARGETING_CARD_POS, 0.999);
			this.playingCard.setZ(CARD_ABILITY_Z);
			this.playingCard.setScale(CARD_SCALE_ABILITY * CARD_SCALE_HAND);
		}
		if (this.unleashingMinion != null) {
			this.unleashingMinion.setZ(CARD_ABILITY_Z);
		}
	}

	@Override
	public void draw(Graphics g) {
		super.draw(g);
		this.b.draw(g);
		for (UICard c : this.targetedCards) {
			g.setColor(org.newdawn.slick.Color.red);
			g.drawRect((float) (c.getFinalPos().x - Card.CARD_DIMENSIONS.x * c.getScale() / 2 * 0.9),
					(float) (c.getFinalPos().y - Card.CARD_DIMENSIONS.y * c.getScale() / 2 * 0.9),
					(float) (Card.CARD_DIMENSIONS.x * c.getScale() * 0.9),
					(float) (Card.CARD_DIMENSIONS.y * c.getScale() * 0.9));
			g.setColor(org.newdawn.slick.Color.white);
		}
		if (this.draggingCard != null && this.draggingCard.getCard() instanceof BoardObject
				&& this.draggingCard.getRelPos().y < CARD_PLAY_Y) {
			g.drawLine(
					(float) (this.playBoardPosToX(
							this.XToPlayBoardPos(this.ui.lastmousepos.x / Config.WINDOW_WIDTH - 0.5, this.b.localteam),
							this.b.localteam) + 0.5) * Config.WINDOW_WIDTH,
					Config.WINDOW_HEIGHT * 0.55f,
					(float) (this.playBoardPosToX(
							this.XToPlayBoardPos(this.ui.lastmousepos.x / Config.WINDOW_WIDTH - 0.5, 1),
							this.b.localteam) + 0.5) * Config.WINDOW_WIDTH,
					Config.WINDOW_HEIGHT * 0.74f);
		}
		for (EventAnimation ea : this.b.currentAnimations) {
			ea.draw(g);
		}
	}

	// auxiliary function for position on board
	private double boardPosToX(int i, int team) {
		// TODO: make rotationally symmetrical
		if (team == 1) {
			return (i - (b.getBoardObjects(team).size() - 1) / 2.) * BO_SPACING;
		}
		return (i - (b.getBoardObjects(team).size() - 1) / 2.) * BO_SPACING;
	}

	private int XToBoardPos(double x, int team) {
		// TODO: make rotationally symmetrical
		int pos = 0;
		if (team == 1) {
			pos = (int) ((x / BO_SPACING) + ((b.getBoardObjects(team).size() - 1) / 2.) + 0.5);
			if (pos >= b.getBoardObjects(team).size()) {
				pos = b.getBoardObjects(team).size() - 1;
			}
			if (pos < 0) {
				pos = 0;
			}
		} else {
			pos = (int) ((x / BO_SPACING) + ((b.getBoardObjects(team).size() - 1) / 2.) + 0.5);
			if (pos >= b.getBoardObjects(team).size()) {
				pos = b.getBoardObjects(team).size() - 1;
			}
			if (pos < 0) {
				pos = 0;
			}
		}
		return pos;
	}

	private double playBoardPosToX(int i, int team) {
		// TODO make rotationally symmetrical
		if (team == 1) {
			return (i - 0.5 - (b.getBoardObjects(team).size() - 1) / 2.) * BO_SPACING;
		}
		return (i - 0.5 - (b.getBoardObjects(team).size() - 1) / 2.) * BO_SPACING;
	}

	private int XToPlayBoardPos(double x, int team) {
		// TODO make rotationally symmetrical
		int pos = 0;
		if (team == 1) {
			pos = (int) ((x / BO_SPACING) + ((b.getBoardObjects(team).size() - 1) / 2.) + 1);
			if (pos > b.getBoardObjects(team).size()) {
				pos = b.getBoardObjects(team).size();
			}
			if (pos < 0) {
				pos = 0;
			}
		} else {
			pos = (int) ((x / BO_SPACING) + ((b.getBoardObjects(team).size() - 1) / 2.) + 1);
			if (pos > b.getBoardObjects(team).size()) {
				pos = b.getBoardObjects(team).size();
			}
			if (pos < 0) {
				pos = 0;
			}
		}
		return pos;
	}

	public void addCard(Card c) {
		UICard uic = new UICard(this.ui, this, c);
		uic.relpos = true;
		c.uiCard = uic;
		this.cards.add(uic);
		this.addChild(uic);
	}

	public UICard cardAtPos(Vector2f pos) {
		for (UICard c : this.cards) {
			if (c.pointIsInHitbox(pos)) {
				return c;
			}
		}
		return null;
	}

	public List<UICard> getBoardObjects(int team) {
		List<UICard> ret = new LinkedList<UICard>();
		for (BoardObject bo : this.b.getBoardObjects(team)) {
			ret.add(bo.uiCard);
		}
		return ret;
	}

	public List<UICard> getBoardObjects(int team, boolean leader, boolean minion, boolean amulet) {
		List<UICard> ret = new LinkedList<UICard>();
		for (BoardObject bo : this.b.getBoardObjects(team, leader, minion, amulet)) {
			ret.add(bo.uiCard);
		}
		return ret;
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		this.mouseDownPos.set(x, y);
		this.selectedCard = null;
		this.preSelectedCard = null;
		this.expandHand = x > 800 && y > (HAND_EXPAND_Y + 0.5) * Config.WINDOW_HEIGHT;
		this.handleTargeting(null);
		// System.out.println("REAL BOARD:");
		// System.out.println(this.realBoard.stateToString());
		// this.realBoard.player1.printHand();
		// this.realBoard.player2.printHand();
		// System.out.println("VISUAL BOARD");
		// this.player1.printHand();
		// this.player2.printHand();
		// System.out.println(this.stateToString());

	}

	public void mousePressedCard(UICard c, int button, int x, int y) {
		this.mouseDownPos.set(x, y);
		this.selectedCard = null;
		this.preSelectedCard = null;
		this.expandHand = y > (HAND_EXPAND_Y + 0.5) * Config.WINDOW_HEIGHT;
		if (!this.handleTargeting(c)) { // if we clicked on a card
			this.preSelectedCard = c;
			switch (c.getCard().status) {
			case HAND:
				if (c.getCard().team == this.b.localteam) {
					c.setScale(CARD_SCALE_HAND_EXPAND);
					if (this.b.realBoard.getPlayer(this.b.localteam).canPlayCard(c.getCard().realCard)
							&& !this.b.disableInput) {
						this.draggingCard = c;
					}
					this.preSelectedCard = c;
					this.expandHand = true;
				}
				break;
			case UNLEASHPOWER:
				if (c.getCard().team == this.b.localteam) {
					if (this.b.getPlayer(this.b.localteam).canUnleash() && !this.b.disableInput) {
						c.setScale(CARD_SCALE_ABILITY);
						this.draggingUnleash = true;
						for (UICard uib : this.getBoardObjects(this.b.localteam)) {
							if (uib.getCard() instanceof Minion
									&& this.b.getPlayer(this.b.localteam).canUnleashCard(uib.getCard())) {
								uib.setScale(CARD_SCALE_TARGET);
							}
						}
					}
				}
				break;
			case LEADER: // TODO allow leader to attac properly
			case BOARD:
				BoardObject bo = (BoardObject) c.getCard();
				if (bo != null && bo instanceof Minion && bo.realCard.team == this.b.localteam
						&& ((Minion) bo.realCard).canAttack() && !this.b.disableInput) {
					this.attackingMinion = c;
					for (UICard uib : this.getBoardObjects(this.b.localteam * -1, true, true, false)) {
						if (uib.getCard() instanceof Minion && ((Minion) this.attackingMinion.getCard().realCard)
								.getAttackableTargets().contains(uib.getCard().realCard)) {
							uib.setScale(CARD_SCALE_TARGET);
						}
					}
					c.setScale(CARD_SCALE_ATTACK);
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		if (this.mouseDownPos.distance(new Vector2f(x, y)) <= CLICK_DISTANCE_THRESHOLD) {
			this.selectedCard = this.preSelectedCard;
		}
		UICard c = this.cardAtPos(new Vector2f(x, y));
		if (this.attackingMinion != null) {
			if (c != null && (c.getCard() instanceof Minion) && c.getCard().team != this.b.localteam
					&& ((Minion) this.attackingMinion.getCard().realCard).getAttackableTargets()
							.contains(c.getCard().realCard)) {
				this.b.realBoard.playerOrderAttack((Minion) this.attackingMinion.getCard().realCard,
						(Minion) c.getCard().realCard);
				c.setScale(CARD_SCALE_BOARD);
			}
			for (UICard uib : this.getBoardObjects(this.b.localteam * -1)) {
				if (uib.getCard() instanceof Minion && ((Minion) this.attackingMinion.getCard().realCard)
						.getAttackableTargets().contains(uib.getCard().realCard)) {
					uib.setScale(CARD_SCALE_BOARD);
				}
			}
			this.attackingMinion.setScale(CARD_SCALE_BOARD);
			this.attackingMinion = null;
		} else if (this.draggingUnleash) {
			// preselected card is unleashpower
			this.preSelectedCard.setScale(CARD_SCALE_DEFAULT);
			for (UICard uib : this.getBoardObjects(this.b.localteam)) {
				if (uib.getCard() instanceof Minion) {
					uib.setScale(CARD_SCALE_BOARD);
				}
			}
			this.draggingUnleash = false;
			if (c != null && c.getCard() instanceof Minion && c.getCard().team == this.b.localteam
					&& this.b.getPlayer(this.b.localteam).canUnleashCard(c.getCard())) {
				this.selectUnleashingMinion(c);
			}
		} else if (this.draggingCard != null) {
			if (this.draggingCard.getRelPos().y < CARD_PLAY_Y
					&& this.b.getPlayer(this.b.localteam).canPlayCard(this.draggingCard.getCard())) {
				this.playingCard = this.draggingCard;
				this.selectedCard = null;
				this.resolveNoBattlecryTarget();
				this.animateBattlecryTargets(true);
				this.playingX = this.draggingCard.getRelPos().x;
			}
			this.draggingCard = null;
		}

		if (this.playingCard != null) {
			this.finishBattlecryTargeting();
		} else if (this.unleashingMinion != null) {
			this.finishUnleashTargeting();
		}
	}

	public void finishBattlecryTargeting() {
		Target t = this.playingCard.getCard().getNextNeededBattlecryTarget();
		if (t == null) {
			// convert visual card's targets to real card's targets
			this.playingCard.getCard().realCard.resetBattlecryTargets();
			List<Target> visualbt = this.playingCard.getCard().getBattlecryTargets();
			List<Target> realbt = this.playingCard.getCard().realCard.getBattlecryTargets();
			for (int i = 0; i < visualbt.size(); i++) {
				for (Card targetc : visualbt.get(i).getTargets()) {
					realbt.get(i).setTarget(targetc.realCard);
				}
			}
			this.b.realBoard.playerPlayCard(this.b.realBoard.getPlayer(this.b.localteam),
					this.playingCard.getCard().realCard, XToPlayBoardPos(this.playingX, this.b.localteam));
			this.playingCard = null;
		}
	}

	public void finishUnleashTargeting() {
		Target t = this.unleashingMinion.getMinion().getNextNeededUnleashTarget();
		if (t == null) {
			// convert visual card's targets to real card's targets
			((Minion) this.unleashingMinion.getMinion().realCard).resetUnleashTargets();
			List<Target> visualut = this.unleashingMinion.getMinion().getUnleashTargets();
			List<Target> realut = ((Minion) this.unleashingMinion.getCard().realCard).getUnleashTargets();
			for (int i = 0; i < visualut.size(); i++) {
				for (Card targetc : visualut.get(i).getTargets()) {
					realut.get(i).setTarget(targetc.realCard);
				}
			}
			this.b.realBoard.playerUnleashMinion(this.b.realBoard.getPlayer(this.b.localteam),
					(Minion) this.unleashingMinion.getMinion().realCard);
			this.unleashingMinion.setScale(CARD_SCALE_BOARD);
			this.unleashingMinion = null;
		}
	}

	public boolean handleTargeting(UICard c) {
		if (this.b.disableInput) {
			return false;
		}
		if (this.playingCard != null && this.playingCard.getCard().getNextNeededBattlecryTarget() != null) {
			if (c != null && c.getCard().realCard.alive && this.playingCard.getCard().realCard
					.getNextNeededBattlecryTarget().canTarget(c.getCard().realCard)) {
				if (this.targetedCards.contains(c)) {
					this.targetedCards.remove(c);
				} else {
					this.targetedCards.add(c);
					// whether max targets have been selected or all selectable
					// targets have been selected
					if (this.targetedCards
							.size() >= this.playingCard.getCard().getNextNeededBattlecryTarget().maxtargets
							|| this.targetedCards.size() == this
									.getTargetableCards(this.playingCard.getCard().getNextNeededBattlecryTarget())
									.size()) {
						this.animateBattlecryTargets(false);
						this.playingCard.getCard().getNextNeededBattlecryTarget().setTargetsUI(this.targetedCards);
						this.targetedCards.clear();
						this.resolveNoBattlecryTarget();
						this.animateBattlecryTargets(true);
					}
				}

				return true;
			} else {
				this.animateBattlecryTargets(false);
				// this.playingCard.scale = this.expandHand ? CARD_SCALE_HAND_EXPAND :
				// CARD_SCALE_HAND;
				this.playingCard.getCard().resetBattlecryTargets();
				this.targetedCards.clear();
				this.playingCard = null;
			}
		} else if (this.unleashingMinion != null
				&& this.unleashingMinion.getMinion().getNextNeededUnleashTarget() != null) {
			if (c != null && c.getCard().realCard.alive && ((Minion) this.unleashingMinion.getCard().realCard)
					.getNextNeededUnleashTarget().canTarget(c.getCard().realCard)) {
				if (this.targetedCards.contains(c)) {
					this.targetedCards.remove(c);
				} else {
					this.targetedCards.add(c);
					// whether max targets have been selected or all selectable
					// targets have been selected
					if (this.targetedCards
							.size() >= this.unleashingMinion.getMinion().getNextNeededUnleashTarget().maxtargets
							|| this.targetedCards.size() == this
									.getTargetableCards(this.unleashingMinion.getMinion().getNextNeededUnleashTarget())
									.size()) {
						this.animateUnleashTargets(false);
						this.unleashingMinion.getMinion().getNextNeededUnleashTarget().setTarget(c.getCard());
						this.targetedCards.clear();
						this.resolveNoUnleashTarget();
						this.animateUnleashTargets(true);
					}
				}
				return true;
			} else {
				this.animateUnleashTargets(false);
				this.unleashingMinion.setScale(CARD_SCALE_BOARD);
				this.unleashingMinion.getMinion().resetUnleashTargets();
				this.targetedCards.clear();
				this.unleashingMinion = null;
			}
		}
		return false;
	}

	public void selectUnleashingMinion(UICard c) {
		c.getMinion().resetUnleashTargets();
		((Minion) c.getMinion().realCard).resetUnleashTargets();
		this.unleashingMinion = c;
		this.resolveNoUnleashTarget();
		this.animateUnleashTargets(true);
		c.setScale(CARD_SCALE_ABILITY * CARD_SCALE_BOARD);
		this.finishUnleashTargeting();
	}

	public List<UICard> getTargetableCards(Target t) {
		List<UICard> list = new LinkedList<UICard>();
		if (t == null) {
			return list;
		}
		for (Card c : this.b.getTargetableCards(t)) {
			list.add(c.uiCard);
		}
		return list;
	}

	public void animateBattlecryTargets(boolean activate) {
		List<UICard> tc = this.getTargetableCards(this.playingCard.getCard().getNextNeededBattlecryTarget());
		for (UICard c : tc) {
			c.setScale(activate ? CARD_SCALE_TARGET
					: (c.getCard().status.equals(CardStatus.HAND) ? CARD_SCALE_HAND : CARD_SCALE_BOARD));

		}
	}

	public void animateUnleashTargets(boolean activate) {
		List<UICard> tc = this.getTargetableCards(this.unleashingMinion.getMinion().getNextNeededUnleashTarget());
		for (UICard c : tc) {
			c.setScale(activate ? CARD_SCALE_TARGET
					: (c.getCard().status.equals(CardStatus.HAND) ? CARD_SCALE_HAND : CARD_SCALE_BOARD));
		}
	}

	public void resolveNoBattlecryTarget() {
		List<UICard> tc = this.getTargetableCards(this.playingCard.getCard().getNextNeededBattlecryTarget());
		while (tc.isEmpty() && this.playingCard.getCard().getNextNeededBattlecryTarget() != null) {
			this.playingCard.getCard().getNextNeededBattlecryTarget().setTarget(null);
			tc = this.getTargetableCards(this.playingCard.getCard().getNextNeededBattlecryTarget());
		}
	}

	public void resolveNoUnleashTarget() {
		List<UICard> tc = this.getTargetableCards(this.unleashingMinion.getMinion().getNextNeededUnleashTarget());
		while (tc.isEmpty() && this.unleashingMinion.getMinion().getNextNeededUnleashTarget() != null) {
			this.unleashingMinion.getMinion().getNextNeededUnleashTarget().setTarget(null);
			tc = this.getTargetableCards(this.unleashingMinion.getMinion().getNextNeededUnleashTarget());
		}
	}
}