import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import config.XmlConfig;
import exception.CannotChangeConfig;
import model.Episode;
import model.Podcast;
import org.javalite.activejdbc.Base;

import java.io.*;
import java.net.URL;
import java.time.Instant;
import java.util.List;

/**
 * Created by vitorteixeira on 6/6/16.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Rodandoam!! :D");
        XmlConfig xmlConfig = null;
        try {
            URL filePath = Parser.class.getClassLoader().getResource("xmlconfig/parser.yml");
            if (filePath == null) {
                throw new FileNotFoundException();
            }
            FileReader yamlfile = new FileReader(filePath.getFile());
            YamlReader reader = new YamlReader(yamlfile);
            xmlConfig = reader.read(XmlConfig.class);
        } catch (FileNotFoundException e) {
            System.out.println("No config file found" + e.toString());
            System.exit(1);
        } catch (YamlException e) {
            System.out.println("Failed to load config" + e.toString());
            System.exit(1);
        }

        try {
            Parser.withConfig(xmlConfig);
        } catch (CannotChangeConfig cannotChangeConfig) {
            System.out.println("Config already set");
        }

        //Load list of podcasts;
        Base.open("org.postgresql.Driver", "jdbc:postgresql://localhost/flycast_test", "postgres", "postgres");
        final int minutesFromLastCheck = 1;
        final int podcastsCountLimit = 10;
        List<Podcast> podcasts = new Podcast().toGetFromFeed(minutesFromLastCheck, podcastsCountLimit);
        Base.close();

        try {

            //URL url = new URL("https://jovemnerd.com.br/categoria/nerdcast/feed/");
            //InputStream stream = url.openStream();

            String xmlFileName = Parser.class.getClassLoader().getResource("jovemnerd.xml").getFile();
            FileInputStream stream = new FileInputStream(xmlFileName);

            Instant start = Instant.now();

            List nodes = Parser.parse(stream);

        } catch (Exception e) {
            //next podcast
        }

    }
}
