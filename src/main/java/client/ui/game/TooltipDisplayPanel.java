package client.ui.game;

import client.tooltip.TooltipCard;
import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.ui.Animation;
import client.ui.Text;
import client.ui.UI;
import client.ui.UIBox;

public class TooltipDisplayPanel extends UIBox {
    Tooltip tooltip;
    final Text name;
    final Text description;
    final UIBox cardImageDisplayer;

    public TooltipDisplayPanel(UI ui) {
        super(ui, new Vector2f(0, 0), new Vector2f(430, 0), "ui/uiboxborder.png");
        this.margins.set(10, 10);
        this.alignv = -1;
        this.cardImageDisplayer = new UIBox(ui, new Vector2f(), new Vector2f( 300, 360));
        this.cardImageDisplayer.relpos = true;
        this.cardImageDisplayer.setAlpha(0.12f);
        this.cardImageDisplayer.ignorehitbox = true;
        this.addChild(this.cardImageDisplayer);
        this.name = new Text(ui, new Vector2f(-this.getWidth(true) / 2, -this.getHeight(true) / 2), "name",
                this.getWidth(true), 40, 46, -1, -1);
        this.addChild(name);
        this.description = new Text(ui, new Vector2f(-this.getWidth(true) / 2, this.name.getBottom(false, false) + 10), "jeff",
                this.getWidth(true), 32, 36, -1, -1);
        this.addChild(description);
        this.clip = true;
    }

    public void setTooltip(Tooltip tooltip) {
        this.tooltip = tooltip;
        this.name.setText("<b>" + tooltip.name + "</b>");
        this.description.setText(tooltip.description);
        this.setDim(new Vector2f(this.getWidth(false), this.name.getHeight(false) + this.description.getHeight(false) + 30));
        this.name.setPos(new Vector2f(- this.getWidth(true) / 2, -this.getHeight(true) / 2), 1);
        this.description.setPos(new Vector2f(-this.getWidth(true) / 2,
                this.name.getBottom(false, false) + 10), 1);
        if (tooltip instanceof TooltipCard) {
            Animation animation = ((TooltipCard) this.tooltip).animation.get();
            this.cardImageDisplayer.setAnimation(animation);
            this.cardImageDisplayer.setVisible(true);
        } else {
            this.cardImageDisplayer.setVisible(false);
        }
    }
}
