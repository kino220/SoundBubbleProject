package com.example.soundbubble;

import com.example.soundbubble.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class FullscreenActivity extends Activity implements RecognitionListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private SpeechRecognizer mSpeechRecognizer;
    private String LOGTAG = "SpeechRecognizerTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                //       RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString());

                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        getPackageName());
                mSpeechRecognizer.startListening(intent);
            }
        });

        TextView textView = new TextView(this);
        String str = "Hello World!";
        textView.setText(str);
        FrameLayout mainLayout = (FrameLayout)findViewById(R.id.main_layout);
        mainLayout.addView(textView);


        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(this);



        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    // 音声認識準備完了
    @Override
    public void onReadyForSpeech(Bundle params) {
        Toast.makeText(this, "音声認識準備完了", Toast.LENGTH_SHORT);
        //iv.setImageResource(R.drawable.ready);
    }

    // 音声入力開始
    @Override
    public void onBeginningOfSpeech() {
        Toast.makeText(this, "入力開始", Toast.LENGTH_SHORT);
        //iv.setImageResource(R.drawable.listen);
    }

    // 録音データのフィードバック用
    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.v(LOGTAG,"onBufferReceived");
    }

    // 入力音声のdBが変化した
    @Override
    public void onRmsChanged(float rmsdB) {
        Log.v(LOGTAG,"recieve : " + rmsdB + "dB");
    }

    // 音声入力終了
    @Override
    public void onEndOfSpeech() {
        Toast.makeText(this, "入力終了", Toast.LENGTH_SHORT);
        //iv.setImageResource(R.drawable.tb);
    }

    // ネットワークエラー又は、音声認識エラー
    @Override
    public void onError(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                // 音声データ保存失敗
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                // Android端末内のエラー(その他)
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                // 権限無し
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                // ネットワークエラー(その他)
                Log.e(LOGTAG, "network error");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                // ネットワークタイムアウトエラー
                Log.e(LOGTAG, "network timeout");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                // 音声認識結果無し
                Toast.makeText(this, "no match Text data", Toast.LENGTH_LONG);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                // RecognitionServiceへ要求出せず
                break;
            case SpeechRecognizer.ERROR_SERVER:
                // Server側からエラー通知
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                // 音声入力無し
                Toast.makeText(this, "no input?", Toast.LENGTH_LONG);
                break;
            default:
        }
    }

    // イベント発生時に呼び出される
    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.v(LOGTAG,"onEvent");
    }

    // 部分的な認識結果が得られる場合に呼び出される
    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.v(LOGTAG,"onPartialResults");
    }

    // 認識結果
    @Override
    public void onResults(Bundle results) {
        ArrayList recData = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        String getData = new String();
        for (Object s : recData) {
            getData += s + ",";
        }

        Toast.makeText(this, getData, Toast.LENGTH_SHORT).show();
    }
}
