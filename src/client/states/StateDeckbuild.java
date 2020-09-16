package client.states;

import java.util.*;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.state.*;

import client.Game;
import client.ui.*;
import client.ui.game.*;
import client.ui.menu.*;
import server.card.*;
import server.card.cardpack.*;

public class StateDeckbuild extends BasicGameState {
	UI ui;
	DeckSelectPanel deckselectpanel;
	boolean newDeck;
	ConstructedDeck currentDeck;
	CardSet currentCardSet;
	CardSetDisplayPanel cardsetpanel;
	DeckDisplayPanel deckdisplaypanel;
	ClassSelectPanel classSelect;
	CardSelectTooltipPanel cardTooltip;

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		// TODO Auto-generated method stub

	}

	@Override
	public void enter(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		this.ui = new UI();
		arg0.getInput().addListener(this.ui);
		this.ui.addListener(new UIEventListener() {
			@Override
			public void onAlert(String strarg, int... intarg) {
				StringTokenizer st = new StringTokenizer(strarg);
				switch (st.nextToken()) {
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
					Class<? extends Card> cardClass;
					try {
						cardClass = (Class<? extends Card>) Class.forName(st.nextToken());
						switch (intarg[0]) {
						case 1:
							// display its tooltip
							cardTooltip.setTooltip(CardSet.getCardTooltip(cardClass));
							break;
						case 2:
							deckdisplaypanel.removeCard(cardClass);
							break;
						default:
							break;
						}
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					break;
				case DeckDisplayPanel.DECK_CONFIRM:
					// confirm and save deck
					// TODO verify deck is legit
					cardTooltip.setTooltip(null);
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
				case DeckDisplayPanel.BACKGROUND_CLICK:
					cardTooltip.setTooltip(null);
					break;
				case CardSetDisplayPanel.CARDSET_CLICK:
					// select card in cards to choose from
					try {
						String cardClassString = st.nextToken();
						cardClass = (Class<? extends Card>) Class.forName(cardClassString);
						switch (intarg[0]) {
						case 1:
							// display its tooltip
							cardTooltip.setTooltip(CardSet.getCardTooltip(cardClass));
							break;
						case 2:
							deckdisplaypanel.addCard(cardClass);
							break;
						default:
							break;
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				default:
					break;
				case CardSetDisplayPanel.BACKGROUND_CLICK:
					cardTooltip.setTooltip(null);
				}
			}
		});
		this.deckdisplaypanel = new DeckDisplayPanel(ui, new Vector2f(0, -0.2f), true);
		this.deckdisplaypanel.setHide(true);
		this.deckdisplaypanel.relpos = true;
		this.ui.addUIElementParent(this.deckdisplaypanel);
		this.cardsetpanel = new CardSetDisplayPanel(ui, new Vector2f(0, 0.2f));
		this.cardsetpanel.setHide(true);
		this.cardsetpanel.relpos = true;
		this.ui.addUIElementParent(this.cardsetpanel);
		this.deckselectpanel = new DeckSelectPanel(ui, new Vector2f(0, 0), true);
		this.deckselectpanel.relpos = true;
		this.ui.addUIElementParent(this.deckselectpanel);
		this.classSelect = new ClassSelectPanel(ui, new Vector2f(0, 0));
		this.classSelect.setHide(true);
		this.classSelect.relpos = true;
		this.ui.addUIElementParent(this.classSelect);
		this.cardTooltip = new CardSelectTooltipPanel(this.ui, new Vector2f(200, 300), 5);
		this.cardTooltip.setHide(true);
		this.ui.addUIElementParent(this.cardTooltip);

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
		arg0.getInput().removeListener(this.ui);
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
