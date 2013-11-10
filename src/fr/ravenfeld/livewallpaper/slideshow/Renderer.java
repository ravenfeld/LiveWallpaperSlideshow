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

import fr.ravenfeld.livewallpaper.library.objects.Utils;
import fr.ravenfeld.livewallpaper.slideshow.objects.Background;
import fr.ravenfeld.livewallpaper.slideshow.objects.BackgroundGIF;
import fr.ravenfeld.livewallpaper.slideshow.objects.IBackground;
import rajawali.Camera2D;
import rajawali.animation.Animation;
import rajawali.animation.Animation3D;
import rajawali.animation.DisappearAnimationTexture;
import rajawali.animation.RotateAnimation3D;
import rajawali.materials.Material;
import rajawali.materials.methods.DiffuseMethod;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.Texture;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Cube;
import rajawali.renderer.RajawaliRenderer;

import android.app.WallpaperManager;
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


public class Renderer extends RajawaliRenderer implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private enum ModeRenderer {
        CLASSIC, LETTER_BOXED, STRETCHED
    }

    private enum TimePref {
        TIME_5_SECONDS, TIME_30_SECONDS, TIME_1_MINUTE, TIME_5_MINUTES, TIME_15_MINUTES, TIME_30_MINUTES, TIME_1_HOUR, TIME_1_DAY
    }

    private TimePref mTimePref = TimePref.TIME_5_MINUTES;

    private Background mBackground;
    private BackgroundGIF mBackgroundGIF;

    private final ArrayList<String> mListFiles;
    private int mIdCurrent = -1;
    private boolean mUseGIF = false;
    private boolean mUseFolder;
    private Date mDateLastChange;
    private final Object mLock = new Object();
    private float mXOffset =0.5f;
    private final int REQUIRED_SIZE_WIDTH;
    private final int REQUIRED_SIZE_HEIGHT;

    public Renderer(Context context) {
        super(context);
        mListFiles = new ArrayList<String>();
        mDateLastChange = new Date();
        WallpaperManager wallpaperManager = WallpaperManager
                .getInstance(mContext);
        REQUIRED_SIZE_WIDTH = wallpaperManager
                .getDesiredMinimumWidth();
        REQUIRED_SIZE_HEIGHT = wallpaperManager
                .getDesiredMinimumHeight();
    }

    @Override
    public void setSharedPreferences(SharedPreferences preferences) {
        super.setSharedPreferences(preferences);
        preferences.registerOnSharedPreferenceChangeListener(this);
        updateTime();
    }

    @Override
    protected void initScene() {
        setFrameRate(30);
        Camera2D cam = new Camera2D();
        this.replaceAndSwitchCamera(getCurrentCamera(), cam);
        getCurrentScene().setBackgroundColor(Color.BLACK);
        getCurrentCamera().setLookAt(0, 0, 0);

        try {
            mBackground = new Background("bg", R.drawable.rajawali_tex);
            loadResourceDefault(R.drawable.bg);
            mBackgroundGIF = new BackgroundGIF("bg", R.drawable.bob);
        } catch (TextureException e) {
            e.printStackTrace();
        }
        initBackground();
        addChild(mBackground.getObject3D());
        addChild(mBackgroundGIF.getObject3D());
        //initTest();
    }

    private void initBackground() {
        String uri = preferences.getString("uri", "");
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
            boolean random = preferences
                    .getBoolean("random_file", false);
            String uri;
            if (random) {
                uri = mListFiles.get(randomId());
            } else {
                uri = mListFiles.get(nextId());
            }
            updateBackground(uri);
        }
    }

    private void updateBackground(String uri) {

        transitionBackground(uri);

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
                mUseGIF = true;
                try {

                    mBackgroundGIF.updateTexture(uri);

                } catch (TextureException e) {
                    Log.e("TEST", "TEXTURE EXCEPTION");
                    e.printStackTrace();
                }
            } else {
                mUseGIF = false;
                try {
                    Bitmap b = Utils.decodeUri(mContext,
                            Uri.parse("file:///" + file.getPath()));
                    mBackground.setWidthBitmap(b.getWidth());
                    mBackground.setHeightBitmap(b.getHeight());
                    mBackground.setTexture(Bitmap.createScaledBitmap(b, REQUIRED_SIZE_WIDTH, REQUIRED_SIZE_HEIGHT, true));
                    b.recycle();
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
        mBackground.setTexture(Bitmap.createScaledBitmap(b, REQUIRED_SIZE_WIDTH, REQUIRED_SIZE_HEIGHT, true));
        b.recycle();
    }

    private void transitionBackground(final String uri) {
        boolean nextImageUseGIF = false;
        File file = FileUtils.getFile(Uri.parse(uri.replaceFirst("/",
                "file:///")));
        if (file.isFile() && FileUtils.getExtension(uri).equalsIgnoreCase(".gif")) {
            nextImageUseGIF = true;
        }
        transitionBackgroundJPG(uri, nextImageUseGIF);

        transitionBackgroundGIF(uri, nextImageUseGIF);
    }

    private void transitionBackgroundJPG(final String uri, boolean nextImageUseGIF) {
        DisappearAnimationTexture transition = new DisappearAnimationTexture() {
            protected void eventRepeat() {
                super.eventRepeat();
                loadFile(uri);
                mBackground.getTexture().setInfluence(0);
                mBackground.setVisible(true);
                initPlane();
                onOffsetsChanged(mXOffset, 0, 0, 0, 0, 0);
                mTextureManager.replaceTexture(mBackground.getTexture());

            }

            protected void eventEnd() {
                super.eventEnd();
                if (mBackground.getTexture().getInfluence() == 0) {
                    mBackground.setVisible(false);
                }
                unregisterAnimation(this);
            }
        };
        transition.setTexture(mBackground.getTexture());
        if (nextImageUseGIF) {
            transition.setRepeatMode(Animation.RepeatMode.NONE);
        } else {
            transition.setRepeatMode(Animation3D.RepeatMode.REVERSE);
            transition.setRepeatCount(1);
        }
        transition.setDuration(750);

        registerAnimation(transition);
        transition.play();
    }

    private void transitionBackgroundGIF(final String uri, boolean nextImageUseGIF) {
        DisappearAnimationTexture transitionGIF = new DisappearAnimationTexture() {
            protected void eventRepeat() {
                super.eventRepeat();
                loadFile(uri);
                mBackgroundGIF.getTexture().setInfluence(0);
                mBackgroundGIF.setVisible(true);
                initPlane();
                onOffsetsChanged(mXOffset, 0, 0, 0, 0, 0);
                mBackgroundGIF.getTexture().setInfluence(0);
            }

            protected void eventEnd() {
                super.eventEnd();
                if (mBackgroundGIF.getTexture().getInfluence() == 0) {
                    mBackgroundGIF.setVisible(false);
                }
                unregisterAnimation(this);
            }
        };
        transitionGIF.setTexture(mBackgroundGIF.getTexture());
        if (nextImageUseGIF) {
            transitionGIF.setRepeatMode(Animation.RepeatMode.REVERSE);
            transitionGIF.setRepeatCount(1);
        } else {
            transitionGIF.setRepeatMode(Animation.RepeatMode.NONE);
        }
        transitionGIF.setDuration(750);

        registerAnimation(transitionGIF);
        transitionGIF.play();
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
            case TIME_30_SECONDS:
                lastDate.add(GregorianCalendar.SECOND, 30);
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
        String timePref = preferences
                .getString("time", "time_5_minutes");
        if (timePref.equalsIgnoreCase("time_5_seconds")) {
            mTimePref = TimePref.TIME_5_SECONDS;
        } else if (timePref.equalsIgnoreCase("time_30_seconds")) {
            mTimePref = TimePref.TIME_30_SECONDS;
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
        String renderer = preferences.getString("rendererMode",
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
            if (mBackgroundGIF != null) {
                try {
                    mBackgroundGIF.update();
                } catch (TextureException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (mBackground != null
                    && mListFiles != null && mListFiles.size() > 0 && mUseFolder
                    && checkDate()) {
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
        try {
            mBackground.surfaceDestroyed();
            mTextureManager.taskRemove(mBackground.getTexture());
            mMaterialManager.taskRemove(mBackground.getMaterial());

            mBackgroundGIF.surfaceDestroyed();
            mTextureManager.taskRemove(mBackgroundGIF.getTexture());
            mMaterialManager.taskRemove(mBackgroundGIF.getMaterial());
        } catch (TextureException e) {
            e.printStackTrace();
        }
        super.onSurfaceDestroyed();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        synchronized (mLock) {
            if (mBackground != null) {
                initBackground();
                mTextureManager.replaceTexture(mBackground.getTexture());
                updateTime();
            }
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset,
                                 float xOffsetStep, float yOffsetStep, int xPixelOffset,
                                 int yPixelOffset) {
        if (mBackground != null) {
            mXOffset = xOffset;
            getBackground().offsetsChanged(xOffset);
        }
    }

    private float getTextureWidth() {
        if (mUseGIF) {
            return mBackgroundGIF.getWidth();
        } else {
            return mBackground.getWidthBitmap();
        }
    }

    private float getTextureHeight() {
        if (mUseGIF) {

            return mBackgroundGIF.getHeight();


        } else {
            return mBackground.getHeightBitmap();
        }
    }

    private IBackground getBackground() {
        if (mUseGIF) {
            return mBackgroundGIF;
        } else {
            return mBackground;
        }
    }

}
