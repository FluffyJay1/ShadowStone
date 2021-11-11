package server.playeraction;

// Class that allows the ID of a player action to be
// turned into its corresponding object.
public class ActionIDLinker {

    public ActionIDLinker() {
        // TODO Auto-generated constructor stub
    }

    public static Class<? extends PlayerAction> getClass(int id) {
        // next id is 5
        switch (id) {
        case EndTurnAction.ID:
            return EndTurnAction.class;
        case OrderAttackAction.ID:
            return OrderAttackAction.class;
        case PlayCardAction.ID:
            return PlayCardAction.class;
        case UnleashMinionAction.ID:
            return UnleashMinionAction.class;
        default:
            return null;
        }
    }

}
