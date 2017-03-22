package uk.co.frips.sample.login.data.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("avatar")
    @Expose
    private String avatar;

    public User(String email, String avatar) {
        this.email = email;
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", avatar='" + ((avatar == null) ? "emptyAvatar" : "anAvatarBase64") + '\'' +
                '}';
    }
}
