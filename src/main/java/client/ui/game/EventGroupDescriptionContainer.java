package client.ui.game;

import client.Config;
import client.ui.UI;
import client.ui.UIBox;
import org.newdawn.slick.geom.Vector2f;
import server.event.eventgroup.EventGroup;

import java.util.*;

public class EventGroupDescriptionContainer extends UIBox {
    private static final double MAX_TIME_PER_PANEL = 7; // max time before it slides off to the left
    private static final double REMOVE_TIME = 8; // time before it gets deleted
    private static final float PANEL_SPACING = 10;
    List<EventGroupDescriptionPanel> activePanels, removingPanels;
    Map<EventGroup, EventGroupDescriptionPanel> groupPanelMap;

    public EventGroupDescriptionContainer(UI ui, Vector2f pos) {
        super(ui, pos, new Vector2f(450, Config.WINDOW_HEIGHT));
        this.margins.set(30, 30);
        this.ignorehitbox = true;
        this.activePanels = new ArrayList<>();
        this.removingPanels = new ArrayList<>();
        this.groupPanelMap = new HashMap<>();
    }

    public void addPanel(EventGroup eg) {
        EventGroupDescriptionPanel panel = new EventGroupDescriptionPanel(this.ui,
                new Vector2f(-this.getWidth(false) * 2, this.getHeight(true)/2),
                this.getWidth(true), eg);
        panel.alignv = 1;
        this.activePanels.add(0, panel);
        this.addChild(panel);
        this.groupPanelMap.put(eg, panel);
        this.updatePositions();
    }

    public void markDone(EventGroup eg) {
        EventGroupDescriptionPanel panel = this.groupPanelMap.get(eg);
        if (panel != null) {
            panel.markDone();
        }
    }

    private void updatePositions() {
        float lastTop = this.getHeight(true) / 2;
        for (EventGroupDescriptionPanel panel : this.activePanels) {
            panel.setPos(new Vector2f(0, lastTop), 0.99999);
            lastTop -= panel.getHeight(false) + PANEL_SPACING;
        }
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        Iterator<EventGroupDescriptionPanel> panelIterator = this.activePanels.iterator();
        boolean removed = false;
        while (panelIterator.hasNext()) {
            EventGroupDescriptionPanel panel = panelIterator.next();
            if (panel.getTime() > MAX_TIME_PER_PANEL) {
                panelIterator.remove();
                this.removingPanels.add(panel);
                panel.setPos(new Vector2f(-this.getWidth(false) * 2, panel.getPos().y), 0.999);
                removed = true;
            }
        }
        if (removed) {
            this.updatePositions();
        }
        panelIterator = this.removingPanels.iterator();
        while (panelIterator.hasNext()) {
            EventGroupDescriptionPanel panel = panelIterator.next();
            if (panel.getTime() > REMOVE_TIME) {
                panelIterator.remove();
                this.removeChild(panel);
                this.groupPanelMap.remove(panel.eg);
            }
        }
    }
}
