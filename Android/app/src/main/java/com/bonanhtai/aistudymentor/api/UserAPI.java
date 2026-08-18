package com.bonanhtai.aistudymentor.api;

import com.bonanhtai.aistudymentor.model.EduLevelDTO;
import com.bonanhtai.aistudymentor.model.UserDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

public interface UserAPI {
    @PUT("api/user/profile")
    Call<UserDTO> editUserProfile(@Body EduLevelDTO eduLevelDTO);
}
