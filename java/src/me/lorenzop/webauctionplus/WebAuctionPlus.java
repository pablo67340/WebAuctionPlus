package me.lorenzop.webauctionplus;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import me.lorenzop.webauctionplus.listeners.WebAuctionBlockListener;
import me.lorenzop.webauctionplus.listeners.WebAuctionCommands;
import me.lorenzop.webauctionplus.listeners.WebAuctionPlayerListener;
import me.lorenzop.webauctionplus.listeners.failPlayerListener;
import me.lorenzop.webauctionplus.mysql.DataQueries;
import me.lorenzop.webauctionplus.mysql.MySQLTables;
import me.lorenzop.webauctionplus.mysql.MySQLUpdate;
import me.lorenzop.webauctionplus.tasks.AnnouncerTask;
import me.lorenzop.webauctionplus.tasks.PlayerAlertTask;
import me.lorenzop.webauctionplus.tasks.RecentSignTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class WebAuctionPlus extends JavaPlugin {

	private static volatile WebAuctionPlus instance = null;
	private static volatile boolean isOk    = false;

	private static final String loggerPrefix  = "[WebAuctionPlus] ";
	public static final String chatPrefix = ChatColor.DARK_GREEN+"["+ChatColor.WHITE+"WebAuctionPlus+"+ChatColor.DARK_GREEN+"] ";

	private static volatile Plugins3rdParty plugins3rd = null;

	public static pxnMetrics metrics;

	// plugin version
	public static String currentVersion = null;
	public static String newVersion = null;
	public static boolean newVersionAvailable = false;

	// config
	public FileConfiguration config = null;
	public static waSettings settings = null;

	// language
	public static volatile Language Lang;

	public static volatile DataQueries dataQueries = null;
	public volatile WebAuctionCommands WebAuctionCommandsListener = new WebAuctionCommands(this);

	public Map<String,   Long>    lastSignUse = new HashMap<String , Long>();
	public Map<Location, Integer> recentSigns = new HashMap<Location, Integer>();
	public Map<Location, Integer> shoutSigns  = new HashMap<Location, Integer>();

	public int signDelay			= 0;
	public int numberOfRecentLink	= 0;

	// use recent signs
	private static boolean useOriginalRecent = false;
	//	// sign link
	//	private static boolean useSignLink = false;
	// tim the enchanter
	private static boolean timEnabled = false;
	// globally announce new auctions (vs using shout signs)
	private static boolean announceGlobal = false;

	// JSON Server
	//	public waJSONServer jsonServer;

	// recent sign task
	public static RecentSignTask recentSignTask = null;

	// announcer
	public AnnouncerTask waAnnouncerTask = null;
	public boolean announcerEnabled	= false;


	public WebAuctionPlus() {
	}


	public void onEnable() {
		if(isOk) {
			getServer().getConsoleSender().sendMessage(ChatColor.RED+"********************************************");
			getServer().getConsoleSender().sendMessage(ChatColor.RED+"*** WebAuctionPlus is already running!!! ***");
			getServer().getConsoleSender().sendMessage(ChatColor.RED+"********************************************");
			return;
		}
		instance = this;
		isOk = false;
		failMsg = null;
		currentVersion = getDescription().getVersion();

		// 3rd party plugins
		if(plugins3rd == null)
			plugins3rd = new Plugins3rdParty(getLog());

		// Command listener
		getCommand("rm").setExecutor(WebAuctionCommandsListener);

		// load config.yml
		if(!onLoadConfig())
			return;

		// load more services
		onLoadMetrics();
		checkUpdateAvailable();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new WebAuctionPlayerListener(this), this);
		pm.registerEvents(new WebAuctionBlockListener (this), this);
		isOk = true;
	}


	public void onDisable() {
		isOk = false;
		failPlayerListener.stop();
		// unregister listeners
		HandlerList.unregisterAll(this);
		// stop schedulers
		try {
			getServer().getScheduler().cancelTasks(this);
		} catch (Exception ignore) {}
		if(waAnnouncerTask != null) waAnnouncerTask.clearMessages();
		if(shoutSigns      != null) shoutSigns.clear();
		if(recentSigns     != null) recentSigns.clear();
		// close inventories
		try {
			WebInventory.ForceCloseAll();
		} catch (Exception ignore) {}
		// close mysql connection
		try {
			if(dataQueries != null)
				dataQueries.forceCloseConnections();
		} catch (Exception ignore) {}
		// close config
		try {
			config = null;
		} catch (Exception ignore) {}
		settings = null;
		Lang = null;
		getLog().info("Disabled, bye for now :-)");
	}


	public void onReload() {
		failMsg = null;
		onDisable();
		// load config.yml
		if(!onLoadConfig()) return;
		isOk = true;
	}


	public static WebAuctionPlus getPlugin() {
		return instance;
	}
	public static boolean isOk()    {return isOk;}
	//	public static boolean isDebug() {return isDebug;}


	/**
	 * 3rd party plugins
	 */
	public static Plugins3rdParty getPlugins() {
		return plugins3rd;
	}


	/**
	 * Logger bootstrap
	 */
	private static volatile logBoots bLog = null;
	private static final Object logLock = new Object();
	public static logBoots getLog() {
		if(bLog == null) {
			synchronized(logLock) {
				if(bLog == null)
					bLog = new logBoots(getPlugin(), loggerPrefix);
			}
		}
		return bLog;
	}


	private static volatile String failMsg = null;
	public static void fail(String msg) {
		if(msg != null && !msg.isEmpty()) {
			getLog().severe(msg);
			if(failMsg == null || failMsg.isEmpty())
				failMsg = msg;
			else
				failMsg += "|"+msg;
		}
		{
			final JavaPlugin plugin = getPlugin();
			plugin.onDisable();
			failPlayerListener.start(plugin);
		}
	}
	public static String getFailMsg() {
		if(failMsg == null || failMsg.isEmpty())
			return null;
		return failMsg;
	}


	public boolean onLoadConfig() {
		// init configs
		if(config != null) config = null;
		config = getConfig();
		configDefaults();

		// connect MySQL
		if(dataQueries == null)
			if(!ConnectDB())
				return false;

		// load stats class


		// load settings from db
		if(settings != null) settings = null;
		settings = new waSettings(this);
		settings.LoadSettings();
		if(!settings.isOk()) {
			fail("Failed to load settings from database.");
			return false;
		}

		// update the version in db
		if(! currentVersion.equals(settings.getString("Version")) ){
			String oldVersion = settings.getString("Version");
			// update database
			MySQLUpdate.doUpdate(oldVersion);
			// update version number
			settings.setString("Version", currentVersion);
			getLog().info("Updated version from "+oldVersion+" to "+currentVersion);
		}

		// load language file
		if(Lang != null) Lang = null;
		Lang = new Language(this);
		Lang.loadLanguage(settings.getString("Language"));
		if(!Lang.isOk()) {
			fail("Failed to load language file.");
			return false;
		}

		try {
			if(config.getBoolean("Development.Debug"))
				getLog().setDebug();
			//			addComment("debug_mode", Arrays.asList("# This is where you enable debug mode"))
			signDelay          = config.getInt    ("Misc.SignClickDelay");
			timEnabled         = config.getBoolean("Misc.UnsafeEnchantments");
			announceGlobal     = config.getBoolean("Misc.AnnounceGlobally");
			//			numberOfRecentLink = config.getInt    ("SignLink.NumberOfLatestAuctionsToTrack");
			//			useSignLink        = config.getBoolean("SignLink.Enabled");
			//			if(useSignLink && !plugins3rd.isLoaded_SignLink()) {
			//				getLog().warning("SignLink is found but plugin is not loaded!");
			//				useSignLink = false;
			//			}

			// scheduled tasks
			BukkitScheduler scheduler = Bukkit.getScheduler();
			boolean UseMultithreads = config.getBoolean("Development.UseMultithreads");

			// announcer
			announcerEnabled = config.getBoolean("Announcer.Enabled");
			long announcerMinutes = 20 * 60 * config.getLong("Tasks.AnnouncerMinutes");
			if(announcerEnabled) waAnnouncerTask = new AnnouncerTask(this);
			if (announcerEnabled && announcerMinutes>0) {
				if(announcerMinutes < 6000) announcerMinutes = 6000; // minimum 5 minutes
				waAnnouncerTask.chatPrefix     = config.getString ("Announcer.Prefix");
				waAnnouncerTask.announceRandom = config.getBoolean("Announcer.Random");
				waAnnouncerTask.addMessages(     config.getStringList("Announcements"));
				scheduler.runTaskTimerAsynchronously(this, waAnnouncerTask,
						(announcerMinutes/2), announcerMinutes);
				getLog().info("Enabled Task: Announcer (always multi-threaded)");
			}

			long saleAlertSeconds        = 20 * config.getLong("Tasks.SaleAlertSeconds");
			long shoutSignUpdateSeconds  = 20 * config.getLong("Tasks.ShoutSignUpdateSeconds");
			long recentSignUpdateSeconds = 20 * config.getLong("Tasks.RecentSignUpdateSeconds");
			useOriginalRecent            =      config.getBoolean("Misc.UseOriginalRecentSigns");

			// Build shoutSigns map
			if (shoutSignUpdateSeconds > 0)
				shoutSigns.putAll(dataQueries.getShoutSignLocations());
			// Build recentSigns map
			if (recentSignUpdateSeconds > 0)
				recentSigns.putAll(dataQueries.getRecentSignLocations());

			// report sales to players (always multi-threaded)
			if (saleAlertSeconds > 0) {
				if(saleAlertSeconds < 3*20) saleAlertSeconds = 3*20;
				scheduler.runTaskTimerAsynchronously(this, new PlayerAlertTask(),
						saleAlertSeconds, saleAlertSeconds);
				getLog().info("Enabled Task: Sale Alert (always multi-threaded)");
			}
			// shout sign task
			
			// update recent signs
			if(recentSignUpdateSeconds > 0 && useOriginalRecent) {
				recentSignTask = new RecentSignTask(this);
				if (UseMultithreads)
					scheduler.runTaskTimerAsynchronously(this, recentSignTask,
							5*20, recentSignUpdateSeconds);
				else
					scheduler.scheduleSyncRepeatingTask (this, recentSignTask,
							5*20, recentSignUpdateSeconds);
				getLog().info("Enabled Task: Recent Sign (using " + (UseMultithreads?"multiple threads":"single thread") + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Failed loading the config.");
			return false;
		}
		return true;
	}

	public void onSaveConfig() {
	}


	// Init database
	public synchronized boolean ConnectDB() {
		if(config.getString("MySQL.Password").equals("password123")) {
			fail("Please set the database connection info in the config.");
			return false;
		}
		getLog().info("MySQL Initializing.");
		if(dataQueries != null) {
			fail("Database connection already made?");
			return false;
		}
		try {
			int port = config.getInt("MySQL.Port");
			if(port < 1) port = Integer.valueOf(config.getString("MySQL.Port"));
			if(port < 1) port = 3306;
			dataQueries = new DataQueries(
					config.getString("MySQL.Host"),
					port,
					config.getString("MySQL.Username"),
					config.getString("MySQL.Password"),
					config.getString("MySQL.Database"),
					config.getString("MySQL.TablePrefix")
					);
			dataQueries.setConnPoolSizeWarn(config.getInt("MySQL.ConnectionPoolSizeWarn"));
			dataQueries.setConnPoolSizeHard(config.getInt("MySQL.ConnectionPoolSizeHard"));
			// create/update tables
			MySQLTables dbTables = new MySQLTables(this);
			if(!dbTables.isOk()) {
				fail("Error loading db updater class.");
				return false;
			}
			dbTables = null;
			//} catch (SQLException e) {
		} catch (Exception e) {
			fail("Unable to connect to MySQL database.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void configDefaults() {
		config.addDefault("MySQL.Host",						"localhost");
		config.addDefault("MySQL.Username",					"minecraft");
		config.addDefault("MySQL.Password",					"password123");
		config.addDefault("MySQL.Port",						3306);
		config.addDefault("MySQL.Database",					"minecraft");
		config.addDefault("MySQL.TablePrefix",				"WA_");
		config.addDefault("MySQL.ConnectionPoolSizeWarn",	5);
		config.addDefault("MySQL.ConnectionPoolSizeHard",	10);
		config.addDefault("Misc.ReportSales",				true);
		config.addDefault("Misc.UseOriginalRecentSigns",	true);
		config.addDefault("Misc.SignClickDelay",			500);
		config.addDefault("Misc.UnsafeEnchantments",		false);
		config.addDefault("Misc.AnnounceGlobally",			true);
		config.addDefault("Tasks.SaleAlertSeconds",			20L);
		config.addDefault("Tasks.ShoutSignUpdateSeconds",	20L);
		config.addDefault("Tasks.RecentSignUpdateSeconds",	60L);
		config.addDefault("Tasks.AnnouncerMinutes",			60L);
		//		config.addDefault("SignLink.Enabled",				false);
		//		config.addDefault("SignLink.NumberOfLatestAuctionsToTrack", 10);
		config.addDefault("Development.UseMultithreads",	false);
		config.addDefault("Development.Debug",				false);
		config.addDefault("Announcer.Enabled",				false);
		config.addDefault("Announcer.Prefix",				"&c[Info] ");
		config.addDefault("Announcer.Random",				false);
		config.addDefault("Announcements", new String[]{"This server is running WebAuctionPlus!"} );
		config.options().copyDefaults(true);
		saveConfig();
	}


	public static boolean useOriginalRecent() {
		return useOriginalRecent;
	}
	public static boolean useSignLink() {
		//		return useSignLink;
		return false;
	}
	public static boolean timEnabled() {
		return timEnabled;
	}
	public static boolean announceGlobal() {
		return announceGlobal;
	}


	@SuppressWarnings("deprecation")
	public static synchronized void doUpdateInventory(Player p) {
		p.updateInventory();
	}

	public static long getCurrentMilli() {
		return System.currentTimeMillis();
	}

	// format chat colors
	public static String ReplaceColors(String text){
		return text.replaceAll("&([0-9a-fA-F])", "\247$1");
	}

	// add strings with delimiter
	public static String addStringSet(String baseString, String addThis, String Delim) {
		if (addThis.isEmpty())    return baseString;
		if (baseString.isEmpty()) return addThis;
		return baseString + Delim + addThis;
	}

	//	public static String format(double amount) {
	//		DecimalFormat formatter = new DecimalFormat("#,##0.00");
	//		String formatted = formatter.format(amount);
	//		if (formatted.endsWith("."))
	//			formatted = formatted.substring(0, formatted.length() - 1);
	//		return Common.formatted(formatted, Constants.Nodes.Major.getStringList(), Constants.Nodes.Minor.getStringList());
	//	}

	// work with doubles
	public static String FormatPrice(double value) {
		return settings.getString("Currency Prefix") + FormatDouble(value) + settings.getString("Currency Postfix");
	}
	public static String FormatDouble(double value) {
		DecimalFormat decim = new DecimalFormat("##,###,##0.00");
		return decim.format(value);
	}
	public static double ParseDouble(String value) {
		return Double.parseDouble( value.replaceAll("[^0-9.]+","") );
	}
	public static double RoundDouble(double value, int precision, int roundingMode) {
		BigDecimal bd = new BigDecimal(value);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.doubleValue();
	}

	public static int getNewRandom(int oldNumber, int maxNumber) {
		if (maxNumber == 0) return maxNumber;
		if (maxNumber == 1) return 1 - oldNumber;
		Random randomGen = new Random();
		int newNumber = 0;
		while (true) {
			newNumber = randomGen.nextInt(maxNumber + 1);
			if (newNumber != oldNumber) return newNumber;
		}
	}

	// min/max value
	public static int MinMax(int value, int min, int max) {
		if(value < min) value = min;
		if(value > max) value = max;
		return value;
	}
	public static long MinMax(long value, long min, long max) {
		if(value < min) value = min;
		if(value > max) value = max;
		return value;
	}
	public static double MinMax(double value, double min, double max) {
		if(value < min) value = min;
		if(value > max) value = max;
		return value;
	}
	// min/max by object
	public static boolean MinMax(Integer value, int min, int max) {
		boolean changed = false;
		if(value < min) {value = min; changed = true;}
		if(value > max) {value = max; changed = true;}
		return changed;
	}
	public static boolean MinMax(Long value, long min, long max) {
		boolean changed = false;
		if(value < min) {value = min; changed = true;}
		if(value > max) {value = max; changed = true;}
		return changed;
	}
	public static boolean MinMax(Double value, double min, double max) {
		boolean changed = false;
		if(value < min) {value = min; changed = true;}
		if(value > max) {value = max; changed = true;}
		return changed;
	}

	public static String MD5(String str) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.update(str.getBytes());
		byte[] byteData = md.digest();
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			String hex = Integer.toHexString(0xFF & byteData[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static void PrintProgress(double progress, int width) {
		String output = "[";
		int prog = (int)(progress * width);
		if (prog > width) prog = width;
		int i = 0;
		for (; i < prog; i++) {
			output += ".";
		}
		for (; i < width; i++) {
			output += " ";
		}
		getLog().info(output+"]");
	}
	public static void PrintProgress(int count, int total, int width) {
		try {
			// finished 100%
			if (count == total)
				PrintProgress( 1D, width);
			// total to small - skip
			else if (total < (width / 2) ) {}
			// print only when adding a .
			else if ( (int)(count % (total / width)) == 0)
				PrintProgress( (double)count / (double)total, width);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void PrintProgress(int count, int total) {
		PrintProgress(count, total, 20);
	}

	// announce radius
	public static void BroadcastRadius(String msg, Location loc, int radius) {
		Player[] playerList = Bukkit.getOnlinePlayers();
		Double x = loc.getX();
		Double z = loc.getZ();
		for(Player player : playerList) {
			Double playerX = player.getLocation().getX();
			Double playerZ = player.getLocation().getZ();
			if( (playerX < x + (double)radius ) &&
					(playerX > x - (double)radius ) &&
					(playerZ < z + (double)radius ) &&
					(playerZ > z - (double)radius ) )
				player.sendMessage(chatPrefix+msg);
		}
	}


	public void onLoadMetrics() {
		// usage stats
		try {
			metrics = new pxnMetrics(this);
			if(metrics.isOptOut()) {
				getLog().info("Plugin metrics are disabled, you bum");
				return;
			}
			//			log.info(logPrefix+"Starting metrics");
			// Create graphs for total Buy Nows / Auctions
			final pxnMetrics.Graph lineGraph  = metrics.createGraph("Stacks For Sale");
			final pxnMetrics.Graph pieGraph   = metrics.createGraph("Selling Method");
			final pxnMetrics.Graph stockTrend = metrics.createGraph("Stock Trend");
			// buy now count
			
			// auction count
			
			// total selling
			
			// stock trends
			
			// start reporting
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
			if(getLog().isDebug())
				e.printStackTrace();
		}
	}

	// updateCheck() from MilkBowl's Vault
	// modified for my compareVersions() function
	private static String doUpdateCheck() throws Exception {
		String pluginUrlString = "";
		try {
			URL url = new URL(pluginUrlString);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
			doc.getDocumentElement().normalize();
			NodeList nodes = doc.getElementsByTagName("item");
			Node firstNode = nodes.item(0);
			if (firstNode.getNodeType() == 1) {
				Element firstElement = (Element) firstNode;
				NodeList firstElementTagName = firstElement.getElementsByTagName("title");
				Element firstNameElement = (Element) firstElementTagName.item(0);
				NodeList firstNodes = firstNameElement.getChildNodes();
				String version = firstNodes.item(0).getNodeValue();
				return version.substring(version.lastIndexOf(" ")+1);
			}
		} catch (Exception ignored) {}
		return null;
	}

	// compare versions
	public static String compareVersions(String oldVersion, String newVersion) {
		if(oldVersion == null || newVersion == null) return null;
		oldVersion = normalisedVersion(oldVersion);
		newVersion = normalisedVersion(newVersion);
		int cmp = oldVersion.compareTo(newVersion);
		return cmp<0 ? "<" : cmp>0 ? ">" : "=";
	}
	public static String normalisedVersion(String version) {
		final String delim = ".";
		int maxWidth = 5;
		String[] split = Pattern.compile(delim, Pattern.LITERAL).split(version);
		String output = "";
		for(String s : split) {
			output += String.format("%"+maxWidth+'s', s);
		}
		return output;
	}

	// check for an updated version
	private void checkUpdateAvailable() {
		getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				try {
					newVersion = doUpdateCheck();
					final String cmp = compareVersions(currentVersion, newVersion);
					if(cmp == "<") {
						newVersionAvailable = true;
						final logBoots log = getLog();
						log.warning("An update is available!");
						log.warning("You're running "+currentVersion+" new version available is "+newVersion);
						log.warning("Message pablo67340!");
					}
				} catch (Exception ignored) {}
			}
		}, 5 * 20, 14400 * 20); // run every 4 hours
	}


}
