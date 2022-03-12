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
import server.card.cardset.*;

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
    GameContainer container;

    @Override
    public void init(GameContainer arg0, StateBasedGame arg1) {
        this.container = arg0;
    }

    @Override
    public void enter(GameContainer arg0, StateBasedGame arg1) {
        this.ui = new UI();
        arg0.getInput().addListener(this.ui);
        this.ui.addListener((strarg, intarg) -> {
            StringTokenizer st = new StringTokenizer(strarg);
            switch (st.nextToken()) {
                case DeckSelectPanel.DECK_CONFIRM -> {
                    // selected deck to edit
                    if (deckselectpanel.selectedDeckUnit != null) {
                        if (deckselectpanel.selectedDeckUnit.deck == null) {
                            classSelect.setVisible(true);
                        } else {
                            newDeck = false;
                            // select and edit deck
                            currentDeck = deckselectpanel.selectedDeckUnit.deck;
                            enterDeckbuilding();

                        }
                        deckselectpanel.setVisible(false);

                    }
                }
                case DeckSelectPanel.DECK_CANCEL -> {
                    // go back to menu
                    arg1.enterState(Game.STATE_MENU);
                }
                case ClassSelectPanel.SELECT -> {
                    // select class for new deck
                    newDeck = true;
                    currentDeck = new ConstructedDeck(ClassCraft.values()[intarg[0]]);
                    currentDeck.name = currentDeck.craft.toString() + " deck " + ConstructedDeck.decks.size();
                    enterDeckbuilding();
                }
                case ClassSelectPanel.SELECT_CANCEL -> {
                    // cancel selecting class
                    currentDeck = null;
                    deckselectpanel.setVisible(true);
                    deckdisplaypanel.setVisible(false);
                }
                case DeckDisplayPanel.CARD_CLICK -> {
                    // select card in deckbuilder
                    CardText cardText = CardText.fromString(st.nextToken());
                    switch (intarg[0]) {
                        case 1 -> {
                            // display its tooltip
                            assert cardText != null;
                            cardTooltip.setTooltip(cardText.getTooltip());
                        }
                        case 2 -> deckdisplaypanel.removeCard(cardText);
                        default -> {
                        }
                    }
                }
                case DeckDisplayPanel.DECK_CONFIRM -> {
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
                    deckselectpanel.setVisible(true);
                    deckdisplaypanel.setVisible(false);
                    cardsetpanel.setVisible(false);
                }
                case CardSetDisplayPanel.CARDSET_CLICK -> {
                    // select card in cards to choose from
                    CardText cardText = CardText.fromString(st.nextToken());
                    switch (intarg[0]) {
                        case 1 -> {
                            // display its tooltip
                            assert cardText != null;
                            cardTooltip.setTooltip(cardText.getTooltip());
                        }
                        case 2 -> deckdisplaypanel.addCard(cardText);
                        default -> {
                        }
                    }
                }
            }
        });
        this.ui.setOnPress(element -> {
            if (element == null || (element != this.cardTooltip && !element.isChildOf(this.cardTooltip) && !(element instanceof CardDisplayUnit))) {
                this.cardTooltip.setTooltip(null);
            }
        });
        Text titleText = new Text(this.ui, new Vector2f(0, -0.5f), "Deck Management", 300, 20, Game.DEFAULT_FONT, 34, 0, 0);
        titleText.relpos = true;
        titleText.alignv = -1;
        this.ui.addUIElementParent(titleText);
        this.deckdisplaypanel = new DeckDisplayPanel(ui, new Vector2f(0, -0.2f), true);
        this.deckdisplaypanel.setVisible(false);
        this.deckdisplaypanel.relpos = true;
        this.ui.addUIElementParent(this.deckdisplaypanel);
        this.cardsetpanel = new CardSetDisplayPanel(ui, new Vector2f(0, 0.2f));
        this.cardsetpanel.setVisible(false);
        this.cardsetpanel.relpos = true;
        this.ui.addUIElementParent(this.cardsetpanel);
        this.deckselectpanel = new DeckSelectPanel(ui, new Vector2f(0, 0), true);
        this.deckselectpanel.relpos = true;
        this.ui.addUIElementParent(this.deckselectpanel);
        this.classSelect = new ClassSelectPanel(ui, new Vector2f(0, 0), true);
        this.classSelect.setVisible(false);
        this.classSelect.relpos = true;
        this.ui.addUIElementParent(this.classSelect);
        this.cardTooltip = new CardSelectTooltipPanel(this.ui, new Vector2f(-0.45f, -0.45f), 3);
        this.cardTooltip.relpos = true;
        this.cardTooltip.alignh = -1;
        this.cardTooltip.alignv = -1;
        this.cardTooltip.setVisible(false);
        this.ui.addUIElementParent(this.cardTooltip);

    }

    private void enterDeckbuilding() {
        currentCardSet = new CardSet(CardSet.PLAYABLE_SET).filterCraft(ClassCraft.NEUTRAL, currentDeck.craft);
        cardsetpanel.setVisible(true);
        cardsetpanel.setCardSet(currentCardSet);
        deckdisplaypanel.setVisible(true);
        deckdisplaypanel.setDeck(currentDeck.copy());
    }

    @Override
    public void leave(GameContainer arg0, StateBasedGame arg1) {
        arg0.getInput().removeListener(this.ui);
    }

    @Override
    public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2) {
        // TODO Auto-generated method stub
        this.ui.draw(arg2);
    }

    @Override
    public void update(GameContainer arg0, StateBasedGame arg1, int arg2) {
        // TODO Auto-generated method stub
        this.ui.update(arg2 / 1000.);
    }

    @Override
    public int getID() {
        // TODO Auto-generated method stub
        return Game.STATE_DECKBUILD;
    }

    @Override
    public void keyPressed(int key, char c) {
        if (key == Input.KEY_ESCAPE) {
            this.container.exit();
        }
    }
}
