package org.codehaus.enunciate.modules.amf;

import junit.framework.TestCase;

public class TestAMFMapperIntrospector extends TestCase {

    public void testGetAMFMapperPerformance() {
        // prime the pump for the cache
        AMFMapperIntrospector.getAMFMapper(Long.class, long.class);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 200; i++) {
            AMFMapperIntrospector.getAMFMapper(Long.class, long.class);
        }

        long duration = System.currentTimeMillis() - start;
        assertTrue("Took too long to get mapper that should have been cache. duration " + duration, duration < 2000);
    }
}
