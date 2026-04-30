package com.example.seniorcaresos;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_SOS =
            "com.example.seniorcaresos.ACTION_SOS";

    private static final int REQ_PERMISSION = 100;
    private static final int REQ_MIC_PERMISSION = 200;
    private static final int REQ_CALL_PERMISSION = 300;

    Button btnSOS, btnEditContacts;
    FusedLocationProviderClient locationClient;

    MediaPlayer mediaPlayer;
    Vibrator vibrator;
    boolean isSOSActive = false;

    Animation pulseAnimation;
    View waveView;
    Animation waveAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSOS = findViewById(R.id.btnSOS);
        btnEditContacts = findViewById(R.id.btnEditContacts);

        waveView = findViewById(R.id.waveView);
        waveAnimation = AnimationUtils.loadAnimation(this, R.anim.wave_expand);

        locationClient = LocationServices.getFusedLocationProviderClient(this);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        pulseAnimation = new ScaleAnimation(
                1.0f, 1.15f,
                1.0f, 1.15f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        pulseAnimation.setDuration(600);
        pulseAnimation.setRepeatMode(Animation.REVERSE);
        pulseAnimation.setRepeatCount(Animation.INFINITE);

        btnEditContacts.setOnClickListener(v ->
                startActivity(new Intent(this, SaveContactsActivity.class))
        );

        btnSOS.setOnClickListener(v -> {
            if (!isSOSActive) {
                checkPermissionsAndSendSOS();
            } else {
                stopSOS();
            }
        });

        checkLocationEnabled();
        checkMicPermission();

        if (getIntent() != null &&
                ACTION_SOS.equals(getIntent().getAction())) {
            checkPermissionsAndSendSOS();
        }
    }

    // ================= MIC =================
    private void checkMicPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQ_MIC_PERMISSION
            );
        } else {
            startService(new Intent(this, VoiceSOSService.class));
        }
    }

    // ================= LOCATION =================
    private void checkLocationEnabled() {
        LocationManager lm =
                (LocationManager) getSystemService(LOCATION_SERVICE);

        if (lm != null && !lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(this)
                    .setTitle("Location Required")
                    .setMessage("Please enable location for emergency sharing.")
                    .setPositiveButton("Turn ON", (d, w) ->
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .show();
        }
    }

    // ================= PERMISSIONS =================
    private void checkPermissionsAndSendSOS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQ_PERMISSION
            );
        } else {
            startSOS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSOS();
        }

        if (requestCode == REQ_MIC_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, VoiceSOSService.class));
        }

        if (requestCode == REQ_CALL_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callPrimaryContact();
        }
    }

    // ================= START SOS =================
    private void startSOS() {
        isSOSActive = true;
        btnSOS.setText("STOP");

        mediaPlayer = MediaPlayer.create(this, R.raw.sos_alarm);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (vibrator != null) {
            vibrator.vibrate(new long[]{0, 1000, 1000}, 0);
        }

        if (waveView != null) {
            waveView.setVisibility(View.VISIBLE);
            waveView.startAnimation(waveAnimation);
        }

        btnSOS.setBackgroundResource(R.drawable.bg_sos_green_wave);
        btnSOS.startAnimation(pulseAnimation);

        sendSOSWithLocation();
        checkCallPermissionAndCall();
    }

    // ================= SINGLE CALL =================
    private void checkCallPermissionAndCall() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQ_CALL_PERMISSION
            );
        } else {
            callPrimaryContact();
        }
    }

    private void callPrimaryContact() {
        SharedPreferences prefs =
                getSharedPreferences("EMERGENCY_CONTACTS", MODE_PRIVATE);

        String phone = prefs.getString("PHONE_1", "");
        if (phone.isEmpty()) return;

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    // ================= STOP SOS =================
    private void stopSOS() {
        isSOSActive = false;
        btnSOS.setText("SOS");

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (vibrator != null) vibrator.cancel();

        if (waveView != null) {
            waveView.clearAnimation();
            waveView.setVisibility(View.GONE);
        }

        btnSOS.setBackgroundResource(R.drawable.bg_sos_neon_circle);
    }

    // ================= SMS + LOCATION =================
    private void sendSOSWithLocation() {
        sendSMS("🚨 SOS ALERT 🚨 Fetching location...");
        requestLiveLocation();
    }

    private void requestLiveLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        LocationRequest request = LocationRequest.create();
        request.setNumUpdates(1);

        locationClient.requestLocationUpdates(
                request,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult result) {
                        Location loc = result.getLastLocation();
                        if (loc != null) {
                            sendSMS("📍 https://maps.google.com/?q=" +
                                    loc.getLatitude() + "," + loc.getLongitude());
                        }
                        locationClient.removeLocationUpdates(this);
                    }
                },
                getMainLooper()
        );
    }

    private void sendSMS(String msg) {
        SharedPreferences prefs =
                getSharedPreferences("EMERGENCY_CONTACTS", MODE_PRIVATE);
        SmsManager sms = SmsManager.getDefault();

        for (int i = 1; i <= 5; i++) {
            String phone = prefs.getString("PHONE_" + i, "");
            if (!phone.isEmpty()) {
                sms.sendTextMessage(phone, null, msg, null, null);
            }
        }
    }
}
