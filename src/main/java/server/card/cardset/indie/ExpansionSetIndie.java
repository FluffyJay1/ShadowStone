package server.card.cardset.indie;

import server.card.cardset.CardSet;
import server.card.cardset.ExpansionSet;
import server.card.cardset.indie.bloodwarlock.Dedan;
import server.card.cardset.indie.forestrogue.Batter;
import server.card.cardset.indie.neutral.*;
import server.card.cardset.indie.runemage.Japhet;
import server.card.cardset.indie.runemage.LordOfTheSecondZone;
import server.card.cardset.indie.shadowshaman.Enoch;
import server.card.cardset.indie.portalhunter.Judge;

public class ExpansionSetIndie extends ExpansionSet {
    public static final CardSet UNPLAYABLE_SET = new CardSet(new Spectre(), new Japhet(), new BikeEffect(), new CatEffect(), new FatEffect(),
            new KnifeEffect(), new MedamaudeEffect(), new MidgetEffect(), new TowelEffect(), new TriangleKerchiefEffect());
    public static final CardSet PLAYABLE_SET = new CardSet(new Batter(), new Judge(), new Enoch(), new Elsen(), new Dedan(), new LordOfTheSecondZone(),
            new Madotsuki());

    @Override
    public CardSet getCards() {
        return PLAYABLE_SET;
    }
}
