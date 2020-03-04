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
package com.adobe.target.edge.client.entities;

import com.adobe.target.delivery.v1.model.*;
import com.adobe.target.edge.client.Attributes;
import com.adobe.target.edge.client.ClientConfig;
import com.adobe.target.edge.client.TargetClient;
import com.adobe.target.edge.client.http.DefaultTargetHttpClient;
import com.adobe.target.edge.client.local.LocalDecisioningService;
import com.adobe.target.edge.client.local.ParamsCollator;
import com.adobe.target.edge.client.local.RuleLoader;
import com.adobe.target.edge.client.model.ExecutionMode;
import com.adobe.target.edge.client.model.TargetDeliveryRequest;
import com.adobe.target.edge.client.service.DefaultTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.adobe.target.edge.client.entities.TargetTestDeliveryRequestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TargetDeliveryAttributesTest {

    static final String TEST_ORG_ID = "0DD934B85278256B0A490D44@AdobeOrg";
    static final String TEST_RULE_SET = "{\"version\":\"1.0.0\",\"meta\":{\"generatedAt\":\"2020-02-27T23:27:26.445Z\"},\"rules\":[{\"condition\":{\"and\":[{\"<\":[0,{\"var\":\"allocation\"},50]},{\"and\":[{\"and\":[{\"==\":[\"bar\",{\"var\":\"mbox.foo\"}]},{\"==\":[\"buz\",{\"substr\":[{\"var\":\"mbox.baz_lc\"},0,3]}]}]},{\"and\":[{\"or\":[{\"<=\":[1582790400000,{\"var\":\"current_timestamp\"},4736217600000]}]},{\"or\":[{\"and\":[{\"or\":[{\"==\":[{\"var\":\"current_day\"},\"1\"]},{\"==\":[{\"var\":\"current_day\"},\"3\"]},{\"==\":[{\"var\":\"current_day\"},\"5\"]},{\"==\":[{\"var\":\"current_day\"},\"7\"]}]},{\"<=\":[\"0900\",{\"var\":\"current_time\"},\"1745\"]}]},{\"and\":[{\"or\":[{\"==\":[{\"var\":\"current_day\"},\"1\"]},{\"==\":[{\"var\":\"current_day\"},\"2\"]},{\"==\":[{\"var\":\"current_day\"},\"3\"]},{\"==\":[{\"var\":\"current_day\"},\"4\"]},{\"==\":[{\"var\":\"current_day\"},\"5\"]}]},{\"<=\":[\"0515\",{\"var\":\"current_time\"},\"1940\"]}]}]}]},{\"and\":[{\"==\":[{\"var\":\"user.browserType\"},\"firefox\"]},{\"and\":[{\">=\":[{\"var\":\"user.browserVersion\"},72]},{\"!=\":[{\"var\":\"user.browserType\"},\"ipad\"]}]}]},{\"and\":[{\"in\":[\"foo\",{\"var\":\"page.query\"}]},{\"==\":[\".jpg\",{\"substr\":[{\"var\":\"page.path\"},-4]}]},{\"==\":[\"ref1\",{\"var\":\"page.fragment_lc\"}]}]}]}]},\"consequence\":{\"mboxes\":[{\"options\":[{\"id\":632283,\"content\":{\"test\":true,\"experience\":\"a\"},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"IbG2Jz2xmHaqX7Ml/YRxRGqipfsIHvVzTQxHolz2IpSCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"testoffer\"}]},\"meta\":{\"activityId\":334694,\"experienceId\":0,\"type\":\"ab\",\"mboxes\":[\"testoffer\"],\"views\":[]}},{\"condition\":{\"and\":[{\"<\":[0,{\"var\":\"allocation\"},50]},{\"and\":[{\"and\":[{\"==\":[\"bar\",{\"var\":\"mbox.foo\"}]},{\"==\":[\"buz\",{\"substr\":[{\"var\":\"mbox.baz_lc\"},0,3]}]}]},{\"and\":[{\"or\":[{\"<=\":[1582790400000,{\"var\":\"current_timestamp\"},4736217600000]}]},{\"or\":[{\"and\":[{\"or\":[{\"==\":[{\"var\":\"current_day\"},\"1\"]},{\"==\":[{\"var\":\"current_day\"},\"3\"]},{\"==\":[{\"var\":\"current_day\"},\"5\"]},{\"==\":[{\"var\":\"current_day\"},\"7\"]}]},{\"<=\":[\"0900\",{\"var\":\"current_time\"},\"1745\"]}]},{\"and\":[{\"or\":[{\"==\":[{\"var\":\"current_day\"},\"1\"]},{\"==\":[{\"var\":\"current_day\"},\"2\"]},{\"==\":[{\"var\":\"current_day\"},\"3\"]},{\"==\":[{\"var\":\"current_day\"},\"4\"]},{\"==\":[{\"var\":\"current_day\"},\"5\"]}]},{\"<=\":[\"0515\",{\"var\":\"current_time\"},\"1940\"]}]}]}]},{\"and\":[{\"==\":[{\"var\":\"user.browserType\"},\"firefox\"]},{\"and\":[{\">=\":[{\"var\":\"user.browserVersion\"},72]},{\"!=\":[{\"var\":\"user.browserType\"},\"ipad\"]}]}]},{\"and\":[{\"in\":[\"foo\",{\"var\":\"page.query\"}]},{\"==\":[\".jpg\",{\"substr\":[{\"var\":\"page.path\"},-4]}]},{\"==\":[\"ref1\",{\"var\":\"page.fragment_lc\"}]}]}]}]},\"consequence\":{\"mboxes\":[{\"options\":[{\"id\":632285,\"content\":{\"test\":true,\"offer\":1},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"IbG2Jz2xmHaqX7Ml/YRxRGqipfsIHvVzTQxHolz2IpSCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"testoffer2\"}]},\"meta\":{\"activityId\":334694,\"experienceId\":0,\"type\":\"ab\",\"mboxes\":[\"testoffer2\"],\"views\":[]}},{\"condition\":{\"and\":[{\"<\":[50,{\"var\":\"allocation\"},100]},{\"and\":[{\"and\":[{\"==\":[\"bar\",{\"var\":\"mbox.foo\"}]},{\"==\":[\"buz\",{\"substr\":[{\"var\":\"mbox.baz_lc\"},0,3]}]}]},{\"and\":[{\"or\":[{\"<=\":[1582790400000,{\"var\":\"current_timestamp\"},4736217600000]}]},{\"or\":[{\"and\":[{\"or\":[{\"==\":[{\"var\":\"current_day\"},\"1\"]},{\"==\":[{\"var\":\"current_day\"},\"3\"]},{\"==\":[{\"var\":\"current_day\"},\"5\"]},{\"==\":[{\"var\":\"current_day\"},\"7\"]}]},{\"<=\":[\"0900\",{\"var\":\"current_time\"},\"1745\"]}]},{\"and\":[{\"or\":[{\"==\":[{\"var\":\"current_day\"},\"1\"]},{\"==\":[{\"var\":\"current_day\"},\"2\"]},{\"==\":[{\"var\":\"current_day\"},\"3\"]},{\"==\":[{\"var\":\"current_day\"},\"4\"]},{\"==\":[{\"var\":\"current_day\"},\"5\"]}]},{\"<=\":[\"0515\",{\"var\":\"current_time\"},\"1940\"]}]}]}]},{\"and\":[{\"==\":[{\"var\":\"user.browserType\"},\"firefox\"]},{\"and\":[{\">=\":[{\"var\":\"user.browserVersion\"},72]},{\"!=\":[{\"var\":\"user.browserType\"},\"ipad\"]}]}]},{\"and\":[{\"in\":[\"foo\",{\"var\":\"page.query\"}]},{\"==\":[\".jpg\",{\"substr\":[{\"var\":\"page.path\"},-4]}]},{\"==\":[\"ref1\",{\"var\":\"page.fragment_lc\"}]}]}]}]},\"consequence\":{\"mboxes\":[{\"options\":[{\"id\":632284,\"content\":{\"test\":true,\"experience\":\"b\"},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"IbG2Jz2xmHaqX7Ml/YRxRJNWHtnQtQrJfmRrQugEa2qCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"testoffer\"}]},\"meta\":{\"activityId\":334694,\"experienceId\":1,\"type\":\"ab\",\"mboxes\":[\"testoffer\"],\"views\":[]}},{\"condition\":{\"and\":[{\"<\":[50,{\"var\":\"allocation\"},100]},{\"and\":[{\"and\":[{\"==\":[\"bar\",{\"var\":\"mbox.foo\"}]},{\"==\":[\"buz\",{\"substr\":[{\"var\":\"mbox.baz_lc\"},0,3]}]}]},{\"and\":[{\"or\":[{\"<=\":[1582790400000,{\"var\":\"current_timestamp\"},4736217600000]}]},{\"or\":[{\"and\":[{\"or\":[{\"==\":[{\"var\":\"current_day\"},\"1\"]},{\"==\":[{\"var\":\"current_day\"},\"3\"]},{\"==\":[{\"var\":\"current_day\"},\"5\"]},{\"==\":[{\"var\":\"current_day\"},\"7\"]}]},{\"<=\":[\"0900\",{\"var\":\"current_time\"},\"1745\"]}]},{\"and\":[{\"or\":[{\"==\":[{\"var\":\"current_day\"},\"1\"]},{\"==\":[{\"var\":\"current_day\"},\"2\"]},{\"==\":[{\"var\":\"current_day\"},\"3\"]},{\"==\":[{\"var\":\"current_day\"},\"4\"]},{\"==\":[{\"var\":\"current_day\"},\"5\"]}]},{\"<=\":[\"0515\",{\"var\":\"current_time\"},\"1940\"]}]}]}]},{\"and\":[{\"==\":[{\"var\":\"user.browserType\"},\"firefox\"]},{\"and\":[{\">=\":[{\"var\":\"user.browserVersion\"},72]},{\"!=\":[{\"var\":\"user.browserType\"},\"ipad\"]}]}]},{\"and\":[{\"in\":[\"foo\",{\"var\":\"page.query\"}]},{\"==\":[\".jpg\",{\"substr\":[{\"var\":\"page.path\"},-4]}]},{\"==\":[\"ref1\",{\"var\":\"page.fragment_lc\"}]}]}]}]},\"consequence\":{\"mboxes\":[{\"options\":[{\"id\":632286,\"content\":{\"test\":true,\"offer\":2},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"IbG2Jz2xmHaqX7Ml/YRxRJNWHtnQtQrJfmRrQugEa2qCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"testoffer2\"}]},\"meta\":{\"activityId\":334694,\"experienceId\":1,\"type\":\"ab\",\"mboxes\":[\"testoffer2\"],\"views\":[]}}]}";
    static final String TEST_RULE_SET_NO_AUDIENCE = "{\"version\":\"1.0.0\",\"meta\":{\"generatedAt\":\"2020-02-27T23:27:26.445Z\"},\"rules\":[{\"condition\":{\"<\":[0,{\"var\":\"allocation\"},50]},\"consequence\":{\"mboxes\":[{\"options\":[{\"id\":632283,\"content\":{\"test\":true,\"experience\":\"a\"},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"IbG2Jz2xmHaqX7Ml\\/YRxRGqipfsIHvVzTQxHolz2IpSCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"testoffer\"}]},\"meta\":{\"activityId\":334694,\"experienceId\":0,\"type\":\"ab\",\"mboxes\":[\"testoffer\"],\"views\":[]}},{\"condition\":{\"<\":[0,{\"var\":\"allocation\"},50]},\"consequence\":{\"mboxes\":[{\"options\":[{\"id\":632285,\"content\":{\"test\":true,\"offer\":1},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"IbG2Jz2xmHaqX7Ml\\/YRxRGqipfsIHvVzTQxHolz2IpSCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"testoffer2\"}]},\"meta\":{\"activityId\":334694,\"experienceId\":0,\"type\":\"ab\",\"mboxes\":[\"testoffer2\"],\"views\":[]}},{\"condition\":{\"<\":[50,{\"var\":\"allocation\"},100]},\"consequence\":{\"mboxes\":[{\"options\":[{\"id\":632284,\"content\":{\"test\":true,\"experience\":\"b\"},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"IbG2Jz2xmHaqX7Ml\\/YRxRJNWHtnQtQrJfmRrQugEa2qCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"testoffer\"}]},\"meta\":{\"activityId\":334694,\"experienceId\":1,\"type\":\"ab\",\"mboxes\":[\"testoffer\"],\"views\":[]}},{\"condition\":{\"<\":[50,{\"var\":\"allocation\"},100]},\"consequence\":{\"mboxes\":[{\"options\":[{\"id\":632286,\"content\":{\"test\":true,\"offer\":2},\"type\":\"json\"}],\"metrics\":[{\"type\":\"display\",\"eventToken\":\"IbG2Jz2xmHaqX7Ml\\/YRxRJNWHtnQtQrJfmRrQugEa2qCnQ9Y9OaLL2gsdrWQTvE54PwSz67rmXWmSnkXpSSS2Q==\"}],\"name\":\"testoffer2\"}]},\"meta\":{\"activityId\":334694,\"experienceId\":1,\"type\":\"ab\",\"mboxes\":[\"testoffer2\"],\"views\":[]}}]}";

    @Mock
    private DefaultTargetHttpClient defaultTargetHttpClient;

    private TargetClient targetJavaClient;

    private LocalDecisioningService localService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void init() throws NoSuchFieldException {

        Mockito.lenient().doReturn(getTestDeliveryResponse())
                .when(defaultTargetHttpClient).execute(any(Map.class), any(String.class), any(DeliveryRequest.class),
                any(Class.class));

        ClientConfig clientConfig = ClientConfig.builder()
                .client("emeaprod4")
                .organizationId(TEST_ORG_ID)
                .build();

        DefaultTargetService targetService = new DefaultTargetService(clientConfig);
        localService = new LocalDecisioningService(clientConfig, targetService);

        targetJavaClient = TargetClient.create(clientConfig);

        FieldSetter.setField(targetService, targetService.getClass()
                .getDeclaredField("targetHttpClient"), defaultTargetHttpClient);
        FieldSetter.setField(targetJavaClient, targetJavaClient.getClass()
                .getDeclaredField("targetService"), targetService);
        FieldSetter.setField(targetJavaClient, targetJavaClient.getClass()
                .getDeclaredField("localService"), localService);

        RuleLoader testRuleLoader = TargetTestDeliveryRequestUtils.getTestRuleLoader(TEST_RULE_SET);
        FieldSetter.setField(localService, localService.getClass()
                .getDeclaredField("ruleLoader"), testRuleLoader);
        ParamsCollator specificTimeCollator = TargetTestDeliveryRequestUtils.getSpecificTimeCollator(1582818503000L);
        FieldSetter.setField(localService, localService.getClass()
                .getDeclaredField("timeCollator"), specificTimeCollator);
    }

    @Test
    void testTargetDeliveryAttributesVisitor1() {
        TargetDeliveryRequest targetDeliveryRequest = localDeliveryRequest("38734fba-262c-4722-b4a3-ac0a93916874");
        Attributes attrs = targetJavaClient.getAttributes(targetDeliveryRequest, "testoffer", "testoffer2");
        assertNotNull(attrs);
        assertNotNull(attrs.getResponse());
        assertEquals(1, attrs.getFeatureInteger("offer"));
        assertTrue(attrs.getFeatureBoolean("test"));
        assertEquals("a", attrs.getFeatureString("experience"));
        assertEquals("a", attrs.toMap().get("experience"));
    }

    @Test
    void testTargetDeliveryAttributesVisitor2() {
        TargetDeliveryRequest targetDeliveryRequest = localDeliveryRequest("38734fba-262c-4722-b4a3-ac0a93916873");
        Attributes attrs = targetJavaClient.getAttributes(targetDeliveryRequest, "testoffer", "testoffer2");
        assertNotNull(attrs);
        assertNotNull(attrs.getResponse());
        assertEquals(2, attrs.getFeatureInteger("offer"));
        assertTrue(attrs.getFeatureBoolean("test"));
        assertEquals("b", attrs.getFeatureString("experience"));
        assertEquals("b", attrs.toMap().get("experience"));
    }

    @Test
    void testTargetDeliveryAttributesNoRequest() throws NoSuchFieldException {
        RuleLoader testRuleLoader = TargetTestDeliveryRequestUtils.getTestRuleLoader(TEST_RULE_SET_NO_AUDIENCE);
        FieldSetter.setField(localService, localService.getClass()
                .getDeclaredField("ruleLoader"), testRuleLoader);
        Attributes attrs = targetJavaClient.getAttributes(null, "testoffer", "testoffer2");
        assertNotNull(attrs);
        assertNotNull(attrs.getResponse());
        assertTrue(attrs.getFeatureBoolean("test"));
        assertTrue(attrs.toMap().containsKey("offer"));
        assertTrue(attrs.toMap().containsKey("experience"));
    }

    private TargetDeliveryRequest localDeliveryRequest(String visitorIdStr) {
        Context context = getLocalContext();
        PrefetchRequest prefetchRequest = getMboxPrefetchLocalRequest();
        ExecuteRequest executeRequest = getMboxExecuteLocalRequest();
        VisitorId visitorId = new VisitorId().tntId(visitorIdStr);

        TargetDeliveryRequest targetDeliveryRequest = TargetDeliveryRequest.builder()
                .context(context)
                .prefetch(prefetchRequest)
                .execute(executeRequest)
                .id(visitorId)
                .executionMode(ExecutionMode.LOCAL)
                .build();

        assertEquals(prefetchRequest, targetDeliveryRequest.getDeliveryRequest().getPrefetch());
        assertEquals(executeRequest, targetDeliveryRequest.getDeliveryRequest().getExecute());
        assertEquals(context, targetDeliveryRequest.getDeliveryRequest().getContext());
        return targetDeliveryRequest;
    }

}
