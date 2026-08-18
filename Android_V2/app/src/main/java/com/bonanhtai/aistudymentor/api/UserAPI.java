package com.bonanhtai.aistudymentor.api;

import com.bonanhtai.aistudymentor.model.EduLevelDTO;
import com.bonanhtai.aistudymentor.model.UserDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UserAPI {
    @GET("api/user/profile")
    Call<UserDTO> getUserProfile();
    @POST("api/user/profile/edit")
    Call<UserDTO> editUserProfile(EduLevelDTO eduLevelDTO);
}
