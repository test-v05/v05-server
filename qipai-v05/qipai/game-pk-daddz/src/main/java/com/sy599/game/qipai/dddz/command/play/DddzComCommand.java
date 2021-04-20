package com.sy599.game.qipai.dddz.command.play;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.dddz.bean.DddzPlayer;
import com.sy599.game.qipai.dddz.bean.DddzTable;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class DddzComCommand extends BaseCommand<DddzPlayer> {

    @Override
    public void execute(DddzPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        DddzTable table = player.getPlayingTable(DddzTable.class);
        if (table == null) {
            return;
        }
        synchronized (table) {
            switch (req.getCode()) {
                case 131:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    break;
                case WebSocketMsgType.REQ_JIAOFEN:
                	 int fen = req.getParams(0);
                	 table.playJiaoFen(player, fen);
                    break;
                case WebSocketMsgType.REQ_XUANZHU:
                	int zhu = req.getParams(0);
                	table.playXuanzhu(player, zhu);
                    break;
                case WebSocketMsgType.RES_CHUPAI_RECORD://查出牌
                	table.playChuPaiRecord(player);
                    break;
//                case WebSocketMsgType.RES_Liushou:
//                	int color = req.getParams(0);
//                	table.playLiushou(player,color);
//                    break;
                case WebSocketMsgType.RES_TOUX:
                	int type = req.getParams(0);
                	//1 投降。0bu投降
                	table.playTouxiang(player,type);
                    break;
                case WebSocketMsgType.res_code_pk_dddz_dzztdct:
                    int ztct = req.getParams(0);
                    //1 da。0buda
                    table.playZtct(player,ztct);
                    break;
                case WebSocketMsgType.res_code_pk_dddz_koudipai:
                    int koudipai = req.getParams(0);
                    //1 kou 0 bukou
                    table.playKouDiPai(player,koudipai);
                    break;
                case 3119://WebSocketMsgType.RES__XTBP_CHADI://庄查底牌
//                    int type = req.getParams(0);
                    table.playChaDi(player);
                    break;
            }
            if (table.isAutoPlay()&&!player.isAutoPlay()) {
                player.setAutoPlay(false, table);
                player.setLastOperateTime(System.currentTimeMillis());
            }
        }
    }

    @Override
    public void setMsgTypeMap() {

    }



}
