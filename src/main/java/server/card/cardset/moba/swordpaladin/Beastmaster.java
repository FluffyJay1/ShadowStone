package server.card.cardset.moba.swordpaladin;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;

import server.ServerBoard;
import server.card.*;
import server.card.cardset.standard.neutral.StonetuskBoar;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Beastmaster extends MinionText {
    public static final String NAME = "Beastmaster";
    public static final String DESCRIPTION = "<b>Aura</b>: adjacent minions have +1 attacks per turn.\n<b>Unleash</b>: Summon M <b>Stonetusk Boars</b>.";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/beastmaster.png"),
            CRAFT, TRAITS, RARITY, 5, 2, 2, 4, false, Beastmaster.class,
            new Vector2f(140, 100), 2, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.AURA, Tooltip.UNLEASH, StonetuskBoar.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        Effect auraBuff = new Effect("+1 attacks per turn (from <b>Beastmaster's Aura</b>).");
        auraBuff.effectStats.change.set(Stat.ATTACKS_PER_TURN, 1);
        return List.of(new EffectAura(DESCRIPTION, 1, true, false, auraBuff) {
            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply instanceof Minion && Math.abs(cardToApply.getIndex() - this.owner.getIndex()) == 1;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                String resolverDescription = "<b>Unleash</b>: Summon M <b>Stonetusk Boars</b>.";
                return new ResolverWithDescription(resolverDescription, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int x = owner.finalStats.get(Stat.MAGIC);
                        List<CardText> summons = Collections.nCopies(x, new StonetuskBoar());
                        List<Integer> pos = IntStream.range(0, x)
                                .map(i -> owner.getIndex() + (i + 1) * (i % 2)) // 0 2 0 4 0 6...
                                .boxed()
                                .collect(Collectors.toList());
                        this.resolve(b, rq, el, new CreateCardResolver(summons, owner.team, CardStatus.BOARD, pos));
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return 3 + owner.finalStats.get(Stat.MAGIC) / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
