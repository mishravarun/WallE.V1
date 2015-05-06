package com.varunmishra.wallev1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;


public class MainActivity extends Activity implements edu.cmu.pocketsphinx.RecognitionListener  {

    private UsbManager usbManager;
    public TextView txtSpeechInput;
    private UsbSerialDriver device;
    public TextView txtStatus,txtOutput;
    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "okay jarvis";
    TextToSpeech ttobj;
    private String url1 = "http://api.openweathermap.org/data/2.5/weather?q=";
    private HandleJSON obj;
    public ImageView imgSmile;
    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    int countforText = 0;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    int commandSaid= 0;
    public final static String TAG = "Arduandro";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        txtStatus=(TextView)findViewById(R.id.txtStatus);
        imgSmile=(ImageView)findViewById(R.id.imageView);
        getActionBar().hide();
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        txtOutput=(TextView)findViewById(R.id.txtSpeechOutput);
        txtSpeechInput.setText("Preparing the recognizer");
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.image_0);
        bmp= Bitmap.createScaledBitmap(bmp, 700, 700, true);
        imgSmile.setImageBitmap(bmp);
        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.UK);
                            Toast.makeText(getApplicationContext(), "Initialized", Toast.LENGTH_LONG).show();
                        }
                    }
                });



        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    txtSpeechInput.setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        recognizer.cancel();
        recognizer.shutdown();
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (device != null) {
            try {
                device.close();
            } catch (IOException e) {
            }
            device = null;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        device = UsbSerialProber.acquire(usbManager);
        if (device == null) {
            txtStatus.setTextColor(Color.RED);
            txtStatus.setText("Disconnected");
            Log.d(TAG, "No USB serial device connected.");
        } else {
            try {
                device.open();
                txtStatus.setTextColor(Color.GREEN);
                txtStatus.setText("Connected");

                device.setBaudRate(115200);
                if(commandSaid==1) {
                    sendToArduino("a");
                    ttobj.speak("Sending Command Start",TextToSpeech.QUEUE_FLUSH,null);
                    txtOutput.setText("Sending Command Start");

                }

                if(commandSaid==2) {
                    sendToArduino("b");
                    ttobj.speak("Sending Command Stop",TextToSpeech.QUEUE_FLUSH,null);
                    txtOutput.setText("Sending Command Stop");
                }
                if(commandSaid==11) {
                    sendToArduino("a");
                    ttobj.speak("Going to A",TextToSpeech.QUEUE_FLUSH,null);
                    txtOutput.setText("Going to A");
                }
                if(commandSaid==12) {
                    sendToArduino("b");
                    ttobj.speak("Going to B",TextToSpeech.QUEUE_FLUSH,null);
                    txtOutput.setText("Going to B");
                }
                if(commandSaid==13) {
                    sendToArduino("c");
                    ttobj.speak("Going to C",TextToSpeech.QUEUE_FLUSH,null);
                    txtOutput.setText("Going to C");
                }
                if(commandSaid==14) {
                    sendToArduino("d");
                    ttobj.speak("Going to D",TextToSpeech.QUEUE_FLUSH,null);
                    txtOutput.setText("Going to D");
                }
                if(commandSaid==15) {
                    sendToArduino("h");
                    ttobj.speak("Going to Home",TextToSpeech.QUEUE_FLUSH,null);
                    txtOutput.setText("Going to Home");
                }

                commandSaid=0;
            } catch (IOException err) {
                Log.e(TAG, "Error setting up USB device: " + err.getMessage(), err);
                try {
                    device.close();
                } catch (IOException err2) {
                }
                device = null;
                return;
            }
        }
    }
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.image_1);
            bmp= Bitmap.createScaledBitmap(bmp, 700, 700, true);
            imgSmile.setImageBitmap(bmp);
            txtSpeechInput.setText("Please Wait");
            Toast.makeText(getApplicationContext(), "Action", Toast.LENGTH_LONG).show();
            promptSpeechInput();
        }
    }

    private void promptSpeechInput() {
        recognizer.cancel();
        recognizer.shutdown();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak now");
        try {
            startActivityForResult(intent, 100);
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try{
                        finishActivity(100);
                    }
                    catch(Exception e){
                    }
                }
            };
            handler.postDelayed(runnable, 10000);


        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Not Supported",
                    Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String speech = result.get(0);
                    speech=speech.toLowerCase();
                    Toast.makeText(getApplicationContext(),speech,Toast.LENGTH_LONG).show();
                    if(speech.equalsIgnoreCase("Start")) {
                        commandSaid=1;
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.image_2);
                        bmp= Bitmap.createScaledBitmap(bmp, 700, 700, true);
                        imgSmile.setImageBitmap(bmp);

                    }
                    if(speech.contains("goto")||speech.contains("go to")) {
                        if(speech.contains("1")||speech.toLowerCase().contains("one"))
                            commandSaid=11;
                        else if(speech.contains("2")||speech.toLowerCase().contains("two"))
                            commandSaid=12;
                        else if(speech.contains("3")||speech.toLowerCase().contains("three"))
                            commandSaid=13;
                        else if(speech.contains("4")||speech.toLowerCase().contains("four"))
                            commandSaid=14;
                        else if(speech.contains("home")||speech.toLowerCase().contains("center"))
                            commandSaid=15;
                        else commandSaid=0;

                        if(commandSaid==0){

                                ttobj.speak("Command Not Found",TextToSpeech.QUEUE_FLUSH,null);
                                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.image_3);
                                bmp= Bitmap.createScaledBitmap(bmp, 700, 700, true);
                                imgSmile.setImageBitmap(bmp);

                        }else {
                            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.image_2);
                            bmp = Bitmap.createScaledBitmap(bmp, 700, 700, true);
                            imgSmile.setImageBitmap(bmp);
                        }
                    }

                    else if(speech.equalsIgnoreCase("stop")) {
                        commandSaid=2;
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.image_2);
                        bmp= Bitmap.createScaledBitmap(bmp, 700, 700, true);
                        imgSmile.setImageBitmap(bmp);
                    }else if(speech.contains("weather")){
                        fetchWeather();
                        commandSaid=-1;
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.image_2);
                        bmp= Bitmap.createScaledBitmap(bmp, 700, 700, true);
                        imgSmile.setImageBitmap(bmp);
                    }else {
                        commandSaid=0;
                        ttobj.speak("Command Not Found",TextToSpeech.QUEUE_FLUSH,null);
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.image_3);
                        bmp= Bitmap.createScaledBitmap(bmp, 700, 700, true);
                        imgSmile.setImageBitmap(bmp);
                    }

                //    Toast.makeText(getApplicationContext(),result.get(0).toString(),Toast.LENGTH_LONG).show();
                }
                break;

            }


        }
        refreshView();

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;


            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    txtSpeechInput.setText("Failed to init recognizer " + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
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

    private void switchSearch(String searchName) {
        recognizer.stop();
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);
        if (countforText == 0) {
            String caption = getResources().getString(captions.get(searchName));
            txtSpeechInput.setText(caption);
            countforText++;
        }
    }

    private void setupRecognizer(File assetsDir) throws IOException {

        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir)
                .setKeywordThreshold(1e-45f)
                .setBoolean("-allphone_ci", true)
                .getRecognizer();
        recognizer.addListener(this);

        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);


    }
    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }
    private void sendToArduino(String data){
        byte[] dataToSend = data.getBytes();
        if (device != null){
            try{
                device.write(dataToSend, 500);
                Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show();
                refreshView();
            }
            catch (IOException e){
                Log.e(TAG, "couldn't write bytes to serial device");
            }
        }
    }
    public void fetchWeather(){

        String url = "Bangalore";
        String finalUrl = url1 + url;
        obj = new HandleJSON(finalUrl);
        obj.fetchJSON(MainActivity.this);

        while(obj.parsingComplete);
        int temp = (int)Double.parseDouble(obj.getTemperature())-273;
        txtOutput.setText("The weather today is "+temp + " degree celcius with "+obj.getHumidity()+ " percent humidity.");
        ttobj.speak("The weather today is "+temp + " degree celcius with "+obj.getHumidity()+ " percent humidity.",TextToSpeech.QUEUE_FLUSH,null);

        refreshView();

    }
    public void refreshView(){
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try{
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.image_0);
                    bmp= Bitmap.createScaledBitmap(bmp, 700, 700, true);
                    imgSmile.setImageBitmap(bmp);
                    txtOutput.setText("");
                    txtSpeechInput.setText("Ready");
                }
                catch(Exception e){
                }
            }
        };
        handler.postDelayed(runnable, 6000);

    }
}
