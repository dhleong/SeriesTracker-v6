package stv6.sync;

public class TrackData {
	public final int seriesId, userId, episode;
	public final long lastView;
	
	public TrackData(int seriesId, int userId, int episode, long lastView) {
		this.seriesId = seriesId;
		this.userId = userId;
		this.episode = episode;
		this.lastView = lastView;
	}
}
