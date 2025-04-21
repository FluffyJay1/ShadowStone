package gamemode.dungeonrun.passive;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import client.tooltip.Tooltip;
import gamemode.dungeonrun.Passive;
import server.Board;
import server.ServerBoard;
import server.card.CardText;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.event.EventPlayCard;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.UpdateEffectStateResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class OnPlaySameCardBuff extends Passive {
    public static final String DESCRIPTION = "When you play a minion, give it +X/+X/+0. X equals the amount of other times you've played a minion of the same name.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("One trick", DESCRIPTION, List::of);
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectOnPlaySameCardBuff());
    }

    public static class EffectOnPlaySameCardBuff extends Effect {
        public SortedMap<CardText, Integer> playTimes; // needs to be sorted to keep the extraStateString stable

        // required for reflection
        public EffectOnPlaySameCardBuff() {
            super(DESCRIPTION);
            this.playTimes = new TreeMap<>();
        }

        @Override
        public ResolverWithDescription onListenEventWhileInPlay(Event event) {
            if (event instanceof EventPlayCard && this.owner.board.getCurrentPlayerTurn() == this.owner.team) {
                EventPlayCard epc = (EventPlayCard) event;
                if (epc.p.team == this.owner.team && epc.c instanceof Minion) {
                    return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                        @Override
                        public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                            int playTimes = EffectOnPlaySameCardBuff.this.playTimes.getOrDefault(epc.c.getCardText(), 0);
                            if (playTimes > 0) {
                                Effect buff = new Effect("+" + playTimes + "/+" + playTimes + "/+0 (from passive).", EffectStats.builder()
                                        .change(Stat.ATTACK, playTimes)
                                        .change(Stat.MAGIC, playTimes)
                                        .build());
                                this.resolve(b, rq, el, new AddEffectResolver(epc.c, buff));
                            }
                            this.resolve(b, rq, el, new UpdateEffectStateResolver(EffectOnPlaySameCardBuff.this, () -> EffectOnPlaySameCardBuff.this.playTimes.put(epc.c.getCardText(), playTimes + 1)));
                        }
                    });
                }
            }
            return null;
        }

        @Override
        public double getPresenceValue(int refs) {
            return 2;
        }

        @Override
        public String extraStateString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.playTimes.size()).append(" ");
            for (Entry<CardText, Integer> entry : this.playTimes.entrySet()) {
                sb.append(entry.getKey().toString()).append(entry.getValue()).append(" ");
            }
            return sb.toString();
        }

        @Override
        public void loadExtraState(Board b, StringTokenizer st) {
            this.playTimes.clear();
            int count = Integer.parseInt(st.nextToken());
            for (int i = 0; i < count; i++) {
                CardText cardText = CardText.fromString(st.nextToken());
                int times = Integer.parseInt(st.nextToken());
                this.playTimes.put(cardText, times);
            }
        }

        @Override
        public EffectOnPlaySameCardBuff clone() throws CloneNotSupportedException {
            EffectOnPlaySameCardBuff e = (EffectOnPlaySameCardBuff) super.clone();
            e.playTimes = new TreeMap<>(this.playTimes);
            return e;
        }
    }
}
