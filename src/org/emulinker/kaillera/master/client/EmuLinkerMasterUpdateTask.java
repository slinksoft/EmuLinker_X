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
	private static final String		url	= "http://localhost/register_server.php"; // the new EmuLinker alternative list
	private static final String		wgUrl	= "http://localhost/register_game.php"; // the new EmuLinker alternative list

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
		
		
		// Build Waiting Games request
		
		StringBuilder waitingGames = new StringBuilder();
		for(KailleraGame game : kailleraServer.getGames())
		{
			if (game.getStatus() != KailleraGame.STATUS_WAITING)
				continue;

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
		}
		
		String serverInfo = buildServerInfo.toString();
		String gameInfo = waitingGames.toString();
		
		// Replace any spaces in the string with %20 so the request can be valid when sent to the server
		
		if (serverInfo.contains(" "))
			serverInfo = serverInfo.replace(" ", "%20");
		
		if (gameInfo.contains(" "))
			gameInfo = gameInfo.replace(" ", "%20");

		
		// Server Info
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
			log.error("Failed to touch EmuLinker Waiting Games: " + e.getMessage());
		}
		
		// Waiting Games
		
		if (gameInfo != null && !gameInfo.trim().isEmpty())
		{
			try
			{
				URL mUrl = new URL(gameInfo);
				URLConnection conn = mUrl.openConnection();
	            InputStream is = conn.getInputStream();
	            is.close();
				
	            log.info("Touching EmuLinker Waiting Games done");
				/*
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
				log.error("Failed to touch Waiting Games Master Server: " + e.getMessage());
			}
		}
		else
		{
			log.info("Detected No Waiting Games; Not sending data to master server");
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