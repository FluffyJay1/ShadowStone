package server.card.cardset.moba.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageBigExplosion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.Stat;
import server.card.target.*;
import server.event.Event;
import server.resolver.BlastResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class SiegeTank extends MinionText {
    public static final String NAME = "Siege Tank";
    public static final String DESCRIPTION = "<b>Battlecry</b> and <b>Unleash</b>: <b>Choose</b> to <b>Blast(M + 1)</b> twice or M + 3 damage to a minion and M to its neighbors.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/siegetank.png"),
            CRAFT, TRAITS, RARITY, 9, 4, 2, 4, false, SiegeTank.class,
            new Vector2f(), -1, new EventAnimationDamageShoot(),
            () -> List.of(Tooltip.UNLEASH, Tooltip.CHOOSE, Tooltip.BLAST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<TargetingScheme<?>> sharedTargetingSchemes() {
                return List.of(
                        new ModalTargetingScheme(this, 1, "<b>Choose</b> 1", List.of(
                                new ModalOption("<b>Blast(M + 1)</b> twice."),
                                new ModalOption("Deal M + 3 damage to a minion and M to its neighbors.",
                                        e -> e.owner.board.getMinions(e.owner.team * -1, false, true).findAny().isPresent())
                        )),
                        new CardTargetingScheme(this, 0, 1, "Deal M + 3 damage to a minion and M to its neighbors.") {
                            @Override
                            protected boolean criteria(Card c) {
                                return c instanceof Minion && c.team != this.getCreator().owner.team && c.status.equals(CardStatus.BOARD);
                            }

                            @Override
                            public boolean isApplicable(List<TargetList<?>> alreadyTargeted) {
                                // only applicable if we selected the second option in the modal
                                return ((ModalTargetList)alreadyTargeted.get(0)).targeted.get(0).equals(1);
                            }
                        }
                );
            }
            
            private ResolverWithDescription sharedResolver(List<TargetList<?>> targetList, Function<Effect, List<TargetingScheme<?>>> schemesFrom) {
                Effect effect = this; // anonymous fuckery
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int option = ((ModalTargetList) targetList.get(0)).targeted.get(0);
                        int magic = owner.finalStats.get(Stat.MAGIC);
                        switch (option) {
                            case 0 -> {
                                for (int i = 0; i < 2; i++) {
                                    this.resolve(b, rq, el, new BlastResolver(effect, magic + 1, new EventAnimationDamageShoot()));
                                }
                            }
                            case 1 -> {
                                getStillTargetableCards(schemesFrom, targetList, 1).findFirst().ifPresent(targeted -> {
                                    List<Minion> m = new LinkedList<>();
                                    List<Integer> d = new LinkedList<>();
                                    int pos = targeted.getIndex();
                                    m.add((Minion) targeted);
                                    d.add(magic + 3);
                                    for (int i = -1; i <= 1; i += 2) {
                                        int offsetPos = pos + i;
                                        BoardObject adjacent = b.getPlayer(owner.team * -1).getPlayArea().get(offsetPos);
                                        if (adjacent instanceof Minion) {
                                            m.add((Minion) adjacent);
                                            d.add(magic);
                                        }
                                    }
                                    this.resolve(b, rq, el, new DamageResolver(effect, m, d, true, new EventAnimationDamageBigExplosion()));
                                });
                            }
                        }
                    }
                });
            }

            private double sharedResolverValue() {
                return this.owner.finalStats.get(Stat.MAGIC) * 2 * AI.VALUE_PER_DAMAGE;
            }

            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return sharedTargetingSchemes();
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return sharedResolver(targetList, Effect::getUnleashTargetingSchemes);
            }

            @Override
            public double getBattlecryValue(int refs) {
                return sharedResolverValue();
            }

            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return sharedTargetingSchemes();
            }

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                return sharedResolver(targetList, Effect::getUnleashTargetingSchemes);
            }

            @Override
            public double getPresenceValue(int refs) {
                return sharedResolverValue() / 2;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
