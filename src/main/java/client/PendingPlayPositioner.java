package client;

import server.card.BoardObject;
import utils.PendingListManager;

public interface PendingPlayPositioner {
    PendingListManager.Processor<BoardObject> getPendingPlayPositionProcessor();
}
