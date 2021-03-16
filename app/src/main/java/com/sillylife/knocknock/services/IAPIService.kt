package com.sillylife.knocknock.services

import com.sillylife.knocknock.constants.NetworkConstants
import com.sillylife.knocknock.models.responses.GenericResponse
import com.sillylife.knocknock.models.responses.HomeDataResponse
import com.sillylife.knocknock.models.responses.UserResponse
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface IAPIService {

    @GET("${NetworkConstants.V1}/users/me/")
    fun getMe(@QueryMap queryMap: Map<String, String>): Observable<Response<UserResponse>>

    @FormUrlEncoded
    @POST("${NetworkConstants.V1}/users/register-fcm/")
    fun registerFCM(
            @Field("app_name") appName: String,
            @Field("os_type") osType: String,
            @Field("app_instance_id") appInstanceId: String,
            @Field("app_build_number") appBuildNumber: Int,
            @Field("installed_version") installedVersion: String,
            @Field("fcm_token") fcmToken: String
    ): Observable<Response<GenericResponse>>

    @FormUrlEncoded
    @POST("${NetworkConstants.V1}/users/unregister-fcm/")
    fun unregisterFCM(@Field("fcm_token") fcmToken: String): Observable<Response<String>>

    @GET("${NetworkConstants.V1}/home/partner/")
    fun getHomeData(@QueryMap queryMap: Map<String, String>): Observable<Response<HomeDataResponse>>

    @Multipart
    @POST("${NetworkConstants.V1}/users/me/update/")
    fun updateProfile(
            @Part("name") name: RequestBody?,
            @Part avatar: MultipartBody.Part?,
            @Part("phone") phone: RequestBody?,
            @Part("email") email: RequestBody?
    ): Observable<Response<UserResponse>>

    @FormUrlEncoded
    @POST("${NetworkConstants.V1}/knocknock/ring-bell/")
    fun ringBell(
            @Field("profile_id") profileId: Int,
    ): Observable<Response<GenericResponse>>


    @FormUrlEncoded
    @POST("${NetworkConstants.V1}/order/{order_id}/update-item-status/")
    fun updateOrderItem(@Path("order_id") orderId: Int,
                        @Field("accepted_order_item_ids") accepted_ids: List<Int>,
                        @Field("rejected_order_item_ids") rejected_ids: List<Int>): Observable<Response<GenericResponse>>



}