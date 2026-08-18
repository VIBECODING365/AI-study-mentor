package com.bonanhtai.aistudymentor.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bonanhtai.aistudymentor.R;
import com.bonanhtai.aistudymentor.api.ApiCallback;
import com.bonanhtai.aistudymentor.model.EduLevelDTO;
import com.bonanhtai.aistudymentor.model.UserDTO;
import com.bonanhtai.aistudymentor.viewmodel.UserViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private MaterialButton btnLogout;
    private BottomNavigationView bottomNavigation;
    private TextView tvUserName;
    private TextView tvUserMajor;
    private LinearLayout layoutEducationLevel;
    private UserViewModel userViewModel;

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

        initViews();
        setupViewModel();
        setupListeners();
    }

    private void initViews() {
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigationProfile);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserMajor = findViewById(R.id.tvUserMajor);
        layoutEducationLevel = findViewById(R.id.layoutEducationLevel);
        
        // Pre-select Profile tab
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    private void setupViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        
        userViewModel.getUserProfile().observe(this, this::updateUI);
        userViewModel.getErrorMessage().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
        
        // Fetch user profile
        userViewModel.fetchUserProfile();
    }

    private void updateUI(UserDTO userDTO) {
        if (userDTO != null) {
            tvUserName.setText(userDTO.getEmail() != null ? userDTO.getEmail() : "User");
            tvUserMajor.setText(userDTO.getEducationLevel() != null ? userDTO.getEducationLevel() : "Not specified");
        }
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> handleLogout());
        
        layoutEducationLevel.setOnClickListener(v -> showEducationLevelDialog());

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
                // Navigate to Leaderboard if it exists
                // startActivity(new Intent(this, LeaderboardActivity.class));
                // finish();
                return true;
            }
            return true;
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

    private void showEducationLevelDialog() {
        String[] educationLevels = {"Lớp 6", "Lớp 7", "Lớp 8", "Lớp 9", "Lớp 10", "Lớp 11", "Lớp 12"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Education Level");
        builder.setItems(educationLevels, (dialog, which) -> {
            String selectedLevel = educationLevels[which];
            updateEducationLevel(selectedLevel);
        });
        builder.show();
    }

    private void updateEducationLevel(String educationLevel) {
        EduLevelDTO eduLevelDTO = new EduLevelDTO();
        eduLevelDTO.setEduLevel(educationLevel);
        
        userViewModel.editUserProfile(eduLevelDTO, new ApiCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO userDTO) {
                Toast.makeText(ProfileActivity.this, "Education level updated successfully", Toast.LENGTH_SHORT).show();
                // UI will be updated automatically via LiveData observer
            }

            @Override
            public void onError(Throwable t) {
                Toast.makeText(ProfileActivity.this, "Failed to update education level: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
