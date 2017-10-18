package qupath.extension.tracking.overlay;

import qupath.lib.gui.viewer.overlays.AbstractOverlay;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.regions.ImageRegion;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

public class PlaybackOverlay extends AbstractOverlay {

    private Icon cursorIcon = null, eyeIcon = null;
    private boolean drawCursor = false, drawEye = false;

    public PlaybackOverlay() {
        this.cursorIcon = new Icon("cursor");
        this.eyeIcon = new Icon("eye");
    }

    @Override
    public void paintOverlay(Graphics2D g2d, ImageRegion imageRegion, double downsampleFactor, ImageObserver observer, boolean paintCompletely) {
        Rectangle clippingRectangle = new Rectangle(imageRegion.getX(), imageRegion.getY(), imageRegion.getWidth(), imageRegion.getHeight());
        if(drawCursor && cursorIcon != null) {
            cursorIcon.drawIcon(g2d, downsampleFactor, clippingRectangle);
        }
        if(drawEye && eyeIcon != null) {
            eyeIcon.drawIcon(g2d, downsampleFactor, clippingRectangle);
        }
    }

    public void updateIconLocations(ViewRecordingFrame frame) {
        drawCursor = frame.hasCursorPosition();
        if(frame.hasCursorPosition() && cursorIcon != null) {
            Point2D p = frame.getCursorPosition();
            cursorIcon.setLocation((long)p.getX(), (long)p.getY());
        }
        drawEye = frame.hasEyePosition();
        if(frame.hasEyePosition() && eyeIcon != null) {
            Point2D p = frame.getEyePosition();
            eyeIcon.setLocation((long)p.getX(), (long)p.getY());
        }
    }

    static class Icon {
        long x,y;
        BufferedImage icon;

        Icon(String type) {
            BufferedImage img;
            try {
                ClassLoader classLoader = getClass().getClassLoader();
                icon = ImageIO.read(classLoader.getResource("playback/" + type + ".png"));
            } catch (IOException e) {
                System.err.println("Failed to load " + type + " icon!");
                e.printStackTrace();
            }
        }
        public void setLocation(long x, long y) {
            this.x = x;
            this.y = y;
        }

        public void drawIcon(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
            if(clippingRectangle.contains(new Point((int)x,(int)y)))
                g2d.drawImage(icon,
                        (int)x - (int)((double)icon.getWidth() * downsampleFactor / 2) ,
                        (int)y - (int)((double)icon.getHeight() * downsampleFactor / 2),
                        (int)(icon.getWidth() * downsampleFactor),
                        (int)(icon.getHeight() * downsampleFactor),
                        null);
        }
    }
}
