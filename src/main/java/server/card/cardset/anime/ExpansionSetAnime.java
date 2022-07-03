package server.card.cardset.anime;

import server.card.cardset.CardSet;
import server.card.cardset.ExpansionSet;
import server.card.cardset.anime.bloodwarlock.Guts;
import server.card.cardset.anime.bloodwarlock.GutsBerserk;
import server.card.cardset.anime.portalhunter.Kurumi;
import server.card.cardset.anime.dragondruid.KingCrimson;
import server.card.cardset.anime.forestrogue.Guoba;
import server.card.cardset.anime.forestrogue.Xiangling;
import server.card.cardset.anime.havenpriest.Aqua;
import server.card.cardset.anime.neutral.Jotaro;
import server.card.cardset.anime.neutral.YareYareDaze;
import server.card.cardset.anime.runemage.Yoshino;
import server.card.cardset.anime.shadowshaman.Qiqi;
import server.card.cardset.anime.swordpaladin.BerserkerSoul;

public class ExpansionSetAnime extends ExpansionSet {
    public static final CardSet UNPLAYABLE_SET = new CardSet(new Guoba(), new YareYareDaze(), new GutsBerserk());
    public static final CardSet PLAYABLE_SET = new CardSet(new Kurumi(), new KingCrimson(), new BerserkerSoul(), new Xiangling(), new Yoshino(),
            new Jotaro(), new Aqua(), new Guts(), new Qiqi());

    @Override
    public CardSet getCards() {
        return PLAYABLE_SET;
    }
}
