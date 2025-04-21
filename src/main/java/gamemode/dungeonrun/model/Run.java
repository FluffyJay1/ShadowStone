package gamemode.dungeonrun.model;

import server.card.CardText;

import java.io.Serializable;
import java.util.List;

import gamemode.dungeonrun.Passive;

public class Run implements Serializable {
    public List<Contestant> enemies;
    public int current;
    public Contestant player;
    public RunState state;
    // outer list: list of selection rounds
    // middle list: list of options for a given round
    // inner list: list of cards contained in an options
    public List<List<List<Passive>>> passiveOptions;
    public List<List<List<CardText>>> treasureOptions;
    public List<List<List<CardText>>> lootOptions;
    public List<List<List<CardText>>> discardOptions;
}
