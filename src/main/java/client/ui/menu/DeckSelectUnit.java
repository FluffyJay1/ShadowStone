package client.ui.menu;

import client.Game;
import org.newdawn.slick.geom.Vector2f;

import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import server.card.cardset.ConstructedDeck;

public class DeckSelectUnit extends UIBox {
    final Text text;
    public ConstructedDeck deck;

    public DeckSelectUnit(UI ui) {
        super(ui, new Vector2f(0, 0), new Vector2f(180, 100), "res/ui/uiboxborder.png");
        this.text = new Text(ui, new Vector2f(0, 0), "A deck", 180, 20, 30, 0, 0);
        this.addChild(this.text);
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        if (this.getParent().getParent() instanceof DeckSelectPanel) {
            ((DeckSelectPanel) this.getParent().getParent()).selectedDeckUnit = this;
        }
    }

    public void setDeck(ConstructedDeck deck) {
        this.deck = deck;
        if (deck == null) {
            this.text.setText("New deck");
        } else {
            this.text.setText(deck.name);
        }
    }
}
