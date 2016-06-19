package representation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;

/**
 * Created by vitorteixeira on 6/7/16.
 */
public class Podcast extends Node {
    public String title;
    public String description;
}
