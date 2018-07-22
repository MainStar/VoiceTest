package com.example.mainstar.voicetest;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final String KWS_SEARCH = "wakeup";
    private static final String MENU_SEARCH = "menu";
    private static final String KEYPHRASE = "ok";
    private SpeechRecognizer recognizer;
    private AudioRecord record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recognizerSetUp();
    }

    @SuppressLint("StaticFieldLeak")
    private void recognizerSetUp(){
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setUpRecognizer(assetDir);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null){
                    System.out.println(result.getMessage());
                }else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    private void setUpRecognizer(File assertDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup().setAcousticModel(new File(assertDir, "en-us-ptm"))
                .setDictionary(new File(assertDir, "cmudict-en-us.dict")).getRecognizer();
        recognizer.addListener(this);
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        File menuGrammar = new File(assertDir, "mymenu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
    }

    private void switchSearch(String searchName){
        recognizer.stop();
        if (searchName.equals(KWS_SEARCH)){
            recognizer.startListening(searchName);
        }else {
            recognizer.startListening(searchName, 10000);
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)){
            switchSearch(MENU_SEARCH);
        }else if (text.equals("hello")){
            System.out.println("Hello to you too!");
        }else if (text.equals("good morning")){
            System.out.println("Good morning to you too!");
        }else {
            System.out.println(hypothesis.getHypstr());
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null){
            System.out.println(hypothesis.getHypstr());
        }
    }

    @Override
    public void onError(Exception e) {
        System.out.println(e.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recognizer != null){
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("On Destroy");
    }
}
