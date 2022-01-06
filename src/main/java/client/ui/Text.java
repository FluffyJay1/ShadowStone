package client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.Game;

public class Text extends UIElement {
    private String text = ""; // private cuz fuck you
    private final List<List<String>> lines = new ArrayList<>();
    private final List<Double> lineWidths = new ArrayList<>();
    private double maxLineWidth;
    double lineWidth, lineHeight, fontsize;

    String font;
    // 0 = normal, 1 = bold, 2 = italics, 3 = both
    final UnicodeFont[] uFontFamily = new UnicodeFont[4];

    private Image cachedRender;
    private Graphics cachedGraphics;
    private boolean isDirty;

    public Text(UI ui, Vector2f pos, String text, double linewidth, double lineheight, String font, double fontsize,
            int alignh, int alignv) {
        super(ui, pos);
        this.ignorehitbox = true;
        this.lineWidth = linewidth;
        this.lineHeight = lineheight;
        this.maxLineWidth = linewidth;
        this.alignh = alignh;
        this.alignv = alignv;

        this.setFont(font, fontsize);
        this.setText(text);
        this.isDirty = true;
    }

    public void setFont(String font, double fontsize) {
        this.font = font;
        this.fontsize = fontsize;
        for (int i = 0; i < 4; i++) {
            this.uFontFamily[i] = Game.getFont(font, fontsize, (i & 1) > 0, (i & 2) > 0);
        }
        if (this.text != null) {
            this.updateText();
        }
    }

    public void setText(String text) {
        if (this.text.equals(text)) { // optimization lul
            return;
        }
        this.text = text;
        this.updateText();
    }

    // does preprocessing on the text to make rendering easier + faster
    // also for height calculations
    // repaint() does the actual re-rendering
    public void updateText() {
        int flags = 0; // bold and italics
        this.lines.clear();
        this.lineWidths.clear();
        this.maxLineWidth = 0;
        StringTokenizer stlines = new StringTokenizer(text, "\n");
        while (stlines.hasMoreTokens()) {
            // StringTokenizer st = new StringTokenizer(stlines.nextToken(), "
            // ");
            String[] words = stlines.nextToken().split("(?<= )|(?<=</?[bi]>)|(?=</?[bi]>)");
            List<String> line = new ArrayList<>();
            StringBuilder sameFontStreak = new StringBuilder(); // to reduce calls to drawString
            double currlinewidth = 0;

            for (String token : words) { // why do i do this
                // String token = st.nextToken();
                if (token.matches("</?[bi]>")) {
                    if (sameFontStreak.length() > 0) {
                        String sameFontStreakString = sameFontStreak.toString();
                        line.add(sameFontStreakString);
                        currlinewidth += this.uFontFamily[flags].getWidth(sameFontStreakString);
                        sameFontStreak = new StringBuilder();
                    }
                    line.add(token);
                    switch (token) {
                        case "<b>" -> flags = flags | 1;
                        case "</b>" -> flags = flags & ~1;
                        case "<i>" -> flags = flags | 2;
                        case "</i>" -> flags = flags & ~2;
                    }
                } else {
                    String sameFontStreakString = sameFontStreak.toString();
                    String trimmedToken = token.replaceAll("^\\s+", "");
                    if (sameFontStreakString.isEmpty() && currlinewidth == 0) { // first word
                        if (!trimmedToken.isEmpty()) {
                            sameFontStreak.append(trimmedToken);
                        }
                    } else if (currlinewidth + this.uFontFamily[flags].getWidth(sameFontStreakString + token) > this.lineWidth) { // newline
                        if (sameFontStreak.length() > 0) {
                            line.add(sameFontStreakString);
                            currlinewidth += this.uFontFamily[flags].getWidth(sameFontStreakString);
                        }
                        // remove excess spaces at end of line
                        if (line.size() > 0) {
                            String last = line.get(line.size() - 1);
                            String lastTrimmed = last.replaceAll("\\s+$", "");
                            currlinewidth += this.uFontFamily[flags].getWidth(lastTrimmed) - this.uFontFamily[flags].getWidth(last);
                            line.set(line.size() - 1, lastTrimmed);
                        }
                        sameFontStreak = new StringBuilder();
                        if (!trimmedToken.isEmpty()) {
                            sameFontStreak.append(trimmedToken);
                        }
                        this.lines.add(line);
                        this.lineWidths.add(currlinewidth);
                        if (currlinewidth > this.maxLineWidth) {
                            this.maxLineWidth = currlinewidth;
                        }
                        line = new ArrayList<>();
                        currlinewidth = 0;
                    } else {
                        sameFontStreak.append(token);
                    }
                }
            }
            if (sameFontStreak.length() > 0) {
                String sameFontStreakString = sameFontStreak.toString();
                line.add(sameFontStreakString);
                currlinewidth += this.uFontFamily[flags].getWidth(sameFontStreakString);
            }
            this.lines.add(line);
            this.lineWidths.add(currlinewidth);
            if (currlinewidth > maxLineWidth) {
                maxLineWidth = currlinewidth;
            }
        }
        this.isDirty = true;
    }

    public void repaint() {
        try {
            cachedRender = new Image((int) this.getWidth(true), (int) this.getHeight(true));
            cachedGraphics = cachedRender.getGraphics();
            /*
             * so there's this wonderful feature where the clipping of the game's graphics context
             * affects the clipping of completely unrelated graphics contexts created on
             * images 
             */
            cachedGraphics.setClip(null); // so despite how dumb this line looks, it's completely necessary
        } catch (SlickException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int flags = 0;
        for (int i = 0; i < this.lines.size(); i++) {
            double currlinewidth = 0;
            for (String token : this.lines.get(i)) { // why do i do this
                switch (token) {
                    case "<b>" -> flags = flags | 1;
                    case "</b>" -> flags = flags & ~1;
                    case "<i>" -> flags = flags | 2;
                    case "</i>" -> flags = flags & ~2;
                    default -> {
                        float drawx = (float) (currlinewidth + (this.maxLineWidth - this.lineWidths.get(i)) * (this.alignh + 1) / 2.);
                        float drawy = (float) (this.lineHeight * i);
                        cachedGraphics.setFont(this.uFontFamily[flags]);
                        cachedGraphics.drawString(token, drawx, drawy);
                        currlinewidth += this.uFontFamily[flags].getWidth(token);
                    }
                }
            }
        }
        cachedGraphics.flush();
        this.isDirty = false;
    }

    @Override
    public double getWidth(boolean margin) {
        return Math.max(this.lineWidth, this.maxLineWidth);
    }

    @Override
    public double getHeight(boolean margin) {
        return this.lines.size() == 0 ? 0 : this.lineHeight * (this.lines.size() - 1) + this.fontsize * 1.5; // HELP
    }

    @Override
    public void draw(Graphics g) {
        if (this.isVisible()) {
            if (this.isDirty) {
                this.repaint();
            }
            if (this.cachedRender != null) {
                float drawx = (float) (this.getAbsPos().x - this.maxLineWidth * (this.alignh + 1) / 2.);
                float drawy = (float) (this.getAbsPos().y - this.getVOff());
                g.drawImage(this.cachedRender, drawx, drawy);
            }
            this.drawChildren(g);// why not
        }
    }

    @Override
    public String debug() {
        return super.debug() + " " + this.text;
    }
}
