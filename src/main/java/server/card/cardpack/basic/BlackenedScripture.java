package server.card.cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.Board;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.event.Event;
import server.resolver.BanishResolver;
import server.resolver.Resolver;

import java.util.List;

public class BlackenedScripture extends Spell {
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final TooltipSpell TOOLTIP = new TooltipSpell("Blackened Scripture",
            "<b>Banish</b> an enemy minion with 3 health or less.",
            "res/card/basic/blackenedscripture.png", CRAFT, 2, BlackenedScripture.class,
            Tooltip.BANISH);
    final Effect e;

    public BlackenedScripture(Board b) {
        super(b, TOOLTIP);
        this.e = new Effect(TOOLTIP.description) {
            @Override
            public Resolver battlecry() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        Target banishTarget = battlecryTargets.get(0);
                        if (banishTarget.getTargetedCards().size() > 0) {
                            Card targeted = banishTarget.getTargetedCards().get(0);
                            this.resolve(b, rl, el, new BanishResolver(targeted));
                        }
                    }
                };
            }

            @Override
            public double getBattlecryValue() {
                return 3;
            }
        };
        Target t = new Target(e, 1, "Banish a card with 3 health or less.") {
            @Override
            public boolean canTarget(Card c) {
                return c.status.equals(CardStatus.BOARD) && c instanceof Minion
                        && c.team != this.getCreator().owner.team && ((Minion) c).health <= 3;
            }
        };
        e.setBattlecryTargets(List.of(t));
        this.addEffect(true, e);
    }

    @Override
    public boolean conditions() {
        return this.board.getTargetableCards(this.e.battlecryTargets.get(0)).findAny().isPresent();
    }
}
