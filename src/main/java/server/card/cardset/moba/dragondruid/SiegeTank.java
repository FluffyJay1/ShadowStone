package server.card.cardset.moba.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageBigExplosion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageShoot;
import org.newdawn.slick.geom.Vector2f;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.*;
import server.event.Event;
import server.resolver.BlastResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.LinkedList;
import java.util.List;

public class SiegeTank extends MinionText {
    public static final String NAME = "Siege Tank";
    public static final String DESCRIPTION = "<b>Unleash</b>: <b>Choose</b> to <b>Blast(5)</b> or deal 3 damage to a minion and 2 to its neighbors.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/moba/siegetank.png"),
            CRAFT, TRAITS, RARITY, 5, 2, 2, 5, false, SiegeTank.class,
            new Vector2f(), -1, new EventAnimationDamageShoot(),
            () -> List.of(Tooltip.UNLEASH, Tooltip.CHOOSE, Tooltip.BLAST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                return List.of(
                        new ModalTargetingScheme(this, 1, "<b>Choose</b> 1", List.of(
                                new ModalOption("<b>Blast(5)</b>."),
                                new ModalOption("Deal 3 damage to a minion and 2 to its neighbors.",
                                        e -> e.owner.board.getMinions(e.owner.team * -1, false, true).findAny().isPresent())
                        )),
                        new CardTargetingScheme(this, 0, 1, "Deal 3 damage to a minion and 2 to its neighbors.") {
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

            @Override
            public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
                Effect effect = this; // anonymous fuckery
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        int option = ((ModalTargetList) targetList.get(0)).targeted.get(0);
                        switch (option) {
                            case 0 -> {
                                this.resolve(b, rq, el, new BlastResolver(effect, 5, new EventAnimationDamageShoot().toString()));
                            }
                            case 1 -> {
                                getStillTargetableCards(Effect::getUnleashTargetingSchemes, targetList, 1).findFirst().ifPresent(targeted -> {
                                    List<Minion> m = new LinkedList<>();
                                    List<Integer> d = new LinkedList<>();
                                    int pos = targeted.getIndex();
                                    m.add((Minion) targeted);
                                    d.add(3);
                                    for (int i = -1; i <= 1; i += 2) {
                                        int offsetPos = pos + i;
                                        BoardObject adjacent = b.getPlayer(owner.team * -1).getPlayArea().get(offsetPos);
                                        if (adjacent instanceof Minion) {
                                            m.add((Minion) adjacent);
                                            d.add(2);
                                        }
                                    }
                                    this.resolve(b, rq, el, new DamageResolver(effect, m, d, true, new EventAnimationDamageBigExplosion().toString()));
                                });
                            }
                        }
                    }
                });
            }

            @Override
            public double getPresenceValue(int refs) {
                return 6 / 2.;
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
