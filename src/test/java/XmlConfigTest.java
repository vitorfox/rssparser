import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import config.XmlConfig;
import config.XmlConfigField;
import config.XmlConfigNode;
import config.XmlConfigRule;
import exception.CannotChangeConfig;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by vitorteixeira on 6/21/16.
 */
public class XmlConfigTest {

    private static XmlConfig xmlConfig;

    @BeforeClass
    public static void setUp() throws YamlException, CannotChangeConfig, FileNotFoundException {
        final FileReader yamlfile = new FileReader(XmlConfigTest.class.getClassLoader().getResource("xmlconfig/xmlconfig.yml").getFile());
        final YamlReader reader = new YamlReader(yamlfile);
        xmlConfig = reader.read(XmlConfig.class);
    }

    @Test
    public void testGetNodeClassName() {
        assertEquals("representation.Item", xmlConfig.nodes.get(0).className);
    }

    @Test
    public void testGetFieldList() {
        XmlConfigNode xmlConfigNode = xmlConfig.nodes.get(0);
        assertEquals("field1", xmlConfigNode.fields.get(0).name);
    }

    @Test
    public void testGetRuleList() {
        XmlConfigNode xmlConfigNode = xmlConfig.nodes.get(0);
        XmlConfigField xmlConfigField = xmlConfigNode.fields.get(1);
        XmlConfigRule actual = xmlConfigField.rules.get(0);
        assertEquals("description", actual.path);
        assertEquals("value", actual.attribute);
    }

}