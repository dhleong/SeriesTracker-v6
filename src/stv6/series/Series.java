package stv6.series;

import java.util.ArrayList;

import stv6.episodes.BasicEpisode;
import stv6.templating.TemplateObject;

public interface Series extends Comparable<Series>, TemplateObject {

	public static final String CLASS_NAME = "series";

	public abstract String getClassName();

	public abstract int getId();

	/**
	 * @return The HTTP path to "browse" this series
	 */
	public abstract String getLink();

	public abstract String getName();

	public abstract boolean isManaged();

	public abstract void manageify(String localPath, ArrayList<BasicEpisode> eps);

	public abstract void setId(int id);

}