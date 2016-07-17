import app.Parser;
import app.Representation;
import com.esotericsoftware.yamlbeans.YamlReader;
import config.XmlConfig;
import config.XmlConfigNode;
import config.XmlConfigRule;
import exception.CannotChangeConfig;
import exception.ValueNotFound;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by vitorteixeira on 6/8/16.
 */
public class ParserTest {

    private static final String xmlFileName = Parser.class.getClassLoader().getResource("test.xml").getFile();
    private static XmlConfig xmlConfig;
    private static Document doc;
    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    @BeforeClass
    public static void setUp() throws IOException, CannotChangeConfig, ParserConfigurationException, SAXException {
        final FileReader yamlfile = new FileReader(Parser.class.getClassLoader().getResource("xmlconfig/xmlconfig.yml").getFile());
        final YamlReader reader = new YamlReader(yamlfile);
        xmlConfig = reader.read(XmlConfig.class);
        Parser.withConfig(xmlConfig);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();

        doc = dBuilder.parse(new FileInputStream(xmlFileName));
        doc.getDocumentElement().normalize();
    }

    @Test
    public void testGetRulesForFields() {
        final List<XmlConfigRule> rules = Parser.getRulesByField("field1", xmlConfig.nodes.get(0).fields);
        assertEquals("title", rules.get(0).path);
    }

    @Test
    public void testGetXmlValueByPathRule() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException, ValueNotFound {
        final XmlConfigNode node = xmlConfig.nodes.get(0);
        final XmlConfigRule rule = node.fields.get(0).rules.get(0);
        final NodeList nodeList = (NodeList) xPath.compile(node.basePath).evaluate(doc, XPathConstants.NODESET);
        final String test = Parser.getValueByRule(rule, nodeList.item(0).getChildNodes());
        assertEquals("TitleTest", test);
    }

    @Test
    public void testGetXmlValueByAttributeRule() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException, ValueNotFound {
        final XmlConfigNode node = xmlConfig.nodes.get(0);
        final XmlConfigRule rule = node.fields.get(1).rules.get(0);
        final NodeList nodeList = (NodeList) xPath.compile(node.basePath).evaluate(doc, XPathConstants.NODESET);
        final String test = Parser.getValueByRule(rule, nodeList.item(0).getChildNodes());
        assertEquals("DescriptionInAttribute", test);
    }

    @Test(expected=ValueNotFound.class)
    public void testThrowsValueNotFoundGetValueByRule() throws ParserConfigurationException, IOException, XPathExpressionException, SAXException, ValueNotFound {
        final XmlConfigRule rule = new XmlConfigRule();
        rule.path = "/bla";

        final NodeList nodeList = (NodeList) xPath.compile("/rss/channel").evaluate(doc, XPathConstants.NODESET);
        final String test = Parser.getValueByRule(rule, nodeList.item(0).getChildNodes());
    }


    @Test(expected=ValueNotFound.class)
    public void testThrowsValueNotFoundWhenHasPathButNoAttr() throws ParserConfigurationException, IOException, XPathExpressionException, SAXException, ValueNotFound {
        final XmlConfigRule rule = new XmlConfigRule();
        rule.path = "./lastBuildDate";
        rule.attribute = "test";

        final NodeList nodeList = (NodeList) xPath.compile("/rss/channel").evaluate(doc, XPathConstants.NODESET);
        final String test = Parser.getValueByRule(rule, nodeList.item(0).getChildNodes());
    }

    @Test(expected=ValueNotFound.class)
    public void testThrowsValueNotFoundWhenHasPathButNoSpecifiedAttr() throws ParserConfigurationException, IOException, XPathExpressionException, SAXException, ValueNotFound {
        final XmlConfigRule rule = new XmlConfigRule();
        rule.path = "./testPathWithAttrs";
        rule.attribute = "test";

        final NodeList nodeList = (NodeList) xPath.compile("/rss/channel").evaluate(doc, XPathConstants.NODESET);
        final String test = Parser.getValueByRule(rule, nodeList.item(0).getChildNodes());
    }

    @Test
    public void testGetInstances() throws XPathExpressionException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        final XmlConfigNode node = xmlConfig.nodes.get(0);
        List<Representation> instances = Parser.getInstances(doc, node);
        assertEquals(Representation.class, instances.get(0).getClass());
    }

    @Test
    public void testParse() throws ClassNotFoundException, SAXException, IllegalAccessException, IOException, XPathExpressionException, InstantiationException, ParserConfigurationException {
        List<Representation> list = Parser.parse(new FileInputStream(xmlFileName));
        assertEquals("TitleTest", list.get(0).get("field1"));
        assertEquals("Item1", list.get(1).get("field1"));
    }
}