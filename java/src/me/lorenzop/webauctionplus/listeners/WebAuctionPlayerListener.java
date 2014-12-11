package me.lorenzop.webauctionplus.listeners;

import java.math.BigDecimal;
import java.util.Random;

import me.lorenzop.webauctionplus.WebAuctionPlus;
import me.lorenzop.webauctionplus.WebInventory;
import me.lorenzop.webauctionplus.dao.AuctionPlayer;
import me.lorenzop.webauctionplus.tasks.PlayerAlertTask;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WebAuctionPlayerListener implements Listener {

	private final WebAuctionPlus plugin;

	public WebAuctionPlayerListener(WebAuctionPlus plugin) {
		this.plugin = plugin;
	}


	// player join
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		String player = event.getPlayer().getName();
		if (player == null) return;
		// login code runs multi-threaded with a delay
		// run after 2 seconds
		Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new PlayerAlertTask(player), 30);
	}


	// player quit
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		WebInventory.onInventoryClose(event.getPlayer());
		plugin.lastSignUse.remove(event.getPlayer().getName());
	}


	// close inventory
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event){
		WebInventory.onInventoryClose( (Player) event.getPlayer() );
		//		if(WebAuctionPlus.dataQueries.debugSQL) WebAuctionPlus.log.info(WebAuctionPlus.dataQueries."CLOSED!");
		//		Bukkit.getServer().broadcastMessage("Close Inventory");
	}


	//	// inventory click
	//	@EventHandler(priority = EventPriority.NORMAL)
	//	public void onInventoryClickEvent(InventoryClickEvent event) {
	//		if(event == null) return;
	//		// not an inventory click
	//		if(event.getCurrentItem() == null) return;
	//		if(!(event.getWhoClicked() instanceof Player)) return;
	//		WebInventory.onInventoryClick( (Player) event.getWhoClicked(), event.getRawSlot() );
	//	}


	// player interact
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Economy econ = WebAuctionPlus.getPlugins().getEconomy();
		// right click only
		if( event.getAction() != Action.RIGHT_CLICK_BLOCK &&
				event.getAction() != Action.RIGHT_CLICK_AIR) return;
		Block block = event.getClickedBlock();
		// not a sign
		if(block == null) return;
		if(block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) return;
		// it's a sign
		Sign sign = (Sign) block.getState();
		String[] lines = sign.getLines();
		if(!lines[0].equals("[WebAuctionPlus]")) return;
		event.setCancelled(true);
		// get player info
		Player player = event.getPlayer();
		//		String player = p.getName();

		// prevent click spamming signs
		if(plugin.lastSignUse.containsKey(player))
			if( plugin.lastSignUse.get(player)+(long)plugin.signDelay > WebAuctionPlus.getCurrentMilli() ) {
				player.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("please_wait"));
				return;
			}
		plugin.lastSignUse.put(player.getName(), WebAuctionPlus.getCurrentMilli());

		// Shout sign
		if(lines[1].equals("Shout")) {
			clickSignShout(block.getLocation());
			return;
		}

		// Deposit sign (money)
		if(lines[1].equals("Deposit")) {
			if(!player.hasPermission("rm.use.deposit.money")) {
				player.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
				return;
			}
			double amount = 0.0D;
			if(!lines[2].equals("All")) {
				try {
					amount = WebAuctionPlus.ParseDouble(lines[2].replace(",", "."));
				} catch(NumberFormatException ignore) {}
			}
			// player has enough money
			if(!econ.has(player.getName(), amount)) {
				player.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("not_enough_money_pocket"));
				return;
			}
			AuctionPlayer auctionPlayer = WebAuctionPlus.dataQueries.getPlayer(player);
			if(auctionPlayer == null) {
				player.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("account_not_found"));
				return;
			}
			double currentMoney = auctionPlayer.getMoney();
			if(lines[2].equals("All"))
				amount = econ.getBalance(player.getName());
			currentMoney += amount;
			currentMoney = WebAuctionPlus.RoundDouble(currentMoney, 2, BigDecimal.ROUND_HALF_UP);
			player.sendMessage(WebAuctionPlus.chatPrefix + "Added " + amount +
					" to auction account, new auction balance: " + currentMoney);
			WebAuctionPlus.dataQueries.updatePlayerMoney(player, currentMoney);
			econ.withdrawPlayer(player.getName(), amount);
			return;
		}

		// Withdraw sign (money)
		if(lines[1].equals("Withdraw")) {
			if(!player.hasPermission("rm.use.withdraw.money")) {
				player.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
				return;
			}
			double amount = 0.0D;
			try {
				AuctionPlayer auctionPlayer = WebAuctionPlus.dataQueries.getPlayer(player);
				if(auctionPlayer == null) {
					player.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("account_not_found"));
					return;
				}
				// Match found!
				double currentMoney = auctionPlayer.getMoney();
				if(lines[2].equals("All")) {
					amount = currentMoney;
				} else {
					try {
						amount = WebAuctionPlus.ParseDouble(lines[2].replace(",", "."));
					} catch(NumberFormatException ignore) {}
				}
				if(currentMoney < amount) {
					player.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("not_enough_money_account"));
					return;
				}
				currentMoney -= amount;
				currentMoney = WebAuctionPlus.RoundDouble(currentMoney, 2, BigDecimal.ROUND_HALF_UP);
				player.sendMessage(WebAuctionPlus.chatPrefix + "Removed " +
						amount + " from auction account, new auction balance: " + currentMoney);
				WebAuctionPlus.dataQueries.updatePlayerMoney(player, currentMoney);
				econ.depositPlayer(player.getName(), amount);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		// Mailbox (items)
		if(lines[1].equals("MailBox")) {
			if(!player.hasPermission("rm.use.mailbox")) {
				player.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_permission"));
				return;
			}
			// disallow creative
			if(player.getGameMode() != GameMode.SURVIVAL && !player.isOp()) {
				player.sendMessage(WebAuctionPlus.chatPrefix + WebAuctionPlus.Lang.getString("no_cheating"));
				return;
			}
			// load virtual chest
			WebInventory.onInventoryOpen(player);
			return;
		}

	}


	// shout sign
	private static long lastUseShout = 0;
	public static void clickSignShout(Location loc) {
		if(lastUseShout+(10*60*1000) > WebAuctionPlus.getCurrentMilli()) return;
		lastUseShout = WebAuctionPlus.getCurrentMilli();
		WebAuctionPlus.BroadcastRadius(getShoutMessage(), loc, 20);
	}
	protected static String getShoutMessage() {
		Random generator = new Random();
		while(true) {
			int roll = generator.nextInt(11);
			switch(roll) {
			case 0: return "All your base are belong to Notch!";
			case 1: return "Mmmmm, chocolate milk.";
			case 2: return "Minecraft is like a potato because ______";
			case 3: return "99.5% mime free!";
			case 4: return "BAGOCK! I sorry, I thought you was corn.";
			case 5: return "Hey, there's a creeper behind you! jk";
			case 6: return "It's a trap!";
			case 7: return "Muhuhahahaha!";
			case 8: return "Kittens give Morbo gas.";
			case 9: return "Vote for net neutrality!";
			case 10:return "That creeper stole your wallet!";
			}
		}
	}


}
