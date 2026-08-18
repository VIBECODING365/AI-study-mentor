package com.bonanhtai.aistudymentor.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bonanhtai.aistudymentor.api.ApiCallback;
import com.bonanhtai.aistudymentor.api.UserAPI;
import com.bonanhtai.aistudymentor.model.EduLevelDTO;
import com.bonanhtai.aistudymentor.model.Token;
import com.bonanhtai.aistudymentor.model.UserDTO;
import com.bonanhtai.aistudymentor.repository.CallAPI;
import com.bonanhtai.aistudymentor.retrofit.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends AndroidViewModel {

    private final CallAPI repository;
    private final RetrofitService retrofitService;

    private final MutableLiveData<UserDTO> _user = new MutableLiveData<>();
    public LiveData<UserDTO> getUser() {
        return _user;
    }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() {
        return _error;
    }

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.repository = new CallAPI(application);
        this.retrofitService = new RetrofitService(application);
    }

    public void editUserProfile(String educationLevel) {
        _isLoading.setValue(true);
        EduLevelDTO eduLevelDTO = new EduLevelDTO(educationLevel);

        // Retrieve token from SharedPreferences
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString("jwt_token", null);

        if (jwtToken != null) {
            Token token = new Token();
            token.setAccessToken(jwtToken);
        }

        repository.editUserProfile(eduLevelDTO, new ApiCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO data) {
                _user.postValue(data);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(Throwable t) {
                _error.postValue(t.getMessage());
                _isLoading.postValue(false);
            }
        });
    }
}
