package org.emulinker.kaillera.master.client;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.logging.*;
import org.emulinker.kaillera.controller.connectcontroller.ConnectController;
import org.emulinker.kaillera.master.PublicServerInformation;
import org.emulinker.kaillera.model.KailleraGame;
import org.emulinker.kaillera.model.KailleraServer;
import org.emulinker.release.ReleaseInfo;
import org.emulinker.util.EmuUtil;

public class EmuLinkerMasterUpdateTask implements MasterListUpdateTask
{
	private static Log				log	= LogFactory.getLog(EmuLinkerMasterUpdateTask.class);
	private static final String		url	= "https://EmxKaillera.net/register_server.php"; // the new EmuLinker alternative list
	private static final String		wgUrl	= "https://EmxKaillera.net/register_game.php"; // the new EmuLinker alternative list

	private PublicServerInformation	publicInfo;
	private ConnectController		connectController;
	private KailleraServer			kailleraServer;
	private ReleaseInfo				releaseInfo;
	private HttpClient				httpClient;

	public EmuLinkerMasterUpdateTask(PublicServerInformation publicInfo, ConnectController connectController, KailleraServer kailleraServer, ReleaseInfo releaseInfo)
	{
		this.publicInfo = publicInfo;
		this.connectController = connectController;
		this.kailleraServer = kailleraServer;
		this.publicInfo = publicInfo;
		this.releaseInfo = releaseInfo;

		httpClient = new HttpClient();
		httpClient.setConnectionTimeout(5000);
		httpClient.setTimeout(5000);
	}

	public void touchMaster()
	{
		
		// Build Server Info Request
		
		StringBuilder buildServerInfo = new StringBuilder();
		buildServerInfo.append(url);
		buildServerInfo.append("?servername=" + publicInfo.getServerName());
		buildServerInfo.append("&ipaddress=" + publicInfo.getConnectAddress());
		buildServerInfo.append("&port=" + Integer.toString(connectController.getBindPort()));
		buildServerInfo.append("&nbusers=" + Integer.toString(kailleraServer.getNumUsers()));
		buildServerInfo.append("&maxconn=" + Integer.toString(kailleraServer.getMaxUsers()));
		buildServerInfo.append("&nbgames=" + Integer.toString(kailleraServer.getNumGames()));
		buildServerInfo.append("&version=" +"EMX" + releaseInfo.getVersionString());
		buildServerInfo.append("&location=" + publicInfo.getLocation());
		
		
		
		
		String serverInfo = buildServerInfo.toString();

		
		// Replace any spaces in the string with %20 so the request can be valid when sent to the server
		
		if (serverInfo.contains(" "))
			serverInfo = serverInfo.replace(" ", "%20");
		


		
		// Touch Master Server With Server Info
		try
		{
			URL mUrl = new URL(serverInfo);
			URLConnection conn = mUrl.openConnection();
            InputStream is = conn.getInputStream();
            is.close();
			
            log.info("Touching EmuLinker Master done");
            
			/* No longer in use
			int statusCode = httpClient.executeMethod(meth);
			if (statusCode != HttpStatus.SC_OK)
				log.error("Failed to touch EmuLinker Master: " + meth.getStatusLine());
			else
			{
				props.load(meth.getResponseBodyAsStream());
				log.info("Touching EmuLinker Master done");
			}
			*/
		}
		catch (Exception e)
		{
			log.error("Failed to touch EmuLinker Master Server: " + e.getMessage());
		}
		
		
		// Touch Master Server With All Waiting Games
		try 
		{
			// Build Waiting Games requests

			boolean atLeastOneWG = false; // flag to determine if there is at least one waiting game to touch to master server
			
			for (KailleraGame game : kailleraServer.getGames()) 
			{
				if (game.getStatus() != KailleraGame.STATUS_WAITING)
					continue;

				// the above if statement will "continue", aka break out of the current iteration of the for loop. If no waiting games are detected, this boolean will remain false
				atLeastOneWG = true;
				
				StringBuilder waitingGames = new StringBuilder();

				waitingGames.append(wgUrl);
				waitingGames.append("?game=" + game.getRomName());
				waitingGames.append("&ipaddress=" + publicInfo.getConnectAddress());
				waitingGames.append("&port=" + Integer.toString(connectController.getBindPort()));
				waitingGames.append("&user=" + game.getOwner().getName());
				waitingGames.append("&emulator=" + game.getOwner().getClientType());
				waitingGames.append("&nbusers=" + game.getNumPlayers());
				waitingGames.append("&maxusers=" + game.getMaxUsers());
				waitingGames.append("&servername=" + publicInfo.getServerName());
				waitingGames.append("&location=" + publicInfo.getLocation());

				String gameInfo = waitingGames.toString();

				if (gameInfo.contains(" "))
					gameInfo = gameInfo.replace(" ", "%20");

				URL mUrl = new URL(gameInfo);
				URLConnection conn = mUrl.openConnection();
				InputStream is = conn.getInputStream();
				is.close();

				waitingGames = null; // set to null for the GC to dispose of it from memory
				gameInfo = null; // set to null for the GC to dispose of it from memory
			}
			
			/* If at least one waiting game is detected, the program will attempt to send a request to the master server. If none is detected, 
			log to the log file that we are not sending data to the Master Server */
			if (atLeastOneWG)
				log.info("Touching EmuLinker Waiting Games done");
			else if (!atLeastOneWG)
				log.info("No waiting games detected. Not sending data to Master Server");

		} 
		catch (Exception e) 
		{
			log.error("Failed to touch EmuLinker Waiting Games List: " + e.getMessage());
		}
		
		/*
		Properties props = new Properties();
		String updateAvailable = props.getProperty("updateAvailable");
		if (updateAvailable != null && updateAvailable.equalsIgnoreCase("true"))
		{
			String latestVersion = props.getProperty("latest");
			String notes = props.getProperty("notes");
			StringBuilder sb = new StringBuilder();
			sb.append("A updated version of EmuLinker is available: ");
			sb.append(latestVersion);
			if (notes != null)
			{
				sb.append(" (");
				sb.append(notes);
				sb.append(")");
			}
			log.warn(sb.toString());
		}
		*/
	}
}