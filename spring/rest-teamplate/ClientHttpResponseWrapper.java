
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author hexinyu
 * 2020/02/04
 */
public class ClientHttpResponseWrapper implements ClientHttpResponse {
    private final ClientHttpResponse response;
    private final ByteArrayInputStream bodyBytes;

    public ClientHttpResponseWrapper(ClientHttpResponse response,byte[] bodyBytes) {
        this.response = response;
        this.bodyBytes = new ByteArrayInputStream(bodyBytes);
    }

    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    @Override
    public InputStream getBody() throws IOException {
        return bodyBytes;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return response.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return response.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }

    @Override
    public void close() {
        response.close();
    }
}
