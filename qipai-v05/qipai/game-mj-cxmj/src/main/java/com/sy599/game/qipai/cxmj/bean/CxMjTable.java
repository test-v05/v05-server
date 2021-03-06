package com.sy599.game.qipai.cxmj.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sy599.game.qipai.cxmj.rule.MingTang;
import com.sy599.game.qipai.cxmj.tool.ting.TingTool;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.cxmj.constant.CxMjConstants;
import com.sy599.game.qipai.cxmj.constant.CxMj;
import com.sy599.game.qipai.cxmj.rule.CxMjRobotAI;
import com.sy599.game.qipai.cxmj.tool.CxMjHelper;
import com.sy599.game.qipai.cxmj.tool.CxMjQipaiTool;
import com.sy599.game.qipai.cxmj.tool.CxMjResTool;
import com.sy599.game.qipai.cxmj.tool.CxMjTool;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;


public class CxMjTable extends BaseTable {
    /**
	 * ??????????????????
	 */
    private List<CxMj> nowDisCardIds = new ArrayList<>();
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
	 * ?????????????????????????????? ??????????????????????????????????????? 1??????????????????????????????????????? ??????????????????????????? ??????????????????
	 * 2???????????????????????????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????? ?????????????????????????????????????????????
	 */
    private Map<Integer, CxMjTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<CxMj> leftMajiangs = new ArrayList<>();
	/*** ??????map */
    private Map<Long, CxMjPlayer> playerMap = new ConcurrentHashMap<Long, CxMjPlayer>();
	/*** ????????????????????? */
    private Map<Integer, CxMjPlayer> seatMap = new ConcurrentHashMap<Integer, CxMjPlayer>();
	private List<Integer> huConfirmList = new ArrayList<>();// ????????????
    /**
	 * ????????????seat
	 */
    private int moMajiangSeat;
    /**
	 * ???????????????
	 */
    private CxMj moGang;
    /**
	 * ???????????????
	 */
    private int moGangSeat;
    private int moGangSameCount;
    /**
	 * ?????????
	 */
    private List<Integer> moGangHuList = new ArrayList<>();
    /**
	 * ????????????
	 **/
    private int dealDice;
    /**
	 * 0????????? 1?????????
	 **/
    private int canDianPao;
    /**
	 * ???7???
	 **/
    private int hu7dui=1;

	private int isAutoPlay = 0;// ????????????????????????
    
    private int readyTime = 0 ;

	// private int auto_ready_time = 15000;//??????????????????????????????
    /**
	 * ???????????????
	 **/
    private int qiangGangHu;
    /**
	 * ????????????
	 **/
    private int dianGangKeHu;
    /**
	 * ??????????????????
	 **/
    private int qiangGangHuBaoSanJia;
    /**
	 * ??????
	 **/
    private int diFen=1;
	// ???????????????0??????1???
    private int jiaBei=0;
	// ?????????????????????xx???????????????
    private int jiaBeiFen=0;
	// ????????????????????????
    private int jiaBeiShu=0;

	// ??????????????????????????????
    private int bankerRand=0;
	// ???????????????
    private int finishFapai=0;
	// ?????? 0:?????? 1:?????????1 2:?????????2 3:???3 4:???????????? 5:????????????
    private int piaoFenType=0;
	/** ??????1????????????2????????? */
    private int autoPlayGlob;
	private int autoTableCount;
	/*** ????????????????????? */
    private List<Integer> moTailPai = new ArrayList<>();
    //??????below??????
    private int belowAdd=0;
    private int below=0;

    List<Integer> paoHuSeat=new ArrayList<>();
    //??????????????????
    private int lastId=0;
    //???????????????
    private int fangGangSeat=0;
    //1????????? 0??????
    private int bufengding =0;





    //----------------------
    //???5??????
    private int nian5=0;

    //??????2????????????????????????????????????2???????????????????????????????????????
    private int qiHu2Fen=0;


    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
        long id = getCreateTableId(player.getUserId(), play);
        TableInf info = new TableInf();
        info.setMasterId(player.getUserId());
        info.setRoomId(0);
        info.setPlayType(play);
        info.setTableId(id);
        info.setTotalBureau(bureauCount);
        info.setPlayBureau(1);
        info.setServerId(GameServerConfig.SERVER_ID);
        info.setCreateTime(new Date());
        info.setDaikaiTableId(daikaiTableId);
        info.setExtend(buildExtend());
        TableDao.getInstance().save(info);
        loadFromDB(info);


		// 0?????????1??????Id
		payType = StringUtil.getIntValue(params, 2, 1);// ????????????
		maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// ??????
        canDianPao = StringUtil.getIntValue(params, 4, 1);// 0:??????????????????
        nian5 = StringUtil.getIntValue(params, 5, 0);// 0:??????????????????
        qiHu2Fen =  StringUtil.getIntValue(params, 15, 0);// 2????????????
        bufengding =StringUtil.getIntValue(params, 16, 0);// 1????????? 0??????
        isAutoPlay = StringUtil.getIntValue(params, 8, 0);// ????????????
        if(isAutoPlay==1) {
            // ??????1??????
            isAutoPlay=60;
        }
        autoPlayGlob = StringUtil.getIntValue(params, 9, 0);// ????????????3
        if(maxPlayerCount==2){
            jiaBei = StringUtil.getIntValue(params, 10, 0);
            jiaBeiShu = StringUtil.getIntValue(params, 11, 1);
            jiaBeiFen = StringUtil.getIntValue(params, 12, 100);
            int belowAdd = StringUtil.getIntValue(params, 13, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 14, 0);
            if(below<=100&&below>=0){
                this.below=below;
            }

        }

        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
			// getRoomModeMap().put("1", "1"); //?????????????????????
        }
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        for (CxMjPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putString(5, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(6, moMajiangSeat);
        if (moGang != null) {
            wrapper.putInt(7, moGang.getId());
        } else {
            wrapper.putInt(7, 0);
        }
        wrapper.putString(8, StringUtil.implode(moGangHuList, ","));
        wrapper.putInt(9, canDianPao);
        wrapper.putInt(10, hu7dui);

        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putInt(11, maxPlayerCount);
        wrapper.putInt(12, isAutoPlay);
        wrapper.putInt(13, moGangSeat);
        wrapper.putInt(14, moGangSameCount);
        wrapper.putString(15, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(16, diFen);
        wrapper.putInt(17, jiaBei);
        wrapper.putInt(18, jiaBeiFen);
        wrapper.putInt(19, jiaBeiShu);
        wrapper.putInt(20, bankerRand);
        wrapper.putString(21,StringUtil.implode(paoHuSeat, ","));
        wrapper.putInt(22, autoPlayGlob);
        wrapper.putInt(23, finishFapai);
        wrapper.putInt(24, belowAdd);
        wrapper.putInt(25, below);
        wrapper.putInt(26, nian5);
        wrapper.putInt(27, lastId);
        wrapper.putInt(28, fangGangSeat);
        wrapper.putInt(29, qiHu2Fen);
        wrapper.putInt(30, bufengding);
        return wrapper;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (CxMjPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        String huListstr = wrapper.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmList = StringUtil.explodeToIntList(huListstr);
        }
        moMajiangSeat = wrapper.getInt(6, 0);
        int moGangMajiangId = wrapper.getInt(7, 0);
        if (moGangMajiangId != 0) {
            moGang = CxMj.getMajang(moGangMajiangId);
        }
        String moGangHu = wrapper.getString(8);
        if (!StringUtils.isBlank(moGangHu)) {
            moGangHuList = StringUtil.explodeToIntList(moGangHu);
        }
        canDianPao = wrapper.getInt(9, 1);
        hu7dui = wrapper.getInt(10, 0);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(11, 4);
        isAutoPlay = wrapper.getInt(12, 0);

        if(isAutoPlay ==1) {
            isAutoPlay=60;
        }

        moGangSeat = wrapper.getInt(13, 0);
        moGangSameCount = wrapper.getInt(14, 0);
        String s = wrapper.getString(15);
        if (!StringUtils.isBlank(s)) {
            moTailPai = StringUtil.explodeToIntList(s);
        }
        diFen = wrapper.getInt(16,1);
        jiaBei = wrapper.getInt(17,0);
        jiaBeiFen = wrapper.getInt(18,0);
        jiaBeiShu = wrapper.getInt(19,0);
        bankerRand = wrapper.getInt(20,0);
        s = wrapper.getString(21);
        if (!StringUtils.isBlank(s)) {
            paoHuSeat = StringUtil.explodeToIntList(s);
        }
        autoPlayGlob= wrapper.getInt(22,0);
        finishFapai= wrapper.getInt(23,0);
        belowAdd= wrapper.getInt(24,0);
        below= wrapper.getInt(25,0);
        nian5= wrapper.getInt(26,0);
        lastId= wrapper.getInt(27,0);
        fangGangSeat= wrapper.getInt(28,0);
        qiHu2Fen= wrapper.getInt(29,0);
        bufengding= wrapper.getInt(30,0);
    }



    public int getDealDice() {
        return dealDice;
    }

    public int getIsAutoPlay() {
        return isAutoPlay;
    }

    public void setIsAutoPlay(int isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
    }

    public void setDealDice(int dealDice) {
        this.dealDice = dealDice;
    }

    public boolean isHu7dui() {
        return hu7dui == 1;
    }

    public void setHu7dui(int hu7dui) {
        this.hu7dui = hu7dui;
    }

    public int getCanDianPao() {
        return canDianPao;
    }

    public int getNian5() {
        return nian5;
    }

    public void setCanDianPao(int canDianPao) {
        this.canDianPao = canDianPao;
    }

    @Override
    protected boolean quitPlayer1(Player player) {
        return false;
    }

    @Override
    protected boolean joinPlayer1(Player player) {
        return false;
    }

    @Override
    public int isCanPlay() {
        return 0;
    }


    @Override
    public void calcOver() {
        List<Integer> winList = new ArrayList<>(huConfirmList);
        StringBuilder sb = new StringBuilder("CxMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        LogUtil.msgLog.info( sb.toString()+"|start|calcOver");
        boolean selfMo = false;
        int replay =0;
		List<Integer> cardsId = null;// ??????ID
		Map<Integer, Integer> seatBridMap = new HashMap<>();// ??????,?????????
		int catchBirdSeat = 0;// ???????????????
        if (winList.size() == 0 && leftMajiangs.size()<=getMaxPlayerCount()) {
            //?????????????????????????????????2???
                //2021???5???17??? ??????
                //            LogUtil.msgLog.info( sb.toString()+"|?????????????????????????????????2???");
                //            List<CxMjPlayer> tings=new ArrayList<>();
                //            List<CxMjPlayer> losers=new ArrayList<>();
                //            for (CxMjPlayer player:seatMap.values()) {
                //                List<Integer> tingP = TingTool.getTing(player.getHandPais());
                //                if(tingP.size()>0)
                //                    tings.add(player);
                //                else
                //                    losers.add(player);
                //            }
                //            for (CxMjPlayer tingP:tings) {
                //                for (CxMjPlayer loseP:losers) {
                //                    loseP.changePoint(-2);
                //                    tingP.changePoint(2);
                //                    addlogmsg(loseP,(replay++)+"|??????-2");
                //                    addlogmsg(tingP,(replay++)+"|???+2");
                //                }
                //            }
                            for (CxMjPlayer p:seatMap.values()) {
                                p.changePoint(0);
                            }
        } else {
			// ??????????????????????????????
            if (winList.size() == 1 && seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
                selfMo = true;
            }
            if(seatMap.get(winList.get(0)).getVirtualHu()!=0){
                selfMo=true;
            }

            if (selfMo) {
				// ??????
                CxMjPlayer winner = seatMap.get(winList.get(0));
                addlogmsg( winner,(replay++)+"|??????="+selfMo);
                int winFen=0;
                int loseFen= MingTang.getMingTangFen(winner.getHuType(),diFen, bufengding);
                addlogmsg( winner,(replay++)+"|???|diFen="+diFen+"|??????loseFen="+loseFen);
                if(fangGangSeat!=0){
                    loseFen*=(getMaxPlayerCount()-1);
                    CxMjPlayer fgPlayer = seatMap.get(fangGangSeat);
                    fgPlayer.changePoint(-loseFen);
                    winFen+=loseFen;
                    addlogmsg( fgPlayer,(replay++)+"|?????????"+loseFen+"fen");
                }else {
                    for (int seat : seatMap.keySet()) {
                        if (!winList.contains(seat)) {
                            CxMjPlayer player = seatMap.get(seat);
                            player.changePoint(-loseFen);
                            winFen+=loseFen;
                            addlogmsg( player,(replay++)+"|???"+loseFen+"fen");
                        }
                    }
                }
                winner.changePoint(winFen);
                winner.addZiMoNum(1);
                addlogmsg( winner,(replay++)+"|???"+winFen+"fen");
            } else {
				// ???????????? ??????1???
				// ??????????????????????????????
                CxMjPlayer losePlayer = seatMap.get(disCardSeat);
                addlogmsg( losePlayer,(replay++)+"|??????????????????1???");
                losePlayer.addDianPaoNum(1);
                int loseFen=0;
                for (int winnerSeat : winList) {
                    CxMjPlayer winPlayer = seatMap.get(winnerSeat);
                    int winFen = MingTang.getMingTangFen( winPlayer.getHuType(), diFen, bufengding);
                    addlogmsg( winPlayer,(replay++)+"|?????????winFen="+winFen+"fen|difen="+diFen);
                    winPlayer.changePoint(winFen);
                    winPlayer.addJiePaoNum(1);
                    loseFen+=winFen;
                }
                losePlayer.changePoint(-loseFen);
                addlogmsg( losePlayer,(replay++)+"|???"+loseFen+"???" );
            }
			// ??????????????????????????????
            for(int seat:seatMap.keySet()){
                CxMjPlayer player = seatMap.get(seat);
                addlogmsg( player,(replay++)+"|???"+player.getPoint()+"???" );
                addlogmsg( player,(replay++)+"|???"+player.getWinLostPiaoFen()+"???" );
                player.setPoint(player.getPoint()+player.getWinLostPiaoFen());
                player.changeTotalPoint(player.getWinLostPiaoFen());
                addlogmsg( player,(replay++)+"|???Point="+player.getPoint()+"???" );
                addlogmsg( player,(replay++)+"|???TotalPoint="+player.getTotalPoint()+"???" );
            }

        }
        LogUtil.msgLog.info( sb.toString()+"|End|calcOver");
        boolean over = playBureau == totalBureau;
        if(autoPlayGlob >0) {
			// //????????????
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (CxMjPlayer seat : seatMap.values()) {
                 	if(seat.isAutoPlay()) {
                     	diss = true;
                     	break;
                     }
                     
                 }
			} else if (autoPlayGlob == 3) {
				diss = checkAuto3();
			}
            if(diss) {
            	 autoPlayDiss= true;
            	over =true;
            }
        }

        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList, cardsId, null, seatBridMap, catchBirdSeat, false);
        if (!winList.isEmpty()) {
            if (winList.size() > 1) {
				// ???????????????????????????????????????
                setLastWinSeat(disCardSeat);
            } else {
                setLastWinSeat(winList.get(0));
            }
		} else if (leftMajiangs.isEmpty()) {// ??????
            setLastWinSeat(moMajiangSeat);
        }
        calcAfter();
        saveLog(over, 0l, res.build());
        if (over) {
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
        } else {
            initNext();
            calcOver1();
        }
        for (CxMjPlayer player : seatMap.values()) {
            if (player.isAutoPlaySelf()) {
                player.setAutoPlay(false, false);
                //player.clearShuangGangData();
            }
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }

    }

    public int calcGAutoGangHuWinFen(CxMjPlayer winplayer,List<Integer> vir_hand){
            // ??????
            List<CxMj> mj = CxMjHelper.toMajiang(vir_hand);
            List<Integer> list = MingTang.get(winplayer.getCardTypes(),mj,this);
//            setHuType(list);
           // CxMjPlayer winner = winplayer;
            int winSeat = winplayer.getSeat();
            int winFen=0;
            int loseFen= MingTang.getMingTangFen(list,diFen, bufengding);
            if(fangGangSeat!=0){
                loseFen*=(getMaxPlayerCount()-1);
                winFen+=loseFen;
            }else {
                for (int seat : seatMap.keySet()) {
                    if (seat!=winSeat) {
                        winFen+=loseFen;
                    }
                }
            }
            return  winFen;
    }
	private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (CxMjPlayer seat : seatMap.values()) {
			if (seat.isAutoPlay()) {
				diss2 = true;
				break;
			}
		}
		if (diss2) {
			autoTableCount += 1;
		} else {
			autoTableCount = 0;
		}
		if (autoTableCount == 3) {
			diss = true;
		}
		// }
		return diss;
	}

    public void setWinLostPiaoFen(CxMjPlayer win, CxMjPlayer lost) {
        if(piaoFenType>0){
            lost.setWinLostPiaoFen(-win.getPiaoFen()-lost.getPiaoFen());
            win.setWinLostPiaoFen(win.getWinLostPiaoFen()+win.getPiaoFen()+lost.getPiaoFen());
        }
    }


    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingMjInfoRes res = (ClosingMjInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildMJClosingInfoResLog(res));
        String logOtherRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
        Date now = TimeUtil.now();
        UserPlaylog userLog = new UserPlaylog();
        userLog.setLogId(playType);
        userLog.setTableId(id);
        userLog.setRes(logRes);
        userLog.setTime(now);
        userLog.setTotalCount(totalBureau);
        userLog.setCount(playBureau);
        userLog.setStartseat(lastWinSeat);
        userLog.setOutCards(playLog);
        userLog.setExtend(logOtherRes);
		userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setMaxPlayerCount(maxPlayerCount);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);
        for (CxMjPlayer player : playerMap.values()) {
            player.addRecord(logId, playBureau);
        }
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

    public String getMasterName() {
        Player master = PlayerManager.getInstance().getPlayer(creatorId);
        String masterName = "";
        if (master == null) {
            masterName = UserDao.getInstance().selectNameByUserId(creatorId);
        } else {
            masterName = master.getName();
        }
        return masterName;
    }

    private int calcBirdPoint(int[] seatBirdArr, int seat) {
        return seatBirdArr[seat];
    }

    /**
	 * ??????
	 *
	 * @return
	 */
    private int[] zhuaNiao(boolean isQingHu, int birdNum) {
        int[] birdMjIds = null;
        if (birdNum == 10) {
            CxMj birdMj = getLeftMajiang();
            if (birdMj != null) {
                if (birdMj.isFeng() || birdMj.isZhongFaBai()) {
                    birdMjIds = new int[1];
                    birdMjIds[0] = birdMj.getId();
                } else {
                    birdMjIds = new int[3];
                    int[] birdMjVals = new int[3];
                    if (birdMj.getVal() % 10 == 1) {
                        birdMjVals[0] = birdMj.getHuase() * 10 + 9;
                        birdMjVals[1] = birdMj.getVal();
                        birdMjVals[2] = birdMj.getVal() + 1;
                    } else if (birdMj.getVal() % 10 == 9) {
                        birdMjVals[0] = birdMj.getVal() - 1;
                        birdMjVals[1] = birdMj.getVal();
                        birdMjVals[2] = birdMj.getHuase() * 10 + 1;
                    } else {
                        birdMjVals[0] = birdMj.getVal() - 1;
                        birdMjVals[1] = birdMj.getVal();
                        birdMjVals[2] = birdMj.getVal() + 1;
                    }
                    for (int i = 0; i < birdMjVals.length; i++) {
                        if (i == 1) {
                            birdMjIds[i] = birdMj.getId();
                        } else {
                            CxMj mj = CxMj.getMajiangByValue(birdMjVals[i]);
                            if (mj != null) {
                                birdMjIds[i] = mj.getId();
                            }
                        }
                    }
                }
            }
        } else {
            birdNum = isQingHu ? birdNum + 1 : birdNum;
            if (birdNum > leftMajiangs.size()) {
                birdNum = leftMajiangs.size();
            }
			// ?????????
            birdMjIds = new int[birdNum];
            for (int i = 0; i < birdNum; i++) {
                CxMj birdMj = getLeftMajiang();
                if (birdMj != null) {
                    birdMjIds[i] = birdMj.getId();
                }
            }
        }
        return birdMjIds;
    }


    @Override
    public Map<String, Object> saveDB(boolean asyn) {
        if (id < 0) {
            return null;
        }
        Map<String, Object> tempMap = loadCurrentDbMap();
        if (!tempMap.isEmpty()) {
            tempMap.put("tableId", id);
            tempMap.put("roomId", roomId);
            if (tempMap.containsKey("players")) {
                tempMap.put("players", buildPlayersInfo());
            }
            if (tempMap.containsKey("outPai1")) {
                tempMap.put("outPai1", seatMap.get(1).buildOutPaiStr());
            }
            if (tempMap.containsKey("outPai2")) {
                tempMap.put("outPai2", seatMap.get(2).buildOutPaiStr());
            }
            if (tempMap.containsKey("outPai3")) {
                tempMap.put("outPai3", seatMap.get(3).buildOutPaiStr());
            }
            if (tempMap.containsKey("outPai4")) {
                tempMap.put("outPai4", seatMap.get(4).buildOutPaiStr());
            }
            if (tempMap.containsKey("handPai1")) {
                tempMap.put("handPai1", StringUtil.implode(seatMap.get(1).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai2")) {
                tempMap.put("handPai2", StringUtil.implode(seatMap.get(2).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai3")) {
                tempMap.put("handPai3", StringUtil.implode(seatMap.get(3).getHandPais(), ","));
            }
            if (tempMap.containsKey("handPai4")) {
                tempMap.put("handPai4", StringUtil.implode(seatMap.get(4).getHandPais(), ","));
            }
            if (tempMap.containsKey("answerDiss")) {
                tempMap.put("answerDiss", buildDissInfo());
            }
            if (tempMap.containsKey("nowDisCardIds")) {
                tempMap.put("nowDisCardIds", StringUtil.implode(CxMjHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(CxMjHelper.toMajiangIds(leftMajiangs), ","));
            }
            if (tempMap.containsKey("nowAction")) {
                tempMap.put("nowAction", buildNowAction());
            }
            if (tempMap.containsKey("extend")) {
                tempMap.put("extend", buildExtend());
            }
        }
        return tempMap.size() > 0 ? tempMap : null;
    }



    @Override
    public int getPlayerCount() {
        return playerMap.size();
    }

    @Override
    protected void sendDealMsg() {
        sendDealMsg(0);
    }

    @Override
    protected void sendDealMsg(long userId) {
        int dealDice = 0;
        Random r = new Random();
        dealDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + CxMjDisAction.action_dice + "_" + dealDice);
        setDealDice(dealDice);
		// ??????????????????
        logFaPaiTable();
        for (CxMjPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            if (lastWinSeat == tablePlayer.getSeat()) {
                List<Integer> actionList = tablePlayer.checkMo(null);
                if(isQiHu2Fen(tablePlayer.getSeat(), actionList)){
                    //actionList.clear();
                    if(!actionList.contains(1)){
                        //2???????????????????????? ???????????????????????? ??????????????????
                        actionList.clear();
                    }
                }
                if (!actionList.isEmpty()) {
                    addActionSeat(tablePlayer.getSeat(), actionList);
                    res.addAllSelfAct(actionList);
                    logFaPaiPlayer(tablePlayer, actionList);
                }
            }
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            res.setDealDice(dealDice);
//			if (userId == tablePlayer.getUserId()) {
//				continue;
//			}
            tablePlayer.writeSocket(res.build());
            if (tablePlayer.isAutoPlay()) {
                tablePlayer.setAutoPlayTime(0);
            }
            sendTingInfo(tablePlayer);
            logFaPaiPlayer(tablePlayer, null);
            if(tablePlayer.isAutoPlay()) {
            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + CxMjConstants.action_tuoguan + "_" +1+ tablePlayer.getExtraPlayLog());
            }
        }
        for (Player player : getRoomPlayerMap().values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            res.setDealDice(dealDice);
            player.writeSocket(res.build());
        }
        if (playBureau == 1) {
            setCreateTime(new Date());
        }
    }

    public void moMajiang(CxMjPlayer player, boolean isBuZhang) {
        if (state != table_state.play) {
            return;
        }
        if (player.isRobot()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
		// ??????
        CxMj majiang = null;
        if (disCardRound != 0) {
			// ????????????????????????????????????????????????
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (getLeftMajiangCount() <=getMaxPlayerCount()) {
                calcOver();
                return;
            }
            if (GameServerConfig.isDebug() && zp != null && zp.size()==1) {
                majiang=CxMj.getMajiangByValue(zp.get(0).get(0));
                zp.clear();
            }else {
                majiang = getLeftMajiang();
            }
        }
        if (majiang != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CxMjDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
            player.moMajiang(majiang);
            //System.out.println(disCardRound + "_" + player.getSeat() + "_" + CxMjDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
        }
		// ????????????
        clearActionSeatMap();
        if (disCardRound == 0) {
            return;
        }
        if (isBuZhang) {
            addMoTailPai(-1);
        }
        lastId=majiang.getId();
        setMoMajiangSeat(player.getSeat());
        List<Integer> arr = player.checkMo(majiang);
        if(isQiHu2Fen(player.getSeat(), arr)){
            //arr.clear();
            if(!arr.contains(1)){
                //2???????????????????????? ???????????????????????? ??????????????????
                arr.clear();
            }
        }
        if (!arr.isEmpty()) {
            if(arr.contains(99)){
                addlogmsg(player,"shuang Gang Hu");
                return;
            }
            addActionSeat(player.getSeat(), arr);
        }
        logMoMj(player, majiang, arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setRemain(getLeftMajiangCount());
        for (CxMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                MoMajiangRes.Builder copy = res.clone();
                copy.addAllSelfAct(arr);
                if (majiang != null) {
                    copy.setMajiangId(majiang.getId());
                }
                seat.writeSocket(copy.build());
            } else {
                seat.writeSocket(res.build());
            }
        }

//        if(getLeftMajiangCount()==getMaxPlayerCount()){
//            if(arr.contains(1)){
//                if(arr.get(CxMjConstants.ACTION_INDEX_HU)!=1&&arr.get(CxMjConstants.ACTION_INDEX_MINGGANG)!=1){
//                    calcOver();
//                    return;
//                }
//            }else {
//                calcOver();
//                return;
//            }
//        }
        sendTingInfo(player);
        for (Player roomPlayer : roomPlayerMap.values()) {
            MoMajiangRes.Builder copy = res.clone();
            roomPlayer.writeSocket(copy.build());
        }

    }

    public void gangBuPai(Integer id,CxMjPlayer player){
        if(id!=1004&&id!=1005)
            return;
        if(player.isAlreadyMoMajiang())
            return;
        player.moMajiang(CxMj.getMajang(id));
        lastId=id;
        List<Integer> arr = player.checkMo(null);

        addActionSeat(player.getSeat(),arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setRemain(getLeftMajiangCount());
        for (CxMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                MoMajiangRes.Builder copy = res.clone();
                copy.addAllSelfAct(arr);
                copy.setMajiangId(id);
                seat.writeSocket(copy.build());
            } else {
                seat.writeSocket(res.build());
            }
        }
        player.setBuId(id);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CxMjDisAction.action_moMjiang + "_" + id + player.getExtraPlayLog());
        sendTingInfo(player);
        for (Player roomPlayer : roomPlayerMap.values()) {
            MoMajiangRes.Builder copy = res.clone();
            roomPlayer.writeSocket(copy.build());
        }

    }

    /**
	 * ???????????????
	 *
	 * @param player
	 * @param majiangs
	 */
    public void hu(CxMjPlayer player, List<CxMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || (actionList.get(CxMjConstants.ACTION_INDEX_HU) != 1
				&& actionList.get(CxMjConstants.ACTION_INDEX_ZIMO) != 1)) {// ?????????????????????????????????????????????????????????
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();


        CxMj huCard=null;
        boolean zimo = player.isAlreadyMoMajiang();
        if(lastId!=0)
            huCard= CxMj.getMajang(lastId);
        if(player.getVirtualHu()!=0){
            huCard=CxMj.getMajang(player.getVirtualHu());
            zimo=true;
        }
        if(huCard!=null&&!player.getHandMajiang().contains(huCard))
            player.getHandMajiang().add(huCard);
        List<CxMj> huHand = new ArrayList<>(player.getHandMajiang());
        if (!TingTool.isHu(CxMjHelper.toMajiangIds(huHand))) {
            System.err.println("~~~~990~~~~~~~~~~~~~~~~~~~?????????");
            return;
        }

        List<Integer> list = MingTang.get(player.getCardTypes(), player.getHandMajiang(),this);
        player.setHuType(list);
        if (!zimo) {
            builder.setFromSeat(disCardSeat);
        } else {
            builder.addHuArray(CxMjConstants.HU_ZIMO);
            player.getHuType().add(MingTang.MINGTANG_ZIMO);
        }

//        if (moGangHuList.contains(player.getSeat())) {
//            CxMjPlayer moGangPlayer = seatMap.get(moGangSeat);
//            if (moGangPlayer == null) {
//                moGangPlayer = getPlayerByHasMajiang(moGang);
//            }
//            if (moGangPlayer == null) {
//                moGangPlayer = seatMap.get(moMajiangSeat);
//            }
//            List<CxMj> moGangMajiangs = new ArrayList<>();
//            moGangMajiangs.add(moGang);
//            moGangPlayer.addOutPais(moGangMajiangs, CxMjDisAction.action_chupai, 0);
//			// ?????????????????? ??????????????????????????????
//            recordDisMajiang(moGangMajiangs, moGangPlayer);
////			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CxMjDisAction.action_chupai + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
//            moGangPlayer.qGangUpdateOutPais(moGang);
//        }
        buildPlayRes(builder, player, action, huHand);
        if (zimo) {
            builder.setZimo(1);
        }
        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
		// ???
        for (CxMjPlayer seat : seatMap.values()) {
			// ????????????
            seat.writeSocket(builder.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
		// ??????????????????
        addHuList(player.getSeat());
        changeDisCardRound(1);
        List<CxMj> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getHuType(), ",") + player.getExtraPlayLog());
        logActionHu(player, majiangs, "");
        if (isCalcOver()) {
			// ?????????????????? ???????????????????????????
            calcOver();
        } else {
            //removeActionSeat(player.getSeat());
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip, action);
        }
    }

    public void hushuangGangHu(CxMjPlayer player, List<CxMj> majiangs, int action, List<CxMj> huHand) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (actionList == null || (actionList.get(CxMjConstants.ACTION_INDEX_HU) != 1
                && actionList.get(CxMjConstants.ACTION_INDEX_ZIMO) != 1)) {// ?????????????????????????????????????????????????????????
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();


        CxMj huCard=null;
        boolean zimo = player.isAlreadyMoMajiang();
        if(lastId!=0)
            huCard= CxMj.getMajang(lastId);
        if(player.getVirtualHu()!=0){
            huCard=CxMj.getMajang(player.getVirtualHu());
            zimo=true;
        }
//        if(huCard!=null&&!player.getHandMajiang().contains(huCard))
//            player.getHandMajiang().add(huCard);

        List<Integer> list = MingTang.get(player.getCardTypes(), player.getHandMajiang(),this);
        player.setHuType(list);
        if (!zimo) {
            builder.setFromSeat(disCardSeat);
        } else {
            builder.addHuArray(CxMjConstants.HU_ZIMO);
            player.getHuType().add(MingTang.MINGTANG_ZIMO);
        }

//        if (moGangHuList.contains(player.getSeat())) {
//            CxMjPlayer moGangPlayer = seatMap.get(moGangSeat);
//            if (moGangPlayer == null) {
//                moGangPlayer = getPlayerByHasMajiang(moGang);
//            }
//            if (moGangPlayer == null) {
//                moGangPlayer = seatMap.get(moMajiangSeat);
//            }
//            List<CxMj> moGangMajiangs = new ArrayList<>();
//            moGangMajiangs.add(moGang);
//            moGangPlayer.addOutPais(moGangMajiangs, CxMjDisAction.action_chupai, 0);
//			// ?????????????????? ??????????????????????????????
//            recordDisMajiang(moGangMajiangs, moGangPlayer);
////			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CxMjDisAction.action_chupai + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
//            moGangPlayer.qGangUpdateOutPais(moGang);
//        }
        buildPlayRes(builder, player, action, huHand);
        if (zimo) {
            builder.setZimo(1);
        }
        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
        // ???
        for (CxMjPlayer seat : seatMap.values()) {
            // ????????????
            seat.writeSocket(builder.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        // ??????????????????
        addHuList(player.getSeat());
        changeDisCardRound(1);
        List<CxMj> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getHuType(), ",") + player.getExtraPlayLog());
        logActionHu(player, majiangs, "");
        if (isCalcOver()) {
            // ?????????????????? ???????????????????????????
            calcOver();
        } else {
            //removeActionSeat(player.getSeat());
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip, action);
        }
    }
    public void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<CxMj> majiangs) {
        CxMjResTool.buildPlayRes(builder, player, action, majiangs);
        buildPlayRes1(builder);
    }

    private void buildPlayRes1(PlayMajiangRes.Builder builder) {
        // builder
    }

    /**
	 * ?????????????????????????????????
	 *
	 * @param majiang
	 * @return
	 */
    private CxMjPlayer getPlayerByHasMajiang(CxMj majiang) {
        for (CxMjPlayer player : seatMap.values()) {
            if (player.getHandMajiang() != null && player.getHandMajiang().contains(majiang)) {
                return player;
            }
            if (player.getOutMajing() != null && player.getOutMajing().contains(majiang)) {
                return player;
            }
        }
        return null;
    }

    private boolean isCalcOver() {
        List<Integer> huActionList = getHuSeatByActionMap();
        boolean over = false;
        if (!huActionList.isEmpty()) {
            over = true;
            for (int huseat : huActionList) {
                if (!huConfirmList.contains(huseat) &&
                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == CxMjDisAction.action_hu)) {
                    over = false;
                    break;
                }
            }
        }

        if (!over) {
            CxMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                CxMjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }

        return over;
    }

    /**
	 * ??????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    private void chiPengGang(CxMjPlayer player, List<CxMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        logAction(player, action, majiangs, null);
        if (majiangs == null || majiangs.isEmpty()) {
            return;
        }

        if (!checkAction(player, majiangs, new ArrayList<Integer>(), action)) {
			LogUtil.msg("???????????????????????????????????????");
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        CxMj disMajiang = null;
        if (nowDisCardIds.size() > 1) {
			// ????????????????????????
            return;
        }
        List<Integer> huList = getHuSeatByActionMap();
        huList.remove((Object) player.getSeat());
        if (!huList.isEmpty()) {
            return;
        }
        if (!nowDisCardIds.isEmpty()) {
            disMajiang = nowDisCardIds.get(0);
        }
        int sameCount = 0;
        if (majiangs.size() > 0) {
            sameCount = CxMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
		// ???????????? ????????????????????????????????????
        if (action == CxMjDisAction.action_minggang) {
            majiangs = CxMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount >= 4) {
				// ???4????????????????????????
                action = CxMjDisAction.action_angang;
                majiangs=majiangs.subList(0,4);
            }
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        boolean hasQGangHu = false;
        if (action == CxMjDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount);
            if (!can) {
                return;
            }
        } else if (action == CxMjDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount);

            if (!can) {
                return;
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        } else if (action == CxMjDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            ArrayList<CxMj> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());

			// ???????????????????????????????????????????????????
            if (sameCount == 1 && canGangHu()) {
                if (checkQGangHu(player, majiangs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
					LogUtil.msg("tid:" + getId() + " " + player.getName() + "????????????????????????");
                }
            }
			// ????????????
            if (sameCount == 3 && dianGangKeHu == 1) {
                if (checkQGangHu(player, mjs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
					LogUtil.msg("tid:" + getId() + " " + player.getName() + "????????????????????????");
                }
            }
        } else {
            return;
        }
        if (disMajiang != null) {
            if ((action == CxMjDisAction.action_minggang && sameCount == 3)
                    || action == CxMjDisAction.action_peng || action == CxMjDisAction.action_chi) {
                if (action == CxMjDisAction.action_chi) {
					majiangs.add(1, disMajiang);// ?????????????????????
                } else {
                    if(!majiangs.contains(disMajiang))
                        majiangs.add(disMajiang);
                }
                builder.setFromSeat(disCardSeat);
                seatMap.get(disCardSeat).removeOutPais(nowDisCardIds, action);
            }
        }
        switch (action){
            case CxMjDisAction.action_minggang:
            case CxMjDisAction.action_angang:
                gang(builder,player,majiangs,action);
                break;
            default:
                chiPeng(builder, player, majiangs, action, hasQGangHu, sameCount);
                break;
        }
    }

    /**
	 * ?????????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 * @return
	 */
    private boolean checkQGangHu(CxMjPlayer player, List<CxMj> majiangs, int action, int sameCount) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huListMap = new HashMap<>();
        for (CxMjPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
			// ????????????
            List<Integer> hu = seatPlayer.checkDisMajiang(majiangs.get(0), this.canGangHu() || dianGangKeHu == 1);
            if(isQiHu2Fen(player.getSeat(), hu)){
                //hu.clear();
                if(!hu.contains(1)){
                    //2???????????????????????? ???????????????????????? ??????????????????
                    hu.clear();
                }
            }
            if (!hu.isEmpty() && hu.get(0) == 1) {
                addActionSeat(seatPlayer.getSeat(), hu);
                huListMap.put(seatPlayer.getSeat(), hu);
            }
        }
		// ????????????
        if (!huListMap.isEmpty()) {
            setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()), player, sameCount);
            buildPlayRes(builder, player, action, majiangs);
            for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
                PlayMajiangRes.Builder copy = builder.clone();
                CxMjPlayer seatPlayer = seatMap.get(entry.getKey());
                copy.addAllSelfAct(entry.getValue());
                seatPlayer.writeSocket(copy.build());
            }
            return true;
        }
        return false;
    }

    private void chiPeng(PlayMajiangRes.Builder builder, CxMjPlayer player, List<CxMj> majiangs, int action, boolean hasQGangHu, int sameCount) {
        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        if (!hasQGangHu) {
            clearActionSeatMap();
        }
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        setNowDisCardSeat(player.getSeat());
        List<Integer> list = player.checkMo(null);
        if(isQiHu2Fen(player.getSeat(), list)){
            //list.clear();
            if(!list.contains(1)){
                //2???????????????????????? ???????????????????????? ??????????????????
                list.clear();
            }
        }
        if (!list.isEmpty()&&list.size()>0) {
            list.set(0,0);
            if(list.contains(1))
                addActionSeat(player.getSeat(), list);
        }
        for (CxMjPlayer seatPlayer : seatMap.values()) {
			// ????????????
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        sendTingInfo(player);
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        logAction(player, action, majiangs, actList);
    }

    private void gang(PlayMajiangRes.Builder builder, CxMjPlayer player, List<CxMj> majiangs, int action) {
        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        removeActionSeat(player.getSeat());
        // ??????????????????
        setNowDisCardSeat(player.getSeat());
        player.getVirtualGang().clear();
        player.addGangNum();
        int []arr=new int[6];
        if(player.getGangNum()==1){
            List<Integer> copy1 = new ArrayList<>(player.getHandPais());
            copy1.add(1004);
            Map<Integer,List<Integer>> gangM1 = CxMjTool.checkGang(player.getGangNum(),
                    copy1, CxMjHelper.toMajiangValMap(player.getPeng()),1004);
            if(gangM1.size()>0){
                for (Map.Entry<Integer,List<Integer>> entry:gangM1.entrySet()) {
                    if(entry.getKey()==0){
                        arr[CxMjConstants.ACTION_INDEX_HU]=1;
                        player.setVirtualHu(entry.getKey());
                    }else {
                        arr[CxMjConstants.ACTION_INDEX_MINGGANG]=1;
                        List<Integer> virtualGang = player.getVirtualGang();
                        for(Integer id:entry.getValue()){
                            if(!virtualGang.contains(id)){
                                virtualGang.add(id);
                            }
                        }
                    }
                }
            }

            List<Integer> copy2 = new ArrayList<>(player.getHandPais());
            copy2.add(1005);
            Map<Integer,List<Integer>> gangM2 = CxMjTool.checkGang(player.getGangNum(),
                    copy2, CxMjHelper.toMajiangValMap(player.getPeng()),1005);
            if(gangM2.size()>0){
                for (Map.Entry<Integer,List<Integer>> entry:gangM2.entrySet()) {
                    if(entry.getKey()==0){
                        arr[CxMjConstants.ACTION_INDEX_HU]=1;
                        player.setVirtualHu(entry.getKey());
                    }else {
                        arr[CxMjConstants.ACTION_INDEX_MINGGANG]=1;
                        List<Integer> virtualGang = player.getVirtualGang();
                        for(Integer id:entry.getValue()){
                            if(!virtualGang.contains(id)){
                                virtualGang.add(id);
                            }
                        }
                    }
                }
            }


            if(gangM1.size()>0&&gangM2.size()>0){
                //????????????????????? ??????45?????????
                for (CxMjPlayer seatPlayer : seatMap.values()) {
                    // ????????????
                    seatPlayer.writeSocket(builder.build());
                }
//                player.writeComMessage(WebSocketMsgType.res_code_cxmj_gangBu);
                List<Integer> hand4 = new ArrayList<>(player.getHandPais());
                hand4.add(1004);
                List<Integer> hand5 = new ArrayList<>(player.getHandPais());
                hand5.add(1005);
                List<Integer> actionList_ = Arrays.asList(0,0,0,0,0,0);
                actionList_.set(CxMjConstants.ACTION_INDEX_HU,1);

                int fen4= calcGAutoGangHuWinFen(player,hand4);
                int fen5 =calcGAutoGangHuWinFen(player,hand5);
                actionSeatMap.put(player.getSeat(),actionList_);

                if(fen4>=fen5){
                   // player.dealHandPais(CxMjHelper.toMajiang(hand4));
                   // hu(player,CxMjHelper.toMajiang(hand4),CxMjDisAction.action_hu);
                    player.setVirtualHu(1004);
                    hu(player,new ArrayList<>(),CxMjDisAction.action_hu);
                }else{
                  //  player.dealHandPais(CxMjHelper.toMajiang(hand5));
                    player.setVirtualHu(1005);
                    hu(player,new ArrayList<>(),CxMjDisAction.action_hu);
//                    hu(player,CxMjHelper.toMajiang(hand5),CxMjDisAction.action_hu);
                }

            }
            else {
                int nowBu;
                if(gangM2.size()==0){
                    nowBu=1004;
                }else {
                    nowBu=1005;
                }
                player.moMajiang(CxMj.getMajang(nowBu));
                buPai(nowBu,player);
                List<Integer> list=new ArrayList<>();
                for (int val : arr) {
                    list.add(val);
                }
                addActionSeat(player.getSeat(), list);
                player.sendGangMsg();
                for (CxMjPlayer seatPlayer : seatMap.values()) {
                    // ????????????
                    PlayMajiangRes.Builder copy = builder.clone();
                    if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                        copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
                    }
                    seatPlayer.writeSocket(copy.build());
                }
                if(arr[CxMjConstants.ACTION_INDEX_HU]==1&&arr[CxMjConstants.ACTION_INDEX_MINGGANG]==0){
                    player.setVirtualHu(nowBu);
                    hu(player,new ArrayList<>(),CxMjDisAction.action_hu);
                }
            }
        }else if(player.getGangNum()==2){
            List<Integer> copy = new ArrayList<>(player.getHandPais());
            int bu=player.getBuId();
            int nowBu;
            if(bu==1004){
                nowBu=1005;
            }else {
                nowBu=1004;
            }
            copy.add(nowBu);
            player.moMajiang(CxMj.getMajang(nowBu));
            buPai(nowBu,player);
            Map<Integer,List<Integer>> gangM = CxMjTool.checkGang(player.getGangNum(),
                    copy, CxMjHelper.toMajiangValMap(player.getPeng()),1004);
            if(gangM.size()>0)
                arr[CxMjConstants.ACTION_INDEX_HU]=1;
            List<Integer> list=new ArrayList<>();
            for (int val : arr) {
                list.add(val);
            }
            addActionSeat(player.getSeat(), list);
            for (CxMjPlayer seatPlayer : seatMap.values()) {
                // ????????????
                PlayMajiangRes.Builder clone = builder.clone();
                if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                    clone.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
                }
                seatPlayer.writeSocket(clone.build());
            }
            player.setVirtualHu(nowBu);
            hu(player,new ArrayList<>(),CxMjDisAction.action_hu);
        }
    }


    public void buPai(Integer id,CxMjPlayer player){
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + CxMjDisAction.action_moMjiang + "_" + id + player.getExtraPlayLog());
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setRemain(getLeftMajiangCount());
        player.setBuId(id);
        for (CxMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                res.setMajiangId(id);
                seat.writeSocket(res.build());
            } else {
                seat.writeSocket(res.build());
            }
        }
    }

    /**
	 * ????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    private void chuPai(CxMjPlayer player, List<CxMj> majiangs, int action) {
        if (state != table_state.play) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (majiangs.size() != 1) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (!tempActionMap.isEmpty()) {
			LogUtil.e(player.getName() + "???????????????????????????");
            clearTempAction();
        }
        if (!player.isAlreadyMoMajiang()) {
			// ???????????????
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
		if (!actionSeatMap.isEmpty()) {// ??????????????????????????????
            guo(player, null, CxMjDisAction.action_pass);
        }
        if (!actionSeatMap.isEmpty()) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
		// ????????????
        clearActionSeatMap();
        setNowDisCardSeat(calcNextSeat(player.getSeat()));
        if(majiangs.size()==1)
            lastId=majiangs.get(0).getId();
        recordDisMajiang(majiangs, player);
        player.addOutPais(majiangs, action, player.getSeat());
        logAction(player, action, majiangs, null);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        for (CxMjPlayer p : seatMap.values()) {
            List<Integer> list;
            if (p.getUserId() != player.getUserId()) {
                list = p.checkDisMajiang(majiangs.get(0), this.canDianPao());
                if(list==null||list.isEmpty())
                    continue;
                if(list.contains(99)){
                    //????????????hu????????????
                    addlogmsg(player,"shuang Gang Hu");
                    return;
                }
                isQiHu2FenDis(p.getSeat(), list ,majiangs.get(0));
                if (list.contains(1)) {
                    addActionSeat(p.getSeat(), list);
                    p.setLastCheckTime(System.currentTimeMillis());
                    logChuPaiActList(p, majiangs.get(0), list);
                    if(list.get(CxMjConstants.ACTION_INDEX_MINGGANG)==1){
                        if(getMaxPlayerCount()==4){
                            if(getLeftMajiangCount()<=8){
                                fangGangSeat=player.getSeat();//4????????????8
                            }
                        }
                        if(getMaxPlayerCount()==3){
                            if(getLeftMajiangCount()<=6){
                                fangGangSeat=player.getSeat();//3????????????6
                            }
                        }
                         if(getMaxPlayerCount()==2){
                            if(getLeftMajiangCount()<=4){
                                fangGangSeat=player.getSeat();//2????????????4
                            }
                        }
                    }

                }
            }
        }
//        for (int i = 0; i < l.size(); i++) {
//            hu(l.get(i),majiangs,CxMjDisAction.action_hu);
//        }
        sendDisMajiangAction(builder, player);

		// ??????????????????
        checkMo();
    }
    public void addlogmsg(CxMjPlayer player,String str){
        StringBuilder sb = new StringBuilder("Cxmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|"+player.getName());
        sb.append("|"+str);
        LogUtil.msgLog.info(sb.toString());
    }
    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(CxMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(CxMjConstants.ACTION_INDEX_ZIMO) == 1) {
				// ???
                huList.add(seat);
            }
        }
        return huList;
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder, CxMjPlayer player) {
        for (CxMjPlayer seatPlayer : seatMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            List<Integer> actionList;
			// ???????????????????????????????????????????????????????????????????????????????????????
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                actionList = actionSeatMap.get(seatPlayer.getSeat());
            } else {
                actionList = new ArrayList<>();
            }
            copy.addAllSelfAct(actionList);
            seatPlayer.writeSocket(copy.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
    }

    public synchronized void playCommand(CxMjPlayer player, List<CxMj> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
	 * ??????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    public synchronized void playCommand(CxMjPlayer player, List<CxMj> majiangs, List<Integer> hucards, int action) {
        if (state != table_state.play) {
            return;
        }
		// ???????????????
        if (!moGangHuList.isEmpty()) {
            if (!moGangHuList.contains(player.getSeat())) {
				// ???????????????????????????????????? ?????????????????????
                return;
            }
        }
        if (action == CxMjDisAction.action_minggang|| action == CxMjDisAction.action_angang){
            if(player == null){
                return;
            }
            if( null!=player.getShuangGangData() && (player.getShuangGangData().size()>0 || player.checkCanShuangGangHu())){
                if(player.isAlreadyMoMajiang()){
                    player.shuangGangHu();
                }else{
                   player.shuangGangHuDis();
                }
            }
        }
        if(table_state.over ==state){
            return;
        }
        if (CxMjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }
		// ???????????????????????????
        if (action != CxMjDisAction.action_minggang)
            if (!isHandCard(majiangs,player.getHandMajiang())) {
                return;
            }
        changeDisCardRound(1);
        if (action == CxMjDisAction.action_pass) {
            guo(player, majiangs, action);
        } else if (action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
        }
		// ?????????????????????????????????
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    private boolean isHandCard(List<CxMj> majiangs,List<CxMj> handCards){
        for (CxMj mj:majiangs) {
            if(mj==CxMj.mj1004||mj==CxMj.mj1005)
                continue;
            if(!handCards.contains(mj))
                return false;
        }
        return true;
    }


    private void passMoHu(CxMjPlayer player, List<CxMj> majiangs, int action) {
        if (!moGangHuList.contains(player.getSeat())) {
            return;
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

        //CxMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
        if (moGangHuList.isEmpty()) {
            CxMjPlayer moGangPlayer = seatMap.get(getNowDisCardSeat());
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            if (moGangPlayer.getaGang().contains(moGang)) {
                calcPoint(moGangPlayer, CxMjDisAction.action_angang, 4, majiangs);
            } else {
                calcPoint(moGangPlayer, CxMjDisAction.action_minggang, moGangSameCount > 0 ? moGangSameCount : 1, majiangs);
            }

            moMajiang(moGangPlayer, true);
//			builder = PlayMajiangRes.newBuilder();
//			chiPengGang(builder, moGangPlayer, majiangs, CxMjDisAction.action_minggang, false);
        }

    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(CxMjPlayer player, List<CxMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if(paoHuSeat.contains(player.getSeat())){
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_911));
            return;
        }
        if(player.getGangNum()>0){
            LogUtil.msg("?????????????????????");
            player.writeErrMsg("??????????????????");
            return;
        }

        if (!moGangHuList.isEmpty()) {
			// ???????????????????????????
            passMoHu(player, majiangs, action);
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (isCalcOver()) {
            calcOver();
            return;
        }
        if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// ??????
            player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        logAction(player, action, majiangs, removeActionList);
        if (!actionSeatMap.isEmpty()) {
            CxMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
            buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
            for (int seat : actionSeatMap.keySet()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                PlayMajiangRes.Builder copy = disBuilder.clone();
                copy.addAllSelfAct(new ArrayList<>());
                if (actionList != null && !tempActionMap.containsKey(seat) && !huConfirmList.contains(seat)) {
                    copy.addAllSelfAct(actionList);
                    CxMjPlayer seatPlayer = seatMap.get(seat);
                    seatPlayer.writeSocket(copy.build());
                }
            }
        }
        if (player.isAlreadyMoMajiang()) {
            sendTingInfo(player);
        }
		refreshTempAction(player);// ?????? ???????????????????????????????????????????????????????????????
        checkMo();
    }

    private void calcPoint(CxMjPlayer player, int action, int sameCount, List<CxMj> majiangs) {
        int lostPoint = 0;
        int getPoint = 0;
        int[] seatPointArr = new int[getMaxPlayerCount() + 1];
        if (action == CxMjDisAction.action_peng) {
            return;

        } else if (action == CxMjDisAction.action_angang) {
			// ??????????????????????????????2???
            lostPoint = -2;
            getPoint = 2 * (getMaxPlayerCount() - 1);

        } else if (action == CxMjDisAction.action_minggang) {
            if (sameCount == 1) {
				// ????????????????????????????????????1???
				// ???????????????3???

                if (player.isPassGang(majiangs.get(0))) {
					// ???????????? ???????????????????????? ???????????? ???????????????
                    return;
                }
                lostPoint = -1;
                getPoint = 1 * (getMaxPlayerCount() - 1);
            }
			// ??????
            if (sameCount == 3) {
                int bei=diFen;
                CxMjPlayer disPlayer = seatMap.get(disCardSeat);
//                disPlayer.changeLostPoint(-bei);
                seatPointArr[disPlayer.getSeat()] = -bei;
//                player.changeLostPoint(bei);
                seatPointArr[player.getSeat()] = bei;
            }
        }
        getPoint=getPoint*diFen;
        lostPoint=lostPoint*diFen;
        if (lostPoint != 0) {
            for (CxMjPlayer seat : seatMap.values()) {
                if (seat.getUserId() == player.getUserId()) {
//                    player.changeLostPoint(getPoint);
                    seatPointArr[player.getSeat()] = getPoint;
                } else {
//                    seat.changeLostPoint(lostPoint);
                    seatPointArr[seat.getSeat()] = lostPoint;
                }
            }
        }
//        for (CxMjPlayer p : seatMap.values()) {
//            p.changePointArr(2,p.getLostPoint());
//        }

        String seatPointStr = "";
        for (int i = 1; i <= getMaxPlayerCount(); i++) {
            seatPointStr += seatPointArr[i] + ",";
        }
        seatPointStr = seatPointStr.substring(0, seatPointStr.length() - 1);
        ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_gangFen, seatPointStr);
        GeneratedMessage msg = res.build();
        broadMsgToAll(msg);

        if (action != CxMjDisAction.action_chi) {
//            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + CxMjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog() + "_" + seatPointStr);
        }
    }

    private void recordDisMajiang(List<CxMj> majiangs, CxMjPlayer player) {
        setNowDisCardIds(majiangs);
        // changeDisCardRound(1);
        setDisCardSeat(player.getSeat());
    }


    public void setNowDisCardIds(List<CxMj> nowDisCardIds) {
        if (nowDisCardIds == null) {
            this.nowDisCardIds.clear();

        } else {
            this.nowDisCardIds = nowDisCardIds;

        }
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    /**
	 * ????????????
	 */
    public void checkMo() {
        if (actionSeatMap.isEmpty()) {
            if (nowDisCardSeat != 0) {
                moMajiang(seatMap.get(nowDisCardSeat), false);
            }
            robotDealAction();

        } else {
            for (int seat : actionSeatMap.keySet()) {
                CxMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
					// ????????????????????????????????????
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<CxMj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = CxMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(CxMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(CxMjConstants.ACTION_INDEX_ZIMO) == 1) {
						// ???
                        playCommand(player, new ArrayList<CxMj>(), CxMjDisAction.action_hu);

                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, CxMjDisAction.action_angang);

                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, CxMjDisAction.action_minggang);

                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, CxMjDisAction.action_peng);
                    }
                }
                // else {
				// // ???????????????????????????
                // player.writeSocket(builder.build());
                // }

            }

        }
    }

    @Override
    protected void robotDealAction() {
        if (true) {
            return;
        }
        if (isTest()) {
            int nextseat = getNextDisCardSeat();
            CxMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<CxMj> list = null;
                    if (actionList.get(CxMjConstants.ACTION_INDEX_HU) == 1 || actionList.get(CxMjConstants.ACTION_INDEX_ZIMO) == 1) {
						// ???
                        playCommand(next, new ArrayList<CxMj>(), CxMjDisAction.action_hu);
                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_ANGANG) == 1) {
						// ???????????????
                        Map<Integer, Integer> handMap = CxMjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
								// ????????????
                                list = CxMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, CxMjDisAction.action_angang);

                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        Map<Integer, Integer> pengMap = CxMjHelper.toMajiangValMap(next.getPeng());
                        for (CxMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
								// ?????????
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, CxMjDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(CxMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(next, list, CxMjDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    CxMjQipaiTool.dropHongzhongVal(handMajiangs);
                    int maJiangId = CxMjRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<CxMj> majiangList = CxMjHelper.toMajiang(Arrays.asList(maJiangId));
                    if (next.isRobot()) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    playCommand(next, majiangList, 0);
                }

            }
        }
    }

    @Override
    protected void deal() {
        if (lastWinSeat == 0) {
            setLastWinSeat(playerMap.get(masterId).getSeat());
        }
        if (lastWinSeat == 0) {
            setLastWinSeat(1);
        }
        setDisCardSeat(lastWinSeat);
        setNowDisCardSeat(lastWinSeat);
        setMoMajiangSeat(lastWinSeat);

//        List<Integer> copy = CxMjConstants.getMajiangList(getMaxPlayerCount());
        List<Integer> copy = CxMjConstants.getMajiangList(3);
        addPlayLog(copy.size() + "");
        List<List<CxMj>> list = null;
        if (zp != null&&zp.size()!=0) {
            list = CxMjTool.fapai(copy, getMaxPlayerCount(), zp);
        } else {
            list = CxMjTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (CxMjPlayer player : seatMap.values()) {
            player.changeState(player_state.play);
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                continue;
            }
            player.dealHandPais(list.get(i));
            i++;
        }
		// ??????????????????
        setLeftMajiangs(list.get(getMaxPlayerCount()));
        finishFapai=1;
    }

    @Override
    public void startNext() {
		// ????????????
        // autoZiMoHu();
    }

    /**
	 * ???????????????????????????
	 *
	 * @param leftMajiangs
	 */
    public void setLeftMajiangs(List<CxMj> leftMajiangs) {
        if (leftMajiangs == null) {
            this.leftMajiangs.clear();
        } else {
            this.leftMajiangs = leftMajiangs;

        }
        dbParamMap.put("leftPais", JSON_TAG);
    }

    /**
	 * ?????????????????????
	 * @return
	 */
    public CxMj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            CxMj majiang = this.leftMajiangs.remove(0);
            dbParamMap.put("leftPais", JSON_TAG);
            return majiang;
        }
        return null;
    }

    @Override
    public int getNextDisCardSeat() {
        if (state != table_state.play) {
            return 0;
        }
        if (disCardRound == 0) {
            return lastWinSeat;
        } else {
            return nowDisCardSeat;
        }
    }

    /**
	 * ??????seat???????????????
	 *
	 * @param seat
	 * @return
	 */
    public int calcNextSeat(int seat) {
        return seat + 1 > maxPlayerCount ? 1 : seat + 1;
    }

    @Override
    public Player getPlayerBySeat(int seat) {
        return seatMap.get(seat);
    }

    @Override
    public Map<Integer, Player> getSeatMap() {
        Object o = seatMap;
        return (Map<Integer, Player>) o;
    }

    @Override
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
        res.setNowBurCount(getPlayBureau());
        res.setTotalBurCount(getTotalBureau());
        res.setGotyeRoomId(gotyeRoomId + "");
        res.setTableId(getId() + "");
        res.setWanfa(playType);
        res.addExt(payType);                //0
        res.addExt(canDianPao);            //2
        res.addExt(hu7dui);                 //4
        res.addExt(isAutoPlay);             //5
        res.addExt(qiangGangHu);            //6
        res.addExt(qiangGangHuBaoSanJia);   //7
        res.addExt(dianGangKeHu);           //11
        res.addExt(diFen);                  //12
        res.addStrExt(StringUtil.implode(moTailPai, ","));      //0

        res.setMasterId(getMasterId() + "");
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        res.setDealDice(dealDice);
        List<PlayerInTableRes> players = new ArrayList<>();
        for (CxMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null && moGangHuList.isEmpty()) {
                playerRes.addAllOutCardIds(CxMjHelper.toMajiangIds(nowDisCardIds));
            }
            playerRes.addRecover(player.getIsEntryTable());
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (actionSeatMap.containsKey(player.getSeat())) {
				if (!tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {// ????????????????????????
																												// ?????????????????????????????????
																												// ????????????????????????
                    playerRes.addAllRecover(actionSeatMap.get(player.getSeat()));
                }
            }
            players.add(playerRes.build());
        }
        res.addAllPlayers(players);
        if (actionSeatMap.isEmpty()) {
            int nextSeat = getNextDisCardSeat();
            if (nextSeat != 0) {
                res.setNextSeat(nextSeat);
            }
        } else if (!moGangHuList.isEmpty()) {
            for (CxMjPlayer player : seatMap.values()) {
                if (player.getmGang() != null && player.getmGang().contains(moGang)) {
                    res.setNextSeat(player.getSeat());
                    break;
                }
            }
        }
        res.setRenshu(getMaxPlayerCount());
        res.setLastWinSeat(getLastWinSeat());
        res.addTimeOut((int) CxMjConstants.AUTO_TIMEOUT);
        return res.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPlayer(long id, Class<T> cl) {
        return (T) playerMap.get(id);
    }

    @Override
    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
    }

    public int getPiaoFenType() {
        return piaoFenType;
    }

    public void setPiaoFenType(int piaoFenType) {
        this.piaoFenType = piaoFenType;
    }

    @Override
    public Map<Long, Player> getPlayerMap() {
        Object o = playerMap;
        return (Map<Long, Player>) o;
    }

    @Override
    protected void initNext1() {
        clearHuList();
        clearActionSeatMap();
        setLeftMajiangs(null);
        setNowDisCardIds(null);
        clearMoGang();
        setDealDice(0);
        clearMoTailPai();
        paoHuSeat.clear();
        readyTime=0;
        finishFapai=0;
        lastId=0;
        fangGangSeat=0;
    }

    public List<Integer> removeActionSeat(int seat) {
        List<Integer> actionList = actionSeatMap.remove(seat);
        if (moGangHuList.contains(seat)) {
            removeMoGang(seat);
        }
        saveActionSeatMap();
        return actionList;
    }

    public void addActionSeat(int seat, List<Integer> actionlist) {

        actionSeatMap.put(seat, actionlist);
        CxMjPlayer player = seatMap.get(seat);
        addPlayLog(disCardRound + "_" + seat + "_" + CxMjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
        saveActionSeatMap();
    }
    public boolean isQiHu2Fen(int seat, List<Integer> actionlist){
        if(null==actionlist || actionlist.isEmpty()){
            return false;
        }
        if(null==actionlist.get(0)){
            return false;
        }
        if(qiHu2Fen==1){
            if(actionlist.get(0)==1){
                //System.err.println("===="+actionlist);
                //?????????
                List<Integer> list = MingTang.get(seatMap.get(seat).getCardTypes(),seatMap.get(seat).getHandMajiang(),this);
                int fen =MingTang.getMingTangFen(list,diFen, bufengding);
                //System.err.println("name:"+  seatMap.get(seat).getName()+" ????????????ma :"+fen);
                if(fen <2 ){
                    //System.err.println("name:"+  seatMap.get(seat).getName()+" ????????????");
                    actionlist.set(0,0);//?????????
                    return true;
                }
            }
        }else{
            return false;
        }
        return false;
    }

    private boolean isQiHu2FenDis(int seat, List<Integer> actionlist,  CxMj cxMj) {
        if(null==actionlist || actionlist.isEmpty()){
            return false;
        }
        if(null==actionlist.get(0)){
            return false;
        }
        if(qiHu2Fen==1){
            if(actionlist.get(0)==1){
                System.err.println("===="+actionlist);
                //?????????
                List<CxMj> mjList = new ArrayList<>(seatMap.get(seat).getHandMajiang());
                mjList.add(cxMj);
                List<Integer> list = MingTang.get(seatMap.get(seat).getCardTypes(),mjList,this);
                int fen =MingTang.getMingTangFen(list,diFen, bufengding);
                System.err.println("name:"+  seatMap.get(seat).getName()+" ????????????ma :"+fen);
                if(fen <2 ){
                    System.err.println("name:"+  seatMap.get(seat).getName()+" ????????????");
                    actionlist.set(0,0);
                    return true;
                }
            }
        }else{
            return false;
        }
        return false;
    }

    public void clearActionSeatMap() {
        if (!actionSeatMap.isEmpty()) {
            actionSeatMap.clear();
            saveActionSeatMap();
        }
    }

    private void clearTempAction() {
        if (!tempActionMap.isEmpty()) {
            tempActionMap.clear();
            changeExtend();
        }
    }

    public void clearHuList() {
        huConfirmList.clear();
        changeExtend();
    }

    public void addHuList(int seat) {
        if (!huConfirmList.contains(seat)) {
            huConfirmList.add(seat);

        }
        changeExtend();
    }

    public void saveActionSeatMap() {
        dbParamMap.put("nowAction", JSON_TAG);
    }

    @Override
    protected void initNowAction(String nowAction) {
        JsonWrapper wrapper = new JsonWrapper(nowAction);
        for (int i = 1; i <= 4; i++) {
            String val = wrapper.getString(i);
            if (!StringUtils.isBlank(val)) {
                actionSeatMap.put(i, StringUtil.explodeToIntList(val));

            }
        }
    }

    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            nowDisCardIds = CxMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = CxMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }




    private Map<Integer, CxMjTempAction> loadTempActionMap(String json) {
        Map<Integer, CxMjTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            CxMjTempAction tempAction = new CxMjTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    /**
	 * ??????????????? ????????????????????????????????????????????????????????????
	 */
    private boolean checkAction(CxMjPlayer player, List<CxMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// ????????????????????? ???????????????
		if (!canAction) {// ??????????????? ??????????????????
            int seat = player.getSeat();
            tempActionMap.put(seat, new CxMjTempAction(seat, action, cardList, hucards));
			// ?????????????????????????????????????????? ?????????????????????
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (CxMjTempAction temp : tempActionMap.values()) {
                    if (temp.getAction() < maxAction) {
                        maxAction = temp.getAction();
                        maxSeat = temp.getSeat();
                    }
                    prioritySeats.put(temp.getSeat(), temp.getAction());
                }
                Set<Integer> maxPrioritySeats = new HashSet<>();
                for (int mActionSet : prioritySeats.keySet()) {
                    if (prioritySeats.get(mActionSet) == maxAction) {
                        maxActionSize++;
                        maxPrioritySeats.add(mActionSet);
                    }
                }
                if (maxActionSize > 1) {
                    maxSeat = getNearSeat(disCardSeat, new ArrayList<>(maxPrioritySeats));
                    maxAction = prioritySeats.get(maxSeat);
                }
                CxMjPlayer tempPlayer = seatMap.get(maxSeat);
                List<CxMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
                List<Integer> tempHuCards = tempActionMap.get(maxSeat).getHucards();
                for (int removeSeat : prioritySeats.keySet()) {
                    if (removeSeat != maxSeat) {
                        removeActionSeat(removeSeat);
                    }
                }
                clearTempAction();
				playCommand(tempPlayer, tempCardList, tempHuCards, maxAction);// ?????????????????????????????????
            } else {
                if (isCalcOver()) {
                    calcOver();
                }
            }
		} else {// ????????? ????????????????????????
            clearTempAction();
        }
        return canAction;
    }

    /**
	 * ??????????????????????????????????????????????????????
	 *
	 * @param player
	 */
    private void refreshTempAction(CxMjPlayer player) {
        tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();// ?????????????????????
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = CxMjDisAction.parseToDisActionList(actionList);
            int priorityAction = CxMjDisAction.getMaxPriorityAction(list);
            prioritySeats.put(seat, priorityAction);
        }
        int maxPriorityAction = Integer.MAX_VALUE;
        int maxPrioritySeat = 0;
		boolean isSame = true;// ?????????????????????
        for (int seat : prioritySeats.keySet()) {
            if (maxPrioritySeat != Integer.MAX_VALUE && maxPrioritySeat != prioritySeats.get(seat)) {
                isSame = false;
            }
            if (prioritySeats.get(seat) < maxPriorityAction) {
                maxPriorityAction = prioritySeats.get(seat);
                maxPrioritySeat = seat;
            }
        }
        if (isSame) {
            maxPrioritySeat = getNearSeat(disCardSeat, new ArrayList<>(prioritySeats.keySet()));
        }
        Iterator<CxMjTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            CxMjTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<CxMj> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                CxMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
                iterator.remove();
				playCommand(tempPlayer, tempCardList, tempHuCards, action);// ?????????????????????????????????
                break;
            }
        }
        changeExtend();
    }

    /**
	 * ????????????????????????????????? ????????????????????????????????????????????????????????????
	 *
	 * @param player
	 * @param action
	 * @return
	 */
    public boolean checkCanAction(CxMjPlayer player, int action) {
		// ???????????????????????????
        List<Integer> stopActionList = CxMjDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
				// ??????
                boolean can = CxMjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = CxMjDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {
					// ??????????????????????????? ????????????????????????
                    int actionSeat = entry.getKey();
                    int nearSeat = getNearSeat(nowDisCardSeat, Arrays.asList(player.getSeat(), actionSeat));
                    if (nearSeat != player.getSeat()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
	 * ????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
    private boolean canPeng(CxMjPlayer player, List<CxMj> majiangs, int sameCount) {
        if (player.isAlreadyMoMajiang()) {
            return false;
        }
        if (sameCount != 2) {
            return false;
        }
        if (nowDisCardIds.isEmpty()) {
            return false;
        }
        if (majiangs.get(0).getVal() != nowDisCardIds.get(0).getVal()) {
            return false;
        }
        return true;
    }

    /**
	 * ???????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
    private boolean canAnGang(CxMjPlayer player, List<CxMj> majiangs, int sameCount) {
        if (sameCount < 4) {
            return false;
        }
        if (player.getSeat() != getNextDisCardSeat()) {
            return false;
        }
        return true;
    }

    /**
	 * ???????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
    private boolean canMingGang(CxMjPlayer player, List<CxMj> majiangs, int sameCount) {
        List<CxMj> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = CxMjHelper.toMajiangVals(player.getPeng());
        if (majiangs.size() == 1) {
            if (player.getSeat() != getNextDisCardSeat()) {
                return false;
            }
            int id = majiangs.get(0).getId();
            if(id==1004||id==1005)
                return true;
            if (handMajiangs.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal()))
                return true;

        }else if(sameCount==3){
            return true;
        }
        return false;
    }

    public Map<Integer, List<Integer>> getActionSeatMap() {
        return actionSeatMap;
    }


    public void setMoMajiangSeat(int moMajiangSeat) {
        this.moMajiangSeat = moMajiangSeat;
        changeExtend();
    }

    /**
	 * ?????????????????????
	 *
	 * @param moGang
	 * @param moGangHuList
	 */
    public void setMoGang(CxMj moGang, List<Integer> moGangHuList, CxMjPlayer player, int sameCount) {
        this.moGang = moGang;
        this.moGangHuList = moGangHuList;
        this.moGangSeat = player.getSeat();
        this.moGangSameCount = sameCount;
        changeExtend();
    }

    /**
	 * ???????????????
	 */
    public void clearMoGang() {
        this.moGang = null;
        this.moGangHuList.clear();
        this.moGangSeat = 0;
        this.moGangSameCount = 0;
        changeExtend();
    }

    /**
	 * pass ?????????
	 *
	 * @param seat
	 */
    public void removeMoGang(int seat) {
        this.moGangHuList.remove((Object) seat);
        changeExtend();
    }

    public int getMoMajiangSeat() {
        return moMajiangSeat;
    }

    @Override
    protected String buildNowAction() {
        JsonWrapper wrapper = new JsonWrapper("");
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            wrapper.putString(entry.getKey(), StringUtil.implode(entry.getValue(), ","));
        }
        return wrapper.toString();
    }

    @Override
    public void setConfig(int index, int val) {

    }

    /**
	 * ????????????
	 *
	 * @return
	 */
    public boolean canGangHu() {
        return qiangGangHu == 1;
    }

	// ????????????
    public boolean canDianPao() {
        if (getCanDianPao() == 1) {
            return true;
        }
        return false;
    }

    /**
	 * @param over
	 * @param selfMo
	 * @param winList
	 * @param birdCardsId
	 *            ???ID
	 * @param seatBirds
	 *            ?????????
	 * @param seatBridMap
	 *            ??????
	 * @param isBreak
	 * @return
	 */
    public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, List<Integer> birdCardsId, int[] seatBirds, Map<Integer, Integer> seatBridMap, int catchBirdSeat, boolean isBreak) {

		// ????????????????????????
        if (over && jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (CxMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (CxMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //???????????????below???+belowAdd???
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (CxMjPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint >-below&&totalPoint<0) {
                    player.setTotalPoint(player.getTotalPoint()-belowAdd);
                }else if(totalPoint < below&&totalPoint>0){
                    player.setTotalPoint(player.getTotalPoint()+belowAdd);
                }
            }
        }

        List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
        List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
        int fangPaoSeat = selfMo ? 0 : disCardSeat;
        for (CxMjPlayer player : seatMap.values()) {
            ClosingMjPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }
            if (seatBridMap != null && seatBridMap.containsKey(player.getSeat())) {
                build.setBirdPoint(seatBridMap.get(player.getSeat()));
            } else {
                build.setBirdPoint(0);
            }
            if (winList != null && winList.contains(player.getSeat())) {
                if (!selfMo) {
					// ????????????

                    CxMj huMajiang = nowDisCardIds.get(0);
                    if (!build.getHandPaisList().contains(huMajiang.getId())) {
                        if(lastId!=0){
                           // ?????????4 5???
                        }else{
                            build.addHandPais(huMajiang.getId());
                        }
                    }
                    if(lastId!=0){
                            if(lastId==1004){//?????????4 5??? ????????????// mj13 mj14
                                build.setIsHu(1004);   ;
                            }else if(lastId==1005){
                                build.setIsHu(1005);   ;
                            }else{
                                build.setIsHu(huMajiang.getId());
                            }
                    }else{
                        build.setIsHu(huMajiang.getId());
                    }

                } else {
                    build.setIsHu(player.getLastMoMajiang().getId());
                }
            }
            if (player.getSeat() == fangPaoSeat) {
                build.setFanPao(1);
                if(huConfirmList.isEmpty())
                    build.setFanPao(0);
            }
            if (winList != null && winList.contains(player.getSeat())) {
				// ?????????????????????????????????????????????
                builderList.add(0, build);
            } else {
                builderList.add(build);
            }
			// ?????????
            if (isCreditTable()) {
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }
        }

		// ???????????????
        if (isCreditTable()) {
			// ??????????????????
            calcNegativeCredit();
            long dyjCredit = 0;
            for (CxMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                CxMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------??????????????????---------------------------------
            for (CxMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                CxMjPlayer player = seatMap.get(builder.getSeat());
                builder.setWinLoseCredit(player.getWinGold());
            }
        }
        for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }

        ClosingMjInfoRes.Builder res = ClosingMjInfoRes.newBuilder();
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllExt(buildAccountsExt(over?1:0));
        res.addCreditConfig(creditMode);                         //0
        res.addCreditConfig(creditJoinLimit);                    //1
        res.addCreditConfig(creditDissLimit);                    //2
        res.addCreditConfig(creditDifen);                        //3ClosingMjInfoRes
        res.addCreditConfig(creditCommission);                   //4
        res.addCreditConfig(creditCommissionMode1);              //5
        res.addCreditConfig(creditCommissionMode2);              //6
        res.addCreditConfig(creditCommissionLimit);              //7
        if (seatBirds != null) {
            res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
        }
        if (birdCardsId != null) {
            res.addAllBird(birdCardsId);
        }
        res.addAllLeftCards(CxMjHelper.toMajiangIds(leftMajiangs));
        res.setCatchBirdSeat(catchBirdSeat);
        res.addAllIntParams(getIntParams());
        for (CxMjPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        broadMsgRoomPlayer(res.build());
        return res;

    }


    public List<String> buildAccountsExt(int over) {
        List<String> ext = new ArrayList<>();
        if (isGroupRoom()) {
            ext.add(loadGroupId());
        } else {
            ext.add("0");
        }
        ext.add(id + "");                                //1
        ext.add(masterId + "");                            //2
        ext.add(TimeUtil.formatTime(TimeUtil.now()));    //3
        ext.add(playType + "");                            //4
        ext.add(canDianPao + "");                        //5
        ext.add(lastWinSeat + "");                            //6
        ext.add(isAutoPlay + "");                        //7
        ext.add(diFen + "");                            //8
        ext.add(isLiuJu() + "");                        //9
        ext.add(over+"");                               //10
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, 0, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return CxMjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }

//	@Override
//	public boolean isTest() {
//		return super.isTest() && CxMjConstants.isTest;
//	}

    @Override
    public void checkReconnect(Player player) {
        ((CxMjPlayer) player).checkAutoPlay(0, true);
        if (state == table_state.play) {
            CxMjPlayer player1 = (CxMjPlayer) player;
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
        if(((CxMjPlayer) player).getGangNum()==1&&actionSeatMap.isEmpty())
            player.writeComMessage(WebSocketMsgType.res_code_cxmj_gangBu);
//        sendPiaoReconnect(player);
        ((CxMjPlayer) player).sendGangMsg();
    }

    private void sendPiaoReconnect(Player player){
        if(piaoFenType==0||maxPlayerCount!=getPlayerCount())
            return;
        int count=0;
        for(Map.Entry<Integer, CxMjPlayer> entry:seatMap.entrySet()){
            player_state state = entry.getValue().getState();
            if(state==player_state.play||state==player_state.ready)
                count++;
        }
        if(count!=maxPlayerCount)
            return;

        for(Map.Entry<Integer, CxMjPlayer> entry:seatMap.entrySet()){
            CxMjPlayer p = entry.getValue();
            if(p.getUserId()==player.getUserId()){
                if(p.getPiaoFen()==-1){
//                    player.writeComMessage(WebSocketMsgType.res_code_cxmj_piaofen);
                    continue;
                }
            }else {
                List<Integer> l=new ArrayList<>();
                l.add((int)p.getUserId());
                l.add(p.getPiaoFen());
//                player.writeComMessage(WebSocketMsgType.res_code_cxmj_broadcast_piaofen, l);
            }
        }
    }

    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }

    @Override
    public void checkAutoPlay() {
        if (getSendDissTime() > 0) {
            for (CxMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }

        if (isAutoPlayOff()) {
            // ????????????
            for (int seat : seatMap.keySet()) {
                CxMjPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
            }
            return;
        }
        
        if (isAutoPlay < 1) {
            return;
        }

        if (state == table_state.play) {
            autoPlay();
        } else {
            if (getPlayedBureau() == 0) {
                return;
            }
            readyTime ++;

			// ????????????????????????xx???????????????????????????
            for (CxMjPlayer player : seatMap.values()) {
                if (player.getState() != player_state.entry && player.getState() != player_state.over) {
                    continue;
                } else {
                    if (readyTime >= 5 && player.isAutoPlay()) {
						// ????????????????????????5???????????????
                        autoReady(player);
                    } else if (readyTime > 30) {
                        autoReady(player);
                    }
                }
            }
        }

    }
    
    

    /**
	 * ????????????
	 */
    public synchronized void autoPlay() {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.isEmpty()) {
            List<Integer> huSeatList = getHuSeatByActionMap();
            if (!huSeatList.isEmpty()) {
				// ???????????????
                for (int seat : huSeatList) {
                    CxMjPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), CxMjDisAction.action_hu);
                }
                return;
            } else {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    seat = entry.getKey();
                    List<Integer> actList = CxMjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }

                    action = CxMjDisAction.getAutoMaxPriorityAction(actList);
                    CxMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
                        chuPai = true;
                    }
                    if(action == CxMjDisAction.action_minggang||action == CxMjDisAction.action_angang){
                        List<Integer> virtualGang = player.getVirtualGang();
                        List<CxMj> mjs = CxMjHelper.getMajiangList(player.getHandMajiang(), CxMj.getMajang(virtualGang.get(0)).getVal());
                        playCommand(player, mjs, CxMjDisAction.action_minggang);
                    }else if (action == CxMjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
							// ???????????????????????????
                            playCommand(player, new ArrayList<>(), CxMjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                CxMj mj = nowDisCardIds.get(0);
                                List<CxMj> mjList = new ArrayList<>();
                                for (CxMj handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, CxMjDisAction.action_peng);
//							autoChuPai(player);
                            }
                        }
                    } else {
                        playCommand(player, new ArrayList<>(), CxMjDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            boolean gangBu=false;
            CxMjPlayer p=null;
            for(CxMjPlayer player:seatMap.values()){
                if(player.getGangNum()==1){
                    gangBu=true;
                    p=player;
                    break;
                }
            }
            if(gangBu&&p!=null&&p.isAutoPlay()){
                gangBuPai(1005,p);
            }else {
                CxMjPlayer player = seatMap.get(nowDisCardSeat);
                if (player == null || !player.checkAutoPlay(0, false)) {
                    return;
                }
                autoChuPai(player);
            }
        }
    }

    public void autoChuPai(CxMjPlayer player) {

		// CxMjQipaiTool.dropHongzhongVal(handMajiangs);???????????????????????????
//					int mjId = CxMjRobotAI.getInstance().outPaiHandle(0, handMjIds, new ArrayList<>());
        if (!player.isAlreadyMoMajiang()) {
            return;
        }
        List<Integer> handMjIds = new ArrayList<>(player.getHandPais());
        int mjId = -1;
        if (moMajiangSeat == player.getSeat()) {
            mjId = handMjIds.get(handMjIds.size() - 1);
        } else {
            Collections.sort(handMjIds);
            mjId = handMjIds.get(handMjIds.size() - 1);
        }
        if (mjId != -1) {
            List<CxMj> mjList = CxMjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, CxMjDisAction.action_chupai);
        }
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }


    public synchronized void piaoFen(CxMjPlayer player, int fen){
        if (piaoFenType<=3||player.getPiaoFen()!=-1)
            return;
        if(fen>3||fen<0)
            return;
        if(piaoFenType==5&&playBureau>1)
            return;
        player.setPiaoFen(fen);
        StringBuilder sb = new StringBuilder("cxmj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("piaoFen").append("|").append(fen);
        LogUtil.msgLog.info(sb.toString());
        int confirmTime=0;
        for (Map.Entry<Integer, CxMjPlayer> entry : seatMap.entrySet()) {
//            entry.getValue().writeComMessage(WebSocketMsgType.res_code_cxmj_broadcast_piaofen, (int)player.getUserId(),player.getPiaoFen());
            if(entry.getValue().getPiaoFen()!=-1)
                confirmTime++;
        }
        if (confirmTime == maxPlayerCount) {
            checkDeal(player.getUserId());
        }
    }



	// ??????????????????
    public boolean isTwoPlayer() {
        return getMaxPlayerCount() == 2;
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_cxmj);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    public void logFaPaiTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(CxMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(CxMjPlayer player, int action, List<CxMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "unKnown-" + action;
        if (action == CxMjDisAction.action_peng) {
            actStr = "peng";
        } else if (action == CxMjDisAction.action_minggang) {
            actStr = "baoTing";
        } else if (action == CxMjDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == CxMjDisAction.action_pass) {
            actStr = "guo";
        } else if (action == CxMjDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == CxMjDisAction.action_chi) {
            actStr = "chi";
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(CxMjPlayer player, CxMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("moPai");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(leftMajiangs.size());
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(CxMjPlayer player, CxMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("chuPaiActList");
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logActionHu(CxMjPlayer player, List<CxMj> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("CxMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("huPai");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(mjs);
        sb.append("|").append(daHuNames);
        LogUtil.msg(sb.toString());
    }

    public String actListToString(List<Integer> actList) {
        if (actList == null || actList.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < actList.size(); i++) {
            if (actList.get(i) == 1) {
                if (sb.length() > 1) {
                    sb.append(",");
                }
                if (i == CxMjConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == CxMjConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == CxMjConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == CxMjConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == CxMjConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == CxMjConstants.ACTION_INDEX_ZIMO) {
                    sb.append("ziMo");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
	 * ?????????????????????
	 *
	 * @return
	 */
    public int getLeftMajiangCount() {
        return this.leftMajiangs.size();
    }

    public void addMoTailPai(int gangDice) {
        int leftMjCount = getLeftMajiangCount();
        int startIndex = 0;
        if (moTailPai.contains(0)) {
            int lastIndex = moTailPai.get(0);
            for (int i = 1; i < moTailPai.size(); i++) {
                if (moTailPai.get(i) == lastIndex + 1) {
                    lastIndex++;
                } else {
                    break;
                }
            }
            startIndex = lastIndex + 1;
        }
        if (gangDice == -1) {
			// ??????????????????
            for (int i = 0; i < leftMjCount; i++) {
                int nowIndex = i + startIndex;
                if (!moTailPai.contains(nowIndex)) {
                    moTailPai.add(nowIndex);
                    break;
                }
            }

        } else {
            int duo = gangDice / 10 + gangDice % 10;
			// ???????????????????????????
            for (int i = 0, j = 0; i < leftMjCount; i++) {
                int nowIndex = i + startIndex;
                if (nowIndex % 2 == 1) {
					j++; // ???????????????
                }
                if (moTailPai.contains(nowIndex)) {
                    if (nowIndex % 2 == 1) {
                        duo++;
                        leftMjCount = leftMjCount + 2;
                    }
                } else {
                    if (j == duo) {
                        moTailPai.add(nowIndex);
                        moTailPai.add(nowIndex - 1);
                        break;
                    }

                }
            }

        }
        Collections.sort(moTailPai);
        changeExtend();
    }

    /**
	 * ???????????????
	 */
    public void clearMoTailPai() {
        this.moTailPai.clear();
        changeExtend();
    }

    /**
	 * ????????????
	 *
	 * @return
	 */
    public int isLiuJu() {
        return (huConfirmList.size() == 0 && leftMajiangs.size() == 0) ? 1 : 0;
    }


    public void sendTingInfo(CxMjPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<CxMj> cards = new ArrayList<>(player.getHandMajiang());
            Map<Integer, List<Integer>> daTing = TingTool.getDaTing(CxMjHelper.toMajiangIds(cards));
            for(Map.Entry<Integer, List<Integer>> entry : daTing.entrySet()){
                DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
                ting.addAllTingMajiangIds(entry.getValue());
                ting.setMajiangId(entry.getKey());
                tingInfo.addInfo(ting);
            }
            if(daTing.size()>0)
                player.writeSocket(tingInfo.build());

        } else {
            List<CxMj> cards = new ArrayList<>(player.getHandMajiang());
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            List<Integer> tingP = TingTool.getTing(CxMjHelper.toMajiangIds(cards));
            ting.addAllMajiangIds(tingP);
            if(tingP.size()>0)
                player.writeSocket(ting.build());
        }
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "????????????");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (isAutoPlay > 0) {
            json.put("autoTime", isAutoPlay);
            if (autoPlayGlob == 1) {
				json.put("autoName", "??????");
            } else {
				json.put("autoName", "??????");
            }
        }
        return JSON.toJSONString(json);
    }

    @Override
    public String getGameName() {
		return "????????????";
    }

    public List<CxMj> getNowDisCardIds() {
        return nowDisCardIds;
    }
}
