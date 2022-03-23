package gamemode.dungeonrun.controller;

import gamemode.dungeonrun.model.Contestant;
import network.DataStream;
import server.GameController;
import server.ServerBoard;
import server.ai.AI;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class DungeonRunGameRunner implements Runnable {
    final DataStream dslocal;
    final int localteam;
    Contestant player, enemy;
    public DungeonRunGameRunner(DataStream dslocal, int localteam, Contestant player, Contestant enemy) {
        this.dslocal = dslocal;
        this.localteam = localteam;
        this.player = player;
        this.enemy = enemy;
    }

    @Override
    public void run() {
        DataStream dsai = new DataStream();
        DataStream dsexternal = new DataStream();
        DataStream.pair(dsai, dsexternal);
        AI ai = new AI(dsai, this.localteam * -1, 0);
        ai.start();
        GameController gc = new GameController(List.of(this.dslocal, dsexternal),
                List.of(this.player.leaderText, this.enemy.leaderText),
                List.of(this.player.unleashPowerText, this.enemy.unleashPowerText),
                List.of(this.player.deck, this.enemy.deck));
        gc.startInit();
        // adjust the healths based on level
        gc.resolve(new Resolver(false) {
            @Override
            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                b.getPlayer(localteam).getLeader().ifPresent(l -> {
                    this.resolve(b, rq, el, new AddEffectResolver(l, new Effect("", new EffectStats(
                            new EffectStats.Setter(EffectStats.HEALTH, true, player.getBonusHealth())
                    ))));
                });
                b.getPlayer(localteam * -1).getLeader().ifPresent(l -> {
                    this.resolve(b, rq, el, new AddEffectResolver(l, new Effect("", new EffectStats(
                            new EffectStats.Setter(EffectStats.HEALTH, true, enemy.getBonusHealth())
                    ))));
                });
            }
        }, 0);
        gc.startGame();
        while (gc.isGamePhase() && !Thread.currentThread().isInterrupted()) {
            gc.updateGame();
        }
        gc.end();
        dsexternal.close();
        this.dslocal.close();
    }
}
