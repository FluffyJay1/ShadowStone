package gamemode.dungeonrun.controller;

import gamemode.dungeonrun.model.Contestant;
import network.DataStream;
import server.GameController;
import server.ServerBoard;
import server.ai.AI;
import server.ai.AIConfig;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.util.ResolverQueue;

import java.io.IOException;
import java.util.List;

import static java.lang.Thread.sleep;

public class DungeonRunGameRunner implements Runnable {
    final DataStream dslocal;
    Contestant player, enemy;
    public DungeonRunGameRunner(DataStream dslocal, Contestant player, Contestant enemy) {
        this.dslocal = dslocal;
        this.player = player;
        this.enemy = enemy;
    }

    @Override
    public void run() {
        DataStream dsai = new DataStream();
        DataStream dsexternal = new DataStream();
        DataStream.pair(dsai, dsexternal);
        AI ai = new AI(dsai, AIConfig.PRO);
        ai.start();
        try {
            GameController gc = new GameController(List.of(this.dslocal, dsexternal),
                    List.of(this.player.leaderText, this.enemy.leaderText),
                    List.of(this.player.unleashPowerText, this.enemy.unleashPowerText),
                    List.of(this.player.deck, this.enemy.deck));
            gc.startInit();
            // adjust the healths based on level
            gc.resolve(new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    b.getPlayer(gc.indexToTeam(0)).getLeader().ifPresent(l -> {
                        this.resolve(b, rq, el, new AddEffectResolver(l, new Effect("", EffectStats.builder()
                                .set(Stat.HEALTH, player.getHealth())
                                .build()
                        )));
                    });
                    b.getPlayer(gc.indexToTeam(1)).getLeader().ifPresent(l -> {
                        this.resolve(b, rq, el, new AddEffectResolver(l, new Effect("", EffectStats.builder()
                                .set(Stat.HEALTH, enemy.getHealth())
                                .build()
                        )));
                    });
                }
            }, 0);
            gc.startGame();
            while (gc.isGamePhase() && !Thread.currentThread().isInterrupted()) {
                gc.updateGame();
            }
            gc.end();
            sleep(10000); // give 10 seconds for animations to play out or whatever and for the ai to emote
        } catch (IOException | InterruptedException e) {
            // lol
        } finally {
            dsexternal.close();
            this.dslocal.close();
        }
    }
}
