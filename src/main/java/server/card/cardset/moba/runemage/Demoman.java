package server.card.cardset.moba.runemage;

import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSmallExplosion;
import server.ServerBoard;
import server.ai.AI;
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
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class Demoman extends MinionText {
    public static final String NAME = "Demoman";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Gain +<b>S</b>/+0/+0.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Summon a <b>Sticky Trap</b> and give it +0/+0/+M, then gain <b>Rush</b>.";
    public static final String DESCRIPTION = "<b>Cleave</b>.\n" + BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/demoman.png"),
            CRAFT, TRAITS, RARITY, 4, 2, 2, 4, false, Demoman.class,
            new Vector2f(), -1, new EventAnimationDamageSmallExplosion(),
            () -> List.of(Tooltip.CLEAVE, Tooltip.BATTLECRY, Tooltip.SPELLBOOST, Tooltip.UNLEASH, StickyTrap.TOOLTIP, Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.CLEAVE, 1)
                .set(Stat.SPELLBOOSTABLE, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.spellboosts;
                        Effect buff = new Effect("+" + x + "/+0/+0 (from <b>Battlecry</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, x)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueForBuff(owner.finalStats.get(Stat.MAGIC), 0, 0);
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        CreateCardResolver ccr = this.resolve(b, rq, el, new CreateCardResolver(new StickyTrap(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+0/+0/+" + x + " (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .change(Stat.HEALTH, x)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(ccr.event.successfullyCreatedCards, buff));
                        Effect rush = new Effect("<b>Rush</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.RUSH, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(owner, rush));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueForBuff(0,owner.finalStats.get(Stat.MAGIC), 0) + AI.valueForSummoning(List.of(new StickyTrap().constructInstance(owner.board)), refs);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}

