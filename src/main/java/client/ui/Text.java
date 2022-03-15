package client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import client.Config;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import utils.UninvertibleImage;

public class Text extends UIElement {
    public static final String CURSOR_TOKEN = "<c>";
    private static final int CACHED_RENDER_PADDING = 10;
    private static Image tempRender;
    private static Graphics tempGraphics;
    private String text = ""; // private cuz fuck you
    private final List<List<String>> lines = new ArrayList<>();
    private final List<Double> lineWidths = new ArrayList<>();
    private double maxLineWidth;
    double lineWidth, lineHeight, fontsize;

    String font;
    // 0 = normal, 1 = bold, 2 = italics, 3 = both
    final UnicodeFont[] uFontFamily = new UnicodeFont[4];

    private UninvertibleImage cachedRender;
    private boolean isDirty;

    private Color color;

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
        this.color = Color.white;
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

    public void setColor(Color color) {
        this.color = color;
        this.isDirty = true;
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
            String[] words = stlines.nextToken().split("(?<= )|(?<=</?[bic]>)|(?=</?[bic]>)");
            List<String> line = new ArrayList<>();
            StringBuilder sameFontStreak = new StringBuilder(); // to reduce calls to drawString
            double currlinewidth = 0;

            for (String token : words) { // why do i do this
                // String token = st.nextToken();
                if (token.matches("</?[bic]>")) {
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
            if (tempGraphics == null) {
                tempRender = new Image(Config.WINDOW_WIDTH + CACHED_RENDER_PADDING * 2, Config.WINDOW_HEIGHT + CACHED_RENDER_PADDING * 2);
                tempGraphics = tempRender.getGraphics();
            }
            // The clipping of the current render context is inherited on new images, so outside this method we need to temporarily disable clipping
            this.cachedRender = new UninvertibleImage((int) this.getWidth(true) + CACHED_RENDER_PADDING * 2,
                    (int) this.getHeight(true) + CACHED_RENDER_PADDING * 2);
        } catch (SlickException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        tempGraphics.setColor(this.color);
        int flags = 0;
        for (int i = 0; i < this.lines.size(); i++) {
            double currlinewidth = 0;
            for (String token : this.lines.get(i)) { // why do i do this
                switch (token) {
                    case "<b>" -> flags = flags | 1;
                    case "</b>" -> flags = flags & ~1;
                    case "<i>" -> flags = flags | 2;
                    case "</i>" -> flags = flags & ~2;
                    case "<c>" -> {
                        float drawx = CACHED_RENDER_PADDING + (float) (currlinewidth + (this.maxLineWidth - this.lineWidths.get(i)) * (this.alignh + 1) / 2.);
                        float drawy = CACHED_RENDER_PADDING + (float) (this.lineHeight * i);
                        tempGraphics.drawString("|", drawx - tempGraphics.getFont().getWidth("|") * 0.6f, drawy);
                    }
                    default -> {
                        float drawx = CACHED_RENDER_PADDING + (float) (currlinewidth + (this.maxLineWidth - this.lineWidths.get(i)) * (this.alignh + 1) / 2.);
                        float drawy = CACHED_RENDER_PADDING + (float) (this.lineHeight * i);
                        tempGraphics.setFont(this.uFontFamily[flags]);
                        tempGraphics.drawString(token, drawx, drawy);
                        currlinewidth += this.uFontFamily[flags].getWidth(token);
                    }
                }
            }
        }
        // the maths assume y=0 starts at the bottom of the texture, and that the image is flipped
        tempGraphics.copyArea(this.cachedRender, 0, tempRender.getTexture().getTextureHeight() - this.cachedRender.getHeight());
        // the bottom two lines must happen in this order to prevent the text from flashing white for the frame it's being rendered
        // why? who knows
        tempGraphics.flush();
        tempGraphics.clear();
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
                // due to slick spaghetti, the cached render will inherit the current clip, so we need to temporarily disable it
                Rectangle prevClip = g.getClip();
                Rectangle prevClipCloned = null;
                if (prevClip != null) {
                    prevClipCloned = new Rectangle(prevClip.getX(), prevClip.getY(), prevClip.getWidth(), prevClip.getHeight());
                }
                g.setClip(null);
                this.repaint();
                if (prevClipCloned != null) {
                    g.setClip(prevClipCloned);
                }
            }
            if (this.cachedRender != null) {
                float drawx = this.getAbsPos().x - ((float) this.maxLineWidth * (this.alignh + 1) / 2f + CACHED_RENDER_PADDING) * (float) this.getScale();
                float drawy = this.getAbsPos().y - ((float) this.getVOff() + CACHED_RENDER_PADDING) * (float) this.getScale();
                this.cachedRender.setAlpha((float) this.getAlpha());
                g.drawImage(this.cachedRender.getScaledCopy((float) this.getScale()), drawx, drawy);
            }
            this.drawChildren(g);// why not
        }
    }

    @Override
    public String debug() {
        return super.debug() + " " + this.text;
    }
}
