package client.ui.game.eventanimation.board;

import client.ui.game.*;
import client.ui.game.eventanimation.*;
import server.event.*;

//TODO figure out why i wrote this class anyway
public abstract class EventAnimationBoard extends EventAnimation {
	public UIBoard uiboard;

	public EventAnimationBoard() {
		this(1);
	}

	public EventAnimationBoard(double duration) {
		super(duration);
	}

	public void init(Event event, UIBoard uiboard) {
		this.uiboard = uiboard;
		super.init(event);
	}
}
