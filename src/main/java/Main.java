import app.Parser;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import config.XmlConfig;
import exception.CannotChangeConfig;
import model.Podcast;
import org.javalite.activejdbc.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runnable.PodcastProcessor;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vitorteixeira on 6/6/16.
 */
public class Main {
    final static Logger logger = LoggerFactory.getLogger(Main.class);
    final static Config conf = ConfigFactory.load();
    private static final int minutesFromLastCheck = conf.getInt("episode.update_interval");
    private static final int podcastsCountLimit = conf.getInt("podcast.count_limit");
    private static final int podcastMultiThreadCount = conf.getInt("podcast.multithread_count");
    private static final int podcastTimeToSleep = conf.getInt("podcast.time_to_sleep");

    public static void main(String[] args) {
        XmlConfig xmlConfig = null;
        try {

            InputStream in = Main.class.getClassLoader().getResourceAsStream("parser.yml");

            if (in == null) {
                throw new FileNotFoundException();
            }
            BufferedReader yamlfile = new BufferedReader(new InputStreamReader(in));
            YamlReader reader = new YamlReader(yamlfile);
            xmlConfig = reader.read(XmlConfig.class);
        } catch (FileNotFoundException e) {
            logger.error("No config file found" + e.getMessage());
            System.exit(1);
        } catch (YamlException e) {
            logger.error("Failed to load config" + e.getMessage());
            System.exit(1);
        }

        try {
            Parser.withConfig(xmlConfig);
        } catch (CannotChangeConfig cannotChangeConfig) {
            logger.info("Config already set");
        } catch (ParserConfigurationException e) {
            logger.error("Exception:", e);
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                logger.info("Shutdown hook ran!");
                new DB("default").open();
                new DB("default").close();
            }
        });

        new DB("default").open();

        ExecutorService executor;

        while(true) {

            List<Podcast> podcasts = new Podcast().toGetFromFeed(minutesFromLastCheck, podcastsCountLimit);
            if (podcasts.size() < 1) {
                logger.info("No podcast to process. Sleeping...");
                try {
                    Thread.sleep(podcastTimeToSleep * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            executor = Executors.newFixedThreadPool(podcastMultiThreadCount);
            for (int i = 0; i < podcasts.size(); i++) {
                Runnable worker = new PodcastProcessor(podcasts.get(i));
                executor.execute(worker);
            }

            executor.shutdown();

            while (!executor.isTerminated()) {
                logger.info("Waiting for threads to finish");
                try {
                    Thread.sleep(1* 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
