package me.lorenzop.webauctionplus.listeners;

import java.math.BigDecimal;

import me.lorenzop.webauctionplus.WebAuctionPlus;
import me.lorenzop.webauctionplus.WebInventory;
import me.lorenzop.webauctionplus.dao.AuctionPlayer;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;


public class WebAuctionCommands implements CommandExecutor {

	private final WebAuctionPlus plugin;

	public WebAuctionCommands(WebAuctionPlus plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final Economy econ = WebAuctionPlus.getPlugins().getEconomy();
		int params = args.length;
		String player = "";
		if(sender instanceof Player) player = ((Player) sender).getName();
		// 0 args
		if(params == 0) {
			return false;
		}
		// 1 arg
		if(params == 1) {
			//			if(args[0].equalsIgnoreCase("close")){
			//				WebInventory.onInventoryClose((Player) sender);
			//				return true;
			//			}
			// rm reload
			if(args[0].equalsIgnoreCase("reload")){
				if(sender instanceof Player) {
					if(!sender.hasPermission("rm.reload")){
						((Player)sender).sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("no_permission"));
						return true;
					}
				}
				if(sender instanceof Player)
					sender.sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("reloading"));
				WebAuctionPlus.getLog().info(WebAuctionPlus.Lang.getString("reloading"));
				plugin.onReload();
				if(WebAuctionPlus.isOk()) {
					if(sender instanceof Player)
						sender.sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("finished_reloading"));
					WebAuctionPlus.getLog().info(WebAuctionPlus.Lang.getString("finished_reloading"));
				} else {
					if(sender instanceof Player)
						sender.sendMessage(WebAuctionPlus.chatPrefix+"Failed to reload!");
					WebAuctionPlus.getLog().severe("Failed to reload!");
				}
				return true;
			}
			// rm version
			if (args[0].equalsIgnoreCase("version")) {
				if(sender instanceof Player) {
					sender.sendMessage(WebAuctionPlus.chatPrefix+"v"+plugin.getDescription().getVersion());
					if(WebAuctionPlus.newVersionAvailable && sender.hasPermission("rm.webadmin"))
						sender.sendMessage(WebAuctionPlus.chatPrefix+"A new version is available! " + WebAuctionPlus.newVersion);
				} else {
					WebAuctionPlus.getLog().info("v"+plugin.getDescription().getVersion());
					if(WebAuctionPlus.newVersionAvailable) {
						WebAuctionPlus.getLog().info("A new version is available! "+WebAuctionPlus.newVersion);
						WebAuctionPlus.getLog().info("http://dev.bukkit.org/server-mods/webauctionplus");
					}
				}
				return true;
			}


			// Deposit
			
			
			if (args[0].equalsIgnoreCase("inv")) {

				if(sender instanceof Player) {

					if(!sender.hasPermission("rm.use.mailbox")) {
						sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
						
					}
					// disallow creative
					if(((Player) sender).getPlayer().getGameMode() != GameMode.SURVIVAL && !sender.isOp()) {
						sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_cheating"));
						
					}
					// load virtual chest
					WebInventory.onInventoryOpen(((Player) sender).getPlayer());
			


					return true;
				}
			}

			// rm update
			if(args[0].equalsIgnoreCase("update")){
				if(!sender.hasPermission("rm.reload")){
					sender.sendMessage(WebAuctionPlus.chatPrefix+WebAuctionPlus.Lang.getString("no_permission"));
					return true;
				}
				WebAuctionPlus.recentSignTask.run();
				if(sender instanceof Player)
					sender.sendMessage(WebAuctionPlus.chatPrefix+"Updated recent signs.");
				WebAuctionPlus.getLog().info("Updated recent signs.");
				return true;
			}
			return false;
		}
		if(!WebAuctionPlus.isOk()) {
			sender.sendMessage(WebAuctionPlus.chatPrefix+"Plugin isn't loaded");
			return true;
		}
		// 2 args
		if(params == 2 || params == 3) {
			
			if (args[0].equalsIgnoreCase("deposit")) {

				if(sender instanceof Player) {

					if(!sender.hasPermission("rm.use.deposit.money")) {
						sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));

					}
					double amount = 0.0D;
					if(!args[1].equals("All")) {
						try {
							amount = WebAuctionPlus.ParseDouble(args[1].replace(",", "."));
						} catch(NumberFormatException ignore) {}
					}
					// player has enough money
					if(!econ.has(sender.getName(), amount)) {
						sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("not_enough_money_pocket"));

					}
					AuctionPlayer auctionPlayer = WebAuctionPlus.dataQueries.getPlayer(player);
					if(auctionPlayer == null) {
						sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("account_not_found"));

					}
					double currentMoney = auctionPlayer.getMoney();
					if(args[1].equals("All"))
						amount = econ.getBalance(sender.getName());
					currentMoney += amount;
					currentMoney = WebAuctionPlus.RoundDouble(currentMoney, 2, BigDecimal.ROUND_HALF_UP);
					sender.sendMessage(WebAuctionPlus.chatPrefix + "Added " + amount +
							" to auction account, new auction balance: " + currentMoney);
					WebAuctionPlus.dataQueries.updatePlayerMoney(((Player) sender).getPlayer(), currentMoney);
					econ.withdrawPlayer(sender.getName(), amount);

				}
				return true;
			}
			//withdraw
			if (args[0].equalsIgnoreCase("withdraw")) {

				if(sender instanceof Player) {

					if(!sender.hasPermission("rm.use.withdraw.money")) {
						sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));

					}
					double amount = 0.0D;
					try {
						AuctionPlayer auctionPlayer = WebAuctionPlus.dataQueries.getPlayer(player);
						if(auctionPlayer == null) {
							sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("account_not_found"));

						}
						// Match found!
						double currentMoney = auctionPlayer.getMoney();
						if(args[1].equals("All")) {
							amount = currentMoney;
						} else {
							try {
								amount = WebAuctionPlus.ParseDouble(args[1].replace(",", "."));
							} catch(NumberFormatException ignore) {}
						}
						if(currentMoney < amount) {
							sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("not_enough_money_account"));

						}
						currentMoney -= amount;
						currentMoney = WebAuctionPlus.RoundDouble(currentMoney, 2, BigDecimal.ROUND_HALF_UP);
						sender.sendMessage(WebAuctionPlus.chatPrefix + "Removed " +
								amount + " from auction account, new auction balance: " + currentMoney);
						WebAuctionPlus.dataQueries.updatePlayerMoney(((Player) sender).getPlayer(), currentMoney);
						econ.depositPlayer(sender.getName(), amount);
					} catch (Exception e) {
						e.printStackTrace();
					}


					return true;
				}
			}
			
			// rm password
			if (args[0].equalsIgnoreCase("register") ||
					args[0].equalsIgnoreCase("reg")     ||
					args[0].equalsIgnoreCase("r")       ) {
				String pass = "";
				// is player
				boolean isPlayer = (sender instanceof Player);
				if (isPlayer) {
					if (params != 2 || args[1].isEmpty()) return false;
					pass = WebAuctionPlus.MD5(args[1]);
					args[1] = "";
					// is console
				} else {
					if (params != 3) return false;
					if (args[1].isEmpty() || args[2].isEmpty()) return false;
					player = args[1];
					if(!Bukkit.getOfflinePlayer(player).hasPlayedBefore()) {
						sender.sendMessage(WebAuctionPlus.chatPrefix+"Player not found!");
						sender.sendMessage(WebAuctionPlus.chatPrefix+"Note: if you really need to, you can add a player to the database, just md5 the password.");
						return true;
					}
					pass = WebAuctionPlus.MD5(args[2]);
					args[2] = "";
				}
				if(player.isEmpty()) return false;
				AuctionPlayer waPlayer = WebAuctionPlus.dataQueries.getPlayer(player);
				// create that person in database
				if(waPlayer == null) {
					// permission to create an account
					if (isPlayer) {
						if (!sender.hasPermission("rm.password.create")){
							((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
							return true;
						}
					}
					waPlayer = new AuctionPlayer(player);
					waPlayer.setPerms(
							sender.hasPermission("rm.canbuy")   && isPlayer,
							sender.hasPermission("rm.cansell")  && isPlayer,
							sender.hasPermission("rm.webadmin") && isPlayer
							);
					WebAuctionPlus.dataQueries.createPlayer(waPlayer, pass);
					if (sender instanceof Player)
						sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("account_created"));
					WebAuctionPlus.getLog().info(WebAuctionPlus.Lang.getString("account_created")+" "+player+
							" with perms: "+waPlayer.getPermsString());
					// change password for an existing account
				} else {
					// permission to change password
					if(sender instanceof Player) {
						if (!sender.hasPermission("rm.password.change")){
							((Player)sender).sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
							return true;
						}
					}
					WebAuctionPlus.dataQueries.updatePlayerPassword(player, pass);
					if(sender instanceof Player)
						sender.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("password_changed"));
					WebAuctionPlus.getLog().info(WebAuctionPlus.Lang.getString("password_changed")+" "+player);
				}
				return true;
			}
			return false;
		}
		// 4 args
		if(params == 4) {
			//			// wa give <player> <item> <count>
			//			if (args[0].equals("give")) {
			// /wa give lorenzop diamond 3
			//			}
			return false;
		}
		return false;
	}

}
