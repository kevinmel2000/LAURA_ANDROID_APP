package com.laurensius_dede_suhardiman.laura;

import android.app.ProgressDialog;
import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.laurensius_dede_suhardiman.laura.AppController.AppController;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Commandment extends AppCompatActivity implements RecognitionListener, TextToSpeech.OnInitListener{

//    private ProgressBar progressBar;
    private RippleBackground rbRippleFx;
    private ImageView ivMic;
    private TextView tvReceivedComamnd;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "~~>LAURA";
    private TextToSpeech tts;
    private String url;

    private boolean isListening = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commandment);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        url = getResources().getString(R.string.url_control);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        rbRippleFx =(RippleBackground)findViewById(R.id.rb_ripplefx);
        ivMic =(ImageView)findViewById(R.id.iv_mic);
        tvReceivedComamnd = (TextView)findViewById(R.id.tv_receivedcommand);
        ivMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isListening){
                    stopListening();
                    shutdownCommander();
                }else{
                    startCommander();
                    startListening();
                }
            }
        });
    }

    private void startCommander(){
        speech = SpeechRecognizer.createSpeechRecognizer(Commandment.this);
        speech.setRecognitionListener(Commandment.this);
        tts = new TextToSpeech(Commandment.this,Commandment.this);
    }

    private void startListening(){
        ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic));
        speech.startListening(recognizerIntent);
        rbRippleFx.startRippleAnimation();
        isListening = true;
    }

    private void stopListening(){
        ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_off));
        speech.stopListening();
        rbRippleFx.stopRippleAnimation();
        isListening = false;
    }

    private void shutdownCommander(){
        stopListening();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speech != null) {
            speech.destroy();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shutdownCommander();
        Log.i(LOG_TAG, "destroy");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        stopListening();
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        if(errorMessage.equals("No match")){
            startListening();
        }else{
            stopListening();
            shutdownCommander();
            //tvReceivedComamnd.setText(errorMessage);
            Log.d(LOG_TAG,errorMessage);
        }

    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
//        for (String result : matches) text += result + "\n";
        text = matches.get(0);
        //tvReceivedComamnd.setText(text);
        //speakOut(text);
        matchingInstruction(text.toLowerCase());
        startListening();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
//        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    //tts
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("id","ID"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(LOG_TAG, "This Language is not supported");
            }else {
                speakOut(tvReceivedComamnd.getText().toString());
            }
        } else {
            Log.e(LOG_TAG, "Initilization Failed!");
        }
    }

    private void speakOut(String text) {
        //String text = tvReceivedComamnd.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        while(tts.isSpeaking()){
            Log.d(LOG_TAG,"Speaking . . .");
        }
    }

    private void matchingInstruction(String text){
        String[] array_instruction = getResources().getStringArray(R.array.instructions);
        String[] array_response = getResources().getStringArray(R.array.responses);
        for(int x=0;x<array_instruction.length;x++){
            if(text.equals(array_instruction[x].toLowerCase())){
                tvReceivedComamnd.setText(array_response[x]);
                speakOut(tvReceivedComamnd.getText().toString());
                switch (x){
                    case 1 :
                        stopListening();
                        shutdownCommander();
                        break;
                    case 2 :
                        stopListening();
                        shutdownCommander();
                        finish();
                        break;
                    case 4 :
                        doInstruction("1","1");
                        break;
                    case 5 :
                        doInstruction("1","0");
                        break;
                    case 6 :
                        doInstruction("2","1");
                        break;
                    case 7 :
                        doInstruction("2","0");
                        break;
                    case 8 :
                        doInstruction("1","1");
                        doInstruction("2","1");
                        break;
                    case 9 :
                        doInstruction("1","0");
                        doInstruction("2","0");
                        break;
                    default:
                        break;
                }
            }
            Log.d(LOG_TAG,"ON MATCHING");
        }
        tvReceivedComamnd.setText("");
    }


    private void doInstruction(String id,String state) {
        String tag_login = "request_login";
        //final ProgressDialog pDialog = new ProgressDialog(this);
        //pDialog.setMessage("Loading . . . ");
        //pDialog.show();
        Log.d(LOG_TAG,"Loading send instruction");
        final Map<String,String> params = new HashMap<String,String>();
        params.put("id", id);
        params.put("state", state);
        JSONObject parameter = new JSONObject(params);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameter,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            Toast.makeText(Commandment.this,"Success",Toast.LENGTH_SHORT).show();
                            String data = response.getString("data");
                            String message = response.getString("message");
                            Toast.makeText(Commandment.this,data,Toast.LENGTH_SHORT).show();
                            Toast.makeText(Commandment.this,message,Toast.LENGTH_SHORT).show();
                        }catch (JSONException e){
                        }
                        Log.d(LOG_TAG,"Instruction sent");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
            //            pDialog.dismiss();
                        Toast.makeText(Commandment.this,"Error",Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG,"Instruction error");
                    }
                });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_login);
    }

}
