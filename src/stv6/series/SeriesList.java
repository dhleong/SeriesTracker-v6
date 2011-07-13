package stv6.series;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


public class SeriesList implements Iterable<Series> {
	private HashMap<String, Series> seriesByName;
	private HashMap<Integer, Series> seriesById;
	
	public SeriesList() {
		seriesByName = new HashMap<String, Series>();
		seriesById = new HashMap<Integer, Series>();
	}
	
	public void add(Series news) {
		add(news, true);
	}
	
	/**
	 * Note: Does not allow duplicates
	 * @param news
	 * @param overwrite If true, it will ovewrite any
	 * 	previous entry with the same name
	 */
	public void add(Series news, boolean overwrite) {
		// lower once and for all
		String lowerName = news.getName().toLowerCase();
		if (overwrite || !seriesByName.containsKey(lowerName)) {
			seriesByName.put(lowerName, news);
			seriesById.put(news.getId(), news);
		} else if (seriesByName.containsKey(lowerName) &&
				seriesByName.get(lowerName).getId() < 0) {
			seriesByName.get(lowerName).setId(news.getId());
			seriesById.put(news.getId(), news);
		}
	}
	
	public void clear() {
		seriesByName.clear();
		seriesById.clear();
	}

	public boolean contains(String name) {
		return seriesByName.containsKey(name.toLowerCase());
	}

	public boolean contains(int id) {
		return seriesById.containsKey(id);
	}
	
	public Collection<Series> managed() {
		Collection<Series> ret = new LinkedList<Series>();
		for (Series s : this)
			if (s.isManaged())
				ret.add(s);
		return ret;
	}

	@Override
	public Iterator<Series> iterator() {
		return sorted().iterator();//seriesByName.values().iterator();
	}

	public int size() {
		return seriesByName.size();
	}
	
	public Series getById(int id) {
		return seriesById.get(id);
	}

	public Series getByName(String name) {
		return seriesByName.get(name.toLowerCase());
	}

	public Series getByName(Series s) {
		return seriesByName.get( s.getName().toLowerCase() );
	}
	
	public void remove(Series s) {
		seriesByName.remove(s.getName().toLowerCase());
		seriesById.remove(s.getId());
	}

	/**
	 * Collection of series, sorted by name
	 * @return
	 */
	public Collection<Series> sorted() {
		ArrayList<Series> ret = new ArrayList<Series>();
		ret.addAll( seriesByName.values() );
		Collections.sort(ret);
		return ret;
	}
}
