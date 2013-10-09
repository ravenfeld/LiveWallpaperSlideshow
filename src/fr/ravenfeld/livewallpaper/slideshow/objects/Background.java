package fr.ravenfeld.livewallpaper.slideshow.objects;

import android.graphics.Bitmap;

import fr.ravenfeld.livewallpaper.library.objects.simple.BackgroundFixed;
import rajawali.materials.textures.ATexture;

/**
 * Created by alecanu on 08/10/13.
 */
public class Background extends BackgroundFixed implements  IBackground{
    private float mWidthPlane;
    private int mWidthBitmap;
    private int mHeightBitmap;
    public Background(String nameTexture, Bitmap bitmap) throws ATexture.TextureException {
        super(nameTexture, bitmap);
    }

    public Background(String nameTexture, int id) throws ATexture.TextureException {
        super(nameTexture, id);
    }

    public void rendererModeSquare() {
        mPlane.setScaleX(1f);
        mPlane.setScaleY(1f);
        mWidthPlane = 1f;
    }

    public void rendererModeClassic(float width, float height) {
        float ratioDisplay =  height /  width;
        float ratioSize = 1f / getHeightBitmap();
        mWidthPlane = getWidthBitmap() * ratioSize * ratioDisplay;
        mPlane.setScaleX(mWidthPlane);
        mPlane.setScaleY(1);
    }

    public void rendererModeLetterBox(float width, float height) {
        float ratioDisplay =  width / height;
        float ratioSize = 1f / getWidthBitmap();
        mPlane.setScaleY(getHeightBitmap() * ratioSize * ratioDisplay);
        mPlane.setScaleX(1f);
        mWidthPlane = 1f;

    }

    public void rendererModeStretched(float width, float height) {
        float ratioDisplay =  height /  width;
        float ratioSize = 1f / getHeightBitmap();
        mPlane.setScaleX(getWidthBitmap() * ratioSize * ratioDisplay);
        mPlane.setScaleY(1f);
        mWidthPlane = 1f;
    }

    public void offsetsChanged(float xOffset) {
        mPlane.setX((1 - mWidthPlane) * (xOffset - 0.5));
    }

    public int getWidthBitmap(){
        return mWidthBitmap;
    }

    public int getHeightBitmap(){
        return mHeightBitmap;
    }

    public void setWidthBitmap(int width){
        mWidthBitmap =width;
    }
    public void setHeightBitmap(int height){
        mHeightBitmap =height;
    }
}
