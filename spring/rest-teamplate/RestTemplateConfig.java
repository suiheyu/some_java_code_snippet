
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;

@Configuration
@Slf4j
public class GlobalConfig {

    /**
     * 连接超时时间 15S
     */
    private static final int MAX_CONNECT_TIME_OUT = 5000;

    /**
     * 读取超时时间 60S
     */
    private static final int MAX_READ_TIME_OUT = 60000;

    private static final String ZONE_GMT8_STR = "GMT+8";

    private final RestTemplateBuilder builder;

    @Autowired
    public GlobalConfig(RestTemplateBuilder builder) {
        this.builder = builder;
    }



    private ClientHttpResponse executeAndLog(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String url = request.getURI().toString();
        String bodyStr = new String(body, StandardCharsets.UTF_8);
        log.info("request {} url: {}, contentType: {}, token: {}, body: {}"
                , request.getMethodValue()
                , url
                , request.getHeaders().getContentType()
                , request.getHeaders().get(HttpHeaders.AUTHORIZATION)
                , bodyStr
        );
        ClientHttpResponse execute = execution.execute(request, body);
        byte[] bodyBytes = StreamUtils.copyToByteArray(execute.getBody());
        log.info("response code {}, body: {}", execute.getStatusText(), new String(bodyBytes, StandardCharsets.UTF_8));
        if (
                !HttpStatus.OK.equals(execute.getStatusCode())
                        && !HttpStatus.MOVED_PERMANENTLY.equals(execute.getStatusCode())
                        && !HttpStatus.FOUND.equals(execute.getStatusCode())

        ) {
            log.info("request failed : {} ,httpCode :{},params :{}", url, execute.getStatusText(), bodyStr);
            throw new ServiceException(ExceptionCode.API.API_REQUEST_FAILED, "系统内部异常");
        }
        return new ClientHttpResponseWrapper(execute, bodyBytes);
    }

    @Bean("arnhemRestTemplate")
    public RestTemplate arnhemRestTemplate(ArnhemTokenProvider tokenProvider) {
        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder().simpleDateFormat(DateSymbols.YYYY_MM_DD_HH_MM_SS)
                .createXmlMapper(Boolean.FALSE)
                .failOnUnknownProperties(Boolean.FALSE)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .timeZone(TimeZone.getTimeZone(ZONE_GMT8_STR))
                .build();

        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        return builder.setConnectTimeout(MAX_CONNECT_TIME_OUT)
                .setReadTimeout(MAX_READ_TIME_OUT)
                .interceptors((request, body, execution) -> {
                    HttpHeaders headers = request.getHeaders();
                    if (!HttpMethod.GET.equals(request.getMethod()) && headers.getContentType() == null) {
                        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                    }
                    //获取不带参数的url
                    String url = request.getURI().toString();
                    int splitIndex = url.indexOf("?");
                    String urlNoArgs = splitIndex >= 0 ? url.substring(0, splitIndex) : url;

                    headers.set(HttpHeaders.AUTHORIZATION, tokenProvider.getToken(urlNoArgs));
                    return executeAndLog(request, body, execution);
                })
                .messageConverters(jacksonConverter)
                .build();
    }

    /*@bean
    public MethodValidationPostProcessor methodValidationPostProcessor()
    {
        return new MethodValidationPostProcessor();
    }*/

    /**
     * bean of class MethodValidationInterceptor is essential to enable
     * validation on methods. Before method validation is enabled, the
     * method or class which the method belongs to must be annotated
     * with '@Validated' as well. The following is an example.
     * <p>
     * {@code @Validated}
     * public void test(@NotBlank args) { }
     *
     * @return MethodValidationInterceptor
     * @author sunguangtao
     * @date 2019/1/8
     */
    @Bean
    public MethodValidationInterceptor methodValidationInterceptor() {
        return new MethodValidationInterceptor();
    }

    @Bean(initMethod = "init")
    public FastDfsClient fastDfsClient() {
        return new FastDfsClient();
    }

}
