package client.ui.game;

import org.newdawn.slick.geom.*;

import client.ui.*;
import server.playeraction.*;

import java.io.IOException;

public class EndTurnButton extends UIBox {
    final UIBoard b;
    final Text text;

    public EndTurnButton(UI ui, UIBoard b) {
        super(ui, new Vector2f(0.38f, 0), new Vector2f(128, 128), "res/ui/border.png");
        this.text = new Text(ui, new Vector2f(0, 0), "<b>END TURN</b>", 128, 24, 30, 0, 0);
        text.setParent(this);
        this.b = b;
        this.relpos = true;
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        this.setVisible(!this.b.b.disableInput && this.b.b.getCurrentPlayerTurn() == this.b.b.getLocalteam());
    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        if (this.pointIsInHitbox(x, y)) {
            if (!this.b.b.disableInput) {
                try {
                    this.b.ds.sendPlayerAction(new EndTurnAction(this.b.b.getLocalteam()).toString());
                } catch (IOException e) {
                    this.b.connectionClosed = true;
                    this.b.onConnectionClosed.run();
                }
                this.b.handleTargeting(null);
                this.b.b.disableInput = true;
            }
        }
    }
}
