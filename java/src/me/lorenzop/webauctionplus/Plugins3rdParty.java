package me.lorenzop.webauctionplus;

import java.security.Permissions;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;


public class Plugins3rdParty {

	private final logBoots log;


	public Plugins3rdParty(final logBoots log) {
		if(log == null){

		}
		this.log = log;
		if(setupVault())
			log.info("Found Vault.");
		else
			log.warning("Failed to find Vault.");
	}



	/**
	 * Vault
	 */
	private Plugin vault = null;
	private Chat        chat = null;
	private Economy     econ;
	private Permissions perm = null;


	private boolean setupVault() {
		// economy
		{
			final RegisteredServiceProvider<Economy> provider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
			this.econ = provider.getProvider();
			if(this.econ == null)
				log.info("Found economy plugin.");
			else
				log.warning("Failed to find economy plugin.");
		}

		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		this.econ = ((Economy)rsp.getProvider());
		return this.econ != null;


	}


	public boolean isLoaded_Vault() {
		return (this.vault != null);
	}
	public Chat getChat() {
		return this.chat;
	}
	public Economy getEconomy() {
		return this.econ;
	}
	public Permissions getPerms() {
		return this.perm;
	}


	//	/**
	//	 * SignLink
	//	 */
	//	private Plugin signlink = null;


	//	private boolean setupSignLink() {
	//		this.signlink = Bukkit.getPluginManager().getPlugin("SignLink");
	//		if(this.signlink == null) return false;
	//		return isLoaded_SignLink();
	//	}
	public boolean isLoaded_SignLink() {
		//		return (this.signlink != null);
		return false;
	}


}
