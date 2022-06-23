package server.card.cardset.moba;

import server.card.cardset.CardSet;
import server.card.cardset.ExpansionSet;
import server.card.cardset.moba.dragondruid.SiegeTank;
import server.card.cardset.moba.neutral.Tiny;
import server.card.cardset.moba.portalhunter.Immortal;
import server.card.cardset.moba.runemage.LochnLoad;
import server.card.cardset.moba.shadowshaman.Baneling;
import server.card.cardset.moba.shadowshaman.BanelingBust;
import server.card.cardset.moba.swordpaladin.Beastmaster;

public class ExpansionSetMoba extends ExpansionSet {
    public static final CardSet UNPLAYABLE_SET = new CardSet();
    public static final CardSet PLAYABLE_SET = new CardSet(new SiegeTank(), new Tiny(), new Immortal(), new LochnLoad(), new Baneling(),
            new BanelingBust(), new Beastmaster());
    @Override
    public CardSet getCards() {
        return PLAYABLE_SET;
    }
}
