import com.esotericsoftware.yamlbeans.YamlException;
import exception.CannotChangeConfig;
import exception.ValueNotFound;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by vitorteixeira on 6/8/16.
 */
public class ParserTest {

    private static HashMap nodes;
    private static final List<Map> fields = new ArrayList();
    private static final String xmlFileName = Parser.class.getClassLoader().getResource("test.xml").getFile();

    @BeforeClass
    public static void setUp() throws YamlException, CannotChangeConfig, FileNotFoundException {
        final FileReader yamlfile = new FileReader(Parser.class.getClassLoader().getResource("xmlconfig/test.yml").getFile());
        Parser.withYaml(yamlfile);

        final Map<String, String> rule = new HashMap();
        rule.put("path", "/test/test");

        List rules = new ArrayList();
        rules.add(rule);

        final Map<String, List> field1 = new HashMap();
        field1.put("field1", rules);

        final Map<String, List> field2 = new HashMap();
        field2.put("field2", rules);

        fields.add(field1);
        fields.add(field2);

        nodes = new HashMap();
        nodes.put("Test", fields);
    }

    @Test
    public void testGetHashMapByClassName() {
        final List test = Parser.getNodeByClass(representation.Test.class, nodes);
        assertTrue(((HashMap) test.get(0)).containsKey("field1"));
    }

    @Test
    public void testGetRulesForFields() {
        final List test = Parser.getRuleByField("field1", fields);
        assertTrue(((Map) test.get(0)).containsKey("path"));
    }

    @Test
    public void testGetXmlValueByPathRule() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException, ValueNotFound {
        final Map<String, String> rule = new HashMap();
        rule.put("path", "/rss/channel/title");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(new FileInputStream(xmlFileName));
        doc.getDocumentElement().normalize();

        final String test = Parser.getValueByRule(rule, doc);
        assertEquals("TitleTest", test);
    }

    @Test
    public void testGetXmlValueByAttributeRule() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException, ValueNotFound {
        final Map<String, String> rule = new HashMap();
        rule.put("path", "/rss/channel/description");
        rule.put("attribute", "value");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(new FileInputStream(xmlFileName));
        doc.getDocumentElement().normalize();

        final String test = Parser.getValueByRule(rule, doc);
        assertEquals("DescriptionInAttribute", test);
    }

    @Test(expected=ValueNotFound.class)
    public void testThrowsValueNotFoundGetValueByRule() throws ParserConfigurationException, IOException, XPathExpressionException, SAXException, ValueNotFound {
        final Map<String, String> rule = new HashMap();
        rule.put("path", "/rss/channel/bla");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(new FileInputStream(xmlFileName));
        doc.getDocumentElement().normalize();

        final String test = Parser.getValueByRule(rule, doc);
    }

    @Test
    public void testParse() throws ClassNotFoundException, SAXException, IllegalAccessException, IOException, XPathExpressionException, InstantiationException, ParserConfigurationException {
        List<Object> list = Parser.parse(new FileInputStream(xmlFileName));
        assertNotNull(list);
    }
}