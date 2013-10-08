package fr.ravenfeld.livewallpaper.slideshow;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import fr.ravenfeld.livewallpaper.library.objects.simple.ABackground;
import fr.ravenfeld.livewallpaper.slideshow.objects.Background;
import fr.ravenfeld.livewallpaper.slideshow.objects.BackgroundGIF;
import fr.ravenfeld.livewallpaper.slideshow.objects.IBackground;
import rajawali.Camera2D;
import rajawali.animation.Animation3D.RepeatMode;
import rajawali.animation.RotateAnimation3D;
import rajawali.materials.Material;
import rajawali.materials.methods.DiffuseMethod;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.AnimatedGIFTexture;
import rajawali.materials.textures.Texture;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Cube;
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
import android.view.animation.AccelerateDecelerateInterpolator;

import com.ipaulpro.afilechooser.utils.FileUtils;

import fr.ravenfeld.livewallpaper.slideshow.utils.Util;

public class Renderer extends RajawaliRenderer implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private final SharedPreferences mSharedPreferences;

	private enum ModeRenderer {
		CLASSIC, LETTER_BOXED, STRETCHED
	}

	private enum TimePref {
		TIME_5_SECONDS, TIME_1_MINUTE, TIME_5_MINUTES, TIME_15_MINUTES, TIME_30_MINUTES, TIME_1_HOUR, TIME_1_DAY
	}

	private TimePref mTimePref = TimePref.TIME_5_MINUTES;

    private Background mBackground_1;
    private Background mBackground_2;
    private BackgroundGIF mBackgroundGIF_1;
    private BackgroundGIF mBackgroundGIF_2;

	private final ArrayList<String> mListFiles;
	private int mIdCurrent = -1;
	private boolean mUseGIF=false;
	private boolean mUseFolder;
	private Date mDateLastChange;
	private final Object mLock = new Object();
	private float mXoffset;
    private int mCurrent=0;
	public Renderer(Context context) {
		super(context);

		mSharedPreferences = context.getSharedPreferences(
				Wallpaper.SHARED_PREFS_NAME, 0);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		mListFiles = new ArrayList<String>();
		updateTime();
		mDateLastChange = new Date();
	}

	@Override
	protected void initScene() {
		setFrameRate(30);
		Camera2D cam = new Camera2D();
		this.replaceAndSwitchCamera(getCurrentCamera(), cam);
		getCurrentScene().setBackgroundColor(Color.RED);
		getCurrentCamera().setLookAt(0, 0, 0);

        try{
            mBackground_1 = new Background("bg", R.drawable.bg);
            mBackground_2 = new Background("bg", R.drawable.bg);
            mBackgroundGIF_1 = new BackgroundGIF("bg", R.drawable.bob);
            mBackgroundGIF_2 = new BackgroundGIF("bg", R.drawable.bob);
        }catch (TextureException e){
            e.printStackTrace();
        }
		initBackground();
		addChild(mBackground_1.getObject3D());
        addChild(mBackground_2.getObject3D());
        addChild(mBackgroundGIF_1.getObject3D());
        addChild(mBackgroundGIF_2.getObject3D());
		//initTest();
	}

	private void initBackground() {
		String uri = mSharedPreferences.getString("uri", "");
		if (!uri.equalsIgnoreCase("")) {
			File file = FileUtils.getFile(Uri.parse(uri));
			if (file.isDirectory()) {
				mUseFolder = true;
				mListFiles.clear();
				List<File> list = FileUtils.getFileList(file.getAbsolutePath(),
						Settings.INCLUDE_EXTENSIONS_LIST);
				for (File item : list) {
					if (item.isFile()) {
						mListFiles.add(item.getAbsolutePath());
					}
				}
				if (mListFiles.size() > 0) {
					changedBackground();
				} else {
					loadResourceDefault(R.drawable.bg);
				}

			} else if (file.isFile()) {
				mUseFolder = false;
				loadFile(file.getAbsolutePath());
			}
		}
		initPlane();
	}

	private void changedBackground() {
		synchronized (mLock) {
			boolean random = mSharedPreferences
					.getBoolean("random_file", false);
            String uri;
			if (random) {
				 uri =mListFiles.get(randomId());
			} else {
                 uri = mListFiles.get(nextId());
			}
            updateBackground(uri);
        }
	}

    private void updateBackground(String uri){
        loadFile(uri);
        visibleBackground();
        initPlane();
        onOffsetsChanged(mXoffset, 0, 0, 0, 0, 0);
        mTextureManager.reload();
    }

	private int nextId() {
		mIdCurrent += 1;
		if (mIdCurrent >= mListFiles.size()) {
			mIdCurrent = 0;
		}
		return mIdCurrent;
	}

	private int randomId() {
		Random r = new Random();
		return r.nextInt(mListFiles.size());
	}

	private void loadFile(String uri) {
		File file = FileUtils.getFile(Uri.parse(uri.replaceFirst("/",
				"file:///")));
		if (file.isFile()) {
			Log.e("TEST", "FILE " + uri);

			if (FileUtils.getExtension(uri).equalsIgnoreCase(".gif")) {
                mUseGIF=true;
                try {
                    if(mCurrent==0){
                        mBackgroundGIF_2.updateTexture(uri);
                        mCurrent=1;
                    }else{
                        mBackgroundGIF_1.updateTexture(uri);
                        mCurrent=0;
                    }
                } catch (TextureException e) {
                    Log.e("TEST","TEXTURE EXCEPTION");
                    e.printStackTrace();
                }
            } else {
				mUseGIF = false;
				try {
					Bitmap b = Util.decodeUri(mContext,
							Uri.parse("file:///" + file.getPath()));

                    if(mCurrent==0){
                        mBackground_2.updateTexture(b);
                        mCurrent=1;
                    }else{
                        mBackground_1.updateTexture(b);
                        mCurrent=0;
                    }
				} catch (TextureException e) {
                    Log.e("TEST", "TEXTURE EXCEPTION");
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					Log.e("TEST", "FILE NOT FOUND");
					e.printStackTrace();
				}
			}
		} else {
			changedBackground();
			mListFiles.remove(uri);

		}
	}

	private void loadResourceDefault(int resourceId) {
		mUseGIF = false;
		Bitmap b = BitmapFactory.decodeResource(mContext.getResources(),
				resourceId);
		try {
            mBackground_1.updateTexture(b);

		} catch (TextureException e) {
            Log.e("TEST", "TEXTURE EXCEPTION");
            e.printStackTrace();
		}
        mCurrent=0;
	}

    private void visibleBackground(){
        mBackground_1.setVisible(false);
        mBackground_2.setVisible(false);
        mBackgroundGIF_1.setVisible(false);
        mBackgroundGIF_2.setVisible(false);
        getBackground().setVisible(true);

    }

	private boolean checkDate() {
		boolean bool = false;
		Date date = new Date();
		if (mDateLastChange == null) {
			mDateLastChange = date;
		}

        GregorianCalendar lastDate = new GregorianCalendar();
        lastDate.setTime(mDateLastChange);

		switch (mTimePref) {
		case TIME_5_SECONDS:
			lastDate.add(GregorianCalendar.SECOND, 5);
			break;
		case TIME_1_MINUTE:
			lastDate.add(GregorianCalendar.MINUTE, 1);
			break;
		case TIME_5_MINUTES:
			lastDate.add(GregorianCalendar.MINUTE, 5);
			break;
		case TIME_15_MINUTES:
			lastDate.add(GregorianCalendar.MINUTE, 15);
			break;
		case TIME_30_MINUTES:
			lastDate.add(GregorianCalendar.MINUTE, 30);
			break;
		case TIME_1_HOUR:
			lastDate.add(GregorianCalendar.HOUR_OF_DAY, 1);
			break;
		case TIME_1_DAY:
			lastDate.add(GregorianCalendar.DAY_OF_MONTH, 1);
			break;

		}
		if (date.after(lastDate.getTime())) {
			bool = true;
			mDateLastChange = date;
		}
		return bool;
	}

	private void updateTime() {
		String timePref = mSharedPreferences
				.getString("time", "time_5_minutes");
		if (timePref.equalsIgnoreCase("time_5_seconds")) {
			mTimePref = TimePref.TIME_5_SECONDS;
		} else if (timePref.equalsIgnoreCase("time_1_minute")) {
			mTimePref = TimePref.TIME_1_MINUTE;
		} else if (timePref.equalsIgnoreCase("time_5_minutes")) {
			mTimePref = TimePref.TIME_5_MINUTES;
		} else if (timePref.equalsIgnoreCase("time_15_minutes")) {
			mTimePref = TimePref.TIME_15_MINUTES;
		} else if (timePref.equalsIgnoreCase("time_30_minutes")) {
			mTimePref = TimePref.TIME_30_MINUTES;
		} else if (timePref.equalsIgnoreCase("time_1_hour")) {
			mTimePref = TimePref.TIME_1_HOUR;
		} else if (timePref.equalsIgnoreCase("time_1_day")) {
			mTimePref = TimePref.TIME_1_DAY;
		}
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
		float ratioVideo = getTextureHeight() / getTextureWidth();

		if (ratioDisplay == ratioVideo) {
            rendererModeSquare();
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

    private void rendererModeSquare() {
getBackground().rendererModeSquare();
    }

	private void rendererModeClassic() {
getBackground().rendererModeClassic(mViewportWidth, mViewportHeight);
	}

	private void rendererModeLetterBox() {
getBackground().rendererModeLetterBox(mViewportWidth, mViewportHeight);
	}

	private void rendererModeStretched() {
        getBackground().rendererModeStretched(mViewportWidth, mViewportHeight);
	}

	@Override
	public void onDrawFrame(GL10 glUnused) {
		synchronized (mLock) {

			super.onDrawFrame(glUnused);
			if (mBackgroundGIF_1 != null && mBackgroundGIF_2!= null) {
				try {
                    mBackgroundGIF_1.update();
                    mBackgroundGIF_2.update();
				} catch (TextureException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (mBackground_1 != null && mBackground_2 != null
					&& mListFiles != null && mListFiles.size() > 0 && mUseFolder
					&&checkDate()) {
				changedBackground();
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
		initPlane();
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
		synchronized (mLock) {
			if (mBackground_1 != null && mBackground_2 != null) {
				initBackground();
				updateTime();
			}
		}
	}

	@Override
	public void onOffsetsChanged(float xOffset, float yOffset,
			float xOffsetStep, float yOffsetStep, int xPixelOffset,
			int yPixelOffset) {
		if (mBackground_1 != null && mBackground_2!= null) {
			mXoffset = xOffset;
            getBackground().offsetsChanged(xOffset);
		}
	}

	private float getTextureWidth() {
        return getBackground().getWidth();
	}

	private float getTextureHeight() {
         return getBackground().getHeight();
	}

    private IBackground getBackground(){
        if(mCurrent==0){
            if(mUseGIF){
                return mBackgroundGIF_1;
            }else{
                return mBackground_1;
            }
        }else{
            if(mUseGIF){
                return mBackgroundGIF_2;
            }else{
                return mBackground_2;
            }
        }
    }


	private void initTest(){


		try {
			Cube cube = new Cube(0.1f);
			Material material = new Material();
			material.enableLighting(true);
			material.setDiffuseMethod(new DiffuseMethod.Lambert());
			material.addTexture(new Texture("rajawaliTex",
					R.drawable.rajawali_tex));
			material.setColorInfluence(0);
			cube.setMaterial(material);
			addChild(cube);

			Vector3 axis = new Vector3(3, 1, 6);
			axis.normalize();
			RotateAnimation3D anim = new RotateAnimation3D(axis, 360);
			anim.setDuration(8000);
			anim.setRepeatMode(RepeatMode.INFINITE);
			anim.setInterpolator(new AccelerateDecelerateInterpolator());
			anim.setTransformable3D(cube);
			registerAnimation(anim);
			anim.play();

		} catch (TextureException e) {
			e.printStackTrace();
		}
	}

}
