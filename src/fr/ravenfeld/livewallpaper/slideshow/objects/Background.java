package fr.ravenfeld.livewallpaper.slideshow.objects;

import android.graphics.Bitmap;

import fr.ravenfeld.livewallpaper.library.objects.simple.BackgroundFixed;
import rajawali.materials.textures.ATexture;

/**
 * Created by alecanu on 08/10/13.
 */
public class Background extends BackgroundFixed implements  IBackground{
    private float mWidthPlane;
    public Background(String nameTexture, Bitmap bitmap) throws ATexture.TextureException {
        super(nameTexture, bitmap);
    }

    public Background(String nameTexture, int id) throws ATexture.TextureException {
        super(nameTexture, id);
    }


    public void updateTexture(Bitmap bitmap)throws ATexture.TextureException {
        mMaterial.removeTexture(mTexture);
        mTexture.shouldRecycle(true);
        mTexture.setBitmap(bitmap);
        mMaterial.addTexture(mTexture);
    }


    public void rendererModeSquare() {
        mPlane.setScaleX(1f);
        mPlane.setScaleY(1f);
        mWidthPlane = 1f;
    }

    public void rendererModeClassic(float width, float height) {
        float ratioDisplay =  height /  width;
        float ratioSize = 1f / getHeight();
        mWidthPlane = getWidth() * ratioSize * ratioDisplay;
        mPlane.setScaleX(mWidthPlane);
        mPlane.setScaleY(1);
    }

    public void rendererModeLetterBox(float width, float height) {
        float ratioDisplay =  width / height;
        float ratioSize = 1f / getWidth();
        mPlane.setScaleY(getHeight() * ratioSize * ratioDisplay);
        mPlane.setScaleX(1f);
        mWidthPlane = 1f;

    }

    public void rendererModeStretched(float width, float height) {
        float ratioDisplay =  height /  width;
        float ratioSize = 1f / getHeight();
        mPlane.setScaleX(getWidth() * ratioSize * ratioDisplay);
        mPlane.setScaleY(1f);
        mWidthPlane = 1f;
    }

    public void offsetsChanged(float xOffset) {
        mPlane.setX((1 - mWidthPlane) * (xOffset - 0.5));
    }


}
