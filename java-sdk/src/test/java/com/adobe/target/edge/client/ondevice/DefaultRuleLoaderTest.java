/*
 * Copyright 2019 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.adobe.target.edge.client.ondevice;

import com.adobe.target.artifact.ArtifactObfuscator;
import com.adobe.target.artifact.TargetInvalidArtifactException;
import com.adobe.target.edge.client.ClientConfig;
import com.adobe.target.edge.client.http.JacksonObjectMapper;
import com.adobe.target.edge.client.model.DecisioningMethod;
import com.adobe.target.edge.client.model.ondevice.OnDeviceDecisioningRuleSet;
import com.adobe.target.edge.client.model.ondevice.OnDeviceDecisioningHandler;
import com.adobe.target.edge.client.service.TargetClientException;
import com.adobe.target.edge.client.service.TargetExceptionHandler;
import kong.unirest.*;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;

import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultRuleLoaderTest {

    static final String TEST_ORG_ID = "0DD934B85278256B0A490D44@AdobeOrg";
    static final String TEST_RULE_SET = "{\"version\":\"1.0.0\",\"meta\":{\"generatedAt\":\"2020-03-17T22:29:29.115Z\",\"remoteMboxes\":[\"recommendations\"],\"globalMbox\":\"target-global-mbox\"},\"rules\":{\"mboxes\":{\"product\":[{\"condition\":{\"and\":[{\"<\":[0,{\"var\":\"allocation\"},50]},{\"<=\":[1580371200000,{\"var\":\"current_timestamp\"},1600585200000]}]},\"consequence\":{\"mboxes\":[{\"options\":[{\"content\":{\"product\":\"default\"},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"3eLgpLF+APtuSsE47wxq/mqipfsIHvVzTQxHolz2IpSCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"product\"}]},\"meta\":{\"activityId\":317586,\"experienceId\":0,\"type\":\"ab\",\"mbox\":\"product\"}},{\"condition\":{\"and\":[{\"<\":[50,{\"var\":\"allocation\"},100]},{\"<=\":[1580371200000,{\"var\":\"current_timestamp\"},1600585200000]}]},\"consequence\":{\"mboxes\":[{\"options\":[{\"content\":{\"product\":\"new_layout\"},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"3eLgpLF+APtuSsE47wxq/pNWHtnQtQrJfmRrQugEa2qCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"product\"}]},\"meta\":{\"activityId\":317586,\"experienceId\":1,\"type\":\"ab\",\"mbox\":\"product\"}}]},\"views\":{}}}";
    static final String TEST_RULE_SET_HIGHER_VERSION = "{\"version\":\"2.0.0\",\"meta\":{\"generatedAt\":\"2020-03-17T22:29:29.115Z\",\"remoteMboxes\":[\"recommendations\"],\"globalMbox\":\"target-global-mbox\"},\"rules\":{\"mboxes\":{\"product\":[{\"condition\":{\"and\":[{\"<\":[0,{\"var\":\"allocation\"},50]},{\"<=\":[1580371200000,{\"var\":\"current_timestamp\"},1600585200000]}]},\"consequence\":{\"mboxes\":[{\"options\":[{\"content\":{\"product\":\"default\"},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"3eLgpLF+APtuSsE47wxq/mqipfsIHvVzTQxHolz2IpSCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"product\"}]},\"meta\":{\"activityId\":317586,\"experienceId\":0,\"type\":\"ab\",\"mbox\":\"product\"}},{\"condition\":{\"and\":[{\"<\":[50,{\"var\":\"allocation\"},100]},{\"<=\":[1580371200000,{\"var\":\"current_timestamp\"},1600585200000]}]},\"consequence\":{\"mboxes\":[{\"options\":[{\"content\":{\"product\":\"new_layout\"},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"3eLgpLF+APtuSsE47wxq/pNWHtnQtQrJfmRrQugEa2qCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"product\"}]},\"meta\":{\"activityId\":317586,\"experienceId\":1,\"type\":\"ab\",\"mbox\":\"product\"}}]},\"views\":{}}}";

    private final byte[] obfuscatedTestRuleSet;
    private final byte[] obfuscatedTestRuleSetHigherVersion;

    private TargetExceptionHandler exceptionHandler;
    private OnDeviceDecisioningHandler executionHandler;
    private ClientConfig clientConfig;

    public DefaultRuleLoaderTest() {
        String randomKey = "12345678901234567890123456789012";
        ArtifactObfuscator artifactObfuscator = new ArtifactObfuscator();
        obfuscatedTestRuleSet = artifactObfuscator.obfuscate(
            TEST_ORG_ID,
            randomKey,
            TEST_RULE_SET.getBytes(StandardCharsets.UTF_8));
        obfuscatedTestRuleSetHigherVersion = artifactObfuscator.obfuscate(
            TEST_ORG_ID,
            randomKey,
            TEST_RULE_SET_HIGHER_VERSION.getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void init() {
        exceptionHandler = spy(new TargetExceptionHandler() {
            @Override
            public void handleException(TargetClientException e) {

            }
        });

        executionHandler = spy(new OnDeviceDecisioningHandler() {
            @Override
            public void onDeviceDecisioningReady() {

            }

            @Override
            public void artifactDownloadSucceeded(byte[] artifactData) {

            }

            @Override
            public void artifactDownloadFailed(TargetClientException e) {

            }
        });

        clientConfig = ClientConfig.builder()
                .client("emeaprod4")
                .organizationId(TEST_ORG_ID)
                .localEnvironment("production")
                .defaultDecisioningMethod(DecisioningMethod.ON_DEVICE)
                .exceptionHandler(exceptionHandler)
                .onDeviceDecisioningHandler(executionHandler)
                .build();

    }

    static HttpResponse<byte[]> getTestResponse(final byte[] ruleSet, final String etag, final int status) {
        return new HttpResponse<byte[]>() {
            @Override
            public int getStatus() {
                return status;
            }

            @Override
            public String getStatusText() {
                return null;
            }

            @Override
            public Headers getHeaders() {
                Headers headers = new Headers();
                if (etag != null) {
                    headers.add("ETag", etag);
                }
                return headers;
            }

            @Override
            public byte[] getBody() {
                return ruleSet;
            }

            @Override
            public Optional<UnirestParsingException> getParsingError() {
                return Optional.empty();
            }

            @Override
            public <V> V mapBody(Function<byte[], V> func) {
                return null;
            }

            @Override
            public <V> HttpResponse<V> map(Function<byte[], V> func) {
                return null;
            }

            @Override
            public HttpResponse<byte[]> ifSuccess(Consumer<HttpResponse<byte[]>> consumer) {
                return null;
            }

            @Override
            public HttpResponse<byte[]> ifFailure(Consumer<HttpResponse<byte[]>> consumer) {
                return null;
            }

            @Override
            public <E> HttpResponse<byte[]> ifFailure(Class<? extends E> errorClass, Consumer<HttpResponse<E>> consumer) {
                return null;
            }

            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public <E> E mapError(Class<? extends E> errorClass) {
                return null;
            }
        };
    }

    @Test
    void testDefaultRuleLoader() throws TargetInvalidArtifactException, NoSuchFieldException {
        DefaultRuleLoader defaultRuleLoader = mock(DefaultRuleLoader.class, CALLS_REAL_METHODS);
        ArtifactObfuscator artifactObfuscator = new ArtifactObfuscator();
        FieldSetter.setField(defaultRuleLoader, DefaultRuleLoader.class.getDeclaredField("artifactObfuscator"), artifactObfuscator);
        ObjectMapper objectMapper = new JacksonObjectMapper();
        FieldSetter.setField(defaultRuleLoader, DefaultRuleLoader.class.getDeclaredField("objectMapper"), objectMapper);

        String etag = "5b1cf3c050e1a0d16934922bf19ba6ea";
        Mockito.doReturn(null)
                .when(defaultRuleLoader).generateRequest(any(ClientConfig.class));
        Mockito.doReturn(getTestResponse(obfuscatedTestRuleSet, etag, HttpStatus.SC_OK))
                .when(defaultRuleLoader).executeRequest(any());

        defaultRuleLoader.start(clientConfig);
        verify(defaultRuleLoader, timeout(1000)).setLatestRules(any(OnDeviceDecisioningRuleSet.class));
        verify(defaultRuleLoader, timeout(1000)).setLatestETag(eq(etag));
        verify(executionHandler, timeout(1000)).onDeviceDecisioningReady();
        verify(executionHandler, timeout(1000)).artifactDownloadSucceeded(any());
        verify(executionHandler, never()).artifactDownloadFailed(any());
        OnDeviceDecisioningRuleSet rules = defaultRuleLoader.getLatestRules();
        assertNotNull(rules);
        defaultRuleLoader.stop();

        // do it again, make sure starting works again after a stop
        reset(executionHandler);
        defaultRuleLoader.start(clientConfig);
        verify(defaultRuleLoader, timeout(1000)).setLatestRules(any(OnDeviceDecisioningRuleSet.class));
        verify(defaultRuleLoader, timeout(1000)).setLatestETag(eq(etag));
        verify(executionHandler, timeout(1000)).onDeviceDecisioningReady();
        verify(executionHandler, timeout(1000)).artifactDownloadSucceeded(any());
        verify(executionHandler, never()).artifactDownloadFailed(any());
        rules = defaultRuleLoader.getLatestRules();
        assertNotNull(rules);

        Mockito.doReturn(getTestResponse(obfuscatedTestRuleSet, "5b1cf3c050e1a0d16934922bf19ba6ea", HttpStatus.SC_NOT_MODIFIED))
                .when(defaultRuleLoader).executeRequest(any());

        defaultRuleLoader.refresh();
        verify(exceptionHandler, never()).handleException(any(TargetClientException.class));
        defaultRuleLoader.stop();
     }

    @Test
    void testDefaultRuleLoaderNullResponse() throws TargetInvalidArtifactException {
        DefaultRuleLoader defaultRuleLoader = mock(DefaultRuleLoader.class, CALLS_REAL_METHODS);

        Mockito.doReturn(null)
                .when(defaultRuleLoader).generateRequest(any(ClientConfig.class));
        Mockito.doReturn(getTestResponse(null, "5b1cf3c050e1a0d16934922bf19ba6ea", HttpStatus.SC_OK))
                .when(defaultRuleLoader).executeRequest(any());

        defaultRuleLoader.start(clientConfig);
        verify(exceptionHandler, timeout(1000)).handleException(any(TargetClientException.class));
        verify(executionHandler, never()).onDeviceDecisioningReady();
        verify(executionHandler, never()).artifactDownloadSucceeded(any());
        verify(executionHandler, timeout(1000)).artifactDownloadFailed(any());
        defaultRuleLoader.stop();
    }

    @Test
    void testDefaultRuleLoaderInvalidVersion() throws TargetInvalidArtifactException {

        DefaultRuleLoader defaultRuleLoader = mock(DefaultRuleLoader.class, CALLS_REAL_METHODS);

        Mockito.doReturn(null)
                .when(defaultRuleLoader).generateRequest(any(ClientConfig.class));
        Mockito.doReturn(getTestResponse(obfuscatedTestRuleSetHigherVersion, "5b1cf3c050e1a0d16934922bf19ba6ea", HttpStatus.SC_OK))
                .when(defaultRuleLoader).executeRequest(any());

        defaultRuleLoader.start(clientConfig);
        verify(exceptionHandler, timeout(1000)).handleException(any(TargetClientException.class));
        verify(executionHandler, never()).onDeviceDecisioningReady();
        verify(executionHandler, never()).artifactDownloadSucceeded(any());
        verify(executionHandler, timeout(1000)).artifactDownloadFailed(any());
        defaultRuleLoader.stop();
    }

    @Test
    void testDefaultRuleLoaderInvalidStatus() throws TargetInvalidArtifactException {
        DefaultRuleLoader defaultRuleLoader = mock(DefaultRuleLoader.class, CALLS_REAL_METHODS);

        Mockito.doReturn(null)
                .when(defaultRuleLoader).generateRequest(any(ClientConfig.class));
        Mockito.doReturn(getTestResponse(obfuscatedTestRuleSet, "5b1cf3c050e1a0d16934922bf19ba6ea", HttpStatus.SC_NOT_FOUND))
                .when(defaultRuleLoader).executeRequest(any());

        defaultRuleLoader.start(clientConfig);
        verify(exceptionHandler, timeout(1000)).handleException(any(TargetClientException.class));
        verify(executionHandler, never()).onDeviceDecisioningReady();
        verify(executionHandler, never()).artifactDownloadSucceeded(any());
        verify(executionHandler, timeout(1000)).artifactDownloadFailed(any());
        defaultRuleLoader.stop();
    }

    @Test
    void testRuleLoaderArtifactPayload() throws TargetInvalidArtifactException, NoSuchFieldException {
        DefaultRuleLoader defaultRuleLoader = mock(DefaultRuleLoader.class, CALLS_REAL_METHODS);
        ArtifactObfuscator artifactObfuscator = new ArtifactObfuscator();
        FieldSetter.setField(defaultRuleLoader, DefaultRuleLoader.class.getDeclaredField("artifactObfuscator"), artifactObfuscator);
        ObjectMapper objectMapper = new JacksonObjectMapper();
        FieldSetter.setField(defaultRuleLoader, DefaultRuleLoader.class.getDeclaredField("objectMapper"), objectMapper);

        String etag = "5b1cf3c050e1a0d16934922bf19ba6ea";
        Mockito.doReturn(null)
                .when(defaultRuleLoader).generateRequest(any(ClientConfig.class));
        Mockito.doReturn(getTestResponse(obfuscatedTestRuleSet, etag, HttpStatus.SC_OK))
                .when(defaultRuleLoader).executeRequest(any());

        ClientConfig payloadClientConfig = ClientConfig.builder()
                .client("emeaprod4")
                .organizationId(TEST_ORG_ID)
                .localEnvironment("production")
                .defaultDecisioningMethod(DecisioningMethod.ON_DEVICE)
                .exceptionHandler(exceptionHandler)
                .onDeviceDecisioningHandler(executionHandler)
                .onDeviceArtifactPayload(obfuscatedTestRuleSet)
                .build();

        defaultRuleLoader.start(payloadClientConfig);
        verify(defaultRuleLoader, timeout(1000)).setLatestRules(any(OnDeviceDecisioningRuleSet.class));
        verify(executionHandler, timeout(1000)).onDeviceDecisioningReady();
        verify(executionHandler, never()).artifactDownloadSucceeded(any());
        verify(executionHandler, never()).artifactDownloadFailed(any());
        OnDeviceDecisioningRuleSet rules = defaultRuleLoader.getLatestRules();
        assertNotNull(rules);

        defaultRuleLoader.refresh();
        verify(exceptionHandler, never()).handleException(any(TargetClientException.class));
        defaultRuleLoader.stop();
    }

    @Test
    void testCorruptedArtifactPayload() throws NoSuchFieldException {
        DefaultRuleLoader defaultRuleLoader = mock(DefaultRuleLoader.class, CALLS_REAL_METHODS);
        ArtifactObfuscator artifactObfuscator = new ArtifactObfuscator();
        FieldSetter.setField(defaultRuleLoader, DefaultRuleLoader.class.getDeclaredField("artifactObfuscator"), artifactObfuscator);
        ObjectMapper objectMapper = new JacksonObjectMapper();
        FieldSetter.setField(defaultRuleLoader, DefaultRuleLoader.class.getDeclaredField("objectMapper"), objectMapper);

        byte[] badArtifact = { 65, 65, 65 };

        ClientConfig payloadClientConfig = ClientConfig.builder()
            .client("emeaprod4")
            .organizationId(TEST_ORG_ID)
            .localEnvironment("production")
            .defaultDecisioningMethod(DecisioningMethod.ON_DEVICE)
            .exceptionHandler(exceptionHandler)
            .onDeviceDecisioningHandler(executionHandler)
            .onDeviceArtifactPayload(badArtifact)
            .build();

        assertThrows(TargetInvalidArtifactException.class, () -> {
            defaultRuleLoader.start(payloadClientConfig);
        });
    }


}
