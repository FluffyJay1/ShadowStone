package server.playeraction;

import java.util.*;

import server.*;
import server.resolver.*;

public class EndTurnAction extends PlayerAction {

    public static final int ID = 4;

    public final int team;

    public EndTurnAction(int team) {
        super(ID);

        this.team = team;
        // TODO Auto-generated constructor stub
    }

    @Override
    public ResolutionResult perform(ServerBoard b) {
        if (team == b.getCurrentPlayerTurn()) {
            return b.endCurrentPlayerTurn();
        }
        return new ResolutionResult();
    }

    @Override
    public String toString() {
        return this.id + " " + this.team + " ";
    }

    public static EndTurnAction fromString(Board b, StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        return new EndTurnAction(team);
    }
}
