package server.playeraction;

import java.lang.reflect.*;
import java.util.*;

import server.*;
import server.event.*;

public abstract class PlayerAction {
	int id = 0; // literally just copying off of event

	public PlayerAction(int id) {
		this.id = id;
	}

	public List<Event> perform(Board b) {
		return new LinkedList<Event>();
	}

	@Override
	public String toString() {
		return this.id + "\n";
	}

	public static PlayerAction createFromString(Board b, StringTokenizer st) {
		int id = Integer.parseInt(st.nextToken());
		Class c = ActionIDLinker.getClass(id);
		PlayerAction e = null;
		try {
			e = (PlayerAction) c.getMethod("fromString", Board.class, StringTokenizer.class).invoke(null, b, st);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return e;
	}
}
