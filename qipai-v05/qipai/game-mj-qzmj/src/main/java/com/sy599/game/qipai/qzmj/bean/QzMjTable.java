package com.sy599.game.qipai.qzmj.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
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
import com.sy599.game.msg.serverPacket.PlayCardResMsg.*;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.qzmj.constant.QzMj;
import com.sy599.game.qipai.qzmj.constant.QzMjConstants;
import com.sy599.game.qipai.qzmj.rule.MjRobotAI;
import com.sy599.game.qipai.qzmj.rule.QzMingTang;
import com.sy599.game.qipai.qzmj.tool.MjHelper;
import com.sy599.game.qipai.qzmj.tool.MjQipaiTool;
import com.sy599.game.qipai.qzmj.tool.MjTool;
import com.sy599.game.qipai.qzmj.tool.QzMjResTool;
import com.sy599.game.qipai.qzmj.tool.huTool.HuTool;
import com.sy599.game.qipai.qzmj.tool.huTool.TingTool;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


public class QzMjTable extends BaseTable {
    /**
	 * 当前打出的牌
	 */
    private List<QzMj> nowDisCardIds = new ArrayList<>();
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
	 * 玩家位置对应临时操作 当同时存在多个可做的操作时 1当优先级最高的玩家最先操作 则清理所有操作提示 并执行该操作
	 * 2当操作优先级低的玩家先选择操作，则玩家先将所做操作存入临时操作中 当玩家优先级最高的玩家操作后 在判断临时操作是否可执行并执行
	 */
    private Map<Integer, MjTempAction> tempActionMap = new ConcurrentHashMap<>();
    private int maxPlayerCount = 4;
    private List<QzMj> leftMajiangs = new ArrayList<>();
	/*** 玩家map */
    private Map<Long, QzMjPlayer> playerMap = new ConcurrentHashMap<Long, QzMjPlayer>();
	/*** 座位对应的玩家 */
    private Map<Integer, QzMjPlayer> seatMap = new ConcurrentHashMap<Integer, QzMjPlayer>();
	private List<Integer> huConfirmList = new ArrayList<>();// 胡牌数组
    /**
	 * 摸麻将的seat
	 */
    private int moMajiangSeat;
    /**
	 * 骰子点数
	 **/
    private int dealDice;
    /**
	 * 0点炮胡 1自摸胡
	 **/
    private int canDianPao;
	private int isAutoPlay = 0;// 是否开启自动托管
    private int readyTime = 0 ;
	// 是否加倍：0否，1是
    private int jiaBei=0;
	// 加倍分数：低于xx分进行加倍
    private int jiaBeiFen=0;
	// 加倍倍数：翻几倍
    private int jiaBeiShu=0;

	// 是否勾选首局庄家随机
    private int bankerRand=0;
	// 是否已发牌
    private int finishFapai=0;
	/** 托管1：单局，2：全局 */
    private int autoPlayGlob;
	private int autoTableCount;
    //低于below加分
    private int belowAdd=0;
    private int below=0;

    //最后操作的牌
    private int lastId=0;
    //放杠座位号
    private int fangGangSeat=0;
    //正王
    private int zhengWang=0;
    //純王
    private List<Integer> chunWang=new ArrayList<>();
    //互包
    private Map<Integer,List<Integer>> hubao = new HashMap<>();
    //出牌数
    private int disNum=0;
    //自摸翻倍  0：不翻倍，1翻倍
    private int zimoFanbei=0;
    //小局封顶
    private int xiaojuFengding =0;
    //大局封顶
    private int dajuFengding =0;
    //翻醒 1：上：2中,3下
    private int fanxing = 0;
    //杠牌人的座位
    private int gangSeat=0;
    //翻出来的牌的牌的值
    private int kingId=0;
    //是否抽排
//    private int choupai=0;
    //1：出牌，2：摸牌
    private int disOrMo=0;

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


		// 0局数，1玩法Id
		payType = StringUtil.getIntValue(params, 2, 1);// 支付方式
		maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 人数

//        ceiling = StringUtil.getIntValue(params, 3, 0);// 封顶
        zimoFanbei = StringUtil.getIntValue(params, 4, 0);// 自摸翻倍

        xiaojuFengding = StringUtil.getIntValue(params, 5, 0);// 小局封顶
        dajuFengding = StringUtil.getIntValue(params, 6, 0);// 抓鸟算分 0加分 1翻倍
//        onlyZimo = StringUtil.getIntValue(params, 8, 0);// 只能自摸
//        piaoFenType = StringUtil.getIntValue(params, 9, 0);// 飘分
        fanxing = StringUtil.getIntValue(params, 10, 0);// 翻醒
        isAutoPlay = StringUtil.getIntValue(params, 11, 0);// 托管
        if(isAutoPlay==1) {
            // 默认1分钟
            isAutoPlay=60;
        }
        autoPlayGlob = StringUtil.getIntValue(params, 12, 0);// 单局托管
        if(maxPlayerCount==2){
            jiaBei = StringUtil.getIntValue(params, 13, 0);
            jiaBeiFen = StringUtil.getIntValue(params, 14, 100);
            jiaBeiShu = StringUtil.getIntValue(params, 15, 1);
            int belowAdd = StringUtil.getIntValue(params, 16, 0);
            if(belowAdd<=100&&belowAdd>=0)
                this.belowAdd=belowAdd;
            int below = StringUtil.getIntValue(params, 17, 0);
            if(below<=100&&below>=0){
                this.below=below;
            }
        }
//        choupai = StringUtil.getIntValue(params, 19, 0);//0全中鸟，1:13579中鸟
        changeExtend();
        if (!isJoinPlayerAllotSeat()) {
			// getRoomModeMap().put("1", "1"); //可观战（默认）
        }
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
        for (QzMjPlayer player : seatMap.values()) {
            wrapper.putString(player.getSeat(), player.toExtendStr());
        }
        wrapper.putInt(4, disOrMo);
        wrapper.putString(5, StringUtil.implode(huConfirmList, ","));
        wrapper.putInt(6, moMajiangSeat);
        wrapper.putString(7,StringUtil.implode(chunWang, ","));
        wrapper.putInt(9, canDianPao);
        wrapper.putInt(10,zhengWang);
        JSONArray tempJsonArray = new JSONArray();
        for (int seat : tempActionMap.keySet()) {
            tempJsonArray.add(tempActionMap.get(seat).buildData());
        }
        wrapper.putString("tempActions", tempJsonArray.toString());
        wrapper.putInt(11, maxPlayerCount);
        wrapper.putInt(12, isAutoPlay);
        wrapper.putInt(13,disNum);
//        wrapper.putString(15, StringUtil.implode(moTailPai, ","));
        wrapper.putInt(17, jiaBei);
        wrapper.putInt(18, jiaBeiFen);
        wrapper.putInt(19, jiaBeiShu);
        wrapper.putInt(20, bankerRand);
//        wrapper.putString(21,StringUtil.implode(paoHuSeat, ","));
        wrapper.putInt(22, autoPlayGlob);
        wrapper.putInt(23, finishFapai);
        wrapper.putInt(24, belowAdd);
        wrapper.putInt(25, below);
        wrapper.putInt(27, lastId);
        wrapper.putInt(28, fangGangSeat);
//        wrapper.putInt(29, ceiling);
        wrapper.putInt(30, zimoFanbei);
        wrapper.putInt(31, xiaojuFengding);
        wrapper.putInt(32, dajuFengding);
//        wrapper.putInt(33, onlyZimo);
//        wrapper.putInt(34, piaoFenType);
        wrapper.putInt(35, fanxing);
        wrapper.putInt(36, kingId);
        wrapper.putInt(37, gangSeat);
//        wrapper.putString(38,StringUtil.implode(buIds, ","));
//        wrapper.putInt(39, askSeat);
//        wrapper.putInt(40, twoNiaoType);
//        wrapper.putInt(41, sendJzkgSeat);
//        wrapper.putInt(42, hdFirstSeat);
//        wrapper.putInt(43, choupai);
        return wrapper;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
        for (QzMjPlayer player : seatMap.values()) {
            player.initExtend(wrapper.getString(player.getSeat()));
        }
        disOrMo = wrapper.getInt(4,0);
        String huListstr = wrapper.getString(5);
        if (!StringUtils.isBlank(huListstr)) {
            huConfirmList = StringUtil.explodeToIntList(huListstr);
        }
        moMajiangSeat = wrapper.getInt(6, 0);
        String chunWang = wrapper.getString(7);
        if (!StringUtils.isBlank(chunWang)) {
            this.chunWang = StringUtil.explodeToIntList(chunWang);
        }
        canDianPao = wrapper.getInt(9, 1);
        zhengWang = wrapper.getInt(10,0);
        tempActionMap = loadTempActionMap(wrapper.getString("tempActions"));
        maxPlayerCount = wrapper.getInt(11, 4);
        isAutoPlay = wrapper.getInt(12, 0);
        disNum = wrapper.getInt(13, 0);

        if(isAutoPlay ==1) {
            isAutoPlay=60;
        }
        jiaBei = wrapper.getInt(17,0);
        jiaBeiFen = wrapper.getInt(18,0);
        jiaBeiShu = wrapper.getInt(19,0);
        bankerRand = wrapper.getInt(20,0);
        autoPlayGlob= wrapper.getInt(22,0);
        finishFapai= wrapper.getInt(23,0);
        belowAdd= wrapper.getInt(24,0);
        below= wrapper.getInt(25,0);
        lastId= wrapper.getInt(27,0);
        fangGangSeat= wrapper.getInt(28,0);
//        ceiling= wrapper.getInt(29,0);
        zimoFanbei= wrapper.getInt(30,0);
        xiaojuFengding = wrapper.getInt(31,0);
        dajuFengding = wrapper.getInt(32,0);
//        onlyZimo= wrapper.getInt(33,0);
//        piaoFenType= wrapper.getInt(34,0);
        fanxing = wrapper.getInt(35,0);
        kingId= wrapper.getInt(36,0);
        gangSeat= wrapper.getInt(37,0);
//        s = wrapper.getString(38);
//        if (!StringUtils.isBlank(s)) {
//            buIds = StringUtil.explodeToIntList(s);
//        }
//        askSeat= wrapper.getInt(39,0);
//        twoNiaoType= wrapper.getInt(40,0);
//        sendJzkgSeat= wrapper.getInt(41,0);
//        hdFirstSeat= wrapper.getInt(42,0);
//        choupai= wrapper.getInt(43,0);
    }



    public int getDealDice() {
        return dealDice;
    }

    public int getGangSeat() {
        return gangSeat;
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

    public int getCanDianPao() {
        return canDianPao;
    }

    public void setCanDianPao(int canDianPao) {
        this.canDianPao = canDianPao;
    }

    public int getDisNum() {
        return disNum;
    }

    public void setDisNum(int disNum) {
        this.disNum = disNum;
    }

    public List<Integer> getChunWang() {
        return chunWang;
    }


    public void setChunWang(List<Integer> chunWang) {
        this.chunWang = chunWang;
    }

    public int getZhengWang() {
        return zhengWang;
    }

    public void setZhengWang(int zhengWang) {
        this.zhengWang = zhengWang;
    }


    public void setLastId(int lastId) {
        this.lastId = lastId;
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
        boolean selfMo = false;
        int xingId = 0;
        int xingBei = 0;
        List<Integer> hubaoList = new ArrayList<>();
        if (winList.size() == 0) {

        } else {
            hubaoList = getHuBaoBySeat(winList.get(0));
            if(gangSeat==winList.get(0)||disNum==0){
                selfMo = true;
            }else if (seatMap.get(winList.get(0)).getHandMajiang().size() % 3 == 2 && winList.get(0) == moMajiangSeat) {
                selfMo = true;
            }
            QzMj xingCard = null;
            if(fanxing > 0){
                xingCard = leftMajiangs.get(0);
            }
            if(xingCard != null){
                xingId = xingCard.getId();
            }
            //自摸
            if (selfMo) {
				// 自摸
                QzMjPlayer winner = seatMap.get(winList.get(0));
                int winFen=0;
                int mtFen = QzMingTang.getMingTangFen(winner.getIdMingTang());
                mtFen += winner.getGangFen();
                if(zimoFanbei > 0){
                    mtFen *= 2;
                }
                //翻醒
                int xing = getXing(winner,xingCard);
                if(xing > 0){
                    mtFen = mtFen + mtFen * xing;
                }
                xingBei = xing;
                for (int seat : seatMap.keySet()) {
                    if (!winList.contains(seat)) {
                        int loseFen=mtFen;
                        QzMjPlayer player = seatMap.get(seat);
                        int extraFen = countTwoExtraPoint(winner, player);
                        if(hubaoList.contains(seat)){
                            loseFen = loseFen * getHuBaoRate();
                        }
                        if(maxPlayerCount == 2 && xiaojuFengding > 0 && loseFen > xiaojuFengding){
                            loseFen = xiaojuFengding;
                        }
                        player.changePoint(-loseFen-extraFen);
                        winFen=winFen+loseFen+extraFen;
                    }
                }
                winner.changePoint(winFen);
                winner.addZiMoNum(1);
            } else {
                QzMjPlayer losePlayer;
                if(gangSeat!=0){
                    losePlayer=seatMap.get(gangSeat);
                }else{
                    losePlayer= seatMap.get(disCardSeat);
                }
                losePlayer.addDianPaoNum(1);
                int loseFen=0;
                for (int winnerSeat : winList) {
                    QzMjPlayer winPlayer = seatMap.get(winnerSeat);
                    int winFen = QzMingTang.getMingTangFen( winPlayer.getIdMingTang());
                    winFen += winPlayer.getGangFen();
                    //翻醒
                    int xing = getXing(winPlayer,xingCard);;
                    if(xing > 0){
                        winFen = winFen+winFen * xing;
                    }
                    xingBei = xing;
                    int extraFen = countTwoExtraPoint(winPlayer, losePlayer);
                    if(hubaoList.contains(losePlayer.getSeat())){
                        winFen = winFen * getHuBaoRate();
                    }
                    if(maxPlayerCount == 2 && xiaojuFengding > 0 && winFen > xiaojuFengding){
                        winFen = xiaojuFengding;
                    }
                    winPlayer.changePoint(winFen+extraFen);
                    loseFen=loseFen-winFen-extraFen;
                    winPlayer.addJiePaoNum(1);
                }
                losePlayer.changePoint(loseFen);
            }
        }
        boolean over = playBureau == totalBureau;
        if(autoPlayGlob >0) {
			// //是否解散
            boolean diss = false;
            if(autoPlayGlob ==1) {
            	 for (QzMjPlayer seat : seatMap.values()) {
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

        ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList,xingId, xingBei,false,hubaoList);
        if (!winList.isEmpty()) {
            if (winList.size() > 1) {
				// 一炮多响设置放炮的人为庄家
                setLastWinSeat(disCardSeat);
            } else {
                setLastWinSeat(winList.get(0));
            }
		} else if (leftMajiangs.isEmpty()) {// 黄庄
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
        for (QzMjPlayer player : seatMap.values()) {
            if (player.isAutoPlaySelf()) {
                player.setAutoPlay(false, false);
            }
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }

    }

    private int getHuBaoRate(){
        if(maxPlayerCount == 2){
            return 2;
        }else if(maxPlayerCount == 3){
            return 3;
        }else if(maxPlayerCount == 4){
            return 4;
        }
        return 1;
    }

    private int getXing(QzMjPlayer winPlayer,QzMj xingCard){
        if(xingCard == null){
            return 0;
        }
        int xing = 0;

        if(fanxing > 0){
            List<Integer> xingVal = new ArrayList<>();

            if(calState(fanxing,0)){
                xingVal.add(QzMj.getLastMjVal(xingCard));
            }
            if(calState(fanxing,1)){
                xingVal.add(xingCard.getVal());
            }
            if(calState(fanxing,2)){
                xingVal.add(QzMj.getNextMjVal(xingCard));
            }
            if(xingVal.size() > 0){
                xing = winPlayer.getXingNum(xingVal);
            }
        }
        return xing;
    }

    private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (QzMjPlayer seat : seatMap.values()) {
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

    private int[] getBirdNum(List<Integer> birdIds, int[] birdSeat, int playerCount, int catchSeat){
        int[] bird=new int[4];
        if(playerCount==3){
            for (int i = 0; i < birdIds.size(); i++) {
                int yu=((QzMj.getMajang(birdIds.get(i)).getVal()%10)-1)%4;
                bird[yu]++;
                if(yu<3){
                    int bingoSeat = calcSeat(catchSeat, yu, playerCount);
                    birdSeat[i]=bingoSeat;
                }else {
                    birdSeat[i]=0;
                }
            }
        }else {
            for (int i = 0; i < birdIds.size(); i++) {
                int yu=((QzMj.getMajang(birdIds.get(i)).getVal()%10)-1)%playerCount;
                bird[yu]++;
                int bingoSeat = calcSeat(catchSeat, yu, playerCount);
                birdSeat[i]=bingoSeat;
            }
        }

        return bird;
    }

    private int countTwoExtraPoint(QzMjPlayer win, QzMjPlayer lose){
        int extra=0;
        return extra;
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
        userLog.setRes(extendLogDeal(logRes));
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
        for (QzMjPlayer player : playerMap.values()) {
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
                tempMap.put("nowDisCardIds", StringUtil.implode(MjHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(MjHelper.toMajiangIds(leftMajiangs), ","));
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
        dealDice = (r.nextInt(6) + 1);
        addPlayLog(disCardRound + "_" + lastWinSeat + "_" + MjDisAction.action_dice + "_" + dealDice);
        setDealDice(dealDice);
        QzMj king = confirmKing(dealDice);
        // 天胡或者暗杠
        logFaPaiTable(king);
        for (QzMjPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            List<Integer> actionList=Arrays.asList(0,0,0,0,0,0);
            if (lastWinSeat == tablePlayer.getSeat()) {
                actionList = tablePlayer.checkMo();
            }else{
//                actionList=tablePlayer.checkBaoTing(actionList,getChunWang());
            }
            System.out.println("发牌actionList"+actionList.toString());
            if (actionList.contains(1)) {
                addActionSeat(tablePlayer.getSeat(), actionList);
                res.addAllSelfAct(actionList);
                logFaPaiPlayer(tablePlayer, actionList);
            }
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            res.setDealDice(dealDice);
            res.setLaiZiVal(king.getId());
            tablePlayer.writeSocket(res.build());
            if (tablePlayer.isAutoPlay()) {
                tablePlayer.setAutoPlayTime(0);
            }
            sendTingInfo(tablePlayer);
            logFaPaiPlayer(tablePlayer, null);
            if(tablePlayer.isAutoPlay()) {
            	addPlayLog(getDisCardRound() + "_" +tablePlayer.getSeat() + "_" + QzMjConstants.action_tuoguan + "_" +1+ tablePlayer.getExtraPlayLog());
            }
        }
        sendFirstAct(seatMap.get(lastWinSeat));
        if (playBureau == 1) {
            setCreateTime(new Date());
        }
    }

    private QzMj confirmKing(int dealDice){
        QzMj king ;
        int ver=leftMajiangs.size() - dealDice * 2 - 1;
        if(zp!=null&&zp.get(0).size()==1){
            //做王，用于测试
            king = QzMj.getMajiangByVal(zp.get(0).get(0));
            //交换筛子对应位置和王的位置
            leftMajiangs.remove(king);
        }else {
            king = leftMajiangs.get(ver);
        }
//        king = QzMj.getMajiangByVal(28);
        zhengWang=king.getVal();
        int yu=zhengWang%10;
        int chu=zhengWang/10;
        if(zhengWang > 200 && zhengWang < 300){
            if(chu == 22){
                chunWang.add(201);
            }else{
                chunWang.add(chu*10+11);
            }
        }else if(zhengWang > 300){
            if(chu == 33){
                chunWang.add(301);
            }else{
                chunWang.add(chu*10+11);
            }
        }else if(yu==9){
            chunWang.add(chu*10+1);
        }else {
            chunWang.add(zhengWang+1);
        }
        chunWang.add(QzMj.getNextMjVal(king));
        chunWang.add(1000);//万能牌
        this.kingId=king.getId();
        zhengWang = 0;
        return king;
    }

    public void moMajiang(QzMjPlayer player) {
        if (state != table_state.play) {
            return;
        }
		// 摸牌
        if(player.isZhuayu()){
            List<Integer> acts = getActs(1);
            QzMj majiang = QzMj.getMajang(1000);
//            sendMoRes(player,majiang,acts);
            if(majiang != null){
                player.moMajiang(majiang);
            }else{
                majiang = getLeftMajiang();
                player.moMajiang(majiang);
            }
            lastId=majiang.getId();
            setMoMajiangSeat(player.getSeat());
            addActionSeat(player.getSeat(),acts);
            hu(player,new ArrayList<>(),MjDisAction.action_hu);
            return;
        }

        QzMj majiang = null;
        if (disCardRound != 0) {
			// 玩家手上的牌是双数，已经摸过牌了
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            int gangNum = 0;
            for (QzMjPlayer qplayer : seatMap.values()) {
                gangNum += qplayer.getGangNum();
            }
            if (getLeftMajiangCount()==0) {
                calcOver();
                return;
            }else if(maxPlayerCount == 2 && getLeftMajiangCount() <= 62+gangNum*2){
                calcOver();
                return;
            }else if(maxPlayerCount == 3 && getLeftMajiangCount() <= 48+gangNum*2){
                calcOver();
                return;
            }else if(maxPlayerCount == 4 && getLeftMajiangCount() <= 34+gangNum*2){
                calcOver();
                return;
            }

            if (GameServerConfig.isDebug() && zp != null && zp.size()==1) {
                majiang= QzMj.getMajiangByValue(zp.get(0).get(0));
                zp.clear();
            }else {
                majiang = getLeftMajiang();
            }
        }
        if (majiang != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + MjDisAction.action_moMjiang + "_" + majiang.getId() + player.getExtraPlayLog());
            player.moMajiang(majiang);
        }
		// 检查摸牌
        clearActionSeatMap();
        if (disCardRound == 0) {
            return;
        }
        setMoMajiangSeat(player.getSeat());
        player.setPassHu(0);

        lastId=majiang.getId();
        if(player.getMoDa()==1){
            boolean isHu= HuTool.isHu(player.getHandPais(),getChunWang(),player.getCardTypes(),false,true);
            if(isHu){
                List<Integer> acts = getActs(1);
                sendMoRes(player,majiang,acts);
                addActionSeat(player.getSeat(),acts);
                hu(player,new ArrayList<>(),MjDisAction.action_hu);
            }else {
                List<Integer> list = new ArrayList<>();
                int[] arr = player.checkGang();
                for (int val : arr) {
                    list.add(val);
                }
                if (!list.isEmpty() && list.contains(1)) {
                    addActionSeat(player.getSeat(), list);
                    logMoMj(player, majiang, list);
                    sendMoRes(player,majiang,list);
                }else{
                    List<QzMj> dis=new ArrayList<>();
                    dis.add(majiang);
                    sendMoRes(player,majiang,getActs());
                    chuPai(player,dis,MjDisAction.action_chupai);
                }
            }
        }else {
            List<Integer> arr = player.checkMo();
            if (!arr.isEmpty()) {
                addActionSeat(player.getSeat(), arr);
            }
            logMoMj(player, majiang, arr);
            sendMoRes(player,majiang,arr);
            sendTingInfo(player);
        }

    }

    public void sendMoRes(QzMjPlayer player, QzMj mj, List<Integer> acts){
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setRemain(getLeftMajiangCount());
        res.setNextDisCardIndex(nowDisCardSeat);
        for (QzMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                MoMajiangRes.Builder copy = res.clone();
                copy.addAllSelfAct(acts);
                if (mj != null) {
                    copy.setMajiangId(mj.getId());
                }
                seat.writeSocket(copy.build());
            } else {
                seat.writeSocket(res.build());
            }
        }
    }


    /**
     *
     * @param act 对应下标参照{@link MjDisAction}中的静态变量
     * @return
     */
    public List<Integer> getActs(int...act){
        List<Integer> acts=new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if(i>act.length-1){
                acts.add(0);
            }else {
                acts.add(act[i]);
            }
        }
        return acts;
    }




    /**
	 * 玩家表示胡
	 *
	 * @param player
	 * @param majiangs
	 */
    private void hu(QzMjPlayer player, List<QzMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        List<Integer> actionList = actionSeatMap.get(player.getSeat());
		if (actionList == null || actionList.get(QzMjConstants.ACTION_INDEX_HU) != 1) {// 如果集合为空或者第一操作不为胡，则返回
            return;
        }
        if (huConfirmList.contains(player.getSeat())) {
            return;
        }
        if (!checkAction(player, majiangs, new ArrayList<>(), action)) {
            LogUtil.msg("有优先级更高的操作需等待！");
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            return;
        }
        if(!canHu(player))
            return;
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<QzMj> huHand = new ArrayList<>(player.getHandMajiang());
        boolean zimo = player.isAlreadyMoMajiang();
        QzMj huCard=null;
        if(lastId!=0){
            huCard= QzMj.getMajang(lastId);
            if(huCard!=null&&!player.getHandMajiang().contains(huCard))
                player.getHandMajiang().add(huCard);
        }
        List<Integer> huType = HuTool.getHuType(player.getHandPais(), getChunWang(), player, this,zimo);
        if (huType.size()==0) {
            return;
        }
        if(lastId!=0)
            player.addMingTang(lastId,huType);
        else//用于前端显示
            player.addMingTang(player.getHandPais().get(player.getHandMajiang().size()-1),huType);
        QzMingTang.addOddHuType(this,player,zimo);
        QzMingTang.removeLess(player.getIdMingTang(),this);

        if (!zimo) {
            builder.setFromSeat(disCardSeat);
            if(huCard!=null){
                huHand.add(huCard);
            }
        } else {
            builder.addHuArray(QzMjConstants.HU_ZIMO);
        }
        buildPlayRes(builder, player, action, huHand);
        if (zimo) {
            builder.setZimo(1);
        }
        if (!huConfirmList.isEmpty()) {
            builder.addExt(StringUtil.implode(huConfirmList, ","));
        }
		// 胡
        for (QzMjPlayer seat : seatMap.values()) {
			// 推送消息
            seat.writeSocket(builder.build());
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
		// 加入胡牌数组
        addHuList(player.getSeat());
        changeDisCardRound(1);
        List<QzMj> huPai = new ArrayList<>();
        huPai.add(huHand.get(huHand.size() - 1));
//        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(huPai) + "_" + StringUtil.implode(player.getHuType(), ",") + player.getExtraPlayLog());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(huPai) + "_"+ player.getExtraPlayLog());
        logActionHu(player, majiangs, "");
        calcOver();

    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<QzMj> majiangs) {
        QzMjResTool.buildPlayRes(builder, player, action, majiangs);
    }


    private boolean isCalcOver() {
        List<Integer> huActionList = getHuSeatByActionMap();
        boolean over = false;
        if (!huActionList.isEmpty()) {
            over = true;
            for (int huseat : huActionList) {
                if (!huConfirmList.contains(huseat) &&
                        !(tempActionMap.containsKey(huseat) && tempActionMap.get(huseat).getAction() == MjDisAction.action_hu)) {
                    over = false;
                    break;
                }
            }
        }

        if (!over) {
            QzMjPlayer disMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmList.contains(huseat)) {
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                QzMjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disMajiangPlayer, 0, null);
                List<Integer> actionList = actionSeatMap.get(huseat);
                disBuilder.addAllSelfAct(actionList);
                seatPlayer.writeSocket(disBuilder.build());
            }
        }

        return over;
    }


    public boolean canHu(QzMjPlayer player){
        List<Integer> huActionList = getHuSeatByActionMap();
        if (!huActionList.isEmpty()) {
            int seat=nowDisCardSeat;
            do {
                if(actionSeatMap.containsKey(seat)&&actionSeatMap.get(seat).get(0)==1){
                    if(seat==player.getSeat())
                        return true;
                    else
                        return false;
                }else {
                    seat=calcNextSeat(seat);
                }
            } while(nowDisCardSeat!=seat);
        }
        return false;
    }

    /**
	 * 碰杠
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    private void chiPengGang(QzMjPlayer player, List<QzMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        logAction(player, action, majiangs, null);
        if (majiangs == null || majiangs.isEmpty()) {
            return;
        }
        for (QzMj mj:majiangs) {
            if(getChunWang().contains(mj.getVal()))
                return;
        }
        if (!checkAction(player, majiangs, new ArrayList<>(), action)) {
			LogUtil.msg("有优先级更高的操作需等待！");
            player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        QzMj disMajiang = null;
        if (nowDisCardIds.size() > 1) {
			// 当前出牌不能操作
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
            sameCount = MjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
        }
		// 如果是杠 后台来找出是明杠还是暗杠
        if (action == MjDisAction.action_minggang) {
            majiangs = MjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
            sameCount = majiangs.size();
            if (sameCount >= 4) {
				// 有4张一样的牌是暗杠
                action = MjDisAction.action_angang;
                majiangs=majiangs.subList(0,4);
            }
			// 其他是明杠
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        boolean hasQGangHu = false;
        if (action == MjDisAction.action_peng) {
            boolean can = canPeng(player, majiangs, sameCount);
            if (!can) {
                return;
            }
        }else if (action == MjDisAction.action_chi) {
            boolean can = canChi(player, majiangs, disMajiang);
            if (!can) {
                return;
            }
        }else if (action == MjDisAction.action_angang) {
            boolean can = canAnGang(player, majiangs, sameCount);

            if (!can) {
                return;
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        } else if (action == MjDisAction.action_minggang) {
            boolean can = canMingGang(player, majiangs, sameCount);
            if (!can) {
                return;
            }
            ArrayList<QzMj> mjs = new ArrayList<>(majiangs);
            if (sameCount == 3) {
                mjs.add(disMajiang);
            }
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(mjs) + player.getExtraPlayLog());

            // 特殊处理一张牌明杠的时候别人可以胡
            if (sameCount == 1 && canGangHu()) {
                lastId = majiangs.get(0).getId();
                recordDisMajiang(majiangs, player);
                if (checkQGangHu(player, majiangs, action, sameCount)) {
                    hasQGangHu = true;
                    setNowDisCardSeat(player.getSeat());
                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
                }
            }
            //点杠可枪
//            if (sameCount == 3) {
//                if (checkQGangHu(player, mjs, action, sameCount)) {
//                    hasQGangHu = true;
//                    setNowDisCardSeat(player.getSeat());
//                    LogUtil.msg("tid:" + getId() + " " + player.getName() + "可以被抢杠胡！！");
//                }
//            }
        } else {
            return;
        }
        if (hasQGangHu && action == MjDisAction.action_minggang || action == MjDisAction.action_angang) {
            player.setGangStatus(3);
        }
        if (disMajiang != null) {
            if ((action == MjDisAction.action_minggang && sameCount == 3) || action == MjDisAction.action_peng || action == MjDisAction.action_chi) {
                if (action == MjDisAction.action_chi) {
					majiangs.add(0, disMajiang);// 吃的牌放第1位
                    player.setCurChiVal(disMajiang.getVal());
                } else {
                    majiangs.add(disMajiang);
                }
                builder.setFromSeat(disCardSeat);
                seatMap.get(disCardSeat).removeOutPais(nowDisCardIds);
            }
        }
        chiPengGang(builder, player, majiangs, action, hasQGangHu, sameCount);
//        switch (action){
//            case MjDisAction.action_minggang:
//            case MjDisAction.action_angang:
//                gang(builder,player,majiangs,action,sameCount);
//                break;
//            default:
//                if(action == MjDisAction.action_chi){
//                    player.setCurChiVal(disMajiang.getVal());
//                }
//                chiPeng(builder, player, majiangs, action);
//                break;
//        }
    }
    private void chiPengGang(PlayMajiangRes.Builder builder, QzMjPlayer player, List<QzMj> majiangs, int action, boolean hasQGangHu, int sameCount) {

        List<Integer> actionList = actionSeatMap.get(player.getSeat());
        if (action == MjDisAction.action_peng && actionList.get(QzMjConstants.ACTION_INDEX_MINGGANG) == 1) {
            // 可以碰也可以杠
            player.addPassGangVal(majiangs.get(0).getVal());
        }
        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = removeActionSeat(player.getSeat());
        if (!hasQGangHu) {
            clearActionSeatMap();
        }
        if (action == MjDisAction.action_chi || action == MjDisAction.action_peng) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        }
        // 不是普通出牌
        setNowDisCardSeat(player.getSeat());
        for (QzMjPlayer seatPlayer : seatMap.values()) {
            // 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        if (action == MjDisAction.action_chi || action == MjDisAction.action_peng) {
            sendTingInfo(player);
            List<Integer> newAcList = Arrays.asList(0,0,0,0,0,0);
//            arr[QzMjConstants.ACTION_INDEX_BAOTING] = 1;
//            newAcList.set(QzMjConstants.ACTION_INDEX_BAOTING,1);
            newAcList=player.checkBaoTing(newAcList,getChunWang());
            if (newAcList.contains(1)) {
                addActionSeat(player.getSeat(), newAcList);
            }
        }
        for (Player roomPlayer : roomPlayerMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            roomPlayer.writeSocket(copy.build());
        }
        if (!hasQGangHu && action == MjDisAction.action_minggang || action == MjDisAction.action_angang) {
            // 明杠和暗杠摸牌
            player.setGangStatus(1);
            moMajiang(player);
        }
        robotDealAction();
        logAction(player, action, majiangs, actList);
    }
    private void chiPeng(PlayMajiangRes.Builder builder, QzMjPlayer player, List<QzMj> majiangs, int action) {
        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        List<Integer> actList = actionSeatMap.get(player.getSeat());
        clearActionSeatMap();
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        setNowDisCardSeat(player.getSeat());

//        int[] arr = player.checkGang();
//        List<Integer> list = new ArrayList<>();
//        for (int val : arr) {
//            list.add(val);
//        }
//        if(list.contains(1))
//            addActionSeat(player.getSeat(),list);
        for (QzMjPlayer seatPlayer : seatMap.values()) {
			// 推送消息
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        sendTingInfo(player);
        logAction(player, action, majiangs, actList);

    }

    /**
     * 抢杠胡
     *
     * @param player
     * @param majiangs
     * @param action
     * @return
     */
    private boolean checkQGangHu(QzMjPlayer player, List<QzMj> majiangs, int action, int sameCount) {
        for (QzMjPlayer seatPlayer : seatMap.values()) {
            if (seatPlayer.getUserId() == player.getUserId()) {
                continue;
            }
            List<Integer> copyIds = new ArrayList<>(seatPlayer.getHandPais());
            copyIds.add(majiangs.get(0).getId());
            if(HuTool.isHu(copyIds,getChunWang(),seatPlayer.getCardTypes(),false,false)){
                List<Integer> acts=new ArrayList<>();
                acts.add(1);
                for (int i = 1; i < 6; i++) {
                    acts.add(0);
                }
                addActionSeat(seatPlayer.getSeat(),acts);
                return true;
            }
        }
        return false;
    }


    /**
	 * 普通出牌
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    private void chuPai(QzMjPlayer player, List<QzMj> majiangs, int action) {
        if (state != table_state.play) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (majiangs.size() != 1) {
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            return;
        }
        if (!tempActionMap.isEmpty()) {
			LogUtil.e(player.getName() + "出牌清理临时操作！");
            clearTempAction();
        }
        if (!player.isAlreadyMoMajiang()) {
			// 还没有摸牌
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            player.writeErrMsg("等待其他玩家操作");
            return;
        }
        if(player.getCurChiVal() > 0 && player.getCurChiVal() == majiangs.get(0).getVal()){
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            player.writeErrMsg("不能吃同一张，再打同一张");
            return;
        }
        if(player.getBaoTing() == 1 && player.getChuNum() == 0){
            List<QzMj> cards = new ArrayList<>(player.getHandMajiang());
            cards.remove(majiangs.get(0));
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            List<Integer> tingP = TingTool.getTing(MjHelper.toMajiangIds(cards),getChunWang(),player.getCardTypes(),false);
            if(tingP == null || tingP.size() <= 0){
                player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
                player.writeErrMsg("你已选择报听，打这张不听牌了！不允许出");
                return;
            }
        }

        if (chunWang.contains(majiangs.get(0).getVal())) {
            // 不能出出癞子牌
            player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
            player.writeErrMsg("王八不能打");
            return;
        }
        if(!actionSeatMap.isEmpty()){
            if(actionSeatMap.containsKey(player.getSeat()))
                removeActionSeat(player.getSeat());
            else {
                player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
                return;
            }
        }
        if(player.getGangStatus() == 1){
            player.setGangStatus(2);
        }else{
            player.setGangStatus(0);
        }
        player.setChuNum(player.getChuNum()+1);
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
		// 普通出牌
        clearActionSeatMap();
        player.cleanCurChiVal();
        player.cleanGuoPeng();
        player.setPassHu(0);

        setNowDisCardSeat(calcNextSeat(player.getSeat()));
        if(majiangs.size()==1)
            lastId=majiangs.get(0).getId();
        recordDisMajiang(majiangs, player);
        player.addOutPais(majiangs, action, player.getSeat());
        disNum++;
        logAction(player, action, majiangs, null);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        for (QzMjPlayer p : seatMap.values()) {
            List<Integer> list;
            if (p.getUserId() != player.getUserId()) {
                list = p.checkDisMajiang(majiangs.get(0),
                        p.getSeat()==calcNextSeat(player.getSeat()));
                if(list==null||list.isEmpty())
                    continue;
                if (list.contains(1)) {
                    addActionSeat(p.getSeat(), list);
                    p.setLastCheckTime(System.currentTimeMillis());
                    logChuPaiActList(p, majiangs.get(0), list);
                    if(list.get(QzMjConstants.ACTION_INDEX_MINGGANG)==1)
                        fangGangSeat=player.getSeat();
                }
            }
        }
        //出完牌可胡所有，则是摸鱼状态(1张王牌调所有)
        if(getChunWang().size() > 0){
            List<QzMj> copy = new ArrayList<>(player.getHandMajiang());
            int bossNum = 0;
            for (QzMj mj : copy){
                if(getChunWang().contains(mj.getVal())){
                    bossNum ++;
                }
            }
            if(bossNum > 0 && TingTool.getTing(MjHelper.toMajiangIds(copy),getChunWang(),player.getCardTypes(),false).size() >= 34){
                player.setZhuayu(true);
            }
        }
        sendDisMajiangAction(builder);
        checkMo();
    }

    private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
        for (QzMjPlayer seatPlayer : seatMap.values()) {
            PlayMajiangRes.Builder copy = builder.clone();
            List<Integer> actionList;
            // 只推送给胡牌的人改成了推送给所有人但是必须等胡牌的人先答复
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                actionList = actionSeatMap.get(seatPlayer.getSeat());
            } else {
                actionList = new ArrayList<>();
            }
            copy.addAllSelfAct(actionList);
            seatPlayer.writeSocket(copy.build());
        }
    }

    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(QzMjConstants.ACTION_INDEX_HU) == 1) {
				// 胡
                huList.add(seat);
            }
        }
        return huList;
    }



    public synchronized void playCommand(QzMjPlayer player, List<QzMj> majiangs, int action) {
        playCommand(player, majiangs, null, action);
    }

    /**
	 * 出牌
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
    public synchronized void playCommand(QzMjPlayer player, List<QzMj> majiangs, List<Integer> hucards, int action) {
        if (state != table_state.play) {
            return;
        }
        if (MjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }
		// 手上没有要出的麻将
        if (action != MjDisAction.action_minggang)
            if (!isHandCard(majiangs,player.getHandMajiang())) {
                return;
            }
        changeDisCardRound(1);
        if (action == MjDisAction.action_pass) {
            guo(player, majiangs, action);
        }else if(action==MjDisAction.action_baoting){
            baoTing(player,action);
        }else if(action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
        }
		// 记录最后一次动作的时间
        setLastActionTime(TimeUtil.currentTimeMillis());
    }

    public void baoTing(QzMjPlayer player, int action){
//        if(disNum!=0||player.getSeat()==lastWinSeat)
        if(player.getChuNum()>0)
            return;
        removeActionSeat(player.getSeat());
        player.setBaoTing(1);
        logBaoTing(player);
        addPlayLog(player.getSeat(),action);
        for (QzMjPlayer p:seatMap.values()) {
            p.writeComMessage(WebSocketMsgType.res_code_nxkwmj_baoTing,player.getSeat());
        }
        sendTingInfo(player);
        sendFinshActs();
    }

    private void sendFinshActs(){
        if(disNum==0) {
            //如果闲家没有可以起手的操作，则通知庄家操作
            boolean bankerFirst = true;
            for (Integer seat : actionSeatMap.keySet()) {
                if (seat != lastWinSeat) {
                    List<Integer> list = actionSeatMap.get(seat);
                    if (list.get(QzMjConstants.ACTION_INDEX_HU) == 1 || list.get(QzMjConstants.ACTION_INDEX_BAOTING) == 1)
                        bankerFirst = false;
                }
            }
            if (bankerFirst) {
                seatMap.get(lastWinSeat).writeComMessage(WebSocketMsgType.res_code_nxkwmj_finishAct);
            } else {
                seatMap.get(lastWinSeat).writeComMessage(WebSocketMsgType.res_code_nxkwmj_haveAct);
            }
        }
    }

    private boolean isHandCard(List<QzMj> majiangs, List<QzMj> handCards){
        for (QzMj mj:majiangs) {
            if(!handCards.contains(mj))
                return false;
        }
        return true;
    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(QzMjPlayer player, List<QzMj> majiangs, int action) {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if(actionSeatMap.get(player.getSeat()).get(QzMjConstants.ACTION_INDEX_HU)==1){
            player.setPassHu(1);
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MjHelper.toMajiangStrs(majiangs) + player.getExtraPlayLog());
        if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// 漏炮
            player.addPassMajiangVal(nowDisCardIds.get(0).getVal());
        }
        if(action == MjDisAction.action_peng){//过碰
            player.addGouPeng(nowDisCardIds.get(0).getVal());
        }
        logAction(player, action, majiangs, removeActionList);
        if (player.isAlreadyMoMajiang()) {
            sendTingInfo(player);
        }
		refreshTempAction(player);// 先过 后执行临时可做操作里面优先级最高的玩家操作
        if(actionSeatMap.isEmpty()){
            gangSeat=0;
            checkMo();
        }
        sendFinshActs();
    }

    private void recordDisMajiang(List<QzMj> majiangs, QzMjPlayer player) {
        setNowDisCardIds(majiangs);
        disOrMo=1;
        setDisCardSeat(player.getSeat());
    }


    public void setNowDisCardIds(List<QzMj> nowDisCardIds) {
        if (nowDisCardIds == null) {
            this.nowDisCardIds.clear();

        } else {
            this.nowDisCardIds = nowDisCardIds;

        }
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    /**
	 * 检查摸牌
	 */
    public void checkMo() {
        if (actionSeatMap.isEmpty()) {
            if (nowDisCardSeat != 0) {
                QzMjPlayer player = seatMap.get(nowDisCardSeat);
                moMajiang(player);
            }
            robotDealAction();

        } else {
            for (int seat : actionSeatMap.keySet()) {
                QzMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
					// 如果是机器人可以直接决定
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<QzMj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = MjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    if (actionList.get(QzMjConstants.ACTION_INDEX_HU) == 1) {
						// 胡
                        playCommand(player, new ArrayList<QzMj>(), MjDisAction.action_hu);

                    } else if (actionList.get(QzMjConstants.ACTION_INDEX_ANGANG) == 1) {
                        playCommand(player, list, MjDisAction.action_angang);

                    } else if (actionList.get(QzMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        playCommand(player, list, MjDisAction.action_minggang);

                    } else if (actionList.get(QzMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(player, list, MjDisAction.action_peng);
                    }
                }

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
            QzMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {
                    List<QzMj> list = null;
                    if (actionList.get(QzMjConstants.ACTION_INDEX_HU) == 1) {
						// 胡
                        playCommand(next, new ArrayList<QzMj>(), MjDisAction.action_hu);
                    } else if (actionList.get(QzMjConstants.ACTION_INDEX_ANGANG) == 1) {
						// 机器人暗杠
                        Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {
								// 可以暗杠
                                list = MjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, MjDisAction.action_angang);

                    } else if (actionList.get(QzMjConstants.ACTION_INDEX_MINGGANG) == 1) {
                        Map<Integer, Integer> pengMap = MjHelper.toMajiangValMap(next.getPeng());
                        for (QzMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
								// 有碰过
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, MjDisAction.action_minggang);
                                break;
                            }
                        }

                    } else if (actionList.get(QzMjConstants.ACTION_INDEX_PENG) == 1) {
                        playCommand(next, list, MjDisAction.action_peng);
                    }
                } else {
                    List<Integer> handMajiangs = new ArrayList<>(next.getHandPais());
                    MjQipaiTool.dropHongzhongVal(handMajiangs);
                    int maJiangId = MjRobotAI.getInstance().outPaiHandle(0, handMajiangs, new ArrayList<Integer>());
                    List<QzMj> majiangList = MjHelper.toMajiang(Arrays.asList(maJiangId));
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
        List<Integer> copy = QzMjConstants.getMajiangList(3);
        addPlayLog(copy.size() + "");
        List<List<QzMj>> list = null;
        if (zp != null&&zp.size()!=0) {
            list = MjTool.fapai(copy, getMaxPlayerCount(), zp);
        } else {
            list = MjTool.fapai(copy, getMaxPlayerCount());
        }
        int i = 1;
        for (QzMjPlayer player : seatMap.values()) {
            player.changeState(player_state.play);
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                continue;
            }
            player.dealHandPais(list.get(i));
            i++;
        }
		// 桌上剩余的牌
        List<QzMj> lefts = list.get(getMaxPlayerCount());
        setLeftMajiangs(lefts);
        finishFapai=1;
    }

    @Override
    public void startNext() {
    }

    /**
	 * 初始化桌子上剩余牌
	 *
	 * @param leftMajiangs
	 */
    public void setLeftMajiangs(List<QzMj> leftMajiangs) {
        if (leftMajiangs == null) {
            this.leftMajiangs.clear();
        } else {
            this.leftMajiangs = leftMajiangs;

        }
        dbParamMap.put("leftPais", JSON_TAG);
    }

    /**
	 * 剩余牌的第一张
	 *
	 * @return
	 */
    public QzMj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            QzMj majiang = this.leftMajiangs.remove(0);
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
//            if(actionSeatMap.isEmpty()||actionSeatMap.containsKey(lastWinSeat))
//                return lastWinSeat;
//            int seat = 0;
//            do {
//                seat = calcNextSeat(lastWinSeat);
//                if(actionSeatMap.containsKey(seat))
//                    return seat;
//            } while (seat==lastWinSeat);
            return lastWinSeat;
        } else {
            return nowDisCardSeat;
        }
    }

    public static Boolean calState(Integer state, Integer index){
        if(!state.equals(0)){
            return (state >> index & 1) == 1;
        }
        return true;
    }

    public int calcSeat(int seat,int time,int maxCount) {
        int resultSeat=seat;
        for (int i = 0; i < time; i++) {
            resultSeat=resultSeat + 1 > maxCount? 1 : resultSeat + 1;
        }
        return resultSeat;
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
        res.addExt(canDianPao);             //1
        res.addExt(isAutoPlay);             //2
        res.addExt(0);                      //3
        res.addExt(kingId);                //4

        res.addStrExt("");     //0
        res.setMasterId(getMasterId() + "");
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        res.setDealDice(dealDice);
        List<PlayerInTableRes> players = new ArrayList<>();
        for (QzMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                playerRes.addAllOutCardIds(MjHelper.toMajiangIds(nowDisCardIds));
            }
            playerRes.addRecover(player.getIsEntryTable());
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (actionSeatMap.containsKey(player.getSeat())) {
				if (!tempActionMap.containsKey(player.getSeat()) && !huConfirmList.contains(player.getSeat())) {// 如果已做临时操作
                    playerRes.addAllRecover(actionSeatMap.get(player.getSeat()));
                }
            }
            players.add(playerRes.build());
        }
        res.addAllPlayers(players);
        int nextSeat = getNextDisCardSeat();
        if (nextSeat != 0) {
            res.setNextSeat(nextSeat);
        }
        res.setRenshu(getMaxPlayerCount());
        res.setLastWinSeat(getLastWinSeat());
        res.addTimeOut((int) QzMjConstants.AUTO_TIMEOUT);
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
        setDealDice(0);
        readyTime=0;
        finishFapai=0;
        lastId=0;
        fangGangSeat=0;
        disNum=0;
        kingId=0;
        gangSeat=0;
        chunWang.clear();
        zhengWang=0;
        disOrMo=0;
    }

    public List<Integer> removeActionSeat(int seat) {
        List<Integer> actionList = actionSeatMap.remove(seat);
        saveActionSeatMap();
        return actionList;
    }

    public void addActionSeat(int seat, List<Integer> actionlist) {
        actionSeatMap.put(seat, actionlist);
        QzMjPlayer player = seatMap.get(seat);
        addPlayLog(disCardRound + "_" + seat + "_" + MjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist) + player.getExtraPlayLog());
        saveActionSeatMap();
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
            nowDisCardIds = MjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            try {
                leftMajiangs = MjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }




    private Map<Integer, MjTempAction> loadTempActionMap(String json) {
        Map<Integer, MjTempAction> map = new ConcurrentHashMap<>();
        if (json == null || json.isEmpty())
            return map;
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (Object val : jsonArray) {
            String str = val.toString();
            MjTempAction tempAction = new MjTempAction();
            tempAction.initData(str);
            map.put(tempAction.getSeat(), tempAction);
        }
        return map;
    }

    /**
	 * 检查优先度 如果同时出现一个事件，按出牌座位顺序优先
	 */
    private boolean checkAction(QzMjPlayer player, List<QzMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// 是否优先级最高 能执行操作
		if (!canAction) {// 不能操作时 存入临时操作
            int seat = player.getSeat();
            tempActionMap.put(seat, new MjTempAction(seat, action, cardList, hucards));
			// 玩家都已选择自己的临时操作后 选取优先级最高
            if (tempActionMap.size() == actionSeatMap.size()) {
                int maxAction = Integer.MAX_VALUE;
                int maxSeat = 0;
                Map<Integer, Integer> prioritySeats = new HashMap<>();
                int maxActionSize = 0;
                for (MjTempAction temp : tempActionMap.values()) {
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
                QzMjPlayer tempPlayer = seatMap.get(maxSeat);
                List<QzMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
                List<Integer> tempHuCards = tempActionMap.get(maxSeat).getHucards();
                for (int removeSeat : prioritySeats.keySet()) {
                    if (removeSeat != maxSeat) {
                        removeActionSeat(removeSeat);
                    }
                }
                clearTempAction();
				playCommand(tempPlayer, tempCardList, tempHuCards, maxAction);// 系统选取优先级最高操作
            } else {
                if (isCalcOver()) {
                    calcOver();
                }
            }
		} else {// 能操作 清理所有临时操作
            clearTempAction();
        }
        return canAction;
    }

    /**
	 * 执行可做操作里面优先级最高的玩家操作
	 *
	 * @param player
	 */
    private void refreshTempAction(QzMjPlayer player) {
        tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();// 各位置优先操作
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            int seat = entry.getKey();
            List<Integer> actionList = entry.getValue();
            List<Integer> list = MjDisAction.parseToDisActionList(actionList);
            int priorityAction = MjDisAction.getMaxPriorityAction(list);
            prioritySeats.put(seat, priorityAction);
        }
        int maxPriorityAction = Integer.MAX_VALUE;
        int maxPrioritySeat = 0;
		boolean isSame = true;// 是否有相同操作
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
        Iterator<MjTempAction> iterator = tempActionMap.values().iterator();
        while (iterator.hasNext()) {
            MjTempAction tempAction = iterator.next();
            if (tempAction.getSeat() == maxPrioritySeat) {
                int action = tempAction.getAction();
                List<QzMj> tempCardList = tempAction.getCardList();
                List<Integer> tempHuCards = tempAction.getHucards();
                QzMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
                iterator.remove();
				playCommand(tempPlayer, tempCardList, tempHuCards, action);// 系统选取优先级最高操作
                break;
            }
        }
        changeExtend();
    }

    /**
	 * 检查优先度，胡杠补碰吃 如果同时出现一个事件，按出牌座位顺序优先
	 *
	 * @param player
	 * @param action
	 * @return
	 */
    public boolean checkCanAction(QzMjPlayer player, int action) {
		// 优先度为胡杠补碰吃
        List<Integer> stopActionList = MjDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {
				// 别人
                boolean can = MjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = MjDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {
					// 同时拥有同一个事件 根据座位号来判断
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
	 * 是否能碰
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
    private boolean canPeng(QzMjPlayer player, List<QzMj> majiangs, int sameCount) {
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
     * 是否能碰
     *
     * @param player
     * @param majiangs
     * @return
     */
    private boolean canChi(QzMjPlayer player, List<QzMj> majiangs, QzMj disMj) {
        if (nowDisCardIds.isEmpty()) {
            return false;
        }
        TreeSet<Integer> set=new TreeSet();
        set.add(disMj.getVal());
        for (QzMj mj:majiangs) {
            set.add(mj.getVal());
        }
        Iterator<Integer> it = set.iterator();
        int x=0;
        while (it.hasNext()){
            if(x==0){
                x=it.next();
            }else {
                if(it.next()-x!=1)
                    return false;
                x=it.next();
            }
        }
        return true;
    }

    /**
	 * 是否能明杠
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
    private boolean canAnGang(QzMjPlayer player, List<QzMj> majiangs, int sameCount) {
        if (sameCount < 4) {
            return false;
        }
        if (player.getSeat() != getNextDisCardSeat()) {
            return false;
        }
        return true;
    }

    /**
	 * 是否能暗杠
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
    private boolean canMingGang(QzMjPlayer player, List<QzMj> majiangs, int sameCount) {
        List<QzMj> handMajiangs = player.getHandMajiang();
        List<Integer> pengList = MjHelper.toMajiangVals(player.getPeng());
        if (majiangs.size() == 1) {
            if (player.getSeat() != getNextDisCardSeat()) {
                return false;
            }
            if (handMajiangs.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal())) {
                return true;
            }
        } else if (majiangs.size() == 3) {
            if (sameCount != 3) {
                return false;
            }
//            if (nowDisCardIds.size() != 1 || nowDisCardIds.get(0).getVal() != majiangs.get(0).getVal()) {
//                return false;
//            }
            return true;
        }

        return false;
    }

    public Map<Integer, List<Integer>> getActionSeatMap() {
        return actionSeatMap;
    }


    public void setMoMajiangSeat(int moMajiangSeat) {
        this.moMajiangSeat = moMajiangSeat;
        disOrMo=2;
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

    public boolean isHuBaoHuDianPao(int seat){
        List<List<Integer>> hubaoList = getHuBao();
        if(hubaoList == null || hubaoList.size() <=0){
            return true;
        }
        for (List<Integer> list : hubaoList){
            if(list.contains(seat)){
                return true;
            }
        }
        return false;
    }

    public List<Integer> getHuBaoBySeat(int seat){
        List<List<Integer>> hubaoList = getHuBao();
        List<Integer> myHubao = new ArrayList<>();
        if(!hubaoList.isEmpty()){
            for (List<Integer> list : hubaoList){
                if(list.contains(seat)){
                    for (Integer i : list){
                        if(i != seat && !myHubao.contains(i)){
                            myHubao.add(i);
                        }
                    }
                }
            }
        }

       return myHubao;
    }

    /**
     * 获取互包的玩家
     * @return
     */
    public List<List<Integer>> getHuBao(){
        List<List<Integer>> reList = new ArrayList<>();
        for (QzMjPlayer player : seatMap.values()) {
            List<MjCardDisType> cardTypes = player.getCardTypes();
            if(cardTypes == null || cardTypes.size() < 3){
                continue;
            }
            Map<Integer,Integer> hubaoMap = new HashMap<>();
            for (MjCardDisType disType:cardTypes){
                if(hubaoMap.containsKey(disType.getDisSeat())){
                    hubaoMap.put(disType.getDisSeat(),hubaoMap.get(disType.getDisSeat())+1);
                }else{
                    hubaoMap.put(disType.getDisSeat(),1);
                }
            }
            List<Integer> list = new ArrayList<>();
            for (Entry<Integer,Integer> entry : hubaoMap.entrySet()){
                if(entry.getValue() >= 3){
                    list.add(player.getSeat());
                    list.add(entry.getKey());
                }
            }
            if(list.size() > 0){
                reList.add(list);
            }
        }
        return reList;
    }

    /**
	 * 能抢杠胡
	 *
	 * @return
	 */
    public boolean canGangHu() {
        return true;
    }


    /**
	 * @param over
	 * @param selfMo
	 * @param winList
	 * @param isBreak
	 * @return
	 */
    public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, int xingCardId, int xingbei,boolean isBreak,List<Integer> hubaoList) {

		// 大结算计算加倍分
        if (over && jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (QzMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
//                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
//                    int jbhf = player.getTotalPoint() * jiaBeiShu;
//                    if(maxPlayerCount == 2 && dajuFengding>0 && jbhf >dajuFengding){
//                        jiaBeiPoint = dajuFengding - player.getTotalPoint();
//                        player.setTotalPoint(dajuFengding);
//                    }else{
                        player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
//                    }

                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (QzMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //大结算低于below分+belowAdd分
        if(over&&belowAdd>0&&playerMap.size()==2){
            for (QzMjPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint >-below&&totalPoint<0) {
                    player.setTotalPoint(player.getTotalPoint()-belowAdd);
                }else if(totalPoint < below&&totalPoint>0){
                    player.setTotalPoint(player.getTotalPoint()+belowAdd);
                }
            }
        }

        if(over &&playerMap.size()==2 &&  dajuFengding>0){
            for (QzMjPlayer player : seatMap.values()) {
                if(player.getTotalPoint()  > 0 && player.getTotalPoint() >dajuFengding){
                    player.setTotalPoint(dajuFengding);
                }
                if(player.getTotalPoint()  < 0 && player.getTotalPoint() < -dajuFengding){
                    player.setTotalPoint(-dajuFengding);
                }
            }
        }

        List<ClosingMjPlayerInfoRes> list = new ArrayList<>();
        List<ClosingMjPlayerInfoRes.Builder> builderList = new ArrayList<>();
        int fangPaoSeat = selfMo ? 0 : disCardSeat;
        for (QzMjPlayer player : seatMap.values()) {
            ClosingMjPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }
            build.setBirdPoint(0);
//            if (winList != null && winList.contains(player.getSeat()) && !player.isZhuayu()) {
            if (winList != null && winList.contains(player.getSeat())) {
                build.setIsHu(lastId);
                if(hubaoList!=null && hubaoList.size() > 0){
                    build.addAllXiaohus(hubaoList);
                }
            }
            if (player.getSeat() == fangPaoSeat) {
                build.setFanPao(1);
                if(huConfirmList.isEmpty())
                    build.setFanPao(0);
            }
            if (winList != null && winList.contains(player.getSeat())) {
				// 手上没有剩余的牌放第一位为赢家
                builderList.add(0, build);
            } else {
                builderList.add(build);
            }
			// 信用分
            if (isCreditTable()) {
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }
        }

		// 信用分计算
        if (isCreditTable()) {
			// 计算信用负分
            calcNegativeCredit();
            long dyjCredit = 0;
            for (QzMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                QzMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (QzMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                QzMjPlayer player = seatMap.get(builder.getSeat());
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
        res.addAllExt(buildAccountsExt(over?1:0,xingCardId,xingbei));
        res.addCreditConfig(creditMode);                         //0
        res.addCreditConfig(creditJoinLimit);                    //1
        res.addCreditConfig(creditDissLimit);                    //2
        res.addCreditConfig(creditDifen);                        //3ClosingMjInfoRes
        res.addCreditConfig(creditCommission);                   //4
        res.addCreditConfig(creditCommissionMode1);              //5
        res.addCreditConfig(creditCommissionMode2);              //6
        res.addCreditConfig(creditCommissionLimit);              //7
        res.addAllLeftCards(MjHelper.toMajiangIds(leftMajiangs));
        res.addAllIntParams(getIntParams());
        for (QzMjPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        broadMsgRoomPlayer(res.build());
        return res;

    }


    public List<String> buildAccountsExt(int over,int xingCardId,int xingbei) {
        List<String> ext = new ArrayList<>();
        if (isGroupRoom()) {
            ext.add(loadGroupId());
        } else {
            ext.add("0");
        }
        ext.add(id + "");                                //1
        ext.add(masterId + "");                          //2
        ext.add(TimeUtil.formatTime(TimeUtil.now()));    //3
        ext.add(playType + "");                          //4
        ext.add(canDianPao + "");                        //5
        ext.add(lastWinSeat + "");                       //6
        ext.add(isAutoPlay + "");                        //7
        ext.add(isLiuJu() + "");                         //8
        ext.add(over+"");                                //9
        ext.add(kingId+"");                              //10
        ext.add(xingCardId+"");                            //11醒牌ID
        ext.add(xingbei+"");                            //11醒牌ID
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null,0, 0,true,null);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return QzMjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return getPlayType();
    }


    @Override
    public void checkReconnect(Player player) {
        ((QzMjPlayer) player).checkAutoPlay(0, true);
        if (state == table_state.play) {
            QzMjPlayer player1 = (QzMjPlayer) player;
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
        sendFirstAct(player);
    }

    private void sendFirstAct(Player player){
        if(disNum==0){
            boolean bankerFirst=true;
            for (Integer seat:actionSeatMap.keySet()) {
                List<Integer> list = actionSeatMap.get(seat);
                if(seat==lastWinSeat){
                    if(list.get(QzMjConstants.ACTION_INDEX_HU)==1){
                        bankerFirst=true;
                        break;
                    }
                }else {
                    if(list.get(QzMjConstants.ACTION_INDEX_BAOTING)==1)
                        bankerFirst=false;
                }
            }
            if(player.getSeat()==lastWinSeat)
                if(!bankerFirst)
                    player.writeComMessage(WebSocketMsgType.res_code_nxkwmj_haveAct);
                else
                    player.writeComMessage(WebSocketMsgType.res_code_nxkwmj_finishAct);
        }
    }


    @Override
    public boolean isAllReady() {
        if (getPlayerCount() < getMaxPlayerCount()) {
            return false;
        }
        for (Player player : getSeatMap().values()) {
            if(player.getState() != player_state.ready)
                return false;
        }

        if(finishFapai==1)
            return false;
        changeTableState(table_state.play);
        return true;
    }


    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }

    @Override
    public void checkAutoPlay() {
        if (getSendDissTime() > 0) {
            for (QzMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }

        if (getSendDissTime() > 0) {
            for (QzMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
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

			// 开了托管的房间，xx秒后自动开始下一局
            for (QzMjPlayer player : seatMap.values()) {
                if (player.getState() != player_state.entry && player.getState() != player_state.over) {
                    continue;
                } else {
                    if (readyTime >= 5 && player.isAutoPlay()) {
						// 玩家进入托管后，5秒自动准备
                        autoReady(player);
                    } else if (readyTime > 30) {
                        autoReady(player);
                    }
                }
            }
        }

    }



    /**
     * 自动出牌
     */
    public synchronized void autoPlay() {
        if (state != table_state.play) {
            return;
        }
        if (!actionSeatMap.isEmpty()) {
            List<Integer> huSeatList = getHuSeatByActionMap();
            if (!huSeatList.isEmpty()) {
                // 有胡处理胡
                for (int seat : huSeatList) {
                    QzMjPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), MjDisAction.action_hu);
                }
                return;
            } else {
                int action = 0, seat = 0;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    seat = entry.getKey();
                    List<Integer> actList = MjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }

                    action = MjDisAction.getAutoMaxPriorityAction(actList);
                    QzMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang()) {
                        chuPai = true;
                    }
                    if (action == MjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
                            // 自己开启托管直接过
                            playCommand(player, new ArrayList<>(), MjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                QzMj mj = nowDisCardIds.get(0);
                                List<QzMj> mjList = new ArrayList<>();
                                for (QzMj handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, MjDisAction.action_peng);
                            }
                        }
                    } else {
                        playCommand(player, new ArrayList<>(), MjDisAction.action_pass);
                        if (chuPai) {
                            autoChuPai(player);
                        }
                    }
                }
            }
        } else {
            QzMjPlayer player;
            player= seatMap.get(nowDisCardSeat);
            if (player == null || !player.checkAutoPlay(0, false)) {
                return;
            }
            autoChuPai(player);
        }
    }

    public void autoChuPai(QzMjPlayer player) {
        if (!player.isAlreadyMoMajiang()) {
            return;
        }
        List<Integer> handMjIds = new ArrayList<>(player.getHandPais());
        int mjId = -1;
        if (moMajiangSeat == player.getSeat()) {
            mjId = getMjIdNoWang(handMjIds,1);
        } else {
            Collections.sort(handMjIds);
            mjId = getMjIdNoWang(handMjIds,1);
        }
        if (mjId != -1) {
            List<QzMj> mjList = MjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, MjDisAction.action_chupai);
        }
    }

    private int getMjIdNoWang(List<Integer> handMjIds,int num){
        int mjId = handMjIds.get(handMjIds.size() - num);
        int val=QzMj.getMajang(mjId).getVal();
        if(chunWang.contains(val))
            return getMjIdNoWang(handMjIds,num+1);
        return mjId;
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
    }


	// 是否两人麻将
    public boolean isTwoPlayer() {
        return getMaxPlayerCount() == 2;
    }


    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_qzmj);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    public void logFaPaiTable(QzMj king) {
        StringBuilder sb = new StringBuilder();
        sb.append("QzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        sb.append("|").append(king);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(QzMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("QzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(QzMjPlayer player, int action, List<QzMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("QzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "unKnown-" + action;
        if (action == MjDisAction.action_peng) {
            actStr = "peng";
        } else if (action == MjDisAction.action_minggang) {
            actStr = "baoTing";
        } else if (action == MjDisAction.action_chupai) {
            actStr = "chuPai";
        } else if (action == MjDisAction.action_pass) {
            actStr = "guo";
        } else if (action == MjDisAction.action_angang) {
            actStr = "anGang";
        } else if (action == MjDisAction.action_chi) {
            actStr = "chi";
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(QzMjPlayer player, QzMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("QzMj");
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

    public void logMoMj(QzMjPlayer player, QzMj mj) {
        StringBuilder sb = new StringBuilder();
        sb.append("QzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("fengDongMoPai");
        sb.append("|").append(leftMajiangs.size());
        sb.append("|").append(mj);
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(QzMjPlayer player, QzMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("QzMj");
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

    public void logBaoTing(QzMjPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("QzMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("baoTing");
        sb.append("|").append(player.getHandMajiang());
        LogUtil.msg(sb.toString());
    }

    public void logActionHu(QzMjPlayer player, List<QzMj> mjs, String daHuNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("QzMj");
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
                if (i == QzMjConstants.ACTION_INDEX_HU) {
                    sb.append("hu");
                } else if (i == QzMjConstants.ACTION_INDEX_PENG) {
                    sb.append("peng");
                } else if (i == QzMjConstants.ACTION_INDEX_MINGGANG) {
                    sb.append("mingGang");
                } else if (i == QzMjConstants.ACTION_INDEX_ANGANG) {
                    sb.append("anGang");
                } else if (i == QzMjConstants.ACTION_INDEX_CHI) {
                    sb.append("chi");
                } else if (i == QzMjConstants.ACTION_INDEX_BAOTING) {
                    sb.append("baoTing");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
	 * 桌上剩余的牌数
	 *
	 * @return
	 */
    public int getLeftMajiangCount() {
        return this.leftMajiangs.size();
    }


    /**
	 * 是否流局
	 *
	 * @return
	 */
    public int isLiuJu() {
        int gangNum = 0;
        for (QzMjPlayer player : seatMap.values()) {
            gangNum += player.getGangNum();
        }
        return (huConfirmList.size() == 0
                && (leftMajiangs.size() == 0
                || (maxPlayerCount == 2 && getLeftMajiangCount() <= 62+gangNum*2 )
                ||(maxPlayerCount == 3 && getLeftMajiangCount() <= 48+gangNum*2)
                ||(maxPlayerCount == 4 && getLeftMajiangCount() <= 34+gangNum*2))) ? 1 : 0;
    }

    public void sendTingInfo(QzMjPlayer player) {
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
            List<QzMj> cards = new ArrayList<>(player.getHandMajiang());
            Map<Integer, List<Integer>> daTing = TingTool.getDaTing(MjHelper.toMajiangIds(cards),getChunWang(),player.getCardTypes());
            for(Entry<Integer, List<Integer>> entry : daTing.entrySet()){
                DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
                ting.addAllTingMajiangIds(entry.getValue());
                ting.setMajiangId(entry.getKey());
                tingInfo.addInfo(ting);
            }
            if(daTing.size()>0)
                player.writeSocket(tingInfo.build());

        } else {
            List<QzMj> cards = new ArrayList<>(player.getHandMajiang());
            TingPaiRes.Builder ting = TingPaiRes.newBuilder();
            List<Integer> tingP = TingTool.getTing(MjHelper.toMajiangIds(cards),getChunWang(),player.getCardTypes(),false);
            ting.addAllMajiangIds(tingP);
            if(tingP.size()>0)
                player.writeSocket(ting.build());
        }
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "全州麻将");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (isAutoPlay > 0) {
            json.put("autoTime", isAutoPlay);
            if (autoPlayGlob == 1) {
				json.put("autoName", "单局");
            } else {
				json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }

    @Override
    public String getGameName() {
		return "全州麻将";
    }
}
