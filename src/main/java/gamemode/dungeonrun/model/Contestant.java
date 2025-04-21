package gamemode.dungeonrun.model;

import server.UnleashPowerText;
import server.card.CardText;
import server.card.LeaderText;
import server.card.cardset.ConstructedDeck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gamemode.dungeonrun.Passive;

public class Contestant implements Serializable {
    // handled via xml parsing
    public LeaderText leaderText;
    public UnleashPowerText unleashPowerText;

    // auto generated, with the help of xml
    public ConstructedDeck deck;
    public List<CardText> specialCards;

    // generated
    public int level; // decides deck quality and hp
    public List<Passive> passives = new ArrayList<>();

    public Contestant(LeaderText leaderText, UnleashPowerText unleashPowerText, ConstructedDeck deck, int level, List<CardText> specialCards, List<Passive> initialPassives) {
        this.leaderText = leaderText;
        this.unleashPowerText = unleashPowerText;
        this.deck = deck;
        this.level = level;
        this.specialCards = specialCards;
        this.passives = new ArrayList<>(initialPassives);
    }

    public int getHealth() {
        return 5 * this.level + 10;
    }
}
