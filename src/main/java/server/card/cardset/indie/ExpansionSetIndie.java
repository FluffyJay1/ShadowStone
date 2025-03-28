package server.card.cardset.indie;

import server.card.cardset.CardSet;
import server.card.cardset.ExpansionSet;
import server.card.cardset.indie.bloodwarlock.Dedan;
import server.card.cardset.indie.dragondruid.Poniko;
import server.card.cardset.indie.dragondruid.Uboa;
import server.card.cardset.indie.forestrogue.Batter;
import server.card.cardset.indie.havenpriest.Ralsei;
import server.card.cardset.indie.neutral.*;
import server.card.cardset.indie.runemage.Japhet;
import server.card.cardset.indie.runemage.LordOfTheSecondZone;
import server.card.cardset.indie.shadowshaman.Enoch;
import server.card.cardset.indie.portalhunter.Judge;
import server.card.cardset.indie.swordpaladin.Susie;

public class ExpansionSetIndie extends ExpansionSet {
    public static final CardSet UNPLAYABLE_SET = new CardSet(new Spectre(), new Japhet(), new BikeEffect(), new CatEffect(), new FatEffect(),
            new KnifeEffect(), new MedamaudeEffect(), new MidgetEffect(), new TowelEffect(), new TriangleKerchiefEffect(), new Uboa(), new WitchEffect(),
            new EmotionSad(), new EmotionAngry(), new EmotionHappy());
    public static final CardSet PLAYABLE_SET = new CardSet(new Batter(), new Judge(), new Enoch(), new Elsen(), new Dedan(), new LordOfTheSecondZone(),
            new Madotsuki(), new Poniko(), new Ralsei(), new Susie(), new Omori());

    @Override
    public CardSet getCards() {
        return PLAYABLE_SET;
    }
}
