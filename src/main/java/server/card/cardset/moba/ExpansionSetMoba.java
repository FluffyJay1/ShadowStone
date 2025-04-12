package server.card.cardset.moba;

import server.card.cardset.CardSet;
import server.card.cardset.ExpansionSet;
import server.card.cardset.moba.bloodwarlock.Soldier;
import server.card.cardset.moba.dragondruid.Pyro;
import server.card.cardset.moba.dragondruid.SiegeTank;
import server.card.cardset.moba.forestrogue.Sniper;
import server.card.cardset.moba.havenpriest.Medic;
import server.card.cardset.moba.neutral.LilShredder;
import server.card.cardset.moba.neutral.Scout;
import server.card.cardset.moba.neutral.Timbersaw;
import server.card.cardset.moba.neutral.Tiny;
import server.card.cardset.moba.portalhunter.Engineer;
import server.card.cardset.moba.portalhunter.EngineersBuildingDispenser;
import server.card.cardset.moba.portalhunter.EngineersBuildingSentry;
import server.card.cardset.moba.portalhunter.EngineersBuildingTeleporter;
import server.card.cardset.moba.portalhunter.Immortal;
import server.card.cardset.moba.runemage.Demoman;
import server.card.cardset.moba.runemage.LochnLoad;
import server.card.cardset.moba.runemage.StickyTrap;
import server.card.cardset.moba.shadowshaman.Baneling;
import server.card.cardset.moba.shadowshaman.BanelingBust;
import server.card.cardset.moba.shadowshaman.Lich;
import server.card.cardset.moba.shadowshaman.Spy;
import server.card.cardset.moba.shadowshaman.Ultralisk;
import server.card.cardset.moba.swordpaladin.Beastmaster;
import server.card.cardset.moba.swordpaladin.Heavy;

public class ExpansionSetMoba extends ExpansionSet {
    public static final CardSet UNPLAYABLE_SET = new CardSet(new StickyTrap(), new EngineersBuildingSentry(), new EngineersBuildingDispenser(), new EngineersBuildingTeleporter());
    public static final CardSet PLAYABLE_SET = new CardSet(new SiegeTank(), new Tiny(), new Immortal(), new LochnLoad(), new Baneling(),
            new BanelingBust(), new Beastmaster(), new Lich(), new Ultralisk(), new Timbersaw(), new LilShredder(), new Scout(), new Sniper(),
            new Heavy(), new Demoman(), new Pyro(), new Soldier(), new Spy(), new Medic(), new Engineer());
    @Override
    public CardSet getCards() {
        return PLAYABLE_SET;
    }
}
