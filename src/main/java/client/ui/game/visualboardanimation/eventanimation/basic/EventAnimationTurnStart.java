package client.ui.game.visualboardanimation.eventanimation.basic;

import org.newdawn.slick.*;

import client.*;
import client.Game;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationTurnStart extends EventAnimation<EventTurnStart> {
    public EventAnimationTurnStart() {
        super(0, 0.5);
    }

    @Override
    public void onProcess() {
        this.visualBoard.disableInput = this.event.p.team != this.visualBoard.localteam;
    }

    @Override
    public void draw(Graphics g) {
        UnicodeFont font = Game.getFont(Game.DEFAULT_FONT, 80, true, false);
        String dstring = "TURN START";
        switch (this.event.p.team * this.event.p.board.localteam) { // ez hack
        case 1:
            g.setColor(Color.cyan);
            dstring = "YOUR TURN";
            break;
        case -1:
            g.setColor(Color.red);
            dstring = "OPPONENT'S TURN";
            break;
        }
        g.setFont(font);
        g.drawString(dstring, Config.WINDOW_WIDTH / 2 - font.getWidth(dstring) / 2,
                Config.WINDOW_HEIGHT / 2 - font.getHeight(dstring));
        g.setColor(Color.white);
    }
}
