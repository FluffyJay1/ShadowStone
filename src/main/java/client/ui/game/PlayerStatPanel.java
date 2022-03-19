package client.ui.game;

import client.Game;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import org.newdawn.slick.geom.Vector2f;
import server.Player;

public class PlayerStatPanel extends UIBox {
    Text handSizeText, deckSizeText; // TODO shadows text
    public PlayerStatPanel(UI ui, Vector2f pos) {
        super(ui, pos, new Vector2f(200, 100), "res/ui/uiboxborder.png");
        this.margins.set(20, 20);
        this.handSizeText = new Text(ui, new Vector2f(0, -this.getHeight(true) / 2), "Hand:",
                this.getWidth(true), 20, Game.DEFAULT_FONT, 20, 0, -1);
        this.deckSizeText = new Text(ui, new Vector2f(0, this.handSizeText.getBottom(false, false)), "Deck:",
                this.getWidth(true), 20, Game.DEFAULT_FONT, 20, 0, -1);
        this.addChild(this.handSizeText);
        this.addChild(this.deckSizeText);
    }

    public void updateStats(Player p) {
        this.handSizeText.setText("Hand: " + p.getHand().size());
        this.deckSizeText.setText("Deck: " + p.getDeck().size());
    }
}
