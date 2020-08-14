package ut.tools.fullstack.bamboo;

import org.junit.Test;
import tools.fullstack.bamboo.api.MyPluginComponent;
import tools.fullstack.bamboo.impl.MyPluginComponentImpl;

public class MyComponentUnitTest {
    @Test
    public void testMyName() {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent", component.getName());
    }
}
