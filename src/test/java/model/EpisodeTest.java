package model;

import org.javalite.activejdbc.DB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vitorteixeira on 6/28/16.
 */
public class EpisodeTest {

    @BeforeClass
    public static void before() {
        new DB("default").open();
    }

    @AfterClass
    public static void after() {
        new DB("default").close();
    }

    @Test
    public void testSetDuration() {
        Episode ep = new Episode();

        ep.setDuration("1:00:00");
        assertEquals(3600, ep.getLong("duration").longValue());

        ep = new Episode();
        ep.setDuration("1:00");
        assertEquals(60, ep.getLong("duration").longValue());

        ep = new Episode();
        ep.setDuration("1");
        assertEquals(1, ep.getLong("duration").longValue());
    }
}