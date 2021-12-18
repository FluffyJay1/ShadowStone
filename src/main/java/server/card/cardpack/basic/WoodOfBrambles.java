package server.card.cardpack.basic;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class WoodOfBrambles extends Amulet {
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet("Wood of Brambles",
            "<b> Countdown(2). </b> \n <b> Battlecry: </b> add two <b> Faries </b> to your hand. Give all allied minions the following effect until this amulet leaves play: <b> Clash: </b> deal 1 damage to the enemy minion. \n Whenever an allied minion comes into play, give them that effect until this amulet leaves play.",
            "res/card/basic/woodofbrambles.png", CRAFT, 2, WoodOfBrambles.class, Tooltip.COUNTDOWN, Tooltip.BATTLECRY,
            Fairy.TOOLTIP, Tooltip.CLASH);

    public WoodOfBrambles(Board b) {
        super(b, TOOLTIP);
        Effect e = new Effect(TOOLTIP.description) {
            List<Effect> distributedEffects = new LinkedList<>();

            @Override
            public Resolver battlecry() {
                Effect effect = this;
                List<Effect> thisDistributedEffects = this.distributedEffects; // anonymous fuckery
                return new Resolver(false) {

                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        List<Card> cards = List.of(new Fairy(effect.owner.board), new Fairy(effect.owner.board));
                        List<Integer> pos = List.of(-1, -1);
                        this.resolve(b, rl, el, new CreateCardResolver(cards, effect.owner.team, CardStatus.HAND, pos));
                        EffectBrambles e = new EffectBrambles();
                        AddEffectResolver aer = new AddEffectResolver(
                                effect.owner.board.getBoardObjects(effect.owner.team, false, true, false), e);
                        this.resolve(b, rl, el, aer);
                        this.resolve(b, rl, el, new UpdateEffectStateResolver(effect, () -> thisDistributedEffects.addAll(aer.effects)));
                    }

                };
            }

            @Override
            public Resolver onListenEvent(Event event) {
                Effect effect = this;
                return new Resolver(false) {

                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        List<BoardObject> cardsEnteringPlay = event.cardsEnteringPlay();
                        if (cardsEnteringPlay != null && effect.owner.status.equals(CardStatus.BOARD)) {
                            List<BoardObject> relevant = new LinkedList<>();
                            for (BoardObject c : cardsEnteringPlay) {
                                if (c instanceof Minion && c.team == effect.owner.team) {
                                    relevant.add(c);
                                }
                            }
                            if (!relevant.isEmpty()) {
                                AddEffectResolver aer = new AddEffectResolver(relevant, new EffectBrambles());
                                this.resolve(b, rl, el, aer);
                                this.resolve(b, rl, el, new UpdateEffectStateResolver(effect, () -> distributedEffects.addAll(aer.effects)));
                            }
                        }
                    }

                };
            }

            @Override
            public Resolver onLeavePlay() {
                return new RemoveEffectResolver(this.distributedEffects);
            }

            @Override
            public String extraStateString() {
                StringBuilder builder = new StringBuilder();
                builder.append(this.distributedEffects.size()).append(" ");
                for (Effect e : this.distributedEffects) {
                    builder.append(e.toReference());
                }
                return builder.toString();
            }

            @Override
            public void loadExtraState(Board b, StringTokenizer st) {
                int numEffects = Integer.parseInt(st.nextToken());
                this.distributedEffects = new ArrayList<>(numEffects);
                for (int i = 0; i < numEffects; i++) {
                    this.distributedEffects.add(Effect.fromReference(b, st));
                }
            }
        };
        e.set.setStat(EffectStats.COUNTDOWN, 2);
        this.addEffect(true, e);
    }

    public static class EffectBrambles extends Effect {

        public EffectBrambles(String description) {
            super(description);
        }

        public EffectBrambles() {
            this("Has <b> Clash: </b> deal 1 damage to the enemy minion until the corresponding Wood of Brambles leaves play.");
        }

        @Override
        public Resolver clash(Minion target) {
            return new EffectDamageResolver(this, target, 1, true, null);
        }

    }
}
