package runnable;

import app.Parser;
import app.Representation;
import model.Episode;
import model.Podcast;
import model.PodcastUpdateHistory;
import org.javalite.activejdbc.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by vitorteixeira on 6/29/16.
 */
public class PodcastProcessor implements Runnable {

    final static Logger logger = LoggerFactory.getLogger(PodcastProcessor.class);

    private Podcast podcast;

    private void saveUpdater() {
        PodcastUpdateHistory updater = new PodcastUpdateHistory();
        updater.setBigDecimal("podcast_id", podcast.getId());
        try {
            updater.saveIt();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public PodcastProcessor(Podcast podcast) {
        this.podcast = podcast;
    }

    @Override
    public void run() {

        logger.debug("Processing " + podcast.getString("feed_url"));
        List<Representation> nodes = null;
        try {
            URL url = new URL(podcast.getString("feed_url"));
            InputStream stream = url.openStream();
            nodes = Parser.parse(stream);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        new DB("default").open();

        if (nodes != null ) {
            try {
                processEpisodes(nodes);
            } catch (Exception e) {
                logger.error("Trying to process " + podcast.getString("feed_url"));
                e.printStackTrace();
            }
        }

        saveUpdater();

        new DB("default").close();
    }

    private void processEpisodes(List<Representation> nodes) {

        List<Episode> dEpisodes = podcast.getAll(Episode.class);
        episode_loop:
        for (int j = 0; j < nodes.size(); j++) {
            try {
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
                                } catch (Exception e) {
                                    logger.error(e.getMessage());
                                }
                            }
                            continue episode_loop;
                        }

                    }
                    try {
                        episode.saveIt();
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

    }

}
