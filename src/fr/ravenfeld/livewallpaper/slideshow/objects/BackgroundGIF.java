package fr.ravenfeld.livewallpaper.slideshow.objects;

import android.graphics.Bitmap;

import fr.ravenfeld.livewallpaper.library.objects.simple.BackgroundGIFFixed;
import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.AnimatedGIFTexture;

/**
 * Created by alecanu on 08/10/13.
 */
public class BackgroundGIF extends BackgroundGIFFixed implements  IBackground {
    private float mWidthPlane;
    public BackgroundGIF(String nameTexture, int resourceId)
            throws ATexture.TextureException {
        super(nameTexture, resourceId);
    }

    public BackgroundGIF(String nameTexture, int resourceId,
                              int textureSize) throws ATexture.TextureException {
        super(nameTexture, resourceId, textureSize);
    }

    public void updateTexture(String uri)throws ATexture.TextureException {
        mTexture.stopAnimation();

        mTexture.setPathName( uri.replaceFirst("/", "file:///"));
        mMaterial.removeTexture(mTexture);
        mMaterial.addTexture(mTexture);
        mTexture.rewind();
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
