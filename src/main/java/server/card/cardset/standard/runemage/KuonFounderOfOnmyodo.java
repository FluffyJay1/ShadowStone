package server.card.cardset.standard.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.ai.AI;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.CardText;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectSpellboostDiscount;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.IntStream;

public class KuonFounderOfOnmyodo extends MinionText {
    public static final String NAME = "Kuon, Founder of Onmyodo";
    public static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Summon a <b>" + CelestialShikigami.NAME + "</b>, <b>" + DemonicShikigami.NAME + "</b>, and <b>" + PaperShikigami.NAME + "</b>.";
    public static final String ONLISTENEVENT_DESCRIPTION = "Whenever an allied <b>" + DemonicShikigami.NAME + "</b> or <b>" + PaperShikigami.NAME + "</b> comes into play, give it <b>Storm</b>.";
    public static final String DESCRIPTION = EffectSpellboostDiscount.DESCRIPTION + "\n" + BATTLECRY_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/kuonfounderofonmyodo.png"),
            CRAFT, TRAITS, RARITY, 15, 5, 3, 5, true, KuonFounderOfOnmyodo.class,
            new Vector2f(137, 131), 2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.SPELLBOOST, Tooltip.BATTLECRY, CelestialShikigami.TOOLTIP, DemonicShikigami.TOOLTIP, PaperShikigami.TOOLTIP, Tooltip.STORM),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new Effect(BATTLECRY_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION) {
                    private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards

                    @Override
                    public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                        return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                            @Override
                            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                List<CardText> summons = List.of(new CelestialShikigami(), new DemonicShikigami(), new PaperShikigami());
                                List<Integer> pos = IntStream.range(owner.getIndex() + 1, owner.getIndex() + 1 + summons.size()).boxed().toList();
                                this.resolve(b, rq, el, new CreateCardResolver(summons, owner.team, CardStatus.BOARD, pos));
                            }
                        });
                    }

                    @Override
                    public double getBattlecryValue(int refs) {
                        if (this.cachedInstances == null) {
                            this.cachedInstances = List.of(new CelestialShikigami().constructInstance(this.owner.board), new DemonicShikigami().constructInstance(this.owner.board), new PaperShikigami().constructInstance(this.owner.board));
                        }
                        return AI.valueForSummoning(this.cachedInstances, refs);
                    }

                    @Override
                    public ResolverWithDescription onListenEventWhileInPlay(Event e) {
                        if (e != null) {
                            List<BoardObject> relevant = e.cardsEnteringPlay().stream()
                                    .filter(bo -> (bo.getCardText() instanceof DemonicShikigami || bo.getCardText() instanceof PaperShikigami) && bo != this.owner && bo.team == this.owner.team)
                                    .toList();
                            if (!relevant.isEmpty()) {
                                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                                    @Override
                                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                                        Effect buff = new Effect("<b>Storm</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                                .set(Stat.STORM, 1)
                                                .build());
                                        this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                                    }
                                });
                            }
                        }
                        return null;
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        // idk
                        return AI.valueOfStorm(4);
                    }
                },
                new EffectSpellboostDiscount()
        );
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
