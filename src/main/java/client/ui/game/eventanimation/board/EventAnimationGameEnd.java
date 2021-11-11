package client.ui.game.eventanimation.board;

import org.newdawn.slick.*;

import client.*;
import client.Game;
import client.ui.game.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationGameEnd extends EventAnimation<EventGameEnd> {
    public EventAnimationGameEnd() {
        super(0, 0.3);
    }

    @Override
    public void draw(Graphics g) {
        EventGameEnd e = (EventGameEnd) this.event;
        UnicodeFont font = Game.getFont("Verdana", 80, true, false);
        String dstring = "GAME END";
        switch (e.victory * this.visualBoard.localteam) { // ez hack
        case 1:
            g.setColor(Color.cyan);
            dstring = "YOU WIN";
            break;
        case -1:
            g.setColor(Color.red);
            dstring = "UR ASS";
            break;
        }
        g.setFont(font);
        g.drawString(dstring, Config.WINDOW_WIDTH / 2 - font.getWidth(dstring) / 2,
                Config.WINDOW_HEIGHT / 2 - font.getHeight(dstring));
        g.setColor(Color.white);
    }
}
