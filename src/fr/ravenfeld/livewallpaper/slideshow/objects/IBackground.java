package fr.ravenfeld.livewallpaper.slideshow.objects;

/**
 * Created by alecanu on 08/10/13.
 */
public interface  IBackground {

    public void rendererModeSquare();
    public void rendererModeClassic(float width, float height);
    public void rendererModeLetterBox(float width, float height);
    public void rendererModeStretched(float width, float height);
    public void offsetsChanged(float xOffset);
    public int getWidth();
    public int getHeight();
    public void setVisible(boolean visible);
}
