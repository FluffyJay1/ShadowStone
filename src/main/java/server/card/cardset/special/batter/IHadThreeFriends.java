package server.card.cardset.special.batter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipSpell;
import client.ui.Animation;
import server.Player;
import server.ServerBoard;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.target.*;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.CreateCardResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IHadThreeFriends extends SpellText {
    public static final String NAME = "I Had Three Friends.";
    public static final String DESCRIPTION = "Summon <b>Add-on: Alpha</b>, <b>Add-on: Omega</b>, and <b>Add-on: Epsilon</b>. <b>Choose</b> one to give <b>Rush</b>.";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipSpell TOOLTIP = new TooltipSpell(NAME, DESCRIPTION, () -> new Animation("card/special/threefriends.png"),
            CRAFT, TRAITS, RARITY, 8, IHadThreeFriends.class,
            () -> List.of(Alpha.TOOLTIP, Omega.TOOLTIP, Epsilon.TOOLTIP, Tooltip.RUSH),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect(DESCRIPTION) {
            private List<Card> cachedInstances; // for getBattlecryValue, preview the value of the created cards
            @Override
            public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
                return List.of(
                        new ModalTargetingScheme(this, 1, "<b>Choose</b> 1", List.of(
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
            public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
                return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        List<CardText> toCreate = List.of(new Alpha(), new Omega(), new Epsilon());
                        List<Integer> pos = Collections.nCopies(3, -1);
                        Effect rush = new Effect("", EffectStats.builder()
                                .set(Stat.RUSH, 1)
                                .build());
                        int option = ((ModalTargetList) targetList.get(0)).targeted.get(0);
                        List<List<Effect>> buffs = new ArrayList<>();
                        for (int i = 0; i < option; i++) {
                            buffs.add(List.of());
                        }
                        buffs.add(List.of(rush));
                        for (int i = option + 1; i < 3; i++) {
                            buffs.add(List.of());
                        }
                        this.resolve(b, rq, el, CreateCardResolver.builder()
                            .withCards(toCreate)
                            .withTeam(owner.team)
                            .withStatus(CardStatus.BOARD)
                            .withPos(pos)
                            .withAdditionalEffects(buffs)
                            .build());
                    }
                });
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
                return AI.valueForSummoning(this.cachedInstances, refs);
            }
        });
    }

    @Override
    public TooltipSpell getTooltip() {
        return TOOLTIP;
    }
}
