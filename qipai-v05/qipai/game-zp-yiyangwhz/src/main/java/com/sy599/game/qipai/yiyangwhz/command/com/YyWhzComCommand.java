package com.sy599.game.qipai.yiyangwhz.command.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.yiyangwhz.bean.YyWhzPlayer;
import com.sy599.game.qipai.yiyangwhz.bean.YyWhzTable;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;

public class YyWhzComCommand extends BaseCommand {
    @Override
    public void execute(Player player0, MessageUnit message) throws Exception {
        YyWhzPlayer player = (YyWhzPlayer) player0;
        ComReq req = (ComReq) this.recognize(ComReq.class, message);

        YyWhzTable table = player.getPlayingTable(YyWhzTable.class);
        if (table == null) {
            return;
        }
        if (req.getCode()==131){
            player.setAutoPlay(false,table);
            player.setLastOperateTime(System.currentTimeMillis());
        }

//		if (req.getCode() == WebSocketMsgType.req_com_fangzhao) {
//			int pai = req.getParams(0);
//			LogUtil.msgLog.info("----tableId:" + table.getId() + "---userName:" + player.getName() + "------->已确认放招:" + pai);
//			player.setFangZhao(1);
//			List<Integer> cards = new ArrayList<>(Arrays.asList(pai));
//			table.play(player, cards, 0);
//			
//			// player.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
//			for (Player playerTemp : table.getSeatMap().values()) {
//				playerTemp.writeComMessage(WebSocketMsgType.res_code_phz_fangzhao, player.getUserId() + "", 1 + "");
//			}
//		}

    }

    @Override
    public void setMsgTypeMap() {

    }

}
