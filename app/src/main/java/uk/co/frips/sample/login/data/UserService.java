package uk.co.frips.sample.login.data;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;
import uk.co.frips.sample.login.data.entity.Avatar;
import uk.co.frips.sample.login.data.entity.Credentials;
import uk.co.frips.sample.login.data.entity.SessionResponse;
import uk.co.frips.sample.login.data.entity.User;

public interface UserService {

    @POST("session")
    Observable<SessionResponse> login(@Body Credentials credentials);

    @POST("session")
    Call<SessionResponse> loginSync(@Body Credentials credentials);

    @GET("users/{userId}")
    Observable<User> getUser(@Path("userId") String userId);

    @POST("users/{userId}/avatar")
    Observable<Avatar> postAvatar(@Path("userId") String userId, @Body Avatar avatar);
}
