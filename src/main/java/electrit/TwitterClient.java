package electrit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TwitterClient {

    private static final Logger logger = LoggerFactory.getLogger(TwitterClient.class);

    private static final String URL_LOOKUP_USERS = "https://api.twitter.com/1.1/users/lookup.json?include_entities=false&user_id={user_id}&screen_name={screen_name}";
    private static final String URL_SHOW_USER_BY_ID = "https://api.twitter.com/1.1/users/show.json?include_entities=false&user_id={user_id}";
    private static final String URL_FOLLOWERS_BY_ID = "https://api.twitter.com/1.1/followers/ids.json?cursor={cursor}&user_id={user_id}";
    private static final String URL_FRIENDS_BY_ID = "https://api.twitter.com/1.1/friends/ids.json?cursor={cursor}&user_id={user_id}";
    private static final String URL_PUBLIC_STREAM_SAMPLE = "https://stream.twitter.com/1.1/statuses/sample.json";

    private final RestTemplate restTemplate;
    private final RestTemplate restTemplateForUser;

    @Autowired
    public TwitterClient(@Qualifier("restTemplateForApp") RestTemplate restTemplate,
                         @Qualifier("restTemplateForUser") RestTemplate restTemplateForUser) {
        this.restTemplate = restTemplate;
        this.restTemplateForUser = restTemplateForUser;
    }

    public static void main(String[] args) {
        System.out.println();
    }

    public void publicStreamSample() throws IOException {
        ClientHttpRequestFactory requestFactory = restTemplateForUser.getRequestFactory();
        ClientHttpRequest request = requestFactory.createRequest(URI.create(URL_PUBLIC_STREAM_SAMPLE), HttpMethod.GET);
        request.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ClientHttpResponse clientHttpResponse = request.execute();

        InputStream inputStream = clientHttpResponse.getBody();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            logger.debug("XXX: {}", line);
        }
    }

    public List<User> lookupUsersByName(List<String> names) {
        if (names.size() > 100)
            throw new IllegalArgumentException("Maximum is 100 names");

        User[] users = restTemplate.postForObject(URL_LOOKUP_USERS, null, User[].class, null, String.join(",", names));
        return Arrays.asList(users);
    }

    public List<User> lookupUsersById(List<Long> ids) {
        if (ids.size() > 100)
            throw new IllegalArgumentException("Maximum is 100 ids");

        List<String> idsAsStrings = ids.stream().map(Long::toUnsignedString).collect(Collectors.toList());
        String listOfIds =  String.join(",", idsAsStrings);

        User[] users = restTemplate.postForObject(URL_LOOKUP_USERS, null, User[].class, listOfIds, null);
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
