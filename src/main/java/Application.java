import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

public class Application {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

        OAuth2RestTemplate restTemplate = ctx.getBean(OAuth2RestTemplate.class);

        User user = restTemplate.getForObject("https://api.twitter.com/1.1/users/show.json?screen_name=ikolomiets", User.class);
        System.out.println(user);
    }

}
