package com.jivesoftware.os.jive.utils.id;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ImmutableByteArrayMarshallerTest {

    @Test(dataProviderClass = ImmutableByteArrayTestDataProvider.class, dataProvider = "createString")
    public void testMarshaller(String string) throws Exception {
        ImmutableByteArray key1 = new ImmutableByteArray(string);

        System.out.println("before ImmutableByteArrayMarshaller: " + string);

        ImmutableByteArrayMarshaller marshaller = new ImmutableByteArrayMarshaller();
        byte[] bytes = marshaller.toBytes(key1);

        ImmutableByteArray key2 = marshaller.fromBytes(bytes);

        System.out.println("after ImmutableByteArrayMarshaller: toString()=" + key2.toString());

        Assert.assertTrue(key1.equals(key2), "marshalled object should be equal to the original");
    }

}
