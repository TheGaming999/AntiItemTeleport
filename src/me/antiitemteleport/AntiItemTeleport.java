package me.antiitemteleport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class AntiItemTeleport extends JavaPlugin implements Listener {

	private ProtocolManager protocolManager;
	private final Set<String> disabledWorlds = new HashSet<>();

	@Override
	public void onEnable() {
		protocolManager = ProtocolLibrary.getProtocolManager();
		load();
		Bukkit.getPluginManager().registerEvents(this, this);
		registerPackets();
		getLogger().info("Enabled.");
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabled.");
	}

	public synchronized void load() {
		disabledWorlds.clear();
		getConfig().getStringList("disabled-worlds")
				.forEach(disabledWorld -> Bukkit.getWorlds()
						.stream()
						.map(World::getName)
						.forEach(worldName -> disabledWorlds.add(worldName.equalsIgnoreCase(disabledWorld)
								? worldName.toLowerCase() : disabledWorld.toLowerCase())));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getLabel().equalsIgnoreCase("antiitemteleport")) {
			if (!sender.isOp()) {
				sender.sendMessage("You must be an operator to use this command!");
				return true;
			}
			switch (args.length) {
				case 0:
					sender.sendMessage(
							"/antiitemteleport add <WorldName> - adds given world name to disabled worlds list");
					sender.sendMessage(
							"/antiitemteleport remove/delete/del <WorldName> - removes given world name from disabled worlds list");
					sender.sendMessage("/antiitemteleport list - lists all disabled worlds");
					sender.sendMessage("/antiitemteleport reload/rl - reloads configuration file");
					break;
				case 1:
					switch (args[0].toLowerCase()) {
						case "list":
							sender.sendMessage("Disabled worlds:");
							disabledWorlds.forEach(sender::sendMessage);
							break;
						case "reload":
						case "rl":
							load();
							sender.sendMessage("Reloaded.");
							break;
						case "add":
							sender.sendMessage("/antiitemteleport add <WorldName>");
							break;
						case "remove":
						case "delete":
						case "del":
							sender.sendMessage("/antiitemteleport remove/delete/del <WorldName>");
							break;
					}
					break;
				case 2:
					switch (args[0].toLowerCase()) {
						case "add":
							List<String> disabledWorldsAdd = getConfig().getStringList("disabled-worlds");
							String worldNameAdd = getWorldName(args[1], sender);
							if (CollectionUtils.hasIgnoreCase(disabledWorldsAdd, worldNameAdd)) {
								sender.sendMessage("The world '" + worldNameAdd + "' is already included in the list!");
								break;
							}
							disabledWorldsAdd.add(worldNameAdd);
							getConfig().set("disabled-worlds", disabledWorldsAdd);
							saveConfig();
							load();
							sender.sendMessage("Added '" + worldNameAdd + "' to disabled worlds list.");
							break;
						case "remove":
						case "delete":
						case "del":
							List<String> disabledWorldsRemove = getConfig().getStringList("disabled-worlds");
							String worldNameRemove = getWorldName(args[1], sender);
							if (!disabledWorldsRemove
									.removeIf(worldName -> worldName.equalsIgnoreCase(worldNameRemove))) {
								sender.sendMessage("Unable to find a world with the name of '" + worldNameRemove
										+ "' in the list.");
								break;
							}
							getConfig().set("disabled-worlds", disabledWorldsRemove);
							saveConfig();
							load();
							sender.sendMessage("Removed '" + worldNameRemove + "' from disabled worlds list.");
							break;
					}
					break;
			}
		}
		return true;
	}

	private String getWorldName(String string, CommandSender sender) {
		return sender instanceof Player && (string.equals("~") || string.equals("#") || string.equals("this"))
				? ((Player) sender).getWorld().getName() : string;
	}

	public void registerPackets() {
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL,
				new PacketType[] { PacketType.Play.Server.ENTITY_TELEPORT, PacketType.Play.Server.REL_ENTITY_MOVE }) {
			@Override
			public void onPacketSending(PacketEvent event) {
				World playerWorld = event.getPlayer().getWorld();
				if (!disabledWorlds.contains(playerWorld.getName().toLowerCase())) {
					Entity ent = protocolManager.getEntityFromID(playerWorld,
							event.getPacket().getIntegers().getValues().get(0));
					if (ent.getType() == EntityType.DROPPED_ITEM) event.setCancelled(true);
				}
			}
		});
	}

}
