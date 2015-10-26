import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TwitterClient {

    private static final String URL_LOOKUP_USERS_BY_ID = "https://api.twitter.com/1.1/users/lookup.json?include_entities=false&user_id={user_id}";
    private static final String URL_SHOW_USER_BY_ID = "https://api.twitter.com/1.1/users/show.json?include_entities=false&user_id={user_id}";
    private static final String URL_FOLLOWERS_BY_ID = "https://api.twitter.com/1.1/followers/ids.json?cursor={cursor}&user_id={user_id}";
    private static final String URL_FRIENDS_BY_ID = "https://api.twitter.com/1.1/friends/ids.json?cursor={cursor}&user_id={user_id}";

    private final RestTemplate restTemplate;

    @Autowired
    public TwitterClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<User> lookupUsersById(List<Long> ids) {
        if (ids.size() > 100)
            throw new IllegalArgumentException("Maximum is 100 users");

        List<String> idsAsStrings = ids.stream().map(Long::toUnsignedString).collect(Collectors.toList());
        String listOfIds =  String.join(",", idsAsStrings);

        User[] users = restTemplate.postForObject(URL_LOOKUP_USERS_BY_ID, null, User[].class, listOfIds);
        return Arrays.asList(users);
    }

    public User showUserById(long id) {
        return restTemplate.getForObject(URL_SHOW_USER_BY_ID, User.class, Long.toUnsignedString(id));
    }

    public CursoredResult getFollowersByUserId(long userId, String cursor) {
        return restTemplate.getForObject(URL_FOLLOWERS_BY_ID, CursoredResult.class, cursor == null ? "-1" : cursor, Long.toUnsignedString(userId));
    }

    public CursoredResult getFriendsByUserId(long userId, String cursor) {
        return restTemplate.getForObject(URL_FRIENDS_BY_ID, CursoredResult.class, cursor == null ? "-1" : cursor, Long.toUnsignedString(userId));
    }

}
