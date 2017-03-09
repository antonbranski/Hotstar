package com.hotstar.player.webservice;

import com.hotstar.player.model.HotStarUserInfo;

import retrofit.client.Response;
import retrofit.http.*;

import java.util.ArrayList;

public interface HotStarApi
{
	@GET("/login/authenticate")
	HotStarUserInfo getUserInfo(
		@Query("username") String username,
		@Query("password") String password
	);
}
