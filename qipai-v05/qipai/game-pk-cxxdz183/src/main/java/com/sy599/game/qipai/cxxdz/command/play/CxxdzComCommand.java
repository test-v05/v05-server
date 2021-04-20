package com.sy599.game.qipai.cxxdz.command.play;


import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;

import com.sy599.game.qipai.cxxdz.bean.CxxdzPlayer;
import com.sy599.game.qipai.cxxdz.bean.CxxdzTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;
import java.util.Map;

public class CxxdzComCommand extends BaseCommand<CxxdzPlayer> {

    @Override
    public void execute(CxxdzPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        CxxdzTable table = player.getPlayingTable(CxxdzTable.class);
        if (table == null) {
            return;
        }
        boolean flag=false;
        synchronized (table) {
            switch (req.getCode()) {
                case 131:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    break;
                case WebSocketMsgType.com_cxxdz_mengzhua://闷抓
                    table.menzhua(req.getParams(0),player);
                    flag=true;
                    break;
                case WebSocketMsgType.com_cxxdz_qdz://抢地主
                    table.qdz(req.getParams(0),player);
                    flag=true;
                    break;
                case WebSocketMsgType.com_cxxdz_t1j://踢一脚
                    table.t1j(req.getParams(0),player);
                    flag=true;
                    break;
                case WebSocketMsgType.com_cxxdz_menT://闷踢
                    table.menT(req.getParams(0),player);
                    flag=true;
                    break;
                case WebSocketMsgType.com_cxxdz_h1j://回一脚
                    table.h1j(req.getParams(0),player);
                    flag=true;
                    break;
            }
            LogUtil.msgLog.info(table.getId()+"|"+player.getUserId()+"|CxxdzComCommand|"+req.getCode()+"|"+req.getParamsList());
            if (flag) {
                player.setAutoPlay(false, table);
                player.setLastOperateTime(System.currentTimeMillis());
            }
        }
    }



    @Override
    public void setMsgTypeMap() {

    }



}
