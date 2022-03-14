package client.ui;

import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Vector2f;

import client.Game;

/**
 * Animation class deals with sprites in a spritesheet and how (and if) they
 * animate. The purpose of this class is to allow UIElements to modify how they
 * look in real-time using minimal code.
 * 
 * Frames in an animation are ordered in reading order: left to right, top to
 * bottom.
 * 
 * @author Michael
 *
 */
public class Animation implements Cloneable {
    private final SpriteSheet sheet;
    private int frame = 0;
    private double timer = 0, frameInterval = 1;
    public boolean play = false, loop = false, hflip = false, vflip = false;

    /**
     * Constructor for animation object
     * 
     * @param path
     *            the path for the source sprite sheet
     * @param framedim
     *            number of frames in each row/column
     * @param spacing
     *            spacing between the sprites
     * @param margin
     *            padding around the spritesheet
     * @param filter filter type from Image class
     */
    public Animation(String path, Vector2f framedim, int spacing, int margin, int filter) {
        Image i = Game.getImage(path);
        i.setFilter(filter);
        this.sheet = new SpriteSheet(i, (int) ((i.getWidth() - margin * 2) / framedim.x) - spacing,
                (int) ((i.getHeight() - margin * 2) / framedim.y) - spacing, spacing, margin);
    }

    /**
     * Constructor for animation object
     *
     * @param path
     *            the path for the source sprite sheet
     * @param framedim
     *            number of frames in each row/column
     * @param spacing
     *            spacing between the sprites
     * @param margin
     *            padding around the spritesheet
     */
    public Animation(String path, Vector2f framedim, int spacing, int margin) {
        this(path, framedim, spacing, margin, Image.FILTER_LINEAR);
    }

    /**
     * Simple constructor for a single image animation
     * @param i The image
     */
    public Animation(Image i) {
        this.sheet = new SpriteSheet(i, i.getWidth(), i.getHeight(), 0, 0);
    }

    /**
     * Set the amount of time between each frame
     * 
     * @param interval
     *            the time interval between frames
     */
    public void setFrameInterval(double interval) {
        this.timer = 0;
        if (interval != 0) {
            this.frameInterval = interval;
        } else {
            this.frameInterval = 0.00001; // yeah close enough
        }
    }

    /**
     * Sets the current frame of the animation
     * 
     * @param frame
     *            the frame to set to
     */
    public void setFrame(int frame) {
        this.frame = frame;
        if (this.frame > this.sheet.getHorizontalCount() * this.sheet.getVerticalCount()) {
            if (this.loop) {
                this.frame %= this.sheet.getHorizontalCount() * this.sheet.getVerticalCount();
            } else {
                this.frame = this.sheet.getHorizontalCount() * this.sheet.getVerticalCount() - 1;
                this.play = false;
            }
        }
        if (this.frame < 0) { // somehow if this happens
            this.frame = 0;
        }
    }

    public void update(double frametime) {
        if (this.play) {
            this.timer += frametime;
            if (this.timer > this.frameInterval) {
                this.frame += (int) (this.timer / this.frameInterval);
                this.timer %= this.frameInterval;
            }
            if (this.frame >= this.sheet.getHorizontalCount() * this.sheet.getVerticalCount()) {
                if (this.loop) {
                    this.frame %= this.sheet.getHorizontalCount() * this.sheet.getVerticalCount();
                } else {
                    this.frame = this.sheet.getHorizontalCount() * this.sheet.getVerticalCount() - 1;
                    this.play = false;
                }
            }
        }
    }

    /**
     * Gets the image at the current frame
     * 
     * @return the image at the current frame
     */
    public Image getCurrentFrame() {
        int x = this.frame % this.sheet.getHorizontalCount();
        int y = this.frame / this.sheet.getHorizontalCount();
        return this.sheet.getSubImage(x, y).getFlippedCopy(hflip, vflip);
    }

    @Override
    public Animation clone() {
        try {
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            // this should be good lol
            return (Animation) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
