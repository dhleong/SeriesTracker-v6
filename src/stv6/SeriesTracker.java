package stv6;

import java.io.IOException;

import stv6.handlers.BrowseHandler;
import stv6.handlers.FileHandler;
import stv6.handlers.IndexHandler;
import stv6.handlers.PluginHandler;
import stv6.handlers.ViewHandler;
import stv6.handlers.settings.ManageSaveHandler;
import stv6.handlers.settings.SeriesManageHandler;
import stv6.handlers.settings.SettingsListHandler;
import stv6.handlers.util.CoverHandler;
import stv6.handlers.util.ProfileSelectHandler;
import stv6.handlers.util.StaticMessageHandler;
import stv6.handlers.util.UserSelectHandler;
import stv6.http.HttpServer;

public class SeriesTracker {
	public static void main(String[] args) {
		// prepare profile
		Profile.getInstance().initialize(args);
		
		// prepare server
		HttpServer s = null;		
		try {
			s = STServer.getInstance( Profile.getInstance().getPort() );
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't start server");
			System.exit(1);
		}
		
		// instantiate handlers
		s.registerHandler(new IndexHandler());
		s.registerHandler(new BrowseHandler());
		s.registerHandler(new ViewHandler());
		s.registerHandler(new FileHandler());
		s.registerHandler(new PluginHandler());
		s.registerHandler(new CoverHandler());
		
		// settings handlers
		s.registerHandler(new SettingsListHandler());
		s.registerHandler(new SeriesManageHandler());
		s.registerHandler(new ManageSaveHandler());
		
		// utility handlers
		s.registerHandler(new UserSelectHandler()); // make sure we can select a user
		s.registerHandler(new ProfileSelectHandler()); // make sure we can select a profile
		s.registerHandler(StaticMessageHandler.getInstance());
		
		// TODO: possibly add a task to the server so it
		// 	periodically checks the synchronizer in its loop
		
		// start accepting connections (immediately)
		s.start();
		
		// start reloading stuff
		Profile.getInstance().reload();
	}
}
