package server.card.cardset.moba.shadowshaman;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
import client.ui.game.visualboardanimation.eventanimation.destroy.EventAnimationDestroyDarkElectro;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.event.EventDestroy;
import server.resolver.BlastResolver;
import server.resolver.DestroyResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class BanelingBust extends SpellText {
    public static final String NAME = "Baneling Bust";
    public static final String DESCRIPTION = "Destroy all allied minions. <b>Blast(X)</b> X times. X equals the number of minions destroyed.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.GOLD;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/moba/banelingbust.png"),
            CRAFT, TRAITS, RARITY, 7, BanelingBust.class,
            () -> List.of(Tooltip.BLAST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {

            @Override
            public boolean battlecryPlayConditions() {
                return this.owner.board.getMinions(owner.team, false, true).findFirst().isPresent();
            }

            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<Minion> relevant = b.getMinions(owner.team, false, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DestroyResolver(relevant, EventDestroy.Cause.EFFECT, new EventAnimationDestroyDarkElectro()));
                        for (int i = 0; i < relevant.size(); i++) {
                            this.resolve(b, rq, el, new BlastResolver(effect, relevant.size(), new EventAnimationDamageMagicHit()));
                        }
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                int x = (int) this.owner.board.getMinions(this.owner.team, false, true).count();
                return AI.VALUE_PER_DAMAGE * x * x / 2;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
