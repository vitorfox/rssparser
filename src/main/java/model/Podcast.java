package model;

import org.javalite.activejdbc.Model;

import java.util.List;

/**
 * Created by vitorteixeira on 6/24/16.
 */
public class Podcast extends Model{
    public List<Podcast> toGetFromFeed(int minutes, int limit) {
        String query = String.format("SELECT * from podcasts LEFT JOIN (select podcast_id, max(created_at) as created_at" +
                " from podcast_update_histories group by podcast_id) as h on podcasts.id = h.podcast_id" +
                " where h.created_at + '%d minutes'::interval < now() OR h.created_at IS NULL" +
                " ORDER BY h.created_at NULLS FIRST LIMIT %d", minutes, limit);
        List<Podcast> podcasts = Podcast.findBySQL(query);
        return podcasts;
    }
}
