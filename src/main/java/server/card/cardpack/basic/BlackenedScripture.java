package server.card.cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.Board;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.BanishResolver;
import server.resolver.Resolver;

import java.util.List;

public class BlackenedScripture extends Spell {
    public static final String NAME = "Blackened Scripture";
    public static final String DESCRIPTION = "<b>Banish</b> an enemy minion with 3 health or less.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/basic/blackenedscripture.png",
            CRAFT, RARITY, 2, BlackenedScripture.class,
            () -> List.of(Tooltip.BANISH));
    final Effect e;

    public BlackenedScripture(Board b) {
        super(b, TOOLTIP);
        this.e = new Effect(DESCRIPTION) {
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(new CardTargetingScheme(this, 1, 1, DESCRIPTION) {
                    @Override
                    public boolean canTarget(Card c) {
                        return c.status.equals(CardStatus.BOARD) && c instanceof Minion
                                && c.team != this.getCreator().owner.team && ((Minion) c).health <= 3;
                    }
                });
            }

            @Override
            public Resolver battlecry() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
                        getStillTargetableBattlecryCardTargets(0).findFirst().ifPresent(c -> {
                            this.resolve(b, rl, el, new BanishResolver((c)));
                        });
                    }
                };
            }

            @Override
            public double getBattlecryValue(int refs) {
                return 3;
            }
        };
        this.addEffect(true, e);
    }
}
