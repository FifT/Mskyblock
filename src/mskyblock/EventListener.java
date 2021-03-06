package mskyblock;

import java.util.List;

import com.google.gson.internal.LinkedTreeMap;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.level.ChunkLoadEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import mskyblock.skyblock.Skyblock;

public class EventListener implements Listener {
	private Main plugin;

	public EventListener(Main plugin) {
		this.plugin = plugin;
	}

	public DataBase getDB() {
		return plugin.getDB();
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().toLowerCase().equals(getDB().get("commands-skyblock"))) {
			if (args.length < 1) {
				if (!(sender instanceof Player)) {
					getDB().alert(sender, getDB().get("commands-skyblock-usage"));
					return true;
				}
				if (Skyblock.hasSharedSkyblock((Player) sender) && getDB().config.getBoolean("only-one-skyblock")) {
					getDB().alert(sender, getDB().get("already-shared"));
					getDB().alert(sender, getDB().get("commands-skyblock-usage"));
					List<Skyblock> list = Skyblock.getShareSkyblockList((Player) sender);
					list.forEach((skyblock) -> getDB().message(sender, getDB().get("with-player").replace("%player", skyblock.getOwner())));
					return true;
				}
				if (!Skyblock.hasSkyblock((Player) sender)) {
					Skyblock.makeSkyblock((Player) sender);
					getDB().message(sender, getDB().get("make-skyblock-success-1"));
					getDB().message(sender, getDB().get("make-skyblock-success-2"));
					return true;
				}
				getDB().alert(sender, getDB().get("commands-skyblock-usage"));
				return true;
			}
			if (args[0].toLowerCase().equals(getDB().get("commands-move"))) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.ingame"));
					return true;
				}
				if (args.length < 2) {
					Skyblock skyblock = Skyblock.getSkyblock(sender.getName());
					if (skyblock == null) {
						getDB().alert(sender, getDB().get("commands-move-usage"));
						return true;
					}
					((Player) sender).teleport(skyblock.getSpawn());
					getDB().message(sender, getDB().get("move-success").replace("%player", sender.getName()));
					return true;
				}
				Player player = Server.getInstance().getPlayer(args[1]);
				String target = (player == null) ? args[1] : player.getName();
				Skyblock sblock = Skyblock.getSkyblock(target);
				if (sblock == null) {
					getDB().alert(sender, getDB().get("player-dont-have-skyblock").replace("%player", target));
					return true;
				}
				if (!sblock.isOwner((Player) sender) && !sblock.isShare((Player) sender)
						&& !sblock.isInvited((Player) sender) && !sender.isOp() && !sblock.isInviteAll()) {
					getDB().alert(sender, getDB().get("not-invited"));
					return true;
				}
				((Player) sender).teleport(sblock.getSpawn());
				getDB().message(sender, getDB().get("move-success").replace("%player", target));
				return true;
			} else if (args[0].toLowerCase().equals(getDB().get("commands-remove"))) {
				if (args.length < 2) {
					if (!(sender instanceof Player)) {
						getDB().alert(sender, getDB().get("commands-remove-usage"));
						return true;
					}
					if (!Skyblock.hasSkyblock((Player) sender)) {
						getDB().alert(sender, getDB().get("dont-have-skyblock"));
						return true;
					}
					Skyblock.remove(sender.getName());
					getDB().message(sender, getDB().get("remove-success"));
					return true;
				}
				if (!sender.hasPermission("mskyblock.remove.other")) {
					sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.permission"));
					return true;
				}
				if (!Skyblock.hasSkyblock(args[1])) {
					getDB().alert(sender, getDB().get("player-dont-have-skyblock").replace("%player", args[1]));
					return true;
				}
				Skyblock.remove(args[1]);
				getDB().message(sender, getDB().get("remove-player-skyblock").replace("%player", args[1]));
				return true;
			} else if (args[0].toLowerCase().equals(getDB().get("commands-share"))) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.ingame"));
					return true;
				}
				if (!Skyblock.hasSkyblock((Player) sender)) {
					getDB().alert(sender, getDB().get("dont-have-skyblock"));
					return true;
				}
				if (args.length < 2) {
					getDB().alert(sender, getDB().get("commands-share-usage"));
					return true;
				}
				if (getDB().config.getBoolean("only-one-skyblock")) {
					if (Skyblock.hasSkyblock(args[1])) {
						getDB().alert(sender, getDB().get("already-have-skyblock"));
						return true;
					}
					if (Skyblock.hasSharedSkyblock(args[1])) {
						getDB().alert(sender, getDB().get("already-shared-skyblock"));
						return true;
					}
				}
				Skyblock.getSkyblock(sender.getName()).shareSkyblock(args[1]);
				getDB().message(sender, getDB().get("share-skyblock").replace("%player", args[1]));
				return true;
			} else if (args[0].toLowerCase().equals(getDB().get("commands-invite"))) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.ingame"));
					return true;
				}
				if (!Skyblock.hasSkyblock((Player) sender)) {
					getDB().alert(sender, getDB().get("dont-have-skyblock"));
					return true;
				}
				if (args.length < 2) {
					getDB().alert(sender, getDB().get("commands-invite-usage"));
					return true;
				}
				Skyblock.getSkyblock(sender.getName()).invitePlayer(args[1]);
				getDB().message(sender, getDB().get("invite-skyblock").replace("%player", args[1]));
				return true;
			} else if (args[0].toLowerCase().equals(getDB().get("commands-spawn"))) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.ingame"));
					return true;
				}
				if (!Skyblock.hasSkyblock((Player) sender)) {
					getDB().alert(sender, getDB().get("dont-have-skyblock"));
					return true;
				}
				Skyblock skyblock = Skyblock.getSkyblock(sender.getName());
				if (!skyblock.isInside((Player) sender)) {
					getDB().alert(sender, getDB().get("not-your-skyblock"));
					return true;
				}
				skyblock.setSpawn(new Position(((Player) sender).getX(), ((Player) sender).getY(),
						((Player) sender).getZ(), ((Player) sender).getLevel()));
				getDB().message(sender, getDB().get("set-spawn"));
				return true;
			} else if (args[0].toLowerCase().equals(getDB().get("commands-expulsion"))) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.ingame"));
					return true;
				}
				if (!Skyblock.hasSkyblock((Player) sender)) {
					getDB().alert(sender, getDB().get("dont-have-skyblock"));
					return true;
				}
				if (args.length < 2) {
					getDB().alert(sender, getDB().get("commands-expulsion-usage"));
					return true;
				}
				Skyblock skyblock = Skyblock.getSkyblock(sender.getName());
				if (!skyblock.isShare(args[1]) && !skyblock.isInvited(args[1])) {
					getDB().alert(sender, getDB().get("player-not-share"));
					return true;
				}
				skyblock.expulsion(args[1]);
				getDB().message(sender, getDB().get("expulsion-player").replace("%player", args[1]));
				return true;
			} else if (args[0].toLowerCase().equals(getDB().get("commands-list"))) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.ingame"));
					return true;
				}
				if (!Skyblock.hasSkyblock((Player) sender)) {
					getDB().alert(sender, getDB().get("dont-have-skyblock"));
					return true;
				}
				if (args.length < 2) {
					getDB().alert(sender, getDB().get("commands-list-usage"));
					return true;
				}
				Skyblock skyblock = Skyblock.getSkyblock(sender.getName());
				String list = "";
				if (args[1].toLowerCase().equals(getDB().get("commands-share"))) {
					LinkedTreeMap<String, Object> shares = skyblock.getShares();
					for (Object share : shares.values()) {
						list = list + share + ", ";
					}
					getDB().message(sender, getDB().get("sharelist") + list);
					return true;
				} else if (args[1].toLowerCase().equals(getDB().get("commands-invite"))) {
					LinkedTreeMap<String, Object> invites = skyblock.getInvites();
					for (Object invite : invites.values()) {
						list = list + ", " + invite;
					}
					getDB().message(sender, getDB().get("invitelist") + list);
					return true;
				} else {
					getDB().alert(sender, getDB().get("commands-list-usage"));
					return true;
				}
			} else if (args[0].toLowerCase().equals(getDB().get("commands-exit"))) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.ingame"));
					return true;
				}
				List<Skyblock> list = Skyblock.getShareSkyblockList((Player) sender);
				if (list.isEmpty()) {
					getDB().alert(sender, getDB().get("you-dont-have-share"));
					return true;
				}
				list.forEach((skyblock) -> skyblock.expulsion((Player) sender));
				getDB().message(sender, getDB().get("exit-skyblock"));
				return true;
			} else if (args[0].toLowerCase().equals(getDB().get("commands-inviteall"))) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.ingame"));
					return true;
				}
				Skyblock skyblock = Skyblock.getSkyblock(sender.getName());
				if (skyblock == null) {
					getDB().alert(sender, getDB().get("dont-have-skyblock"));
					return true;
				}

				if (skyblock.isInviteAll()) {
					skyblock.setInviteAll(false);
					getDB().message(sender, getDB().get("cancel-invite-all"));
				} else {
					skyblock.setInviteAll(true);
					getDB().message(sender, getDB().get("invite-all"));
				}
				return true;
			} else {
				getDB().alert(sender, getDB().get("commands-skyblock-usage"));
				return true;
			}
		}
		return true;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (player.isOp())
			return;
		Skyblock skyblock = Skyblock.getSkyblockByPos(event.getBlock());
		if (!event.getBlock().getLevel().getName().startsWith("skyblock")) {
			return;
		}
		if (skyblock == null || (!skyblock.isOwner(player) && !skyblock.isShare(player))) {
			event.setCancelled();
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (player.isOp())
			return;
		Skyblock skyblock = Skyblock.getSkyblockByPos(event.getBlock());
		if (!event.getBlock().getLevel().getName().startsWith("skyblock")) {
			return;
		}
		if (skyblock == null || (!skyblock.isOwner(player) && !skyblock.isShare(player))) {
			event.setCancelled();
		}
	}

	@EventHandler
	public void onTouch(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (player.isOp())
			return;
		Skyblock skyblock = Skyblock.getSkyblockByPos(event.getBlock());
		if (!event.getBlock().getLevel().getName().startsWith("skyblock")) {
			return;
		}
		if (skyblock == null || (!skyblock.isOwner(player) && !skyblock.isShare(player))) {
			event.setCancelled();
		}
	}
}
