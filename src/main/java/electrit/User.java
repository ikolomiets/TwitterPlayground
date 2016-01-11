package electrit;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

    @JsonProperty("id")
    private long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("screen_name")
    private String screenName;

    // The UTC datetime
    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("protected")
    private boolean protectedAccount;

    @JsonProperty("followers_count")
    private int followersCount;

    @JsonProperty("friends_count")
    private int friendsCount;

    @JsonProperty("listed_count")
    private int listedCount;

    @JsonProperty("favourites_count")
    private int favouritesCount;

    @JsonProperty("verified")
    private boolean verified;

    @JsonProperty("statuses_count")
    private int statusesCount;

    @JsonProperty("default_profile_image")
    private boolean defaultProfileImage;

    @JsonProperty("profile_image_url_https")
    private String profileImageUrlHttps;

    public long getId() {
        return id;
    }

    public String getIdAsString() {
        return Long.toUnsignedString(id);
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isProtectedAccount() {
        return protectedAccount;
    }

    public void setProtectedAccount(boolean protectedAccount) {
        this.protectedAccount = protectedAccount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public int getListedCount() {
        return listedCount;
    }

    public void setListedCount(int listedCount) {
        this.listedCount = listedCount;
    }

    public int getFavouritesCount() {
        return favouritesCount;
    }

    public void setFavouritesCount(int favouritesCount) {
        this.favouritesCount = favouritesCount;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getStatusesCount() {
        return statusesCount;
    }

    public void setStatusesCount(int statusesCount) {
        this.statusesCount = statusesCount;
    }

    public boolean isDefaultProfileImage() {
        return defaultProfileImage;
    }

    public void setDefaultProfileImage(boolean defaultProfileImage) {
        this.defaultProfileImage = defaultProfileImage;
    }

    public String getProfileImageUrlHttps() {
        return profileImageUrlHttps;
    }

    public void setProfileImageUrlHttps(String profileImageUrlHttps) {
        this.profileImageUrlHttps = profileImageUrlHttps;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getIdAsString() +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", protectedAccount=" + protectedAccount +
                ", followersCount=" + followersCount +
                ", friendsCount=" + friendsCount +
                ", listedCount=" + listedCount +
                ", favouritesCount=" + favouritesCount +
                ", verified=" + verified +
                ", statusesCount=" + statusesCount +
                ", defaultProfileImage=" + defaultProfileImage +
                ", profileImageUrlHttps='" + profileImageUrlHttps + '\'' +
                '}';
    }
}
