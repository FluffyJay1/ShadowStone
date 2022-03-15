package server.card.cardset.special.batter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import server.Player;
import server.ServerBoard;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.target.ModalOption;
import server.card.target.ModalTargetList;
import server.card.target.ModalTargetingScheme;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.util.ResolverQueue;

import java.util.Collections;
import java.util.List;

public class IHadThreeFriends extends SpellText {
    public static final String NAME = "I Had Three Friends.";
    public static final String DESCRIPTION = "Summon <b>Add-on: Alpha</b>, <b>Add-on: Omega</b>, and <b>Add-on: Epsilon</b>. <b>Choose</b> one to give <b>Rush</b>.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, "res/card/special/threefriends.png",
            CRAFT, RARITY, 8, IHadThreeFriends.class,
            () -> List.of(Alpha.TOOLTIP, Omega.TOOLTIP, Epsilon.TOOLTIP, Tooltip.RUSH));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(
                        new ModalTargetingScheme(this, 1, "<b>Choose 1</b>", List.of(
                                new ModalOption("Give <b>Rush</b> to <b>Add-on: Alpha</b>.", e -> {
                                    Player p = e.owner.board.getPlayer(e.owner.team);
                                    return p.getPlayArea().size() < p.maxPlayAreaSize;
                                }),
                                new ModalOption("Give <b>Rush</b> to <b>Add-on: Omega</b>.", e -> {
                                    Player p = e.owner.board.getPlayer(e.owner.team);
                                    return p.getPlayArea().size() + 1 < p.maxPlayAreaSize;
                                }),
                                new ModalOption("Give <b>Rush</b> to <b>Add-on: Epsilon</b>.", e -> {
                                    Player p = e.owner.board.getPlayer(e.owner.team);
                                    return p.getPlayArea().size() + 2 < p.maxPlayAreaSize;
                                })
                        ))
                );
            }

            @Override
            public Resolver battlecry() {
                return new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> toCreate = List.of(new Alpha(), new Omega(), new Epsilon());
                        List<Integer> pos = Collections.nCopies(3, -1);
                        CreateCardResolver ccr = new CreateCardResolver(toCreate, owner.team, CardStatus.BOARD, pos);
                        this.resolve(b, rq, el, ccr);
                        int option = ((ModalTargetList) getBattlecryTargets().get(0)).targeted.get(0);
                        if (ccr.event.successful.get(option)) {
                            // give it rush
                            Effect rush = new Effect("", new EffectStats(new EffectStats.Setter(EffectStats.RUSH, false, 1)));
                            this.resolve(b, rq, el, new AddEffectResolver(ccr.event.cards.get(option), rush));
                        }
                    }
                };
            }

            @Override
            public double getBattlecryValue(int refs) {
                if (this.cachedInstances == null) {
                    this.cachedInstances = List.of(
                            new Alpha().constructInstance(this.owner.board),
                            new Omega().constructInstance(this.owner.board),
                            new Epsilon().constructInstance(this.owner.board)
                    );
                }
                // behold magic numbers
                double sum = 0;
                double multiplier = 0.9;
                for (Card c : this.cachedInstances) {
                    sum += c.getValue(refs - 1) * multiplier * 0.8;
                    multiplier *= multiplier; // each card has lower and lower chance of being able to fit
                }
                return sum;
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
