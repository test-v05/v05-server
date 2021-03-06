package com.sy599.game.qipai.sandh.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sy599.game.common.bean.CreateTableInfo;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;

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
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.sandh.constant.SandhConstants;
import com.sy599.game.qipai.sandh.tool.CardTool;
import com.sy599.game.qipai.sandh.util.CardType;
import com.sy599.game.qipai.sandh.util.CardUtils;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class SandhTable extends BaseTable {
    public static final String GAME_CODE = "sandh";
    private static final int JSON_TAG = 1;
	/*** ???????????????????????? */
    private volatile List<Integer> nowDisCardIds = new ArrayList<>();
	/*** ??????map */
    private Map<Long, SandhPlayer> playerMap = new ConcurrentHashMap<Long, SandhPlayer>();
	/*** ????????????????????? */
    private Map<Integer, SandhPlayer> seatMap = new ConcurrentHashMap<Integer, SandhPlayer>();
	/*** ?????????????????? */
	private volatile int maxPlayerCount = 3;

	private volatile int showCardNumber = 0; // ???????????????????????????
	
	public static final int FAPAI_PLAYER_COUNT = 3;// ????????????

    private volatile int timeNum = 0;

	/** ???????????? */
	private int shuangjinDC;
	/** ???????????? */
	private int baofuLS;
	/** ???????????? */
	private int checkPai;
	/** ???6 */
	private int chouLiu;
	/** ???????????? */
	private int touxiangXW;
	/** 60????????? */
	private int sixtyQJ;
	/** ????????? */
	private int xiaoguangF;
	
	/** ?????????????????? */
	private int daDaoTQ;
	
	/** ???????????? */
	private int jiaofenJP;
	/** ???????????? */
	private int jiaofenJD;
	
	/** ??????1????????????2????????? */
    private int autoPlayGlob;
    private int autoTableCount;

    private int xianBaoFu;

    private int quGui;
	// ???????????????3??????2???pass???????????????????????????
    private boolean newRound = true;
	// pass??????
    /**
	 * ????????????
	 */
    private volatile int autoTimeOut = 5 * 24 * 60 * 60 * 1000;
    private volatile int autoTimeOut2 = 5 * 24 * 60 * 60 * 1000;

	// ??????????????????
    private int finishFapai=0;

	// ???????????????????????????????????????
    private volatile List<PlayerCard> noPassDisCard = new ArrayList<>();
	// ????????????
    private volatile String replayDisCard = "";


	private  List<Integer> dipai = new ArrayList<>();// ??????
	private  List<Integer> zhuoFen = new ArrayList<>();// ?????????
	private List<Integer> turnWinSeats = new ArrayList<>();
	/** ????????? **/
	private int jiaoFen;
	private boolean isPai = false;// ????????????
	private int zhuColor = -1;// 0:?????? ?????? 1 ??????2 ??????3 ??????4 
	private int banker = 0;// ????????????
	
	private int turnFirstSeat = 0;// ?????????????????????????????????
	private int disColor; //????????????????????? ?????? 1 ??????2 ??????3 ??????4  5??????
	private int turnNum;//????????????????????????1??????
    

	/** ???????????? 1 **/
    private int tableStatus = 0;
	// ??????below??????
    private int belowAdd=0;
    private int below=0;
    
    
    private int daDaoFD=0;
    
    //????????????
    private int cSJiaoFen=0;
    //????????????
    private int touXJind=0;
    
    
    private  HashSet<Integer> jiaoFenSs = new HashSet<>();// ???????????????
    private int noJiaPai=0;
    
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

    public SandhPlayer getPlayer(long id) {
        return playerMap.get(id);
    }


	/**
	 * ????????????
	 */
	public void calcOver() {
		if(state==table_state.ready){	
			return;
		}
		int Adddifen = 0;
		boolean koudi = false;
		int score = 0;
		boolean toux = isTouXiang();
		if(toux){
			 calTouxiFen();
		}else if(!autoPlayDiss){
			if (turnFirstSeat != banker) {
				// ?????????????????????????????????
				Adddifen = checkKoudi();
				if (Adddifen == -1) {
					Adddifen = 0;
				}else {
					koudi = true;
				}
			}
			score = CardUtils.loadCardScore(zhuoFen);
			score += Adddifen;
			commonOver(score);
		}


		boolean isOver = playBureau >= totalBureau;
		if (autoPlayGlob > 0) {
			// //????????????
			boolean diss = false;
			if (autoPlayGlob == 1) {
				for (SandhPlayer seat : seatMap.values()) {
					if (seat.isAutoPlay()) {
						diss = true;
						break;
					}
				}
			} else if (autoPlayGlob == 3) {
				diss = checkAuto3();
			}
			
			if (diss||autoPlayDiss) {
				autoPlayDiss = true;
				isOver = true;
			}
		}

        if(isOver){
            calcPointBeforeOver();
        }

        // -----------solo------------------
        if (isSoloRoom()) {
            for (SandhPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0) {
                    player.setSoloWinner(true);
                } else {
                    player.setSoloWinner(false);
                }
            }
            calcSoloRoom();
        }


		calcAfter();
		ClosingInfoRes.Builder res = sendAccountsMsg(isOver, false, score, koudi, Adddifen,toux);
		 saveLog(isOver, 0, res.build());
		
		if (isOver) {
			calcOver1();
			calcOver2();
			calcOver3();
			diss();
//			for (Player player : seatMap.values()) {
//				player.saveBaseInfo();
//			}
		} else {
			initNext();
			calcOver1();
		}

	}

    public void calcPointBeforeOver() {
        // ???????????????below???+belowAdd???
        if (belowAdd > 0 && playerMap.size() == 2) {
            for (SandhPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint > -below && totalPoint < 0) {
                    player.setTotalPoint(player.getTotalPoint() - belowAdd);
                } else if (totalPoint < below && totalPoint > 0) {
                    player.setTotalPoint(player.getTotalPoint() + belowAdd);
                }
            }
        }
    }

    private void calTouxiFen() {
		SandhPlayer losePlayer = seatMap.get(banker);
		int winScore = 1;
		if (isPai) {
			winScore *= 2;
		}
		
		if (touXJind==1 ||jiaofenJD == 1) {
			winScore *= CardTool.getDang(jiaoFen);
		}
		for (SandhPlayer player : seatMap.values()) {
			if (player.getSeat() == banker) {
				continue;
			}
			// ????????????
			
			player.calcWin(1, winScore);
		}
		losePlayer.calcLost(1,-winScore*(maxPlayerCount - 1));
	}

	private boolean isTouXiang(){
		List<Integer> agreeTX = new ArrayList<Integer>();
		for (SandhPlayer player : seatMap.values()) {
         	if(player.getTouXiang()==2||player.getTouXiang()==1){
         		agreeTX.add(player.getTouXiang());
         	}
         }
		if (agreeTX.size() == maxPlayerCount) {
			return true;
		}else if(touxiangXW!=1 &&agreeTX.contains(1)){
			return true;
		}
		return false;
	}

	private void commonOver(int score) {
		boolean isWin = true;
		if (jiaoFen <= score) {
			isWin = false;
		}
		if (isWin) {
			int winScore = 1;
			if (score == 0) {
				winScore = 3;
			} else if (score < 30) {
				if (xiaoguangF == 1) {
					if (score < 25) {
						winScore = 2;
					}
				} else {
					winScore = 2;
				}
			}
			// ????????????
			if (isPai) {
				winScore *= 2;
			}
			if (jiaofenJD == 1) {
				winScore *= CardTool.getDang(jiaoFen);
			}

			if (shuangjinDC == 1) {
				winScore *= 2;
			}

			SandhPlayer winPlayer = seatMap.get(banker);
			for (SandhPlayer player : seatMap.values()) {
				if (player.getSeat() == banker) {
					continue;
				}
				player.calcLost(1, -winScore);
			}

			winPlayer.calcWin(1, winScore * (maxPlayerCount - 1));
			 setLastWinSeat(winPlayer.getSeat());
		} else {
			int loseScore = 1;
			if (score - jiaoFen >= 70) {
				int addscore = (score - jiaoFen - 70) / 10;
				if(daDaoFD==1){
					loseScore = 3;
				}else{
					loseScore = 3 + addscore;
				}
			} else if (score - jiaoFen >= 40) {
				loseScore = 2;
			}

			// ????????????
			if (isPai) {
				loseScore *= 2;
			}
			if (jiaofenJD == 1) {
				loseScore *= CardTool.getDang(jiaoFen);
			}
			SandhPlayer losePlayer = seatMap.get(banker);
			for (SandhPlayer player : seatMap.values()) {
				if (player.getSeat() == banker) {
					continue;
				}
				player.calcWin(1, loseScore);
			}
			losePlayer.calcLost(1, -loseScore * (maxPlayerCount - 1));
			 setLastWinSeat(calcNextSeat(losePlayer.getSeat()));
		}
	}


	private int checkKoudi() {
		int Adddifen;
		SandhPlayer lastPlayer = seatMap.get(turnFirstSeat);
		if(lastPlayer== null|| lastPlayer.getHandPais().size()>0){
			return -1;
		}
		int addBei = 1;
		List<Integer> cards = lastPlayer.getCurOutCard(getTurnNum() - 1);
		if(!CardTool.allZhu(cards, zhuColor)){
			return -1;
		}
		if (cards != null) {
			CardType ct = CardTool.getCardType(cards, zhuColor,isChouLiu());
			if (ct.getType() == CardType.DUI) {
				addBei = 2;
			} else if (ct.getType() == CardType.TUOLAJI) {
				addBei = 4;
			}
		}
		Adddifen = CardUtils.loadCardScore(dipai) * addBei;
		// ??????
		PlayCardRes.Builder res = PlayCardRes.newBuilder();
		res.setUserId(lastPlayer.getUserId() + "");
		res.setSeat(turnFirstSeat);
		res.setIsPlay(2);
		res.setCardType(SandhConstants.RES_KOUDI);
		res.addAllCardIds(dipai);
		List<Integer> scoreCards = CardUtils.getScoreCards(dipai);
		if (scoreCards.size() > 0) {
			// zhuoFen.addAll(scoreCards);
			res.addAllScoreCard(scoreCards);
		}
		int tfen = CardUtils.loadCardScore(zhuoFen)+Adddifen;
		addPlayLog(addSandhPlayLog(0, SandhConstants.TABLE_KOUDI, dipai,false,tfen,scoreCards,getNowDisCardSeat()));
		res.setCurScore(tfen);
		for (Player player : seatMap.values()) {
			player.writeSocket(res.build());
		}
		return Adddifen;
	}
    
	private boolean checkAuto3() {
		boolean diss = false;
//		if(autoPlayGlob==3) {
			boolean diss2 = false;
			 for (SandhPlayer seat : seatMap.values()) {
		      	if(seat.isAutoPlay()) {
		      		diss2 = true;
		          	break;
		          }
		      }
			 if(diss2) {
				 autoTableCount +=1;
			 }else{
				 autoTableCount = 0;
			 }
			if(autoTableCount==3) {
				diss = true;
			}
//		}
		return diss;
	}

    @Override
    public void calcDataStatistics2() {
		// ??????????????? ???????????????????????????????????????????????????????????????????????????????????????????????? ????????????
        if (isGroupRoom()) {
            String groupId = loadGroupId();
            int maxPoint = 0;
            int minPoint = 0;
            Long dataDate = Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			// ???????????????????????????
            calcDataStatistics3(groupId);

            //Long dataDate, String dataCode, String userId, String gameType, String dataType, int dataValue
            for (SandhPlayer player : playerMap.values()) {
				// ????????????
                DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
				// ????????????
                DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
				// ?????????
                DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", player.loadScore());
                DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);
                if (player.loadScore() > 0) {
                    if (player.loadScore() > maxPoint) {
                        maxPoint = player.loadScore();
                    }
					// ??????????????????
                    DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore", player.loadScore());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
                } else if (player.loadScore() < 0) {
                    if (player.loadScore() < minPoint) {
                        minPoint = player.loadScore();
                    }
					// ??????????????????
                    DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore", player.loadScore());
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
                }
            }

            for (SandhPlayer player : playerMap.values()) {
                if (maxPoint > 0 && maxPoint == player.loadScore()) {
					// ??????????????????
                    DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId, String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
                    DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
                } else if (minPoint < 0 && minPoint == player.loadScore()) {
					// ??????????????????
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
        Map<String, Object> map =  LogUtil.buildClosingInfoResOtherLog(res);
       map.put("intParams", getIntParams());
        String logOtherRes = JacksonUtil.writeValueAsString(map);
        Date now = TimeUtil.now();

        UserPlaylog userLog = new UserPlaylog();
        userLog.setUserId(creatorId);
        userLog.setLogId(playType);
        userLog.setTableId(id);
		userLog.setRes(extendLogDeal(logRes));
        userLog.setTime(now);
        userLog.setTotalCount(totalBureau);
        userLog.setCount(playBureau);
        userLog.setStartseat(lastWinSeat);
        userLog.setOutCards(playLog);
        userLog.setExtend(logOtherRes);
		userLog.setMaxPlayerCount(maxPlayerCount);
        userLog.setType(creditMode == 1 ? 2 : 1 );
        userLog.setGeneralExt(buildGeneralExtForPlaylog().toString());
        long logId = TableLogDao.getInstance().save(userLog);
        saveTableRecord(logId, over, playBureau);

        if (!isGoldRoom()) {
            for (SandhPlayer player : playerMap.values()) {
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


        wrapper.putString(5,replayDisCard);
        wrapper.putInt(6,autoTimeOut);
        wrapper.putInt(7,autoPlayGlob);
        wrapper.putInt(8,jiaoFen);
        wrapper.putInt(9, newRound ? 1 : 0);
        wrapper.putInt(10, finishFapai);
        wrapper.putInt(11, belowAdd);
        wrapper.putInt(12, below);
        
        
        wrapper.putInt(13,isPai?1:0);
        wrapper.putInt(14,zhuColor);
        wrapper.putInt(15,banker);
        wrapper.putInt(16,turnFirstSeat);
        wrapper.putInt(17,disColor);
        wrapper.putInt(18, turnNum);
        wrapper.putInt(19, tableStatus);
        wrapper.putInt(20, touxiangXW);
        
        wrapper.putInt(21, shuangjinDC);
        wrapper.putInt(22, baofuLS);
        wrapper.putInt(23, checkPai);
        wrapper.putInt(24, chouLiu);
        wrapper.putInt(25, sixtyQJ);
        wrapper.putInt(26, xiaoguangF);
        wrapper.putInt(27, daDaoTQ);
        
        wrapper.putInt(28, jiaofenJP);
        wrapper.putInt(29, jiaofenJD);
        wrapper.putLong(30, touxiangTime);
        wrapper.putInt(31, daDaoFD);
        wrapper.putInt(32, cSJiaoFen);
        wrapper.putInt(33, touXJind);
        wrapper.putInt(34, xianBaoFu);
        wrapper.putInt(35, quGui);
        
        return wrapper;
    }



    public void changePlayers() {
        dbParamMap.put("players", JSON_TAG);
    }

    public void changeCards(int seat) {
        dbParamMap.put("outPai" + seat, JSON_TAG);
        dbParamMap.put("handPai" + seat, JSON_TAG);
    }

    /**
	 * ????????????
	 */
    public void fapai() {
    	playLog = "";
        synchronized (this) {
            changeTableState(table_state.play);
            timeNum = 0;
            List<List<Integer>> list;
          
			list = CardTool.fapai(maxPlayerCount, isChouLiu(), zp,quGui>0);
            
            int i = 0;
//            for (int j=1;i<maxPlayerCount;j++) {
//            	SandhPlayer player  =seatMap.get(j);
            for (SandhPlayer player : playerMap.values()) {
            //	: playerMap.values()
                player.changeState(player_state.play);
                player.dealHandPais(list.get(i), this);
                i++;

                if (!player.isAutoPlay()) {
                    player.setAutoPlay(false, this);
                    player.setLastOperateTime(System.currentTimeMillis());
                }

                StringBuilder sb = new StringBuilder("Sandh");
                sb.append("|").append(getId());
                sb.append("|").append(getPlayBureau());
                sb.append("|").append(player.getUserId());
                sb.append("|").append(player.getSeat());
                sb.append("|").append(player.getName());
                sb.append("|").append(player.isAutoPlay() ? 1 : 0);
                sb.append("|").append("fapai");
                sb.append("|").append(player.getHandPais());
                LogUtil.msgLog.info(sb.toString());
            }
            
            if (maxPlayerCount == 2){
				setDipai(list.get(4));
			}else{
				setDipai(list.get(i));
			}
        }
        finishFapai=1;
    }




    @Override
    public int getNextDisCardSeat() {
        if (disCardSeat == 0) {
            return banker;
        }
        return calcNextSeat(disCardSeat);
    }

    /**
	 * ??????seat???????????????
	 *
	 * @param seat
	 * @return
	 */
    public int calcNextSeat(int seat) {
        int nextSeat = seat + 1 > maxPlayerCount ? 1 : seat + 1;
        return nextSeat;
    }

    public SandhPlayer getPlayerBySeat(int seat) {
		//int next = seat >= maxPlayerCount ? 1 : seat + 1;
        return seatMap.get(seat);

    }
    private void addGameActionLog(Player player,String str){
    	
        StringBuilder sb = new StringBuilder("Sandh");
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
            for (SandhPlayer player : playerMap.values()) {
                PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
                if (playerRes == null) {
                    continue;
                }
                if (player.getUserId() == userId) {
					// ????????????????????????????????????
                    playerRes.addAllHandCardIds(player.getHandPais());
                } else {
					// ?????????????????????????????????????????????????????????????????????
                }
                if(player.getSeat()==banker&& getTableStatus()==SandhConstants.TABLE_STATUS_PLAY) {
                	playerRes.addAllMoldIds(dipai);
                }

                if (player.getSeat() == disCardSeat && nowDisCardIds != null && nowDisCardIds.size()>0) {
                  //  playerRes.addAllOutCardIds(nowDisCardIds);
//                    playerRes.addRecover(cardType);
                }
                players.add(playerRes.build());
            }
            res.addAllPlayers(players);
            //int nextSeat = getNextDisCardSeat();
            
//            if(getTableStatus() == SandhConstants.TABLE_STATUS_JIAOFEN) {
//            	nextSeat = getNextActionSeat();
//            }
            if (nowDisCardSeat != 0) {
                res.setNextSeat(nowDisCardSeat);
            }
            
           //????????? 1??????2??????3??????
            res.setRemain(getTableStatus());
            res.addAllScoreCard(zhuoFen);
			res.setRenshu(this.maxPlayerCount);
           
            res.addExt(this.payType);//0????????????
            res.addExt(jiaoFen);//1???????????????
            res.addExt(zhuColor);//2??????????????????
            res.addExt(banker);//3???????????????
           //????????????
            if(noJiaPai==1){
            	res.addExt(2);
            }else{
            	res.addExt(isPai?1:0);//????????????1?????????
            }
            
            
            res.addExt(CardUtils.loadCardScore(zhuoFen));//??????
            
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
            res.addTimeOut( autoPlay ? autoTimeOut : 0);
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
        for (SandhPlayer player : seatMap.values()) {
            if (player.getIsLeave() == 0) {
                num++;
            }
        }
        return num;
    }


    /**
	 * ??????
	 *
	 * @param player
	 * @param cards
	 */
	public void disCards(SandhPlayer player, List<Integer> cards) {
		setDisCardSeat(player.getSeat());

		if (turnFirstSeat == player.getSeat()) {
			int res = CardTool.checkCardValue(player.getHandPais(), cards, zhuColor, disColor, true,isChouLiu());
			if (res < 0) {
				player.writeErrMsg("?????????????????????0???");
				return;
			} else {
				int type = res % 10;
				if (type >= CardType.SHUAIPAI) {
					for (SandhPlayer p : seatMap.values()) {
						if (turnFirstSeat == p.getSeat()) {
							continue;
						}
						if (p.getBaofu() == 0) {
							player.writeErrMsg("??????????????????????????????????????????");
							return;
						}
					}
				}
				disColor = res;
			}
		} else {
			SandhPlayer bankP = seatMap.get(turnFirstSeat);
			List<Integer> list = bankP.getCurOutCard(getTurnNum());
			if (list.size() != cards.size()) {
				addGameActionLog(player, cards+"|"+zhuColor+"|"+disColor+"|"+list);
				player.writeErrMsg("?????????????????????1???");
				return;
			}
			int res = CardTool.checkCardValue(player.getHandPais(), cards, zhuColor, disColor, false,isChouLiu());
			if (res < 0) {
				addGameActionLog(player, cards+"|"+zhuColor+"|"+disColor+"|"+list);
				player.writeErrMsg("?????????????????????2???");
				return;
			}
		}
		player.addOutPais(cards, this);
		setDisCardSeat(player.getSeat());
		int nextSeat = getNextDisCardSeat();

		// ??????????????????
		PlayCardRes.Builder res = PlayCardRes.newBuilder();
		res.setIsClearDesk(0);
		res.setCardType(0);
		boolean isOver = false;
		
		if(xianBaoFu==1&&player.getBaofu()==0){
			boolean  oZhu = CardTool.allFuPai(player.getHandPais(), zhuColor);
			if(oZhu){//??????
				player.setBaofu(1);
				int baofu =CardTool.getBaofuValue(player.getSeat());
				 res.setIsLet(baofu);
			}
		}else{
			if (turnFirstSeat != player.getSeat()) {
				 List<Integer> firstList =seatMap.get(turnFirstSeat).getCurOutCard(getTurnNum());
					boolean firstZhu = CardTool.allZhu(firstList, zhuColor);
					 int baofu = 0;
					 if(firstZhu){
						 baofu += checkBaofu(firstZhu, player, cards);
					 }
					 res.setIsLet(baofu);
			}	
		}
	
		if (nextSeat == turnFirstSeat) {// ????????????
			// 1.??????????????????
			// 2.??????????????????
			// 3.??????????????????
			isOver = turnOver(res,player,cards);
		} else {
			setNowDisCardSeat(getNextDisCardSeat());
			addPlayLog(addSandhPlayLog(player.getSeat(), SandhConstants.TABLE_STATUS_PLAY, cards,player.getBaofu()==1?true:false,0,null,getNextDisCardSeat()));
		}
		
		
		setNowDisCardIds(cards);
		if (cards != null) {
			noPassDisCard.add(new PlayerCard(player.getName(), cards));
		}
		res.addAllCardIds(getNowDisCardIds());
		res.setNextSeat(getNowDisCardSeat());
		res.setUserId(player.getUserId() + "");
		res.setSeat(player.getSeat());
		res.setIsPlay(2);
		setReplayDisCard();
		for (SandhPlayer p : seatMap.values()) {
			p.writeSocket(res.build());
		}

		if (isOver) {
			state = table_state.over;
		}

	}

    
	private boolean turnOver(PlayCardRes.Builder res,SandhPlayer player,List<Integer> cards) {
		boolean isOver = true;
		HashMap<Integer,CardType> pmap  = new HashMap<Integer,CardType>();

		for (SandhPlayer p : seatMap.values()) {
			 List<Integer> list = p.getCurOutCard(getTurnNum());
			pmap.put(p.getSeat(), CardTool.getCardType(list,zhuColor,isChouLiu()));
			if(p.getHandPais().size()!=0){
				isOver = false;
			}
		}
		CardType	result = CardTool.getTunWin(pmap, turnFirstSeat, zhuColor);
		List<Integer> fenCards = null;
		if(result.getCardIds().size()>0&&result.getType()!=banker) {
			zhuoFen.addAll(result.getCardIds());
			res.addAllScoreCard(result.getCardIds());
			fenCards = result.getCardIds();
		}
		int totalScore = CardUtils.loadCardScore(zhuoFen);
		res.setCurScore(totalScore);
		res.setIsClearDesk(1);	
		
		addPlayLog(addSandhPlayLog(player.getSeat(), SandhConstants.TABLE_STATUS_PLAY, cards,player.getBaofu()==1?true:false,totalScore,fenCards,result.getType()));
		
		setNowDisCardSeat(result.getType());
		setTurnFirstSeat(result.getType());
		turnWinSeats.add(result.getType());
		addTurnNum(1);
		disColor =0;
		//??????????????????
		if (totalScore - jiaoFen >= 70&&daDaoTQ==1) {
			isOver = true;
		}
		
		return isOver;
	}
	
	
	
	private CardType sortTurn(){
		HashMap<Integer,CardType> pmap  = new HashMap<Integer,CardType>();
		for (SandhPlayer p : seatMap.values()) {
			 List<Integer> list = p.getCurOutCard(getTurnNum());
			 if(list==null){
				 continue;
			 }
			pmap.put(p.getSeat(), CardTool.getCardType(list,zhuColor,isChouLiu()));
		}
		CardType	result = CardTool.getTunWin(pmap, turnFirstSeat, zhuColor);
		return result;
	}
	
	

	private int checkBaofu(boolean firstZhu, SandhPlayer p, List<Integer> list) {
		if(p.getBaofu()==1){
			return CardTool.getBaofuValue(p.getSeat());
		}
		int baofu =0;
		if(firstZhu&&p.getSeat()!=turnFirstSeat){
			boolean  oZhu = CardTool.allZhu(list, zhuColor);
			if(!oZhu){//??????
				p.setBaofu(1);
				baofu +=CardTool.getBaofuValue(p.getSeat());
			}
		}
		return baofu;
	}
    
    
    
    

    public void setReplayDisCard(){
        List<PlayerCard> cards = new ArrayList<>();
        int size = noPassDisCard.size();
        for (int i = 0; i < 3&&i<size; i++) {
            cards.add(noPassDisCard.get(size-1-i));
        }
        setReplayDisCard(cards.toString());
        noPassDisCard.clear();
    }
    
    
    

    /**
	 * ??????
	 *
	 * @param player
	 * @param cards
	 */
    public void playCommand(SandhPlayer player,int action, List<Integer> cards) {
        synchronized (this) {
            if (state != table_state.play) {
                return;
            }
            
            if(action ==SandhConstants.REQ_MAIPAI) {
            	playMaipai(player, cards);
            	return ;
            }
            
            //????????????
            if(getTableStatus()!=SandhConstants.TABLE_STATUS_PLAY){
            	return;
            }
            
            if(!containCards(player.getHandPais(), cards)){
            	return;
            }

            StringBuilder sb = new StringBuilder("Sandh");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append("chuPai");
            sb.append("|").append(cards);
            LogUtil.msgLog.info(sb.toString());
            if (cards != null && cards.size() > 0) {
                changeDisCardRound(1);
				// ?????????
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
                SandhPlayer nextPlayer = seatMap.get(nextSeat);
                if (!nextPlayer.isRobot()) {
                    nextPlayer.setNextAutoDisCardTime(TimeUtil.currentTimeMillis() + autoTimeOut);
                }
            }
        }
    }
    
    private boolean containCards(List<Integer> handCards,List<Integer> cards){
    	for(Integer id: cards){
    		if(!handCards.contains(id)){
    			return false;
    		}
    	}
    	return true;
    	
    }

    /**
     * 
     * @param player
     * @param cards
     */
	private void playMaipai(SandhPlayer player, List<Integer> cards) {
		if ((!isChouLiu()&&cards.size() != 8)|| (getMaxPlayerCount() == 3 && isChouLiu() && cards.size() != 9)) {
			return;
		}
		if(getTableStatus()!=SandhConstants.TABLE_STATUS_MAIPAI){
    		addGameActionLog(player, "NoMaiPaiState");
    		return;
    	}
		 setDipai(cards);
		 addPlayLog(addSandhPlayLog(player.getSeat(), SandhConstants.TABLE_STATUS_MAIPAI, cards,false,0,null,player.getSeat()));
//		dipai = cards;
		PlayCardRes.Builder res = PlayCardRes.newBuilder();
		res.setUserId(player.getUserId() + "");
		res.setSeat(player.getSeat());
		res.setCardType(SandhConstants.REQ_MAIPAI);
		res.setIsPlay(1);
		res.setNextSeat(player.getSeat());
		res.addAllCardIds(cards);

		for (SandhPlayer sanPlayer : seatMap.values()) {
			sanPlayer.writeSocket(res.build());
		}
		for(Integer id: dipai){
			player.getHandPais().remove(id);
//			player.removeHandPais(dipai);
		}
		
		
		addGameActionLog(player, "maiPai|"+cards+"|"+player.getHandPais());
		
		addTurnNum(1);
		setTurnFirstSeat(player.getSeat());
		setTableStatus(SandhConstants.TABLE_STATUS_PLAY);
	}
	
	
	public String  addSandhPlayLog(int seat,int action,List<Integer> cards,boolean baofu,int fen,List<Integer> fenCards,int nextSeat){
		JSONObject json = new JSONObject();
		json.put("seat", seat);
		json.put("action", action);
		json.put("vals", cards);
		if(baofu){
			json.put("baofu", 1);
		}
		json.put("fen", fen);
		if(fenCards!=null){
			json.put("fenCards", fenCards);
		}
		json.put("nextSeat", nextSeat);
		return json.toJSONString();
		
	}
    
    
    public void playXuanzhu(SandhPlayer player,int zhu) {
    	if(nowDisCardSeat!=player.getSeat()) {
    		LogUtil.msgLog.info("now actionseat is error  + nowDisCardSeat = "+nowDisCardSeat + "actionSeat = "+player.getSeat());
    		return;
    	}
    	if(zhu<0||zhu>4) {
    		LogUtil.msgLog.info("xuanzhu  params error zhu"+zhu + " seat = "+player.getSeat());
    		return;
    	}
    	if(zhuColor!=-1) {
    		LogUtil.msgLog.info("has already xuanzhu "+player.getSeat());
    		return;
    	}
    	
    	if(getTableStatus()!=SandhConstants.TABLE_STATUS_XUANZHU){
    		addGameActionLog(player, "NoXuanZhuState");
    		return;
    	}
    	
    	zhuColor = zhu;
    	
    	ArrayList<Integer> val = new ArrayList<>();
    	val.add(zhu);
    	 addPlayLog(addSandhPlayLog(player.getSeat(), SandhConstants.TABLE_STATUS_XUANZHU, val,false,0,null,player.getSeat()));
    	ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_XUANZHU,zhu);
		for (SandhPlayer splayer : seatMap.values()) {
			splayer.writeSocket(builder.build());
		}
    	
		 setTableStatus(SandhConstants.TABLE_STATUS_MAIPAI);
    	
    }
    
    
	public void playChuPaiRecord(SandhPlayer player) {
		JSONArray jarr = new JSONArray();
		for (SandhPlayer splayer : seatMap.values()) {
			JSONObject json = new JSONObject();
			json.put("seat", splayer.getSeat());
			JSONArray jarr2 = new JSONArray();
			for (int i = 1; i <= turnNum; i++) {
				JSONObject json2 = new JSONObject();
				if(i>turnWinSeats.size()){
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
    
	
	public void playLiushou(SandhPlayer player,int color) {
		for (SandhPlayer splayer : seatMap.values()) {
			if(splayer.getBaofu()==0 && splayer.getSeat()!=banker){
				return;
			}
		}
		player.setLiushou(color);
		
    	ArrayList<Integer> val = new ArrayList<>();
    	val.add(color);
    	 addPlayLog(addSandhPlayLog(player.getSeat(), SandhConstants.TABLE_LIUSHOU_PLAY, val,player.getBaofu()==1?true:false,0,null,getNowDisCardSeat()));
		if (!player.isAutoPlay()) {
            player.setAutoPlay(false, this);
            player.setLastOperateTime(System.currentTimeMillis());
        }
		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_Liushou, player.getSeat(),color);
		for (SandhPlayer splayer : seatMap.values()) {
			splayer.writeSocket(builder.build());
		}
	}
	
	
	public void playTouxiang(SandhPlayer player, int type) {
		if (type < 0 || type > 3) {
			return;
		}
		if (player.getTouXiang() >= 1) {
			return;
		}
		
		if(state==table_state.ready||tableStatus != SandhConstants.TABLE_STATUS_MAIPAI){	
			return;
		}
		player.setTouXiang(type);
		addGameActionLog(player, "toux|"+type);
		
		if (touxiangXW == 1) {
			List<Integer> touxs = new ArrayList<Integer>();
			if(type ==1){
				setTouxiangTime(TimeUtil.currentTimeMillis());
			}else if(type==3){
				setTouxiangTime(0);
			}
			sendTouxiangMsg(touxs);
			if (touxs.size() == maxPlayerCount) {
				state = table_state.over;
			}
			
		}
		else {
			// ????????????
			state = table_state.over;
		}
		if (state == table_state.over) {
			calcOver();
		}

	}

	private void sendTouxiangMsg(List<Integer> touxs) {
		JSONArray jarr = new JSONArray();
		for (SandhPlayer splayer : seatMap.values()) {
			JSONObject json = new JSONObject();
			json.put("seat", splayer.getSeat());
			json.put("state", splayer.getTouXiang());
			if (splayer.getTouXiang() == 1 || splayer.getTouXiang() == 2) {
				touxs.add(splayer.getTouXiang());
			}
			jarr.add(json);
		}
		
		int txTime = (int)(TimeUtil.currentTimeMillis() - getTouxiangTime());
		txTime= autoTimeOut-txTime;
		if(txTime<0||!autoPlay){
			txTime = 0;
		}
		//
		ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_TOUX,txTime, jarr.toString());
		for (SandhPlayer splayer : seatMap.values()) {
			splayer.writeSocket(builder.build());
		}
	}
    
	
    
    
    public void playJiaoFen(SandhPlayer player,int fen,int pai) {
    	
    	if(nowDisCardSeat!=player.getSeat()) {
    		LogUtil.msgLog.info("now actionseat is error  + nowDisCardSeat = "+nowDisCardSeat + "actionSeat = "+player.getSeat());
    		return;
    	}
    	
    	if(getTableStatus()!=SandhConstants.TABLE_STATUS_JIAOFEN){
    		addGameActionLog(player, "Nojiaofenstate");
    		return;
    	}
    	//fen>80 ||
    	if( fen<0||fen%5!=0) {
    		addGameActionLog(player, "jiaofen fen params error fen "+fen);
    		return;
    	}
    	if(pai==1&&fen>50){
    		addGameActionLog(player, "jiaofen params error pai "+pai+" fen "+fen);
    		return;
    	}
    	
    	if(jiaoFen==0&&fen!=0){
    		setJiaoFen(fen);
    	}
    	//???????????????????????????????????????????????????
    	if(jiaoFen<fen || (isPai&&fen>50)){
    		addGameActionLog(player," jiaofen params error,fen over  "+fen);
    		return;
    	}
    	
    	
    	//????????????
    	if(cSJiaoFen==1){
    		//?????????????????????????????????????????????????????????
    		if(pai==1&&jiaoFenSs.size()>0&&!isPai){
    			if(jiaoFenSs.size()<maxPlayerCount){
    				for(Integer seat: jiaoFenSs){
        				SandhPlayer nextPlayer = seatMap.get(seat);
        				nextPlayer.setJiaofen(0);
        			}
    			}
    		}
    		jiaoFenSs.add(player.getSeat());
    		//??????????????????????????????????????????
    		if(jiaoFenSs.size()==maxPlayerCount&&!isPai&&pai!=1){
    			setNoJiaPai(1);
    			addGameActionLog(player,"noJiaPai|"+1);
    		}
    	}
    	
    	
    	if(pai==1&&fen>0) {
    		if(noJiaPai!=1){
    			this.isPai = true;
    		}
    	}
    	
    	addGameActionLog(player,"jiaofen|"+fen+"|"+pai+"|"+isPai);
    	
    	setDisCardSeat(player.getSeat());
    	if(fen!=0){
    		setJiaoFen(fen);
    	}
    	player.setJiaofen(fen);
    	int nextActionSeat= 0;
    	int nextS = player.getSeat();
    	for(int i=0;i<maxPlayerCount-1;i++) {
    		nextS+=1;
    		if(nextS > maxPlayerCount){
    			nextS = 1;
    		}
        	SandhPlayer nextPlayer = seatMap.get(nextS);
        	if(nextPlayer.getJiaofen()==0){
        		continue;
        	}
        	nextActionSeat = nextPlayer.getSeat();
        	break;
    	}
    	
    	//????????????
		if (nextActionSeat == 0 && fen == 0) {
			// autoPlayDiss = true;
			boolean over = true;
			for (SandhPlayer splayer : seatMap.values()) {
				splayer.initNext();
				splayer.changeState(player_state.ready);
				if (!splayer.isAutoPlay()) {
					over = false;
				}
			}
			if (over) {
				// ????????????
				autoPlayDiss = true;
				state = table_state.over;
				calcOver();

			} else {
				changeTableState(table_state.ready);
				checkDeal();
				this.isPai = false;
				//????????????
		    	if(cSJiaoFen==1){
		    		jiaoFenSs.clear();
		    		setNoJiaPai(0);
		    	}
				
			}
			return;
		}
    
    	int bankerSeat = 0;
    	if(fen==5) {
    		bankerSeat = player.getSeat();
    	}else {
    		//????????????
    		int zeroF = 0;
    		int overZ = 0;
    		for (SandhPlayer splayer : seatMap.values()) {
    			if(splayer.getJiaofen()==0){
    				zeroF+=1;
    			}
    			else if(splayer.getJiaofen()>0){
    				overZ=splayer.getSeat();
    			}
    		}
        	if(zeroF==maxPlayerCount-1&&overZ>0) {
        		if(nextActionSeat==0){//????????????????????????????????????
        			nextActionSeat= player.getSeat();
        			bankerSeat = player.getSeat();
        		}else {
        			bankerSeat = nextActionSeat;
        		}
        	}
    	}
    	setNowDisCardSeat(nextActionSeat);
    	
    	ArrayList<Integer> val = new ArrayList<>();
    	val.add(fen);
    	if(isPai){
    		val.add(1);
    	}else{
    		val.add(0);
    	}
    	addPlayLog(addSandhPlayLog(player.getSeat(), SandhConstants.TABLE_STATUS_JIAOFEN, val,false,0,null,nextActionSeat));
    	//nowDisCardSeat = nextActionSeat;
    	
    	
    	if(noJiaPai==1){
    		pai =2;
    	}
		ComRes.Builder jbuilder = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_JIAOFEN, fen, pai, player.getSeat(),
				bankerSeat == 0 ? nextActionSeat : 0);
		for (SandhPlayer splayer : seatMap.values()) {
			splayer.writeSocket(jbuilder.build());
		}

		// ?????????
		if (bankerSeat != 0) {
			ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_DINGZHUANG, dipai, bankerSeat + "");
			for (SandhPlayer splayer : seatMap.values()) {
				splayer.writeSocket(builder.build());
			}
			
			SandhPlayer banker1 = seatMap.get(bankerSeat);
			banker1.addHandPais(dipai);
			banker = bankerSeat;
			setNowDisCardSeat(bankerSeat);
			addPlayLog(addSandhPlayLog(bankerSeat, SandhConstants.TABLE_DINGZHUANG, dipai,false,0,null,nextActionSeat));
			setTableStatus(SandhConstants.TABLE_STATUS_XUANZHU);
		}
		
    }

    public int getAutoTimeOut() {
        return autoTimeOut;
    }

    /**
	 * ??????????????????????????????
	 *
	 * @return 0 ???????????? 1???????????? 2????????????
	 */
    public int isCanPlay() {
        if (seatMap.size() < getMaxPlayerCount()) {
            return 1;
        }
        for (SandhPlayer player : seatMap.values()) {
            if (player.getIsEntryTable() != SharedConstants.table_online) {
				// ?????????????????????
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
        replayDisCard="";
        timeNum = 0;
        newRound = true;
        finishFapai=0;
        zhuoFen.clear();
        dipai.clear();
        turnNum=0;
        turnFirstSeat=0;
        zhuColor = -1;
        banker = 0;
        jiaoFen = 0;
        isPai=false;
        turnWinSeats.clear();
        jiaoFenSs.clear();
        setNoJiaPai(0);
        setTableStatus(0);
        setTouxiangTime(0);
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
        
        if (playedBureau == 0) {
			SandhPlayer player = playerMap.get(masterId);
			int masterseat = player != null ? player.getSeat() : seatMap.keySet().iterator().next();
			nowDisCardSeat = masterseat;
		} else {
			if (getLastWinSeat() == 0) {
				nowDisCardSeat = RandomUtils.nextInt(maxPlayerCount) + 1;
			}else{
				nowDisCardSeat = getLastWinSeat();
			}
		}
        setDisCardSeat(nowDisCardSeat);
        //??????????????????
        setTableStatus(SandhConstants.TABLE_STATUS_JIAOFEN);
        for (SandhPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            res.addAllHandCardIds(tablePlayer.getHandPais());
            //??????????????????
            res.setNextSeat(nowDisCardSeat);
			res.setGameType(getWanFa());//
			res.setRemain(getTableStatus());
           // res.setBanker(lastWinSeat);
            tablePlayer.writeSocket(res.build());
            
            if(tablePlayer.isAutoPlay()) {
            	ArrayList<Integer> val = new ArrayList<>();
            	val.add(1);
          		 addPlayLog(addSandhPlayLog(tablePlayer.getSeat(), SandhConstants.action_tuoguan, val, false, 0, null, 0));
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
        return createTable(new CreateTableInfo(player,TABLE_TYPE_NORMAL,play,bureauCount,params,saveDb));
    }

    @Override
    public boolean createTable(CreateTableInfo createTableInfo) throws Exception {

        Player player = createTableInfo.getPlayer();
        int play = createTableInfo.getPlayType();
        int bureauCount =createTableInfo.getBureauCount();
        int tableType = createTableInfo.getTableType();
        List<Integer> params = createTableInfo.getIntParams();
        List<String> strParams = createTableInfo.getStrParams();
        boolean saveDb = createTableInfo.isSaveDb();

		// objects??????????????????
		// [??????,?????????15??????16??????,this.niao,this.leixing,this.zhuang,this.niaoPoint,????????????3,??????,??????????????????
        long id = getCreateTableId(player.getUserId(), play);
        if (id <= 0) {
            return false;
        }
        if (saveDb) {
            TableInf info = new TableInf();
            info.setTableType(tableType);
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

		payType = StringUtil.getIntValue(params, 2, 1);// 1AA,2??????

		shuangjinDC = StringUtil.getIntValue(params, 3, 1);// ????????????
		baofuLS = StringUtil.getIntValue(params, 4, 2);// ????????????

		checkPai = StringUtil.getIntValue(params, 5, 0);// ????????????1
		chouLiu = StringUtil.getIntValue(params, 6, 1);// ???6

		maxPlayerCount = StringUtil.getIntValue(params, 7, 3);// ??????
		touxiangXW = StringUtil.getIntValue(params, 8, 0);// ???????????????

		sixtyQJ = StringUtil.getIntValue(params, 9, 0);// 60?????????
		xiaoguangF = StringUtil.getIntValue(params, 10, 0);// ???????????? 0:30???
		
		daDaoTQ = StringUtil.getIntValue(params, 11, 0);// ??????????????????
		jiaofenJP = StringUtil.getIntValue(params, 12, 0);// ????????????
		jiaofenJD = StringUtil.getIntValue(params, 13, 0);// ????????????

		if (maxPlayerCount == 0) {
			maxPlayerCount = 3;
		}
		int time = StringUtil.getIntValue(params, 14, 0);

		this.autoPlay = time > 1;
		autoPlayGlob = StringUtil.getIntValue(params, 15, 0);

		if (time > 0) {
			autoTimeOut2 = autoTimeOut = (time * 1000);
		}
		
		daDaoFD = StringUtil.getIntValue(params, 16, 0);
		
		cSJiaoFen  = StringUtil.getIntValue(params, 17, 0);
		touXJind  = StringUtil.getIntValue(params, 18, 0);
		
		xianBaoFu = StringUtil.getIntValue(params, 19, 0);
		quGui = StringUtil.getIntValue(params, 20, 0);
        setLastActionTime(TimeUtil.currentTimeMillis());


        return true;
    }

    @Override
    protected void initNowAction(String nowAction) {

    }

    @Override
    public void initExtend0(JsonWrapper wrapper) {
//		JsonWrapper wrapper = new JsonWrapper(info);
		maxPlayerCount = wrapper.getInt(2, 3);
		if (maxPlayerCount == 0) {
			maxPlayerCount = 3;
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
        autoTimeOut = wrapper.getInt(6,0);
        autoPlayGlob = wrapper.getInt(7,0);
        jiaoFen = wrapper.getInt(8,0);
        newRound = wrapper.getInt(9, 1) == 1;
        finishFapai=wrapper.getInt(10, 0);
        belowAdd=wrapper.getInt(11, 0);
        below=wrapper.getInt(12, 0);
        autoTimeOut2 = autoTimeOut;
		// ???????????????
        if(autoPlay && autoTimeOut<=1) {
            autoTimeOut2 = autoTimeOut=60000;
        }
        
        
        
        isPai = wrapper.getInt(13,0)==1;
        zhuColor = wrapper.getInt(14,0);
        banker = wrapper.getInt(15,0);
        turnFirstSeat = wrapper.getInt(16,0);
        disColor = wrapper.getInt(17, 1) ;
        turnNum=wrapper.getInt(18, 0);
        tableStatus=wrapper.getInt(19, 0);

        touxiangXW=wrapper.getInt(20, 0);
        shuangjinDC=wrapper.getInt(21, 0);
        baofuLS=wrapper.getInt(22, 0);
        checkPai=wrapper.getInt(23, 0);
        chouLiu=wrapper.getInt(24, 0);
        sixtyQJ=wrapper.getInt(25, 0);
        
        //???????????????
        if(sixtyQJ==0){
        	sixtyQJ = 80;
        }else if(sixtyQJ==1){
        	sixtyQJ = 60;
        }
        
        xiaoguangF=wrapper.getInt(26, 0);
        daDaoTQ=wrapper.getInt(27, 0);
        jiaofenJP=wrapper.getInt(28, 0);
        jiaofenJD=wrapper.getInt(29, 0);
        touxiangTime=wrapper.getLong(30, 0);
        
        daDaoFD=wrapper.getInt(31, 0);
        
        cSJiaoFen=wrapper.getInt(32, 0);
        touXJind=wrapper.getInt(33, 0);
        
        xianBaoFu =wrapper.getInt(34, 0);
        quGui=wrapper.getInt(35, 0);
        
    }

    @Override
    protected String buildNowAction() {
        return null;
    }

    @Override
    public void setConfig(int index, int val) {

    }

    /**
	 * ????????????msg
	 *
	 * @param over
	 *            ??????????????????
	 * @param winPlayer
	 *            ????????????
	 * @return
	 */
    public ClosingInfoRes.Builder sendAccountsMsg(boolean over, boolean isBreak, int score,boolean koudi,int difen,boolean touxiang) {
        List<ClosingPlayerInfoRes> list = new ArrayList<>();
        List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();

        int minPointSeat = 0;
        int minPoint = 0;
//        if (winPlayer != null) {
//            for (SandhPlayer player : seatMap.values()) {
//                if (player.getUserId() == winPlayer.getUserId()) {
//                    continue;
//                }
//                if (minPoint == 0 || player.getPoint() < minPoint) {
//                    minPoint = player.getPlayPoint();
//                    minPointSeat = player.getSeat();
//                }
//            }
//        }

        for (SandhPlayer player : seatMap.values()) {
            ClosingPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();

            }
			// ?????????
			// ????????????
//            List<Integer> allCard = new ArrayList<Integer>();
//            for (Integer v : player.getHandPais()) {
//                if (!allCard.contains(v)) {
//                    allCard.add(v);
//                }
//            }
//			// ??????????????????
//            for (List<Integer> c : player.getOutPais()) {
//                for (Integer v : c) {
//                    if (!allCard.contains(v)) {
//                        allCard.add(v);
//                    }
//                }
//            }
//
//            JSONArray jsonArray = new JSONArray();
//            for (int card : allCard) {
//                if (card != 0) {
//                    jsonArray.add(card);
//                }
//            }
			//build.addExt(jsonArray.toString()); // 0

			build.addExt("0");// 3
			build.addExt("0");// 4
			build.addExt("0");// 5

			build.addExt(String.valueOf(player.getCurrentLs()));// 6
			build.addExt(String.valueOf(player.getMaxLs()));// 7
			build.addExt(String.valueOf(matchId));// 8


//            if (winPlayer != null && player.getUserId() == winPlayer.getUserId()) {
//				// ?????????????????????????????????????????????
//                builderList.add(0, build);
//            } else {
                builderList.add(build);
//            }

			// ?????????
            if(isCreditTable()){
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }

        }

		// ???????????????
        if (isCreditTable()) {
			// ??????????????????
            calcNegativeCredit();

            long dyjCredit = 0;
            for (SandhPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                SandhPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);

                builder.addExt(player.getWinLoseCredit() + "");      //10
                builder.addExt(player.getCommissionCredit() + "");   //11

				// 2019-02-26??????
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------??????????????????---------------------------------
            for (SandhPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                SandhPlayer player = seatMap.get(builder.getSeat());
                builder.addExt(player.getWinLoseCredit() + ""); // 10
                builder.addExt(player.getCommissionCredit() + ""); // 11
                builder.setWinLoseCredit(player.getWinGold());
            }
        } else {
            for (ClosingPlayerInfoRes.Builder builder : builderList) {
                SandhPlayer player = seatMap.get(builder.getSeat());
                builder.addExt(0 + ""); //10
                builder.addExt(0 + ""); //11
            }
        }
        for (ClosingPlayerInfoRes.Builder builder : builderList) {
            SandhPlayer player = seatMap.get(builder.getSeat());
            builder.addExt(player.getPiaoFen() + ""); //13
            list.add(builder.build());
        }

        ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.addAllClosingPlayers(list);
        res.addAllExt(buildAccountsExt(over?1:0,score,koudi,touxiang));
//        if(koudi){
//        	res.addAllCutCard(dipai);
//        }
        if (over && isGroupRoom() && !isCreditTable()) {
            res.setGroupLogId((int) saveUserGroupPlaylog());
        }
        for (SandhPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;
    }

    public List<String> buildAccountsExt(int over,int score,boolean koudi,boolean touxiang) {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");//0
        ext.add(masterId + "");//1
        ext.add(TimeUtil.formatTime(TimeUtil.now()));//2
        ext.add(playType + "");//3
		// ?????????????????????
        ext.add(playBureau + "");//4
        ext.add(isGroupRoom() ? "1" : "0");//5
		// ???????????????0
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
        ext.add(over+""); // 21
        ext.add(jiaoFen+""); // 22
        ext.add(score+""); // 23
        ext.add(koudi?"1":"0");
        ext.add(isPai?"1":"0");
        ext.add(touxiang?"1":"0");
        
        return ext;
    }
    
    


    @Override
    public String loadGameCode() {
        return GAME_CODE;
    }

    @Override
    public void sendAccountsMsg() {
        calcPointBeforeOver();
        ClosingInfoRes.Builder builder = sendAccountsMsg(true, true, 0,false,0,false);
        saveLog(true, 0l, builder.build());
    }

    @Override
    public Class<? extends Player> getPlayerClass() {
        return SandhPlayer.class;
    }

    @Override
    public int getWanFa() {
    	return GameUtil.game_type_sandh;
    }

    @Override
    public void checkReconnect(Player player) {
        SandhTable table = player.getPlayingTable(SandhTable.class);
       // player.writeSocket(SendMsgUtil.buildComRes(WebSocketMsgType.req_code_pdk_playBack, table.getReplayDisCard()).build());
        
        checkTouxiang(player);
		//
    }

	private void checkTouxiang(Player player) {
		if(touxiangXW!=1){
			return;
		}
		JSONArray jarr = new JSONArray();
		List<Integer> touxs = new ArrayList<Integer>();
		for (SandhPlayer splayer : seatMap.values()) {
			JSONObject json = new JSONObject();
			json.put("seat", splayer.getSeat());
			json.put("state", splayer.getTouXiang());
				touxs.add(splayer.getTouXiang());
				jarr.add(json);
		}
		if(touxs.contains(1)&&!touxs.contains(3)){
			int txTime = (int)(TimeUtil.currentTimeMillis() - getTouxiangTime());
			txTime= autoTimeOut-txTime;
			if(txTime<0||!autoPlay){
				txTime = 0;
			}
			ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.RES_TOUX, txTime,jarr.toString());
			player.writeSocket(builder.build());
		}
	}


	// ??????????????????????????????
    public boolean isShowCardNumber() {
        return 1 == getShowCardNumber();
    }

    @Override
    public void checkAutoPlay() {
        synchronized (this) {
        	if(checkLastTurn()){
            	return;
            }
            if(!autoPlay){
                return;
            }
			// ??????????????????????????????
            if (getSendDissTime() > 0) {
                for (SandhPlayer player : seatMap.values()) {
                    if (player.getLastCheckTime() > 0) {
                        player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    }
                }
                return;
            }
			// ????????????
            if (state == table_state.ready && playedBureau > 0 ) {
                ++timeNum;
                for (SandhPlayer player : seatMap.values()) {
					// ????????????????????????5???????????????
                    if (timeNum >= 5 && player.isAutoPlay()) {
                        autoReady(player);
                    } else if (timeNum >= 30) {
                        autoReady(player);
                    }
                }
                return;
            }

            SandhPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null) {
                return;
            } 

            if(getTableStatus() ==0||state != table_state.play){
                return;
            }
            
			// ??????????????????
            checkTouxiangTimeOut();
            
            int timeout;
            
            if(autoPlay){
                timeout = autoTimeOut;
                if (disCardRound == 0) {
                    timeout = autoTimeOut ;
                }
            } else if (player.isRobot()) {
                timeout = 3 * SharedConstants.SENCOND_IN_MINILLS;
            }else{
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
                    auto = checkPlayerAuto(player,timeout);
//                }
            }

            if (auto || player.isRobot()) {
                boolean autoPlay = false;
//                if (GameServerConfig.isAbroad()) {
//                    if (player.isRobot()) {
//                        autoPlayTime = MathUtil.mt_rand(2, 6) * 1000;
//                    } else {
//                        autoPlay = true;
//                    }
//                }
//                if (player.getAutoPlayTime() == 0L && !autoPlay) {
//                    player.setAutoPlayTime(now);
//                } else if (autoPlay || (player.getAutoPlayTime() > 0L && now - player.getAutoPlayTime() >= autoPlayTime)) {
                    player.setAutoPlayTime(0L);
                    if (state == table_state.play) {
                    	
                    	if(getTableStatus()==SandhConstants.TABLE_STATUS_PLAY) {
                    		//????????????
                            autoChuPai(player);
                    	}else if(getTableStatus()==SandhConstants.TABLE_STATUS_JIAOFEN){
                    		playJiaoFen(player, 0, 0);
                    		
                    	}else if(getTableStatus()==SandhConstants.TABLE_STATUS_XUANZHU){
                    		playXuanzhu(player, 0);
                    	}else if(getTableStatus()==SandhConstants.TABLE_STATUS_MAIPAI){
                    		List<Integer> disList = new ArrayList<Integer>();
                    		int size = 8;
                    		if(!isChouLiu()){
                    			size= 8;
                    		}else if(getMaxPlayerCount() == 3 && isChouLiu() ) {
                    			size=9;
                    		}
                    		List<Integer>  zCards = CardUtils.getZhu(player.getHandPais(), zhuColor);
                    		List<Integer> curList = new ArrayList<>(player.getHandPais());
                    		curList.removeAll(zCards);
                    		if(curList.size()<size){
                    			disList.addAll(curList);
                    			disList.addAll(zCards.subList(0, size-curList.size()));
                    		}else{
                    			disList.addAll(curList.subList(0,size));
                    		}
                    		playMaipai(player, disList);
                    		
                    	}
                    	
                    }
                }
            }
//        }
    }

	private void checkTouxiangTimeOut() {
		if (tableStatus == SandhConstants.TABLE_STATUS_MAIPAI&&touxiangXW==1) {
			if(getTouxiangTime()>0){
				int txTime = (int)(TimeUtil.currentTimeMillis() - getTouxiangTime());
				List<Integer> agreeTX = new ArrayList<Integer>();
				for (SandhPlayer player : seatMap.values()) {
					 if (player.getLastCheckTime() > 0) {
		                 player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
		             }
		         	if(player.getTouXiang()==0&&txTime>=autoTimeOut){
		         		player.setTouXiang(2);
		         	}
		         	
		         	
		         	if(player.getTouXiang()==2||player.getTouXiang()==1){
		         		agreeTX.add(player.getTouXiang());
		         	}
		         }
				
				if (agreeTX.size() == maxPlayerCount) {
					sendTouxiangMsg(agreeTX);
					state = table_state.over;
					calcOver();
				}
			
			}
		}else{
			//????????????????????????????????????????????????????????????????????????????????????
			 if(getTableStatus()==SandhConstants.TABLE_STATUS_PLAY){
				 setTouxiangTime(0);
					for (SandhPlayer player : seatMap.values()) {
			         	if(player.getTouXiang()>0){
			         		player.setTouXiang(0);
			         	}
			         }
			 }
		}
			
	}

	private boolean checkLastTurn() {
		if(getTableStatus()==SandhConstants.TABLE_STATUS_PLAY) {
			 SandhPlayer player = seatMap.get(nowDisCardSeat);
             if (player == null) {
                 return false;
             } 
			int firstSeat = getTurnFirstSeat();
			
			if (firstSeat != player.getSeat()) {
				SandhPlayer fiser = seatMap.get(firstSeat);
				if(fiser!=null&&fiser.getHandPais().isEmpty()) {
					 playCommand(player,0,new ArrayList<>(player.getHandPais()));
					 return true;
				}
			}else{
				//?????????????????????????????????
				boolean allBaofu = true;
				for (Map.Entry<Integer, SandhPlayer> entry : seatMap.entrySet()) {
					if(entry.getValue().getSeat()==firstSeat){
						continue;
					}
					if(entry.getValue().getBaofu()!=1){
						allBaofu = false;
						break;
					}
		        }
				
				//??????????????????????????????if(!CardTool.allZhu(cards, zhuColor)){
				boolean isAuto =false;
				CardType ct = CardTool.getCardType(player.getHandPais(),zhuColor,isChouLiu());
				if(ct.getType()==CardType.DAN||ct.getType()==CardType.DUI||ct.getType()==CardType.TUOLAJI){
					isAuto = true;
				}else if(ct.getType()==CardType.SHUAI_LIAN_DUI||ct.getType()==CardType.SHUAIPAI||ct.getType()==CardType.SHUAIPAI2){
					if(allBaofu&&CardTool.allZhu(player.getHandPais(), zhuColor)){
						isAuto = true;
					}
				}
				if(isAuto){
					 playCommand(player,0,new ArrayList<>(player.getHandPais()));
					 return true;
				}
				
				
				
			}
		}
		return false;
	}

	private void autoChuPai(SandhPlayer player) {
		List<Integer> curList = new ArrayList<>(player.getHandPais());
		if (curList.isEmpty()) {
		    return;
		}
		int firstSeat = getTurnFirstSeat();
		List<Integer> disList = new ArrayList<Integer>();
		// ???????????????
		if (firstSeat == player.getSeat()) {
			// ??????????????????
			int rand = RandomUtils.nextInt(curList.size());
			disList.add(curList.get(rand));

		} else {

			int color = disColor / 10;
			int type = disColor % 10;
			SandhPlayer fiser = seatMap.get(turnFirstSeat);
			List<Integer> list = fiser.getCurOutCard(getTurnNum());

			List<Integer> cards = null;
			if (color == zhuColor) {
				cards = CardUtils.getZhu(curList, color);
			} else {
				cards = CardUtils.getColorCards(curList, color);
			}
			// ??????????????????
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
						disList.addAll(cards.subList(0, list.size()));
					} else {
						disList.addAll(dui);
						// ????????????????????????
						if (dui.size()/2 < needDuiCount) {
							cards.removeAll(dui);
							CardUtils.sortCards(cards);
							disList.addAll(cards.subList(0, list.size() - dui.size()));
						}
					}

				}

			}

		}
		
        playCommand(player,0,disList);
	}


    public boolean checkPlayerAuto(SandhPlayer player ,int timeout){
        if(player.isAutoPlay()){
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
            if(auto){
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
        for (Map.Entry<Integer, SandhPlayer> entry : seatMap.entrySet()) {
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
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,Object... objects) throws Exception {
        createTable(player, play, bureauCount, params);
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {

    }

    @Override
    public boolean isCreditTable(List<Integer> params){
        return params != null && params.size() > 13 && StringUtil.getIntValue(params, 13, 0) == 1;
    }

    public String getGameName(){
		return "?????????";
    }

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_sandh);


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
			// ???????????????????????????????????????
            if (jiaoFen > 0) {
                boolean isAllDaNiao = true;
                if (this.isTest()) {
					// ?????????????????????
//                    for (SandhPlayer robotPlayer : seatMap.values()) {
//                    }
                }
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_pdk_daniao, 1);
                for (SandhPlayer player : seatMap.values()) {
                }
                if (!isAllDaNiao) {
                    broadMsgRoomPlayer(com.build());
                }
                return isAllDaNiao;
            } else {
                for (SandhPlayer player : seatMap.values()) {
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
        } else if(state == table_state.ready ){
            return true;
        }else {
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
    
    public boolean isChouLiu() {
		return chouLiu ==1;
	}
    
    
	public long getTouxiangTime() {
		return touxiangTime;
	}

	public void setTouxiangTime(long touxiangTime) {
		this.touxiangTime = touxiangTime;
	}

	public String getTableMsg() {
        Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "?????????");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (autoPlay) {
            json.put("autoTime", autoTimeOut2 / 1000);
            if (autoPlayGlob == 1) {
				json.put("autoName", "??????");
            } else {
				json.put("autoName", "??????");
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
	
	
	public void setNoJiaPai(int noJiaPai) {
		this.noJiaPai = noJiaPai;
	}

	public void addTurnNum(int turnNum) {
		this.turnNum += turnNum;
		changeExtend();
	}
}
