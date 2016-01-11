package electrit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth.common.signature.SharedConsumerSecretImpl;
import org.springframework.security.oauth.consumer.BaseProtectedResourceDetails;
import org.springframework.security.oauth.consumer.OAuthConsumerToken;
import org.springframework.security.oauth.consumer.OAuthSecurityContextHolder;
import org.springframework.security.oauth.consumer.OAuthSecurityContextImpl;
import org.springframework.security.oauth.consumer.client.OAuthRestTemplate;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:twitter.properties")
public class AppConfig {

    @Autowired
    private Environment env;

    @Bean(name = "restTemplateForApp")
    public RestTemplate restTemplate() {
        ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
        details.setClientId(env.getProperty("twitter.clientID"));
        details.setClientSecret(env.getProperty("twitter.clientSecret"));
        details.setAccessTokenUri(env.getProperty("twitter.accessTokenUri"));

        RestTemplate restTemplate = new OAuth2RestTemplate(details);

        return prepareRestTemplate(restTemplate);
    }

    @Bean(name = "restTemplateForUser")
    public RestTemplate restTemplateForUser() {
        String owner = env.getProperty("twitter.owner");

        Map<String, OAuthConsumerToken> accessTokens = new HashMap<String, OAuthConsumerToken>() {{
            OAuthConsumerToken consumerToken = new OAuthConsumerToken();
            consumerToken.setValue(env.getProperty("twitter.accessToken"));
            consumerToken.setSecret(env.getProperty("twitter.accessTokenSecret"));

            put(owner, consumerToken);
        }};

        OAuthSecurityContextImpl context = new OAuthSecurityContextImpl();
        context.setAccessTokens(accessTokens);

        OAuthSecurityContextHolder.setContext(context);

        BaseProtectedResourceDetails protectedResourceDetails = new BaseProtectedResourceDetails();
        protectedResourceDetails.setId(owner);
        protectedResourceDetails.setConsumerKey(env.getProperty("twitter.clientID"));
        protectedResourceDetails.setSharedSecret(new SharedConsumerSecretImpl(env.getProperty("twitter.clientSecret")));

        RestTemplate restTemplate = new OAuthRestTemplate(protectedResourceDetails);

        return prepareRestTemplate(restTemplate);
    }

    private RestTemplate prepareRestTemplate(RestTemplate restTemplate) {
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        // Order of interceptors matters: add RateLimitInterceptor last,so
        // it will be called *before* ProtectedUserAccountInterceptor to update RateLimitsScoreborad
        restTemplate.getInterceptors().add(new ProtectedUserAccountInterceptor());
        restTemplate.getInterceptors().add(new RateLimitInterceptor(new RateLimitsScoreborad()));

        for (HttpMessageConverter<?> httpMessageConverter : restTemplate.getMessageConverters()) {
            if (httpMessageConverter instanceof MappingJackson2HttpMessageConverter) {
                ObjectMapper objectMapper = ((MappingJackson2HttpMessageConverter) httpMessageConverter).getObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                break;
            }
        }

        return restTemplate;
    }

}
