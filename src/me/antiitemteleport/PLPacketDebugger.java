package me.antiitemteleport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class PLPacketDebugger {

	private static final String PACKET_NAME_MSG = colorize("&6Packet &7-> &6{0} &7{1}");
	private static final String PACKET_INFO_EXPLANATION_MSG = colorize("&7[&cType&7] (&9FieldName&7) = &aValue &7{");
	private static final String PACKET_INFO_CLOSING_CHAR = colorize("&7}");
	private static final String PACKET_INFO_MSG = colorize("  &7[&c{0}&7]" + " (&9{1}&7)" + " = &a{2}");
	private static final String PACKET_INFO_NOTHING_MSG = colorize("  &4&l* NOTHING FOUND *");
	public static boolean sendExplanation = true;

	private static String colorize(String textToColorize) {
		return ChatColor.translateAlternateColorCodes('&', textToColorize);
	}

	private static String parseObject(Object obj) {
		if (obj instanceof Integer) {
			int parsedInteger = (int) obj;
			return "(int)" + String.valueOf(parsedInteger);
		} else if (obj instanceof Double) {
			double parsedDouble = (double) obj;
			return "(double)" + String.valueOf(parsedDouble);
		} else if (obj instanceof Float) {
			float parsedFloat = (float) obj;
			return "(float)" + String.valueOf(parsedFloat);
		} else if (obj instanceof Short) {
			short parsedShort = (short) obj;
			return "(short)" + String.valueOf(parsedShort);
		} else if (obj instanceof Byte) {
			byte parsedByte = (byte) obj;
			return "(byte)" + String.valueOf(parsedByte);
		} else if (obj instanceof UUID) {
			UUID uuid = (UUID) obj;
			return "(UUID)" + uuid.toString();
		} else if (obj instanceof List) {
			List<?> parsedList = (List<?>) obj;
			return "(List)" + parsedList.toString();
		} else if (obj instanceof Set) {
			Set<?> parsedSet = (Set<?>) obj;
			return "(Set)" + parsedSet.toString();
		} else if (obj instanceof Deque) {
			Deque<?> parsedDeque = (Deque<?>) obj;
			return "(Deque)" + parsedDeque.toString();
		} else if (obj instanceof Collection) {
			Collection<?> parsedCollection = (Collection<?>) obj;
			return "(Collection)" + parsedCollection.toString();
		} else if (obj instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) obj;
			return "(Map)" + map.entrySet().toString();
		} else {
			return "(" + obj.getClass().getSimpleName() + ")" + obj.toString();
		}
	}

	/**
	 * Sends a debug info message to a certain player.
	 * 
	 * @param packet packet container to get info from
	 * @param player player to send the debug info to
	 * @return true after debug info gets sent
	 */
	public static boolean send(PacketContainer packet, Player player) {
		getDebugInfo(packet).forEach(player::sendMessage);
		return true;
	}

	/**
	 * Sends a debug info message to a certain player.
	 * 
	 * @param event  packet event to get info from
	 * @param player player to send the debug info to
	 * @return true after debug info gets sent
	 */
	public static boolean send(PacketEvent event, Player player) {
		return send(event.getPacket(), player);
	}

	/**
	 * Sends a debug info message to the player retrieved from the event.
	 * 
	 * @param event packet event to get info from
	 * @return true after debug info gets sent
	 */
	public static boolean send(PacketEvent event) {
		return send(event.getPacket(), event.getPlayer());
	}

	/**
	 * Sends a debug info message to a certain sender.
	 * 
	 * @param packet packet containers to get info from
	 * @param sender sender to send the debug info to
	 * @return true after debug info gets sent
	 */
	public static boolean send(PacketContainer packet, CommandSender sender) {
		getDebugInfo(packet).forEach(sender::sendMessage);
		return true;
	}

	/**
	 * Sends a debug info message to a certain sender.
	 * 
	 * @param event  packet event to get info from
	 * @param sender sender to send the debug info to
	 * @return true after debug info gets sent
	 */
	public static boolean send(PacketEvent event, CommandSender sender) {
		return send(event.getPacket(), sender);
	}

	/**
	 * Sends a debug info message to the console.
	 * 
	 * @param packet packet container to get info from
	 * @return true after debug info gets sent
	 */
	public static boolean printOut(PacketContainer packet) {
		getDebugInfo(packet).forEach(System.out::println);
		return true;
	}

	/**
	 * Sends a debug info message for all specified packets to the console.
	 * 
	 * @param packets packet containers to get info from
	 * @return true after debug info gets sent
	 */
	public static void printOut(PacketContainer... packets) {
		for (PacketContainer packet : packets) printOut(packet);
	}

	/**
	 * Sends a debug info message to the console.
	 * 
	 * @param event packet event to get info from
	 * @return true after debug info gets sent
	 */
	public static boolean printOut(PacketEvent event) {
		return printOut(event.getPacket());
	}

	/**
	 * Braodcasts a debug info message to everyone.
	 * 
	 * @param packet packet container to get info from
	 * @return true after debug info gets sent
	 */
	public static boolean broadcast(PacketContainer packet) {
		getDebugInfo(packet).forEach(Bukkit::broadcastMessage);
		return true;
	}

	/**
	 * Braodcasts a debug info message for all specified packet containers to
	 * everyone.
	 * 
	 * @param packets packet containers to get info from
	 * @return true after debug info gets sent
	 */
	public static void broadcast(PacketContainer... packets) {
		for (PacketContainer packet : packets) broadcast(packet);
	}

	/**
	 * Braodcasts a debug info message to everyone.
	 * 
	 * @param event packet event to get info from
	 * @return true after debug info gets sent
	 */
	public static boolean broadcast(PacketEvent event) {
		return broadcast(event.getPacket());
	}

	/**
	 * Braodcasts a debug info message to every online operator.
	 * 
	 * @param packet packet container to get info from
	 * @return true if the message has been successfully sent, false otherwise.
	 */
	public static boolean broadcast(PacketContainer packet, boolean operatorsOnly) {
		if (!operatorsOnly) return broadcast(packet);
		Set<OfflinePlayer> operators = Bukkit.getOperators();
		if (operators.isEmpty()) return false;
		boolean onlineOperatorFound = false;
		for (OfflinePlayer operator : operators) {
			if (operator.isOnline()) {
				getDebugInfo(packet).forEach(operator.getPlayer()::sendMessage);
				onlineOperatorFound = true;
			}
		}
		return onlineOperatorFound;
	}

	/**
	 * Braodcasts a debug info message to every online operator.
	 * 
	 * @param event packet event to get info from
	 * @return true if the message has been successfully sent, false otherwise.
	 */
	public static boolean broadcast(PacketEvent event, boolean operatorsOnly) {
		return broadcast(event.getPacket(), operatorsOnly);
	}

	public static List<List<String>> getDebugInfos(PacketContainer... packets) {
		List<List<String>> debugLists = new ArrayList<>();
		for (PacketContainer packet : packets) debugLists.add(getDebugInfo(packet));
		return debugLists;
	}

	public static List<String> getDebugInfo(PacketContainer... packets) {
		List<String> allDebugLines = new ArrayList<>();
		for (PacketContainer packet : packets) getDebugInfo(packet).forEach(allDebugLines::add);
		return allDebugLines;
	}

	public static List<String> getDebugInfo(PacketContainer packet) {
		List<String> debugLines = new ArrayList<>();
		PacketType packetType = packet.getType();
		Protocol packetProtocol = packetType.getProtocol();
		String boundType = packetType.isServer() ? "Server" : "Client";
		String signalType = packetType.isServer() ? "(Sent)" : "(Received)";
		String mainType = StringUtils.capitalize(packetProtocol.name().toLowerCase());
		String packetName = packetType.name();
		String fullName = "PacketType." + mainType + "." + boundType + "." + packetName;
		debugLines.add(PACKET_NAME_MSG.replace("{0}", fullName).replace("{1}", signalType));
		if (sendExplanation) debugLines.add(PACKET_INFO_EXPLANATION_MSG);
		StructureModifier<Object> packetModifier = packet.getModifier();
		if (packetModifier != null && packetModifier.getValues() != null) {
			AtomicInteger index = new AtomicInteger(0);
			for (Object value : packetModifier.getValues()) {
				if (value == null) continue;
				String type = value.getClass().getName();
				String fieldName = packet.getModifier().getField(index.get()).getName();
				String parsedValue = sendExplanation ? parseObject(value) : value.toString();
				debugLines.add(
						PACKET_INFO_MSG.replace("{0}", type).replace("{1}", fieldName).replace("{2}", parsedValue));
				index.incrementAndGet();
			}
		} else {
			debugLines.add(PACKET_INFO_NOTHING_MSG);
		}
		if (sendExplanation) debugLines.add(PACKET_INFO_CLOSING_CHAR);
		return debugLines;
	}

}
