import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;

import java.io.IOException;

public abstract class AbstractDelegatingResponseExtractor<T> implements ResponseExtractor<T> {

    private final ResponseExtractor<T> delegate;

    public AbstractDelegatingResponseExtractor(ResponseExtractor<T> delegate) {
        this.delegate = delegate;
    }

    protected abstract void doExtractData(ClientHttpResponse response);

    @Override
    public T extractData(ClientHttpResponse response) throws IOException {
        doExtractData(response);
        return delegate.extractData(response);
    }

}
