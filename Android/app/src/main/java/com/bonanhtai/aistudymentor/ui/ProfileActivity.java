package com.bonanhtai.aistudymentor.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bonanhtai.aistudymentor.R;
import com.bonanhtai.aistudymentor.viewmodel.ProfileViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {

    private MaterialButton btnLogout;
    private BottomNavigationView bottomNavigation;
    private ProfileViewModel profileViewModel;
    private AutoCompleteTextView actvEducationLevel;
    private TextInputLayout tilEducationLevel;
    private MaterialButton btnSaveEducationLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initViews();
        setupEducationLevelDropdown();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigationProfile);
        actvEducationLevel = findViewById(R.id.actvEducationLevel);
        tilEducationLevel = findViewById(R.id.tilEducationLevel);
        btnSaveEducationLevel = findViewById(R.id.btnSaveEducationLevel);

        // Pre-select Profile tab
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> handleLogout());

        btnSaveEducationLevel.setOnClickListener(v -> {
            String selectedLevel = actvEducationLevel.getText().toString();
            if (!selectedLevel.isEmpty()) {
                profileViewModel.editUserProfile(selectedLevel);
            } else {
                Toast.makeText(this, "Please select an education level", Toast.LENGTH_SHORT).show();
            }
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_practice) {
                startActivity(new Intent(this, PracticeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_leaderboard) {
                // TODO: Navigate to LeaderboardActivity when implemented
                Toast.makeText(this, "Leaderboard coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                // Already on Profile, do nothing
                return true;
            }
            return true;
        });
    }

    private void setupEducationLevelDropdown() {
        String[] educationLevels = new String[]{
            "Elementary School",
            "Middle School",
            "High School",
            "Undergraduate",
            "Graduate",
            "PhD"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            educationLevels
        );

        actvEducationLevel.setAdapter(adapter);

        // Load current education level from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        String currentLevel = sharedPreferences.getString("education_level", "");
        if (!currentLevel.isEmpty()) {
            actvEducationLevel.setText(currentLevel, false);
        }
    }

    private void observeViewModel() {
        profileViewModel.getUser().observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Education level updated successfully!", Toast.LENGTH_SHORT).show();
                // Save to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                sharedPreferences.edit().putString("education_level", user.getEducationLevel()).apply();
            }
        });

        profileViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.isLoading().observe(this, isLoading -> {
            btnSaveEducationLevel.setEnabled(!isLoading);
            btnSaveEducationLevel.setText(isLoading ? "Saving..." : "Save Education Level");
        });
    }

    private void handleLogout() {
        // Clear SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        sharedPreferences.edit().remove("jwt_token").apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to Login via MainActivity logic
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
