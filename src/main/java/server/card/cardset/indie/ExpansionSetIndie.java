package server.card.cardset.indie;

import server.card.cardset.CardSet;
import server.card.cardset.ExpansionSet;
import server.card.cardset.indie.forestrogue.Batter;
import server.card.cardset.indie.portalhunter.Judge;
import server.card.cardset.indie.shadowshaman.Spectre;

public class ExpansionSetIndie extends ExpansionSet {
    public static final CardSet UNPLAYABLE_SET = new CardSet(new Spectre());
    public static final CardSet PLAYABLE_SET = new CardSet(new Batter(), new Judge());

    @Override
    public CardSet getCards() {
        return PLAYABLE_SET;
    }
}
