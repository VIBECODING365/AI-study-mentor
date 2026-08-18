package com.bonanhtai.aistudymentor.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bonanhtai.aistudymentor.api.ApiCallback;
import com.bonanhtai.aistudymentor.model.EduLevelDTO;
import com.bonanhtai.aistudymentor.model.UserDTO;
import com.bonanhtai.aistudymentor.repository.CallAPI;

public class UserViewModel extends AndroidViewModel {

    private final CallAPI repository;

    private final MutableLiveData<UserDTO> _userProfile = new MutableLiveData<>();
    public LiveData<UserDTO> getUserProfile() {
        return _userProfile;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    public UserViewModel(@NonNull Application application) {
        super(application);
        this.repository = new CallAPI(application);
    }

    public void fetchUserProfile() {
        _isLoading.setValue(true);
        repository.getUserProfile(new ApiCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO userDTO) {
                _userProfile.postValue(userDTO);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(Throwable t) {
                _errorMessage.postValue(t.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    public void editUserProfile(EduLevelDTO eduLevelDTO, ApiCallback<UserDTO> callback) {
        _isLoading.setValue(true);
        repository.editUserProfile(eduLevelDTO, new ApiCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO userDTO) {
                _userProfile.postValue(userDTO);
                _isLoading.postValue(false);
                callback.onSuccess(userDTO);
            }

            @Override
            public void onError(Throwable t) {
                _errorMessage.postValue(t.getMessage());
                _isLoading.postValue(false);
                callback.onError(t);
            }
        });
    }
}
