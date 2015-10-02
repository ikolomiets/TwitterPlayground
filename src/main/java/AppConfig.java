import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.AsyncRestTemplate;

@Configuration
@ComponentScan
@PropertySource("classpath:twitter.properties")
public class AppConfig {

    @Autowired
    private Environment env;

    @Bean
    public OAuth2RestTemplate restTemplate() {
        ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
        details.setId("twitter-client");
        details.setClientId(env.getProperty("clientID"));
        details.setClientSecret(env.getProperty("clientSecret"));
        details.setAccessTokenUri(env.getProperty("accessTokenUri"));

        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(details);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getInterceptors().add(new RateLimitHeadersInterceptor());

        for (HttpMessageConverter<?> httpMessageConverter : restTemplate.getMessageConverters()) {
            if (httpMessageConverter instanceof MappingJackson2HttpMessageConverter) {
                ObjectMapper objectMapper = ((MappingJackson2HttpMessageConverter) httpMessageConverter).getObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                break;
            }
        }

        return restTemplate;
    }

    @Bean
    public AsyncRestTemplate asyncRestTemplate(OAuth2RestTemplate restTemplate) {
        return new AsyncRestTemplate(new HttpComponentsAsyncClientHttpRequestFactory(), restTemplate);
    }

}
