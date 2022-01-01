package client.ui.menu;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.ui.*;
import server.card.cardpack.*;

public class DeckSelectPanel extends UIBox {
    public static final String DECK_CONFIRM = "deckselectpanelconfirm";
    public static final String DECK_CANCEL = "deckselectpanelcancel";
    public static final String DECK_DELETE = "deckselectpaneldelete";
    final ScrollingContext scroll;
    final GenericButton confirmButton;
    final GenericButton cancelButton;
    GenericButton deleteButton;
    final ArrayList<DeckSelectUnit> deckButtons = new ArrayList<>();
    public DeckSelectUnit selectedDeckUnit;
    final UIBox highlight;
    // who needs inheritance when u can have just one class behave differently
    // depending on what's passed in the constructor
    final boolean deckbuild;

    public DeckSelectPanel(UI ui, Vector2f pos, boolean deckbuild) {
        super(ui, pos, new Vector2f(700, 600), "res/ui/uiboxborder.png");
        this.margins.set(10, 10);
        this.deckbuild = deckbuild;
        this.addChild(new Text(ui, new Vector2f(0, -250), "Select a deck", 300, 20, "Verdana", 34, 0, 0));
        this.confirmButton = new GenericButton(ui, new Vector2f(deckbuild ? -175 : -100, 250), new Vector2f(150, 50),
                "Confirm", 0) {
            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                this.alert(DECK_CONFIRM);
            }
        };
        this.addChild(this.confirmButton);
        this.cancelButton = new GenericButton(ui, new Vector2f(deckbuild ? 175 : 100, 250), new Vector2f(150, 50),
                "Back", 1) {
            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                this.alert(DECK_CANCEL);
            }
        };
        this.addChild(this.cancelButton);
        if (deckbuild) {
            this.deleteButton = new GenericButton(ui, new Vector2f(0, 250), new Vector2f(150, 50), "Delete", 1) {
                @Override
                public void mouseClicked(int button, int x, int y, int clickCount) {
                    this.alert(DECK_DELETE);
                }
            };
            this.addChild(this.deleteButton);
        }
        this.scroll = new ScrollingContext(ui, new Vector2f(), new Vector2f((float) this.getWidth(true), 400));
        this.scroll.clip = true;
        this.addChild(this.scroll);
        this.highlight = new UIBox(ui, new Vector2f(), new Vector2f(190, 110), "res/ui/highlight.png");
        this.highlight.setVisible(false);
        this.highlight.ignorehitbox = true;
        this.scroll.addChild(this.highlight);
        this.updateDecks();
    }

    @Override
    public void onAlert(String strarg, int... intarg) {
        StringTokenizer st = new StringTokenizer(strarg);
        switch (st.nextToken()) {
        case DECK_CONFIRM:
            if (this.selectedDeckUnit != null) {
                this.setVisible(false);
            }
            this.alert(strarg, intarg);
            break;
        case DECK_CANCEL:
            this.setVisible(false);
            this.alert(strarg, intarg);
            break;
        case DECK_DELETE:
            if (this.selectedDeckUnit != null) {
                ConstructedDeck.decks.remove(this.selectedDeckUnit.deck);
                ConstructedDeck.saveToFile();
                this.selectedDeckUnit = null;
                this.updateDecks();
            }
            break;
        default:
            this.alert(strarg, intarg);
            break;
        }
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        if (this.selectedDeckUnit != null) {
            this.highlight.setVisible(true);
            this.highlight.setPos(this.selectedDeckUnit.getPos(), 0.99999);
            if (this.deckbuild) {
                this.deleteButton.setVisible(this.selectedDeckUnit.deck != null);
            }
        } else {
            this.highlight.setVisible(false);
        }
    }

    public void updateDecks() {
        for (DeckSelectUnit button : this.deckButtons) {
            this.scroll.removeChild(button);
        }
        this.deckButtons.clear();
        int i = 0;
        for (; i < ConstructedDeck.decks.size(); i++) {
            ConstructedDeck cd = ConstructedDeck.decks.get(i);
            DeckSelectUnit button = new DeckSelectUnit(ui);
            button.setDeck(cd);
            button.setPos(new Vector2f((i % 3) * 225 - 225, i / 3 * 125 - 125), 1);
            this.deckButtons.add(button);
            this.scroll.addChild(button);
        }
        if (this.deckbuild) {
            DeckSelectUnit button = new DeckSelectUnit(ui);
            button.setDeck(null);
            button.setPos(new Vector2f((i % 3) * 225 - 225, i / 3 * 125 - 125), 1);
            this.deckButtons.add(button);
            this.scroll.addChild(button);
        }
    }
}
