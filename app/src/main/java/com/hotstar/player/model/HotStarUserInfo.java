package com.hotstar.player.model;

import com.google.gson.annotations.SerializedName;

/**
 * Wrapper since BWF API returns object wrapped around access_token
 *
 */

public class HotStarUserInfo extends HotStarApiResponse
{
	@SerializedName("mail_id")
	public String mailId;
	@SerializedName("gender")
	public String gender;
	@SerializedName("user_name")
	public String userName;
	@SerializedName("dob")
	public String birthday;
	@SerializedName("telephone_number")
	public String telephone;
	@SerializedName("authentication_status")
	public String authStatus;
	@SerializedName("last_name")
	public String lastName;
	@SerializedName("location")
	public String location;
	@SerializedName("first_name")
	public String firstName;
	@SerializedName("age")
	public int age;
}
