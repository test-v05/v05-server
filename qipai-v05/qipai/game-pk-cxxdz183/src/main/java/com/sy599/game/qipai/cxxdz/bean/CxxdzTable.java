package com.sy599.game.qipai.cxxdz.bean;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.bean.CreateTableInfo;
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
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.cxxdz.constant.CxxdzConstants;
import com.sy599.game.qipai.cxxdz.tool.CardTool;
import com.sy599.game.qipai.cxxdz.util.CardUtils;
import com.sy599.game.qipai.cxxdz.util.DdzSfNew;
import com.sy599.game.qipai.cxxdz.util.DdzUtil;
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

public class CxxdzTable extends BaseTable {
	public static final String GAME_CODE = "Ddz";
	private static final int JSON_TAG = 1;
	/*** 当前牌桌上出的牌 */
	private volatile List<Integer> nowDisCardIds = new ArrayList<>();
	/*** 玩家map */
	private Map<Long, CxxdzPlayer> playerMap = new ConcurrentHashMap<Long, CxxdzPlayer>();
	/*** 座位对应的玩家 */
	private Map<Integer, CxxdzPlayer> seatMap = new ConcurrentHashMap
	/*** 最大玩家数量 */<Integer, CxxdzPlayer>();
	private volatile int maxPlayerCount = 3;

	// private volatile int showCardNumber = 0; // 是否显示剩余牌数量

	public static final int FAPAI_PLAYER_COUNT = 3;// 发牌人数

	private volatile int timeNum = 0;

	private volatile  int is3dai2;// 1三带2
	private volatile  int duiwu;//0= 铁队 ；1= 摸队：
	private volatile  String showCardsUser ;
	//随机亮牌
	private volatile  int randomFenZuCard=0;
	private int op_gametype;//
	private int op_fengding;
	private int op_rangpai;
	private int op_bujiabei;
	private int op_dipaijiabei;
	private int op_lunliuJiao;
	private int boomSuan2Fen;
	private int jiaBei;
	private int jiaBeiFen;
	private int jiaBeiShu;

	/**底牌 */
	private volatile List<Integer> dplist = new ArrayList<>();

	private volatile int bankRangPaiNumBeiShu=1;

	/**炸弹番数*/
	private volatile int boomBeiShu =1;

	/**选择加倍番数*/
	private volatile int sel_jiabeiBeiShu =1;
	private volatile int isCt = 0;

	/** 托管1：单局，2：全局 */
	private int autoPlayGlob;
	private int autoTableCount;

	// 新的一轮，3人为2人pass之后为新的一轮出牌
	private boolean newRound = true;
	// pass累计
	/**
	 * 托管时间
	 */
	private volatile int autoTimeOut = 5 * 24 * 60 * 60 * 1000;
	private volatile int autoTimeOut2 = 5 * 24 * 60 * 60 * 1000;

	// 是否已经发牌
	private int finishFapai = 0;

	// 一轮出的牌都会临时存在这里
	private volatile List<PlayerCard> noPassDisCard = new ArrayList<>();
	// 回放手牌
	private volatile String replayDisCard = "";

	private List<Integer> turnCards = new ArrayList<>();// 一个回合出的牌。


	private List<Integer> teamSeat = new ArrayList<>();// 队伍。

	private List<Integer> actionSeats = new ArrayList<>();// 可操作性的座位。

	private int banker = 0;// 庄座位

	private int turnFirstSeat = 1;// 当前轮第一个出牌的座位
	private int turnNum;// 回合，每出一轮为1回合

	/** 特殊状态 1 **/
	private int tableStatus = CxxdzConstants.TABLE_STATUS_ZERO;
	// 低于below加分
	private int belowAdd = 0;
	private int below = 0;

	private int tRank;
	private List<Integer> outfencard  = new ArrayList<>();

	public String getReplayDisCard() {
		return replayDisCard;
	}

	public void setReplayDisCard(String replayDisCard) {
		this.replayDisCard = replayDisCard;
	}






	//底分
	private int difen =1;
	//显示剩余
	private int xsyp =0;
	//王炸不可拆
	private int wzbc =0;
	//三个2必抢
	private int san2bq =0;



	//闷抓
	private int menzhua =0;
	//四代两对
	private int sidai2dui=0;
	//飞机不可带
	private int fjbkd=0;
	//四代随时出
	private int sdssc=0;
	//三带王随时出
	private int sdwssc=0;

	private List<Integer> canTiSeat=new ArrayList<>();
	//闷抓抢地主操作次数
	private int passQdz=0;
	private int dizhuSeat=0;



	public boolean createTable(CreateTableInfo createTableInfo)
			throws Exception {
		Player player = createTableInfo.getPlayer();
		int play = createTableInfo.getPlayType();
		int bureauCount =createTableInfo.getBureauCount();
		int tableType = createTableInfo.getTableType();
		List<Integer> params = createTableInfo.getIntParams();
		List<String> strParams = createTableInfo.getStrParams();
		boolean saveDb = createTableInfo.isSaveDb();

		long id = getCreateTableId(player.getUserId(), play);
		TableInf info = new TableInf();
		info.setTableId(id);
		info.setTableType(tableType);
		info.setMasterId(player.getUserId());
		info.setRoomId(0);
		info.setPlayType(play);
		info.setTotalBureau(bureauCount);
		info.setPlayBureau(1);
		info.setServerId(GameServerConfig.SERVER_ID);
		info.setCreateTime(new Date());
		info.setDaikaiTableId(daikaiTableId);
		info.setExtend(buildExtend());
		TableDao.getInstance().save(info);
		loadFromDB(info);

		payType = StringUtil.getIntValue(params, 2, 1);// 1AA,2房主
		op_fengding =StringUtil.getIntValue(params, 3, 24);// 封顶
		difen =StringUtil.getIntValue(params, 4, 0);//底分
		xsyp =StringUtil.getIntValue(params, 5, 0);//显示剩余
		wzbc =StringUtil.getIntValue(params, 6, 0);//王炸不可拆
		san2bq =StringUtil.getIntValue(params, 8, 0);//三个2必抢
		menzhua =StringUtil.getIntValue(params, 9, 0);//闷抓
		is3dai2=StringUtil.getIntValue(params, 10, 0);//三带一对
		sidai2dui=StringUtil.getIntValue(params, 11, 0);//四代两对
		maxPlayerCount = StringUtil.getIntValue(params, 7, 2);// 人数
		int time = StringUtil.getIntValue(params, 12, 0);
		this.autoPlay = time > 1;
		autoPlayGlob = StringUtil.getIntValue(params, 13, 0);// 1单局  2整局  3三局
		if (time > 0) {
			autoTimeOut2 = autoTimeOut = (time * 1000);
		}
		this.jiaBei = StringUtil.getIntValue(params, 14, 0);
		this.jiaBeiFen = StringUtil.getIntValue(params, 15, 100);
		this.jiaBeiShu = StringUtil.getIntValue(params, 16, 1);

		if(this.getMaxPlayerCount() != 2){
			jiaBei = 0 ;
		}
		if(maxPlayerCount==2){
			belowAdd = StringUtil.getIntValue(params, 17, 0);
			below = StringUtil.getIntValue(params, 18, 0);
		}
		fjbkd= StringUtil.getIntValue(params, 19, 0);//飞机不可带
		sdssc= StringUtil.getIntValue(params, 20, 0);//四代随时出
		sdwssc= StringUtil.getIntValue(params, 21, 0);//三带王随时出
		boomSuan2Fen = StringUtil.getIntValue(params, 22, 0);//炸弹算2分
		setLastActionTime(TimeUtil.currentTimeMillis());
		return true;
	}


	@Override
	public JsonWrapper buildExtend0(JsonWrapper wrapper) {
		for (CxxdzPlayer player : seatMap.values()) {
			wrapper.putString(player.getSeat(), player.toExtendStr());
		}
		wrapper.putInt(5, maxPlayerCount);
		wrapper.putInt(6, autoTimeOut);
		wrapper.putInt(7, autoPlayGlob);
		wrapper.putInt(8, difen);
		wrapper.putInt(9, newRound ? 1 : 0);
		wrapper.putInt(10, finishFapai);
		wrapper.putInt(11, belowAdd);
		wrapper.putInt(12, below);
		wrapper.putInt(13, banker);
		wrapper.putInt(14, turnFirstSeat);
		wrapper.putInt(15, turnNum);
		wrapper.putInt(16, tableStatus);
		wrapper.putInt(17, tRank);
		wrapper.putInt(18, op_gametype);
		wrapper.putInt(19, op_fengding);
		wrapper.putInt(20, op_rangpai);
		wrapper.putInt(21, op_bujiabei);
		wrapper.putInt(22, op_dipaijiabei);
		wrapper.putInt(23, is3dai2);
		wrapper.putInt(26, op_lunliuJiao);
		wrapper.putInt(27, jiaBei);
		wrapper.putInt(28, jiaBeiFen);
		wrapper.putInt(29, jiaBeiShu);
		//牌桌数据
		wrapper.putString(33, StringUtil.implode(dplist,","));
		wrapper.putInt(36, boomBeiShu);
		wrapper.putInt(37, sel_jiabeiBeiShu);
		wrapper.putInt(38, bankRangPaiNumBeiShu);
		wrapper.putInt(40, xsyp);
		wrapper.putInt(41, wzbc);
		wrapper.putInt(42, san2bq);
		wrapper.putInt(43, menzhua);
		wrapper.putInt(44, sidai2dui);
		wrapper.putInt(45, fjbkd);
		wrapper.putInt(46, sdssc);
		wrapper.putInt(47, sdwssc);
		wrapper.putInt(48, dizhuSeat);
		wrapper.putInt(49, passQdz);
		wrapper.putString(50, StringUtil.implode(canTiSeat,","));
		wrapper.putInt(51, boomSuan2Fen);
		return wrapper;
	}
	@Override
	public void initExtend0(JsonWrapper wrapper) {
		for (CxxdzPlayer player : seatMap.values()) {
			player.initExtend(wrapper.getString(player.getSeat()));
		}
		maxPlayerCount = wrapper.getInt(5, 4);
		if (maxPlayerCount == 0) {
			maxPlayerCount = 2;
		}
		if (payType == -1) {
			String isAAStr = wrapper.getString("isAAConsume");
			if (!StringUtils.isBlank(isAAStr)) {
				this.payType = Boolean.parseBoolean(wrapper.getString("isAAConsume")) ? 1 : 2;
			} else {
				payType = 1;
			}
		}
		autoTimeOut = wrapper.getInt(6, 0);
		autoPlayGlob = wrapper.getInt(7, 0);
		difen = wrapper.getInt(8, 0);
		newRound = wrapper.getInt(9, 1) == 1;
		finishFapai = wrapper.getInt(10, 0);
		belowAdd = wrapper.getInt(11, 0);
		below = wrapper.getInt(12, 0);
		autoTimeOut2 = autoTimeOut;
		// 设置默认值
		if (autoPlay && autoTimeOut <= 1) {
			autoTimeOut2 = autoTimeOut = 60000;
		}
		banker = wrapper.getInt(13, 0);
		turnFirstSeat = wrapper.getInt(14, 0);
		turnNum = wrapper.getInt(15, 0);
		tableStatus = wrapper.getInt(16, 0);
		tRank = wrapper.getInt(17, 0);
		op_gametype = wrapper.getInt(18, 0);
		op_fengding = wrapper.getInt(19, 0);
		op_rangpai = wrapper.getInt(20, 0);
		op_bujiabei = wrapper.getInt(21, 0);
		op_dipaijiabei = wrapper.getInt(22, 0);
		is3dai2 = wrapper.getInt(23, 0);
		op_lunliuJiao = wrapper.getInt(26, 0);
		jiaBei = wrapper.getInt(27, 0);
		jiaBeiFen = wrapper.getInt(28, 0);
		jiaBeiShu = wrapper.getInt(29, 0);

		String dp=wrapper.getString(33);
		dplist = StringUtil.explodeToIntList(dp);
		boomBeiShu=wrapper.getInt(36, 1);
		sel_jiabeiBeiShu=wrapper.getInt(37, 1);
		bankRangPaiNumBeiShu=wrapper.getInt(38, 1);
		if(maxPlayerCount != 2){
			bankRangPaiNumBeiShu = 1;
		}
		xsyp =wrapper.getInt(40, 0);
		wzbc =wrapper.getInt(41, 0);
		san2bq =wrapper.getInt(42, 0);
		menzhua =wrapper.getInt(43, 0);
		sidai2dui =wrapper.getInt(44, 0);
		fjbkd =wrapper.getInt(45, 0);
		sdssc =wrapper.getInt(46, 0);
		sdwssc =wrapper.getInt(47, 0);
		dizhuSeat =wrapper.getInt(48, 0);
		passQdz =wrapper.getInt(49, 0);
		String canTiSeat=wrapper.getString(50);
		if(canTiSeat!=null&&!"".equals(canTiSeat))
			this.canTiSeat = StringUtil.explodeToIntList(canTiSeat);
		boomSuan2Fen =wrapper.getInt(51, 0);
	}

	@Override
	protected void loadFromDB1(TableInf info) {
		if (!StringUtils.isBlank(info.getNowDisCardIds())) {
			this.nowDisCardIds = StringUtil.explodeToIntList(info.getNowDisCardIds());
		}

		if (!StringUtils.isBlank(info.getHandPai9())) {
			this.teamSeat = StringUtil.explodeToIntList(info.getHandPai9());
		}

		if (!StringUtils.isBlank(info.getHandPai10())) {
			this.actionSeats = StringUtil.explodeToIntList(info.getHandPai10());
		}

		if (!StringUtils.isBlank(info.getOutPai9())) {
			this.turnCards = StringUtil.explodeToIntList(info.getOutPai9());
		}
		if (!StringUtils.isBlank(info.getHandPai8())) {
			//明牌的玩家
			String  showcardsuser = info.getHandPai8() ;
			for (CxxdzPlayer sb: seatMap.values()){
				if(showcardsuser.contains(String.valueOf(sb.getSeat()))){
					sb.setShowCards(1);
				}
			}
		}
		if (!StringUtils.isBlank(info.getHandPai7())) {
			//庄
			this.banker =Integer.valueOf(info.getHandPai7()) ;
		}
		if (!StringUtils.isBlank(info.getHandPai6())) {
			//出过的分牌
			String  cards =String.valueOf(info.getHandPai6()) ;
			if(cards.length()>0){
				String[] cardsary = cards.split(",");
				for (String c : cardsary){
					this.outfencard.add(Integer.valueOf(c));
				}
			}
		}
	}

	public long getId() {
		return id;
	}

	public CxxdzPlayer getPlayer(long id) {
		return playerMap.get(id);
	}

	/**
	 * 一局结束
	 */
	public void calcOver() {
		//

		boolean isOver = playBureau >= totalBureau;
		boolean dizhuWin=false;
		if(seatMap.get(dizhuSeat).getHandPais().size()==0){
			dizhuWin=true;
			CxxdzPlayer dizhuPlayer = seatMap.get(dizhuSeat);
			boolean isChuntian = true;
			for (CxxdzPlayer dp : seatMap.values()) {
				if (dp.getSeat()!=dizhuSeat) {
					if(dp.getHandPais().size() < 17 || dp.getCpnum()>0){
						isChuntian = false;
					}
				}
			}
			//计算总番数
			if(isChuntian)
				setCt(1);
			int score = 0;
			int countScore=0;
			for (CxxdzPlayer dp : seatMap.values()) {
				if(dp.getSeat() != dizhuSeat){
					score=difen*countBeiShuFD(dp.getSeat());
					dp.setPoint(-score);
					dp.changePlayPoint(-score);
					countScore+=score;
				}
			}
			dizhuPlayer.setPoint(countScore);
			dizhuPlayer.changePlayPoint(countScore);
		}else{
			//农民赢
			CxxdzPlayer dz = seatMap.get(dizhuSeat);
			if(dz.getCpnum()==1)
				setCt(2);

			int score = 0;
			int countScore=0;
			for (CxxdzPlayer dp : seatMap.values()) {
				if (dp.getSeat()!=dizhuSeat) {
					score=difen*countBeiShuFD(dp.getSeat());
					dp.setPoint(score);
					dp.changePlayPoint(score);
					countScore+=score;
				}
			}
			dz.setPoint(-countScore);
			dz.changePlayPoint(-countScore);
		}
		
		for (CxxdzPlayer dp : seatMap.values()) {
			if(dp.getSeat()==dizhuSeat){
				dp.changeAction(0,1);//地主次数
			}
			if(dp.getPoint()>0){
				dp.changeAction(1,1);//赢次数
			}
			
		}
		
		for (CxxdzPlayer dtp : seatMap.values()) {
			if (totalBureau >= 100) {
				if (dtp.getTotalPoint() >= totalBureau) {
					isOver = true;
				}
			}
		}
		
		if (autoPlayGlob > 0) {
			// //是否解散
			boolean diss = false;
			if (autoPlayGlob == 1) {
				for (CxxdzPlayer seat : seatMap.values()) {
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
		/// 算分提取于消息前
		if(isOver){
			calcPointBeforeOver();
		}
		
		if(isGoldRoom()){
			calcGoldRoom();
		}
		calcAfter();

		//流程变更 如果地主闷抓  结算发送农民底牌消息
		if(seatMap.get(dizhuSeat).getMengzhua()==1){
			System.err.println("地主闷抓 发送结算前底牌 "+dplist);
			for (CxxdzPlayer p : seatMap.values()) {
				if(p.getSeat()!=dizhuSeat){
					ComRes.Builder dipaiMsg = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_DIPAIMSG,dizhuSeat,countBeiShuFD(dizhuSeat),dplist);
					p.writeSocket(dipaiMsg.build());
				}
			}
		}
		ClosingInfoRes.Builder res = sendAccountsMsg(isOver, false, dizhuWin);
		saveLog(isOver, 0, res.build());
		if (isOver) {
			calcOver1();
			calcOver2();
			calcOver3();
			diss();
			
		} else {
			initNext();
			calcOver1();
		}
		outfencard.clear();
	}

	public void calcPointBeforeOver() {
		if(jiaBei == 1){
			int jiaBeiPoint = 0;
			int loserCount = 0;
			for (CxxdzPlayer player : seatMap.values()) {
				if (player.getPlayPoint() > 0 && player.getPlayPoint() < jiaBeiFen) {
					jiaBeiPoint += player.getPlayPoint() * (jiaBeiShu - 1);
					player.setPlayPoint(player.getPlayPoint() * jiaBeiShu);
				} else if (player.getPlayPoint() < 0) {
					loserCount++;
				}
			}
			if (jiaBeiPoint > 0) {
				for (CxxdzPlayer player : seatMap.values()) {
					if (player.getPlayPoint() < 0) {
						player.setPlayPoint(player.getPlayPoint() - (jiaBeiPoint / loserCount));
					}
				}
			}
		}
		// 大结算低于below分+belowAdd分
		if (belowAdd > 0 && playerMap.size() == 2) {
			for (CxxdzPlayer player : seatMap.values()) {
				int totalPoint = player.getPlayPoint();
				if (totalPoint > -below && totalPoint < 0) {
					player.setPlayPoint(player.getPlayPoint() - belowAdd);
				} else if (totalPoint < below && totalPoint > 0) {
					player.setPlayPoint(player.getPlayPoint() + belowAdd);
				}
			}
		}
	}

	private boolean checkAuto3() {
		boolean diss = false;
		// if(autoPlayGlob==3) {
		boolean diss2 = false;
		for (CxxdzPlayer seat : seatMap.values()) {
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

	public int countBeiShuFD(int countSeat){
 		CxxdzPlayer dizhu = seatMap.get(dizhuSeat);
		int bei=0;
		if(countSeat==dizhuSeat){
			for (CxxdzPlayer player:seatMap.values())
				if (player.getSeat() != countSeat) {
					bei+= countTwoPeopleBeiFD(dizhu, player,isCt>0);
				}
		}else {
			bei =countTwoPeopleBeiFD(dizhu,seatMap.get(countSeat),isCt>0);
		}
		return bei;
	}

	/**
	 *
	 * @param player1 地主
	 * @param player2 闲
	 * @param ct
	 * @return
	 */
	public int countTwoPeopleBeiFD(CxxdzPlayer player1,CxxdzPlayer player2,boolean ct){
		int bei=1;

		if(player1!=null&&player2!=null)
			bei=player1.getBeishu()*player2.getBeishu();
		bei*=boomBeiShu;
		if(passQdz>=2 && getMaxPlayerCount()==3 && menzhua==0){
			bei*=2;//3人为例，现在为地主2倍，闲家为1倍，改为地主4倍，闲家2倍） 在原来基础上X2
		}
		if(ct)
			bei*=2;
		if(op_fengding!=0&&bei>op_fengding)
			bei=op_fengding;
		return bei;
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

			for (CxxdzPlayer player : playerMap.values()) {
				// 总小局数
				DataStatistics dataStatistics1 = new DataStatistics(dataDate, "group" + groupId,
						String.valueOf(player.getUserId()), String.valueOf(playType), "xjsCount", playedBureau);
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics1, 3);
				// 总大局数
				DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId,
						String.valueOf(player.getUserId()), String.valueOf(playType), "djsCount", 1);
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics5, 3);
				// 总积分
				DataStatistics dataStatistics6 = new DataStatistics(dataDate, "group" + groupId,
						String.valueOf(player.getUserId()), String.valueOf(playType), "zjfCount", player.loadScore());
				DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics6, 3);
				if (player.loadScore() > 0) {
					if (player.loadScore() > maxPoint) {
						maxPoint = player.loadScore();
					}
					// 单大局赢最多
					DataStatistics dataStatistics2 = new DataStatistics(dataDate, "group" + groupId,
							String.valueOf(player.getUserId()), String.valueOf(playType), "winMaxScore",
							player.loadScore());
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics2, 4);
				} else if (player.loadScore() < 0) {
					if (player.loadScore() < minPoint) {
						minPoint = player.loadScore();
					}
					// 单大局输最多
					DataStatistics dataStatistics3 = new DataStatistics(dataDate, "group" + groupId,
							String.valueOf(player.getUserId()), String.valueOf(playType), "loseMaxScore",
							player.loadScore());
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics3, 5);
				}
			}

			for (CxxdzPlayer player : playerMap.values()) {
				if (maxPoint > 0 && maxPoint == player.loadScore()) {
					// 单大局大赢家
					DataStatistics dataStatistics4 = new DataStatistics(dataDate, "group" + groupId,
							String.valueOf(player.getUserId()), String.valueOf(playType), "dyjCount", 1);
					DataStatisticsDao.getInstance().saveOrUpdateDataStatistics(dataStatistics4, 1);
				} else if (minPoint < 0 && minPoint == player.loadScore()) {
					// 单大局大负豪
					DataStatistics dataStatistics5 = new DataStatistics(dataDate, "group" + groupId,
							String.valueOf(player.getUserId()), String.valueOf(playType), "dfhCount", 1);
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
			for (CxxdzPlayer player : playerMap.values()) {
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

			if (tempMap.containsKey("answerDiss")) {
				tempMap.put("answerDiss", buildDissInfo());
			}
			if (tempMap.containsKey("nowDisCardIds")) {
				tempMap.put("nowDisCardIds", StringUtil.implode(nowDisCardIds, ","));
			}

			if (tempMap.containsKey("handPai9")) {
				tempMap.put("handPai9", StringUtil.implode(teamSeat, ","));
				// tempMap.put("handPai10", StringUtil.implode(currCards, ","));
			}

			if (tempMap.containsKey("handPai10")) {
				tempMap.put("handPai10", StringUtil.implode(actionSeats, ","));
			}

			if (tempMap.containsKey("outPai9")) {
				tempMap.put("outPai9", StringUtil.implode(turnCards, ","));
			}

			if (tempMap.containsKey("extend")) {
				tempMap.put("extend", buildExtend());
			}

			// TableDao.getInstance().save(tempMap);
		}
		return tempMap.size() > 0 ? tempMap : null;
	}



	protected String buildPlayersInfo() {
		StringBuilder sb = new StringBuilder();
		for (CxxdzPlayer pdkPlayer : playerMap.values()) {
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

	public void changeLoaclTableStatus(int tableStatus){
		if(tableStatus==CxxdzConstants.TABLE_STATUS_MZ&& menzhua ==0){
			changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_QDZ);
			return;
		}
		if(tableStatus==CxxdzConstants.TABLE_STATUS_T1J&&canTiSeat.size()==0){
			changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_FINISH);
			return;
		}
		if(this.tableStatus!=tableStatus){
			setTableStatus(tableStatus);
			sendUniformByTableStatus();
		}
	}

	public void sendUniformByTableStatus(){
		switch (tableStatus){
			case CxxdzConstants.TABLE_STATUS_MZ:
			 	if(getPassCount()==2){
				 	for (CxxdzPlayer p : seatMap.values()) {
						p.writeComMessage(WebSocketMsgType.com_cxxdz_mustMengzhua,dizhuSeat);
					}
				}else{
					for (CxxdzPlayer p : seatMap.values()) {
						p.writeComMessage(WebSocketMsgType.com_cxxdz_mengzhua,dizhuSeat);
					}
				}
				break;
			case CxxdzConstants.TABLE_STATUS_QDZ:
				for (CxxdzPlayer p : seatMap.values()) {
					p.writeComMessage(WebSocketMsgType.com_cxxdz_qdz,dizhuSeat,-1,mustQdz(p)?1:0);
				}
				break;
			case CxxdzConstants.TABLE_STATUS_T1J:
				for (CxxdzPlayer p : seatMap.values()) {
					if(p.getLookCard()==1)
						p.writeComMessage(WebSocketMsgType.com_cxxdz_t1j,canTiSeat.contains(p.getSeat())?1:0);
					else
						p.writeComMessage(WebSocketMsgType.com_cxxdz_menT,canTiSeat.contains(p.getSeat())?1:0);
				}
				break;
			case CxxdzConstants.TABLE_STATUS_H1J:
				for (CxxdzPlayer p : seatMap.values()) {
					p.writeComMessage(WebSocketMsgType.com_cxxdz_h1j,dizhuSeat);
				}
				break;
			case CxxdzConstants.TABLE_STATUS_FINISH:
				for (CxxdzPlayer p : seatMap.values()) {
					p.lookCard();
					p.writeComMessage(WebSocketMsgType.com_cxxdz_finishStart,dizhuSeat,countBeiShuFD(p.getSeat()));
				}
				break;
		}
	}

	public void reconnectBeforeAct(CxxdzPlayer player){
		switch (tableStatus){
			case CxxdzConstants.TABLE_STATUS_MZ:
				player.writeComMessage(WebSocketMsgType.com_cxxdz_mengzhua,dizhuSeat);
				if(getPassCount()==2){
					player.writeComMessage(WebSocketMsgType.com_cxxdz_mustMengzhua,dizhuSeat);
				}
				break;
			case CxxdzConstants.TABLE_STATUS_QDZ:
				player.writeComMessage(WebSocketMsgType.com_cxxdz_qdz,dizhuSeat,-1,mustQdz(player)?1:0);
				break;
			case CxxdzConstants.TABLE_STATUS_T1J:
				if(player.getLookCard()==1)
					player.writeComMessage(WebSocketMsgType.com_cxxdz_t1j,canTiSeat.contains(player.getSeat())?1:0);
				else
					player.writeComMessage(WebSocketMsgType.com_cxxdz_menT,canTiSeat.contains(player.getSeat())?1:0);
				break;
			case CxxdzConstants.TABLE_STATUS_H1J:
				player.writeComMessage(WebSocketMsgType.com_cxxdz_h1j,dizhuSeat);
				break;
		}
	}

	public void broadcastMsg(int msgCode, CxxdzPlayer player, int act){
		addPlayLog(addCxxdzPlayLog(player.getSeat(),msgCode,act,null,false));
		for (Player p:seatMap.values()) {
			p.writeComMessage(msgCode,player.getSeat(),act,countBeiShuFD(p.getSeat()));
			//System.out.println("-------------"+p.getName()+"|"+msgCode+"|"+act+"|"+countBeiShuFD(p.getSeat()));
		}
	}


	public synchronized void menzhua(int param,CxxdzPlayer player){
		if(player.getDizhu()==1||player.getLookCard()==1)
			return;

		canTiSeat.remove((Integer) player.getSeat());
		//选择闷抓
		if(param==1){
			player.setMengzhua(1);
			broadcastMsg(WebSocketMsgType.com_cxxdz_mengzhua,player,param);
			confirmDz(player);
			if(menzhua==0){
				//勾选闷抓 调整为轮到地主出牌前才能看牌（保证地主在选闷回时看不到牌）
				player.lookdp();
			}
			changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_T1J);
		}else{
			//看牌
			player.lookCard();
			broadcastMsg(WebSocketMsgType.com_cxxdz_mengzhua,player,param);
			changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_QDZ);
		}
	}


	private void confirmDz(CxxdzPlayer player){
		setDizhuSeat(player.getSeat());
		seatMap.get(dizhuSeat).getHandPais().addAll(dplist);
		changeCards(player.getSeat());
		player.setDizhu(1);
		setNowDisCardSeat(dizhuSeat);

		CxxdzPlayer dizhup = seatMap.get(dizhuSeat);
		for (CxxdzPlayer p : seatMap.values()) {
			if(dizhup.getMengzhua()==1){
				//xiugai 闷抓 地主可以看到底牌，但不允许农民看到底牌,
				if(p.getSeat() == dizhuSeat){
					ComRes.Builder dipaiMsg = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_DIPAIMSG,dizhuSeat,countBeiShuFD(dizhuSeat),dplist);
					p.writeSocket(dipaiMsg.build());
				}else{
					List<Integer> dp = Arrays.asList(0,0,0);
					ComRes.Builder dipaiMsg = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_DIPAIMSG,dizhuSeat,countBeiShuFD(dizhuSeat),dp);
					p.writeSocket(dipaiMsg.build());
				}
			}else{
				ComRes.Builder dipaiMsg = SendMsgUtil.buildComRes(WebSocketMsgType.REQ_2RenDDZ_DIPAIMSG,dizhuSeat,countBeiShuFD(dizhuSeat),dplist);
				p.writeSocket(dipaiMsg.build());
			}

		}

		addPlayLog(addCxxdzPlayLog(dizhuSeat,WebSocketMsgType.REQ_2RenDDZ_DIPAIMSG,null,dplist,false));
	}

	public synchronized void qdz(int param,CxxdzPlayer player){
		if(player.getSeat()!=dizhuSeat)
			return;
		canTiSeat.remove((Integer) player.getSeat());
		if(param==1){
			if(player.getDizhu()==1)
				return;
			confirmDz(player);
			for (CxxdzPlayer p:seatMap.values()) {
				p.lookdp();
				p.lookCard();
			}

			broadcastMsg(WebSocketMsgType.com_cxxdz_qdz,player,param);
			changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_T1J);
		}else {
			if(player.getPassDz()==1)
				return;
			if(mustQdz(player)){
				return;
			}
			setPassQdz(passQdz+1);
			broadcastMsg(WebSocketMsgType.com_cxxdz_qdz,player,param);
			changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_ZERO);
			if(passQdz>=maxPlayerCount){
				setPassQdz(0);
				canTiSeat.clear();
				for (CxxdzPlayer p:seatMap.values()) {
					p.clearStart();
				}
				//重新发牌
				fapai();
				playLog="";
				for (int i = 1; i <= getMaxPlayerCount(); i++) {
					CxxdzPlayer p = seatMap.get(i);
					addPlayLog(StringUtil.implode(p.getHandPais(), ","));
				}
				sendDealMsg(0);
				startNext();
			}else {
				player.setPassDz(1);
				setDizhuSeat(calcNextSeat(player.getSeat()));
				changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_MZ);
			}
		}
	}

	public List<Integer> getTiSeat(){
		List<Integer> tiSeat=new ArrayList<>();
		for (CxxdzPlayer player:seatMap.values()) {
			if(player.getT1j()==1||player.getMenT()==1)
				tiSeat.add(player.getSeat());
		}
		return tiSeat;
	}

	public synchronized void t1j(int param,CxxdzPlayer player){
		if(!canTiSeat.contains(player.getSeat()))
			return;

		if(canTiSeat.contains(player.getSeat())&&param==1){
			if(player.getT1j()==1){
				return;
			}
			player.setT1j(1);
		}


		canTiSeat.remove((Integer) player.getSeat());
		broadcastMsg(WebSocketMsgType.com_cxxdz_t1j_result,player,param);
		if(canTiSeat.size()==0){
			List<Integer> tiSeat = getTiSeat();
			if(tiSeat.size()==0)//还没对
				changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_FINISH);
			else {
				changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_H1J);
			}
		}

	}

	public synchronized void menT(int param,CxxdzPlayer player){
		if(!canTiSeat.contains(player.getSeat()))
			return;

		player.lookCard();
		if(canTiSeat.contains(player.getSeat())&&param==1){
			if(player.getMenT()==1){
				return;
			}
			player.setMenT(1);
		}


		canTiSeat.remove((Integer) player.getSeat());
		broadcastMsg(WebSocketMsgType.com_cxxdz_menT_result,player,param);
		if(canTiSeat.size()==0){
			List<Integer> tiSeat = getTiSeat();
			if(tiSeat.size()==0)
				changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_FINISH);
			else {
				changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_H1J);
			}
		}
	}

	public synchronized void h1j(int param,CxxdzPlayer player){
		if(player.getSeat()!=dizhuSeat)
			return;
		if(player.getH1j()==1){
			//已经回了
			return;
		}
		if(param==1){
			for (CxxdzPlayer p:seatMap.values()) {
				if(p.getT1j()==1||p.getMenT()==1)
					player.setH1j(1,p);
			}
		}

		if(menzhua==1){
			player.lookCard();
			//调整为轮到地主出牌前才能看牌（保证地主在选闷回时看不到牌）
			player.lookdp();
		}
		broadcastMsg(WebSocketMsgType.com_cxxdz_h1j,player,param);
		changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_FINISH);
	}

	public boolean mustQdz(CxxdzPlayer player){
		int countFen=0;
		int num2=0;
		for (Integer id:player.getHandPais()) {
			if(id==501||id==502){
				//大小王2分
				countFen+=2;
			}else if(id%100==15){
				countFen+=1;
				num2++;
			}
		}
		if(boomSuan2Fen ==1){
			countFen += DdzSfNew.getBoomNum(player.getHandPais()) * 2;
		}
		if(countFen>=4||(san2bq==1&&num2>=3))
			return true;
		if(maxPlayerCount==3&&passQdz==2)
			return true;
		return false;
	}

	/**
	 * 看牌且不抢地主人数
	 * @return
	 */
	public int getPassCount(){
		if(maxPlayerCount==3){
			int passCount =0;
			for (CxxdzPlayer p:seatMap.values() ) {
				if(p.getPassDz()==1 && p.getMengzhua()==0){
					//不抢 且 看牌人数
					passCount++;
				}
			}
			return passCount;
		}else{
			return 0;
		}
	}







	/**
	 * 开始发牌
	 */
	public void fapai() {
		playLog = "";
		synchronized (this) {
			changeTableState(table_state.play);
			timeNum = 0;
			List<List<Integer>> fplist = CardTool.fapai(maxPlayerCount,zp);
			setDplist(fplist.get(3));
			int i = 0;
			randomFenZuCard =fplist.get(4).get(0);
			for (CxxdzPlayer player :seatMap.values()) {
				player.changeState(player_state.play);
				player.dealHandPais(fplist.get(i), this);
				if (!player.isAutoPlay() || isGoldRoom()) {
					player.setAutoPlay(false, this);
					player.setLastOperateTime(System.currentTimeMillis());
				}
				i++;
			}

			for (CxxdzPlayer player :seatMap.values()){
				if(player.getHandPais().contains(randomFenZuCard)){
					setDizhuSeat(player.getSeat());
				}else {
					canTiSeat.add(player.getSeat());
				}
				if(menzhua ==0)
					player.setLookCard(1);
			}
		}
		finishFapai = 1;
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

	public CxxdzPlayer getPlayerBySeat(int seat) {
		// int next = seat >= maxPlayerCount ? 1 : seat + 1;
		return seatMap.get(seat);

	}

	public Map<Integer, Player> getSeatMap() {
		Object o = seatMap;
		return (Map<Integer, Player>) o;
	}

	public void startNext() {
		if(finishFapai==1){
			for (CxxdzPlayer player:seatMap.values()) {
				player.writeComMessage(WebSocketMsgType.com_cxxdz_showCard,randomFenZuCard);
			}
			changeLoaclTableStatus(CxxdzConstants.TABLE_STATUS_MZ);
		}
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
			CxxdzPlayer reconnet_player = null;
			for (CxxdzPlayer player : playerMap.values()) {
				if (player.getUserId() == userId) {
					reconnet_player = player;
					break;
				}
			}
			int reconnet_player_teamseat = 0;
			if(!isGoldRoom()){
				List<Integer> teamlist = getTeamPlayerSeat(reconnet_player.getSeat());
				if(!teamlist.isEmpty()){
					reconnet_player_teamseat = teamlist.get(0);
				}
			}

			for (CxxdzPlayer player : playerMap.values()) {
				PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(userId, isrecover);
				if (playerRes == null) {
					continue;
				}
				if (player.getUserId() == userId) {
					// 如果是自己重连能看到手牌
					playerRes.addAllHandCardIds(player.getHandPais());
				} else {
					//  判断重连玩家是否已经明牌（明牌=1） 如果是。那么吧轮询到的队友的手牌设置进去
					if(reconnet_player.isShowCards()==1 && player.getSeat()== reconnet_player_teamseat ){
						playerRes.addAllHandCardIds(player.getHandPais());
					}
				}
				if(player.isShowCards()==1 && player.getUserId() != userId && player.getSeat()!= reconnet_player_teamseat){
					playerRes.addAllHandCardIds(player.getHandPais());
				}
				if (player.getHandPais().isEmpty()) {
					List<Integer> teamSeats = getTeamPlayerSeat(player.getSeat());
					if (!teamSeats.isEmpty() ) {
						CxxdzPlayer tp = seatMap.get(teamSeats.get(0));
						playerRes.addAllMoldIds(tp.getHandPais());
					}
				}
				players.add(playerRes.build());
			}
			res.addAllPlayers(players);
			res.setNextSeat(nowDisCardSeat);
			res.setRemain(getTableStatus());
			res.setRenshu(this.maxPlayerCount);
			res.addExt(this.payType);// 0支付方式
			if(tableStatus>CxxdzConstants.TABLE_STATUS_QDZ)
				res.addExt(dizhuSeat);// 1 地主座位号
			else
				res.addExt(0);// 1 地主座位号
			int ratio;
			int pay;
			ratio = 1;
			pay = consumeCards() ? loadPayConfig(payType) : 0;
			List<Integer> scoreCards = DdzUtil.getScoreCardsList(getTurnCards());
			if (scoreCards.size() > 0) {
				res.addAllScoreCard(scoreCards);
				int totalScore = DdzUtil.getScoreCards(getTurnCards());
				res.addExt(totalScore);//2
			} else {
				res.addExt(0);// 2
			}
			res.addExt(ratio);// 3
			res.addExt(pay);// 4
			res.addExt(lastWinSeat);//5
			res.addExtStr(String.valueOf(matchId));// 0
			res.addExtStr(cardMarkerToJSON());// 1
			res.addTimeOut(autoPlay ? autoTimeOut : 0);//

			res.addExt(playedBureau);// 6
			res.addExt(disCardRound);// 7
			res.addExt(creditMode); // 8
			res.addExt(creditCommissionMode1);// 9
			res.addExt(creditCommissionMode2);// 10
			res.addExt(autoPlay ? 1 : 0);// 11
			res.addExt(tableStatus);// 12
			res.addExt(0);//13
			res.addExt(0);// 14 抢地主倍数 每抢一次地主X2
			res.addExt(0);// 15 让牌张数 每抢一次+1
			res.addExt(0);//16 底牌倍数
			res.addExt(0);//17 勾选让牌 地主的让牌张数
			res.addExt(0);//18 重发牌次数 5次后解散
			res.addExt(boomBeiShu);//19 炸弹番数
			res.addExt(sel_jiabeiBeiShu);//21 选择加倍番数
			res.addExt(boomBeiShu*sel_jiabeiBeiShu*bankRangPaiNumBeiShu);  // 22总倍数


			if(getTableStatus()>CxxdzConstants.TABLE_STATUS_ZERO){
				res.addStrExt(StringUtil.implode(dplist,","));
			}else{
				List<Integer> d = new ArrayList<>();
				d.add(0);d.add(0);d.add(0);
				res.addStrExt(StringUtil.implode(d,","));//  底牌
			}
		}

		return res.build();
	}

	public int getOnTablePlayerNum() {
		int num = 0;
		for (CxxdzPlayer player : seatMap.values()) {
			if (player.getIsLeave() == 0) {
				num++;
			}
		}
		return num;
	}


	public void disCards(CxxdzPlayer player, int action, List<Integer> cards) {

		List<Integer> chuPaiCards = new ArrayList<>();
		if(!cards.isEmpty()){
			chuPaiCards.addAll(cards);
		}

		int code = 0;
		if (action == 0) {
			code = chupai(player, action, cards);
			if (code < 0)
				return;
			setTurnFirstSeat(player.getSeat());
			//其他该出牌的玩家座位
			List<Integer> chuPiaSeat = new ArrayList<Integer>();
			for (CxxdzPlayer p : seatMap.values()) {
				if (p.getSeat()==player.getSeat()|| p.getHandPais().isEmpty()) {
					continue;
				}
				chuPiaSeat.add(p.getSeat());
			}
			setActionSeats(chuPiaSeat);
		}else {
			removeActionSeat(player.getSeat());
		}
		player.setActionS(action);
		setDisCardSeat(player.getSeat());
		// 构建出牌消息
		PlayCardRes.Builder res = PlayCardRes.newBuilder() ;
		res.setIsClearDesk(0);
		res.setCardType(action);
		boolean isOver = false;
		boolean turnOver = false;
		if ( checkTiqianOver(player)||getActionSeats().isEmpty()) {// 一轮打完
			turnOver = true;
			CxxdzPlayer winPlayer = seatMap.get(getTurnFirstSeat());
			isOver = turnOver(res, winPlayer);
		} else
			setNowDisCardSeat(getNextPlaySeat(player.getSeat()));

		if(turnOver && action==1 && cards.isEmpty())
			setNowDisCardSeat(getNextPlaySeat(player.getSeat()));
		addPlayLog(addCxxdzPlayLog(player.getSeat(), action,null,chuPaiCards,turnOver));
		addGameActionLog(player, "chupai|" + action + "|" + chuPaiCards+"|"+getNowDisCardSeat());

		res.addAllCardIds(chuPaiCards);
		res.setNextSeat(getNowDisCardSeat());
		res.setUserId(player.getUserId() + "");
		res.setSeat(player.getSeat());
		res.setIsPlay(2);
		if(player.getHandPais().isEmpty() || player.getHandPais().size()>2){
			res.setIsBt(0);//0 1 2
		}else if( player.getHandPais().size()==2){
			res.setIsBt(2);//0 1 2
		}else if( player.getHandPais().size()==1){
			res.setIsBt(1);//0 1 2
		}
//		System.out.println("==>");
//		System.out.println(res.build().toString());
		setReplayDisCard();
		if(isOver){
			res.setIsClearDesk(0);
		}
		for (CxxdzPlayer p : seatMap.values()) {
			res.setCurScore(countBeiShuFD(p.getSeat()));
			p.writeSocket(res.build());
		}


		if (isOver) {
			state = table_state.over;
		}
		if(!isOver){
			checkLaskOut(getNowDisCardIds(),getNowDisCardSeat());
		}

	}

	/**
	 * 部分牌型只能最后一手出
	 * @param player
	 * @param type4
	 * @param chuPaiCards
	 * @return
	 */
	private boolean checkDisType(CxxdzPlayer player,String type4, List<Integer> chuPaiCards) {
		switch (type4){
			case "danz":
				if(wzbc==1){
					Integer id = chuPaiCards.get(0);
					if((id==501&&player.getHandPais().contains(502))||(id==502&&player.getHandPais().contains(501)))
						return false;
				}
				break;
			case "3z":
				if(player.getHandPais().size()!=chuPaiCards.size())
					return false;
				break;
			case "4d2":
			case "4d2dui":
				if(sdssc==1)
					return true;
				else if(player.getHandPais().size()!=chuPaiCards.size())
					return false;
				break;
			case "3d1":
			case "fjddan":
				if(sdwssc==0)
					if(chuPaiCards.contains(501)||chuPaiCards.contains(502))
						if(player.getHandPais().size()!=chuPaiCards.size())
							return false;
				break;
		}
		return true;
	}


	private void checkLaskOut(List<Integer> deskCards, int nowDisCardSeat) {
		if(null==deskCards || deskCards.isEmpty()){
			//主动出完
			List<Integer> myout = new  ArrayList<>(seatMap.get(nowDisCardSeat).getHandPais());
			String type =  DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(myout),is3dai2,sidai2dui,fjbkd);
			if(!"".equals(type)){
				if("fjddan".equals(type)){
					for(int a:myout){
						int num =CardUtils.getNumByVal(myout,CardUtils.loadCardValue(a));
						if(num>=4){
							//33334444不当飞机甩
							return;
						}
					}
				}
				if("4d2".equals(type)||"4d2dui".equals(type)){
					//4带2不主动甩
					return;
				}
				if(myout.size()>2 && myout.contains(501) && myout.contains(502)){
					//不甩王炸
					return;
				}
				addGameActionLog(seatMap.get(nowDisCardSeat),"主动 自动出最后手 牌:"+myout+" | type="+type);
				try {
					//玩家先于系统出最后手 myout =null;处理。
					if(null==myout){
						return;
					}
					playCommand(seatMap.get(nowDisCardSeat),0,myout);
				}catch (Exception e){
					return;
				}
			}
		}else{
			//被动接完
			List<Integer> cpdesk = new ArrayList<>(deskCards);
			String deskType =  DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(cpdesk),is3dai2,sidai2dui,fjbkd);
			List<Integer> myout = new  ArrayList<>(seatMap.get(nowDisCardSeat).getHandPais());
			String type =  DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(myout),is3dai2,sidai2dui,fjbkd);
			if(null==type || "".equals(type)){
				return;
			}
			if("4d2".equals(type)||"4d2dui".equals(type)){
				//4带2不主动甩
				return;
			}
			if("fjddan".equals(type)){
				for(int a:myout){
					int num =CardUtils.getNumByVal(myout,CardUtils.loadCardValue(a));
					if(num>=4){
						//33334444不当飞机甩
						return;
					}
				}
			}
			if(myout.size()>2 && myout.contains(501) && myout.contains(502)){
				//不甩王炸
				return;
			}
			boolean result =CardUtils.canChuPai(cpdesk,myout,deskType,type);
			if(result){
				addGameActionLog(seatMap.get(nowDisCardSeat),"被动 自动出最后手 result="+result+"|上家："+getNowDisCardIds()+"|type="+deskType+" | 接牌:"+myout+" | type="+type);
				seatMap.get(nowDisCardSeat).setAutoLast(1);
			}
		}
	}

	private boolean checkTiqianOver( CxxdzPlayer player) {
		int dizhu = dizhuSeat;
		if(player.getHandPais().isEmpty()){
			setLastWinSeat(player.getSeat());
			return true;
		}
		return false;
	}
	private int chupai(CxxdzPlayer player, int action, List<Integer> cards) {

		List<Integer> copy = new ArrayList<>(player.getHandPais());
		copy.removeAll(cards);

		String type = DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(cards),is3dai2,sidai2dui,fjbkd);
		if(!checkDisType(player,type,cards))
			return -1;

		if("".equals(type)){
			player.writeErrMsg("出牌不符合规则。");
			return -1;
		}else{
			if (getNowDisCardIds().size() > 0) {
				List<Integer> cpdesk =new ArrayList<Integer>(getNowDisCardIds());
				List<Integer> myout =new ArrayList<Integer>(cards);
				String deskType =  DdzSfNew.getCpType2(DdzSfNew.intCardToStringCard(cpdesk),is3dai2,sidai2dui,fjbkd);
				boolean result =CardUtils.canChuPai(cpdesk,myout,deskType,type);
				addGameActionLog(player,"result="+result+"|上家："+getNowDisCardIds()+"|type="+deskType+" | 接牌:"+myout+" | type="+type);
				if(!result){
					player.writeErrMsg("出牌不符合规则!");
					return -1;
				}
			}
		}

		if("boom".equals(type) || "wboom".equals(type)){
			setBoomBeiShu(boomBeiShu*2);
			player.changeAction(2,1);//炸弹次数
		}else if("jmBoom".equals(type)){
			int boomNun=(cards.size()/4);
			setBoomBeiShu(boomBeiShu*(int)Math.pow(2,boomNun));
			player.changeAction(2,1);//炸弹次数
		}
		if(action!=1){
			player.changeCpNum();//出牌次数
		}
		player.addOutPais(cards, this);
		cleanActionState(player.getSeat());
		int xifen=1;
		addTurnCards(cards, false);
		setNowDisCardIds(cards);
		List<Integer> scoreCards = DdzUtil.getScoreCardsList(cards);
		if(!scoreCards.isEmpty()){
			setOutfencard(scoreCards);
		}
		return xifen;
	}


	private boolean turnOver(PlayCardRes.Builder res, CxxdzPlayer winPlayer) {
		res.setIsClearDesk(1);
		int nextSeat = winPlayer.getSeat();
		setNowDisCardSeat(nextSeat);
		cleanActionState(0);
		addTurnCards(null, true);
		setNowDisCardIds(null);
		return checkTiqianOver(winPlayer);
	}

	private int getNextPlaySeat(int nextSeat) {
		for (int i = 0; i < maxPlayerCount - 1; i++) {
			nextSeat += 1;
			if (nextSeat > maxPlayerCount) {
				nextSeat = 1;
			}
			CxxdzPlayer nextPlayer = seatMap.get(nextSeat);
			if (nextPlayer.getRank() > 0) {
				continue;
			}
			break;
		}
		return nextSeat;
	}

	/**
	 *
	 * @param disSeat
	 *            不清位置
	 */
	private void cleanActionState(int disSeat) {
		for (CxxdzPlayer p : seatMap.values()) {
			if (p.getSeat() == disSeat) {
				continue;
			}
			if (disSeat == 0) {
				p.setActionS(-1);
			} else {
				if (p.getActionS() > 0) {
					p.setActionS(0);
				}
			}
		}
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
	public void playCommand(CxxdzPlayer player, int action, List<Integer> cards) {
		synchronized (this) {
			if (state != table_state.play) {
				return;
			}
			if(null==cards){
				return;
			}
			if (!containCards(player.getHandPais(), cards)) {
				return;
			}
			if (player.getHandPais().isEmpty()) {
				return;
			}

			changeDisCardRound(1);
			// 出牌了
			disCards(player, action, cards);
			setLastActionTime(TimeUtil.currentTimeMillis());

			if (isOver()) {
				calcOver();
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

	public String addCxxdzPlayLog(int seat,int action,Integer actParam,List<Integer> cardIds,boolean over) {
		JSONObject json = new JSONObject();
		json.put("seat", seat);
		json.put("action", action);
		json.put("actParam", actParam);
		json.put("vals", cardIds);
		json.put("bei", countBeiShuFD(seat));
		json.put("over", over?1:0);
		return json.toJSONString();
	}

	/**
	 * 人数未满或者人员离线
	 *c
	 * @return 0 可以打牌 1人数未满 2人员离线
	 */
	public int isCanPlay() {
		if (seatMap.size() < getMaxPlayerCount()) {
			return 1;
		}
		for (CxxdzPlayer player : seatMap.values()) {
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

	private void addGameActionLog(Player player, String str) {

		StringBuilder sb = new StringBuilder("Cxxdz");
		sb.append("|").append(getId());
		sb.append("|").append(getPlayBureau());
		sb.append("|").append(player.getUserId());
		sb.append("|").append(player.getSeat());
		sb.append("|").append(player.getName());
		sb.append("|").append("tableType="+tableType);
		sb.append("|").append(str);
		LogUtil.msgLog.info(sb.toString());
	}

	@Override
	protected void initNext1() {
		setNowDisCardIds(null);
		replayDisCard = "";
		timeNum = 0;
		newRound = true;
		finishFapai = 0;
		turnNum = 0;
		turnFirstSeat = 0;
		banker = 0;
		tRank = 0;
		showCardsUser ="";
		dplist.clear();
		setTableStatus(CxxdzConstants.TABLE_STATUS_ZERO);
		dplist = new ArrayList<>();
		bankRangPaiNumBeiShu=1;
		boomBeiShu =1;
		sel_jiabeiBeiShu=1;
		setCt(0);
		dizhuSeat=0;
		passQdz=0;
		canTiSeat.clear();
		boomBeiShu=1;
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
		banker=0;
		setNowDisCardSeat(nowDisCardSeat);
		for (CxxdzPlayer tablePlayer : seatMap.values()) {
			DealInfoRes.Builder res = DealInfoRes.newBuilder();
			res.addAllHandCardIds(tablePlayer.getHandPais());
			res.setNextSeat(nowDisCardSeat);//先叫地主的人
			res.setGameType(getWanFa());//
			res.setRemain(getTableStatus());//发牌之前 0.  打牌1
			res.setDealDice(getRandomFenZuCard());//第一局亮牌
			tablePlayer.writeSocket(res.build());
			if (tablePlayer.isAutoPlay()){
				ArrayList<Integer> val = new ArrayList<>();
				val.add(1);
				addPlayLog(addCxxdzPlayLog(tablePlayer.getSeat(), CxxdzConstants.action_tuoguan, null,val,false));
			}
		}
		addPlayLog(addCxxdzPlayLog(0, WebSocketMsgType.com_cxxdz_lookdp, null,dplist,false));
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

	public boolean createSimpleTable(Player player, int play, int bureauCount, List<Integer> params,
									 List<String> strParams, boolean saveDb) throws Exception {
		return createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, true));
		//return createTable(player, play, bureauCount, params, saveDb);

	}

	public void createTable(Player player, int play, int bureauCount, List<Integer> params) throws Exception {
//		createTable(player, play, bureauCount, params, true);
		createTable(new CreateTableInfo(player, TABLE_TYPE_NORMAL, play, bureauCount, params, strParams, true));
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

	/**
	 * 发送结算msg
	 *
	 * @param over
	 *            是否已经结束
	 *            赢的玩家
	 * @return
	 */
	public ClosingInfoRes.Builder sendAccountsMsg(boolean over, boolean isBreak, boolean dizhuWin) {
		List<ClosingPlayerInfoRes> list = new ArrayList<>();
		List<ClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();
		for (CxxdzPlayer player : seatMap.values()) {
			ClosingPlayerInfoRes.Builder build = null;
			if (over) {
				build = player.bulidTotalClosingPlayerInfoRes();
			} else {
				build = player.bulidOneClosingPlayerInfoRes();

			}
			if(dizhuWin && player.getSeat() == dizhuSeat){
				build.setIsHu(1);//win ? 1 : 0
			}else{
				if(!dizhuWin && player.getSeat() != dizhuSeat){
					build.setIsHu(1);//win ? 1 : 0
				}else{
					build.setIsHu(0);//win ? 1 : 0
				}
			}

			builderList.add(build);
			// }

			// 信用分
			if (isCreditTable()) {
				player.setWinLoseCredit(player.getPlayPoint() * creditDifen);
			}

		}

		// 信用分计算
		if (isCreditTable()) {
			// 计算信用负分
			calcNegativeCredit();
			long dyjCredit = 0;
			for (CxxdzPlayer player : seatMap.values()) {
				if (player.getWinLoseCredit() > dyjCredit) {
					dyjCredit = player.getWinLoseCredit();
				}
			}
			for (ClosingPlayerInfoRes.Builder builder : builderList) {
				CxxdzPlayer player = seatMap.get(builder.getSeat());
				calcCommissionCredit(player, dyjCredit);

				builder.addExt(player.getWinLoseCredit() + ""); // 10
				builder.addExt(player.getCommissionCredit() + ""); // 11

				// 2019-02-26更新
				builder.setWinLoseCredit(player.getWinLoseCredit());
				builder.setCommissionCredit(player.getCommissionCredit());
			}
		} else if (isGroupTableGoldRoom()) {
			// -----------亲友圈金币场---------------------------------
			for (CxxdzPlayer player : seatMap.values()) {
				player.setWinGold(player.getPlayPoint() * gtgDifen);
			}
			calcGroupTableGoldRoomWinLimit();
			for (ClosingPlayerInfoRes.Builder builder : builderList) {
				CxxdzPlayer player = seatMap.get(builder.getSeat());
				builder.addExt(player.getWinLoseCredit() + ""); // 10
				builder.addExt(player.getCommissionCredit() + ""); // 11
				builder.setWinLoseCredit(player.getWinGold());
			}
		} else {
			for (ClosingPlayerInfoRes.Builder builder : builderList) {
				CxxdzPlayer player = seatMap.get(builder.getSeat());
				builder.addExt(0 + ""); // 10
				builder.addExt(0 + ""); // 11
			}
		}
		for (ClosingPlayerInfoRes.Builder builder : builderList) {
			CxxdzPlayer player = seatMap.get(builder.getSeat());
//			builder.addExt(player.getPiaoFen() + ""); // 13
			builder.addExt( "");
			list.add(builder.build());
		}

		ClosingInfoRes.Builder res = ClosingInfoRes.newBuilder();
		res.setIsBreak(isBreak ? 1 : 0);
		res.setWanfa(getWanFa());
		res.addAllClosingPlayers(list);
		res.addAllExt(buildAccountsExt(over ? 1 : 0));
		if (over && isGroupRoom() && !isCreditTable()) {
			res.setGroupLogId((int) saveUserGroupPlaylog());
		}

		for (CxxdzPlayer player : seatMap.values()) {
			if(over){
				player.setTotalPoint(player.getPlayPoint());
			}
			player.writeSocket(res.build());
		}
		res.addAllCutCard(dplist);
		return res;
	}

	public List<String> buildAccountsExt(int over) {
		List<String> ext = new ArrayList<>();
		ext.add(id + "");// 0
		ext.add(masterId + "");// 1
		ext.add(TimeUtil.formatTime(TimeUtil.now()));// 2
		ext.add(playType + "");// 3
		// 设置当前第几局
		ext.add(playBureau + "");// 4
		ext.add(isGroupRoom() ? "1" : "0");// 5
		// 金币场大于0
		ext.add(CommonUtil.isPureNumber(modeId) ? modeId : "0");// 6
		int ratio;
		int pay;
		ratio = 1;
		pay = loadPayConfig(payType);
		ext.add(String.valueOf(ratio));// 7
		ext.add(String.valueOf(pay >= 0 ? pay : 0));// 8
		ext.add(String.valueOf(payType));// 9
		ext.add(String.valueOf(playedBureau));// 10
		ext.add(String.valueOf(matchId));// 11
		ext.add(isGroupRoom() ? loadGroupId() : "");// 12
		ext.add(creditMode + ""); // 13
		ext.add(creditJoinLimit + "");// 14
		ext.add(creditDissLimit + "");// 15
		ext.add(creditDifen + "");// 16
		ext.add(creditCommission + "");// 17
		ext.add(creditCommissionMode1 + "");// 18
		ext.add(creditCommissionMode2 + "");// 19
		ext.add((autoPlay ? 1 : 0) + "");// 20
		ext.add(over + ""); // 21
		ext.add(isCt + ""); // 22
		return ext;
	}

	@Override
	public String loadGameCode() {
		return GAME_CODE;
	}

	@Override
	public void sendAccountsMsg() {
        calcPointBeforeOver();
		ClosingInfoRes.Builder builder = sendAccountsMsg(true, true, false);
		saveLog(true, 0l, builder.build());
	}

	@Override
	public Class<? extends Player> getPlayerClass() {
		return CxxdzPlayer.class;
	}

	@Override
	public int getWanFa() {
		return GameUtil.game_type_pk_cxxdz276;
	}

	@Override
	public void checkReconnect(Player player) {
		reconnectBeforeAct((CxxdzPlayer)player);
	}

	@Override
	public void checkAutoPlay() {
		synchronized (this) {
			if (!autoPlay) {
				return;
			}
			// 发起解散，停止倒计时
			if (getSendDissTime() > 0) {
				for (CxxdzPlayer player : seatMap.values()) {
					if (player.getLastCheckTime() > 0) {
						player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
					}
				}
				return;
			}

			if (isAutoPlayOff()) {
				// 托管关闭
				for (int seat : seatMap.keySet()) {
					CxxdzPlayer player = seatMap.get(seat);
					player.setAutoPlay(false, this);
				}
				return;
			}
			// 准备托管
			if (state == table_state.ready && playedBureau > 0) {
				++timeNum;
				for (CxxdzPlayer player : seatMap.values()) {
					// 玩家进入托管后，5秒自动准备
					if (timeNum >= 5 && player.isAutoPlay()) {
						autoReady(player);
					} else if (timeNum >= 30) {
						autoReady(player);
					}
				}
				return;
			}

			CxxdzPlayer player=null;
			if(tableStatus==CxxdzConstants.TABLE_STATUS_T1J){
				checkAutoT1j();
			}else
				player = seatMap.get(nowDisCardSeat);
			if (player == null)
				return;


			if (getTableStatus() == CxxdzConstants.TABLE_STATUS_ZERO || state != table_state.play) {
				return;
			}

			//被动甩最后手
			if(player.getAutoLast()==1){
				List<Integer> ccp = new ArrayList<>(player.getHandPais());
				playCommand(player,0,ccp);
				player.setAutoLast(0);
				return;
			}

			// 托管投降检查

			int timeout;
			if (autoPlay) {
				timeout = autoTimeOut;
			} else if (player.isRobot()) {
				timeout = 3 * SharedConstants.SENCOND_IN_MINILLS;
			} else {
				return;
			}
			boolean auto = player.isAutoPlay();
			if (!auto)
				auto = checkPlayerAuto(player, timeout);

			if (auto || player.isRobot()) {
				player.setAutoPlayTime(0L);
				if (state == table_state.play) {
					if (getTableStatus() == CxxdzConstants.TABLE_STATUS_FINISH) {
						List<Integer> curList = new ArrayList<>(player.getHandPais());
						if (curList.isEmpty()) {
							return;
						}
						int size = getTurnCards().size();
						int action = 0;
						List<Integer> disList = new ArrayList<Integer>();
						if (size != 0) {
							action = 1;//要不起
						} else {
							disList.add(curList.get(0));
						}
						// 轮首次出牌
						playCommand(player, action, disList);
					}else if(getTableStatus()==CxxdzConstants.TABLE_STATUS_MZ){
						menzhua(0,player);
					}else if(getTableStatus()==CxxdzConstants.TABLE_STATUS_QDZ){
						qdz(mustQdz(player)?1:0,player);
					}else if(getTableStatus()==CxxdzConstants.TABLE_STATUS_H1J){
						h1j(0,player);
					}
				}
			}
		}
	}


	public void checkAutoT1j(){
		List<Integer> canTSeat=new ArrayList<>(canTiSeat);
		int timeout;
		for (Integer tSeat:canTSeat) {
			CxxdzPlayer player = seatMap.get(tSeat);
			if (autoPlay) {
				timeout = autoTimeOut;
			} else if (player.isRobot()) {
				timeout = 3 * SharedConstants.SENCOND_IN_MINILLS;
			} else {
				return;
			}
			boolean auto = player.isAutoPlay();
			if (!auto)
				auto = checkPlayerAuto(player, timeout);
			if(auto){
				if(player.getLookCard()==1)
					t1j(0,player);
				else
					menT(0,player);
			}
		}
	}


	public boolean checkPlayerAuto(CxxdzPlayer player, int timeout) {
		if (player.isAutoPlay()) {
			return true;
		}
		long now = TimeUtil.currentTimeMillis();
		boolean auto = false;
		if (player.isAutoPlayChecked()
				|| (player.getAutoPlayCheckedTime() >= timeout && !player.isAutoPlayCheckedTimeAdded())) {
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
		for (Map.Entry<Integer, CxxdzPlayer> entry : seatMap.entrySet()) {
			jsonObject.put("" + entry.getKey(), entry.getValue().getOutPais());
		}
		return jsonObject.toString();
	}

	@Override
	public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams,
							Object... objects) throws Exception {
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
		return "2人斗地主";
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_pk_cxxdz276);//GameUtil.play_type_pk_ddz=270

	public static void loadWanfaTables(Class<? extends BaseTable> cls) {
		for (Integer integer : wanfaList) {
			TableManager.wanfaTableTypesPut(integer, cls);
		}
	}

	@Override
	public boolean allowRobotJoin() {
		return StringUtils.contains(ResourcesConfigsUtil.loadServerPropertyValue("robot_modes", ""),
				new StringBuilder().append("|").append(modeId).append("|").toString());
	}

	public void setTableStatus(int tableStatus) {
		this.tableStatus = tableStatus;
		changeExtend();
	}

	public int getTableStatus() {
		return tableStatus;
	}

	public int getDizhuSeat() {
		return dizhuSeat;
	}

	public void setDizhuSeat(int dizhuSeat) {
		this.dizhuSeat = dizhuSeat;
		setNowDisCardSeat(dizhuSeat);
		changeExtend();
	}

	public void setPassQdz(int passQdz) {
		this.passQdz = passQdz;
		changeExtend();
	}

	@Override
	public boolean isAllReady() {
		return isAllReady1();
	}

	public boolean isAllReady1() {
		if (super.isAllReady()) {
			if (playBureau != 1) {
				return true;
			}
			for (CxxdzPlayer player : seatMap.values()) {
			}
			return true;
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

	public String getTableMsg() {
		Map<String, Object> json = new HashMap<>();
		json.put("wanFa", "牛十别");
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

	public List<Integer> getTeamPlayerSeat(int seat) {

		List<Integer> seats = new ArrayList<Integer>(seatMap.keySet());
		List<Integer> teamSeats = new ArrayList<Integer>(getTeamSeat());
		if (getTeamSeat().contains(seat)) {
			teamSeats.remove((Integer) seat);
			return teamSeats;
		} else {
			seats.remove((Integer) seat);
			seats.removeAll(teamSeats);
			return seats;
		}

	}

	public List<Integer> getTurnCards() {
		return turnCards;
	}

	public void addTurnCards(List<Integer> cards, boolean isClean) {
		if (!isClean) {
			this.turnCards.addAll(cards);
		} else {
			turnCards.clear();
		}
		dbParamMap.put("outPai9", JSON_TAG);
	}

	public int getTurnFirstSeat() {
		return turnFirstSeat;
	}

	public void setTurnFirstSeat(int turnFirstSeat) {
		this.turnFirstSeat = turnFirstSeat;
		changeExtend();
	}

	public int getTurnNum() {
		return turnNum;
	}

	public List<Integer> getTeamSeat() {
		return teamSeat;
	}

	public List<Integer> getActionSeats() {
		return actionSeats;
	}

	public void setActionSeats(List<Integer> actionSeats) {
		this.actionSeats = actionSeats;
		dbParamMap.put("handPai10", JSON_TAG);
	}

	public void removeActionSeat(Integer seat) {
		actionSeats.remove(seat);
		dbParamMap.put("handPai10", JSON_TAG);
	}


	public void setTeamSeat(List<Integer> teamSeat) {
		this.teamSeat = teamSeat;
		dbParamMap.put("handPai9", JSON_TAG);
	}

	public void setTurnNum(int turnNum) {
		this.turnNum = turnNum;
	}

	public void addTurnNum(int turnNum) {
		this.turnNum += turnNum;
		changeExtend();
	}

	public void rankIncre() {
		tRank += 1;
		changeExtend();
	}

	public void playChangeSeat() {

	}
	public void setPlayerMap( Map<Long, CxxdzPlayer>  p) {
		this.playerMap = p;
	}

	public void setSeatMap(Map<Integer, CxxdzPlayer> map) {
		this.seatMap = map;
	}

	public int getRandomFenZuCard() {
		return randomFenZuCard;

	}

	public void setRandomFenZuCard(int randomFenZuCard) {
		this.randomFenZuCard = randomFenZuCard;
		changeExtend();
	}

	public int getDuiwu() {
		return duiwu;
	}

	public void setDuiwu(int duiwu) {
		this.duiwu = duiwu;
		changeExtend();
	}

	public String getShowCardsUser() {
		return showCardsUser;
	}

	public void setShowCardsUser(String showCardsUser) {
		this.showCardsUser += showCardsUser+",";
		dbParamMap.put("handPai8" , this.showCardsUser);
	}

	public void setBanker(int banker) {
		this.banker = banker;//庄
		dbParamMap.put("handPai7" , banker);
	}

	public List<Integer> getOutfencard() {
		return outfencard;
	}

	public void setOutfencard(List<Integer> outfencard) {
		this.outfencard.addAll(outfencard);
		StringBuffer sb = new StringBuffer();
		for (int card : this.outfencard){
			sb.append(card).append(",");
		}
		dbParamMap.put("handPai6" , sb.toString());
	}


	public List<Integer> getDplist() {
		return dplist;
	}

	public void setDplist(List<Integer> dplist) {
		this.dplist = dplist;
		changeExtend();
	}

	public int getBankRangPaiNumBeiShu() {
		return bankRangPaiNumBeiShu;
	}
	public void setBankRangPaiNumBeiShu(int bankRangPaiNumBeiShu) {
		this.bankRangPaiNumBeiShu = bankRangPaiNumBeiShu;
		changeExtend();
	}

	public int getBoomBeiShu() {
		return boomBeiShu;
	}

	public void setBoomBeiShu(int boomBeiShu) {
		this.boomBeiShu = boomBeiShu;
		changeExtend();
	}

	public int getSel_jiabeiBeiShu() {
		return sel_jiabeiBeiShu;
	}

	public void setSel_jiabeiBeiShu(int sel_jiabeiBeiShu) {
		this.sel_jiabeiBeiShu = sel_jiabeiBeiShu;
		changeExtend();
	}

	public int isCt() {
		return isCt;
	}

	public void setCt(int ct) {
		isCt = ct;
	}

	public boolean isJiaofenMoshi(){
		return op_gametype == 2;
	}

	public int getMenzhua() {
		return menzhua;
	}
}
