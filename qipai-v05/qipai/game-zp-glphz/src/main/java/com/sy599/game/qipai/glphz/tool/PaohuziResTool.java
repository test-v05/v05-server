package com.sy599.game.qipai.glphz.tool;

import java.util.List;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.qipai.glphz.constant.PaohzCard;

public class PaohuziResTool {
	public static void buildPlayRes(PlayPaohuziRes.Builder builder, Player player, int action, List<PaohzCard> majiangs) {
		List<Integer> list = PaohuziTool.toPhzCardIds(majiangs);
		builder.addAllPhzIds(list);
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
	}

}
