package stv6.handlers.settings;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import stv6.Profile;
import stv6.STServer;
import stv6.handlers.AbstractHandler;
import stv6.http.request.Request;
import stv6.http.request.variables.ListVariable;
import stv6.http.request.variables.Variable;
import stv6.series.BasicSeries;
import stv6.series.Series;
import stv6.templating.Templator.Template;

public class ManageSaveHandler extends AbstractHandler {
	private final static String HANDLED_PAGE = "settings-manage-save";
	private final static String TEMPLATE_NAME = "settings.generic.tpl";

	private boolean anyNull(Object... values) {
		for (Object o : values) {
			if (o == null)
				return true;
		}
		
		return false;
	}
	
	@Override
	protected String getTemplatePath() {
		return TEMPLATE_NAME;
	}

	@Override
	protected boolean wrappedHandle(Request r, Template t) {
		// read in post vars
		r.readBody();		
		
		/* uids is a list of indexes into names/ids/inProfile which
		 * gives the series which have been chosen for the profile
		 */
		ListVariable uids = (ListVariable) r.getPostVars().get("accepted");
		ListVariable names = (ListVariable) r.getPostVars().get("names");
		ListVariable ids = (ListVariable) r.getPostVars().get("ids");
		ListVariable inProfile = (ListVariable) r.getPostVars().get("inProfile");
		
		// double check
		if (anyNull(uids, names, ids, inProfile) || 
				!(names.size() == ids.size() && ids.size() == inProfile.size())) {
			t.putVariable("body", "Incomplete data. Check template");
			return true;	
		}
		
		List<Series> toCreate = new LinkedList<Series>();
		List<Series> toInsert = new LinkedList<Series>();
		List<Series> toRemove = new LinkedList<Series>();
		
		Iterator<Variable> acceptedIter = uids.iterator();
		Variable curr; int currValue = -1, id = -1; Series newSeries = null;
		// loop through all series passed via HTTP
		for (int i=0; i<names.size(); i++) {
			// loop through the accepted items up into the current
			//  position in the global list (i)
			while (currValue < i && acceptedIter.hasNext()) {  			
				curr = acceptedIter.next();
				try {
					currValue = Integer.parseInt(curr.value);
					id = Integer.parseInt( ids.get(currValue).value );
					newSeries = new BasicSeries(id, names.get(currValue).value);
					
					// if it doesn't have a real id, add to "create" list
					if (id == BasicSeries.NO_ID) 
						toCreate.add( newSeries );				
					else if (inProfile.get(currValue).value.equals("false")) {
						// if it wasn't in the series already, add to "insert" list
						toInsert.add(newSeries);
					}	
					
					// make sure we move along (?)
					if (currValue == i)
						i++;
				} catch (NumberFormatException e) {
					t.putVariable("body", "Invalid data format. Check template");
					return true;
				} 
			}
			
			if (i != currValue && i < names.size() && 
					inProfile.get(i).value.equals("true")) {
				try {					
					// only try to remove it if it was actually in the profile
					id = Integer.parseInt( ids.get(i).value );
					toRemove.add(new BasicSeries(id, names.get(i).value));
										
				} catch (NumberFormatException e) {
					t.putVariable("body", "Invalid data format. Check template");
					return true;
				}
			}
		}
		
		Profile.getInstance().removeSeries(toRemove);
		boolean needsReload = false;
		needsReload = needsReload || Profile.getInstance().addExistingSeries(toInsert);		
		needsReload = needsReload || Profile.getInstance().addNewSeries(toCreate);
		
		if (needsReload) {
			t.putVariable("body", "Update successful! Please wait...");

			Profile.getInstance().reload();
		} else {
			t.putVariable("body", "Update successful! Redirecting you to the home screen");
			t.putVariable("homeLink", STServer.getHomeLink());	
		}	
		t.putVariable("refresh", "2;URL="+STServer.getHomeLink());	
		
		return true;
	}

	@Override
	public String getHandledPage() {
		return HANDLED_PAGE;
	}

}
