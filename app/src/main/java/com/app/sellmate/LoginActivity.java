package com.app.sellmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editContactNumber, editNicNumber;
    private Button loginButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        editContactNumber = findViewById(R.id.edit_contact_number);
        editNicNumber = findViewById(R.id.edit_nic_number);
        loginButton = findViewById(R.id.login_button);
        databaseHelper = new DatabaseHelper(this);

        // Set login button click listener
        loginButton.setOnClickListener(v -> login());
    }

    private void login() {
        String contactNumber = editContactNumber.getText().toString().trim();
        String nicNumber = editNicNumber.getText().toString().trim();

        if (TextUtils.isEmpty(contactNumber) || TextUtils.isEmpty(nicNumber)) {
            showToast("Please enter both contact number and NIC number");
            return;
        }

        if (isAdminCredentials(contactNumber, nicNumber)) {
            loginAsAdmin();
        } else {
            loginAsSalesman(contactNumber, nicNumber);
        }
    }

    private boolean isAdminCredentials(String contactNumber, String nicNumber) {
        return "0000".equals(contactNumber) && "root".equals(nicNumber);
    }

    private void loginAsAdmin() {
        showToast("Admin login successful");
        navigateToHome(true, null);
    }

    private void loginAsSalesman(String contactNumber, String nicNumber) {
        try {
            Salesmen salesmen = databaseHelper.getSalesmenByCredentials(contactNumber, nicNumber);
            if (salesmen != null) {
                showToast("Login successful");
                navigateToHome(false, salesmen.getId());
            } else {
                showToast("Invalid credentials");
            }
        } catch (Exception e) {
            showToast("An error occurred: " + e.getMessage());
        }
    }

    private void navigateToHome(boolean isAdmin, String salesmenId) {
        Intent intent = new Intent(LoginActivity.this, Home.class);
        intent.putExtra("isAdmin", isAdmin);
        if (!isAdmin) {
            intent.putExtra("salesmen_id", salesmenId);
        }
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
