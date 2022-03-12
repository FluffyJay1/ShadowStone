package gamemode.dungeonrun.model;

import server.UnleashPowerText;
import server.card.CardText;
import server.card.LeaderText;
import server.card.cardset.ConstructedDeck;

import java.io.Serializable;
import java.util.List;

public class Contestant implements Serializable {
    // handled via xml parsing
    public LeaderText leaderText;
    public UnleashPowerText unleashPowerText;

    // auto generated, with the help of xml
    public ConstructedDeck deck;
    public List<CardText> specialCards;

    // generated
    public int level; // decides deck quality and hp

    public Contestant(LeaderText leaderText, UnleashPowerText unleashPowerText, ConstructedDeck deck, int level, List<CardText> specialCards) {
        this.leaderText = leaderText;
        this.unleashPowerText = unleashPowerText;
        this.deck = deck;
        this.level = level;
        this.specialCards = specialCards;
    }

    public int getBonusHealth() {
        return 5 * this.level;
    }
}
