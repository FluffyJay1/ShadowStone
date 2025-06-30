package client;

import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import server.Board;
import server.Player;
import server.card.BoardObject;
import server.card.CardText;
import server.card.cardset.anime.dragondruid.KingCrimson;
import server.card.cardset.anime.neutral.Jotaro;
import server.card.cardset.anime.portalshaman.AdultNatsumi;
import server.card.cardset.anime.portalshaman.Kurumi;
import server.card.cardset.anime.portalshaman.Natsumi;
import server.card.cardset.anime.runemage.Megumin;
import server.card.cardset.anime.runemage.Yoshino;
import server.card.cardset.anime.swordpaladin.BerserkerSoul;
import server.card.cardset.indie.bloodwarlock.Dedan;
import server.card.cardset.indie.dragondruid.Uboa;
import server.card.cardset.indie.forestrogue.Batter;
import server.card.cardset.indie.havenpriest.Ralsei;
import server.card.cardset.indie.portalshaman.Judge;
import server.card.cardset.indie.runemage.Japhet;
import server.card.cardset.indie.shadowdeathknight.Enoch;
import server.card.cardset.indie.shadowdeathknight.Sans;
import server.card.cardset.indie.shadowdeathknight.TheGreatPapyrus;
import server.card.cardset.indie.swordpaladin.Susie;
import server.card.cardset.special.batter.Alpha;
import server.card.cardset.special.batter.Epsilon;
import server.card.cardset.special.batter.Omega;
import server.card.cardset.special.kurumi.CityOfDevouringTime;
import server.card.cardset.special.omori.OmoriDidNotSuccumb;
import server.card.effect.Stat;
import server.event.eventgroup.EventGroupType;

import java.util.List;
import java.util.function.Predicate;

/**
 * Manages the playing of music depending on what the board state is. The idea
 * behind this is that certain character's themes will play when they hit the
 * board.
 */
public class MusicThemeController {
    private static final double FADE_IN_TIME = 0.5;
    private static final double FADE_OUT_TIME = 0.5;

    private static List<Theme> THEMES;
    private double fadeOutTimer; // if > 0, then we are currently fading out of the current theme
    private Theme currentTheme;
    private Theme targetTheme; // if != currentTheme, then we should be currently fading out of the current theme

    public static void initThemes() throws SlickException {
        // higher on the list = higher priority
        THEMES = List.of(
                new Theme("music/explosion.ogg",
                        b -> b.peekEventGroup() != null && b.peekEventGroup().type.equals(EventGroupType.UNLEASH)
                                && b.peekEventGroup().cards.stream()
                                        .anyMatch(c -> c.getCardText() instanceof Megumin
                                                && c.finalStats.get(Stat.MAGIC) >= Megumin.MAGIC_THRESHOLD
                                                && b.getPlayer(c.team).mana >= Megumin.MANA_THRESHOLD),
                        false),
                new Theme("music/fang_of_critias.ogg", b -> b.getMinions(0, false, true)
                        .anyMatch(m -> m.getFinalEffects(true).anyMatch(e -> e instanceof BerserkerSoul.EffectBerserkerSoul))),
                new Theme("music/omori_omega.ogg", b -> cardInPlayMatches(b, bo -> bo.getCardText().equals(new OmoriDidNotSuccumb()) && b.getPlayerCard(bo.team, Player::getLeader).anyMatch(l -> l.health == 1))),
                new Theme("music/omori.ogg", b -> cardIsInPlay(b, new OmoriDidNotSuccumb())),
                new Theme("music/avatar_beat.ogg", b -> cardIsInPlay(b, new Judge())),
                new Theme("music/uboa.ogg", b -> cardIsInPlay(b, new Uboa())),
                new Theme("music/stardust_crusaders.ogg", b -> cardIsInPlay(b, new Jotaro())),
                new Theme("music/megalovania.ogg", b -> cardIsInPlay(b, new Sans())),
                new Theme("music/rhapsody_rage.ogg", b -> cardIsInPlay(b, new Kurumi())),
                new Theme("music/o_rosto_de_um_assassino.ogg", b -> cardIsInPlay(b, new Enoch())),
                new Theme("music/minuit_a_fond_la_caisse.ogg", b -> cardIsInPlay(b, new Japhet())),
                new Theme("music/fake_orchestra.ogg", b -> cardIsInPlay(b, new Dedan())),
                new Theme("music/rhapsody_flame.ogg", b -> cardIsInPlay(b, new CityOfDevouringTime())),
                new Theme("music/king_crimson.ogg", b -> cardIsInPlay(b, new KingCrimson())),
                new Theme("music/bonetrousle.ogg", b -> cardIsInPlay(b, new TheGreatPapyrus())),
                new Theme("music/natsumi2.ogg", b -> cardIsInPlay(b, new AdultNatsumi())),
                new Theme("music/rain_in_the_park.ogg", b -> cardIsInPlay(b, new Yoshino())),
                new Theme("music/rude_buster.ogg", b -> anyCardIsInPlay(b, List.of(new Ralsei(), new Susie()))),
                new Theme("music/pepper_steak.ogg", b -> anyCardIsInPlay(b, List.of(new Batter(), new Alpha(), new Omega(), new Epsilon()))),
                new Theme("music/hidden_trick.ogg", b -> cardIsInPlay(b, new Natsumi())),
                new Theme("music/fighto.ogg", b -> true)
        );
    }

    public MusicThemeController() {
        this.fadeOutTimer = 0;
    }

    public void update(double frametime) {
        if (this.fadeOutTimer > 0) {
            this.fadeOutTimer -= frametime;
            if (this.fadeOutTimer <= 0 && this.currentTheme != null) {
                this.currentTheme.finishFadeOut();
                this.currentTheme = null;
            }
        }
        if (this.currentTheme == null && this.targetTheme != null) {
            this.currentTheme = this.targetTheme;
            this.currentTheme.beginFadeIn();
        }
    }

    public void updateThemeChoice(Board b) {
        this.targetTheme = THEMES.stream().filter(t -> t.shouldPlay(b)).findFirst().orElse(null);
        if (this.currentTheme != null && this.targetTheme != this.currentTheme && this.fadeOutTimer <= 0) {
            this.currentTheme.beginFadeOut();
            this.fadeOutTimer = FADE_OUT_TIME;
        }
    }

    public void stop() {
        if (this.currentTheme != null) {
            this.currentTheme.beginFadeOut();
            this.currentTheme.finishFadeOut();
        }
    }

    private static boolean cardInPlayMatches(Board b, Predicate<BoardObject> predicate) {
        return b.getBoardObjects(0, true, true, true, true).anyMatch(predicate);
    }

    private static boolean cardIsInPlay(Board b, CardText card) {
        return cardInPlayMatches(b, bo -> bo.getCardText().equals(card));
    }

    private static boolean anyCardIsInPlay(Board b, List<CardText> cards) {
        return cardInPlayMatches(b, bo -> cards.contains(bo.getCardText()));
    }

    private static class Theme {
        private final Predicate<Board> playCondition;
        private final Music music;
        private final boolean shouldLoop;

        public Theme(String path, Predicate<Board> playCondition, boolean shouldLoop) throws SlickException {
            // streaming works only if we don't seek
            this.music = new Music(path, true);
            this.playCondition = playCondition;
            this.shouldLoop = shouldLoop;
        }
        public Theme(String path, Predicate<Board> playCondition) throws SlickException {
            this(path, playCondition, true);
        }

        public boolean shouldPlay(Board b) {
            return this.playCondition.test(b);
        }

        public void beginFadeOut() {
            this.music.fade((int) (FADE_OUT_TIME * 1000), 0, true); // this shit doesn't work as advertised
        }

        public void finishFadeOut() {
            this.music.stop();
        }

        public void beginFadeIn() {
            if (this.shouldLoop) {
                this.music.loop();
            } else {
                this.music.play();
            }
            this.music.setVolume(0);
            this.music.fade((int) (FADE_IN_TIME * 1000), 1, false);
        }
    }
}
