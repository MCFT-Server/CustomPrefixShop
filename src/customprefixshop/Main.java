package customprefixshop;

import java.util.HashMap;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import mycash.cash.Account;
import mycash.exception.PlayerNotHaveEnoughCashExeception;
import myprefix.prefix.PrefixManager;

public class Main extends PluginBase {
	private Map<CommandSender, String> inputlist = new HashMap<>();

	@Override
	public void onEnable() {
		initConfig();
	}

	public void initConfig() {
		saveDefaultConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof ConsoleCommandSender) {
			sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.ingame"));
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		switch (args[0].toLowerCase()) {
		case "구매":
			if (isInput(sender)) {
				processBuy(sender, getInputList().get(sender));
				break;
			}
			if (args.length < 2) {
				alert(sender, "/개인칭호 구매 <칭호>");
				break;
			}
			if (isBannedPrefix(args[1])) {
				alert(sender, args[1] + "에는 사용할 수 없는 단어가 포함되어 있습니다.");
				alert(sender, "칭호명을 바꿔서 구매해주세요.");
				break;
			}
			message(sender, "칭호 [" + args[1] + TextFormat.DARK_AQUA + "]를 " + getConfig().getString("price")
					+ "캐쉬에 구매하시려면 30초 내로 /개인칭호 구매 명령어를 한번더 입력해주세요.");
			message(sender, "구매하기 원치 않는다면 /개인칭호 취소 명령어를 입력해주세요.");
			addInput(sender, args[1]);
			break;
		case "취소":
			removeInput(sender);
			alert(sender, "칭호 구매를 취소했습니다.");
			break;
		default:
			return false;
		}
		return true;
	}

	private void processBuy(CommandSender sender, String prefix) {
		Account account = new Account((Player) sender);
		try {
			account.reduceCash(getConfig().getInt("price"));
			PrefixManager.getInstance().addPrefix(sender.getName(), prefix);
			message(sender, "칭호를 성공적으로 구입했습니다. 칭호를 설정하시려면 /칭호 설정 명령어를 이용해 설정해주세요.");
		} catch (PlayerNotHaveEnoughCashExeception e) {
			alert(sender, "당신은 칭호를 구입하기 위한 캐쉬가 부족합니다.");
		} finally {
			removeInput(sender);
		}
	}

	private void alert(CommandSender sender, String msg) {
		sender.sendMessage(TextFormat.RED + "[개인칭호] " + msg);
	}

	private void message(CommandSender sender, String msg) {
		sender.sendMessage(TextFormat.DARK_AQUA + "[개인칭호] " + msg);
	}

	private boolean isInput(CommandSender sender) {
		return inputlist.containsKey(sender);
	}

	private void addInput(CommandSender sender, String prefix) {
		inputlist.put(sender, prefix);
		getServer().getScheduler().scheduleDelayedTask(() -> {
			removeInput(sender);
		}, 30 * 20);
	}

	private void removeInput(CommandSender sender) {
		inputlist.remove(sender);
	}

	public Map<CommandSender, String> getInputList() {
		return inputlist;
	}

	public boolean isBannedPrefix(String prefix) {
		for (String banprefix : getConfig().getStringList("banprefix")) {
			if (TextFormat.clean(prefix.toLowerCase()).contains(TextFormat.clean(banprefix.toLowerCase()))) {
				return true;
			}
		}
		return false;
	}
}
