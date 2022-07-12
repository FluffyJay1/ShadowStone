package client.ui.game;

import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import org.newdawn.slick.geom.Vector2f;
import server.Player;

public class PlayerStatPanel extends UIBox {
    Text handSizeText, deckSizeText, shadowsText;
    public PlayerStatPanel(UI ui, Vector2f pos) {
        super(ui, pos, new Vector2f(200, 140), "ui/uiboxborder.png");
        this.margins.set(20, 20);
        this.handSizeText = new Text(ui, new Vector2f(0, -this.getHeight(true) / 2), "Hand:",
                this.getWidth(true), 20, 20, 0, -1);
        this.deckSizeText = new Text(ui, new Vector2f(0, 0), "Deck:",
                this.getWidth(true), 20, 20, 0, 0);
        this.shadowsText = new Text(ui, new Vector2f(0, this.getHeight(true)/2), "Shadows:",
                this.getWidth(true), 20, 20, 0, 1);
        this.addChild(this.handSizeText);
        this.addChild(this.deckSizeText);
        this.addChild(this.shadowsText);
    }

    public void updateStats(Player p) {
        this.handSizeText.setText("Hand: " + p.getHand().size());
        this.deckSizeText.setText("Deck: " + p.getDeck().size());
        this.shadowsText.setText("Shadows: " + p.shadows);
    }
}
