import com.esotericsoftware.yamlbeans.YamlException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

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

    }
}
