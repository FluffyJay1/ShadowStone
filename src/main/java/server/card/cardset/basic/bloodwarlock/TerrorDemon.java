package server.card.cardset.basic.bloodwarlock;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.CardRarity;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.Event;
import server.event.EventUnleash;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class TerrorDemon extends MinionText {
    public static final String NAME = "Terror Demon";
    public static final String DESCRIPTION = "<b>Lifesteal</b>.\nWhenever an allied minion is <b>Unleashed</b> while this is in your hand, gain +1/+1/+0.";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/terrordemon.png",
            CRAFT, RARITY, 4, 2, 1, 4, true, TerrorDemon.class,
            new Vector2f(166, 134), 1.4, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.LIFESTEAL, Tooltip.UNLEASH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(EffectStats.LIFESTEAL, 1)
                .build()) {
            @Override
            public ResolverWithDescription onListenEvent(Event event) {
                if (!(event instanceof EventUnleash) || ((EventUnleash) event).source.team != this.owner.team
                        || !this.owner.status.equals(CardStatus.HAND)) {
                    return null;
                }
                // it's an unleash on our team
                String resolverDescription = "Whenever an allied minion is <b>Unleashed</b> while this is in your hand, gain +1/+1/+0.";
                Effect e = new Effect("", EffectStats.builder()
                        .change(EffectStats.ATTACK, 1)
                        .change(EffectStats.MAGIC, 1)
                        .build());
                return new ResolverWithDescription(resolverDescription, new AddEffectResolver(this.owner, e));
            }

            @Override
            public double getBattlecryValue(int refs) {
                // ok this doesn't actually have a battlecry but you do lose something when u play it
                return 1;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
