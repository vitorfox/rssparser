import app.Parser;
import app.Representation;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import config.XmlConfig;
import exception.CannotChangeConfig;
import model.Episode;
import model.Podcast;
import model.PodcastUpdateHistory;
import org.javalite.activejdbc.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
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

    static class PodcastProcessor implements Runnable {

        private Podcast podcast;

        public PodcastProcessor(Podcast podcast) {
            this.podcast = podcast;
        }
        @Override
        public void run() {

            logger.info("Processing " + podcast.getString("feed_url"));
            List<Representation> nodes = null;
            try {
                URL url = new URL(podcast.getString("feed_url"));
                InputStream stream = url.openStream();
                nodes = Parser.parse(stream);
            } catch (Exception e) {
                logger.error(e.getMessage());
                Thread.currentThread().interrupt();
            }

            new DB("default").open();
            List<Episode> dEpisodes = podcast.getAll(Episode.class);
            episode_loop:
            for (int j = 0; j < nodes.size(); j++) {
                Representation node = nodes.get(j);
                if (node.name.equals("Episode")) {
                    Episode episode = new Episode().fromMap(node.getMap());
                    episode.setPubDate(node.get("pub_date"));
                    episode.setInteger("file_size", node.get("file_size"));
                    episode.setDuration(node.get("duration"));
                    episode.setFileUrl(node.get("file_url"));
                    episode.setBigDecimal("podcast_id", podcast.getId());

                    for (int k = 0; k < dEpisodes.size(); k++) {
                        Episode dbEpisode = dEpisodes.get(k);
                        String urlNoScheme = "file_url_no_scheme";
                        if (dbEpisode.getString(urlNoScheme).equals(episode.getString(urlNoScheme))) {
                            String url = "file_url";
                            if (!dbEpisode.getString(url).equals(episode.getString(url))) {
                                dbEpisode.setFileUrl(episode.getString(url));
                                try {
                                    dbEpisode.saveIt();
                                }catch(Exception e) {
                                    logger.error(e.getMessage());
                                }
                            }
                            continue episode_loop;
                        }

                    }
                    try {
                        episode.saveIt();
                    }catch(Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }

            PodcastUpdateHistory updater = new PodcastUpdateHistory();
            updater.setBigDecimal("podcast_id", podcast.getId());
            try {
                updater.saveIt();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            new DB("default").close();
        }
    }

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

        while(true) {

            List<Podcast> podcasts = new Podcast().toGetFromFeed(minutesFromLastCheck, podcastsCountLimit);
            if (podcasts.size() < 1) {
                logger.info("No podcast to process. Sleeping...");
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            ExecutorService executor = Executors.newFixedThreadPool(podcastMultiThreadCount);
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
