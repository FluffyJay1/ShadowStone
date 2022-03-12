package gamemode.dungeonrun.model;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import server.UnleashPowerText;
import server.card.CardText;
import server.card.LeaderText;
import server.card.cardset.CardSet;
import server.card.cardset.ExpansionSet;

import java.util.ArrayList;
import java.util.List;

// an enemy as parsed by xml
public class EnemySpec {
    public LeaderText leaderText;
    public UnleashPowerText unleashPowerText;
    public CardSet sets;
    public List<CardText> cards;
    public EnemySpec(Element element) {
        String leader = element.getAttribute("leader");
        this.leaderText = LeaderText.fromString(leader);
        String unleash = element.getAttribute("unleash");
        this.unleashPowerText = UnleashPowerText.fromString(unleash);
        this.sets = new CardSet();
        NodeList expansionSetNodeList = element.getElementsByTagName("cardset");
        for (int i = 0; i < expansionSetNodeList.getLength(); i++) {
            Node expansionSetNode = expansionSetNodeList.item(i);
            String setName = expansionSetNode.getTextContent();
            ExpansionSet set = ExpansionSet.fromString(setName);
            if (set != null) {
                this.sets.add(set.getCards());
            }
        }
        NodeList cardsNodeList = element.getElementsByTagName("card");
        this.cards = new ArrayList<>(cardsNodeList.getLength());
        for (int i = 0; i < cardsNodeList.getLength(); i++) {
            Node cardNode = cardsNodeList.item(i);
            String cardName = cardNode.getTextContent();
            this.cards.add(CardText.fromString(cardName));
        }
    }
}
