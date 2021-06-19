package com.sy599.game.qipai.dddz.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.*;
import com.sy599.game.qipai.dddz.constant.DddzConstants;
import com.sy599.game.qipai.dddz.tool.CardTool;
import com.sy599.game.qipai.dddz.util.CardType;
import com.sy599.game.qipai.dddz.util.CardUtils;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DddzTable extends BaseTable {
    public static final String GAME_CODE = "Dddz";
    private static final int JSON_TAG = 1;
    /*** 当前牌桌上出的牌 */
    private volatile List<Integer> nowDisCardIds = new ArrayList<>();
    /*** 玩家map */
    private Map<Long, DddzPlayer> playerMap = new ConcurrentHashMap<Long, DddzPlayer>();
    /*** 座位对应的玩家 */
    private Map<Integer, DddzPlayer> seatMap = new ConcurrentHashMap<Integer, DddzPlayer>();
    /*** 最大玩家数量 */
    private volatile int maxPlayerCount = 5;

    private volatile int showCardNumber = 0; // 是否显示剩余牌数量

    public static final int FAPAI_PLAYER_COUNT = 3;// 发牌人数

    private volatile int timeNum = 0;


    private int lastbanker;
    /**
     * 托管1：单局，2：全局
     */
    private int autoPlayGlob;
    private int autoTableCount;


    // 新的一轮，3人为2人pass之后为新的一轮出牌
    private boolean newRound = true;
    /**
     * 托管时间
     */
    private volatile int autoTimeOut = 5 * 24 * 60 * 60 * 1000;
    private volatile int autoTimeOut2 = 5 * 24 * 60 * 60 * 1000;

    // 是否已经发牌
    private int finishFapai = 0;
    // 是否已经飘分
    private int finishiPiaofen = 0;
    // 一轮出的牌都会临时存在这里
    private volatile List<PlayerCard> noPassDisCard = new ArrayList<>();
    // 回放手牌
    private volatile String replayDisCard = "";

    private List<Integer> dipai = new ArrayList<>();// 底牌
    private List<Integer> zhuoFen = new ArrayList<>();// 捉的分
    private List<Integer> turnWinSeats = new ArrayList<>();
    /**
     * 叫的分
     **/
    private int jiaoFen;
    private boolean isPai = false;// 是否加拍
    private int zhuColor = -1;//  方片 1 梅花2 红3 黑桃4
    private int banker = 0;// 庄家座位

    private int turnFirstSeat = 0;// 当前轮第一个出牌的座位
    private int disColor; //轮首次出的花色 方片 1 梅花2 洪涛3 黑桃4  5主牌
    private int turnNum;//回合，每出一轮为1回合

    /**
     * 特殊状态 1
     **/
    private int tableStatus = 0;
    // 低于below加分
    private int belowAdd = 0;
    private int below = 0;

    /*可出龙牌*/
    private int op_chulongpai = 0;

    /*地主可投降*/
    private int op_bankerTX = 0;

    /*可出翻版*/
    private int op_chuFB = 0;

    /*底分*/
    private int op_difen = 0;

    /*满3、4、5、人可开 不限人数=1*/
    private int op_autoStarPlayerNum = 5;

    /*叫分的时候叫春天了 1 */
    private int isJiaoCt = 0;

    /*第一个叫地主的人啊*/
    private int firstJiaoDzSeat = 0;

    /*牌桌剩余总分*/
    private int deskSyScore = 160;

    /*地主捡分*/
    private int bankGetScore = 0;


    /*一直地主出牌 =1 */
    private int dzdct = 1;
    /* 1=毙掉  2=盖毙  */
    private int isKill = 0;
    private int calcType = 0;

    //是否点过中途打春天
    private int bankerClickedZTCT = 0;
    //是否点过头像
    private int bankerClickedTouXiang = 0;

    /**
     * 牌中春天 默认-1 打牌中春天=1 不打=0
     */
    private int pzct = -1;

    /** 地主不扣底牌 算分X2*/
    private int bankerKouDiPai=1;

    public DddzTable() {
        bankGetScore = 0;
        deskSyScore = 160;
        dzdct = 1;
        pzct = -1;
        isJiaoCt = 0;
        isKill = 0;
        bankerClickedZTCT = 0;
        bankerClickedTouXiang = 0;
        bankerKouDiPai=1;
    }

    private long touxiangTime;

    public String getReplayDisCard() {
        return replayDisCard;
    }

    public void setReplayDisCard(String replayDisCard) {
        this.replayDisCard = replayDisCard;
    }


    @Override
    protected void loadFromDB1(TableInf info) {
        if (!StringUtils.isBlank(info.getNowDisCardIds())) {
            this.nowDisCardIds = StringUtil.explodeToIntList(info.getNowDisCardIds());
        }
        if (!StringUtils.isBlank(info.getHandPai9())) {
            this.dipai = StringUtil.explodeToIntList(info.getHandPai9());
        }
        if (!StringUtils.isBlank(info.getHandPai10())) {
            this.zhuoFen = StringUtil.explodeToIntList(info.getHandPai10());
        }
        if (!StringUtils.isBlank(info.getOutPai9())) {
            this.turnWinSeats = StringUtil.explodeToIntList(info.getOutPai9());
        }

    }

    public long getId() {
        return id;
    }

    public DddzPlayer getPlayer(long id) {
        return playerMap.get(id);
    }


    /**
     * 一局结束
     */
    public void calcOver() {
        if (state == table_state.ready) {
            return;
        }
        int score = bankGetScore;
        int calcType = 0;
        //投降
        if (1 == seatMap.get(banker).getTouXiang()) {
            calcType = 10;
            int winscore = op_difen*bankerKouDiPai;
            for (DddzPlayer pl : seatMap.values()) {
                if (pl.getSeat() == banker) {
                    pl.calcLost(1, winscore * -1 * (seatMap.size() - 1));
                } else {
                    pl.calcWin(1, winscore);
                }
            }
        } else {
            //地主直接叫春天

            if (dzdct == 1 && jiaoFen == 300 && isJiaoCt == 1) {
                calcType = 1;
                //地主直接 叫春天打赢
                // System.out.println("地主直接 叫春天打赢");
                int winscore = op_difen * 8*bankerKouDiPai;
                for (DddzPlayer pl : seatMap.values()) {
                    if (pl.getSeat() == banker) {
                        pl.calcWin(1, winscore * (seatMap.size() - 1));
                    } else {
                        pl.calcLost(1, -1 * winscore);
                    }
                }
            } else if (dzdct == 0 && jiaoFen == 300 && isJiaoCt == 1) {
                calcType = 2;
                // System.out.println(" 地主直接 叫春天打输 ");
                int winscore = op_difen * 8*bankerKouDiPai;
                for (DddzPlayer pl : seatMap.values()) {
                    if (pl.getSeat() == banker) {
                        pl.calcLost(1, winscore * -1 * (seatMap.size() - 1));
                    } else {
                        pl.calcWin(1, winscore);
                    }
                }
            } else if ((jiaoFen <= 160 && pzct == 1)) {
                //7、如果地主捡分达到了叫分之后再叫春天,输赢为4倍。
                //地主叫分并且进行牌中春天玩法
                if (dzdct == 1) {
                    calcType = 3;
                    // System.out.println(" 地主叫分并且进行牌中春天玩法  地主win ");
                    int winscore = op_difen * 4*bankerKouDiPai;
                    for (DddzPlayer pl : seatMap.values()) {
                        if (pl.getSeat() == banker) {
                            pl.calcWin(1, winscore * (seatMap.size() - 1));
                        } else {
                            pl.calcLost(1, -1 * winscore);
                        }
                    }
                } else {
                    // System.out.println(" 地主叫分并且进行牌中春天玩法  nongming win ");
                    int winscore = op_difen * 4*bankerKouDiPai;
                    calcType = 4;
                    for (DddzPlayer pl : seatMap.values()) {
                        if (pl.getSeat() == banker) {
                            pl.calcLost(1, winscore * -1 * (seatMap.size() - 1));
                        } else {
                            pl.calcWin(1, winscore);
                        }
                    }
                }
                //score>=jiaofen
            } else if ( dzdct == 1 && isJiaoCt == 0 && pzct == -1) {
                //	8、如果地主未达到叫分,但是每手牌都大过农民,打出春天,则每人赔地主4	倍。
               // 2020年9月23日 add 如果地主在结束时刚好达到叫分，但是每手牌都大过农民,打出春天,则每人赔地主4倍。
                calcType = 7;
                // System.out.println(" 如果地主未达到叫分,但是每手牌都大过农民,打出春天  地主win ");
                int winscore = op_difen * 4*bankerKouDiPai;
                for (DddzPlayer pl : seatMap.values()) {
                    if (pl.getSeat() == banker) {
                        pl.calcWin(1, winscore * (seatMap.size() - 1));
                    } else {
                        pl.calcLost(1, -1 * winscore);
                    }
                }

            } else {
                if (score >= jiaoFen) {
                    //地主捡分大于叫分 1配
                    // System.out.println("地主捡分大于叫分");
                    calcType = 8;
                    int winscore = op_difen*bankerKouDiPai;
                    for (DddzPlayer pl : seatMap.values()) {
                        if (pl.getSeat() == banker) {
                            pl.calcWin(1, winscore * (seatMap.size() - 1));
                        } else {
                            pl.calcLost(1, -1 * winscore);
                        }
                    }
                } else if (score >= (jiaoFen / 2) && score < jiaoFen) {
                    //3、如果：叫分的一半=<地主捡分<小于叫分,则地主赔每人1倍。
                    calcType = 9;
                    // System.out.println("如果：叫分的一半=<地主捡分<小于叫分,则地主赔每人1倍");
                    int winscore = op_difen * 1*bankerKouDiPai;
                    for (DddzPlayer pl : seatMap.values()) {
                        if (pl.getSeat() == banker) {
                            pl.calcLost(1, winscore * -1 * (seatMap.size() - 1));
                        } else {
                            pl.calcWin(1, winscore);
                        }
                    }
                } else if (score < (jiaoFen / 2) && score > 0) {
                    //、如果:地主捡分<叫分的一半,则算小关,地主每人赔2倍。 小关
                    // System.out.println("如果:地主捡分<叫分的一半,则算小关,地主每人赔2倍");
                    calcType = 5;
                    int winscore = op_difen * 2*bankerKouDiPai;
                    for (DddzPlayer pl : seatMap.values()) {
                        if (pl.getSeat() == banker) {
                            pl.calcLost(1, winscore * -1 * (seatMap.size() - 1));
                        } else {
                            pl.calcWin(1, winscore);
                        }
                    }
                } else if (score == 0) {
                    //5、如果:地主捡分==0,则算大关,地主每人赔4倍。
                    // System.out.println("如果:地主捡分≤0,则算大关,地主每人赔4倍");
                    calcType = 6;
                    int winscore = op_difen * 4*bankerKouDiPai;
                    for (DddzPlayer pl : seatMap.values()) {
                        if (pl.getSeat() == banker) {
                            pl.calcLost(1, winscore * -1 * (seatMap.size() - 1));
                        } else {
                            pl.calcWin(1, winscore);
                        }
                    }
                }else if (score < 0) {
                    //5、如果:地主捡分<0,则算大关,地主每人赔8倍。
                    // System.out.println("如果:地主捡分≤0,则算大关,地主每人赔4倍");
                    calcType = 6;
                    int winscore = op_difen * 8*bankerKouDiPai;
                    for (DddzPlayer pl : seatMap.values()) {
                        if (pl.getSeat() == banker) {
                            pl.calcLost(1, winscore * -1 * (seatMap.size() - 1));
                        } else {
                            pl.calcWin(1, winscore);
                        }
                    }
                }
            }
        }
        boolean isOver = playBureau >= totalBureau;
        if (autoPlayGlob > 0) {
            // //是否解散
            boolean diss = false;
            if (autoPlayGlob == 1) {
                for (DddzPlayer seat : seatMap.values()) {
                    if (seat.isAutoPlay()) {
                        diss = true;
                        break;
                    }
                }
            } else if (autoPlayGlob == 3) {
                diss = checkAuto3();
            }

            if (diss || autoPlayDiss) {
                autoPlayDiss = true;
                isOver = true;
            }
        }
        calcAfter();
        ClosingInfoRes.Builder res = sendAccountsMsg(isOver, false, score, false, 0, seatMap.get(banker).getTouXiang() == 1, calcType);
        saveLog(isOver, 0, res.build());
        setLastWinSeat(banker);//下轮开始 地主先先叫
        lastbanker = banker;
        if (isOver) {
            calcOver1();
            calcOver2();
            calcOver3();
            diss();
        } else {
            initNext();
            calcOver1();
        }

    }

    /**
     * 单局 投降算分
     */
    private void calTouxiFen() {
        DddzPlayer losePlayer = seatMap.get(banker);
//		if(touxiangXW==1 || touxiangXW==2){
//			int winScore = touxiangXW;
//			int xianjia_piaofen = 0;
//			for (DddzPlayer player : seatMap.values()) {
//				if (player.getSeat() == banker) {
//					continue;
//				}
//				if(option_piaofen==1){
//					if(player.getPiaofen()==-1){
//						player.setPiaofen(0);
//					}
//					xianjia_piaofen += player.getPiaofen();
//					player.calcWin(1, winScore+player.getPiaofen()+losePlayer.getPiaofen());
//				}else{
//					player.calcWin(1, winScore);
//				}
//
//			}
//			if(option_piaofen==1){
//				losePlayer.calcLost(1,-winScore*(maxPlayerCount - 1)-xianjia_piaofen-(maxPlayerCount - 1)*losePlayer.getPiaofen());
//			}else{
//				losePlayer.calcLost(1,-winScore*(maxPlayerCount - 1));
//			}
//		}else{
//			System.out.println(" ERROR ！ 投降算分参数touxiangXW错误");
//		}

    }

    private boolean isTouXiang() {
        List<Integer> agreeTX = new ArrayList<Integer>();
        for (DddzPlayer player : seatMap.values()) {
            if (player.getTouXiang() == 2 || player.getTouXiang() == 1) {
                agreeTX.add(player.getTouXiang());
            }
        }
        if (agreeTX.size() == maxPlayerCount) {
            return true;
        }
        return false;
    }


    private boolean checkAuto3() {
        boolean diss = false;
//		if(autoPlayGlob==3) {
        boolean diss2 = false;
        for (DddzPlayer seat : seatMap.values()) {
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
//		}
        return diss;
    }

    @Override
    public void calcDataStatistics2() {
        // 俱乐部房间 单大局大赢家、单大局大负豪、总小局数、单大局赢最多、单大局输最多 数据统计
        if (isGroupRoom()) {
            String groupId = loadGroupId();
            int maxPoint = 0;
            int minPoint = 0;
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            // 俱乐部活动总大局数
            calcDataStatistics3(groupId);

            //Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue
            for (DddzPlayer player : playerMap.values()) {
                // 总小局数
                DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
                // 总大局数
                DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
                // 总积分
                DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", player.loadScore());
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);
                if (player.loadScore() > 0) {
                    if (player.loadScore() > maxPoint) {
                        maxPoint = player.loadScore();
                    }
                    // 单大局赢最多
                    DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore", player.loadScore());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
                } else if (player.loadScore() < 0) {
                    if (player.loadScore() < minPoint) {
                        minPoint = player.loadScore();
                    }
                    // 单大局输最多
                    DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore", player.loadScore());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
                }
            }

            for (DddzPlayer player : playerMap.values()) {
                if (maxPoint > 0 && maxPoint == player.loadScore()) {
                    // 单大局大赢家
                    DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
                } else if (minPoint < 0 && minPoint == player.loadScore()) {
                    // 单大局大负豪
                    DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dfhCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 2);
                }
            }
        }
    }


    public void saveLog(boolean over, long winId, Object resObject) {
        ClosingInfoRes res = (ClosingInfoRes) resObject;
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + res);
        String logRes = JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
        Map<String, Object> map = LogUtil.buildClosingInfoResOtherLog(res);
        map.put("intParams", getIntParams());
        String logOtherRes = JacksonUtil.writeValueAsString(map);
        Date now = TimeUtil.now();

        UserPlaylog userLog = new UserPlaylog();
        userLog.setUserId(creatorId);
        userLog.setLogId(playType);
        userLog.setTableId(id);
        userLog.setRes(logRes);
        userLog.setTime(now);
        userLog.setTotalCount(totalBureau);
        userLog.setCount(playBureau);
        userLog.setStartseat(lastWinSeat);
        userLog.setOutCards(playLog);
        userLog.setExtend(logOtherRes);
        userLog.setMaxPlayerCount(maxPlayerCount);
        userLog.setType(creditMode == 1 ? 2 : 1);
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);

        if (!isGoldRoom()) {
            for (DddzPlayer player : playerMap.values()) {
                player.addRecord(logId, playBureau);
            }
        }

        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

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
                tempMap.put("outPai1", StringUtil.implodeLists(seatMap.get(1).getOutPais()));
            }
            if (tempMap.containsKey("outPai2")) {
                tempMap.put("outPai2", StringUtil.implodeLists(seatMap.get(2).getOutPais()));
            }
            if (tempMap.containsKey("outPai3")) {
                tempMap.put("outPai3", StringUtil.implodeLists(seatMap.get(3).getOutPais()));
            }
            if (tempMap.containsKey("outPai4")) {
                tempMap.put("outPai4", StringUtil.implodeLists(seatMap.get(4).getOutPais()));
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

            if (tempMap.containsKey("handPai9")) {
                tempMap.put("handPai9", StringUtil.implode(dipai, ","));
            }

            if (tempMap.containsKey("answerDiss")) {
                tempMap.put("answerDiss", buildDissInfo());
            }
            if (tempMap.containsKey("nowDisCardIds")) {
                tempMap.put("nowDisCardIds", StringUtil.implode(nowDisCardIds, ","));
            }
            if (tempMap.containsKey("extend")) {
                tempMap.put("extend", buildExtend());
                tempMap.put("handPai10", StringUtil.implode(zhuoFen, ","));
                tempMap.put("outPai9", StringUtil.implode(turnWinSeats, ","));
            }
//			TableDao.getInstance().save(tempMap);
        }
        return tempMap.size() > 0 ? tempMap : null;
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper wrapper) {
//		JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putInt(2, maxPlayerCount);
        wrapper.putInt(3, showCardNumber);
//		return wrapper.toString();
        wrapper.putString(5, replayDisCard);
        wrapper.putInt(6, autoTimeOut);
        wrapper.putInt(7, autoPlayGlob);
        wrapper.putInt(8, jiaoFen);
        wrapper.putInt(9, newRound ? 1 : 0);
        wrapper.putInt(10, finishFapai);
        wrapper.putInt(11, belowAdd);
        wrapper.putInt(12, below);
        wrapper.putInt(13, isPai ? 1 : 0);
        wrapper.putInt(14, zhuColor);
        wrapper.putInt(15, banker);
        wrapper.putInt(16, turnFirstSeat);
        wrapper.putInt(17, disColor);
        wrapper.putInt(18, turnNum);
        wrapper.putInt(19, tableStatus);
        wrapper.putLong(20, touxiangTime);
        wrapper.putInt(21, lastbanker);
        wrapper.putInt(22, op_chulongpai);
        wrapper.putInt(23, op_bankerTX);
        wrapper.putInt(24, op_chuFB);
        wrapper.putInt(25, op_difen);
        wrapper.putInt(26, op_autoStarPlayerNum);
        wrapper.putInt(27, isJiaoCt);
        wrapper.putInt(28, firstJiaoDzSeat);
        wrapper.putInt(29, bankGetScore);
        wrapper.putInt(30, deskSyScore);
        wrapper.putInt(31, 0);
        wrapper.putInt(32, 0);
        wrapper.putInt(33, dzdct);
        wrapper.putInt(34, pzct);
        wrapper.putInt(35, isKill);
        wrapper.putInt(36, nowDisCardSeat);
        wrapper.putInt(37, bankerClickedTouXiang);
        wrapper.putInt(38, bankerClickedZTCT);
        wrapper.putInt(39, bankerKouDiPai);

        return wrapper;
    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
//		JsonWrapper wrapper = new JsonWrapper(info);
        maxPlayerCount = wrapper.getInt(2, 3);
        if (maxPlayerCount == 0) {
            maxPlayerCount = 4;
        }
        showCardNumber = wrapper.getInt(3, 0);
        if (payType == -1) {
            String isAAStr = wrapper.getString("isAAConsume");
            if (!StringUtils.isBlank(isAAStr)) {
                this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume")) ? 1 : 2;
            } else {
                payType = 1;
            }
        }
        replayDisCard = wrapper.getString(5);
        autoTimeOut = wrapper.getInt(6, 0);
        autoPlayGlob = wrapper.getInt(7, 0);
        jiaoFen = wrapper.getInt(8, 0);
        newRound = wrapper.getInt(9, 1) == 1;
        finishFapai = wrapper.getInt(10, 0);
        belowAdd = wrapper.getInt(11, 0);
        below = wrapper.getInt(12, 0);
        autoTimeOut2 = autoTimeOut;
        // 设置默认值
        if (autoPlay && autoTimeOut <= 1) {
            autoTimeOut2 = autoTimeOut = 60000;
        }
        isPai = wrapper.getInt(13, 0) == 1;
        zhuColor = wrapper.getInt(14, 0);
        banker = wrapper.getInt(15, 0);
        turnFirstSeat = wrapper.getInt(16, 0);
        disColor = wrapper.getInt(17, 1);
        turnNum = wrapper.getInt(18, 0);
        tableStatus = wrapper.getInt(19, 0);
        touxiangTime = wrapper.getLong(20, 0);
        lastbanker = wrapper.getInt(21, 0);
        op_chulongpai = wrapper.getInt(22, 0);
        op_bankerTX = wrapper.getInt(23, 0);
        op_chuFB = wrapper.getInt(24, 0);
        op_difen = wrapper.getInt(25, 0);
        op_autoStarPlayerNum = wrapper.getInt(26, 0);
        isJiaoCt = wrapper.getInt(27, 0);
        firstJiaoDzSeat = wrapper.getInt(28, 0);
        bankGetScore = wrapper.getInt(29, 0);
        deskSyScore = wrapper.getInt(30, 0);
        // JiaoCtRate=0;
        // ztJiaoCtRate=0;
        dzdct = wrapper.getInt(33, 0);
        pzct = wrapper.getInt(34, 0);
        isKill = wrapper.getInt(35, 0);
        nowDisCardSeat = wrapper.getInt(36, 0);
        bankerClickedTouXiang = wrapper.getInt(37, 0);
        bankerClickedZTCT = wrapper.getInt(38, 0);
        bankerKouDiPai=wrapper.getInt(39, 1);
    }

    protected String buildPlayersInfo() {
        StringBuilder sb = new StringBuilder();
        for (DddzPlayer pdkPlayer : playerMap.values()) {
            sb.append(pdkPlayer.toInfoStr()).append(";");
        }
        // playerInfos = sb.toString();
        return sb.toString();
    }

    public void changePlayers() {
        dbParamMap.put("players", JSON_TAG);
    }

    public void changeCards(int seat) {
        dbParamMap.put("outPai" + seat, JSON_TAG);
        dbParamMap.put("handPai" + seat, JSON_TAG);
    }

    /**
     * 开始发牌
     */
    public void fapai() {
        playLog = "";
        synchronized (this) {
            changeTableState(table_state.play);
            timeNum = 0;
            List<List<Integer>> list;
            list = CardTool.fapai(seatMap.size(), zp);
            for (int i=0;i<maxPlayerCount;i++) {
                DddzPlayer player = seatMap.get(i+1);
                player.changeState(player_state.play);
                player.dealHandPais(list.get(i), this);

                if (!player.isAutoPlay()) {
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }
                addGameActionLog(player, "fapai|" + player.getHandPais());
            }
            setDipai(list.get(maxPlayerCount));
        }
        finishFapai = 1;
        noticeJiaofen();
    }


    @Override
    public int getNextDisCardSeat() {
        if (disCardSeat == 0) {
            return banker;
        }
        return calcNextSeat(disCardSeat);
    }

    /**
     * 计算seat右边的座位
     *
     * @param seat
     * @return
     */
    public int calcNextSeat(int seat) {
        int nextSeat = seat + 1 > maxPlayerCount ? 1 : seat + 1;
        return nextSeat;
    }

    public DddzPlayer getPlayerBySeat(int seat) {
        //int next = seat >= maxPlayerCount ? 1 : seat + 1;
        return seatMap.get(seat);

    }

    private void addGameActionLog(Player player, String str) {

        StringBuilder sb = new StringBuilder("DaDouDiZhu");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.getName());
        sb.append("|").append(str);
        LogUtil.msgLog.info(sb.toString());
    }

    public Map<Integer, Player> getSeatMap() {
        Object o = seatMap;
        return (Map<Integer, Player>) o;
    }


    @Override
    public CreateTableRes buildCreateTableRes(long userId, boolean isrecover, boolean isLastReady) {
        CreateTableRes.Builder res = CreateTableRes.newBuilder();
        buildCreateTableRes0(res);
        synchronized (this) {
            res.setNowBurCount(getPlayBureau());
            res.setTotalBurCount(getTotalBureau());
            res.setGotyeRoomId(gotyeRoomId + "");
            res.setTableId(getId() + "");
            res.setWanfa(playType);
            List<PlayerInTableRes> players = new ArrayList<>();
            for (DddzPlayer player : playerMap.values()) {
                PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
                if (playerRes == null) {
                    continue;
                }
                if (player.getUserId() == userId) {
                    // 如果是自己重连能看到手牌
                    playerRes.addAllHandCardIds(player.getHandPais());
                } else {
                    // 如果是别人重连，轮到出牌人出牌时要不起可以去掉
                }
                if (player.getSeat() == banker && getTableStatus() == DddzConstants.TABLE_STATUS_PLAY) {
                    playerRes.addAllMoldIds(dipai);
                }

                if (player.getSeat() == disCardSeat && nowDisCardIds != null && nowDisCardIds.size() > 0) {
                    //  playerRes.addAllOutCardIds(nowDisCardIds);
//                    playerRes.addRecover(cardType);
                }
                players.add(playerRes.build());
            }
            res.addAllPlayers(players);
            //int nextSeat = getNextDisCardSeat();

//            if(getTableStatus() == DddzConstants.TABLE_STATUS_JIAOFEN) {
//            	nextSeat = getNextActionSeat();
//            }
            if (nowDisCardSeat != 0) {
                res.setNextSeat(nowDisCardSeat);
            }

            //桌状态 1叫分2选主3埋牌 8飘分
            res.setRemain(getTableStatus());
            res.addAllScoreCard(zhuoFen);
            res.setRenshu(this.maxPlayerCount);

            res.addExt(this.payType);//0支付方式
            res.addExt(jiaoFen);//1牌桌叫的分
            res.addExt(zhuColor);//2叫的主的花色
            res.addExt(banker);//3庄的座位号

            if (isJiaoCt == 1) {  //  1= 叫分直接春天  2 中途春天
                res.addExt(1);
            } else if (pzct == 1) {
                res.addExt(2);
            } else {
                res.addExt(0);
            }

            res.addExt(bankGetScore);//分值

            res.addExt(CommonUtil.isPureNumber(modeId) ? Integer.parseInt(modeId) : 0);// 5
            int ratio;
            int pay;

            ratio = 1;
            pay = consumeCards() ? loadPayConfig(payType) : 0;
            res.addExt(ratio);//6
            res.addExt(pay);//7
            res.addExt(lastWinSeat);//8

            res.addExtStr(String.valueOf(matchId));//0
            res.addExtStr(cardMarkerToJSON());//1
            res.addTimeOut(autoPlay ? autoTimeOut : 0);
//			if (autoPlay) {
//				if (disCardRound == 0) {
//					res.addTimeOut((autoTimeOut + 5000));
//				} else {
//					res.addTimeOut(autoTimeOut);
//				}
//			} else {
//				res.addTimeOut(0);
//			}

            res.addExt(playedBureau);//11
            res.addExt(disCardRound);//12
            res.addExt(creditMode); //14
            res.addExt(creditCommissionMode1);//19
            res.addExt(creditCommissionMode2);//20
            res.addExt(autoPlay ? 1 : 0);//21
            res.addExt(tableStatus);//25
        }

        return res.build();
    }

    public int getOnTablePlayerNum() {
        int num = 0;
        for (DddzPlayer player : seatMap.values()) {
            if (player.getIsLeave() == 0) {
                num++;
            }
        }
        return num;
    }


    /**
     * 出牌
     *
     * @param player
     * @param cards
     */
    public void disCards(DddzPlayer player, List<Integer> cards) {
        setDisCardSeat(player.getSeat());

        if (turnFirstSeat == player.getSeat()) {
            //插画
            int res = CardTool.checkCardValue(player.getHandPais(), cards, zhuColor, disColor, true, op_chulongpai);
            if (res < 0) {
                player.writeErrMsg("出牌不符合规则。");
                return;
            } else {
                int type = res % 10;
                if (type >= CardType.SHUAIPAI) {
                    Map<String, Object> map = CardTool.checkShuaiPai(cards, seatMap, zhuColor, player.getSeat());

                    if (null != map) {
                        int fen = (int) map.get("score");
                        if (fen < 0) {
                            //甩失败
                            addGameActionLog(player, "|shuaiPaiFail|cards=" + cards + "|result=" + map.toString());

                            if (player.getSeat() == banker) {
                                bankGetScore = bankGetScore + fen;
                            } else {
                                bankGetScore = bankGetScore - fen;
                            }
                            ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pk_dddz_shuaiPaiFail, player.getSeat() + "", bankGetScore + "", cards);
                            for (DddzPlayer splayer : seatMap.values()) {
                                splayer.writeSocket(builder.build());
                            }
                            addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_REPLAY_CallBackPai, cards, false, bankGetScore, null, getNextDisCardSeat()));

                            //addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_REPLAY_SHUAIPAI, (List<Integer>) map.get("Cards"), true, bankGetScore, null, getNextDisCardSeat()));
                        }
                        cards = (List<Integer>) map.get("Cards");
                    }
                }
                disColor = res;
            }
        } else {
            DddzPlayer bankP = seatMap.get(turnFirstSeat);
            List<Integer> list = bankP.getCurOutCard(getTurnNum());
            if (list.size() != cards.size()) {
                player.writeErrMsg("出牌不符合规则。");
                return;
            }
            int res = CardTool.checkCardValue(player.getHandPais(), cards, zhuColor, disColor, false, op_chulongpai);
            if (res < 0) {
                player.writeErrMsg("出牌不符合规则。");
                return;
            }
        }
        player.addOutPais(cards, this);
        setDisCardSeat(player.getSeat());
        int nextSeat = getNextDisCardSeat();
        //能否盖毙当前桌牌
        int cankill = outCardKillDeskCards(player.getSeat());
        // 构建出牌消息
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setIsClearDesk(0);
        res.setCardType(0);
        res.setIsBt(cankill);
        boolean isOver = false;
        if (turnFirstSeat != player.getSeat()) {
            List<Integer> firstList = seatMap.get(turnFirstSeat).getCurOutCard(getTurnNum());
            boolean firstZhu = CardTool.allZhu(firstList, zhuColor);
            int baofu = 0;
            if (firstZhu) {
                baofu += checkBaofu(firstZhu, player, cards);
            }
            res.setIsLet(baofu);
        }
        if (nextSeat == turnFirstSeat) {// 一轮打完
            // 1.算一轮哪家大
            // 2.下一轮谁出牌
            // 3.报副状态设置
            isOver = turnOver(res, player, cards);
        } else {
            setNowDisCardSeat(nextSeat);
            changeExtend();
            addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_STATUS_PLAY, cards, false, bankGetScore, null, getNextDisCardSeat()));
        }


        setNowDisCardIds(cards);
        if (cards != null) {
            noPassDisCard.add(new PlayerCard(player.getName(), cards));
        }

        res.addAllCardIds(cards);
        res.setNextSeat(getNowDisCardSeat());
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setIsPlay(2);
        setReplayDisCard();


        for (DddzPlayer p : seatMap.values()) {
            p.writeSocket(res.build());
        }
        if(!isOver){
            //地主过叫分的一半之后,如果整个牌面剩余分数不足以打够地主叫分,地主可	以选择直接投降。(可在创建房间时勾选是否带此玩法)
            if (jiaoFen!=300 && bankGetScore >= jiaoFen / 2 && bankGetScore + deskSyScore < jiaoFen && op_bankerTX == 1 && bankerClickedTouXiang == 0) {
                //没叫春天 可投降
                setNowDisCardSeat(banker);
                ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pk_dddz_dztouxiang, banker, -1);
                for (DddzPlayer splayer : seatMap.values()) {
                    splayer.writeSocket(builder.build());
                }
                setTableStatus(DddzConstants.TABLE_STATUS_SELDZTX);
            }

            //牌中春天:如果地主捡分达到所叫分,并且之前每轮牌都大过农民,则可以	  选择是否继续打春天,不打则直接结束。若打春天,则后面只要被压过一次	  就算春天失败。
            if (bankGetScore >= jiaoFen && dzdct == 1 && bankerClickedZTCT == 0 && pzct == -1) {
                if(getNowDisCardSeat() == banker){
                    //选择是否继续打春天
                    //setNowDisCardSeat(banker);
                    ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pk_dddz_dzztdct, banker, -1);
                    for (DddzPlayer splayer : seatMap.values()) {
                        splayer.writeSocket(builder.build());
                    }
                    setTableStatus(DddzConstants.TABLE_STATUS_SELZTCT);
                }
            }
            //当地主捡分大于叫分时且打不了春天的时候，直接结束牌局
            if(bankGetScore >= jiaoFen && dzdct == 0){
                isOver = true;
            }
        }

        if (isOver) {
            state = table_state.over;
        }

    }

    private int outCardKillDeskCards(int seat) {
        HashMap<Integer, CardType> pmap = new HashMap<Integer, CardType>();
//        int proSeat = 0;
//        if(maxPlayerCount==4){
//            if(seat==4||seat==3||seat ==2){
//                proSeat=seat-1;
//            }else if(seat==1){
//                proSeat=4;
//            }
//        }else if(maxPlayerCount==3){
//            if(seat==3||seat ==2){
//                proSeat=seat-1;
//            }else if(seat==1){
//                proSeat=4;
//            }
//        }

        for (DddzPlayer p : seatMap.values()) {
            List<Integer> list = p.getCurOutCard(getTurnNum());
            if (null == list) {
                continue;
            }
            pmap.put(p.getSeat(), CardTool.getCardType(new ArrayList<>(list), zhuColor, op_chulongpai));
        }

        //CardType result = CardTool.getTunKill(pmap, turnFirstSeat, zhuColor, maxPlayerCount);
         CardType result = CardTool.getTunWin2(pmap, turnFirstSeat, zhuColor);
        List<Integer> firstOut2 = seatMap.get(turnFirstSeat).getCurOutCard(getTurnNum());
        List<Integer> firstOut = new ArrayList<>(firstOut2); //防止下面变动指针改变数据内容
        if (firstOut == null) {
            return 0;
        }
        if (CardUtils.isSameColor(firstOut) && !CardTool.haveZhu(firstOut, zhuColor)) {
            //第一家出副  第二家出主
            if (seat == turnFirstSeat) {
                return 0;
            }

            List<Integer>  myOut2= seatMap.get(seat).getCurOutCard(getTurnNum());
            List<Integer> myOut =  new ArrayList<>(myOut2);
            if (null == myOut) {
                return 0;
            }

            if (CardTool.allZhu(myOut, zhuColor) && seat == result.getType()) {
                if (isKill == 0) {
                    isKill = 1;
                    return 1;
                } else if (isKill == 1) {
                    return 2;
                }
            }
        }
        return 0;
    }

    private boolean turnOver(PlayCardRes.Builder res, DddzPlayer player, List<Integer> cards) {
        boolean isOver = true;
        HashMap<Integer, CardType> pmap = new HashMap<Integer, CardType>();

        for (DddzPlayer p : seatMap.values()) {
            List<Integer> list = p.getCurOutCard(getTurnNum());
            pmap.put(p.getSeat(), CardTool.getCardType(new ArrayList<>(list), zhuColor, op_chulongpai));
            if (p.getHandPais().size() != 0) {
                isOver = false;
            }
        }
        CardType result = CardTool.getTunWin2(pmap, turnFirstSeat, zhuColor);
        //System.err.println("一轮结束：");
        //System.err.println(result.getType()+"| ");
        List<Integer> fenCards = null;
        if (result.getCardIds().size() > 0 && result.getType() == banker) {
            zhuoFen.addAll(result.getCardIds());
            res.addAllScoreCard(result.getCardIds());
            fenCards = result.getCardIds();
            bankGetScore = bankGetScore + CardUtils.loadCardScore(fenCards);
        }
        //int totalScore = CardUtils.loadCardScore(zhuoFen);
        //剩余桌分
        deskSyScore = deskSyScore - CardUtils.loadCardScore(result.getCardIds());
        res.setCurScore(bankGetScore);
        res.setIsClearDesk(1);

        addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_STATUS_PLAY, cards, false, bankGetScore, fenCards, result.getType()));

        setNowDisCardSeat(result.getType());
        setTurnFirstSeat(result.getType());
        turnWinSeats.add(result.getType());
        if ((pzct == 1 || dzdct == 1) && result.getType() != banker) {
            dzdct = 0;//地主打春天 或者地主打牌中春天
            if (isJiaoCt == 1 || pzct == 1) {
                //开局选择大春天 失败。结算
                isOver = true;
            }
        }
        addTurnNum(1);
        disColor = 0;
        isKill = 0;

        return isOver;
    }


    private int checkBaofu(boolean firstZhu, DddzPlayer p, List<Integer> list) {
        if (p.getBaofu() == 1) {
            return CardTool.getBaofuValue(p.getSeat());
        }
        int baofu = 0;
        if (firstZhu && p.getSeat() != turnFirstSeat) {
            boolean oZhu = CardTool.allZhu(list, zhuColor);
            if (!oZhu) {//报副
                p.setBaofu(1);
                baofu += CardTool.getBaofuValue(p.getSeat());
            }
        }
        return baofu;
    }


    public void setReplayDisCard() {
        List<PlayerCard> cards = new ArrayList<>();
        int size = noPassDisCard.size();
        for (int i = 0; i < 3 && i < size; i++) {
            cards.add(noPassDisCard.get(size - 1 - i));
        }
        setReplayDisCard(cards.toString());
        noPassDisCard.clear();
    }


    /**
     * 打牌
     *
     * @param player
     * @param cards
     */
    public void playCommand(DddzPlayer player, int action, List<Integer> cards) {
        synchronized (this) {
            if (state != table_state.play) {
                return;
            }
            //100111
            if (action == DddzConstants.REQ_MAIPAI) {
                playMaipai(player, cards);
                return;
            }

            //出牌阶段
            if (getTableStatus() != DddzConstants.TABLE_STATUS_PLAY) {
                return;
            }

            if (!containCards(player.getHandPais(), cards)) {
                return;
            }

            StringBuilder sb = new StringBuilder("DaDouDiZhu");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append("chuPai");
            sb.append("|").append(cards);
            sb.append("|").append(CardUtils.toStringCards(cards).toString());
            LogUtil.msgLog.info(sb.toString());

            if (cards != null && cards.size() > 0) {
                changeDisCardRound(1);
                // 出牌了
                disCards(player, cards);
            } else {
                if (disCardRound > 0) {
                    changeDisCardRound(1);
                }
            }
            setLastActionTime(TimeUtil.currentTimeMillis());
            if (isOver()) {
                calcOver();
            } else {
                int nextSeat = calcNextSeat(player.getSeat());
                DddzPlayer nextPlayer = seatMap.get(nextSeat);
                if (!nextPlayer.isRobot()) {
                    nextPlayer.setNextAutoDisCardTime(TimeUtil.currentTimeMillis() + autoTimeOut);
                }
            }
        }
    }

    private boolean containCards(List<Integer> handCards, List<Integer> cards) {
        for (Integer id : cards) {
            if (!handCards.contains(id)) {
                return false;
            }
        }
        return true;

    }

    /**
     * @param player
     * @param cards
     */
    private void playMaipai(DddzPlayer player, List<Integer> cards) {
        if (seatMap.size() < 5 && cards.size() != 4) {
            return;
        }
        if (seatMap.size() == 5 && cards.size() != 2) {
            return;
        }
        if (getTableStatus() != DddzConstants.TABLE_STATUS_MAIPAI) {
            addGameActionLog(player, "NoMaiPaiState");
            return;
        }
        deskSyScore = deskSyScore - CardUtils.loadCardScore(cards);
        setDipai(cards);
        addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_STATUS_MAIPAI, cards, false, 0, null, player.getSeat()));
        PlayCardRes.Builder res = PlayCardRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setSeat(player.getSeat());
        res.setCardType(DddzConstants.REQ_MAIPAI);
        res.setIsPlay(1);
        res.setNextSeat(player.getSeat());
        res.addAllCardIds(cards);

        for (DddzPlayer sanPlayer : seatMap.values()) {
            sanPlayer.writeSocket(res.build());
        }
        for (Integer id : dipai) {
            player.getHandPais().remove(id);
//			player.removeHandPais(dipai);
        }
        //addTurnNum(1);
        setTurnFirstSeat(player.getSeat());
        setTableStatus(DddzConstants.TABLE_STATUS_PLAY);
    }


    public String addSandhPlayLog(int seat, int action, List<Integer> cards, boolean shuaiPaiFail, int fen, List<Integer> fenCards, int nextSeat) {
        JSONObject json = new JSONObject();
        json.put("seat", seat);
        json.put("action", action);
        json.put("vals", cards);
        if (shuaiPaiFail) {
            json.put("shuaiPaiFail", 1);
        }
        json.put("fen", fen);
        if (fenCards != null) {
            json.put("fenCards", fenCards);
        }
        json.put("nextSeat", nextSeat);
        return json.toJSONString();

    }


    public void playXuanzhu(DddzPlayer player, int zhu) {
        if (nowDisCardSeat != player.getSeat()) {
            LogUtil.msgLog.info("now actionseat is error  + nowDisCardSeat = " + nowDisCardSeat + "actionSeat = " + player.getSeat());
            return;
        }
        if (zhu < 0 || zhu > 4) {
            LogUtil.msgLog.info("xuanzhu  params error zhu" + zhu + " seat = " + player.getSeat());
            return;
        }
        if (zhuColor != -1) {
            LogUtil.msgLog.info("has already xuanzhu " + player.getSeat());
            return;
        }

        if (getTableStatus() != DddzConstants.TABLE_STATUS_XUANZHU) {
            addGameActionLog(player, "NoXuanZhuState");
            return;
        }

        zhuColor = zhu;


        ArrayList<Integer> val = new ArrayList<>();
        val.add(zhu);
        addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_STATUS_XUANZHU, val, false, 0, null, player.getSeat()));
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_XUANZHU, zhu);
        for (DddzPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }
        setTableStatus(DddzConstants.TABLE_STATUS_KOUDIPAI);



    }


    public void playChuPaiRecord(DddzPlayer player) {
        JSONArray jarr = new JSONArray();
        for (DddzPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", splayer.getSeat());
            JSONArray jarr2 = new JSONArray();
            for (int i = 1; i <= turnNum; i++) {
                JSONObject json2 = new JSONObject();
                if (i > turnWinSeats.size()) {
                    continue;
                }
                Integer seat = turnWinSeats.get(i - 1);
                List<Integer> cards = splayer.getCurOutCard(i);
                json2.put("cards", cards);
                json2.put("win", splayer.getSeat() == seat ? 1 : 0);
                jarr2.add(json2);

            }
            json.put("cardArr", jarr2);
            jarr.add(json);
        }
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_CHUPAI_RECORD, jarr.toJSONString());
        player.writeSocket(builder.build());
    }

    /**
     * 发送已出分牌
     *
     * @param player
     */
    public void playFenPaiRecord(DddzPlayer player) {
        JSONArray jarr = new JSONArray();
        for (DddzPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", splayer.getSeat());
            List<List<Integer>> outpais = splayer.getOutPais();
            List<Integer> fecardsAry = new ArrayList<>();
            for (int i = 0; i < outpais.size(); i++) {
                List<Integer> cards = outpais.get(i);
                for (int fencard : cards) {
                    int val = CardUtils.loadCardValue(fencard);
                    if (val == 5 || val == 10 || val == 13) {
                        fecardsAry.add(fencard);
                    }
                }
                json.put("cards", fecardsAry);
            }
            jarr.add(json);
        }
        ComRes.Builder builder = SendMsgUtil.buildComRes(4102, jarr.toJSONString());
        player.writeSocket(builder.build());
    }

    /**
     * 查看分牌
     *
     * @param player
     */
    public void playChaDi(DddzPlayer player) {
        if (player.getSeat() != banker) {
            return;
        }
        JSONArray jarr = new JSONArray();
        for (DddzPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", splayer.getSeat());
            json.put("cardArr", dipai);
            jarr.add(json);
        }
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_CHUPAI_RECORD, jarr.toJSONString());
        player.writeSocket(builder.build());
    }

//    public void playLiushou(DddzPlayer player, int color) {
//        for (DddzPlayer splayer : seatMap.values()) {
//            if (splayer.getBaofu() == 0 && splayer.getSeat() != banker) {
//                return;
//            }
//        }
//        player.setLiushou(color);
//
//        ArrayList<Integer> val = new ArrayList<>();
//        val.add(color);
//        addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_LIUSHOU_PLAY, val, player.getBaofu() == 1 ? true : false, 0, null, getNowDisCardSeat()));
//        if (!player.isAutoPlay()) {
//            player.setAutoPlay(false, this);
//            player.setLastOperateTime(System.currentTimeMillis());
//        }
//        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_Liushou, player.getSeat(), color);
//        for (DddzPlayer splayer : seatMap.values()) {
//            splayer.writeSocket(builder.build());
//        }
//    }

    /**
     * @param player 玩家
     * @param type   //投降  1：投降  0=不投降
     */
    public void playTouxiang(DddzPlayer player, int type) {
        if (type > 1) {
            return;
        }
        if (player.getTouXiang() == 1) {
            return;
        }
        if (state == table_state.ready) {
            return;
        }
        if (getTableStatus() != DddzConstants.TABLE_STATUS_SELDZTX) {
            return;
        }
        if (player.getSeat() != banker) {
            return;
        }
        List<Integer> touxs = new ArrayList<Integer>();
        player.setTouXiang(type);
        bankerClickedTouXiang = 1;
        int nextseat = getTurnFirstSeat();
        setNowDisCardSeat(nextseat);
        changeExtend();
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pk_dddz_dztouxiang, nextseat, type);
        for (DddzPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }
        if (type == 1) {
            setTouxiangTime(TimeUtil.currentTimeMillis());
            sendTouxiangMsg(touxs);
            state = table_state.over;
            if (state == table_state.over) {
                calcOver();
            }
        } else {
            setTableStatus(DddzConstants.TABLE_STATUS_PLAY);
        }
        addGameActionLog(player, "|TouXiang=" + type + "|nextseat=" + nextseat);
        List val = new ArrayList();
        val.add(type);
        addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_STATUS_SELDZTX, val, false, 0, null, nextseat));


    }

    /**
     * 地主选择中途春天
     *
     * @param player
     * @param type   1打中途春天 0不打
     */
    public void playZtct(DddzPlayer player, int type) {
        if (type > 1) {
            return;
        }
        if (player.getTouXiang() == 1) {
            return;
        }
        if (state == table_state.ready) {
            return;
        }
        if (getTableStatus() != DddzConstants.TABLE_STATUS_SELZTCT) {
            return;
        }
        if (player.getSeat() != banker) {
            return;
        }
        pzct = type;
        int nextseat = getTurnFirstSeat();
        setNowDisCardSeat(nextseat);
        bankerClickedZTCT = 1;
        changeExtend();
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pk_dddz_dzztdct, nextseat, type);
        for (DddzPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }
        if (type == 0) {
            state = table_state.over;
            calcOver();
        } else {
            setTableStatus(DddzConstants.TABLE_STATUS_PLAY);
        }
        addGameActionLog(player, "|zhongTuChunTian=" + type + "|nextseat=" + nextseat);
        List val = new ArrayList();
        val.add(type);
        addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_STATUS_SELZTCT, val, false, 0, null, nextseat));

    }

    /**
     * 地主选择扣底牌
     *
     * @param player
     * @param type   1扣 0不扣底牌
     */
    public void playKouDiPai(DddzPlayer player, int type) {
        if (type > 1) {
            return;
        }

        if (getTableStatus() != DddzConstants.TABLE_STATUS_KOUDIPAI) {
            return;
        }
        if (player.getSeat() != banker) {
            return;
        }
        int nextseat =banker;
        turnFirstSeat=banker;
        setNowDisCardSeat(banker);
        addTurnNum(1);
        changeExtend();
        //扣底牌消息录像
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pk_dddz_koudipai, nextseat, type);
        for (DddzPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }
        addGameActionLog(player, "|KouDiPai=" + type + "|nextseat=" + nextseat);
        List val = new ArrayList();
        val.add(type);
        addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_STATUS_KOUDIPAI, val, false, 0, null, nextseat));

        if(type==1){
            bankerKouDiPai=1;
            // 拿底牌埋牌
            DddzPlayer banker1 = seatMap.get(banker);
            banker1.addHandPais(dipai);
            addPlayLog(addSandhPlayLog(banker, DddzConstants.TABLE_DINGZHUANG, dipai, false, 0, null, banker));
            addGameActionLog(player, "底牌=" + dipai);
            ComRes.Builder builder2 = SendMsgUtil.buildComRes(WebSocketMsgType.RES_DINGZHUANG, dipai, banker + "");
            for (DddzPlayer splayer : seatMap.values()) {
                splayer.writeSocket(builder2.build());
            }
            setTableStatus(DddzConstants.TABLE_STATUS_MAIPAI);
        }else{
            bankerKouDiPai=2;
            //不扣X2
            setTableStatus(DddzConstants.TABLE_STATUS_PLAY);
        }


    }
    private void sendTouxiangMsg(List<Integer> touxs) {
        JSONArray jarr = new JSONArray();
        for (DddzPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", splayer.getSeat());
            json.put("state", splayer.getTouXiang());
            if (splayer.getTouXiang() == 1 || splayer.getTouXiang() == 2) {
                touxs.add(splayer.getTouXiang());
            }
            jarr.add(json);
        }

        int txTime = (int) (TimeUtil.currentTimeMillis() - getTouxiangTime());
        txTime = autoTimeOut - txTime;
        if (txTime < 0 || !autoPlay) {
            txTime = 0;
        }
        //
        ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_TOUX, txTime, jarr.toString());
        for (DddzPlayer splayer : seatMap.values()) {
            splayer.writeSocket(builder.build());
        }
    }

    /**
     * 叫分
     *
     * @param player
     * @param fen
     */
    public void playJiaoFen(DddzPlayer player, int fen) {
        if (nowDisCardSeat != player.getSeat()) {
            return;
        }

        if (getTableStatus() != DddzConstants.TABLE_STATUS_JIAOFEN) {
            return;
        }
        int qijiaofen = 40;
        if (seatMap.size() < 5) {
            qijiaofen = 80;
        } else {
            qijiaofen = 40;
        }
        if (fen < qijiaofen && fen != 0) {
            return;
        }
        int bankerSeat = 0;
        if (firstJiaoDzSeat == 0) {
            firstJiaoDzSeat = player.getSeat();
        }
        if (fen != 0 && fen > jiaoFen) {
            setJiaoFen(fen);
            banker = player.getSeat();
        }
        setDisCardSeat(player.getSeat());
        player.setJiaofen(fen);
        addGameActionLog(player, "setJiaofen=" + fen);
        int nextActionSeat = 0;
        int nextS = player.getSeat();
        for (int i = 0; i < maxPlayerCount - 1; i++) {
            nextS += 1;
            if (nextS > maxPlayerCount) {
                nextS = 1;
            }
            DddzPlayer nextPlayer = seatMap.get(nextS);
            if (nextPlayer.getJiaofen() == 0) {
                continue;
            }
            nextActionSeat = nextPlayer.getSeat();
            break;
        }

        boolean isjiaofenOver = false;
        int count0 = 0;
        int allJiaoguo = 0;
        for (DddzPlayer p : playerMap.values()) {
            if (p.getJiaofen() == 0) {
                count0++;
            }
            if (p.getJiaofen() >= 0) {
                allJiaoguo++;
            }
        }
        // 都不叫或者仅剩1人叫 叫分结束
        if ((count0 == (maxPlayerCount - 1) && jiaoFen > 0) || (count0 == maxPlayerCount && jiaoFen == 0)) {
            isjiaofenOver = true;
            bankerSeat = banker;
        }

        //叫了160可以叫春天。 // 有人叫160 了 且4个人都叫过了叫分结束
        if ((fen == 160 && allJiaoguo == maxPlayerCount) || (jiaoFen == 160 && allJiaoguo == maxPlayerCount)) {
            isjiaofenOver = true;
        }
        if (fen == 300) {
            isjiaofenOver = true;
            isJiaoCt = 1;//叫春天 当地主
        }
        if (isjiaofenOver) {
            bankerSeat = banker;
        }

        boolean isAllBujiao = false;
        if (isjiaofenOver) {
            for (DddzPlayer p : playerMap.values()) {
                if (p.getJiaofen() == 0) {
                    isAllBujiao = true;
                    continue;
                } else {
                    isAllBujiao = false;
                    break;
                }
            }
        }
        setNowDisCardSeat(nextActionSeat);
        ArrayList<Integer> val = new ArrayList<>();
        val.add(fen);
        val.add(0);
        addPlayLog(addSandhPlayLog(player.getSeat(), DddzConstants.TABLE_STATUS_JIAOFEN, val, false, 0, null, nextActionSeat));

        ComRes.Builder jbuilder = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_JIAOFEN, fen, 0, player.getSeat(),
                bankerSeat == 0 ? nextActionSeat : 0);

        for (DddzPlayer splayer : seatMap.values()) {
            splayer.writeSocket(jbuilder.build());
        }

        // 定地主
        if (bankerSeat != 0) {
            List<Integer> dipailist = new ArrayList<>();
            ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_DINGZHUANG, dipailist, bankerSeat + "");
            for (DddzPlayer splayer : seatMap.values()) {
                splayer.writeSocket(builder.build());
            }
            banker = bankerSeat;
            lastbanker = banker;
            turnFirstSeat = banker;
            setNowDisCardSeat(bankerSeat);
            changeExtend();
            addPlayLog(addSandhPlayLog(bankerSeat, DddzConstants.TABLE_DINGZHUANG, dipailist, false, 0, null, nextActionSeat));
            setTableStatus(DddzConstants.TABLE_STATUS_XUANZHU);
        }

        if (isAllBujiao) {
            jiaoFen = qijiaofen;
            banker = firstJiaoDzSeat;
            lastbanker = banker;
            turnFirstSeat = banker;
            setNowDisCardSeat(firstJiaoDzSeat);
            changeExtend();
            ArrayList<Integer> val2 = new ArrayList<>();
            val2.add(jiaoFen);
            val2.add(0);
            addPlayLog(addSandhPlayLog(bankerSeat, DddzConstants.TABLE_DINGZHUANG, new ArrayList<>(), false, 0, null, nextActionSeat));
            ComRes.Builder jbuilder2 = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_JIAOFEN, qijiaofen, 0, firstJiaoDzSeat);
            for (DddzPlayer splayer : seatMap.values()) {
                splayer.writeSocket(jbuilder2.build());
            }
            List<Integer> dipailist = new ArrayList<>();
            ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_DINGZHUANG, dipailist, banker + "");
            for (DddzPlayer splayer : seatMap.values()) {
                splayer.writeSocket(builder.build());
            }
            setTableStatus(DddzConstants.TABLE_STATUS_XUANZHU);
        }
    }


    public int getAutoTimeOut() {
        return autoTimeOut;
    }

    /**
     * 人数未满或者人员离线
     *
     * @return 0 可以打牌 1人数未满 2人员离线
     */
    public int isCanPlay() {
        if (seatMap.size() < getMaxPlayerCount()) {
            return 1;
        }
        for (DddzPlayer player : seatMap.values()) {
            if (player.getIsEntryTable() != SharedConstants.table_online) {
                // 通知其他人离线
                broadIsOnlineMsg(player, player.getIsEntryTable());
                return 2;
            }
        }
        return 0;
    }

    @Override
    public <T> T getPlayer(long id, Class<T> cl) {
        return (T) playerMap.get(id);
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
    protected void initNext1() {
        setNowDisCardIds(null);
        replayDisCard = "";
        timeNum = 0;
        newRound = true;
        finishFapai = 0;
        zhuoFen.clear();
        dipai.clear();
        turnNum = 0;
        turnFirstSeat = 0;
        zhuColor = -1;
        banker = 0;
        jiaoFen = 0;
        isPai = false;
        turnWinSeats.clear();
        setTableStatus(0);
        setTouxiangTime(0);
        finishiPiaofen = 0;
        isJiaoCt = 0;
        firstJiaoDzSeat = 0;
        bankGetScore = 0;
        deskSyScore = 160;
        dzdct = 1;
        pzct = -1;
        isKill = 0;
        bankerClickedZTCT = 0;
        bankerClickedTouXiang = 0;
        bankerKouDiPai=1;
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

//        if (playedBureau == 0) {
//			DddzPlayer player = playerMap.get(masterId);
//			int masterseat = player != null ? player.getSeat() : seatMap.keySet().iterator().next();
//			nowDisCardSeat = masterseat;
//			lastWinSeat = masterseat;
//		} else {
//			nowDisCardSeat = lastWinSeat;
//		}
//		if(option_piaofen==1 && finishiPiaofen ==0){
//			noticePiaofen();
//			return ;
//		}else{
//			noticeJiaofen();
//		}


    }

    private void noticeJiaofen() {
        //第一句随机。第二句上把坐庄
        if (playedBureau == 0 || lastbanker == 0) {
            nowDisCardSeat = RandomUtils.nextInt(maxPlayerCount) + 1;
        } else {
            nowDisCardSeat = lastbanker;
        }
        setDisCardSeat(nowDisCardSeat);
        changeExtend();
        //进入叫分环节
        setTableStatus(DddzConstants.TABLE_STATUS_JIAOFEN);
        for (DddzPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            //叫分的人座位
            res.setNextSeat(nowDisCardSeat);
            res.setGameType(getWanFa());//
            res.setRemain(getTableStatus());
            // res.setBanker(lastWinSeat);
            tablePlayer.writeSocket(res.build());
            if (tablePlayer.isAutoPlay()) {
                ArrayList<Integer> val = new ArrayList<>();
                val.add(1);
                addPlayLog(addSandhPlayLog(tablePlayer.getSeat(), DddzConstants.action_tuoguan, val, false, 0, null, 0));
            }
        }
    }

    @Override
    protected void robotDealAction() {
    }

    @Override
    protected void deal() {

    }

    @Override
    public Map<Long, Player> getPlayerMap() {
        Object o = playerMap;
        return (Map<Long, Player>) o;
    }

    @Override
    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }


    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
        changeExtend();
    }

    public List<Integer> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setNowDisCardIds(List<Integer> nowDisCardIds) {
        if (nowDisCardIds == null) {
            this.nowDisCardIds.clear();

        } else {
            this.nowDisCardIds = nowDisCardIds;

        }
        dbParamMap.put("nowDisCardIds", JSON_TAG);
    }

    public boolean saveSimpleTable() throws Exception {
        TableInf info = new TableInf();
        info.setMasterId(masterId);
        info.setRoomId(0);
        info.setPlayType(playType);
        info.setTableId(id);
        info.setTotalBureau(totalBureau);
        info.setPlayBureau(1);
        info.setServerId(GameServerConfig.SERVER_ID);
        info.setCreateTime(new Date());
        info.setDaikaiTableId(daikaiTableId);
        changeExtend();
        info.setExtend(buildExtend());
        TableDao.getInstance().save(info);
        loadFromDB(info);
        return true;
    }

    public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, boolean saveDb) throws Exception {
        return createTable(player, play, bureauCount, params, saveDb);
    }

    public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
        createTable(player, play, bureauCount, params, true);
    }

    public boolean createTable(Player player, int play, int bureauCount, List<Integer> params, boolean saveDb) throws Exception {
        // objects对象的值列表
        long id = getCreateTableId(player.getUserId(), play);
        if (id <= 0) {
            return false;
        }
        if (saveDb) {
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
        } else {
            setPlayType(play);
            setDaikaiTableId(daikaiTableId);
            this.id = id;
            this.totalBureau = bureauCount;
            this.playBureau = 1;
        }

        payType = StringUtil.getIntValue(params, 2, 1);// 1AA,2房主
        op_difen = StringUtil.getIntValue(params, 3, 1);
        op_chulongpai = StringUtil.getIntValue(params, 4, 1);
        ;
        op_bankerTX = StringUtil.getIntValue(params, 5, 1);
        op_chuFB = StringUtil.getIntValue(params, 6, 1);
        maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// 人数
        if (maxPlayerCount == 0) {
            maxPlayerCount = 4;
        }
        int time = StringUtil.getIntValue(params, 8, 0);
        this.autoPlay = time > 1;
        autoPlayGlob = StringUtil.getIntValue(params, 9, 0); //1单局  2整局  3三局
        if (time > 0) {
            autoTimeOut2 = autoTimeOut = (time * 1000);
        }
        setLastActionTime(TimeUtil.currentTimeMillis());
        return true;
    }

    @Override
    protected void initNowAction(String nowAction) {

    }


    @Override
    protected String buildNowAction() {
        return null;
    }

    @Override
    public void setConfig(int index, int val) {

    }

    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, Player winPlayer, boolean isBreak) {
        return sendAccountsMsg(over, isBreak, 0, false, 0, false, 0);
    }

    /**
     * 发送结算msg
     * calcType值如下
     * 起手打春天成功1
     * 起手打春天失败2
     * 牌中打春天成功3
     * 牌中打春天失败4
     * 小关 5
     * 大关6
     * 地主未达到叫分 未叫春天牌中没选择打春天却打出了春天  7
     * 地主捡分大于叫分 地主赢了8
     * 叫分的一半=<地主捡分<小于叫分 地主输了 9
     * 投降 10
     *
     * @param over 是否已经结束
     * @return
     */
    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, boolean isBreak, int score, boolean koudi, int difen, boolean touxiang, int calcType ) {
        List<ClosingPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();

        int minPointSeat = 0;
        int minPoint = 0;
//        if (winPlayer != null) {
//            for (DddzPlayer player : seatMap.values()) {
//                if (player.getUserId() == winPlayer.getUserId()) {
//                    continue;
//                }
//                if (minPoint == 0 || player.getPoint() < minPoint) {
//                    minPoint = player.getPlayPoint();
//                    minPointSeat = player.getSeat();
//                }
//            }
//        }


        // 大结算低于below分+belowAdd分
        if (over && belowAdd > 0 && playerMap.size() == 2) {
            for (DddzPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint > -below && totalPoint < 0) {
                    player.setTotalPoint(player.getTotalPoint() - belowAdd);
                } else if (totalPoint < below && totalPoint > 0) {
                    player.setTotalPoint(player.getTotalPoint() + belowAdd);
                }
            }
        }


        for (DddzPlayer player : seatMap.values()) {
            ClosingPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();

            }

            build.addExt("0");// 3
            build.addExt("0");// 4
            build.addExt("0");// 5

            build.addExt(String.valueOf(player.getCurrentLs()));// 6
            build.addExt(String.valueOf(player.getMaxLs()));// 7
            build.addExt(String.valueOf(matchId));// 8

//            if (winPlayer != null && player.getUserId() == winPlayer.getUserId()) {
//				// 手上没有剩余的牌放第一位为赢家
//                builderList.add(0, build);
//            } else {
            builderList.add(build);
//            }

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
            for (DddzPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                DddzPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);

                builder.addExt(player.getWinLoseCredit() + "");      //10
                builder.addExt(player.getCommissionCredit() + "");   //11

                // 2019-02-26更新
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------亲友圈金币场---------------------------------
            for (DddzPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                DddzPlayer player = seatMap.get(builder.getSeat());
                builder.addExt(player.getWinLoseCredit() + ""); // 10
                builder.addExt(player.getCommissionCredit() + ""); // 11
                builder.setWinLoseCredit(player.getWinGold());
            }
        } else {
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                DddzPlayer player = seatMap.get(builder.getSeat());
                builder.addExt(0 + ""); //10
                builder.addExt(0 + ""); //11
            }
        }
        for (ClosingPlayerInfoRes.Builder builder : builderList) {
            DddzPlayer player = seatMap.get(builder.getSeat());
            builder.addExt(player.getPiaofen() + ""); //13
            list.add(builder.build());
        }

        ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllClosingPlayers(list);
        res.addAllExt(buildAccountsExt(over ? 1 : 0, score, koudi, touxiang));
        res.addExt(calcType + "");//26
        res.addExt(bankerKouDiPai+ "");//27
//        if(koudi){
//        	res.addAllCutCard(dipai);
//        }
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (DddzPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }

    public List<String> buildAccountsExt(int over, int score, boolean koudi, boolean touxiang) {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");//0
        ext.add(masterId + "");//1
        ext.add(TimeUtil.formatTime(TimeUtil.now()));//2
        ext.add(playType + "");//3
        // 设置当前第几局
        ext.add(playBureau + "");//4
        ext.add(isGroupRoom() ? "1" : "0");//5
        // 金币场大于0
        ext.add(CommonUtil.isPureNumber(modeId) ? modeId : "0");//6
        int ratio;
        int pay;
        ratio = 1;
        pay = loadPayConfig(payType);
        ext.add(String.valueOf(ratio));//7
        ext.add(String.valueOf(pay >= 0 ? pay : 0));//8
        ext.add(String.valueOf(payType));//9
        ext.add(String.valueOf(playedBureau));//10

        ext.add(String.valueOf(matchId));//11
        ext.add(isGroupRoom() ? loadGroupId() : "");//12

        ext.add(creditMode + ""); //13
        ext.add(creditJoinLimit + "");//14
        ext.add(creditDissLimit + "");//15
        ext.add(creditDifen + "");//16
        ext.add(creditCommission + "");//17
        ext.add(creditCommissionMode1 + "");//18
        ext.add(creditCommissionMode2 + "");//19
        ext.add((autoPlay ? 1 : 0) + "");//20
        ext.add(over + ""); // 21
        ext.add(jiaoFen + ""); // 22
        ext.add(score + ""); // 23
        ext.add(koudi ? "1" : "0");//24
        ext.add(isPai ? "1" : "0");//25
        return ext;
    }


    @Override
    public String loadGameCode() {
        return GAME_CODE;
    }

    @Override
    public void sendAccountsMsg() {
        ClosingInfoRes.Builder builder = sendAccountsMsg(true, null, true);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return DddzPlayer.class;
    }

    @Override
    public int getWanFa() {
        return GameUtil.play_type_pk_dddz;
    }

    @Override
    public void checkReconnect(Player player) {
        DddzTable table = player.getPlayingTable(DddzTable.class);
        // player.writeSocket(SendMsgUtil.buildComRes(WebSocketMsgType.req_code_pdk_playBack, table.getReplayDisCard()).build());

        checkTouxiang(player);
        //
    }

    private void checkTouxiang(Player player) {
        JSONArray jarr = new JSONArray();
        List<Integer> touxs = new ArrayList<Integer>();
        for (DddzPlayer splayer : seatMap.values()) {
            JSONObject json = new JSONObject();
            json.put("seat", splayer.getSeat());
            json.put("state", splayer.getTouXiang());
            touxs.add(splayer.getTouXiang());
            jarr.add(json);
        }
        if (touxs.contains(1) && !touxs.contains(3)) {
            int txTime = (int) (TimeUtil.currentTimeMillis() - getTouxiangTime());
            txTime = autoTimeOut - txTime;
            if (txTime < 0 || !autoPlay) {
                txTime = 0;
            }
            ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_TOUX, txTime, jarr.toString());
            player.writeSocket(builder.build());
        }
    }


    // 是否显示剩余牌的数量
    public boolean isShowCardNumber() {
        return 1 == getShowCardNumber();
    }

    @Override
    public void checkAutoPlay() {
        synchronized (this) {

            if (checkLastTurn()) {
                return;
            }
            if (!autoPlay) {
                return;
            }
            // 发起解散，停止倒计时
            if (getSendDissTime() > 0) {
                for (DddzPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }
            if (isAutoPlayOff()) {
                // 托管关闭
                for (int seat : seatMap.keySet()) {
                    DddzPlayer player = seatMap.get(seat);
                    player.setAutoPlay(false, this);
                }
                return;
            }
            // 准备托管
            if (state == table_state.ready && playedBureau > 0) {
                ++timeNum;
                for (DddzPlayer player : seatMap.values()) {
                    // 玩家进入托管后，5秒自动准备
                    if (timeNum >= 5 && player.isAutoPlay()) {
                        autoReady(player);
                    } else if (timeNum >= 30) {
                        autoReady(player);
                    }
                }
                return;
            }

//            //自动飘分飘分
//			if (state == table_state.play && getTableStatus()==DddzConstants.TABLE_STATUS_PIAOFEN && option_piaofen == 1 ) {
//				for ( DddzPlayer player: seatMap.values()){
//					long time2 =System.currentTimeMillis();
//					if((player.isAutoPlay()&& player.getPiaofen()==-1) || ( time2>player.getPiaofenCheckTime() && player.getPiaofen()==-1)){
//						playPiaoFen(player, 0);
//						player.setAutoPlay(true,player.getPlayingTable());
//					}
//				}
//				return;
//			}
            DddzPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null) {
                return;
            }

            if (getTableStatus() == 0 || state != table_state.play) {
                return;
            }

            // 托管投降检查
            checkTouxiangTimeOut();

            int timeout;

            if (autoPlay) {
                timeout = autoTimeOut;
                if (disCardRound == 0) {
                    timeout = autoTimeOut;
                }
            } else if (player.isRobot()) {
                timeout = 3 * SharedConstants.SENCOND_IN_MINILLS;
            } else {
                return;
            }

            long autoPlayTime = ResourcesConfigsUtil.loadIntegerValue("ServerConfig", "autoPlayTimePdk", 2 * 1000);
            long now = TimeUtil.currentTimeMillis();
            boolean auto = player.isAutoPlay();
            if (!auto) {
//                if (GameServerConfig.isAbroad()) {
//                    if (!player.isRobot() && now >= player.getNextAutoDisCardTime()) {
//                        auto = true;
//                        player.setAutoPlay(true, this);
//                    }
//                } else {
                auto = checkPlayerAuto(player, timeout);
//                }
            }

            if (auto || player.isRobot()) {
                boolean autoPlay = false;
                player.setAutoPlayTime(0L);
                if (state == table_state.play) {
                    if (getTableStatus() == DddzConstants.TABLE_STATUS_PLAY) {
                        //托管出牌
                        autoChuPai(player);
                    } else if (getTableStatus() == DddzConstants.TABLE_STATUS_JIAOFEN) {
                        playJiaoFen(player, 0);
                    } else if (getTableStatus() == DddzConstants.TABLE_STATUS_SELZTCT) {
                        playZtct(seatMap.get(banker), 0);
                    } else if (getTableStatus() == DddzConstants.TABLE_STATUS_SELDZTX) {
                        playTouxiang(seatMap.get(banker), 1);
                    } else if (getTableStatus() == DddzConstants.TABLE_STATUS_XUANZHU) {
                        playXuanzhu(player, 1);
                    } else if (getTableStatus() == DddzConstants.TABLE_STATUS_KOUDIPAI) {
                        playKouDiPai(player, 1);
                    } else  if (getTableStatus() == DddzConstants.TABLE_STATUS_MAIPAI) {
                        List<Integer> disList = new ArrayList<Integer>();
                        int size = 3;
                        if (maxPlayerCount < 5) {
                            size = 4;
                        }
                        List<Integer> zCards = CardUtils.getZhu(player.getHandPais(), zhuColor);
                        List<Integer> curList = new ArrayList<>(player.getHandPais());
                        curList.removeAll(zCards);
                        if (curList.size() < size) {
                            disList.addAll(curList);
                            disList.addAll(zCards.subList(0, size - curList.size()));
                        } else {
                            disList.addAll(curList.subList(0, size));
                        }
                        playMaipai(player, disList);
                    }

                }
            }
        }
//        }
    }

    private void checkTouxiangTimeOut() {
        if (tableStatus == DddzConstants.TABLE_STATUS_MAIPAI) {
            if (getTouxiangTime() > 0) {
                int txTime = (int) (TimeUtil.currentTimeMillis() - getTouxiangTime());
                List<Integer> agreeTX = new ArrayList<Integer>();
                for (DddzPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                    if (player.getTouXiang() == 0 && txTime >= autoTimeOut) {
                        player.setTouXiang(2);
                    }

                    if (player.getTouXiang() == 2 || player.getTouXiang() == 1) {
                        agreeTX.add(player.getTouXiang());
                    }
                }

                if (agreeTX.size() == maxPlayerCount) {
                    sendTouxiangMsg(agreeTX);
                    state = table_state.over;
                    calcOver();
                }

            }
        } else {
            //投降在开始打牌之前，开始打牌。检测投降状态，如果有清掉。
            if (getTableStatus() == DddzConstants.TABLE_STATUS_PLAY) {
                setTouxiangTime(0);
                for (DddzPlayer player : seatMap.values()) {
                    if (player.getTouXiang() > 0) {
                        player.setTouXiang(0);
                    }
                }
            }
        }
    }

    private boolean checkLastTurn() {
        if (getTableStatus() == DddzConstants.TABLE_STATUS_PLAY) {
            DddzPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null) {
                return false;
            }
            int firstSeat = getTurnFirstSeat();

            if (firstSeat != player.getSeat()) {
                DddzPlayer fiser = seatMap.get(firstSeat);
                if (fiser != null && fiser.getHandPais().isEmpty()) {
                    playCommand(player, 0, new ArrayList<>(player.getHandPais()));
                    return true;
                }
            } else {
                //除了这个人其他都报副了
                boolean allBaofu = true;
                for (Map.Entry<Integer, DddzPlayer> entry : seatMap.entrySet()) {
                    if (entry.getValue().getSeat() == firstSeat) {
                        continue;
                    }
                    if (entry.getValue().getBaofu() != 1) {
                        allBaofu = false;
                        break;
                    }
                }

                //都报副了就全部甩出去if(!CardTool.allZhu(cards, zhuColor)){
                boolean isAuto = false;
                CardType ct = CardTool.getCardType(player.getHandPais(), zhuColor, op_chulongpai);
                if (ct.getType() == CardType.DAN || ct.getType() == CardType.DUI || ct.getType() == CardType.TUOLAJI) {
                    isAuto = true;
                } else if (ct.getType() == CardType.SHUAI_LIAN_DUI || ct.getType() == CardType.SHUAIPAI || ct.getType() == CardType.SHUAIPAI2) {
                    if (allBaofu && CardTool.allZhu(player.getHandPais(), zhuColor)) {
                        isAuto = true;
                    }
                }
                if (isAuto) {
                    playCommand(player, 0, new ArrayList<>(player.getHandPais()));
                    return true;
                }


            }
        }
        return false;
    }

    private void autoChuPai(DddzPlayer player) {
        List<Integer> curList = new ArrayList<>(player.getHandPais());
        if (curList.isEmpty()) {
            return;
        }
        int firstSeat = getTurnFirstSeat();
        List<Integer> disList = new ArrayList<Integer>();
        // 轮首次出牌
        if (firstSeat == player.getSeat()) {
            // 随便出个单牌
            int rand = RandomUtils.nextInt(curList.size());
            disList.add(curList.get(rand));

        } else {

            int color = disColor / 10;
            int type = disColor % 10;
            DddzPlayer fiser = seatMap.get(turnFirstSeat);
            List<Integer> list = fiser.getCurOutCard(getTurnNum());
            boolean isallzhu =  CardUtils.isAllZhu(list,zhuColor);

            List<Integer> cards = null;
            if (color == zhuColor) {
                cards = CardUtils.getDianCards3(curList, color);
            } else {
                cards = CardUtils.getColorCards(curList, color);
            }
            if(zhuColor==0 && isallzhu){
                //无主的时候张数不够 如果调的主 ;cards应该为所有硬主的集合
                cards = CardUtils.getDianCards3(curList,zhuColor);
            }
            // 没有这个花色
            if (cards == null || cards.isEmpty()) {
                CardUtils.sortCards(curList);
                int addC = list.size();
                disList.addAll(curList.subList(0, addC));
            } else if (cards.size() < list.size()) {
                disList.addAll(cards);
                curList.removeAll(cards);
                CardUtils.sortCards(curList);
                int addC = list.size() - cards.size();
                disList.addAll(curList.subList(0, addC));
            } else {

                if (type == CardType.DAN) {
                    disList.add(cards.get(0));
                } else if (type == CardType.DUI || type == CardType.TUOLAJI) {
                    int needDuiCount = list.size() / 2;
                    List<Integer> dui = CardUtils.getDuiCards(cards, needDuiCount);
                    if (dui.isEmpty()) {
                        //无对子
                        if(cards.size()>0){
                            //无对但至少有1个同花色
                            List<Integer> dianSameColorPai = Collections.emptyList();
                            if(color==zhuColor){
                               dianSameColorPai = CardUtils.getDianCards3(curList,color);
                            } else{
                               dianSameColorPai =  CardUtils.getColorCards(curList,color);
                            }
                         
                            if(list.size()>dianSameColorPai.size()){
                                disList.addAll(dianSameColorPai);
                                List<Integer> curList2 = new ArrayList<>(curList);
                                curList2.removeAll(dianSameColorPai);
                                curList=curList2;
                                int needbu = list.size()-dianSameColorPai.size();
                                disList.addAll(curList.subList(0,needbu));
                            }else{
                                disList.addAll(dianSameColorPai.subList(0, list.size()));
                            }
                        }else{
                            disList.addAll(cards.subList(0, list.size()));
                        }
                    } else {
                        disList.addAll(dui);
                        // 对子数小于别人的
                        if (dui.size() / 2 < needDuiCount) {
                            cards.removeAll(dui);
                            CardUtils.sortCards(cards);
                            disList.addAll(cards.subList(0, list.size() - dui.size()));
                        }
                    }
                }else if(type == CardType.SHUAIPAI){
                    //甩主
                    cards = CardUtils.getZhu(curList, zhuColor);
                    int shuaiColor =zhuColor;
                    //有对
                    int hasDuiNum = CardUtils.getDuiCards(list).size()/2;
                    List<Integer> dianpai = cards;
                    List<Integer>   dianpaiDuiList = new ArrayList<>();
                    if(hasDuiNum>0){
                        dianpaiDuiList = CardUtils.getDuiCards(dianpai);
                    }
                    int needFu = list.size();
                    if(dianpaiDuiList.size()/2>0 && hasDuiNum>0){
                        //  甩中含对 且自己 可垫对
                        if(dianpaiDuiList.size()<hasDuiNum*2){
                            //有对但是对子不够垫
                            needFu = list.size()-dianpaiDuiList.size();
                            disList.addAll(dianpaiDuiList);
                            dianpai.removeAll(dianpaiDuiList);
                        }else{
                            List<Integer> d1 = dianpaiDuiList.subList(0,hasDuiNum*2);
                            disList.addAll(d1);
                            dianpai.removeAll(d1);
                            needFu = needFu - hasDuiNum*2;
                        }
                    }

                    if(dianpai.size()>=needFu){
                        //所垫花色牌够
                        disList.addAll(dianpai.subList(0,needFu));
                    }else{
                        //所垫花色牌不够
                        disList.addAll(dianpai);
                        int neednum = needFu-dianpai.size();
                        disList.addAll(curList.subList(0,neednum));
                    }
                }
                else if(type == CardType.SHUAIPAI2){
                      // 甩牌 有可能没主 甩副//4321 黑红没房
                        int shuaiColor = CardUtils.loadCardColor(list.get(0));
                        //有对
                        int hasDuiNum = CardUtils.getDuiCards(list).size()/2;
                        List<Integer> dianpai = CardUtils.getColorCards(curList,shuaiColor);

                        List<Integer>   dianpaiDuiList = new ArrayList<>();
                        if(hasDuiNum>0){
                             dianpaiDuiList = CardUtils.getDuiCards(dianpai);
                        }
                        int needFu=list.size();
                        if(dianpaiDuiList.size()/2>0 && hasDuiNum>1){
                            //甩中含对 且自己 可垫对
                            List<Integer> d1 = dianpaiDuiList.subList(0,hasDuiNum*2);
                            disList.addAll(d1);
                            dianpai.removeAll(d1);
                            needFu= needFu-hasDuiNum*2;
                        }

                        if(dianpai.size()>=needFu){
                            //所垫花色牌够
                            disList.addAll(dianpai.subList(0,needFu));
                        }else{
                            //所垫花色牌不够
                            disList.addAll(dianpai);
                            curList.removeAll(dianpai);
                            int neednum = needFu-dianpai.size();
                            disList.addAll(curList.subList(0,neednum));
                        }
                }else if(type == CardType.SHUAI_LIAN_DUI) {
                    //甩对子。
                    int shuaiColor = CardUtils.loadCardColor(list.get(0));
                    if(isallzhu){
                        shuaiColor=zhuColor;
                    }
                    //有对
                    int hasDuiNum = list.size()/2;
                    List<Integer> dianpai = CardUtils.getDianCards3(curList,shuaiColor);

                    List<Integer>  dianpaiDuiList = new ArrayList<>();
                    if(hasDuiNum>0){
                        dianpaiDuiList = CardUtils.getDuiCards(dianpai);
                    }
                    //有对子垫对子。没对子垫其他
                    //甩对子数量》垫牌对子
                    if(list.size()-dianpaiDuiList.size()==0){
                        disList.addAll(dianpaiDuiList);//刚好能垫对
                    }else if(dianpaiDuiList.size()==0 && dianpai.size()<=list.size()){
                        //无能 垫对子
                        disList.addAll(dianpai.subList(0,list.size()));
                    }
                    else if(list.size()>dianpaiDuiList.size() && dianpaiDuiList.size()>0){
                        //还需 补的数量
                        int neednum =0;
                        int dsy = dianpai.size()-dianpaiDuiList.size();//垫牌集合剩余数量
                        boolean ndd =false;
                        if(dianpaiDuiList.size()>0){
                            //能垫对
                            neednum = list.size()-dianpaiDuiList.size() ;
                            ndd = true;
                        } else{
                            //不能垫对
                            neednum = list.size()-dianpai.size();
                        }
                        if(dsy>=neednum){
                            //同花色够补
                            disList.addAll(dianpaiDuiList);
                            dianpai.removeAll(dianpaiDuiList);
                            disList.addAll(dianpai.subList(0,neednum));
                        }else{
                            //dsy<neednum;
                            //同花色不够
                            List<Integer> copyDian = new ArrayList<>(dianpai);
                            List<Integer> copyDian2 = new ArrayList<>(dianpai);
                            List<Integer> copycurList= new ArrayList<>(curList);
                            disList.addAll(copyDian);
                            copycurList.removeAll(copyDian2);
                            if(ndd){
                                neednum=list.size()-dianpaiDuiList.size()-dsy;
                            }
                            disList.addAll(copycurList.subList(0, neednum));
                        }
                    }else{
                        // list.size()<dianpaiDuiList
                        disList.addAll(dianpaiDuiList.subList(0,list.size()));
                    }

                }

            }
            //System.err.println(list);
            //System.err.println("==>");
            //System.err.println(disList);
        }
        playCommand(player, 0, disList);
    }

    public static void main(String[] args) {

        int zhuColor =0;
        List<Integer> list =  Arrays.asList(501,501,502);
        List<Integer> curList =  Arrays.asList(115,115,215,315);
        List<Integer> disList =  new ArrayList<>();
        int color =0;
        boolean isallzhu =  CardUtils.isAllZhu(list,zhuColor);
        int type=5;
        List<Integer> cards = null;
        if (color == zhuColor) {
            cards = CardUtils.getZhu(curList, color);
        } else {
            cards = CardUtils.getColorCards(curList, color);
        }
        if(zhuColor==0 && isallzhu){
            //无主的时候张数不够 如果调的主 ;cards应该为所有硬主的集合
            cards = CardUtils.getDianCards2(curList,zhuColor);
        }
        // 没有这个花色
        if (cards == null || cards.isEmpty()) {
            CardUtils.sortCards(curList);
            int addC = list.size();
            disList.addAll(curList.subList(0, addC));
        } else if (cards.size() < list.size()) {
            disList.addAll(cards);
            curList.removeAll(cards);
            CardUtils.sortCards(curList);
            int addC = list.size() - cards.size();
            disList.addAll(curList.subList(0, addC));
        } else {

            if (type == CardType.DAN) {
                disList.add(cards.get(0));
            } else if (type == CardType.DUI || type == CardType.TUOLAJI) {
                int needDuiCount = list.size() / 2;
                List<Integer> dui = CardUtils.getDuiCards(cards, needDuiCount);
                if (dui.isEmpty()) {
                    //无对子
                    if(cards.size()>0){
                        //无对但至少有1个同花色
                        disList.addAll(cards);
                        List<Integer>  curList2 = new ArrayList<>(curList);
                        curList2.removeAll(cards);
                        curList=curList2;
                        int needBu =list.size()-cards.size();
                        disList.addAll(curList.subList(0, needBu));
                    }else{
                        disList.addAll(cards.subList(0, list.size()));
                    }
                } else {
                    disList.addAll(dui);
                    // 对子数小于别人的
                    if (dui.size() / 2 < needDuiCount) {
                        cards.removeAll(dui);
                        CardUtils.sortCards(cards);
                        disList.addAll(cards.subList(0, list.size() - dui.size()));
                    }
                }
            }else if(type == CardType.SHUAIPAI){
                //甩主
                cards = CardUtils.getZhu(curList, zhuColor);
                int shuaiColor =zhuColor;
                //有对
                int hasDuiNum = CardUtils.getDuiCards(list).size()/2;
                List<Integer> dianpai = cards;
                List<Integer>   dianpaiDuiList = new ArrayList<>();
                if(hasDuiNum>0){
                    dianpaiDuiList = CardUtils.getDuiCards(dianpai);
                }
                int needFu = list.size();
                if(dianpaiDuiList.size()/2>0 && hasDuiNum>0){
                    //  甩中含对 且自己 可垫对
                    if(dianpaiDuiList.size()<hasDuiNum*2){
                        //有对但是对子不够垫
                        needFu = list.size()-dianpaiDuiList.size();
                        disList.addAll(dianpaiDuiList);
                        dianpai.removeAll(dianpaiDuiList);
                    }else{
                        List<Integer> d1 = dianpaiDuiList.subList(0,hasDuiNum*2);
                        disList.addAll(d1);
                        dianpai.removeAll(d1);
                        needFu = needFu - hasDuiNum*2;
                    }
                }

                if(dianpai.size()>=needFu){
                    //所垫花色牌够
                    disList.addAll(dianpai.subList(0,needFu));
                }else{
                    //所垫花色牌不够
                    disList.addAll(dianpai);
                    int neednum = needFu-dianpai.size();
                    disList.addAll(curList.subList(0,neednum));
                }
            }
            else if(type == CardType.SHUAIPAI2){
                // 甩牌 有可能没主 甩副//4321 黑红没房
                int shuaiColor = CardUtils.loadCardColor(list.get(0));
                //有对
                int hasDuiNum = CardUtils.getDuiCards(list).size()/2;
                List<Integer> dianpai = CardUtils.getColorCards(curList,shuaiColor);

                List<Integer>   dianpaiDuiList = new ArrayList<>();
                if(hasDuiNum>0){
                    dianpaiDuiList = CardUtils.getDuiCards(dianpai);
                }
                int needFu=list.size();
                if(dianpaiDuiList.size()/2>0 && hasDuiNum>1){
                    //甩中含对 且自己 可垫对
                    List<Integer> d1 = dianpaiDuiList.subList(0,hasDuiNum*2);
                    disList.addAll(d1);
                    dianpai.removeAll(d1);
                    needFu= needFu-hasDuiNum*2;
                }

                if(dianpai.size()>=needFu){
                    //所垫花色牌够
                    disList.addAll(dianpai.subList(0,needFu));
                }else{
                    //所垫花色牌不够
                    disList.addAll(dianpai);
                    curList.removeAll(dianpai);
                    int neednum = needFu-dianpai.size();
                    disList.addAll(curList.subList(0,neednum));
                }
            }
            else if(type == CardType.SHUAI_LIAN_DUI) {
                //甩对子。
                int shuaiColor = CardUtils.loadCardColor(list.get(0));
                if(isallzhu){
                    shuaiColor=zhuColor;
                }
                //有对
                int hasDuiNum = list.size()/2;
                List<Integer> dianpai = CardUtils.getDianCards3(curList,shuaiColor);

                List<Integer>  dianpaiDuiList = new ArrayList<>();
                if(hasDuiNum>0){
                    dianpaiDuiList = CardUtils.getDuiCards(dianpai);
                }
                //有对子垫对子。没对子垫其他
                //甩对子数量》垫牌对子
                if(list.size()-dianpaiDuiList.size()==0){
                    disList.addAll(dianpaiDuiList);
                }else if(list.size()>dianpaiDuiList.size()){
                    //还需 补的数量
                    int neednum =0;
                    int dsy = dianpai.size()-dianpaiDuiList.size();//垫牌集合剩余数量
                    boolean ndd =false;
                    if(dianpaiDuiList.size()>0){
                        //能垫对
                        neednum = list.size()-dianpaiDuiList.size() ;
                        ndd = true;
                    } else{
                        //不能垫对
                        neednum = list.size()-dianpai.size();
                    }
                    if(dsy>=neednum){
                        //同花色够补
                        disList.addAll(dianpaiDuiList);
                        dianpai.removeAll(dianpaiDuiList);
                        disList.addAll(dianpai.subList(0,neednum));
                    }else{
                        //dsy<neednum;
                        //同花色不够
                        List<Integer> copyDian = new ArrayList<>(dianpai);
                        List<Integer> copyDian2 = new ArrayList<>(dianpai);
                        List<Integer> copycurList= new ArrayList<>(curList);
                        disList.addAll(copyDian);
                        copycurList.removeAll(copyDian2);
                        if(ndd){
                            neednum=list.size()-dianpaiDuiList.size()-dsy;
                        }
                        disList.addAll(copycurList.subList(0, neednum));
                    }
                }else{
                    // list.size()<dianpaiDuiList
                    disList.addAll(dianpaiDuiList.subList(0,list.size()));
                }

            }

        }
        System.out.println(list);
        System.out.println("==>");
        System.out.println(disList);
    }
    public static void main2(String[] args) {
        {
            List<Integer> list = Arrays.asList(502, 502, 501, 501, 415);
            List<Integer> curList = Arrays.asList(215, 115, 315, 414, 114, 313, 113, 213, 112, 312, 412, 412, 311, 211, 110, 110);
            List<Integer> disList =  new ArrayList<>();
            //甩对子。
            int shuaiColor = CardUtils.loadCardColor(list.get(0));
            //有对
            int hasDuiNum = list.size()/2;
            List<Integer> dianpai = CardUtils.getColorCards(curList,shuaiColor);

            List<Integer>  dianpaiDuiList = new ArrayList<>();
            if(hasDuiNum>0){
                dianpaiDuiList = CardUtils.getDuiCards(dianpai);
            }
            //有对子垫对子。没对子垫其他
            //甩对子数量》垫牌对子
            if(list.size()-dianpaiDuiList.size()==0){
                disList.addAll(dianpaiDuiList);
            }else if(list.size()>dianpaiDuiList.size()){
                //还需 补的数量
                int neednum = list.size()-dianpaiDuiList.size();
                int  dsy =dianpai.size()-dianpaiDuiList.size();
                if(dsy>=neednum){
                    //同花色够补
                    disList.addAll(dianpaiDuiList);
                    dianpai.removeAll(dianpaiDuiList);
                    disList.addAll(dianpai.subList(0,neednum));
                }else{
                    //dsy<neednum;
                    //同花色不够
                    List<Integer> copyDian = new ArrayList<>(dianpai);
                    List<Integer> copyDian2 = new ArrayList<>(dianpai);
                    List<Integer> copycurList= new ArrayList<>(curList);
                    disList.addAll(copyDian);
                    copycurList.removeAll(copyDian2);
                    neednum = curList.size()-dianpai.size();
                    disList.addAll(copycurList.subList(0, neednum));
                }
            }else{
                    // list.size()<dianpaiDuiList
                disList.addAll(dianpaiDuiList.subList(0,list.size()));
            }
            System.out.println(list);
            System.out.println("==>");
            System.out.println(disList);
        }
    }
    public boolean checkPlayerAuto(DddzPlayer player, int timeout) {
        if (player.isAutoPlay()) {
            return true;
        }
        long now = TimeUtil.currentTimeMillis();
        boolean auto = false;
        if (player.isAutoPlayChecked() || (player.getAutoPlayCheckedTime() >= timeout && !player.isAutoPlayCheckedTimeAdded())) {
            player.setAutoPlayChecked(true);
            timeout = autoTimeOut2;
        }
        if (player.getLastCheckTime() > 0) {
            int checkedTime = (int) (now - player.getLastCheckTime());
            if (checkedTime >= timeout) {
                auto = true;
            }
            if (auto) {
                player.setAutoPlay(true, this);
            }
        } else {
            player.setLastCheckTime(now);
            player.setAutoPlayCheckedTimeAdded(false);
        }

        return auto;
    }

    private String cardMarkerToJSON() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Integer, DddzPlayer> entry : seatMap.entrySet()) {
            jsonObject.put("" + entry.getKey(), entry.getValue().getOutPais());
        }
        return jsonObject.toString();
    }


    public int getShowCardNumber() {
        return showCardNumber;
    }

    public void setShowCardNumber(int showCardNumber) {
        this.showCardNumber = showCardNumber;
        changeExtend();
    }


    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
        createTable(player, play, bureauCount, params);
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {

    }

    @Override
    public boolean isCreditTable(List<Integer> params) {
        return params != null && params.size() > 13 && StringUtil.getIntValue(params, 13, 0) == 1;
    }

    public String getGameName() {
        return "大斗地主";
    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_pk_dddz);


    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
    }

    @Override
    public boolean allowRobotJoin() {
        return StringUtils.contains(ResourcesConfigsUtil.loadServerPropertyValue("robot_modes", ""), new StringBuilder().append("|").append(modeId).append("|").toString());
    }

    public void setTableStatus(int tableStatus) {
        this.tableStatus = tableStatus;
        changeExtend();
    }

    public int getTableStatus() {
        return tableStatus;
    }


    @Override
    public boolean isAllReady() {
        return isAllReady1();
//        }else {
//            return super.isAllReady();
//        }
    }

    public boolean isAllReady1() {
        if (super.isAllReady()) {
            if (playBureau != 1) {
                return true;
            }
            // 只有第一局需要推送打鸟消息
            if (jiaoFen > 0) {
                boolean isAllDaNiao = true;
                if (this.isTest()) {
                    // 机器人默认处理
//                    for (DddzPlayer robotPlayer : seatMap.values()) {
//                    }
                }
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pdk_daniao, 1);
                for (DddzPlayer player : seatMap.values()) {
                }
                if (!isAllDaNiao) {
                    broadMsgRoomPlayer(com.build());
                }
                return isAllDaNiao;
            } else {
                for (DddzPlayer player : seatMap.values()) {
                }
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean canQuit(Player player) {
        if (state == table_state.play || playedBureau > 0 || isMatchRoom() || isGoldRoom()) {
            return false;
        } else if (state == table_state.ready) {
            return true;
        } else {
            return true;
        }
    }


    public boolean isNewRound() {
        return newRound;
    }

    public void setNewRound(boolean newRound) {
        this.newRound = newRound;
        changeExtend();
    }


    public int getBanker() {
        return banker;
    }

    @Override
    public int getDissPlayerAgreeCount() {
        return getPlayerCount();
    }

    public long getTouxiangTime() {
        return touxiangTime;
    }

    public void setTouxiangTime(long touxiangTime) {
        this.touxiangTime = touxiangTime;
    }

    public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "新田包牌");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (autoPlay) {
            json.put("autoTime", autoTimeOut2 / 1000);
            if (autoPlayGlob == 1) {
                json.put("autoName", "单局");
            } else {
                json.put("autoName", "整局");
            }
        }
        return JSON.toJSONString(json);
    }


    public void setDipai(List<Integer> dipai) {
        this.dipai = dipai;
        dbParamMap.put("handPai9", JSON_TAG);
    }

    public int getTurnFirstSeat() {
        return turnFirstSeat;
    }

    public void setTurnFirstSeat(int turnFirstSeat) {
        this.turnFirstSeat = turnFirstSeat;
    }

    public int getTurnNum() {
        return turnNum;
    }

    public void setJiaoFen(int jiaoFen) {
        this.jiaoFen = jiaoFen;
        changeExtend();
    }

    public void setTurnNum(int turnNum) {
        this.turnNum = turnNum;
    }

    public void addTurnNum(int turnNum) {
        this.turnNum += turnNum;
        changeExtend();
    }


}
