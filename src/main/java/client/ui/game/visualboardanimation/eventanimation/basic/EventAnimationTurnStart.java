package client.ui.game.visualboardanimation.eventanimation.basic;

import client.ui.interpolation.Interpolation;
import client.ui.interpolation.meta.ComposedInterpolation;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.ClampedInterpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import org.newdawn.slick.*;

import client.*;
import client.Game;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.Player;
import server.event.*;

import java.util.List;

public class EventAnimationTurnStart extends EventAnimation<EventTurnStart> {
    private final Interpolation<Double> TEXT_SLIDE_X = new ComposedInterpolation<>(new ClampedInterpolation(0, 1.5),
            new SequentialInterpolation<>(List.of(new QuadraticInterpolationA(-0.3, -0.03, -0.2), new LinearInterpolation(-0.03, 0.03), new QuadraticInterpolationA(0.03, 0.3, 0.2)),
                    List.of(0.2, 0.6, 0.2)));
    private final Interpolation<Double> TEXT_ALPHA = new ComposedInterpolation<>(new ClampedInterpolation(0, 1.5),
            new SequentialInterpolation<>(List.of(new LinearInterpolation(0, 1), new ConstantInterpolation(1), new LinearInterpolation(1, 0)),
                    List.of(0.2, 0.6, 0.2)));
    private final Interpolation<Double> FADE_ALPHA = new ComposedInterpolation<>(new ClampedInterpolation(0, 1.5), new QuadraticInterpolationA(0, 0, -3));

    public EventAnimationTurnStart() {
        super(0.5, 1);
    }

    @Override
    public boolean shouldAnimate() {
        return true;
    }

    @Override
    public void onProcess() {
        this.visualBoard.disableInput = this.event.p.team != this.visualBoard.getLocalteam();
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(new Color(0, 0, 0, FADE_ALPHA.get(this.getTime()).floatValue()));
        g.fillRect(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        UnicodeFont fontbig = Game.getFont(80, true, false);
        UnicodeFont fontsmall = Game.getFont(50, true, false);
        String dstring = "TURN START", tstring = "", ustring = "";
        int unleashTurn = 0;
        switch (this.event.p.team * this.event.p.board.getLocalteam()) { // ez hack
            case 1 -> {
                g.setColor(new Color(0f, 1f, 1f, TEXT_ALPHA.get(this.getTime()).floatValue()));
                dstring = "YOUR TURN";
            }
            case -1 -> {
                g.setColor(new Color(1f, 0f, 0f, TEXT_ALPHA.get(this.getTime()).floatValue()));
                dstring = "OPPONENT'S TURN";
            }
        }
        switch (this.event.p.team) {
            case 1 -> unleashTurn = Player.UNLEASH_FIRST_TURN;
            case -1 -> unleashTurn = Player.UNLEASH_SECOND_TURN;
        }
        tstring = "Turn " + this.event.p.turn;
        if (this.event.p.turn < unleashTurn) {
            int remaining = unleashTurn - this.event.p.turn;
            if (remaining == 1) {
                ustring = "Can unleash in 1 turn";
            } else {
                ustring = "Can unleash in " + (unleashTurn - this.event.p.turn) + " turns";
            }
        } else if (this.event.p.turn == unleashTurn && !this.isPre()) {
            ustring = "CAN UNLEASH";
        }
        float drawX = (float) (this.TEXT_SLIDE_X.get(this.getTime()) * Config.WINDOW_WIDTH);
        g.setFont(fontbig);
        g.drawString(dstring, drawX + Config.WINDOW_WIDTH / 2 - fontbig.getWidth(dstring) / 2,
                Config.WINDOW_HEIGHT / 2 - fontbig.getHeight(dstring));
        g.setFont(fontsmall);
        g.drawString(tstring, drawX + Config.WINDOW_WIDTH / 2 - fontsmall.getWidth(tstring) / 2,
                Config.WINDOW_HEIGHT / 2 - fontsmall.getHeight(tstring) / 2 + 100);
        g.drawString(ustring, drawX + Config.WINDOW_WIDTH / 2 - fontsmall.getWidth(ustring) / 2,
                Config.WINDOW_HEIGHT / 2 - fontsmall.getHeight(ustring) / 2 + 200);
        g.setColor(Color.white);
    }
}
