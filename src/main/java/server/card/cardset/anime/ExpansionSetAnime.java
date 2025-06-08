package server.card.cardset.anime;

import server.card.cardset.CardSet;
import server.card.cardset.ExpansionSet;
import server.card.cardset.anime.bloodwarlock.Guts;
import server.card.cardset.anime.bloodwarlock.GutsBerserk;
import server.card.cardset.anime.bloodwarlock.Xiao;
import server.card.cardset.anime.bloodwarlock.YakshasMask;
import server.card.cardset.anime.dragondruid.*;
import server.card.cardset.anime.havenpriest.SesshouSakura;
import server.card.cardset.anime.havenpriest.YaeMiko;
import server.card.cardset.anime.neutral.Paimon;
import server.card.cardset.anime.forestrogue.Guoba;
import server.card.cardset.anime.forestrogue.Xiangling;
import server.card.cardset.anime.havenpriest.Aqua;
import server.card.cardset.anime.neutral.HopOnAbandonedArchive;
import server.card.cardset.anime.neutral.Jotaro;
import server.card.cardset.anime.neutral.YareYareDaze;
import server.card.cardset.anime.portalshaman.EyeOfStormyJudgement;
import server.card.cardset.anime.portalshaman.Kurumi;
import server.card.cardset.anime.portalshaman.RaidenShogun;
import server.card.cardset.anime.runemage.PhantomOfFate;
import server.card.cardset.anime.runemage.Mona;
import server.card.cardset.anime.runemage.Yoshino;
import server.card.cardset.anime.shadowdeathknight.Qiqi;
import server.card.cardset.anime.swordpaladin.BerserkerSoul;
import server.card.cardset.anime.swordpaladin.DandelionField;
import server.card.cardset.anime.swordpaladin.Jean;

public class ExpansionSetAnime extends ExpansionSet {
    public static final CardSet UNPLAYABLE_SET = new CardSet(new Guoba(), new YareYareDaze(), new GutsBerserk(), new OneHundredSitups(),
            new OneHundredSquats(), new TenKilometerRun(), new EverySingleDay(), new DandelionField(), new PhantomOfFate(),
            new YakshasMask(), new SesshouSakura(), new EyeOfStormyJudgement());
    public static final CardSet PLAYABLE_SET = new CardSet(new Kurumi(), new KingCrimson(), new BerserkerSoul(), new Xiangling(), new Yoshino(),
            new Jotaro(), new Aqua(), new Guts(), new Qiqi(), new OneHundredPushups(), new Jean(), new Mona(), new Zhongli(), new Xiao(), new YaeMiko(),
            new RaidenShogun(), new Paimon(), new HopOnAbandonedArchive());

    @Override
    public CardSet getCards() {
        return PLAYABLE_SET;
    }
}
