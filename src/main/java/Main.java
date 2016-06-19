import com.esotericsoftware.yamlbeans.YamlException;
import exception.CannotChangeConfig;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by vitorteixeira on 6/6/16.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Rodandoam!! :D");
        FileReader yamlfile = null;
        try {
            URL filePath = Parser.class.getClassLoader().getResource("xmlconfig/parser.yml");
            if (filePath == null) {
                throw new FileNotFoundException();
            }
            yamlfile = new FileReader(filePath.getFile());
        } catch (FileNotFoundException e) {
            System.out.println("No config file found" + e.toString());
            System.exit(1);
        }

        try {
            Parser.withYaml(yamlfile);
        } catch (CannotChangeConfig cannotChangeConfig) {
            System.out.println("Config already set");
        } catch (YamlException e) {
            System.out.println("Failed to load config" + e.toString());
            System.exit(1);
        }

        try {
            URL url = new URL("https://jovemnerd.com.br/categoria/nerdcast/feed/");
            InputStream stream = url.openStream();
            List nodes = Parser.parse(stream);
            System.out.print(nodes);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

    }
}
