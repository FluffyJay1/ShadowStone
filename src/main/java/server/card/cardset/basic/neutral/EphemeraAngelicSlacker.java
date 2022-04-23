package server.card.cardset.basic.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.EffectUntilTurnEnd;
import server.event.Event;
import server.event.EventMinionAttack;
import server.resolver.AddEffectResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class EphemeraAngelicSlacker extends MinionText {
    public static final String NAME = "Ephemera, Angelic Slacker";
    public static final String DESCRIPTION = "<b>Stealth</b>.\nWhenever another allied minion attacks, give that minion +1/+0/+0 until the end of the turn.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/ephemeraangelicslacker.png",
            CRAFT, RARITY, 5, 1, 1, 3, true, EphemeraAngelicSlacker.class,
            new Vector2f(103, 133), 1.6, EventAnimationDamageSlash.class,
            () -> List.of(Tooltip.STEALTH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(EffectStats.STEALTH, 1)
                .build()
        ) {
            @Override
            public ResolverWithDescription onListenEvent(Event e) {
                if (e instanceof EventMinionAttack && this.owner.isInPlay() && ((EventMinionAttack) e).m1.team == this.owner.team
                        && ((EventMinionAttack) e).m1 != this.owner) {
                    String description = "Whenever another allied minion attacks, give that minion +1/+0/+0 until the end of the turn.";
                    Effect effect = new EffectUntilTurnEnd("+1/+0/+0 until the end of the turn (from <b>Ephemera, Angelic Slacker</b>).",
                            EffectStats.builder()
                                    .change(EffectStats.ATTACK, 1)
                                    .build());
                    return new ResolverWithDescription(description, new AddEffectResolver(((EventMinionAttack) e).m1, effect));
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                // idk it's pretty good
                return 3.5;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
