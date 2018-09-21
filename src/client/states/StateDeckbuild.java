package client.states;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import client.Game;
import client.ui.UI;
import client.ui.UIEventListener;
import client.ui.UIMouseListenerWrapper;
import client.ui.menu.*;
import server.card.ClassCraft;
import server.card.cardpack.CardSet;
import server.card.cardpack.ConstructedDeck;

public class StateDeckbuild extends BasicGameState {
	UI ui;
	UIMouseListenerWrapper listener;
	DeckSelectPanel deckselectpanel;
	boolean newDeck;
	ConstructedDeck currentDeck;
	CardSet currentCardSet;
	CardSetDisplayPanel cardsetpanel;
	DeckDisplayPanel deckdisplaypanel;
	ClassSelectPanel classSelect;

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enter(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		this.ui = new UI();
		this.listener = new UIMouseListenerWrapper(this.ui);
		arg0.getInput().addMouseListener(listener);
		arg0.getInput().addKeyListener(this.ui);
		this.ui.addListener(new UIEventListener() {
			@Override
			public void onAlert(String strarg, int... intarg) {
				switch (strarg) {
				case DeckSelectPanel.DECK_CONFIRM:
					// selected deck to edit
					if (deckselectpanel.selectedDeckUnit != null) {
						if (deckselectpanel.selectedDeckUnit.deck == null) {
							classSelect.setHide(false);
						} else {
							newDeck = false;
							// select and edit deck
							currentDeck = deckselectpanel.selectedDeckUnit.deck;
							enterDeckbuilding();

						}
						deckselectpanel.setHide(true);

					}
					break;
				case DeckSelectPanel.DECK_CANCEL:
					// go back to menu
					arg1.enterState(Game.STATE_MENU);
					break;
				case ClassSelectPanel.SELECT:
					// select class for new deck
					newDeck = true;
					currentDeck = new ConstructedDeck();
					currentDeck.craft = ClassCraft.values()[intarg[0]];
					currentDeck.name = currentDeck.craft.toString() + " deck " + ConstructedDeck.decks.size();
					enterDeckbuilding();
					break;
				case ClassSelectPanel.SELECT_CANCEL:
					// cancel selecting class
					currentDeck = null;
					deckselectpanel.setHide(false);
					deckdisplaypanel.setHide(true);
					break;
				case DeckDisplayPanel.CARD_CLICK:
					// select card in deckbuilder
					switch (intarg[1]) {
					case 1:
						// display its tooltip
						break;
					case 2:
						deckdisplaypanel.removeCard(intarg[0]);
						break;
					default:
						break;
					}
					break;
				case DeckDisplayPanel.DECK_CONFIRM:
					// confirm and save deck
					// TODO verify deck is legit
					if (newDeck) {
						ConstructedDeck.decks.add(currentDeck);
					}
					currentDeck.copyFrom(deckdisplaypanel.deck);
					ConstructedDeck.saveToFile();
					currentDeck = null;
					deckselectpanel.selectedDeckUnit = null;
					deckselectpanel.updateDecks();
					deckselectpanel.setHide(false);
					deckdisplaypanel.setHide(true);
					cardsetpanel.setHide(true);
					break;
				case CardSetDisplayPanel.CARDSET_CLICK:
					// select card in cards to choose from
					switch (intarg[1]) {
					case 1:
						// display its tooltip
						break;
					case 2:
						deckdisplaypanel.addCard(intarg[0]);
						break;
					default:
						break;
					}
					break;
				default:
					break;
				}
			}
		});
		this.deckdisplaypanel = new DeckDisplayPanel(ui, new Vector2f(Game.WINDOW_WIDTH / 2, 300), true);
		this.deckdisplaypanel.setHide(true);
		this.ui.addUIElementParent(this.deckdisplaypanel);
		this.cardsetpanel = new CardSetDisplayPanel(ui, new Vector2f(Game.WINDOW_WIDTH / 2, 800));
		this.cardsetpanel.setHide(true);
		this.ui.addUIElementParent(this.cardsetpanel);
		this.deckselectpanel = new DeckSelectPanel(ui, new Vector2f(Game.WINDOW_WIDTH / 2, Game.WINDOW_HEIGHT / 2),
				true);
		this.ui.addUIElementParent(this.deckselectpanel);
		this.classSelect = new ClassSelectPanel(ui, new Vector2f(Game.WINDOW_WIDTH / 2, Game.WINDOW_HEIGHT / 2));
		this.classSelect.setHide(true);
		this.ui.addUIElementParent(this.classSelect);

	}

	private void enterDeckbuilding() {
		currentCardSet = new CardSet(CardSet.PLAYABLE_SET).filterCraft(ClassCraft.NEUTRAL, currentDeck.craft);
		cardsetpanel.setHide(false);
		cardsetpanel.setCardSet(currentCardSet);
		deckdisplaypanel.setHide(false);
		deckdisplaypanel.setDeck(currentDeck.copy());
	}

	@Override
	public void leave(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		arg0.getInput().removeMouseListener(listener);
	}

	@Override
	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2) throws SlickException {
		// TODO Auto-generated method stub
		this.ui.draw(arg2);
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int arg2) throws SlickException {
		// TODO Auto-generated method stub
		this.ui.update(arg2 / 1000.);
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return Game.STATE_DECKBUILD;
	}
}
