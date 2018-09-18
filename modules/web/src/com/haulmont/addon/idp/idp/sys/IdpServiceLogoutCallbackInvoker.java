package com.haulmont.addon.idp.idp.sys;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.haulmont.addon.idp.idp.config.IdpConfig;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component("cuba_IdpServiceLogoutCallbackInvoker")
public class IdpServiceLogoutCallbackInvoker {

    private static final Logger log = LoggerFactory.getLogger(IdpServiceLogoutCallbackInvoker.class);

    @Inject
    protected IdpConfig idpConfig;

    protected ExecutorService asyncServiceLogoutExecutor =
            Executors.newSingleThreadExecutor(
                    new ThreadFactoryBuilder()
                            .setNameFormat("IdpServiceLogoutCallbackInvoker-%d")
                            .build()
            );

    public void performLogoutOnServiceProviders(String idpSessionId) {
        List<String> serviceProviderLogoutUrls = idpConfig.getServiceProviderLogoutUrls();

        asyncServiceLogoutExecutor.submit(() -> {
            HttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
            HttpClient client = HttpClientBuilder.create()
                    .setConnectionManager(connectionManager)
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                    .build();

            try {
                for (String serviceProviderLogoutUrl : serviceProviderLogoutUrls) {
                    callLoggedOutServiceUrl(client, serviceProviderLogoutUrl, idpSessionId);
                }
            } catch (Throwable ex) {
                log.error("Unable to perform logout on IDP services for session {}", idpSessionId, ex);
            } finally {
                connectionManager.shutdown();
            }
        });
    }

    protected void callLoggedOutServiceUrl(HttpClient httpClient, String serviceProviderLogoutUrl, String idpSessionId) {
        HttpPost httpPost = new HttpPost(serviceProviderLogoutUrl);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());

        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(Arrays.asList(
                new BasicNameValuePair("idpSessionId", idpSessionId),
                new BasicNameValuePair("trustedServicePassword", idpConfig.getTrustedServicePassword())
        ), StandardCharsets.UTF_8);

        httpPost.setEntity(formEntity);

        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log.warn("Service provider logout url {} returns code {}", serviceProviderLogoutUrl, statusCode);
            }
        } catch (IOException e) {
            log.warn("Service provider logout url {} error {}", serviceProviderLogoutUrl, e.getMessage());
        }
    }

    @PreDestroy
    protected void stopAsyncExecutor() {
        asyncServiceLogoutExecutor.shutdown();
    }
}