package server;

import server.card.BoardObject;
import server.card.CardText;

public abstract class BoardObjectText extends CardText {
    @Override
    public abstract BoardObject constructInstance(Board b);
}
