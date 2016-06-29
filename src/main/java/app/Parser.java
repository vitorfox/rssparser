package app;

import config.XmlConfig;
import config.XmlConfigField;
import config.XmlConfigNode;
import config.XmlConfigRule;
import exception.CannotChangeConfig;
import exception.ValueNotFound;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vitorteixeira on 6/8/16.
 */


public final class Parser {

    private static XmlConfig xmlConfig;
    private static final XPath xPath = XPathFactory.newInstance().newXPath();
    private static final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder dBuilder;

    private Parser() {}

    public static void withConfig(XmlConfig lxmlConfig) throws CannotChangeConfig, ParserConfigurationException {
        if (xmlConfig != null) throw new CannotChangeConfig();
        xmlConfig = lxmlConfig;

        dBuilder = dbFactory.newDocumentBuilder();

        dBuilder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) throws SAXException {}

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }
        });
    }

    public static List<XmlConfigRule> getRulesByField(String field, List<XmlConfigField> fields) {

        for (int i = 0; i < fields.size(); i++) {
            if (field.equals(fields.get(i).name)) {
                return fields.get(i).rules;
            }
        }

        return new ArrayList();
    }

    public static String getValueByRule(XmlConfigRule rule, NodeList nodeList) throws ValueNotFound, ParserConfigurationException {

        try {
            String value = null;
            Node item = (Node) xPath.compile(rule.path).evaluate(nodeList, XPathConstants.NODE);
            if (item == null) {
                throw new Exception();
            }
            if (rule.attribute != null) {
                if (item.hasAttributes()) {
                    value = item.getAttributes().getNamedItem(rule.attribute).getNodeValue();
                }
            } else {
                value = item.getTextContent();
            }
            return value;
        } catch (Exception e) {
            throw new ValueNotFound();
        }
    }

    public static List<Representation> parse(InputStream xmlFile) throws ClassNotFoundException, ParserConfigurationException, IOException, SAXException, XPathExpressionException, IllegalAccessException, InstantiationException {

        List<Representation> response = new ArrayList<Representation>();

        Document doc = dBuilder.parse(xmlFile);

        for(int i = 0; i < xmlConfig.nodes.size(); i++) {
            XmlConfigNode node = xmlConfig.nodes.get(i);

            List<Representation> representationInstance = getInstances(doc, node);
            response.addAll(representationInstance);
        }

        return response;
    }

    public static List<Representation> getInstances(Document doc, XmlConfigNode node) throws IllegalAccessException, InstantiationException, XPathExpressionException {
        List<Representation> response = new ArrayList<>();
        final List<XmlConfigField> fields = node.fields;

        final NodeList nodeList = (NodeList) xPath.compile(node.basePath).evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            NodeList itemNodeList = (NodeList) nodeList.item(i);
            Representation representationInstance = new Representation(node.name);
            for (int j = 0; j < fields.size(); j++) {
                XmlConfigField field = fields.get(j);
                List<XmlConfigRule> rules = Parser.getRulesByField(field.name, node.fields);
                for (int k = 0; k < rules.size(); k++) {
                    XmlConfigRule rule = rules.get(k);
                    try {
                        String value = Parser.getValueByRule(rule, itemNodeList);
                        representationInstance.set(field.name, value);
                        break;
                    } catch (Exception e) {}
                }
            }
            response.add(representationInstance);
        }
        return response;
    }

}
