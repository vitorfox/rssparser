package model;

import org.javalite.activejdbc.Model;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by vitorteixeira on 6/20/16.
 */
public class Episode extends Model{
    public void setFileUrl(String fileUrl) {
        set("file_url", fileUrl);
        try {
            URL sameUrl = new URL(fileUrl);
            fileUrl.replaceFirst(sameUrl.getProtocol(), "");
            String fileUrlNoSchema = fileUrl.replaceFirst(sameUrl.getProtocol() + "://", "");
            set("file_url_no_scheme", fileUrlNoSchema);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    public void setDuration(String duration) {
        char[] format = "00:00:00".toCharArray();

        for (int i = 0; i < duration.length(); i++){
            format[format.length - 1 -i] = duration.charAt(duration.length() - 1 -i);
        }
        try {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = null;
            date = dateFormat.parse(new String(format));
            long seconds = date.getTime() / 1000L;
            set("duration", seconds);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setPubDate(String pubDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss Z");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = null;
            date = dateFormat.parse(pubDate);
            setDate("pub_date", date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
