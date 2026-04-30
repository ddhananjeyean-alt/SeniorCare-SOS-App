            package com.example.seniorcaresos;

            import android.app.Service;
            import android.content.Intent;
            import android.os.Bundle;
            import android.os.IBinder;
            import android.speech.RecognitionListener;
            import android.speech.RecognizerIntent;
            import android.speech.SpeechRecognizer;
            import android.util.Log;

            import java.util.ArrayList;
            import java.util.Locale;

            public class VoiceSOSService extends Service {

                private SpeechRecognizer speechRecognizer;
                private Intent speechIntent;

                @Override
                public void onCreate() {
                    super.onCreate();

                    // Create SpeechRecognizer safely
                    if (SpeechRecognizer.isRecognitionAvailable(this)) {
                        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                        speechRecognizer.setRecognitionListener(listener);

                        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        speechIntent.putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        );
                        speechIntent.putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE,
                                Locale.getDefault()
                        );
                        speechIntent.putExtra(
                                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                                true
                        );

                        startListening();
                    }
                }

                private void startListening() {
                    try {
                        speechRecognizer.startListening(speechIntent);
                    } catch (Exception e) {
                        Log.e("VoiceSOS", "Error starting voice recognition", e);
                    }
                }

                private final RecognitionListener listener = new RecognitionListener() {

                    @Override
                    public void onResults(Bundle results) {
                        processResults(results);
                        startListening(); // keep listening
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                        processResults(partialResults);
                    }

                    private void processResults(Bundle bundle) {
                        if (bundle == null) return;

                        ArrayList<String> matches =
                                bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                        if (matches == null) return;

                        for (String text : matches) {
                            text = text.toLowerCase();

                            // 🔊 KEYWORDS (SAFE – NO EXISTING CODE TOUCHED)
                            if (text.contains("help")
                                    || text.contains("sos")
                                    || text.contains("emergency")) {

                                triggerSOS();
                                break;
                            }
                        }
                    }

                    @Override public void onReadyForSpeech(Bundle params) {}
                    @Override public void onBeginningOfSpeech() {}
                    @Override public void onRmsChanged(float rmsdB) {}
                    @Override public void onBufferReceived(byte[] buffer) {}
                    @Override public void onEndOfSpeech() {}
                    @Override public void onError(int error) {
                        startListening(); // restart on error (crash-safe)
                    }
                    @Override public void onEvent(int eventType, Bundle params) {}
                };

                private void triggerSOS() {
                    // 🔥 SEND INTENT TO MAIN ACTIVITY (NO CODE CHANGE THERE)
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setAction(MainActivity.ACTION_SOS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

                @Override
                public void onDestroy() {
                    super.onDestroy();
                    if (speechRecognizer != null) {
                        speechRecognizer.destroy();
                    }
                }

                @Override
                public IBinder onBind(Intent intent) {
                    return null;
                }
            }
