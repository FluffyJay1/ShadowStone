package server.card.cardset.anime.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSplash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
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

import java.util.List;
import java.util.stream.Collectors;

public class Mona extends MinionText {
    public static final String NAME = "Mona Megistus";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: Reduce all enemy minions' <b>Armor</b> by <b>S</b>.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Summon a <b>Phantom of Fate</b> and give it +0/+M/+M.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/mona.png"),
            CRAFT, TRAITS, RARITY, 3, 3, 1, 3, false, Mona.class,
            new Vector2f(150, 135), 1.6, new EventAnimationDamageSplash(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.ARMOR, Tooltip.SPELLBOOST, Tooltip.UNLEASH, PhantomOfFate.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.SPELLBOOSTABLE, 1)
                .build()) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.spellboosts;
                        Effect debuff = new Effect("-" + x + "<b>Armor</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .change(Stat.ARMOR, -x)
                                .build());
                        List<Minion> relevant = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new AddEffectResolver(relevant, debuff));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return AI.valueOfMinionDamage(owner.finalStats.get(Stat.MAGIC)) * 2;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        CreateCardResolver ccr = this.resolve(b, rq, el, new CreateCardResolver(new PhantomOfFate(), owner.team, CardStatus.BOARD, owner.getIndex() + 1));
                        int x = owner.finalStats.get(Stat.MAGIC);
                        Effect buff = new Effect("+0/+" + x + "/+" + x + " (from <b>Mona</b>).", EffectStats.builder()
                                .change(Stat.MAGIC, x)
                                .change(Stat.HEALTH, x)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(ccr.event.successfullyCreatedCards, buff));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.valueForBuff(0,owner.finalStats.get(Stat.MAGIC), 0) + AI.valueForSummoning(List.of(new PhantomOfFate().constructInstance(owner.board)), refs);
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
