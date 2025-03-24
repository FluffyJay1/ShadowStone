package server.card.cardset.moba.neutral;

import java.util.*;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;

import org.newdawn.slick.geom.*;

import server.ServerBoard;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class Timbersaw extends MinionText {
    public static final String NAME = "Timbersaw";
    public static final String DESCRIPTION = "<b>Rush</b>. <b>Clash</b>: Gain 1 <b>Armor</b> until the end of the opponent's turn.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/moba/timbersaw.png",
            CRAFT, TRAITS, RARITY, 4, 1, 3, 4, true, Timbersaw.class,
            new Vector2f(), -1, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.RUSH, Tooltip.CLASH, Tooltip.ARMOR),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION, EffectStats.builder()
                .set(Stat.RUSH, 1)
                .build()) {
            @Override
            public ResolverWithDescription clash(Minion target) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect ef = new Effect("+1 <b>Armor</b> (from <b>Clash</b>).", EffectStats.builder()
                                .change(Stat.ARMOR, 1)
                                .build(),
                                e -> e.untilTurnEndTeam = -1);
                        this.resolve(b, rq, el, new AddEffectResolver(owner, ef));
                    }
                });
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
