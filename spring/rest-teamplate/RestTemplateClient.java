package com.inspur.bss.waf.vendor.arnhem.utils;

import com.inspur.bss.waf.common.exception.model.ServiceException;
import com.inspur.bss.waf.common.json.JsonUtils;
import com.inspur.bss.waf.vendor.arnhem.bean.ArnhemResponse;
import com.inspur.bss.waf.vendor.arnhem.constant.ArnhemException;
import com.rabbitmq.tools.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestTemplateClient {

    private final RestTemplate restTemplate;
    private final String URL_TEMPLATE = "#={#}";
    private final String URL_PARAM_PLACE_HOLDER = "#";

    @Autowired
    public RestTemplateClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> getDefaultRequestBody() {
        Map<String, Object> request = new HashMap<>();
        return request;
    }

    public <R,P> R sendPostRequest(String url, P object, Class<R> responseType) {
        return sendRequest(url,HttpMethod.POST,object,responseType);
    }

    public <R> R sendGetRequest(String url,Map<String,?> variables, Class<R> responseType) {
        return sendRequest(url,HttpMethod.GET,HttpEntity.EMPTY,responseType,variables);
    }

    public <R,P> R sendRequest(String url,HttpMethod httpMethod,HttpHeaders httpHeaders, P object, Class<R> clazz){
            return sendRequest(url, httpMethod, new HttpEntity<>(object,httpHeaders), clazz);
    }

    public <R,P> R sendRequest(String url,HttpMethod httpMethod, P object, Class<R> clazz){
        return sendRequest(url, httpMethod, new HttpEntity<>(object), clazz);
    }

    public <R,P> R sendRequest(String url,HttpMethod httpMethod, P object, Class<R> clazz, Map<String,?> uriVariables){
        return sendRequest(url, httpMethod, new HttpEntity<>(object), clazz,uriVariables);
    }

    public <R,P> R sendRequest(String url, HttpMethod httpMethod, HttpEntity<P> httpEntity, Class<R> clazz){
        ResponseEntity<R> responseEntity = restTemplate.exchange(url, httpMethod, httpEntity, clazz);
        return responseEntity.getBody();
    }

    public <R,P> R sendRequest(String url, HttpMethod httpMethod, HttpEntity<P> httpEntity, Class<R> clazz,Map<String,?> uriVariables){
        url = getVariableUri(url,uriVariables);
        ResponseEntity<R> responseEntity = restTemplate.exchange(url, httpMethod, httpEntity, clazz,uriVariables);
        return responseEntity.getBody();
    }

    public <R,P> R sendPostRequestAndGetData(String url, P object, ParameterizedTypeReference<ArnhemResponse<R>> responseType){
        return sendPostRequest(url,object,responseType).getData();
    }

    public <R,P> R sendPostRequestAndGetData(String url, P object, Map<String,?> uriVariables, ParameterizedTypeReference<ArnhemResponse<R>> responseType){
        return sendPostRequest(url,object,uriVariables,responseType).getData();
    }

    public <R,P> ArnhemResponse<R> sendPostRequest(String url, P object, Map<String,?> uriVariables, ParameterizedTypeReference<ArnhemResponse<R>> responseType){
        return sendRequest(url,HttpMethod.POST,new HttpEntity<>(object),uriVariables,responseType);
    }

    public <R,P> ArnhemResponse<R> sendPostRequest(String url, P object, ParameterizedTypeReference<ArnhemResponse<R>> responseType){
        return sendPostRequest(url,object,null,responseType);
    }

    public <R,P> ArnhemResponse<R> sendPostRequest(String url, P object,Class<?>... parametrizeds){
        return sendRequest(url,HttpMethod.POST,new HttpEntity<>(object),null,parametrizeds);
    }

    public <R> ArnhemResponse<R> sendGetRequest(String url, Map<String,?> variables,ParameterizedTypeReference<ArnhemResponse<R>> typeReference){
        return sendRequest(url,HttpMethod.GET,HttpEntity.EMPTY,variables,typeReference);
    }

    public <R> ArnhemResponse<R> sendGetRequest(String url, Map<String,?> variables,Class<?>... parametrizeds){
        return sendRequest(url,HttpMethod.GET,HttpEntity.EMPTY,variables,parametrizeds);
    }

    public <R> R sendGetRequestAndGetData(String url, Map<String,?> variables,ParameterizedTypeReference<ArnhemResponse<R>> typeReference){
        return sendRequest(url,HttpMethod.GET,HttpEntity.EMPTY,variables,typeReference).getData();
    }

    public <R> R sendGetRequestAndGetData(String url, Map<String,?> variables,Class<?>... parametrizeds){
        ArnhemResponse<R> arnhemResponse = sendRequest(url,HttpMethod.GET,HttpEntity.EMPTY,variables,parametrizeds);
        return arnhemResponse.getData();
    }

    public <R> R sendGetRequestAndGetData(String url,Object params,ParameterizedTypeReference<ArnhemResponse<R>> typeReference){
        return sendRequest(url,HttpMethod.GET,HttpEntity.EMPTY, objectToMap(params),typeReference).getData();
    }

    public <R> R sendGetRequestAndGetData(String url,Object params,Class<?>... parametrizeds){
        ArnhemResponse<R> arnhemResponse = sendRequest(url,HttpMethod.GET,HttpEntity.EMPTY, objectToMap(params),parametrizeds);
        return arnhemResponse.getData();
    }

    private Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                log.error("ojbectToMap Exception:",e);
            }
        }
        return map;
    }

    /**
     * 组装参数到url后面
     * @param url
     * @param uriVariables
     * @return
     */
    private String getVariableUri(String url ,Map<String,?> uriVariables){
        if(CollectionUtils.isNotEmpty(uriVariables.keySet())){
            String urlParamDelimiter = "&";
            String urlParamsPrefix = "?";
            url += urlParamsPrefix + uriVariables.keySet().stream()
                    .map(key -> URL_TEMPLATE.replaceAll(URL_PARAM_PLACE_HOLDER, key))
                    .collect(Collectors.joining(urlParamDelimiter));
        }
        return url;
    }

    public <R,P> ArnhemResponse<R> sendRequest(String url, HttpMethod httpMethod, HttpEntity<P> httpEntity, Map<String,?> uriVariables,ParameterizedTypeReference<ArnhemResponse<R>> typeReference){
        uriVariables = uriVariables == null ? new HashMap<>(1) : uriVariables;
        url = getVariableUri(url,uriVariables);
        ResponseEntity<ArnhemResponse<R>> responseEntity = restTemplate.exchange(url, httpMethod, httpEntity, typeReference,uriVariables);
        ArnhemResponse<R> response = responseEntity.getBody();
        if(response == null || !response.getSuccess()){
            throw new ServiceException(ArnhemException.API_REQUEST_ERROR
                    ,response == null ? responseEntity.getStatusCode().getReasonPhrase()
                    : String.format("%s", response.getMessage()));
//                    : String.format("errorCode:%s,message:%s,responseCode:%s", response.getErrorCode(), response.getMessage(), response.getResponseCode()));
        }
        return response;
    }

    public <R,P> ArnhemResponse<R> sendRequest(String url, HttpMethod httpMethod, HttpEntity<P> httpEntity, Map<String,?> uriVariables,Class<?>... parametrizeds){
        uriVariables = uriVariables == null ? new HashMap<>(1) : uriVariables;
        url = getVariableUri(url,uriVariables);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, httpMethod, httpEntity, String.class,uriVariables);
        ArnhemResponse<R> response;
        response = JsonUtils.fromJsonByClass(responseEntity.getBody(),parametrizeds);
        if(response == null || !response.getSuccess()){
            throw new ServiceException(ArnhemException.API_REQUEST_ERROR
                    ,response == null ? responseEntity.getStatusCode().getReasonPhrase()
                    : String.format("errorCode:%s,message:%s,responseCode:%s", response.getErrorCode(), response.getMessage(), response.getResponseCode()));
        }
        return response;
    }

}
