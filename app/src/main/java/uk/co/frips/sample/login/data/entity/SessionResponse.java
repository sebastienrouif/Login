package uk.co.frips.sample.login.data.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SessionResponse {

    public SessionResponse(String userid, String token) {
        this.userid = userid;
        this.token = token;
    }

    @SerializedName("userid")
    @Expose
    private String userid;

    @SerializedName("token")
    @Expose
    private String token;

    public String getUserid() {
        return userid;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "SessionResponse{" +
                "userid='" + userid + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
