package client.ui.game;

import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;
import org.newdawn.slick.geom.Vector2f;
import server.Player;
import server.card.ClassCraft;
import server.card.Leader;


public class ClassCraftTrackerPanel extends UIBox {
    Player p;
    Text text;

    public ClassCraftTrackerPanel(UI ui, Vector2f pos, Player p) {
        super(ui, pos, new Vector2f(200, 66), "res/ui/uiboxborder.png");
        this.p = p;
        this.text = new Text(ui, new Vector2f(), "", 180, 20, 20, 0, 0);
        this.addChild(this.text);
        this.updateTrackerText();
    }

    public void updateTrackerText() {
        String trackerText = this.calculateTracker();
        if (trackerText == null) {
            this.setVisible(false);
        } else {
            this.setVisible(true);
            this.text.setText(trackerText);
        }
    }

    private String calculateTracker() {
        if (this.p.getLeader().isPresent()) {
            Leader l = this.p.getLeader().get();
            ClassCraft craft = l.getTooltip().craft;
            return switch (craft) {
                case FORESTROGUE -> String.format("Cards played: %d", p.cardsPlayedThisTurn);
                case DRAGONDRUID -> p.overflow() ? "<b>Overflow</b>: <b>ACTIVE</b>" : String.format("<b>Overflow</b>: %d mana remaining", Player.OVERFLOW_THRESHOLD - p.maxmana);
                case BLOODWARLOCK -> p.vengeance() ? "<b>Vengeance</b>: <b>ACTIVE</b>" : String.format("<b>Vengeance</b>: %d health remaining", l.health - Player.VENGEANCE_THRESHOLD);
                case PORTALHUNTER -> p.resonance() ? "<b>Resonance</b>: <b>ACTIVE</b>" : "<b>Resonance</b>: inactive";
                default -> null;
            };
        }
        return null;
    }
}
