package com.sy599.game.qipai.zhengzmj.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.zhengzmj.bean.ZhengzMjDisAction;
import com.sy599.game.qipai.zhengzmj.bean.ZhengzMjPlayer;
import com.sy599.game.qipai.zhengzmj.bean.ZhengzMjTable;
import com.sy599.game.qipai.zhengzmj.rule.ZhengzMj;
import com.sy599.game.qipai.zhengzmj.tool.ZhengzMjHelper;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZhengzMjPlayCommand extends BaseCommand {
	@Override
	public void execute(Player player0, MessageUnit message) throws Exception {
		ZhengzMjPlayer player = (ZhengzMjPlayer) player0;
		ZhengzMjTable table = player.getPlayingTable(ZhengzMjTable.class);
		if (table == null) {
			return;
		}

		// 该牌局是否能打牌
		int canPlay = table.isCanPlay();
		if (canPlay != 0) {
			if (canPlay == 1) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
			} else if (canPlay == 2) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_7));
			}
			return;
		}

		if (table.getState() != table_state.play) {
			return;
		}
		PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
		int action = playCard.getCardType();
		List<Integer> majiangIds = playCard.getCardIdsList();

		if (action == ZhengzMjDisAction.action_pass) {
			Map<Integer, List<Integer>> actionMap = table.getActionSeatMap();
			if (actionMap != null && actionMap.containsKey(player.getSeat())) {
				List<Integer> list = actionMap.get(player.getSeat());
				// 检查过
				if (!majiangIds.isEmpty()) {
					// 是否已经摸牌
					majiangIds = new ArrayList<>(majiangIds);
					int playIsMo = majiangIds.remove((int) 0);
					int isAlreadyMo = player.isAlreadyMoMajiang() ? 1 : 0;
					if (playIsMo != isAlreadyMo) {
						//player.writeErrMsg("过---手牌数量错误!!");
						return;
					}
					// 比较动作
					if (!DataMapUtil.compareVal(list, majiangIds)) {
						//player.writeErrMsg("过---动作错误!!");
						return;
					}
					majiangIds.clear();
				}
			}
		}
		List<ZhengzMj> majiangs = ZhengzMjHelper.toMajiang(majiangIds);
		table.playCommand(player, majiangs, action);
		player.setAutoPlay(false,false);
		player.setCheckAutoPlay(false);
	}

	@Override
	public void setMsgTypeMap() {

	}

}
