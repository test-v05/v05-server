package com.sy599.game.qipai.bsmj.bean;

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

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiInfo;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.DaPaiTingPaiRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangMoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangPlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.TingPaiRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjInfoRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg.ClosingMjPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.bsmj.constant.BsMjAction;
import com.sy599.game.qipai.bsmj.constant.BsMjConstants;
import com.sy599.game.qipai.bsmj.rule.BsMj;
import com.sy599.game.qipai.bsmj.rule.BsMjHelper;
import com.sy599.game.qipai.bsmj.rule.BsMjRobotAI;
import com.sy599.game.qipai.bsmj.tool.BsMjQipaiTool;
import com.sy599.game.qipai.bsmj.tool.BsMjResTool;
import com.sy599.game.qipai.bsmj.tool.BsMjTool;
import com.sy599.game.qipai.bsmj.tool.hulib.util.HuUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.PayConfigUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

/**
 * @author l ????????????????????????
 */
public class BsMjTable extends BaseTable {
	/**
	 * ????????????????????????
	 */
	private List<BsMj> nowDisCardIds = new ArrayList<>();
	/**
	 * ?????????????????????????????????
	 */
	private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
	/**
	 * 0??? 1??? 2?????? 3??????(???????????????????????? ????????????3??????)4??? 5??????(6????????? 7????????? 8????????? 9????????? 10????????? 11??????
	 * 12????????? 13???????????? 14???????????????)
	 */
	private Map<Integer, Map<Integer, List<Integer>>> gangSeatMap = new ConcurrentHashMap<>();
	/**
	 * ??????????????????????????????
	 */
	private int maxPlayerCount = 4;
	/**
	 * ????????????????????????????????????
	 */
	private List<BsMj> leftMajiangs = new ArrayList<>();
	/**
	 * ??????????????????????????????map
	 */
	private Map<Long, BsMjPlayer> playerMap = new ConcurrentHashMap<Long, BsMjPlayer>();
	/**
	 * ???????????????????????????MAP
	 */
	private Map<Integer, BsMjPlayer> seatMap = new ConcurrentHashMap<Integer, BsMjPlayer>();
	/**
	 * ???????????????
	 */
	private Map<Integer, Integer> huConfirmMap = new HashMap<>();
	/**
	 * ?????????????????????????????? ??????????????????????????????????????? 1??????????????????????????????????????? ??????????????????????????? ??????????????????
	 * 2???????????????????????????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????? ?????????????????????????????????????????????
	 */
	private Map<Integer, BsMjTempAction> tempActionMap = new ConcurrentHashMap<>();
	/**
	 * ??????
	 */
	private int birdNum;
	/**
	 * ????????????
	 */
	private int isCalcBanker;
	/**
	 * ?????????????????? 1?????? 2??????
	 */
	private int calcBird;
	/**
	 * ???????????? 1.???????????????1 2.???????????????2 3???????????????1 4.???????????????2 5.???????????????1
	 */
	private int buyPoint;

	private int chajiao;//

	private int isAutoPlay;// ????????????????????????
	private int readyTime = 0;

	/**
	 * ????????????seat
	 */
	private int moMajiangSeat;
	/**
	 * ???????????????
	 */
	private BsMj moGang;
	/**
	 * ??????????????????
	 */
	private BsMj gangMajiang;
	/**
	 * ?????????
	 */
	private List<Integer> moGangHuList = new ArrayList<>();
	/**
	 * ?????????????????????
	 */
	private List<BsMj> gangDisMajiangs = new ArrayList<>();
	/**
	 * ?????????????????????
	 */
	private int moLastMajiangSeat;
	/**
	 * ????????????????????????
	 */
	private int askLastMajaingSeat;
	/**
	 * ??????????????????????????????
	 */
	private int fristLastMajiangSeat;

	/**
	 * ?????????????????????
	 */
	private List<Integer> moLastSeats = new ArrayList<>();
	/**
	 * ??????????????????
	 */
	private BsMj lastMajiang;
	/**
	 *
	 */
	private int disEventAction;

	/*** GPS?????? */
	private int gpsWarn = 0;
	/*** ????????? */
	private int youfeng = 0;
	/*** ????????? */
	private int yitiaolong = 0;
	/*** ????????? */
	private int siguiyi = 0;
	/*** ?????? */
	private int baoting = 0;

	/*** ????????????????????????????????? */
	private List<Integer> showMjSeat = new ArrayList<>();

	private int tableStatus;// ???????????? 1??????

	/** ??????1????????????2????????? */
	private int autoPlayGlob;
	
	private int autoTableCount;


	/*** ???????????? **/
	private int gangDice = -1;

	/*** ????????????????????? */
	private List<Integer> moTailPai = new ArrayList<>();

	/** ???????????????????????????????????? **/
	private BsMj gangActedMj = null;

	/** ??????????????? **/
	private boolean isBegin = false;

	private int dealDice;

	private int gangSeat;
	
	
	
    //???????????????0??????1???
    private int jiaBei;
    //?????????????????????xx???????????????
    private int jiaBeiFen;
    //????????????????????????
    private int jiaBeiShu;
	
	
    //belowAdd???
    private int belowAdd=0;
    //??????below???+
    private int below=0;
    

	@Override
	protected boolean quitPlayer1(Player player) {
		return false;
	}

	@Override
	protected boolean joinPlayer1(Player player) {

		BsMjPlayer pl = (BsMjPlayer) player;
		// ???????????????????????????
		if (getBuyPoint() == 0) {
			pl.setPiaoPoint(0);
		}

		return false;
	}

	@Override
	public int isCanPlay() {
		return 0;
	}

	@Override
	public void calcOver() {
		List<Integer> winSeatList = new ArrayList<>(huConfirmMap.keySet());
		boolean selfMo = false;
		int[] birdMjIds = null;
		int[] seatBirds = null;
		// Map<Integer, Integer> seatBirdMap = new HashMap<>();
		boolean flow = false;

		for (BsMjPlayer seat : seatMap.values()) {
			seat.setGangPoint(seat.getLostPoint());
			seat.setLostPoint(0);
			if (winSeatList.size() == 0) {
				seat.setGangPoint(0);
			}
		}

		int fangpaoSeat = 0;
		if (winSeatList.size() == 0) {
			// ??????
			flow = true;
			checkChaJiao();

		} else {
			// ??????????????????????????????
			BsMjPlayer winPlayer = null;
			if (winSeatList.size() == 1) {
				winPlayer = seatMap.get(winSeatList.get(0));
				if ((winPlayer.isAlreadyMoMajiang() || winPlayer.isGangshangHua())
						&& winSeatList.get(0) == moMajiangSeat) {
					selfMo = true;
				}
			}

			// ???????????????????????????????????????
			if (!winSeatList.isEmpty()) {
				// ?????????
				birdMjIds = zhuaNiao();
			}

			// ??????
			if (selfMo || (winPlayer != null && winPlayer.getDahu().contains(14))) {
				// seatBirds = birdToSeat(birdMjIds, winPlayer.getSeat());
				int daHuCount = winPlayer.getDahuFan();
				int winPoint = 1;
				// ??????
				if (daHuCount > 0) {
					if (buyPoint > 2) {
						if(daHuCount > 3){
							daHuCount = 3;
						}
						winPoint = (int) Math.pow(2, daHuCount);
					}else{
						winPoint= daHuCount+1;
					}
					
					// winPlayer.changeActionTotal(BsMjAction.ACTION_COUNT_DAHU_ZIMO,1);
				}

				int totalWinPoint = 0;
				// (winPlayer.getActionNum(1)-winPlayer.getActionNum(5)) +
				// int gangfen1 =
				// ((seatMap.size()-1)*(winPlayer.getActionNum(2)+winPlayer.getActionNum(3)*2));
				for (int loserSeat : seatMap.keySet()) {
					// ????????????????????????
					if (winSeatList.contains(loserSeat)) {
						continue;
					}
					BsMjPlayer loser = seatMap.get(loserSeat);

					int losePoint = winPoint;
					if (buyPoint == 1 || buyPoint == 2) {
						losePoint += (loser.getPiaoPoint() + winPlayer.getPiaoPoint());
					} else if (buyPoint > 2) {
						losePoint = (1 + loser.getPiaoPoint() + winPlayer.getPiaoPoint()) * (losePoint);
					} else {
						// losePoint -=gangfen;
					}

					totalWinPoint += losePoint;
					loser.setLostPoint(-losePoint);
				}

				winPlayer.changeAction(7, 1);
				winPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);

				winPlayer.setLostPoint(totalWinPoint);

				// checkGangFen();
				winPlayer.setWinCount(winPlayer.getWinCount() + 1);

			} else {
				BsMjPlayer losePlayer = seatMap.get(disCardSeat);
				fangpaoSeat = losePlayer.getSeat();

				losePlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index10, winSeatList.size());
				int totalLosePoint = 0;
				for (int winSeat : winSeatList) {
					// ??????
					winPlayer = seatMap.get(winSeat);
					int daHuCount = winPlayer.getDahuFan();

					// ??????
					int winPoint = 1;
					if (daHuCount > 0) {
						if (buyPoint > 2) {
							if(daHuCount > 3){
								daHuCount = 3;
							}
							winPoint = (int) Math.pow(2, daHuCount);
						}else{
							winPoint  = daHuCount+1;
						}
					}

					// int gangFen = player.ge

					//
					if (buyPoint == 1 || buyPoint == 2) {
						winPoint += (losePlayer.getPiaoPoint() + winPlayer.getPiaoPoint());
					} else if (buyPoint > 2) {
						winPoint = (1 + losePlayer.getPiaoPoint() + winPlayer.getPiaoPoint()) * winPoint;
					} else {
						// winPoint +=gangfen;
					}

					totalLosePoint += winPoint;
					winPlayer.changeAction(6, 1);
					winPlayer.setWinCount(winPlayer.getWinCount() + 1);
					losePlayer.changeAction(0, 1);
					winPlayer.changeActionTotal(6, 1);
					losePlayer.changeActionTotal(0, 1);
					winPlayer.setLostPoint(winPoint);
				}

				losePlayer.setLostPoint(-totalLosePoint);

			}

		}

		if (!flow) {
			for (BsMjPlayer seat : seatMap.values()) {
				seat.changePoint(seat.getLostPoint() + seat.getGangPoint());
			}
		}

		boolean over = playBureau == totalBureau;
		if (autoPlayGlob > 0) {
			// //????????????
			boolean diss = false;
			if (autoPlayGlob == 1) {
				for (BsMjPlayer seat : seatMap.values()) {
					if (seat.isAutoPlay()) {
						diss = true;
						break;
					}

				}
			}else if (autoPlayGlob == 3) {
				diss = checkAuto3();
			}
			if (diss) {
				autoPlayDiss = true;
				over = true;
			}
		}

		// ?????????????????????
		ClosingMjInfoRes.Builder res = sendAccountsMsg(over, selfMo, winSeatList, birdMjIds, seatBirds,
				playBureau == totalBureau, 1, fangpaoSeat);
		// ????????????
		if (!flow) {
			if (winSeatList.size() > 1) {
				// ???????????????????????????????????????
				setLastWinSeat(disCardSeat);
			} else {
				setLastWinSeat(winSeatList.get(0));

			}

		} else {
			if (moLastMajiangSeat != 0) {
				// ??????????????????????????????????????????
				setLastWinSeat(moLastMajiangSeat);

			} else if (fristLastMajiangSeat != 0) {
				// ???????????????????????????????????????????????????????????????
				setLastWinSeat(fristLastMajiangSeat);

			}

		}
		saveLog(over, 0l, res.build());

		calcAfter();
		if (playBureau >= totalBureau || over) {
			calcOver1();
			calcOver2();
			calcOver3();
			diss();
		} else {
			initNext();
			calcOver1();
		}

		for (Player player : seatMap.values()) {
			player.saveBaseInfo();
		}
	}
	
	private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (BsMjPlayer seat : seatMap.values()) {
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

	private void checkChaJiao() {
		if (chajiao < 1) {
			return;
		}
		List<Integer> copy = new ArrayList<>(BsMjConstants.baoshan_mjList);
		if (youfeng == 1) {
			copy.addAll(BsMjConstants.feng_mjList);
		}
		List<BsMj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(BsMj.getMajang(id));
		}

		List<BsMj> tingList = new ArrayList<>();
		HashMap<Integer, BsMjiangHu> tingHuMap = new HashMap<>();
		for (int loserSeat : seatMap.keySet()) {
			BsMjPlayer player = seatMap.get(loserSeat);
			BsMjiangHu bigHu = null;
			for (BsMj mj : allMjs) {
				BsMjiangHu temp = player.checkHu(mj, false);
				BsMjTool.checkDahuMax(temp, this, player, mj, false);
				if (temp.isHu()) {
					if (bigHu == null) {
						bigHu = temp;
					}

					if (bigHu.getDahuFan() > 0) {
						if (bigHu.getDahuFan() < temp.getDahuFan()) {
							bigHu = temp;
						}
						bigHu.initDahuList();
						player.setDahu(bigHu.getDahuList(), bigHu.getDahuFan());
					}

					tingHuMap.put(loserSeat, bigHu);
				}
			}
		}

		if (tingHuMap.size() == seatMap.size()) {
			return;
		}
		Set<Integer> keys = tingHuMap.keySet();
		for (Entry<Integer, BsMjiangHu> entry : tingHuMap.entrySet()) {
			int seat = entry.getKey();
			// ??????
			BsMjPlayer winPlayer = seatMap.get(seat);

			// ??????
			int winPoint = 1;

			if (chajiao == 2) {
				int daHuCount = winPlayer.getDahuFan();
				if (daHuCount > 0) {
					if (daHuCount > 3&&buyPoint > 2) {
						daHuCount = 3;
						winPoint = (int) Math.pow(2, daHuCount);
					}else{
						winPoint= daHuCount+1;
					}
					// winPlayer.changeActionTotal(BsMjAction.ACTION_COUNT_DAHU_ZIMO,1);
				}
				
				
			}

			int totalWp = 0;
			for (BsMjPlayer seat2 : seatMap.values()) {
				if (keys.contains(seat2.getSeat())) {
					continue;
				}
				int losePoint = winPoint;
				if (chajiao == 1) {
					totalWp += winPoint;
				} else {
					if (buyPoint == 1 || buyPoint == 2) {
						losePoint += (seat2.getPiaoPoint() + winPlayer.getPiaoPoint());
					} else if (buyPoint > 2) {
						losePoint = (1 + seat2.getPiaoPoint() + winPlayer.getPiaoPoint()) * (losePoint);
					}
					totalWp += losePoint;

				}

				seat2.changePoint(-losePoint);
			}

			winPlayer.changePoint(totalWp);
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
		userLog.setUserId(creatorId);
		userLog.setTableId(id);
		userLog.setRes(extendLogDeal(logRes));
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
		for (BsMjPlayer player : playerMap.values()) {
			player.addRecord(logId, playBureau);
		}
		UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);

		// LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" +
		// res);
		// String logRes =
		// JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResLog(res));
		// String logOtherRes =
		// JacksonUtil.writeValueAsString(LogUtil.buildClosingInfoResOtherLog(res));
		// Date now = TimeUtil.now();
		// UserPlaylog userLog = new UserPlaylog();
		// userLog.setLogId(playType);
		// userLog.setTableId(id);
		// userLog.setRes(logRes);
		// userLog.setTime(now);
		// userLog.setTotalCount(totalBureau);
		// userLog.setCount(playBureau);
		// userLog.setStartseat(lastWinSeat);
		// userLog.setOutCards(playLog);
		// userLog.setExtend(logOtherRes);
		// long logId = TableLogDao.getInstance().save(userLog);
		// for (BsMjPlayer player : playerMap.values()) {
		// player.addRecord(logId, playBureau);
		// }
		// UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
	}

	/**
	 * ??????
	 *
	 * @return
	 */
	private int[] zhuaNiao() {
		// ?????????
		int realBirdNum = leftMajiangs.size() > birdNum ? birdNum : leftMajiangs.size();
		int[] bird = new int[realBirdNum];
		for (int i = 0; i < realBirdNum; i++) {
			BsMj prickbirdMajiang = getLeftMajiang();
			if (prickbirdMajiang != null) {
				bird[i] = prickbirdMajiang.getId();
			} else {
				break;
			}
		}
		// ???????????????
		return bird;
	}

	/**
	 * ???????????????????????????
	 *
	 * @param prickBirdMajiangIds
	 * @param winSeat
	 * @return
	 */
	private int[] birdToSeat(int[] prickBirdMajiangIds, int winSeat) {
		int[] seatArr = new int[prickBirdMajiangIds.length];
		for (int i = 0; i < prickBirdMajiangIds.length; i++) {
			BsMj majiang = BsMj.getMajang(prickBirdMajiangIds[i]);
			int prickbirdPai = majiang.getPai();
			prickbirdPai = (prickbirdPai - 1) % 4;// ?????????????????? ?????????1
			int prickbirdseat = prickbirdPai + winSeat > 4 ? prickbirdPai + winSeat - 4 : prickbirdPai + winSeat;
			seatArr[i] = prickbirdseat;
		}
		return seatArr;
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
				tempMap.put("nowDisCardIds", StringUtil.implode(BsMjHelper.toMajiangIds(nowDisCardIds), ","));
			}
			if (tempMap.containsKey("leftPais")) {
				tempMap.put("leftPais", StringUtil.implode(BsMjHelper.toMajiangIds(leftMajiangs), ","));
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

	// public String buildExtend() {
	// JsonWrapper wrapper = new JsonWrapper("");
	// for (BsMjPlayer player : seatMap.values()) {
	// wrapper.putString(player.getSeat(), player.toExtendStr());
	// }
	// wrapper.putString(5, DataMapUtil.explode(huConfirmMap));
	// wrapper.putInt(6, birdNum);
	// wrapper.putInt(7, moMajiangSeat);
	// if (moGang != null) {
	// wrapper.putInt(8, moGang.getId());
	//
	// } else {
	// wrapper.putInt(8, 0);
	//
	// }
	// wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
	// wrapper.putString(10, MajiangHelper.implodeMajiang(gangDisMajiangs,
	// ","));
	// if (gangMajiang != null) {
	// wrapper.putInt(11, gangMajiang.getId());
	//
	// } else {
	// wrapper.putInt(11, 0);
	//
	// }
	// wrapper.putInt(12, askLastMajaingSeat);
	// wrapper.putInt(13, moLastMajiangSeat);
	// if (lastMajiang != null) {
	// wrapper.putInt(14, lastMajiang.getId());
	// } else {
	// wrapper.putInt(14, 0);
	// }
	// wrapper.putInt(15, fristLastMajiangSeat);
	// wrapper.putInt(16, disEventAction);
	// wrapper.putInt(17, isCalcBanker);
	// wrapper.putInt(18, calcBird);
	// return wrapper.toString();
	// }

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
		Random r = new Random();
		int dealDice = (r.nextInt(6) + 1) * 10 + (r.nextInt(6) + 1);
		addPlayLog(disCardRound + "_" + lastWinSeat + "_" + BsMjDisAction.action_dice + "_" + dealDice);
		setDealDice(dealDice);
		logFaPaiTable();

		for (BsMjPlayer tablePlayer : seatMap.values()) {
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			List<Integer> actionList = tablePlayer.checkMo(null, true);
			if (!actionList.isEmpty()) {
				addActionSeat(tablePlayer.getSeat(), actionList);
				res.addAllSelfAct(actionList);
			}
			res.addAllHandCardIds(tablePlayer.getHandPais());
			res.setNextSeat(getNextDisCardSeat());
			res.setGameType(getWanFa());
			res.setRemain(leftMajiangs.size());
			res.setBanker(lastWinSeat);
			res.setDealDice(dealDice);
			logFaPaiPlayer(tablePlayer, actionList);
			tablePlayer.writeSocket(res.build());
			sendTingInfo(tablePlayer);

			checkBaoTingMsg(tablePlayer);

		}

		isBegin = true;
		if (!hasBaoTing()) {
			// ????????????????????????????????????
			BsMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// ??????????????????
			bankPlayer.writeSocket(com.build());
			isBegin = false;
		}
	}

	private boolean checkBaoTingMsg(BsMjPlayer tablePlayer) {
		if (checkBaoting(tablePlayer)) {
			if(tablePlayer.getBaoTingS()==0){
				ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_ts_pao_ting,
						tablePlayer.getSeat(), tablePlayer.getBaoTingS());
				tablePlayer.writeSocket(com.build());
			}
			return true;
		}
		return false;
	}

	/**
	 * ??????
	 *
	 * @param player
	 */
	public void moMajiang(BsMjPlayer player, boolean isBuzhang) {
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
		if (disCardRound != 0 && player.isAlreadyMoMajiang()) {
			return;
		}
		if (leftMajiangs.size() == 0) {
			calcOver();
			return;
		}

		// ???????????????????????? ????????????
		if (getLeftMajiangCount() == 1 && !isBuzhang) {
			// calcMoLastSeats(player.getSeat());
			// sendAskLastMajiangRes(0);
			// if(moLastSeats == null || moLastSeats.size() == 0){
			// calcOver();
			// }
			// return;
		}
		if (isBuzhang) {
			addMoTailPai(-1);
		}
		// ??????
		BsMj majiang = null;
		if (disCardRound != 0) {
			// ????????????????????????????????????????????????
			if (player.isAlreadyMoMajiang()) {
				return;
			}
			if (GameServerConfig.isDebug() && !player.isRobot()) {
				if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
					majiang = BsMjQipaiTool.findMajiangByVal(leftMajiangs, zpMap.get(player.getUserId()));
					if (majiang != null) {
						zpMap.remove(player.getUserId());
						leftMajiangs.remove(majiang);
					}
				}
			}
			// ???????????????????????????
			// ????????????????????? ?????????
			// majiang=majiangt
			// majiang = MajiangHelper.findMajiangByVal(leftMajiangs, 25);
			// leftMajiangs.remove(majiang);
			if (majiang == null) {
				majiang = getLeftMajiang();
			}
		}
		if (majiang != null) {
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + BsMjDisAction.action_moMjiang + "_"
					+ majiang.getId());
			player.moMajiang(majiang);
		}

		processHideMj(player);

		// ????????????
		clearActionSeatMap();
		setGangSeat(0);
		if (disCardRound == 0) {
			return;
		}
		setMoMajiangSeat(player.getSeat());
		List<Integer> arr = player.checkMo(majiang, false);
		if (!arr.isEmpty()) {
			coverAddActionSeat(player.getSeat(), arr);
		}
		MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
		res.setUserId(player.getUserId() + "");
		res.setRemain(getLeftMajiangCount());
		res.setSeat(player.getSeat());
		// boolean playCommand = !player.getGang().isEmpty() && arr.isEmpty();
		logMoMj(player, majiang, arr);
		for (BsMjPlayer seat : seatMap.values()) {
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
		if(arr.isEmpty()){
			if (player.getActionNum(9) ==0) {
				checkBaoTingMsg(player);
			}
		}
		sendTingInfo(player);
	}

	// TODO:??????
	public void calcMoLastSeats(int firstSeat) {
		for (int i = 0; i < getMaxPlayerCount(); i++) {
			BsMjPlayer player = seatMap.get(firstSeat);
			// if(player.isTingPai(-1)){
			// setFristLastMajiangSeat(player.getSeat());
			// addMoLastSeat(player.getSeat());
			// }
			firstSeat = calcNextSeat(firstSeat);
		}
		if (moLastSeats != null && moLastSeats.size() > 0) {
			setFristLastMajiangSeat(moLastSeats.get(0));
			setAskLastMajaingSeat(moLastSeats.get(0));
		}
	}

	/**
	 * ?????????????????????
	 * 
	 * @param seat
	 *            0????????????????????????>0??????????????????????????????????????????
	 * @return ???????????????????????????
	 */
	public void sendAskLastMajiangRes(int seat) {
		if (moLastSeats == null || moLastSeats.size() == 0) {
			return;
		}
		int sendSeat = moLastSeats.get(0);
		if (seat > 0 && sendSeat != seat) {
			return;
		}
		setAskLastMajaingSeat(sendSeat);
		BsMjPlayer player = seatMap.get(sendSeat);
		sendMoLast(player, 1);
	}

	private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<BsMj> majiangs) {
		BsMjResTool.buildPlayRes(builder, player, action, majiangs);
		buildPlayRes1(builder);
	}

	private void buildPlayRes1(PlayMajiangRes.Builder builder) {
		// builder
	}

	/**
	 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	 */
	public void checkBegin(BsMjPlayer player) {
		boolean isBegin = isBegin();
		if (isBegin && !hasBaoTing()) {
			BsMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			List<Integer> actList = bankPlayer.checkMo(null, isBegin);
			if (!actList.isEmpty()) {
				PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
				buildPlayRes(builder, player, BsMjDisAction.action_pass, new ArrayList<>());
				if (!actList.isEmpty()) {
					addActionSeat(bankPlayer.getSeat(), actList);
					builder.addAllSelfAct(actList);
				}
				bankPlayer.writeSocket(builder.build());
			}
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// ??????????????????
			bankPlayer.writeSocket(com.build());
		}
	}

	/**
	 * ???????????????
	 *
	 * @param player
	 * @param majiangs
	 */
	private void hu(BsMjPlayer player, List<BsMj> majiangs, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (huConfirmMap.containsKey(player.getSeat())) {
			return;
		}
		boolean zimo = player.isAlreadyMoMajiang();
		BsMj disMajiang = null;
		BsMjiangHu huBean = null;
		List<BsMj> huMjs = new ArrayList<>();
		int fromSeat = 0;
		boolean isGangShangHu = false;
		if (!zimo) {
			if (moGangHuList.contains(player.getSeat())) {// ?????????
				disMajiang = moGang;
				fromSeat = moMajiangSeat;
				huMjs.add(moGang);
			} else if (isHasGangAction(player.getSeat())) {// ????????? ?????????
				fromSeat = moMajiangSeat;
				Map<Integer, BsMjiangHu> huMap = new HashMap<>();
				List<Integer> daHuMjIds = new ArrayList<>();
				List<Integer> huMjIds = new ArrayList<>();
				for (int majiangId : gangSeatMap.keySet()) {
					BsMjiangHu temp = player.checkHu(BsMj.getMajang(majiangId), disCardRound == 0);
					BsMjTool.checkDahuMax(temp, this, player, BsMj.getMajang(majiangId), zimo);
					if (!temp.isHu()) {
						continue;
					}
					temp.initDahuList();
					huMap.put(majiangId, temp);
					huMjIds.add(majiangId);
					if (temp.isDahu()) {
						daHuMjIds.add(majiangId);
					}
				}
				if (daHuMjIds.size() > 0) {
					// ?????????
					for (int mjId : huMjIds) {
						BsMjiangHu temp = huMap.get(mjId);
						if (moMajiangSeat == player.getSeat()) {
							temp.setGangShangHua(true);
							isGangShangHu = true;
						} else {
							temp.setGangShangPao(true);
						}
						temp.initDahuList();
						if (huBean == null) {
							huBean = temp;
						} else {
							huBean.addToDahu(temp.getDahuList());
							huBean.getShowMajiangs().add(BsMj.getMajang(mjId));
						}
						player.addHuMjId(mjId);
						huMjs.add(BsMj.getMajang(mjId));
					}
				} else if (huMjIds.size() > 0) {
					// ????????????
					for (int mjId : huMjIds) {
						BsMjiangHu temp = huMap.get(mjId);
						if (moMajiangSeat == player.getSeat()) {
							temp.setGangShangHua(true);
							isGangShangHu = true;
						} else {
							temp.setGangShangPao(true);
						}
						temp.initDahuList();
						if (huBean == null) {
							huBean = temp;
						} else {
							huBean.addToDahu(temp.getDahuList());
							huBean.getShowMajiangs().add(BsMj.getMajang(mjId));
						}
						player.addHuMjId(mjId);
						huMjs.add(BsMj.getMajang(mjId));
					}
				} else {
					huBean = new BsMjiangHu();
				}

				if (huBean.isHu()) {
					if (disCardSeat == player.getSeat()) {
						zimo = true;
					}
				}

			} else if (lastMajiang != null) {
				huBean = player.checkHu(lastMajiang, disCardRound == 0);
				BsMjTool.checkDahuMax(huBean, this, player, lastMajiang, zimo);
				if (huBean.isHu()) {
					// if (moLastMajiangSeat == player.getSeat()) {
					// // huBean.setHaidilaoyue(true);
					// } else {
					// huBean.setHaidipao(true);
					// }
					huBean.initDahuList();
				}
				fromSeat = moLastMajiangSeat;
				huMjs.add(lastMajiang);

			} else if (!nowDisCardIds.isEmpty()) {

				disMajiang = nowDisCardIds.get(0);
				fromSeat = disCardSeat;
				huMjs.add(disMajiang);
			}
		} else {
			huMjs.add(player.getHandMajiang().get(player.getHandMajiang().size() - 1));
		}
		// if(isBegin()){
		// //??????
		// if(huBean == null){
		// huBean = player.checkHu(null,true);
		// }
		// if(huBean.isHu()){
		// huBean.setTianhu(true);
		// huBean.initDahuList();
		// }
		// }else if(nowDisCardSeat == lastWinSeat &&
		// seatMap.get(lastWinSeat).getOutPais().size() == 0){
		// //??????
		// if(huBean == null){
		// huBean = player.checkHu(lastMajiang,true);
		// }
		// if(huBean.isHu()){
		// huBean.setDihu(true);
		// huBean.initDahuList();
		// }
		// }

		if (huBean == null) {
			// ??????
			huBean = player.checkHu(disMajiang, disCardRound == 0);
			if (disMajiang == null) {
				if (player.getHuMjIds().size() > 0) {
					disMajiang = BsMj.getMajang(player.getHuMjIds().get(0));
				}
				if (disMajiang == null) {
					disMajiang = player.getLastMoMajiang();
				}
				if (disMajiang == null) {
					disMajiang = nowDisCardIds.get(0);
				}

			}
			BsMjTool.checkDahuMax(huBean, this, player, disMajiang, zimo);
			if (huBean.isHu()) {
				if (huBean.isQuanqiuren() && zimo) {
					huBean.setQuanqiuren(false);
				}

				if (gangSeat > 0) {
					if (moMajiangSeat == player.getSeat()) {
						if (player.getConGangNum() == 2) {
							huBean.setShuangGSH(true);
						} else if (player.getConGangNum() == 3) {
							huBean.setSanGSH(true);
						} else {
							huBean.setGangShangHua(true);
						}

						// isGangShangHu = true;
					} else {
						huBean.setGangShangPao(true);
					}
				}
				huBean.initDahuList();
			}
		}
		if (!huBean.isHu()) {
			return;
		}
		// ???????????????
		if (moGangHuList.contains(player.getSeat())) {
			// ??????????????????????????????
			if (disEventAction != BsMjDisAction.action_buzhang) {
				huBean.setQGangHu(true);
				huBean.initDahuList();
			}
			// ?????????
			BsMjPlayer moGangPlayer = getPlayerByHasMajiang(moGang);
			if (moGangPlayer == null) {
				moGangPlayer = seatMap.get(moMajiangSeat);
			}
			List<BsMj> moGangMajiangs = new ArrayList<>();
			moGangMajiangs.add(moGang);
			moGangPlayer.addOutPais(moGangMajiangs, 0, 0);
			// ?????????????????? ??????????????????????????????
			recordDisMajiang(moGangMajiangs, moGangPlayer);
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + BsMjHelper.toMajiangStrs(majiangs));
		}
		
		
		
		
		if (huBean.getDahuFan() > 0) {
			//
			if (zimo && huBean.isLongZhuaBeiHu()) {
			//	huBean.addFan(1);
			}
			player.setDahu(huBean.getDahuList(), huBean.getDahuFan());
		}
		// if (huBean.getDahuPoint() > 0) {
		// player.setDahu(huBean.getDahuList());
		// if (zimo) {
		// int point = 0;
		// for (BsMjPlayer seatPlayer : seatMap.values()) {
		// if (seatPlayer.getSeat() != player.getSeat()) {
		// point += huBean.getDahuPoint();
		// seatPlayer.changeLostPoint(-huBean.getDahuPoint());
		// }
		// }
		// player.changeLostPoint(point);
		// } else {
		// player.changeLostPoint(huBean.getDahuPoint());
		// seatMap.get(disCardSeat).changeLostPoint(-huBean.getDahuPoint());
		// }
		// }

		if (isGangShangHu) {
			// ????????????????????????????????????????????????????????????
			List<BsMj> gangDisMajiangs = getGangDisMajiangs();
			List<BsMj> chuMjs = new ArrayList<>();
			if (gangDisMajiangs != null && gangDisMajiangs.size() > 0) {
				for (BsMj mj : gangDisMajiangs) {
					if (!huMjs.contains(mj)) {
						chuMjs.add(mj);
					}
				}
			}
			if (chuMjs != null) {
				PlayMajiangRes.Builder chuPaiMsg = PlayMajiangRes.newBuilder();
				buildPlayRes(chuPaiMsg, player, BsMjDisAction.action_chupai, chuMjs);
				chuPaiMsg.setFromSeat(-1);
				broadMsgToAll(chuPaiMsg.build());
				player.addOutPais(chuMjs, BsMjDisAction.action_chupai, player.getSeat());
			}
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, huBean.getShowMajiangs());
		builder.addAllHuArray(player.getDahu());
		if (zimo) {
			builder.setZimo(1);
		}
		builder.setFromSeat(fromSeat);
		// ???
		for (BsMjPlayer seat : seatMap.values()) {
			// ????????????
			seat.writeSocket(builder.build());
		}
		// ??????????????????
		addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());
		
		if(huBean.isGangShangPao()){
			calcPointGangRemain(disCardSeat);
		}
		
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + BsMjHelper.toMajiangStrs(huMjs) + "_"
				+ StringUtil.implode(player.getDahu(), ","));
		if (isCalcOver()) {
			// ?????????????????? ???????????????????????????
			calcOver();
		} else {
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
		}
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @param majiang
	 * @return
	 */
	private BsMjPlayer getPlayerByHasMajiang(BsMj majiang) {
		for (BsMjPlayer player : seatMap.values()) {
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
		// ??????????????????????????????
		if (!huActionList.isEmpty()) {
			over = true;
			BsMjPlayer moGangPlayer = null;
			if (!moGangHuList.isEmpty()) {
				// ??????????????????
				moGangPlayer = getPlayerByHasMajiang(moGang);
				LogUtil.monitor_i("mogang player:" + moGangPlayer.getSeat() + " moGang:" + moGang);
			}
			for (int huseat : huActionList) {
				if (moGangPlayer != null) {
					// ?????????????????????????????? ??????
					if (moGangPlayer.getSeat() == huseat) {
						continue;
					}
				}
				if (!huConfirmMap.containsKey(huseat)) {
					over = false;
					break;
				}
			}
		}

		if (!over) {
			BsMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
			for (int huseat : huActionList) {
				if (huConfirmMap.containsKey(huseat)) {
					if (disCardRound == 0) {
						// ??????
						removeActionSeat(huseat);
					}
					continue;
				}
				PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
				BsMjPlayer seatPlayer = seatMap.get(huseat);
				buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
				List<Integer> actionList = actionSeatMap.get(huseat);
				disBuilder.addAllSelfAct(actionList);
				seatPlayer.writeSocket(disBuilder.build());
			}
		}
		return over;
	}

	// private boolean isCalcOver() {
	// return isCalcOver(null);
	// }

	/**
	 * ?????????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void chiPengGang(BsMjPlayer player, List<BsMj> majiangs, int action) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		logAction(player, action, 0, majiangs, null);
		// if (nowDisCardIds.size() > 1 && !isHasGangAction()) {
		// // ????????????????????????
		// return;
		// }
		List<Integer> huList = getHuSeatByActionMap();
		huList.remove((Object) player.getSeat());
		// if (!huList.isEmpty()) {
		// // ????????????
		// return;
		// }

		if (!checkAction(player, majiangs, new ArrayList<>(), action)) {
			player.writeComMessage(WebSocketMsgType.res_com_code_temp_action_skip);
			return;
		}

		List<BsMj> handMajiang = new ArrayList<>(player.getHandMajiang());
		BsMj disMajiang = null;
		if (isHasGangAction()) {
			for (int majiangId : gangSeatMap.keySet()) {
				if (action == BsMjDisAction.action_chi) {
					List<Integer> majiangIds = BsMjHelper.toMajiangIds(majiangs);
					if (majiangIds.contains(majiangId)) {
						disMajiang = BsMj.getMajang(majiangId);
						gangActedMj = disMajiang;
						handMajiang.add(disMajiang);
						if (majiangs.size() > 1) {
							majiangs.remove(disMajiang);
						}
						break;
					}
				} else {
					BsMj mj = BsMj.getMajang(majiangId);
					if (mj != null && majiangs.get(0).getVal() == mj.getVal()) {
						disMajiang = mj;
						int removeIndex = -1;
						for (int i = 0; i < majiangs.size(); i++) {
							if (majiangs.get(i).getId() == majiangId) {
								removeIndex = i;
							}
						}
						if (removeIndex != -1) {
							majiangs.remove(removeIndex);
						}
					}
				}
			}
			if (disMajiang == null) {
				return;
			}
		} else {
			if (!nowDisCardIds.isEmpty()) {
				disMajiang = nowDisCardIds.get(0);
			}
		}

		int sameCount = 0;
		if (majiangs.size() > 0) {
			sameCount = BsMjHelper.getMajiangCount(majiangs, majiangs.get(0).getVal());
		}
		if (action == BsMjDisAction.action_buzhang) {
			majiangs = BsMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
		} else if (action == BsMjDisAction.action_minggang) {
			// ???????????? ????????????????????????????????????
			majiangs = BsMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
			if (sameCount == 4) {
				// ???4????????????????????????
				action = BsMjDisAction.action_angang;
			}
			// ???????????????

		} else if (action == BsMjDisAction.action_buzhang_an) {
			// ????????????
			majiangs = BsMjHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal());
			sameCount = majiangs.size();
		}
		// /////////////////////
		if (action == BsMjDisAction.action_chi) {
			boolean can = canChi(player, player.getHandMajiang(), majiangs, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == BsMjDisAction.action_peng) {
			boolean can = canPeng(player, majiangs, sameCount, disMajiang);
			if (!can) {
				return;
			}
		} else if (action == BsMjDisAction.action_angang) {
			boolean can = canAnGang(player, majiangs, sameCount, action);
			if (!can) {
				return;
			}
			// if (!player.isTingPai(majiangs.get(0).getVal())) {
			// return;
			// }
		} else if (action == BsMjDisAction.action_minggang) {
			boolean can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
			if (!can) {
				return;
			}
			// if (!player.isTingPai(majiangs.get(0).getVal())) {
			// return;
			// }
			// ???????????????????????????????????????????????????
			if (sameCount == 1 && canGangHu()) {
				if (checkQGangHu(player, majiangs, action)) {
					return;
				}
			}
		} else if (action == BsMjDisAction.action_buzhang) {
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount, action);
			} else {
				can = canMingGang(player, player.getHandMajiang(), majiangs, sameCount, disMajiang);
			}
			if (!can) {
				return;
			}
			// ???????????????????????????????????????????????????
			if (sameCount == 1 && canGangHu()) {
				if (checkQGangHu(player, majiangs, action)) {
					return;
				}
			}
		} else if (action == BsMjDisAction.action_buzhang_an) {
			boolean can = false;
			if (sameCount == 4) {
				can = canAnGang(player, majiangs, sameCount, action);
			}
			if (!can) {
				return;
			}
		} else {
			return;
		}
		calcPoint(player, action, sameCount, majiangs);
		boolean disMajiangMove = false;
		if (disMajiang != null) {
			// ????????????
			if (action == BsMjDisAction.action_minggang && sameCount == 3) {
				// ??????
				disMajiangMove = true;
			} else if (action == BsMjDisAction.action_chi) {
				// ???
				disMajiangMove = true;
			} else if (action == BsMjDisAction.action_peng) {
				// ???
				disMajiangMove = true;
			} else if (action == BsMjDisAction.action_buzhang && sameCount == 3) {
				// ??????????????????
				disMajiangMove = true;
			}
		}
		if (disMajiangMove) {
			if (action == BsMjDisAction.action_chi) {
				majiangs.add(1, disMajiang);// ?????????????????????
			} else {
				majiangs.add(disMajiang);
			}
			builder.setFromSeat(disCardSeat);
			List<BsMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(disMajiang);
			seatMap.get(disCardSeat).removeOutPais(disMajiangs, action);
		}
		chiPengGang(builder, player, majiangs, action);
	}

	private void chiPengGang(PlayMajiangRes.Builder builder, BsMjPlayer player, List<BsMj> majiangs, int action) {
		setIsBegin(false);
		processHideMj(player);

		player.addOutPais(majiangs, action, disCardSeat);
		buildPlayRes(builder, player, action, majiangs);
		List<Integer> removeActList = removeActionSeat(player.getSeat());
		clearGangActionMap();
		clearActionSeatMap();
		setGangSeat(0);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + BsMjHelper.toMajiangStrs(majiangs));
		// ??????????????????
		setNowDisCardSeat(player.getSeat());
		checkClearGangDisMajiang();
		if (action == BsMjDisAction.action_chi || action == BsMjDisAction.action_peng) {
			player.setConGangNum(0);
			 List<Integer> arr = player.checkMo(null, false);
			 if (!arr.isEmpty()) {
			 arr.set(BsMjAction.ZIMO,0);
			 arr.set(BsMjAction.HU,0);
			 addActionSeat(player.getSeat(), arr);
			 }
		}
		for (BsMjPlayer seatPlayer : seatMap.values()) {
			// ????????????
			PlayMajiangRes.Builder copy = builder.clone();
			if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
				copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
			}
			seatPlayer.writeSocket(copy.build());
		}

		// ????????????
		player.setPassMajiangVal(0);
		if (action == BsMjDisAction.action_minggang || action == BsMjDisAction.action_angang) {
			// ?????????????????????
			// gangMoMajiang(player,majiangs.get(0),action);
			// ??????
			moMajiang(player, false);
			gangSeat = player.getSeat();
			player.addConGangNum(1);
		} else if (action == BsMjDisAction.action_buzhang) {
			// ??????
			moMajiang(player, true);
			gangSeat = player.getSeat();
			player.addConGangNum(1);
		} else if (action == BsMjDisAction.action_buzhang_an) {
			// ??????
			moMajiang(player, true);
			gangSeat = player.getSeat();
			player.addConGangNum(1);
		}

		if (action == BsMjDisAction.action_chi || action == BsMjDisAction.action_peng) {
			sendTingInfo(player);
		}

		setDisEventAction(action);
		robotDealAction();
		logAction(player, action, 0, majiangs, removeActList);
		player.changeAction(9, 1);
	}

	private boolean checkQGangHu(BsMjPlayer player, List<BsMj> majiangs, int action) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		Map<Integer, List<Integer>> huListMap = new HashMap<>();
		for (BsMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				continue;
			}
			// ????????????
			List<Integer> hu = seatPlayer.checkDisMajiang(majiangs.get(0));
			if (!hu.isEmpty() && hu.get(0) == 1) {
				addActionSeat(seatPlayer.getSeat(), hu);
				huListMap.put(seatPlayer.getSeat(), hu);
			}
		}

		// ????????????
		if (!huListMap.isEmpty()) {
			setDisEventAction(action);
			setMoGang(majiangs.get(0), new ArrayList<>(huListMap.keySet()));
			buildPlayRes(builder, player, action, majiangs);
			for (Entry<Integer, List<Integer>> entry : huListMap.entrySet()) {
				PlayMajiangRes.Builder copy = builder.clone();
				BsMjPlayer seatPlayer = seatMap.get(entry.getKey());
				copy.addAllSelfAct(entry.getValue());
				seatPlayer.writeSocket(copy.build());
			}
			return true;
		}
		return false;

	}

	public void checkSendGangRes(Player player) {
		if (isHasGangAction()) {
			List<BsMj> moList = getGangDisMajiangs();
			BsMjPlayer disPlayer = seatMap.get(disCardSeat);
			GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
			gangbuilder.setGangId(gangMajiang.getId());
			gangbuilder.setUserId(disPlayer.getUserId() + "");
			gangbuilder.setName(disPlayer.getName() + "");
			gangbuilder.setSeat(disPlayer.getSeat());
			gangbuilder.setRemain(leftMajiangs.size());
			gangbuilder.setReconnect(1);
			gangbuilder.setDice(gangDice);
			gangbuilder.setHasAct(isHasGangAction() ? 1 : 0);
			gangbuilder.setMjNum(moList.size());
			for (BsMj mj : moList) {
				GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
				playBuilder.setMajiangId(mj.getId());
				Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(mj.getId());
				if (seatActionList != null && seatActionList.containsKey(player.getSeat())) {
					playBuilder.addAllSelfAct(seatActionList.get(player.getSeat()));
				}
				gangbuilder.addGangActs(playBuilder);
			}
			if (isHasGangAction(disCardSeat) && player.getSeat() != disCardSeat) {
				// ???????????????????????????????????????????????????????????????
				gangbuilder.clearGangActs();
			}
			player.writeSocket(gangbuilder.build());
		}
	}

	/**
	 * ????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void chuPai(BsMjPlayer player, List<BsMj> majiangs, int action) {
		if (majiangs.size() != 1) {
			return;
		}
		if (!player.isAlreadyMoMajiang()) {
			// ???????????????
			return;
		}
		if (!tempActionMap.isEmpty()) {
			LogUtil.e(player.getName() + "???????????????????????????");
			clearTempAction();
		}
		if (!player.getGang().isEmpty()) {
			// ??????????????????
			// if (player.getLastMoMajiang().getId() != majiangs.get(0).getId())
			// {
			// return;
			// }
		}
		if (!actionSeatMap.isEmpty()) {// ??????????????????????????????
			guo(player, null, BsMjDisAction.action_pass);
		}
		if (!actionSeatMap.isEmpty()) {
			player.writeComMessage(WebSocketMsgType.res_code_mj_dis_err, action, majiangs.get(0).getId());
			return;
		}
		
		BsMj lasmj = player.getLastMoMajiang();
		
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		// ????????????
		clearActionSeatMap();
		clearGangActionMap();
		player.setConGangNum(0);
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(majiangs, player);
		player.addOutPais(majiangs, action, player.getSeat());
		player.clearPassHu();
		logAction(player, action, 0, majiangs, null);

		for (BsMjPlayer seat : seatMap.values()) {
			List<Integer> list = new ArrayList<>();
			if (seat.getUserId() != player.getUserId()) {
				list = seat.checkDisMajiang(majiangs.get(0));
				if (list.contains(1)) {
					addActionSeat(seat.getSeat(), list);
					logChuPaiActList(seat, majiangs.get(0), list);
				}
			}
		}
		// ?????????????????????
		if (majiangs.get(0).isFeng()) {
			player.changeAction(8, 1);
			if (player.getActionNum(8) >= 10) {
				List<Integer> list = new ArrayList<>();
				list.add(1);
				addActionSeat(player.getSeat(), list);
			}
		} else {
			player.setAction(8, 0);
		}

		player.changeAction(9, 1);

		
		if(player.getBaoTingS()==1){
			//??????????????????
			if(!checkBaoting(player)||(majiangs.get(0).getId()!=lasmj.getId()&&player.getActionNum(9) > 1)||player.isChiPengGang()){
				player.setBaoTingS(2);
				ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_bs_Baoting,
						player.getSeat(), player.getBaoTingS());
				for (BsMjPlayer seatPlayer : seatMap.values()) {
					seatPlayer.writeSocket(com.build());
				}
			}
			
		}
		
		
		setDisEventAction(action);
		sendDisMajiangAction(builder);
		// ????????????
		player.setPassMajiangVal(0);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + BsMjHelper.toMajiangStrs(majiangs));
		setIsBegin(false);
		// ??????????????????
		checkMo();
	}

	public List<Integer> getPengGangSeatByActionMap() {
		List<Integer> huList = new ArrayList<>();
		for (int seat : actionSeatMap.keySet()) {
			List<Integer> actionList = actionSeatMap.get(seat);
			if (actionList.get(0) == 3) {
				// ???
				huList.add(seat);
			}

		}
		return huList;
	}

	public List<Integer> getHuSeatByActionMap() {
		List<Integer> huList = new ArrayList<>();
		for (int seat : actionSeatMap.keySet()) {
			List<Integer> actionList = actionSeatMap.get(seat);
			if (actionList.get(BsMjAction.HU) == 1 || actionList.get(BsMjAction.ZIMO) == 1) {
				// ???
				huList.add(seat);
			}

		}
		return huList;
	}

	private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
		// ????????????????????? ?????????
		// ??????????????????
		buildPlayRes1(builder);
		List<Integer> huList = getHuSeatByActionMap();
		if (huList.size() > 0) {
			// ?????????,?????????
			for (BsMjPlayer seatPlayer : seatMap.values()) {
				PlayMajiangRes.Builder copy = builder.clone();
				List<Integer> actionList;
				// ???????????????????????????????????????????????????????????????????????????????????????
				if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
					// if (huList.contains(seatPlayer.getSeat())) {
					actionList = actionSeatMap.get(seatPlayer.getSeat());
				} else {
					// ?????????????????????
					actionList = new ArrayList<>();
				}
				copy.addAllSelfAct(actionList);
				seatPlayer.writeSocket(copy.build());
			}

		} else {
			// ??????????????????????????????
			for (BsMjPlayer seat : seatMap.values()) {
				PlayMajiangRes.Builder copy = builder.clone();
				List<Integer> actionList;
				if (actionSeatMap.containsKey(seat.getSeat())) {
					actionList = actionSeatMap.get(seat.getSeat());
				} else {
					actionList = new ArrayList<>();
				}
				copy.addAllSelfAct(actionList);
				seat.writeSocket(copy.build());
			}
		}

	}

	private void err(BsMjPlayer player, int action, String errMsg) {
		LogUtil.e("play:tableId-->" + id + " playerId-->" + player.getUserId() + " action-->" + action + " err:"
				+ errMsg);
	}

	/**
	 * ??????
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	public synchronized void playCommand(BsMjPlayer player, List<BsMj> majiangs, int action) {
		if (!moGangHuList.isEmpty()) {// ???????????????
			if (!moGangHuList.contains(player.getSeat())) {
				return;
			}
		}

		if (BsMjDisAction.action_hu == action) {
			hu(player, majiangs, action);
			return;
		}
		// ???????????????????????????
		if (!isHasGangAction() && action != BsMjDisAction.action_minggang && action != BsMjDisAction.action_buzhang)
			if (!player.getHandMajiang().containsAll(majiangs)) {
				err(player, action, "?????????????????????" + majiangs);
				return;
			}
		changeDisCardRound(1);
		if (action == BsMjDisAction.action_pass) {
			guo(player, majiangs, action);
		} else if (action == BsMjDisAction.action_moMjiang) {
		} else if (action != 0) {

			chiPengGang(player, majiangs, action);
		} else {

			chuPai(player, majiangs, action);
		}

	}

	/**
	 * ???????????????(?????????)
	 *
	 * @param player
	 * @param action
	 */
	public synchronized void moLastMajiang(BsMjPlayer player, int action) {
		if (getLeftMajiangCount() != 1) {
			return;
		}
		if (player.getSeat() != askLastMajaingSeat) {
			return;
		}

		if (action == BsMjDisAction.action_passmo) {
			// ???????????????????????????res
			sendMoLast(player, 0);
			removeMoLastSeat(player.getSeat());
			if (moLastSeats == null || moLastSeats.size() == 0) {
				calcOver();
				return;
			}
			sendAskLastMajiangRes(0);
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + BsMjDisAction.action_pass + "_");
		} else {
			sendMoLast(player, 0);
			clearMoLastSeat();
			clearActionSeatMap();
			setMoLastMajiangSeat(player.getSeat());
			BsMj majiang = getLeftMajiang();
			addPlayLog(disCardRound + "_" + player.getSeat() + "_" + BsMjDisAction.action_moLastMjiang + "_"
					+ majiang.getId());
			setMoMajiangSeat(player.getSeat());
			player.setPassMajiangVal(0);
			setLastMajiang(majiang);
			setDisCardSeat(player.getSeat());

			// /////////////////////////////////////////////
			// ?????????????????????

			// /////////////////////////////////////////

			List<BsMj> disMajiangs = new ArrayList<>();
			disMajiangs.add(majiang);

			MoMajiangRes.Builder moRes = MoMajiangRes.newBuilder();
			moRes.setUserId(player.getUserId() + "");
			moRes.setRemain(getLeftMajiangCount());
			moRes.setSeat(player.getSeat());

			// ???????????????????????????
			List<Integer> selfActList = player.checkDisMajiang(majiang);
			player.moMajiang(majiang);
			selfActList = BsMjAction.keepHu(selfActList);
			if (selfActList != null && !selfActList.isEmpty()) {
				addActionSeat(player.getSeat(), selfActList);
			}
			for (BsMjPlayer seatPlayer : seatMap.values()) {
				if (seatPlayer.getUserId() == player.getUserId()) {
					MoMajiangRes.Builder selfMsg = moRes.clone();
					selfMsg.addAllSelfAct(selfActList);
					selfMsg.setMajiangId(majiang.getId());
					player.writeSocket(selfMsg.build());
				} else {
					MoMajiangRes.Builder otherMsg = moRes.clone();
					seatPlayer.writeSocket(otherMsg.build());
				}
			}

			// ????????????
			if (BsMjAction.hasHu(selfActList)) {
				// ???????????????
				// hu(player, null, BsMjDisAction.action_moLastMjiang_hu);
				return;
			} else {
				chuLastPai(player);
			}
			// for (int seat : actionSeatMap.keySet()) {
			// hu(seatMap.get(seat), null, action);
			// }
		}

	}

	private void chuLastPai(BsMjPlayer player) {
		BsMj majiang = lastMajiang;
		List<BsMj> disMajiangs = new ArrayList<>();
		disMajiangs.add(majiang);
		PlayMajiangRes.Builder chuRes = BsMjResTool.buildPlayRes(player, BsMjDisAction.action_chupai, disMajiangs);
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + BsMjDisAction.action_chupai + "_"
				+ BsMjHelper.toMajiangStrs(disMajiangs));
		setNowDisCardIds(disMajiangs);
		setNowDisCardSeat(calcNextSeat(player.getSeat()));
		recordDisMajiang(disMajiangs, player);
		player.addOutPais(disMajiangs, BsMjDisAction.action_chupai, player.getSeat());
		player.clearPassHu();
		for (BsMjPlayer seatPlayer : seatMap.values()) {
			if (seatPlayer.getUserId() == player.getUserId()) {
				seatPlayer.writeSocket(chuRes.clone().build());
				continue;
			}
			List<Integer> otherActList = seatPlayer.checkDisMajiang(majiang);
			otherActList = BsMjAction.keepHu(otherActList);
			PlayMajiangRes.Builder msg = chuRes.clone();
			if (BsMjAction.hasHu(otherActList)) {
				addActionSeat(seatPlayer.getSeat(), otherActList);
				msg.addAllSelfAct(otherActList);
			}
			seatPlayer.writeSocket(msg.build());
		}
		if (actionSeatMap.isEmpty()) {
			calcOver();
		}
	}

	private void passMoHu(BsMjPlayer player, List<BsMj> majiangs, int action) {
		if (!moGangHuList.contains(player.getSeat())) {
			return;
		}

		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		removeActionSeat(player.getSeat());
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + BsMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		player.setPassMajiangVal(nowDisCardIds.get(0).getVal());

		BsMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
		if (moGangHuList.isEmpty()) {
			majiangs = new ArrayList<>();
			majiangs.add(moGang);
			calcPoint(moGangPlayer, BsMjDisAction.action_minggang, 1, majiangs);
			builder = PlayMajiangRes.newBuilder();
			chiPengGang(builder, moGangPlayer, majiangs, BsMjDisAction.action_minggang);
		}

	}

	/**
	 * guo
	 *
	 * @param player
	 * @param majiangs
	 * @param action
	 */
	private void guo(BsMjPlayer player, List<BsMj> majiangs, int action) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return;
		}
		if (!moGangHuList.isEmpty()) {
			// ???????????????????????????
			passMoHu(player, majiangs, action);
			return;
		}
		List<Integer> removeActionList = removeActionSeat(player.getSeat());
		int xiaoHu = BsMjAction.getFirstXiaoHu(removeActionList);
		logAction(player, action, xiaoHu, majiangs, removeActionList);
		boolean isBegin = isBegin();
	

		if (moLastMajiangSeat == player.getSeat()) {
			// ???????????????????????????????????????????????????
			chuLastPai(player);
			return;
		}
		checkClearGangDisMajiang();
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		buildPlayRes(builder, player, action, majiangs);
		builder.setSeat(nowDisCardSeat);
		player.writeSocket(builder.build());
		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + BsMjHelper.toMajiangStrs(majiangs));
		if (isCalcOver()) {
			calcOver();
			return;
		}
		if (BsMjAction.hasHu(removeActionList) && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			// ??????
			player.passHu(nowDisCardIds.get(0).getVal());
		}

		if (removeActionList.get(0) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
		}
		if (removeActionList.get(1) == 1 && disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
			player.setPassPengMajVal(nowDisCardIds.get(0).getVal());
		}
		
		
		
		if (!actionSeatMap.isEmpty()) {
			BsMjPlayer disCSMajiangPlayer = seatMap.get(disCardSeat);
			PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
			buildPlayRes(disBuilder, disCSMajiangPlayer, 0, null);
			for (int seat : actionSeatMap.keySet()) {
				List<Integer> actionList = actionSeatMap.get(seat);
				PlayMajiangRes.Builder copy = disBuilder.clone();
				copy.addAllSelfAct(new ArrayList<>());
				if (actionList != null && !tempActionMap.containsKey(seat)) {
					if (actionList != null) {
						copy.addAllSelfAct(actionList);
					}
				}
				BsMjPlayer seatPlayer = seatMap.get(seat);
				seatPlayer.writeSocket(copy.build());
			}
		}
		
		

		/*
		 * if (player.isAlreadyMoMajiang() && !player.getGang().isEmpty()) { //
		 * ????????????????????? List<BsMj> disMjiang = new ArrayList<>();
		 * disMjiang.add(player.getLastMoMajiang()); chuPai(player, disMjiang,
		 * 0); }
		 */

		if (isBegin && !hasBaoTing() && player.getSeat() == lastWinSeat) {
			// ???????????????????????????????????????
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);
			player.writeSocket(com.build());
		} else {
			checkBegin(player);
		}
		
		
		boolean chekcmmo = true;
		if (player.isAlreadyMoMajiang() && player.getActionNum(9) ==0) {
			boolean baoting = checkBaoTingMsg(player);
			if(baoting){
				chekcmmo = false;
			}
		}
		

		// ?????? ???????????????????????????????????????????????????????????????
		refreshTempAction(player);
		if(chekcmmo){
			checkMo();
		}

		if (player.isAlreadyMoMajiang()) {
			sendTingInfo(player);
		}
	}

	private void calcPoint(BsMjPlayer player, int action, int sameCount, List<BsMj> majiangs) {

		int lostPoint = 0;
		// int getPoint = 0;
		int[] seatPointArr = new int[getMaxPlayerCount() + 1];
		if (action == BsMjDisAction.action_peng) {
			return;

		} else if (action == BsMjDisAction.action_angang) {
			// ??????????????????????????????2???
			lostPoint = -2;
			// getPoint = 2 * (getMaxPlayerCount() - 1);
			player.changeGangRemainArr(BsMjDisAction.action_angang,0);
		} else if (action == BsMjDisAction.action_minggang) {
			if (sameCount == 1) {
				// ????????????????????????????????????1???
				// ???????????????3???

				if (player.isPassGang(majiangs.get(0))) {
					// ???????????? ???????????????????????? ???????????? ???????????????
					return;
				}
				
				int fangSeat = player.getFangPengSeat(majiangs.get(0).getVal());
				if(fangSeat>0){
					calFangGangFen(player, seatPointArr,fangSeat);
				}else{
					
					lostPoint = -1;
				}
				// getPoint = 1 * (getMaxPlayerCount() - 1);
			} else if (sameCount == 3) {
				// ??????
				calFangGangFen(player, seatPointArr,disCardSeat);
			}
		}

		if (lostPoint != 0) {
			int totalPoint = 0;
			int lostPoint2 = lostPoint;
			for (BsMjPlayer seat : seatMap.values()) {
				if (seat.getUserId() != player.getUserId()) {
					if (getBuyPoint() > 2) {
						lostPoint2 = (seat.getPiaoPoint() + player.getPiaoPoint() + 1) * lostPoint;
					}
					totalPoint += lostPoint2;
					seat.changeLostPoint(lostPoint2);
					seat.changeGangRemainArr(-lostPoint2, 0);
					seatPointArr[seat.getSeat()] = lostPoint2;
				}
			}
			player.changeLostPoint(-totalPoint);

		}

		// String seatPointStr = "";
		// for (int i = 1; i <= getMaxPlayerCount(); i++) {
		// seatPointStr += seatPointArr[i] + ",";
		// }
		// seatPointStr = seatPointStr.substring(0, seatPointStr.length() - 1);
		// ComMsg.ComRes.Builder res =
		// SendMsgUtil.buildComRes(WebSocketMsgType.res_code_gangFen,
		// seatPointStr);
		// GeneratedMessage msg = res.build();
		// broadMsgToAll(msg);

	}

	private void calFangGangFen(BsMjPlayer player, int[] seatPointArr,int disSeat) {
		BsMjPlayer disPlayer = seatMap.get(disSeat);
		
		// disPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index13,
		// 1);
		int point = 1;
		if (getBuyPoint() > 2) {
			point = disPlayer.getPiaoPoint() + player.getPiaoPoint() + 1;
		}
		disPlayer.changeLostPoint(-(point));
		seatPointArr[disPlayer.getSeat()] = -point;
		player.changeLostPoint(point);
		seatPointArr[player.getSeat()] = point;
		player.changeGangRemainArr(point,disPlayer.getSeat());
	}
	
	private void calcPointGangRemain(int gangSeat) {
		
		BsMjPlayer player = seatMap.get(gangSeat);
		if(player==null){
			return;
		}
		if(huConfirmMap.size()>1){
			return;
		}
		int[] gangRemainArr = player.getGangRemainArr();
		
//		int gangAction = gangRemainArr[0];
		int fangGangSeat = gangRemainArr[1];
		
		
		if(fangGangSeat>0){
			BsMjPlayer fPlayer = seatMap.get(fangGangSeat);
			if(fPlayer==null){
				return;
			}
			int gangf = gangRemainArr[0];
			fPlayer.changeLostPoint(gangf);
			player.changeLostPoint(-gangf);
			
		}else{
			
			int totalPoint = 0;
			for (BsMjPlayer seat : seatMap.values()) {
				if (seat.getUserId() != player.getUserId()) {
					
					int[] gangRemainArr2 = seat.getGangRemainArr();
//					int gangAction = gangRemainArr[0];
					int gangf = gangRemainArr2[0];
					
					totalPoint += gangf;
					seat.changeLostPoint(gangf);
				}
			}
			player.changeLostPoint(-totalPoint);
		}
		

	}


	private void recordDisMajiang(List<BsMj> majiangs, BsMjPlayer player) {
		setNowDisCardIds(majiangs);
		setDisCardSeat(player.getSeat());
	}

	public List<BsMj> getNowDisCardIds() {
		return nowDisCardIds;
	}

	public void setDisEventAction(int disAction) {
		this.disEventAction = disAction;
		changeExtend();
	}

	public void setNowDisCardIds(List<BsMj> nowDisCardIds) {
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
				BsMjPlayer player = seatMap.get(seat);
				if (player != null && player.isRobot()) {
					// ????????????????????????????????????
					List<Integer> actionList = actionSeatMap.get(seat);
					if (actionList == null) {
						continue;
					}
					List<BsMj> list = new ArrayList<>();
					if (!nowDisCardIds.isEmpty()) {
						list = BsMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
					}
					if (actionList.get(0) == 1) {
						// ???
						playCommand(player, new ArrayList<BsMj>(), BsMjDisAction.action_hu);

					} else if (actionList.get(3) == 1) {
						playCommand(player, list, BsMjDisAction.action_angang);

					} else if (actionList.get(2) == 1) {
						playCommand(player, list, BsMjDisAction.action_minggang);

					} else if (actionList.get(1) == 1) {
						playCommand(player, list, BsMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						playCommand(player, player.getCanChiMajiangs(nowDisCardIds.get(0)), BsMjDisAction.action_chi);

					} else {
						System.out.println("---------->" + JacksonUtil.writeValueAsString(actionList));
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
		if (isTest()) {
			// for (BsMjPlayer player : seatMap.values()) {
			// if (player.isRobot() && player.canXiaoHu()) {
			// playCommand(player, new ArrayList<BsMj>(),
			// BsMjDisAction.action_xiaohu);
			// }
			// }

			int nextseat = getNextActionSeat();
			BsMjPlayer next = seatMap.get(nextseat);
			if (next != null && next.isRobot()) {
				List<Integer> actionList = actionSeatMap.get(next.getSeat());
				int xiaoHuAction = -1;
				if (actionList != null) {
					List<BsMj> list = null;
					if (actionList.get(0) == 1) {
						// ???
						playCommand(next, new ArrayList<BsMj>(), BsMjDisAction.action_hu);

					} else if ((xiaoHuAction = BsMjAction.getFirstXiaoHu(actionList)) > 0) {

						playCommand(next, new ArrayList<BsMj>(), BsMjDisAction.action_pass);

					} else if (actionList.get(3) == 1) {
						// ???????????????
						Map<Integer, Integer> handMap = BsMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// ????????????
								list = BsMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						playCommand(next, list, BsMjDisAction.action_angang);

					} else if (actionList.get(5) == 1) {
						// ???????????????
						Map<Integer, Integer> handMap = BsMjHelper.toMajiangValMap(next.getHandMajiang());
						for (Entry<Integer, Integer> entry : handMap.entrySet()) {
							if (entry.getValue() == 4) {
								// ????????????
								list = BsMjHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
							}
						}
						if (list == null) {
							if (next.isAlreadyMoMajiang()) {
								list = BsMjQipaiTool.getVal(next.getHandMajiang(), next.getLastMoMajiang().getVal());

							} else {
								list = BsMjQipaiTool.getVal(next.getHandMajiang(), nowDisCardIds.get(0).getVal());
								list.add(nowDisCardIds.get(0));
							}
						}

						playCommand(next, list, BsMjDisAction.action_buzhang);

					} else if (actionList.get(2) == 1) {
						Map<Integer, Integer> pengMap = BsMjHelper.toMajiangValMap(next.getPeng());
						for (BsMj handMajiang : next.getHandMajiang()) {
							if (pengMap.containsKey(handMajiang.getVal())) {
								// ?????????
								list = new ArrayList<>();
								list.add(handMajiang);
								playCommand(next, list, BsMjDisAction.action_minggang);
								break;
							}
						}

					} else if (actionList.get(1) == 1) {
						// playCommand(next, list, BsMjDisAction.action_peng);

					} else if (actionList.get(4) == 1) {
						BsMj majiang = null;
						List<BsMj> chiList = null;
						if (nowDisCardIds.size() == 1) {
							majiang = nowDisCardIds.get(0);
							chiList = next.getCanChiMajiangs(majiang);
						} else {
							for (int majiangId : gangSeatMap.keySet()) {
								Map<Integer, List<Integer>> actionMap = gangSeatMap.get(majiangId);
								List<Integer> action = actionMap.get(next.getSeat());
								if (action != null) {
									// List<Integer> disActionList =
									// MajiangDisAction.parseToDisActionList(action);
									if (action.get(4) == 1) {
										majiang = BsMj.getMajang(majiangId);
										chiList = next.getCanChiMajiangs(majiang);
										chiList.add(majiang);
										break;
									}

								}

							}

						}

						playCommand(next, chiList, BsMjDisAction.action_chi);

					} else {
						System.out.println("!!!!!!!!!!" + JacksonUtil.writeValueAsString(actionList));

					}

				} else {
					int maJiangId = BsMjRobotAI.getInstance().outPaiHandle(0, next.getHandPais(),
							new ArrayList<Integer>());
					List<BsMj> majiangList = BsMjHelper.toMajiang(Arrays.asList(maJiangId));
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
			// ??????????????????
			int masterseat = playerMap.get(masterId).getSeat();
			setLastWinSeat(masterseat);
		}
		setDisCardSeat(lastWinSeat);
		setNowDisCardSeat(lastWinSeat);
		setMoMajiangSeat(lastWinSeat);
		List<Integer> copy = new ArrayList<>(BsMjConstants.baoshan_mjList);
		if (youfeng == 1) {
			copy.addAll(BsMjConstants.feng_mjList);
		}
		addPlayLog(copy.size() + "");
		List<List<BsMj>> list;
		if (zp == null) {
			list = BsMjTool.fapai(copy, getMaxPlayerCount());
		} else {
			list = BsMjTool.fapai(copy, getMaxPlayerCount(), zp);
		}
		int i = 1;
		List<Integer> removeIndex = new ArrayList<>();
		for (BsMjPlayer player : playerMap.values()) {
			player.changeState(player_state.play);
			if (player.getSeat() == lastWinSeat) {
				player.dealHandPais(list.get(0));
				removeIndex.add(0);
				continue;
			}
			player.dealHandPais(list.get(i));
			removeIndex.add(i);
			i++;
		}

		// ??????????????????
		List<BsMj> leftMjs = new ArrayList<>();
		// ???????????????????????????????????????
		for (int j = 0; j < list.size(); j++) {
			if (!removeIndex.contains(j)) {
				leftMjs.addAll(list.get(j));
			}
		}
		setLeftMajiangs(leftMjs);
	}

	/**
	 * ???????????????????????????
	 *
	 * @param leftMajiangs
	 */
	public void setLeftMajiangs(List<BsMj> leftMajiangs) {
		if (leftMajiangs == null) {
			this.leftMajiangs.clear();
		} else {
			this.leftMajiangs = leftMajiangs;

		}
		dbParamMap.put("leftPais", JSON_TAG);
	}

	/**
	 * ?????????????????????
	 *
	 * @return
	 */
	public BsMj getLeftMajiang() {
		if (this.leftMajiangs.size() > 0) {
			BsMj majiang = this.leftMajiangs.remove(0);
			dbParamMap.put("leftPais", JSON_TAG);
			return majiang;
		}
		return null;
	}

	/**
	 * ?????????????????????
	 *
	 * @return
	 */
	public int getLeftMajiangCount() {
		return this.leftMajiangs.size();
		// return 1;
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
	 * ??????????????????????????????????????????????????????
	 *
	 * @return
	 */
	public int getNextActionSeat() {
		if (actionSeatMap.isEmpty()) {
			return getNextDisCardSeat();

		} else {
			int seat = 0;
			for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
				if (seat == 0) {
					seat = entry.getKey();
				}
				if (entry.getValue().get(0) == 1) {// ???
					return entry.getKey();
				}
				if (entry.getValue().get(2) == 1) {// ???
					return entry.getKey();
				}
				if (entry.getValue().get(1) == 1) {// ???
					return entry.getKey();
				}
				if (entry.getValue().get(4) == 1) {// ???
					return entry.getKey();
				}
			}
			return seat;
		}
	}

	//
	// private int getNearSeat(int nowSeat, List<Integer> seatList) {
	// if (seatList.contains(nowSeat)) {
	// // ???????????????????????????
	// return nowSeat;
	// }
	// for (int i = 0; i < 3; i++) {
	// int seat = calcNextSeat(nowSeat);
	// if (seatList.contains(seat)) {
	// return seat;
	// }
	// nowSeat = seat;
	// }
	// return 0;
	// }

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

	public Map<Integer, BsMjPlayer> getSeatMap2() {
		return seatMap;
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
		res.setLastWinSeat(lastWinSeat);
		res.setMasterId(masterId + "");
		res.addExt(payType); // 0
		res.addExt(getConifg(0)); // 1
		res.addExt(calcBird); // 2
		res.addExt(birdNum); // 3
		res.addExt(gpsWarn); // 4
		res.addExt(youfeng); // 5
		res.addExt(yitiaolong); // 6
		res.addExt(baoting); // 7
		res.addExt(siguiyi); // 8
		res.addExt(buyPoint); // 15
		res.addExt(isCalcBanker); // 16
		res.addExt(isBegin() ? 1 : 0); // 17
		res.addExt(isAutoPlay); // 17

		res.addCreditConfig(creditMode); // 0
		res.addCreditConfig(creditJoinLimit); // 1
		res.addCreditConfig(creditDissLimit); // 2
		res.addCreditConfig(creditDifen); // 3
		res.addCreditConfig(creditCommission); // 4
		res.addCreditConfig(creditCommissionMode1); // 5
		res.addCreditConfig(creditCommissionMode2); // 6
		res.addCreditConfig(creditCommissionLimit); // 7

		res.addStrExt(StringUtil.implode(moTailPai, ",")); // 0
		res.setDealDice(dealDice);
		res.setRenshu(getMaxPlayerCount());
		if (leftMajiangs != null) {
			res.setRemain(leftMajiangs.size());
		} else {
			res.setRemain(0);
		}
		List<PlayerInTableRes> players = new ArrayList<>();
		for (BsMjPlayer player : playerMap.values()) {
			PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
			if (player.getUserId() == userId || showMjSeat.contains(player.getSeat())) {
				playerRes.addAllHandCardIds(player.getHandPais());
			}
			if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
				playerRes.addAllOutCardIds(BsMjHelper.toMajiangIds(nowDisCardIds));
			}
			playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
			if (!isHasGangAction(player.getSeat()) && actionSeatMap.containsKey(player.getSeat())
					&& !huConfirmMap.containsKey(player.getSeat())) {
				if (!tempActionMap.containsKey(player.getSeat())) {// ????????????????????????
																	// ?????????????????????????????????
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
		}
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

	@Override
	public Map<Long, Player> getPlayerMap() {
		Object o = playerMap;
		return (Map<Long, Player>) o;
	}

	@Override
	protected void initNext1() {
		clearHuList();
		clearActionSeatMap();
		clearGangActionMap();
		setGangSeat(0);
		setLeftMajiangs(null);
		setNowDisCardIds(null);
		clearMoGang();
		clearGangDisMajiangs();
		setAskLastMajaingSeat(0);
		setFristLastMajiangSeat(0);
		setMoLastMajiangSeat(0);
		setDisEventAction(0);
		setLastMajiang(null);
		clearTempAction();
		clearShowMjSeat();
		clearMoLastSeat();
		setDealDice(0);
		clearMoTailPai();
		readyTime = 0;
	}

	@Override
	public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
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
		info.setConfig(objects[1].toString());
		TableDao.getInstance().save(info);
		loadFromDB(info);
		// int birdNum = (int) objects[0];
		// if (birdNum > 2) {
		// birdNum = 2;
		// }
		setBirdNum(2);
		// setIsCalcBanker((int) objects[2]);
		// setCalcBird((int) objects[3]);
		setIsCalcBanker(1);
		setCalcBird(2);
	}

	private Map<Integer, BsMjTempAction> loadTempActionMap(String json) {
		Map<Integer, BsMjTempAction> map = new ConcurrentHashMap<>();
		if (json == null || json.isEmpty())
			return map;
		JSONArray jsonArray = JSONArray.parseArray(json);
		for (Object val : jsonArray) {
			String str = val.toString();
			BsMjTempAction tempAction = new BsMjTempAction();
			tempAction.initData(str);
			map.put(tempAction.getSeat(), tempAction);
		}
		return map;
	}

	private void clearTempAction() {
		tempActionMap.clear();
		changeExtend();
	}

	public List<Integer> removeActionSeat(int seat) {
		List<Integer> actionList = actionSeatMap.remove(seat);
		if (moGangHuList.contains(seat)) {
			removeMoGang(seat);
		}
		removeGangActionSeat(0, seat);
		saveActionSeatMap();
		return actionList;
	}

	public boolean isHasGangAction() {
		boolean has = false;
		if (gangSeatMap.isEmpty()) {
			has = false;
		}
		for (Map<Integer, List<Integer>> actionList : gangSeatMap.values()) {
			if (!actionList.isEmpty()) {
				has = true;
				break;
			}
		}
		return has;
	}

	public boolean isHasGangAction(int seat) {
		boolean has = false;
		for (Map<Integer, List<Integer>> actionMap : gangSeatMap.values()) {
			if (!actionMap.isEmpty() && actionMap.containsKey(seat)) {
				has = true;
				break;
			}
		}
		return has;
	}

	public boolean isHasGangAction(int majiang, int seat) {
		if (gangSeatMap.containsKey(majiang)) {
			if (gangSeatMap.get(majiang).containsKey(seat)) {
				return true;
			}
		}
		return false;
	}

	public void removeGangActionSeat(int majiangId, int seat) {
		if (majiangId != 0) {
			Map<Integer, List<Integer>> actionMap = gangSeatMap.get(majiangId);
			if (actionMap != null) {
				actionMap.remove(seat);
				saveActionSeatMap();

			}
		} else {
			for (Map<Integer, List<Integer>> actionMap : gangSeatMap.values()) {
				actionMap.remove(seat);
			}
			saveActionSeatMap();
		}

	}

//	public void addGangActionSeat(int majiang, int seat, List<Integer> actionList) {
//		Map<Integer, List<Integer>> actionMap;
//		if (gangSeatMap.containsKey(majiang)) {
//			actionMap = gangSeatMap.get(majiang);
//		} else {
//			actionMap = new HashMap<>();
//			gangSeatMap.put(majiang, actionMap);
//		}
//		if (!actionList.isEmpty()) {
//			actionMap.put(seat, actionList);
//
//		}
//		saveActionSeatMap();
//	}

	public void clearGangActionMap() {
		if (!gangSeatMap.isEmpty()) {
			gangSeatMap.clear();
			saveActionSeatMap();
		}
	}

	public void coverAddActionSeat(int seat, List<Integer> actionlist) {
		actionSeatMap.put(seat, actionlist);
		addPlayLog(disCardRound + "_" + seat + "_" + BsMjDisAction.action_hasAction + "_"
				+ StringUtil.implode(actionlist));
		saveActionSeatMap();
	}

	public void addActionSeat(int seat, List<Integer> actionlist) {
		if (actionSeatMap.containsKey(seat)) {
			List<Integer> a = actionSeatMap.get(seat);
			DataMapUtil.appendList(a, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + BsMjDisAction.action_hasAction + "_" + StringUtil.implode(a));
		} else {
			actionSeatMap.put(seat, actionlist);
			addPlayLog(disCardRound + "_" + seat + "_" + BsMjDisAction.action_hasAction + "_"
					+ StringUtil.implode(actionlist));
		}
		saveActionSeatMap();
	}

	public void clearActionSeatMap() {
		if (!actionSeatMap.isEmpty()) {
			actionSeatMap.clear();
			saveActionSeatMap();
		}
	}

	/**
	 * ????????????????????????
	 *
	 * @return
	 */
	public boolean canHuXiaohu() {
		for (List<Integer> list : actionSeatMap.values()) {
			List<Integer> xiaoHuActions = list.subList(6, 14);
			if (xiaoHuActions.contains(1)) {
				return true;
			}
		}
		return false;
	}

	public void clearHuList() {
		huConfirmMap.clear();
		changeExtend();
	}

	public void addHuList(int seat, int majiangId) {
		if (!huConfirmMap.containsKey(seat)) {
			huConfirmMap.put(seat, majiangId);

		}
		changeExtend();
	}

	public void saveActionSeatMap() {
		dbParamMap.put("nowAction", JSON_TAG);
	}

	@Override
	protected void initNowAction(String nowAction) {
		JsonWrapper wrapper = new JsonWrapper(nowAction);
		String val1 = wrapper.getString(1);
		if (!StringUtils.isBlank(val1)) {
			actionSeatMap = DataMapUtil.toListMap(val1);

		}
		String val2 = wrapper.getString(2);
		if (!StringUtils.isBlank(val2)) {
			gangSeatMap = DataMapUtil.toListMapMap(val2);

		}
	}

	@Override
	protected void loadFromDB1(TableInf info) {
		if (!StringUtils.isBlank(info.getNowDisCardIds())) {
			nowDisCardIds = BsMjHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
		}

		if (!StringUtils.isBlank(info.getLeftPais())) {
			leftMajiangs = BsMjHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
		}

	}

	// @Override
	// public void initExtend(String info) {
	// if (StringUtils.isBlank(info)) {
	// return;
	// }
	// JsonWrapper wrapper = new JsonWrapper(info);
	// for (BsMjPlayer player : seatMap.values()) {
	// player.initExtend(wrapper.getString(player.getSeat()));
	// }
	// String huListstr = wrapper.getString(5);
	// if (!StringUtils.isBlank(huListstr)) {
	// huConfirmMap = DataMapUtil.implode(huListstr);
	// }
	// birdNum = wrapper.getInt(6, 0);
	// moMajiangSeat = wrapper.getInt(7, 0);
	// int moGangMajiangId = wrapper.getInt(8, 0);
	// if (moGangMajiangId != 0) {
	// moGang = Majiang.getMajang(moGangMajiangId);
	// }
	// String moGangHu = wrapper.getString(9);
	// if (!StringUtils.isBlank(moGangHu)) {
	// moGangHuList = StringUtil.explodeToIntList(moGangHu);
	// }
	// String gangDisMajiangstr = wrapper.getString(10);
	// if (!StringUtils.isBlank(gangDisMajiangstr)) {
	// gangDisMajiangs = MajiangHelper.explodeMajiang(gangDisMajiangstr, ",");
	// }
	// int gangMajiang = wrapper.getInt(11, 0);
	// if (gangMajiang != 0) {
	// this.gangMajiang = Majiang.getMajang(gangMajiang);
	// }
	//
	// askLastMajaingSeat = wrapper.getInt(12, 0);
	// moLastMajiangSeat = wrapper.getInt(13, 0);
	// int lastMajiangId = wrapper.getInt(14, 0);
	// if (lastMajiangId != 0) {
	// this.lastMajiang = Majiang.getMajang(lastMajiangId);
	// }
	// fristLastMajiangSeat = wrapper.getInt(15, 0);
	// disEventAction = wrapper.getInt(16, 0);
	// isCalcBanker = wrapper.getInt(17, 1);
	// calcBird = wrapper.getInt(18, 1);
	// // disAction = wrapper.getInt(11, 0);
	// // wrapper.putInt(17, isCalcBanker);
	// // wrapper.putInt(18, calcBird);
	//
	// }

	/**
	 * ????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param disMajiang
	 * @return
	 */
	private boolean canChi(BsMjPlayer player, List<BsMj> handMajiang, List<BsMj> majiangs, BsMj disMajiang) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return false;
		}
		if (player.isAlreadyMoMajiang()) {
			return false;
		}
		List<Integer> pengGangSeatList = getPengGangSeatByActionMap();
		pengGangSeatList.remove((Object) player.getSeat());
		if (!pengGangSeatList.isEmpty()) {
			return false;
		}
		//
		// Majiang playCommand = null;
		// if (nowDisCardIds.size() == 1) {
		// playCommand = nowDisCardIds.get(0);
		//
		// } else {
		// for (int majiangId : gangSeatMap.keySet()) {
		// Map<Integer, List<Integer>> actionMap = gangSeatMap.get(majiangId);
		// List<Integer> action = actionMap.get(player.getSeat());
		// if (action != null) {
		// List<Integer> disActionList =
		// MajiangDisAction.parseToDisActionList(action);
		// if (disActionList.contains(MajiangDisAction.action_chi)) {
		// playCommand = Majiang.getMajang(majiangId);
		// break;
		// }
		//
		// }
		//
		// }
		//
		// }

		if (disMajiang == null) {
			return false;
		}

		if (!handMajiang.containsAll(majiangs)) {
			return false;
		}

		List<BsMj> chi = BsMjTool.checkChi(majiangs, disMajiang);
		return !chi.isEmpty();
	}

	/**
	 * ????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
	private boolean canPeng(BsMjPlayer player, List<BsMj> majiangs, int sameCount, BsMj disMajiang) {
		if (!actionSeatMap.containsKey(player.getSeat())) {
			return false;
		}
		if (player.isAlreadyMoMajiang()) {
			return false;
		}
		if (sameCount != 2) {
			return false;
		}
		if (disMajiang == null) {
			return false;
		}
		if (majiangs.get(0).getVal() != disMajiang.getVal()) {
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
	private boolean canAnGang(BsMjPlayer player, List<BsMj> majiangs, int sameCount, int action) {
		if (sameCount != 4) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat() && action != BsMjDisAction.action_buzhang) {
			return false;
		}
		if (player.getSeat() != getNextDisCardSeat() && action != BsMjDisAction.action_buzhang_an) {
			return false;
		}
		return true;
	}

	/**
	 * ??????????????? ????????????????????????????????????????????????????????????
	 */
	private boolean checkAction(BsMjPlayer player, List<BsMj> cardList, List<Integer> hucards, int action) {
		boolean canAction = checkCanAction(player, action);// ????????????????????? ???????????????
		if (canAction == false) {// ??????????????? ??????????????????
			int seat = player.getSeat();
			tempActionMap.put(seat, new BsMjTempAction(seat, action, cardList, hucards));
			// ?????????????????????????????????????????? ?????????????????????
			if (tempActionMap.size() == actionSeatMap.size()) {
				int maxAction = Integer.MAX_VALUE;
				int maxSeat = 0;
				Map<Integer, Integer> prioritySeats = new HashMap<>();
				int maxActionSize = 0;
				for (BsMjTempAction temp : tempActionMap.values()) {
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
				BsMjPlayer tempPlayer = seatMap.get(maxSeat);
				List<BsMj> tempCardList = tempActionMap.get(maxSeat).getCardList();
				for (int removeSeat : prioritySeats.keySet()) {
					if (removeSeat != maxSeat) {
						removeActionSeat(removeSeat);
					}
				}
				clearTempAction();
				playCommand(tempPlayer, tempCardList, maxAction);// ?????????????????????????????????
			} else {
				if (isCalcOver()) {// ??????????????????????????????
					calcOver();
					return canAction;
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
	private void refreshTempAction(BsMjPlayer player) {
		tempActionMap.remove(player.getSeat());
		Map<Integer, Integer> prioritySeats = new HashMap<>();
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			int seat = entry.getKey();
			List<Integer> actionList = entry.getValue();
			List<Integer> list = BsMjDisAction.parseToDisActionList(actionList);
			int priorityAction = BsMjDisAction.getMaxPriorityAction(list);
			prioritySeats.put(seat, priorityAction);
		}
		int maxPriorityAction = Integer.MAX_VALUE;
		int maxPrioritySeat = 0;
		boolean isSame = true;
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
		Iterator<BsMjTempAction> iterator = tempActionMap.values().iterator();
		while (iterator.hasNext()) {
			BsMjTempAction tempAction = iterator.next();
			if (tempAction.getSeat() == maxPrioritySeat) {
				int action = tempAction.getAction();
				List<BsMj> tempCardList = tempAction.getCardList();
				BsMjPlayer tempPlayer = seatMap.get(tempAction.getSeat());
				playCommand(tempPlayer, tempCardList, action);// ?????????????????????????????????
				iterator.remove();
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
	public boolean checkCanAction(BsMjPlayer player, int action) {
		// ???????????????????????????
		List<Integer> stopActionList = BsMjDisAction.findPriorityAction(action);
		for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
			if (player.getSeat() != entry.getKey()) {
				// ??????
				boolean can = BsMjDisAction.canDisMajiang(stopActionList, entry.getValue());
				if (!can) {
					return false;
				}
				List<Integer> disActionList = BsMjDisAction.parseToDisActionList(entry.getValue());
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
	 * ???????????????
	 *
	 * @param player
	 * @param majiangs
	 * @param sameCount
	 * @return
	 */
	private boolean canMingGang(BsMjPlayer player, List<BsMj> handMajiang, List<BsMj> majiangs, int sameCount,
			BsMj disMajiang) {
		List<Integer> pengList = BsMjHelper.toMajiangVals(player.getPeng());

		if (majiangs.size() == 1) {
			if (!isHasGangAction() && player.getSeat() != getNextDisCardSeat()) {
				return false;
			}
			if (handMajiang.containsAll(majiangs) && pengList.contains(majiangs.get(0).getVal())) {
				return true;
			}
		} else if (majiangs.size() == 3) {
			if (sameCount != 3) {
				return false;
			}
			if (!actionSeatMap.containsKey(player.getSeat())) {
				return false;
			}
			if (disMajiang == null || disMajiang.getVal() != majiangs.get(0).getVal()) {
				return false;
			}
			return true;
		}

		return false;
	}

	public Map<Integer, List<Integer>> getActionSeatMap() {
		robotDealAction();
		return actionSeatMap;
	}

	public int getBirdNum() {
		return birdNum;
	}

	public void setBirdNum(int birdNum) {
		this.birdNum = birdNum;
		changeExtend();
	}

	public int getBuyPoint() {
		return buyPoint;
	}

	public void setKePiao(int kePiao) {
		this.buyPoint = kePiao;
		changeExtend();
	}

	public void setMoMajiangSeat(int moMajiangSeat) {
		this.moMajiangSeat = moMajiangSeat;
		changeExtend();
	}

	public void setAskLastMajaingSeat(int askLastMajaingSeat) {
		this.askLastMajaingSeat = askLastMajaingSeat;
		changeExtend();
	}

	public void setFristLastMajiangSeat(int fristLastMajiangSeat) {
		this.fristLastMajiangSeat = fristLastMajiangSeat;
		changeExtend();
	}

	public void setLastMajiang(BsMj lastMajiang) {
		this.lastMajiang = lastMajiang;
		changeExtend();
	}

	public void setMoLastMajiangSeat(int moLastMajiangSeat) {
		this.moLastMajiangSeat = moLastMajiangSeat;
		changeExtend();
	}

	public void setGangMajiang(BsMj gangMajiang) {
		this.gangMajiang = gangMajiang;
		changeExtend();
	}

	/**
	 * ?????????????????????
	 *
	 * @param moGang
	 *            ?????????
	 * @param moGangHuList
	 *            ????????????????????????list
	 */
	public void setMoGang(BsMj moGang, List<Integer> moGangHuList) {
		this.moGang = moGang;
		this.moGangHuList = moGangHuList;
		changeExtend();
	}

	/**
	 * ???????????????
	 */
	public void clearMoGang() {
		this.moGang = null;
		this.moGangHuList.clear();
		changeExtend();
	}

	public void setGangDisMajiangs(List<BsMj> gangDisMajiangs) {
		this.gangDisMajiangs = gangDisMajiangs;
		changeExtend();
	}

	public List<BsMj> getGangDisMajiangs() {
		return gangDisMajiangs;
	}

	/**
	 * ?????????????????????
	 */
	public void clearGangDisMajiangs() {
		this.gangActedMj = null;
		this.gangMajiang = null;
		this.gangDisMajiangs.clear();
		this.gangDice = -1;
		changeExtend();
	}

	/**
	 * guo ?????????
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
		wrapper.putString(1, DataMapUtil.explodeListMap(actionSeatMap));
		wrapper.putString(2, DataMapUtil.explodeListMapMap(gangSeatMap));
		// w
		return wrapper.toString();
	}

	@Override
	public void setConfig(int index, int val) {

	}

	/**
	 * ???????????????
	 *
	 * @return
	 */
	public boolean moHu() {
		if (getConifg(0) == 2) {
			return true;

		}
		return false;
	}

	/**
	 * ????????????
	 *
	 * @return
	 */
	public boolean canGangHu() {
		return true;
	}

	public ClosingMjInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList,
			int[] prickBirdMajiangIds, int[] seatBirds, boolean isBreak, int bankerSeat, int fangpaoSeat) {
		
		
		// ????????????????????????
		if (over && jiaBei == 1) {
			int jiaBeiPoint = 0;
			int loserCount = 0;
			for (BsMjPlayer player : seatMap.values()) {
				if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
					jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
					player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
				} else if (player.getTotalPoint() < 0) {
					loserCount++;
				}
			}
			if (jiaBeiPoint > 0) {
				for (BsMjPlayer player : seatMap.values()) {
					if (player.getTotalPoint() < 0) {
						player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
					}
				}
			}
		}

		//???????????????below???+belowAdd???
		if(over&&belowAdd>0&&playerMap.size()==2){
			for (BsMjPlayer player : seatMap.values()) {
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
		for (BsMjPlayer player : seatMap.values()) {
			ClosingMjPlayerInfoRes.Builder build = null;
			if (over) {
				build = player.buildTotalClosingPlayerInfoRes();
			} else {
				build = player.buildOneClosingPlayerInfoRes();
			}

			build.setBirdPoint(player.getPiaoPoint());
			if (winList != null && winList.contains(player.getSeat())) {
				if (!selfMo) {
					// ????????????
					List<Integer> huMjIds = player.getHuMjIds();
					if (huMjIds != null && huMjIds.size() > 0) {
						for (int mjId : huMjIds) {
							if (!build.getHandPaisList().contains(mjId)) {
								build.addHandPais(mjId);
							}
						}
						int isHu = 0;
						if (huMjIds.size() == 2) {
							isHu = huMjIds.get(0) * 1000 + huMjIds.get(1);
						} else {
							isHu = huMjIds.get(0);
						}
						build.setIsHu(isHu);
					} else {
						BsMj huMajiang = nowDisCardIds.get(0);
						if (!build.getHandPaisList().contains(huMajiang.getId())) {
							build.addHandPais(huMajiang.getId());
						}
						build.setIsHu(huMajiang.getId());
					}
				} else {
					List<Integer> huMjIds = player.getHuMjIds();
					if (huMjIds != null && huMjIds.size() > 0) {
						for (int mjId : huMjIds) {
							if (!build.getHandPaisList().contains(mjId)) {
								build.addHandPais(mjId);
							}
						}
						int isHu = 0;
						if (huMjIds.size() == 2) {
							isHu = huMjIds.get(0) * 1000 + huMjIds.get(1);
						} else {
							isHu = huMjIds.get(0);
						}
						build.setIsHu(isHu);
					} else {
						build.setIsHu(player.getLastMoMajiang().getId());
					}
				}
				// build.addAllDahus(player.getDahu());
				build.setTotalFan(player.getDahuFan());
			}
			if (player.getSeat() == fangpaoSeat) {
				build.setFanPao(1);
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
			for (BsMjPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
				BsMjPlayer player = seatMap.get(builder.getSeat());
				calcCommissionCredit(player, dyjCredit);
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
			}
        } else if (isGroupTableGoldRoom()) {
            // -----------??????????????????---------------------------------
            for (BsMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
                BsMjPlayer player = seatMap.get(builder.getSeat());
                builder.setWinLoseCredit(player.getWinGold());
            }
        }

        for (ClosingMjPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }

        ClosingMjInfoRes.Builder res = ClosingMjInfoRes.newBuilder();
        res.addAllClosingPlayers(list);
		// ????????? ?????????????????????????????????
		if (bankerSeat > 0) {
			res.setIsBreak(0);
		} else {
			// ???????????????
			res.setIsBreak(isBreak ? 1 : 0);
		}

		res.setWanfa(getWanFa());
		res.addAllExt(buildAccountsExt(bankerSeat, over ? 1 : 0));
		if (seatBirds != null) {
			res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
		}
		if (prickBirdMajiangIds != null) {
			res.addAllBird(DataMapUtil.toList(prickBirdMajiangIds));
		}
		// ??????

		res.addCreditConfig(creditMode); // 0
		res.addCreditConfig(creditJoinLimit); // 1
		res.addCreditConfig(creditDissLimit); // 2
		res.addCreditConfig(creditDifen); // 3
		res.addCreditConfig(creditCommission); // 4
		res.addCreditConfig(creditCommissionMode1); // 5
		res.addCreditConfig(creditCommissionMode2); // 6
		res.addCreditConfig(creditCommissionLimit); // 7

		// res.setCatchBirdSeat(catchBirdSeat);
		res.addAllLeftCards(BsMjHelper.toMajiangIds(leftMajiangs));
		for (BsMjPlayer player : seatMap.values()) {
			player.writeSocket(res.build());
		}
		return res;

	}

	/**
	 * ?????????????????????
	 *
	 * @return
	 */
	public BsMj getGangHuMajiang(int seat) {
		int majiangId = 0;
		for (Entry<Integer, Map<Integer, List<Integer>>> entry : gangSeatMap.entrySet()) {
			Map<Integer, List<Integer>> actionMap = entry.getValue();
			if (actionMap.containsKey(seat)) {
				List<Integer> actionList = actionMap.get(seat);
				if (actionList != null && !actionList.isEmpty() && actionList.get(0) == 1) {
					majiangId = entry.getKey();
					break;
				}
			}
		}
		return BsMj.getMajang(majiangId);

	}

	public List<String> buildAccountsExt(int bankerSeat, int over) {
		List<String> ext = new ArrayList<>();
		if (isGroupRoom()) {
			ext.add(loadGroupId());
		} else {
			ext.add("0");
		}
		ext.add(id + "");
		ext.add(masterId + "");
		ext.add(TimeUtil.formatTime(TimeUtil.now()));
		ext.add(playType + "");
		ext.add(getMasterName() + "");

		// ext.add(getConifg(0) + "");
		// ext.add(bankerSeat + "");
		// ext.add(calcBird + "");
		// ext.add(gpsWarn + "");
		ext.add(youfeng + "");
		ext.add(baoting + "");
		ext.add(yitiaolong + "");
		ext.add(siguiyi + "");
		ext.add(buyPoint + "");
		ext.add(chajiao + "");
		ext.add(String.valueOf(playedBureau));// 12
		ext.add(String.valueOf(over));// 12

		// ext.add(isCalcBanker + "");
		// ext.add(birdNum + "");
		return ext;
	}

	@Override
	public void sendAccountsMsg() {
		ClosingMjInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, true, 0, 0);
		saveLog(true, 0l, builder.build());
	}

	public Class<? extends Player> getPlayerClass() {
		return BsMjPlayer.class;
	}

	@Override
	public int getWanFa() {
		return GameUtil.game_type_bsmj;
	}

	@Override
	public void checkReconnect(Player player) {
		if (super.isAllReady() && getBuyPoint() > 0 && getTableStatus() == BsMjConstants.TABLE_STATUS_PIAO) {
			BsMjPlayer player1 = (BsMjPlayer) player;
			if (player1.getPiaoPoint() < 0) {
				if (getBuyPoint() == 5) {
					player1.setPiaoPoint(1);
					// ??????
					ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_buy_point,
							player1.getSeat(), player1.getPiaoPoint());
					for (Player tableplayer : getSeatMap().values()) {// ?????????????????????????????????
						tableplayer.writeSocket(com.build());
					}
				} else {
					ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_table_status_buy_point,
							getBuyPoint());
					player1.writeSocket(com.build());
				}
				return;
			}
		}
		checkSendGangRes(player);
		if (askLastMajaingSeat != 0) {
			sendAskLastMajiangRes(player.getSeat());
		}
		if (actionSeatMap.isEmpty()) {
			// ????????????????????????????????????
			if (player instanceof BsMjPlayer) {
				BsMjPlayer BsMjPlayer = (BsMjPlayer) player;
				if (BsMjPlayer != null) {
					if (BsMjPlayer.isAlreadyMoMajiang()) {
						// if (!BsMjPlayer.getGang().isEmpty()) {
						// List<BsMj> disMajiangs = new ArrayList<>();
						// disMajiangs.add(BsMjPlayer.getLastMoMajiang());
						// chuPai(BsMjPlayer, disMajiangs, 0);
						// }
					}
				}
			}
		}
		if (isBegin() && player.getSeat() == lastWinSeat &&  !hasBaoTing()) {
			// ???????????????????????????????????????????????? ??????????????????????????????
			BsMjPlayer bankPlayer = seatMap.get(lastWinSeat);
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_kt_ask_dismajiang);// ??????????????????
			bankPlayer.writeSocket(com.build());
		}

		if (state == table_state.play) {
			BsMjPlayer player1 = (BsMjPlayer) player;
			if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
				sendTingMsg(player1, true);
			}
		}

	}

	public static void main(String[] args) {
		System.out.println(Math.pow(3, 2));
	}

	@Override
	public boolean consumeCards() {
		return SharedConstants.consumecards;
	}

	@Override
	public void checkAutoPlay() {
		if (System.currentTimeMillis() - lastAutoPlayTime < 100) {
			return;
		}
		// ???????????????????????????
		if (getSendDissTime() > 0) {
			for (BsMjPlayer player : seatMap.values()) {
				if (player.getLastCheckTime() > 0) {
					player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
				}
			}
			return;
		}

		// ???????????????????????????
		if (!getActionSeatMap().isEmpty()) {
			for (BsMjPlayer player : seatMap.values()) {
				if (!player.isChiPengGang() && player.getActionNum(8) >= 10) {
					playCommand(player, new ArrayList<BsMj>(), BsMjDisAction.action_hu);
					return;
				}

			}

			// int leftCount=4;
			// if(getPlayerCount()<4) {
			// leftCount = 20;
			// }
			// if(getLeftMajiangCount()<=leftCount) {
			// for(BsMjPlayer player : seatMap.values()){
			// List<Integer> actionList = actionSeatMap.get(player.getSeat());
			// if(actionList!= null && actionList.size()>0 &&actionList.get(0)
			// == 1) {
			// // ???
			// playCommand(player, new ArrayList<BsMj>(),
			// BsMjDisAction.action_hu);
			// }
			// }
			// }

		}

		if (isAutoPlay < 1) {
			return;
		}

		
        if (isAutoPlayOff()) {
            // ????????????
            for (int seat : seatMap.keySet()) {
                BsMjPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
                player.setCheckAutoPlay(false);
            }
            return;
        }

		
		if (state == table_state.play) {
			autoPlay();
		} else {
			if (getPlayedBureau() == 0) {
				return;
			}
			readyTime++;
			// for (BsMjPlayer player : seatMap.values()) {
			// if (player.checkAutoPlay(1, false)) {
			// autoReady(player);
			// }
			// }
			// ????????????????????????xx???????????????????????????
			for (BsMjPlayer player : seatMap.values()) {
				if (player.getState() != player_state.entry && player.getState() != player_state.over) {
					continue;
				} else {
					if (readyTime >= 5 && player.isAutoPlay()) {
						// ????????????????????????3???????????????
						autoReady(player);
					} else if (readyTime > 30) {
						autoReady(player);
					}
				}
			}
		}
	}

	public void sendTingInfo(BsMjPlayer player) {
		sendTingMsg(player, false);
	}

	private void sendTingMsg(BsMjPlayer player, boolean checkBaoting) {
		List<Integer> allMjs = new ArrayList<>(BsMjConstants.baoshan_mjList);

		if (youfeng == 1) {
			allMjs.addAll(BsMjConstants.feng_mjList);
		}

		if (player.isAlreadyMoMajiang()) {
			// if (actionSeatMap.containsKey(player.getSeat())) {
			// return;
			// }
			DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<BsMj> cards = new ArrayList<>(player.getHandMajiang());

			for (BsMj card : player.getHandMajiang()) {
				cards.remove(card);
				List<BsMj> huCards = BsMjTool.getTingMjs(cards, player, this, allMjs);
				cards.add(card);
				if (huCards == null || huCards.size() == 0) {
					continue;
				}
				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(card.getId());
				for (BsMj mj : huCards) {
					ting.addTingMajiangIds(mj.getId());
				}
				tingInfo.addInfo(ting.build());
			}
			if (tingInfo.getInfoCount() > 0) {
				player.writeSocket(tingInfo.build());
			}
		} else {
			List<BsMj> cards = new ArrayList<>(player.getHandMajiang());
			List<BsMj> huCards = BsMjTool.getTingMjs(cards, player, this, allMjs);
			if (huCards == null || huCards.size() == 0) {
//				if (checkBaoting) {
//					if (player.getBaoTingS() == 1) {
//						player.setBaoTingS(0);
//						ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_ts_pao_ting,
//								player.getSeat(), player.getBaoTingS());
//						player.writeSocket(com.build());
//					}
//				}
				return;
			}

//			if (checkBaoting) {
//				if (player.getBaoTingS() < 0 && player.getActionNum(9) <= 1) {
//					ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_ts_pao_ting,
//							player.getSeat(), player.getBaoTingS());
//					player.writeSocket(com.build());
//				}
//			} else {
				TingPaiRes.Builder ting = TingPaiRes.newBuilder();
				for (BsMj mj : huCards) {
					ting.addMajiangIds(mj.getId());
				}
				player.writeSocket(ting.build());
//			}
		}
	}

	private boolean checkBaoting(BsMjPlayer player) {
		if(getBaoting()!=1||player.getBaoTingS()==0||player.getBaoTingS()==2){
			return false;
		}
		List<Integer> allMjs = new ArrayList<>(BsMjConstants.baoshan_mjList);
		if (youfeng == 1) {
			allMjs.addAll(BsMjConstants.feng_mjList);
		}
		if (player.isAlreadyMoMajiang()) {
			List<BsMj> cards = new ArrayList<>(player.getHandMajiang());
			for (BsMj card : player.getHandMajiang()) {
				cards.remove(card);
				List<BsMj> huCards = BsMjTool.getTingMjs(cards, player, this, allMjs);
				cards.add(card);
				if (huCards == null || huCards.size() == 0) {
					continue;
				}
				if (player.getBaoTingS() == -1) {
					player.setBaoTingS(0);
				}
				return true;

			}

		} else {
			List<BsMj> cards = new ArrayList<>(player.getHandMajiang());
			List<BsMj> huCards = BsMjTool.getTingMjs(cards, player, this, allMjs);
			if (huCards == null || huCards.size() == 0) {
				return false;
			}
			if (player.getBaoTingS() == -1) {
				player.setBaoTingS(0);
			}
			return true;
		}
		return false;
	}

	public void sendTingInfo2(BsMjPlayer player) {
		if (player.isAlreadyMoMajiang()) {
			if (actionSeatMap.containsKey(player.getSeat())) {
				return;
			}
			DaPaiTingPaiRes.Builder tingInfo = DaPaiTingPaiRes.newBuilder();
			List<BsMj> cards = new ArrayList<>(player.getHandMajiang());
			int[] cardArr = HuUtil.toCardArray(cards);
			Map<Integer, List<BsMj>> checked = new HashMap<>();
			for (BsMj card : cards) {
				if (card.isHongzhong()) {
					continue;
				}
				List<BsMj> lackPaiList;
				if (checked.containsKey(card.getVal())) {
					lackPaiList = checked.get(card.getVal());
				} else {
					int cardIndex = HuUtil.getMjIndex(card);
					cardArr[cardIndex] = cardArr[cardIndex] - 1;
					lackPaiList = BsMjTool.getLackList(cardArr, 0, false);
					cardArr[cardIndex] = cardArr[cardIndex] + 1;
					if (lackPaiList.size() > 0) {
						checked.put(card.getVal(), lackPaiList);
					} else {
						continue;
					}
				}

				DaPaiTingPaiInfo.Builder ting = DaPaiTingPaiInfo.newBuilder();
				ting.setMajiangId(card.getId());
				if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
					// ?????????
					ting.addTingMajiangIds(BsMj.mj201.getId());
				} else {
					for (BsMj lackPai : lackPaiList) {
						ting.addTingMajiangIds(lackPai.getId());
					}
					ting.addTingMajiangIds(BsMj.mj201.getId());
				}
				tingInfo.addInfo(ting.build());
			}
			if (tingInfo.getInfoCount() > 0) {
				player.writeSocket(tingInfo.build());
			}
		} else {
			List<BsMj> cards = new ArrayList<>(player.getHandMajiang());
			int hzCount = BsMjTool.dropHongzhong(cards).size();
			int[] cardArr = HuUtil.toCardArray(cards);
			List<BsMj> lackPaiList = BsMjTool.getLackList(cardArr, hzCount, false);
			if (lackPaiList == null || lackPaiList.size() == 0) {
				return;
			}
			TingPaiRes.Builder ting = TingPaiRes.newBuilder();
			if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
				// ?????????
				ting.addMajiangIds(BsMj.mj201.getId());
			} else {
				for (BsMj lackPai : lackPaiList) {
					ting.addMajiangIds(lackPai.getId());
				}
				ting.addMajiangIds(BsMj.mj201.getId());
			}
			player.writeSocket(ting.build());
		}
	}

	public boolean IsCalcBankerPoint() {
		return isCalcBanker == 1;
	}

	public void setIsCalcBanker(int isCalcBanker) {
		this.isCalcBanker = isCalcBanker;
		changeExtend();
	}

	public int getCalcBird() {
		return calcBird;
	}

	public void setCalcBird(int calcBird) {
		this.calcBird = calcBird;
		changeExtend();
	}

	@Override
	public void initExtend0(JsonWrapper extend) {
		for (BsMjPlayer player : seatMap.values()) {
			player.initExtend(extend.getString(player.getSeat()));
		}
		String huListstr = extend.getString(5);
		if (!StringUtils.isBlank(huListstr)) {
			huConfirmMap = DataMapUtil.implode(huListstr);
		}
		birdNum = extend.getInt(6, 0);
		moMajiangSeat = extend.getInt(7, 0);
		int moGangMajiangId = extend.getInt(8, 0);
		if (moGangMajiangId != 0) {
			moGang = BsMj.getMajang(moGangMajiangId);
		}
		String moGangHu = extend.getString(9);
		if (!StringUtils.isBlank(moGangHu)) {
			moGangHuList = StringUtil.explodeToIntList(moGangHu);
		}
		String gangDisMajiangstr = extend.getString(10);
		if (!StringUtils.isBlank(gangDisMajiangstr)) {
			gangDisMajiangs = BsMjHelper.explodeMajiang(gangDisMajiangstr, ",");
		}
		int gangMajiang = extend.getInt(11, 0);
		if (gangMajiang != 0) {
			this.gangMajiang = BsMj.getMajang(gangMajiang);
		}

		askLastMajaingSeat = extend.getInt(12, 0);
		moLastMajiangSeat = extend.getInt(13, 0);
		int lastMajiangId = extend.getInt(14, 0);
		if (lastMajiangId != 0) {
			this.lastMajiang = BsMj.getMajang(lastMajiangId);
		}
		fristLastMajiangSeat = extend.getInt(15, 0);
		disEventAction = extend.getInt(16, 0);
		isCalcBanker = extend.getInt(17, 1);
		calcBird = extend.getInt(18, 1);
		buyPoint = extend.getInt(19, 0);
		tempActionMap = loadTempActionMap(extend.getString("tempActions"));

		gpsWarn = extend.getInt(20, 0);
		youfeng = extend.getInt(21, 0);
		yitiaolong = extend.getInt(22, 0);
		siguiyi = extend.getInt(23, 0);
		baoting = extend.getInt(24, 0);
		String showMj = extend.getString(31);
		if (!StringUtils.isBlank(showMj)) {
			showMjSeat = StringUtil.explodeToIntList(showMj);
		}
		maxPlayerCount = extend.getInt(32, 4);
		gangDice = extend.getInt(33, -1);
		String moTailPaiStr = extend.getString(34);
		if (!StringUtils.isBlank(moTailPaiStr)) {
			moTailPai = StringUtil.explodeToIntList(moTailPaiStr);
		}
		String moLastSeatsStr = extend.getString(35);
		if (!StringUtils.isBlank(moLastSeatsStr)) {
			moLastSeats = StringUtil.explodeToIntList(moLastSeatsStr);
		}
		isBegin = extend.getInt(36, 0) == 1;
		dealDice = extend.getInt(37, 0);

		isAutoPlay = extend.getInt(38, 0);
		autoPlayGlob = extend.getInt(39, 0);
		chajiao = extend.getInt(40, 0);
		if (isAutoPlay == 1) {
			isAutoPlay = 60;
		}
		 jiaBei = extend.getInt(41, 0);
	        jiaBeiFen = extend.getInt(42, 0);
	        jiaBeiShu = extend.getInt(43, 0);
	        below = extend.getInt(44, 0);
	        belowAdd = extend.getInt(45, 0);
	}

	@Override
	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		// 1-4 ??????????????????
		for (BsMjPlayer player : seatMap.values()) {
			wrapper.putString(player.getSeat(), player.toExtendStr());
		}
		wrapper.putString(5, DataMapUtil.explode(huConfirmMap));
		wrapper.putInt(6, birdNum);
		wrapper.putInt(7, moMajiangSeat);
		wrapper.putInt(8, moGang != null ? moGang.getId() : 0);
		wrapper.putString(9, StringUtil.implode(moGangHuList, ","));
		wrapper.putString(10, BsMjHelper.implodeMajiang(gangDisMajiangs, ","));
		wrapper.putInt(11, gangMajiang != null ? gangMajiang.getId() : 0);
		wrapper.putInt(12, askLastMajaingSeat);
		wrapper.putInt(13, moLastMajiangSeat);
		wrapper.putInt(14, lastMajiang != null ? lastMajiang.getId() : 0);
		wrapper.putInt(15, fristLastMajiangSeat);
		wrapper.putInt(16, disEventAction);
		wrapper.putInt(17, isCalcBanker);
		wrapper.putInt(18, calcBird);
		wrapper.putInt(19, buyPoint);
		JSONArray tempJsonArray = new JSONArray();
		for (int seat : tempActionMap.keySet()) {
			tempJsonArray.add(tempActionMap.get(seat).buildData());
		}
		wrapper.putString("tempActions", tempJsonArray.toString());

		wrapper.putInt(20, gpsWarn);
		wrapper.putInt(21, youfeng);
		wrapper.putInt(22, yitiaolong);
		wrapper.putInt(23, siguiyi);
		wrapper.putInt(24, baoting);

		wrapper.putString(31, StringUtil.implode(showMjSeat, ","));
		wrapper.putInt(32, maxPlayerCount);
		wrapper.putInt(33, gangDice);
		wrapper.putString(34, StringUtil.implode(moTailPai, ","));
		wrapper.putString(35, StringUtil.implode(moLastSeats, ","));
		wrapper.putInt(36, isBegin ? 1 : 0);
		wrapper.putInt(37, dealDice);
		wrapper.putInt(38, isAutoPlay);
		wrapper.putInt(39, autoPlayGlob);
		wrapper.putInt(40, chajiao);
		  wrapper.putInt(41, jiaBei);
	        wrapper.putInt(42, jiaBeiFen);
	        wrapper.putInt(43, jiaBeiShu);
	        wrapper.putInt(44, below);
	        wrapper.putInt(45, belowAdd);
		return wrapper;
	}

	@Override
	public void createTable(Player player, int playType, int bureauCount, List<Integer> params, List<String> strParams,
			Object... objects) throws Exception {
		long id = getCreateTableId(player.getUserId(), playType);
		TableInf info = new TableInf();
		info.setMasterId(player.getUserId());
		info.setRoomId(0);
		info.setPlayType(playType);
		info.setTableId(id);
		info.setTotalBureau(bureauCount);
		info.setPlayBureau(1);
		info.setServerId(GameServerConfig.SERVER_ID);
		info.setCreateTime(new Date());
		info.setDaikaiTableId(daikaiTableId);
		info.setConfig(String.valueOf(0));
		TableDao.getInstance().save(info);
		loadFromDB(info);

		maxPlayerCount = StringUtil.getIntValue(params, 3, 4);// ????????????
		payType = StringUtil.getIntValue(params, 2, 1);// ????????????
		// calcBird = StringUtil.getIntValue(params, 3, 0);
		// birdNum = StringUtil.getIntValue(params, 4, 0);
		gpsWarn = StringUtil.getIntValue(params, 4, 0);
		youfeng = StringUtil.getIntValue(params, 5, 0);
		yitiaolong = StringUtil.getIntValue(params, 6, 1);
		siguiyi = StringUtil.getIntValue(params, 7, 0);
		baoting = StringUtil.getIntValue(params, 8, 0);
		buyPoint = StringUtil.getIntValue(params, 9, 0);
		chajiao = StringUtil.getIntValue(params, 10, 0);

		isAutoPlay = StringUtil.getIntValue(params, 11, 0);

		autoPlayGlob = StringUtil.getIntValue(params, 12, 0);

		if (isAutoPlay == 1) {
			isAutoPlay = 60;// ??????20s
		}

		autoPlay = (isAutoPlay > 1);

		playedBureau = 0;
		
		  this.jiaBei = StringUtil.getIntValue(params, 13, 0);
	        this.jiaBeiFen = StringUtil.getIntValue(params, 14, 0);
	        this.jiaBeiShu = StringUtil.getIntValue(params, 15, 0);
		
	        if(maxPlayerCount==2){
	            int belowAdd = StringUtil.getIntValue(params, 16, 0);
	            if(belowAdd<=100&&belowAdd>=0)
	                this.belowAdd=belowAdd;
	            int below = StringUtil.getIntValue(params, 17, 0);
	            if(below<=100&&below>=0){
	                this.below=below;
	                if(belowAdd>0&&below==0)
	                    this.below=10;
	            }
	        }
		
		

		// getRoomModeMap().put("1", "1"); //?????????????????????
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_bsmj);

	public static void loadWanfaTables(Class<? extends BaseTable> cls) {
		for (Integer integer : wanfaList) {
			TableManager.wanfaTableTypesPut(integer, cls);
		}
	}

	/**
	 * ????????????
	 */
	public synchronized void autoPlay() {
		if (state != table_state.play) {
			return;
		}
		
		
		if(isBegin()){
			boolean flag = false;
			for (BsMjPlayer robotPlayer : seatMap.values()) {
				if(robotPlayer.getSeat()==lastWinSeat||robotPlayer.getBaoTingS()!=0){
					continue;
				}
				flag = true;
				if (!robotPlayer.checkAutoPlay(0, false)) {
					continue;
				}
				playBaoting(robotPlayer, 2);
			}
			if(flag){
				return;
			}
			
		}
		
		
		if (!actionSeatMap.isEmpty()) {
			List<Integer> huSeatList = getHuSeatByActionMap();
			if (!huSeatList.isEmpty()) {
				// ???????????????
				for (int seat : huSeatList) {
					BsMjPlayer player = seatMap.get(seat);
					if (player == null) {
						continue;
					}
					if (!player.checkAutoPlay(2, false)) {
						continue;
					}
					playCommand(player, new ArrayList<>(), BsMjDisAction.action_hu);
				}
				return;
			} else {
				int action, seat;
				for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
					List<Integer> actList = BsMjDisAction.parseToDisActionList(entry.getValue());
					if (actList == null) {
						continue;
					}
					seat = entry.getKey();
					action = BsMjDisAction.getAutoMaxPriorityAction(actList);
					BsMjPlayer player = seatMap.get(seat);
					if (!player.checkAutoPlay(0, false)) {
						continue;
					}
					boolean chuPai = false;
					if (player.isAlreadyMoMajiang()) {
						chuPai = true;
					}
					if (action == BsMjDisAction.action_peng) {
						if (player.isAutoPlaySelf()) {
							// ???????????????????????????
							playCommand(player, new ArrayList<>(), BsMjDisAction.action_pass);
							if (chuPai) {
								autoChuPai(player);
							}
						} else {
							if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
								BsMj mj = nowDisCardIds.get(0);
								List<BsMj> mjList = new ArrayList<>();
								for (BsMj handMj : player.getHandMajiang()) {
									if (handMj.getVal() == mj.getVal()) {
										mjList.add(handMj);
										if (mjList.size() == 2) {
											break;
										}
									}
								}
								playCommand(player, mjList, BsMjDisAction.action_peng);
							}
						}
					} else {
						playCommand(player, new ArrayList<>(), BsMjDisAction.action_pass);
						if (chuPai) {
							autoChuPai(player);
						}
					}
				}
			}
		} else {
			BsMjPlayer player = seatMap.get(nowDisCardSeat);
			if (player == null || !player.checkAutoPlay(0, false)) {
				return;
			}
			
			if(player.getBaoTingS()==0){
					playBaoting(player, 2);
			}
			
			autoChuPai(player);
		}
	}

	public void autoChuPai(BsMjPlayer player) {

		if (!player.isAlreadyMoMajiang()) {
			return;
		}
		List<Integer> handMjIds = new ArrayList<>(player.getHandPais());
		int index = handMjIds.size() - 1;
		int mjId = -1;
		if (moMajiangSeat == player.getSeat()) {
			mjId = handMjIds.get(index);
		} else {
			Collections.sort(handMjIds);
			mjId = handMjIds.get(index);
		}
		if (mjId != -1) {
			List<BsMj> mjList = BsMjHelper.toMajiang(Arrays.asList(mjId));
			playCommand(player, mjList, BsMjDisAction.action_chupai);
		}
	}

	
	public void playBaoting(BsMjPlayer player, int baoting) {
		player.setBaoTingS(baoting);
		ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_bs_Baoting, player.getSeat(), player.getBaoTingS());
		for (Player tableplayer : getSeatMap().values()) {// ?????????????????????????????????
			tableplayer.writeSocket(com.build());
		}
		checkBegin(player);
		checkMo();
	}
	/**
	 * ?????????????????????
	 *
	 * @param actionIndex
	 *            CSMajiangConstants?????????
	 * @return
	 */
	public boolean canXiaoHu(int actionIndex) {
		// switch (actionIndex) {
		// case BsMjAction.QUEYISE:
		// return queYiSe == 1;
		// default:
		// return false;
		// }
		return false;
	}

	public void logFaPaiTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("BsMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append("faPai");
		sb.append("|").append(playType);
		sb.append("|").append(maxPlayerCount);
		sb.append("|").append(getPayType());
		sb.append("|").append(calcBird);
		sb.append("|").append(birdNum);
		sb.append("|").append(buyPoint);
		sb.append("|").append(youfeng);
		sb.append("|").append(yitiaolong);
		sb.append("|").append(siguiyi);
		sb.append("|").append(baoting);
		sb.append("|").append(chajiao);
		sb.append("|").append(lastWinSeat);
		LogUtil.msg(sb.toString());
	}

	public void logFaPaiPlayer(BsMjPlayer player, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("BsMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("faPai");
		sb.append("|").append(player.getHandMajiang());
		sb.append("|").append(actListToString(actList));
		LogUtil.msg(sb.toString());
	}

	public void logMoMj(BsMjPlayer player, BsMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("BsMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append("moPai");
		sb.append("|").append(getLeftMajiangCount());
		sb.append("|").append(mj);
		sb.append("|").append(actListToString(actList));
		sb.append("|").append(player.getHandMajiang());
		LogUtil.msg(sb.toString());
	}

	public void logChuPaiActList(BsMjPlayer player, BsMj mj, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("BsMj");
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

	public void logAction(BsMjPlayer player, int action, int xiaoHuType, List<BsMj> mjs, List<Integer> actList) {
		StringBuilder sb = new StringBuilder();
		sb.append("BsMj");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		String actStr = "unKnown-" + action;
		if (action == BsMjDisAction.action_peng) {
			actStr = "peng";
		} else if (action == BsMjDisAction.action_minggang) {
			actStr = "mingGang";
		} else if (action == BsMjDisAction.action_chupai) {
			actStr = "chuPai";
		} else if (action == BsMjDisAction.action_pass) {
			actStr = "guo";
		} else if (action == BsMjDisAction.action_angang) {
			actStr = "anGang";
		} else if (action == BsMjDisAction.action_chi) {
			actStr = "chi";
		} else if (action == BsMjDisAction.action_buzhang) {
			actStr = "buZhang";
		} else if (action == BsMjDisAction.action_xiaohu) {
			actStr = "xiaoHu";
		} else if (action == BsMjDisAction.action_buzhang_an) {
			actStr = "buZhangAn";
		}
		sb.append("|").append(xiaoHuType);
		sb.append("|").append(actStr);
		sb.append("|").append(mjs);
		sb.append("|").append(actListToString(actList));
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
				if (i == BsMjAction.HU) {
					sb.append("hu");
				} else if (i == BsMjAction.PENG) {
					sb.append("peng");
				} else if (i == BsMjAction.MINGGANG) {
					sb.append("mingGang");
				} else if (i == BsMjAction.ANGANG) {
					sb.append("anGang");
				} else if (i == BsMjAction.CHI) {
					sb.append("chi");
				} else if (i == BsMjAction.BUZHANG) {
					sb.append("buZhang");
				} else if (i == BsMjAction.QUEYISE) {
					sb.append("queYiSe");
				} else if (i == BsMjAction.BUZHANG_AN) {
					sb.append("buZhangAn");
				}
			}
		}
		sb.append("]");
		System.out.println("??????  ===========" + sb.toString());
		return sb.toString();
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param player
	 */
	public void processHideMj(BsMjPlayer player) {
		if (showMjSeat.contains(player.getSeat()) && disCardRound != 0) {
			PlayMajiangRes.Builder hideMj = PlayMajiangRes.newBuilder();
			buildPlayRes(hideMj, player, BsMjDisAction.action_hideMj, null);
			broadMsgToAll(hideMj.build());
			showMjSeat.remove(Integer.valueOf(player.getSeat()));
		}
	}

	public void clearShowMjSeat() {
		showMjSeat.clear();
		changeExtend();
	}

	public void addShowMjSeat(int seat, int xiaoHuType) {
		if (xiaoHuType == BsMjAction.QUEYISE) {
			if (!showMjSeat.contains(seat)) {
				showMjSeat.add(seat);
				changeExtend();
			}
		}

	}

	/**
	 * ????????????????????????
	 * 
	 * @return
	 */
	public boolean isBegin() {
		return isBegin && nowDisCardIds.size() == 0;
	}

	public void setIsBegin(boolean begin) {
		if (isBegin != begin) {
			isBegin = begin;
			changeExtend();
		}
	}

	@Override
	public boolean isAllReady() {
		if (super.isAllReady()) {
			if (getBuyPoint() > 0) {
				setTableStatus(BsMjConstants.TABLE_STATUS_PIAO);
				boolean bReturn = true;
				// ?????????????????????
				if (this.isTest()) {
					for (BsMjPlayer robotPlayer : seatMap.values()) {
						if (robotPlayer.isRobot()) {
							robotPlayer.setPiaoPoint(1);
						}
					}
				}
				for (BsMjPlayer player : seatMap.values()) {
					if (player.getPiaoPoint() < 0) {
						if (getBuyPoint() == 5) {
							player.setPiaoPoint(1);
							// ??????
							ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_buy_point,
									player.getSeat(), player.getPiaoPoint());
							for (Player tableplayer : getSeatMap().values()) {// ?????????????????????????????????
								tableplayer.writeSocket(com.build());
							}
						} else {
							ComRes.Builder com = SendMsgUtil
									.buildComRes(WebSocketMsgType.res_code_table_status_buy_point, getBuyPoint());
							player.writeSocket(com.build());
							bReturn = false;
						}
					}
				}
				return bReturn;
			} else {
				for (BsMjPlayer player : seatMap.values()) {
					player.setPiaoPoint(0);
				}
				return true;
			}
		}
		return false;
	}

	public void setTableStatus(int tableStatus) {
		this.tableStatus = tableStatus;
	}

	public int getTableStatus() {
		return tableStatus;
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
	 * ??????????????????????????????
	 */
	public void checkClearGangDisMajiang() {
		List<BsMj> moList = getGangDisMajiangs();
		if (moList != null && moList.size() > 0 && actionSeatMap.isEmpty()) {
			BsMjPlayer player = seatMap.get(getMoMajiangSeat());
			for (BsMjPlayer seatPlayer : seatMap.values()) {
				GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
				gangbuilder.setRemain(getLeftMajiangCount());
				gangbuilder.setGangId(gangMajiang.getId());
				gangbuilder.setUserId(player.getUserId() + "");
				gangbuilder.setName(player.getName() + "");
				gangbuilder.setSeat(player.getSeat());
				gangbuilder.setReconnect(0);
				gangbuilder.setDice(0);
				if (gangActedMj != null) {
					GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
					playBuilder.setMajiangId(gangActedMj.getId());
					gangbuilder.addGangActs(playBuilder);
				}
				seatPlayer.writeSocket(gangbuilder.build());
			}
			clearGangDisMajiangs();
		}
	}

	public void clearMoLastSeat() {
		moLastSeats.clear();
		changeExtend();
	}

	public void addMoLastSeat(int seat) {
		if (moLastSeats == null) {
			moLastSeats = new ArrayList<>();
		}
		moLastSeats.add(seat);
		changeExtend();
	}

	public void removeMoLastSeat(int seat) {
		int removIndex = -1;
		for (int i = 0; i < moLastSeats.size(); i++) {
			if (moLastSeats.get(i) == seat) {
				removIndex = i;
				break;
			}
		}
		if (removIndex != -1) {
			moLastSeats.remove(removIndex);
		}
		changeExtend();
	}

	/**
	 * ?????????????????????
	 * 
	 * @param player
	 * @param state
	 *            1????????????????????????0?????????????????????????????????
	 */
	public void sendMoLast(BsMjPlayer player, int state) {
		ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_asklastmajiang, state);
		player.writeSocket(res.build());
	}

	/**
	 * ?????????????????????
	 * 
	 * @return
	 */
	public boolean hasBaoTing() {
		for (BsMjPlayer seat : seatMap.values()) {
			if (seat.getBaoTingS() == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ?????????????????????
	 * 
	 * @return
	 */
	public boolean hasXiaoHu(List<Integer> actList) {
		if (BsMjAction.getFirstXiaoHu(actList) != -1) {
			return true;
		}
		return false;
	}

	public int getDealDice() {
		return dealDice;
	}

	public void setDealDice(int dealDice) {
		this.dealDice = dealDice;
	}

	public int getChajiao() {
		return chajiao;
	}

	public void setChajiao(int chajiao) {
		this.chajiao = chajiao;
	}

	public int getYoufeng() {
		return youfeng;
	}

	public void setYoufeng(int youfeng) {
		this.youfeng = youfeng;
	}

	public int getYitiaolong() {
		return yitiaolong;
	}

	public void setYitiaolong(int yitiaolong) {
		this.yitiaolong = yitiaolong;
	}

	public int getSiguiyi() {
		return siguiyi;
	}

	public void setSiguiyi(int siguiyi) {
		this.siguiyi = siguiyi;
	}

	public int getBaoting() {
		return baoting;
	}

	public void setBaoting(int baoting) {
		this.baoting = baoting;
	}

	public int getGangSeat() {
		return gangSeat;
	}

	public void setGangSeat(int gangSeat) {
		this.gangSeat = gangSeat;
	}

	public int getIsAutoPlay() {
		return isAutoPlay;
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

}
