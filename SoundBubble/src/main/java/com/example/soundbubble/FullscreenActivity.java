package com.example.soundbubble;

import com.example.soundbubble.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;


@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class FullscreenActivity extends Activity implements RecognitionListener {


    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    private String LOGTAG = "SpeechRecognizerTest";
    private FrameLayout mainLayout;
    private WindowManager wm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fullscreen);

        final View contentView = findViewById(R.id.fullscreen_content);
        wm = getWindowManager();
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);


        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
                */
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                //       RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString());

                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        getPackageName());

                mSpeechRecognizer.startListening(intent);
            }
        });

//        TextView textView = new TextView(this);
//        String str = "Hello World!";
//        textView.setText(str);
        mainLayout = (FrameLayout)findViewById(R.id.main_layout);
        //mainLayout.addView(textView);


        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }

    // 音声認識準備完了
    @Override
    public void onReadyForSpeech(Bundle params) {
        //Toast.makeText(this, "音声認識準備完了", Toast.LENGTH_SHORT).show();
        //iv.setImageResource(R.drawable.ready);
    }

    // 音声入力開始
    @Override
    public void onBeginningOfSpeech() {
        //Toast.makeText(this, "入力開始", Toast.LENGTH_SHORT).show();
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
        //Toast.makeText(this, "入力終了", Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(this, "no match Text data", Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                // RecognitionServiceへ要求出せず
                break;
            case SpeechRecognizer.ERROR_SERVER:
                // Server側からエラー通知
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                // 音声入力無し
                //Toast.makeText(this, "no input?", Toast.LENGTH_LONG).show();
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
        TextView textView = new TextView(this);

        for (Object s : recData) {
            getData += s + ",";
        }
        if(!recData.isEmpty()){
            //LinearLayout.LayoutParams lp  = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            LinearLayout ll = new LinearLayout(this);

            //ディスプレイの情報を入手
            DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;

            //テキストビューの座標をランダムに決定
            Random rnd = new Random();
            float tx = (float)rnd.nextInt(width-100)+100;
            float ty = (float)rnd.nextInt(height-100)+100;

            String str =  (String)recData.get(0);
            textView.setText(str);
            textView.setX(tx);
            textView.setY(ty);
            textView.setBackgroundResource(R.drawable.text_border);
            ll.addView(textView);
            mainLayout.addView(ll);
        }

        Toast.makeText(this, getData, Toast.LENGTH_SHORT).show();
    }
}
