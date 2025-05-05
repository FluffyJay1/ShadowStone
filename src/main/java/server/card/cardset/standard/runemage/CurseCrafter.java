package server.card.cardset.standard.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.PutCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.Collections;
import java.util.List;

public class CurseCrafter extends MinionText {
    public static final String NAME = "Curse Crafter";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Summon a <b>" + DemonicShikigami.NAME + "</b>. Randomly put 2 different cards with <b>Spellboost</b> from your deck into your hand.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Summon a <b>" + PaperShikigami.NAME + "</b> and gain +M/+0/+0 and <b>Rush</b> until the end of the turn.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/cursecrafter.png"),
            CRAFT, TRAITS, RARITY, 5, 1, 2, 1, false, CurseCrafter.class,
            new Vector2f(129, 139), 1.7, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.BATTLECRY, DemonicShikigami.TOOLTIP, Tooltip.SPELLBOOST, Tooltip.UNLEASH, PaperShikigami.TOOLTIP, Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstancesBattlecry; // for getBattlecryValue, preview the value of the created cards
            private List<Card> cachedInstancesUnleash; // for getUnleashValue, preview the value of the created cards

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new CreateCardResolver(new DemonicShikigami(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                        List<Card> relevant = owner.player.getDeck().stream()
                                .filter(c -> c.finalStats.get(Stat.SPELLBOOSTABLE) > 0)
                                .toList();
                        if (!relevant.isEmpty()) {
                            List<Card> selection = SelectRandom.from(relevant, 2);
                            List<Integer> pos = Collections.nCopies(selection.size(), -1);
                            this.resolve(b, rq, el, new PutCardResolver(selection, CardStatus.HAND, owner.team, pos, true));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstancesBattlecry == null) {
                    this.cachedInstancesBattlecry = List.of(new DemonicShikigami().constructInstance(this.owner.board));
                }
                return AI.VALUE_PER_CARD_IN_HAND * 2 + AI.valueForSummoning(this.cachedInstancesBattlecry, refs);
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        this.resolve(b, rq, el, new CreateCardResolver(new PaperShikigami(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+" + x + "/+0/+0 and <b>Rush</b> until the end of the turn (from <b>Unleash</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, x)
                                .set(Stat.RUSH, 1)
                                .build(),
                                e -> e.untilTurnEndTeam = 0);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                if (this.cachedInstancesUnleash == null) {
                    this.cachedInstancesUnleash = List.of(new PaperShikigami().constructInstance(this.owner.board));
                }
                return (AI.valueForSummoning(this.cachedInstancesUnleash, refs) + AI.valueOfRush(this.owner.finalStats.get(Stat.MAGIC) + this.owner.finalStats.get(Stat.ATTACK))) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
