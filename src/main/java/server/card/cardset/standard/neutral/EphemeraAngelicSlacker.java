package server.card.cardset.standard.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageArrow;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
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
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/standard/ephemeraangelicslacker.png",
            CRAFT, TRAITS, RARITY, 5, 1, 1, 3, true, EphemeraAngelicSlacker.class,
            new Vector2f(103, 133), 1.6, new EventAnimationDamageArrow(),
            () -> List.of(Tooltip.STEALTH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.STEALTH, 1)
                .build()
        ) {
            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event instanceof EventMinionAttack && ((EventMinionAttack) event).m1.team == this.owner.team
                        && ((EventMinionAttack) event).m1 != this.owner) {
                    String description = "Whenever another allied minion attacks, give that minion +1/+0/+0 until the end of the turn.";
                    Effect effect = new Effect("+1/+0/+0 until the end of the turn (from <b>Ephemera, Angelic Slacker</b>).",
                            EffectStats.builder()
                                    .change(Stat.ATTACK, 1)
                                    .build(),
                            e -> e.untilTurnEndTeam = 0);
                    return new ResolverWithDescription(description, new AddEffectResolver(((EventMinionAttack) event).m1, effect));
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
