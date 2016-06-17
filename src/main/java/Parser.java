import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import representation.Podcast;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by vitorteixeira on 6/8/16.
 */

class CannotChangeConfig extends Exception{

}

public final class Parser {

    private static Map<String, Object> map;

    private static final XPath xPath =  XPathFactory.newInstance().newXPath();

    private Parser() {}

    public static void withYaml(FileReader yamlFile) throws CannotChangeConfig, YamlException {
        if (map != null) throw new CannotChangeConfig();

        final YamlReader reader = new YamlReader(yamlFile);

        map = (Map) reader.read();
    }

    protected static ArrayList getNodeByClass(Class nodeClass, HashMap config) {
        ArrayList node = (ArrayList) config.get(nodeClass.getSimpleName());
        return node;
    }

    public static List getRuleByField(String field, List<Map> fields) {

        for (Map<String, List> lfield: fields) {
            if (lfield.get(field) != null) {
                return lfield.get(field);
            }
        }

        return new ArrayList();
    }

    public static String getValueByRule(Map<String, String> rule, Document doc) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {

        String value = null;

        NodeList nodeList;

        if (rule.containsKey("path")) {
            String expression = rule.get("path");
            nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                value = nodeList.item(i).getTextContent();
            }
        }
        return value;
    }

    public static List<Object> parse(File xmlFile) throws ClassNotFoundException, ParserConfigurationException, IOException, SAXException, XPathExpressionException, IllegalAccessException, InstantiationException {

        ArrayList<Object> response = new ArrayList<Object>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        Iterator it = map.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Class<?> nodeClass = Class.forName((String) pair.getKey());

            Object node = nodeClass.newInstance();

            Field[] fields = nodeClass.getDeclaredFields();

            for (Field field: fields) {
                List rules = Parser.getRuleByField(field.getName(), (List<Map>) pair.getValue());
                for (Object rule: rules) {
                    String value = Parser.getValueByRule((Map<String, String>) rule, doc);
                    if (value != null) {
                        field.set(node, value);
                        continue;
                    }
                }
            }

            response.add(node);
            it.remove(); // avoids a ConcurrentModificationException
        }

        return response;
    }
}