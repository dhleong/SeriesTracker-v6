package stv6.series;

import java.util.ArrayList;

import stv6.episodes.BasicEpisode;
import stv6.templating.TemplateObject;

public class RecentSeries implements Series, TemplateObject {
    
    final Series mBase;
    
    public RecentSeries(Series s) {
        mBase = s;
    }

    @Override
    public int compareTo(Series arg0) {
        return mBase.compareTo(arg0);
    }

    @Override
    public String getClassName() {
        return "recentseries";
    }

    @Override
    public int getId() {
        return mBase.getId();
    }

    @Override
    public String getLink() {
        return mBase.getLink();
    }

    @Override
    public String getName() {
        return mBase.getName();
    }

    @Override
    public boolean isManaged() {
        return mBase.isManaged();
    }

    @Override
    public void manageify(String localPath, ArrayList<BasicEpisode> eps) {
        mBase.manageify(localPath, eps);
    }

    @Override
    public void setId(int id) {
        mBase.setId(id);
    }

}
