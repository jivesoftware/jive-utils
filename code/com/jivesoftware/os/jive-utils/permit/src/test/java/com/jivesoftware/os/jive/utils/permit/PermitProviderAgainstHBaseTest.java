/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.permit.PermitProviderImplInitializer.PermitProviderConfig;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.inmemory.InMemorySetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.tests.EmbeddedHBase;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.merlin.config.BindInterfaceToConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan
 */
public class PermitProviderAgainstHBaseTest {

    EmbeddedHBase embeddedHBase;
    PermitProvider permitProvider;

    @BeforeMethod
    public void setup() throws Exception {

        if (permitProvider == null) {
            embeddedHBase = new EmbeddedHBase();
            //embeddedHBase.start(false);

            SetOfSortedMapsImplInitializer sos = new InMemorySetOfSortedMapsImplInitializer();
            //SetOfSortedMapsImplInitializer sos = new HBaseSetOfSortedMapsImplInitializer(embeddedHBase.getConfiguration());

            PermitProviderConfig config = BindInterfaceToConfiguration.bindDefault(PermitProviderConfig.class);
            permitProvider = new PermitProviderImplInitializer().initPermitProvider(config, sos);
        }
    }

    @AfterMethod
    public void teardown() throws Exception {
        //embeddedHBase.stop();
    }

    @Test
    public void basicTest() throws IOException, InterruptedException {

        ConstantPermitConfig permitConfig = new ConstantPermitConfig(0, 1, 100);
        int numberOfActivePermitHolders = permitProvider.getNumberOfActivePermitHolders("tenant", "group", permitConfig);
        Assert.assertEquals(0, numberOfActivePermitHolders);

        List<Permit> got = permitProvider.requestPermit("tenant", "group", permitConfig, 1);
        Assert.assertEquals(got.size(), 1);

        List<Permit> empty = permitProvider.requestPermit("tenant", "group", permitConfig, 1);
        Assert.assertEquals(empty.size(), 0);

        List<Optional<Permit>> renewed = permitProvider.renewPermit(Arrays.asList(got.get(0)));
        Assert.assertEquals(got.size(), 1);
        Assert.assertTrue(renewed.get(0).isPresent());

        numberOfActivePermitHolders = permitProvider.getNumberOfActivePermitHolders("tenant", "group", permitConfig);
        Assert.assertEquals(1, numberOfActivePermitHolders);

        permitProvider.releasePermit(Arrays.asList(renewed.get(0).get()));

        numberOfActivePermitHolders = permitProvider.getNumberOfActivePermitHolders("tenant", "group", permitConfig);
        Assert.assertEquals(0, numberOfActivePermitHolders);

        got = permitProvider.requestPermit("tenant", "group", permitConfig, 1);
        Assert.assertEquals(got.size(), 1);

        Thread.sleep(500); // lets permit expireI

        renewed = permitProvider.renewPermit(Arrays.asList(got.get(0)));
        Assert.assertEquals(got.size(), 1);
        Assert.assertFalse(renewed.get(0).isPresent());

        got = permitProvider.requestPermit("tenant", "group", permitConfig, 1);
        Assert.assertEquals(got.size(), 1);
    }
}
