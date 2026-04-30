package com.example.seniorcaresos;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SaveContactsActivity extends AppCompatActivity {

    EditText etName1, etPhone1;
    EditText etName2, etPhone2;
    EditText etName3, etPhone3;

    EditText etName4, etPhone4;
    EditText etName5, etPhone5;

    Button btnSave;

    // ===== NEW (UNLOCK FEATURE) =====
    LinearLayout layoutAdvancedContacts;
    TextView txtUnlock;
    // ===============================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_contacts);

        etName1 = findViewById(R.id.etName1);
        etPhone1 = findViewById(R.id.etPhone1);
        etName2 = findViewById(R.id.etName2);
        etPhone2 = findViewById(R.id.etPhone2);
        etName3 = findViewById(R.id.etName3);
        etPhone3 = findViewById(R.id.etPhone3);

        etName4 = findViewById(R.id.etName4);
        etPhone4 = findViewById(R.id.etPhone4);
        etName5 = findViewById(R.id.etName5);
        etPhone5 = findViewById(R.id.etPhone5);

        btnSave = findViewById(R.id.btnSave);

        // ===== UNLOCK VIEWS =====
        layoutAdvancedContacts = findViewById(R.id.layoutAdvancedContacts);
        txtUnlock = findViewById(R.id.txtUnlock);

        txtUnlock.setOnClickListener(v -> {
            layoutAdvancedContacts.setVisibility(View.VISIBLE);
            txtUnlock.setVisibility(View.GONE);
        });
        // ========================

        loadSavedContacts();

        btnSave.setOnClickListener(v -> saveContacts());
    }

    private void saveContacts() {

        SharedPreferences prefs =
                getSharedPreferences("EMERGENCY_CONTACTS", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("NAME_1", etName1.getText().toString().trim());
        editor.putString("PHONE_1", etPhone1.getText().toString().trim());

        editor.putString("NAME_2", etName2.getText().toString().trim());
        editor.putString("PHONE_2", etPhone2.getText().toString().trim());

        editor.putString("NAME_3", etName3.getText().toString().trim());
        editor.putString("PHONE_3", etPhone3.getText().toString().trim());

        editor.putString("NAME_4", etName4.getText().toString().trim());
        editor.putString("PHONE_4", etPhone4.getText().toString().trim());

        editor.putString("NAME_5", etName5.getText().toString().trim());
        editor.putString("PHONE_5", etPhone5.getText().toString().trim());

        editor.apply();

        Toast.makeText(this, "Emergency contacts saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadSavedContacts() {

        SharedPreferences prefs =
                getSharedPreferences("EMERGENCY_CONTACTS", MODE_PRIVATE);

        etName1.setText(prefs.getString("NAME_1", ""));
        etPhone1.setText(prefs.getString("PHONE_1", ""));

        etName2.setText(prefs.getString("NAME_2", ""));
        etPhone2.setText(prefs.getString("PHONE_2", ""));

        etName3.setText(prefs.getString("NAME_3", ""));
        etPhone3.setText(prefs.getString("PHONE_3", ""));

        etName4.setText(prefs.getString("NAME_4", ""));
        etPhone4.setText(prefs.getString("PHONE_4", ""));

        etName5.setText(prefs.getString("NAME_5", ""));
        etPhone5.setText(prefs.getString("PHONE_5", ""));

        // ===== AUTO-UNLOCK IF DATA EXISTS =====
        if (!etPhone4.getText().toString().isEmpty()
                || !etPhone5.getText().toString().isEmpty()) {

            layoutAdvancedContacts.setVisibility(View.VISIBLE);
            txtUnlock.setVisibility(View.GONE);
        }
        // =====================================
    }
}
