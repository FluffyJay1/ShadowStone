package server.card.cardset.indie.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOff;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.cardset.basic.portalhunter.Puppet;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.TransformResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class Judge extends MinionText {
    public static final String NAME = "The Judge";
    private static final String BATTLECRY_DESCRIPTION = "<b>Battlecry</b>: <b>Transform</b> all other cards into <b>Puppets</b>.";
    private static final String UNLEASH_DESCRIPTION = "<b>Unleash</b>: Give your minions <b>Storm</b>.";
    private static final String ONLISTENEVENT_DESCRIPTION = "Whenever another non-<b>Puppet</b> comes into play, <b>Transform</b> it into a <b>Puppet</b>.";
    public static final String DESCRIPTION = BATTLECRY_DESCRIPTION + "\n" + UNLEASH_DESCRIPTION + "\n" + ONLISTENEVENT_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/indie/judge.png",
            CRAFT, TRAITS, RARITY, 8, 2, 3, 2, false, Judge.class,
            new Vector2f(150, 175), 1.1, new EventAnimationDamageOff(),
            () -> List.of(Tooltip.BATTLECRY, Tooltip.TRANSFORM, Puppet.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(BATTLECRY_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<BoardObject> targets = b.getBoardObjects(0, false, true, true, true)
                                .filter(m -> owner != m)
                                .collect(Collectors.toList());
                        this.resolve(b, rq, el, new TransformResolver(targets, new Puppet()));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 5;
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(UNLEASH_DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        Effect buff = new Effect("<b>Storm</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .set(Stat.STORM, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
                    }
                });
            }

            @Override
            public ResolverWithDescription onListenEventWhileInPlay(Event event) {
                if (event != null) {
                    List<BoardObject> relevant = event.cardsEnteringPlay().stream()
                            .filter(bo -> !(bo.getCardText() instanceof Puppet) && this.owner != bo)
                            .collect(Collectors.toList());
                    if (!relevant.isEmpty()) {
                        return new ResolverWithDescription(ONLISTENEVENT_DESCRIPTION, new TransformResolver(relevant, new Puppet()));
                    }
                }
                return null;
            }

            @Override
            public double getPresenceValue(int refs) {
                return 4; // idk it's pretty good
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
