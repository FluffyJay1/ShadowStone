package server.playeraction;

// Class that allows the ID of a player action to be
// turned into its corresponding object.
public class ActionIDLinker {

    public static Class<? extends PlayerAction> getClass(int id) {
        // next id is 5
        return switch (id) {
            case EndTurnAction.ID -> EndTurnAction.class;
            case OrderAttackAction.ID -> OrderAttackAction.class;
            case PlayCardAction.ID -> PlayCardAction.class;
            case UnleashMinionAction.ID -> UnleashMinionAction.class;
            default -> null;
        };
    }

}
