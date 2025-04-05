package server.card.cardset.basic.portalhunter;

import client.tooltip.TooltipSpell;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageAOESlice;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.target.TargetList;
import server.event.Event;
import server.resolver.CreateCardResolver;
import server.resolver.DamageResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PuppeteersStrings extends SpellText {
    public static final String NAME = "Puppeteer's Strings";
    public static final String DESCRIPTION = "Put 3 <b>Puppets</b> in your hand. Deal 1 damage to all enemy minions.";
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/basic/puppeteersstrings.png"),
            CRAFT, TRAITS, RARITY, 3, PuppeteersStrings.class,
            () -> List.of(Puppet.TOOLTIP),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances;
            @Override
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                Effect effect = this;
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> cards = Collections.nCopies(3, new Puppet());
                        List<Integer> pos = Collections.nCopies(3, -1);
                        this.resolve(b, rq, el, new CreateCardResolver(cards, owner.team, CardStatus.HAND, pos));
                        List<Minion> targets = b.getMinions(owner.team * -1, false, true).collect(Collectors.toList());
                        this.resolve(b, rq, el, new DamageResolver(effect, targets, 1, true, new EventAnimationDamageAOESlice(owner.team * -1, false).toString()));
                    }
                });
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = Collections.nCopies(3, new Puppet().constructInstance(this.owner.board));
                }
                return AI.valueForAddingToHand(this.cachedInstances, refs) + AI.valueOfMinionDamage(1) * 3;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
