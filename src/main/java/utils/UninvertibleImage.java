package utils;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 * This class intends to erase the ensureInverted() functionality of the slick Image class.
 * This looks absolutely unhinged and I will attempt to explain.
 *
 * The original problem was that re-rendering text every frame was kind of
 * expensive. So instead of re-rendering it each frame, we instead cache the
 * render by rendering it to an image. Then when we need to draw the text, we
 * just draw the cached image. If the text gets updated, we make a new image
 * (because the dimensions of the text might have changed) and redraw that
 * cached image. Simple.
 *
 * Except to draw the images, we need to access that image's Graphics context.
 * And when we do that on new images, there's a split second of lag. Not very
 * noticeable when playing in windowed mode, but VERY noticeable when in
 * fullscreen. The screen flashes black, you catch a glimpse of things you
 * weren't supposed to see, and overall it feels like you're suffering some kind
 * of stand attack. Turns out this is a known issue and was solved long ago. The
 * little documentation that survived the ravages of time gives
 * us a little insight into the solution:
 * https://slick.ninjacave.com/wiki/index.php?title=Rendering_to_an_Image
 * i.e. have a single static graphics context that does all the rendering work,
 * and when the time comes, we call copyArea to paste it into our individual
 * cached render. The copyArea method is magical, in that it doesn't require us
 * to create a new graphics context to update this image. Okay, makes sense.
 *
 * Except this little copyArea method does a little something extra: it ensures
 * that the target image is inverted, via the ensureInverted method. That
 * completely messes up our plan, now barely anything will ever get rendered
 * because the coordinate system is shagged, and the text that does get rendered
 * will be upside down.
 *
 * But what if our target image could not be inverted?
 *
 * Ergo, this class.
 */
public class UninvertibleImage extends Image {
    public UninvertibleImage(int width, int height) throws SlickException {
        super(width, height);
    }

    @Override
    public void ensureInverted() {
        // no i don't tink so
    }
}
