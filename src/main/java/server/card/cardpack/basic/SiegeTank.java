package server.card.cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageFire;
import org.newdawn.slick.geom.Vector2f;
import server.Board;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.*;
import server.event.Event;
import server.resolver.BlastResolver;
import server.resolver.EffectDamageResolver;
import server.resolver.Resolver;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class SiegeTank extends Minion {
    public static final String NAME = "Siege Tank";
    public static final String DESCRIPTION = "<b>Unleash</b>: <b>Choose</b> to <b>Blast(5)</b> or deal 3 damage to a minion and 2 to its neighbors.";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/siegetank.png",
            CRAFT, RARITY, 5, 2, 2, 5, false, SiegeTank.class,
            new Vector2f(), -1, null,
            () -> List.of(Tooltip.UNLEASH, Tooltip.CHOOSE, Tooltip.BLAST));

    public SiegeTank(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
                Function<Effect, Boolean> hasMinion = e -> e.owner.board.getMinions(e.owner.team * -1, false, true).findAny().isPresent();
                return List.of(
                        new ModalTargetingScheme(this, 1, "<b>Choose 1</b>", List.of(
                                new ModalOption("<b>Blast(5)</b>"),
                                new ModalOption("Deal 3 damage to a minion and 2 to its neighbors", hasMinion)
                        )),
                        new CardTargetingScheme(this, 0, 1, "Deal 3 damage to a minion and 2 to its neighbors") {
                            @Override
                            public boolean canTarget(Card c) {
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
            public Resolver unleash() {
                Effect effect = this; // anonymous fuckery
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        int option = ((ModalTargetList) getUnleashTargets().get(0)).targeted.get(0);
                        switch (option) {
                            case 0 -> {
                                this.resolve(b, rl, el, new BlastResolver(effect, 5, null));
                            }
                            case 1 -> {
                                getStillTargetableUnleashCardTargets(1).findFirst().ifPresent(targeted -> {
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
                                    this.resolve(b, rl, el, new EffectDamageResolver(effect, m, d, true, null));
                                });
                            }
                        }
                    }
                };
            }

            @Override
            public double getPresenceValue(int refs) {
                return 6 / 2.;
            }
        };
        this.addEffect(true, e);
    }
}
