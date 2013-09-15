package fr.ravenfeld.livewallpaper.slideshow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.Camera2D;
import rajawali.materials.Material;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.AnimatedGIFTexture;
import rajawali.materials.textures.Texture;
import rajawali.primitives.Plane;
import rajawali.renderer.RajawaliRenderer;
import rajawali.wallpaper.Wallpaper;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class Renderer extends RajawaliRenderer implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private final SharedPreferences mSharedPreferences;

	private enum ModeRenderer {
		CLASSIC, LETTER_BOXED, STRETCHED
	}
	private Texture mTexture;
	private AnimatedGIFTexture mAnimatedTexture;
	private Material mMaterial;
	private Plane mPlane;
	private float mWidthPlane;

	private final ArrayList<String> mListFiles;
	public Renderer(Context context) {
		super(context);

		mSharedPreferences = context.getSharedPreferences(
				Wallpaper.SHARED_PREFS_NAME, 0);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		mListFiles = new ArrayList<String>();
	}

	@Override
	protected void initScene() {
		Camera2D cam = new Camera2D();
		this.replaceAndSwitchCamera(getCurrentCamera(), cam);
		getCurrentScene().setBackgroundColor(Color.RED);
		getCurrentCamera().setLookAt(0, 0, 0);

		mPlane = new Plane(1f, 1f, 1, 1);
		mMaterial = new Material();
		mTexture = new Texture("bg", R.drawable.bg);
		mAnimatedTexture = new AnimatedGIFTexture("bgAnimated", R.drawable.bob);
		try {
			mMaterial.addTexture(mTexture);
		} catch (TextureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mPlane.setMaterial(mMaterial);
		mPlane.setPosition(0, 0, 0);
		mPlane.setRotY(180);

		initBackground();
		addChild(mPlane);
	}

	private void initBackground() {

		String uri = mSharedPreferences.getString("uri", "");

		File file = FileUtils.getFile(Uri.parse(uri));
		Bitmap b = null;
		if (file.isDirectory()) {
			List<File> list = FileUtils.getFileList(file.getAbsolutePath(),
					Settings.INCLUDE_EXTENSIONS_LIST);
			for (File item : list) {
				mListFiles.add(item.getAbsolutePath());

			}
			b = BitmapFactory.decodeFile(mListFiles.get(0));
		} else {
			if (FileUtils.getExtension(uri).equalsIgnoreCase(".gif")) {
				Log.e("TEST", "GIF " + file.exists());

				mAnimatedTexture.setPathName(file.getAbsolutePath());
				try {
					mMaterial.removeTexture(mTexture);
					mMaterial.addTexture(mAnimatedTexture);
				} catch (TextureException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mAnimatedTexture.rewind();
			} else {
				b = BitmapFactory.decodeFile(uri.replace("file:///", ""));
				mTexture.setBitmap(b);
				try {
					mMaterial.removeTexture(mAnimatedTexture);
					mMaterial.addTexture(mTexture);
				} catch (TextureException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		initPlane();

	}

	private void initPlane() {
		String renderer = mSharedPreferences.getString("rendererMode",
				"classic");
		if (renderer.equalsIgnoreCase("letter_boxed")) {
			rendererMode(ModeRenderer.LETTER_BOXED);
		} else if (renderer.equalsIgnoreCase("stretched")) {
			rendererMode(ModeRenderer.STRETCHED);
		} else {
			rendererMode(ModeRenderer.CLASSIC);
		}

	}

	private void rendererMode(ModeRenderer modeRenderer) {
		float ratioDisplay = (float) mViewportHeight / (float) mViewportWidth;
		float ratioVideo = (float) mTexture.getHeight() / mTexture.getWidth();

		if (ratioDisplay == ratioVideo) {
			mPlane.setScaleX(1f);
			mPlane.setScaleY(1f);
			mWidthPlane = 1f;
		} else if (ratioDisplay >= 1) {
			// PORTRAIT
			switch (modeRenderer) {
			case STRETCHED:
				rendererModeStretched();
				break;
			case LETTER_BOXED:
				rendererModeLetterBox();
				break;
			default:
				rendererModeClassic();
				break;
			}
		} else {
			// LANDSCAPE
			switch (modeRenderer) {
			case STRETCHED:
				rendererModeStretched();
				break;
			case LETTER_BOXED:
				rendererModeStretched();
				break;
			default:
				rendererModeStretched();
				break;
			}
		}
	}

	private void rendererModeClassic() {
		float ratioDisplay = (float) mViewportHeight / (float) mViewportWidth;
		float ratioSize = 1f / mTexture.getHeight();
		mWidthPlane = mTexture.getWidth() * ratioSize * ratioDisplay;
		mPlane.setScaleX(mWidthPlane);
		mPlane.setScaleY(1);
	}

	private void rendererModeLetterBox() {
		float ratioDisplay = (float) mViewportWidth / (float) mViewportHeight;
		float ratioSize = 1f / mTexture.getWidth();
		mPlane.setScaleY(mTexture.getHeight() * ratioSize
				* ratioDisplay);
		mPlane.setScaleX(1f);
		mWidthPlane = 1f;

	}

	private void rendererModeStretched() {
		float ratioDisplay = (float) mViewportHeight / (float) mViewportWidth;
		float ratioSize = 1f / mTexture.getHeight();
		mPlane.setScaleX(mTexture.getWidth() * ratioSize
				* ratioDisplay);
		mPlane.setScaleY(1f);
		mWidthPlane = 1f;
	}
	@Override
	public void onDrawFrame(GL10 glUnused) {
		super.onDrawFrame(glUnused);
		if (mAnimatedTexture != null) {
			try {
				mAnimatedTexture.update();
			} catch (TextureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		super.onSurfaceCreated(gl, config);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		super.onSurfaceChanged(gl, width, height);
		// if (mBackgroundSwipe != null) {
		// mBackgroundSwipe.surfaceChanged(width, height);
		// }
	}

	@Override
	public void onVisibilityChanged(boolean visible) {
		super.onVisibilityChanged(visible);

	}

	@Override
	public void onSurfaceDestroyed() {

		super.onSurfaceDestroyed();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		initBackground();
	}

	@Override
	public void onOffsetsChanged(float xOffset, float yOffset,
			float xOffsetStep, float yOffsetStep, int xPixelOffset,
			int yPixelOffset) {
		if (mPlane != null) {
			mPlane.setX((1 - mWidthPlane) * (xOffset - 0.5));
		}
	}
}
