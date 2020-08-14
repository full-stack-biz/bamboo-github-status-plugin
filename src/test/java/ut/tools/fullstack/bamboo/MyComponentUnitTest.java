package ut.tools.fullstack.bamboo;

import org.junit.Test;
import tools.fullstackbiz.bamboo.api.MyPluginComponent;
import tools.fullstackbiz.bamboo.impl.MyPluginComponentImpl;

import static junit.framework.Assert.assertEquals;

public class MyComponentUnitTest {
    @Test
    public void testMyName() {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent", component.getName());
    }
}
