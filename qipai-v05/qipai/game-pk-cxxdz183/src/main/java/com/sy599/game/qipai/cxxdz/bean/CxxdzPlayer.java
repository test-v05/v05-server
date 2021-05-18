package com.sy599.game.qipai.cxxdz.bean;

import com.google.protobuf.GeneratedMessage;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDatasDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableRes.ClosingPlayerInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.cxxdz.command.CxxdzCommandProcessor;
import com.sy599.game.qipai.cxxdz.constant.CxxdzConstants;
import com.sy599.game.qipai.cxxdz.util.DdzSfNew;
import com.sy599.game.qipai.cxxdz.util.DdzUtil;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class CxxdzPlayer extends Player {
	// 座位id
	private volatile int seat;
	// 状态
	private volatile player_state state;// 1进入 2已准备 3正在玩 4已结束
	private int isEntryTable;
	private List<Integer> handPais;
	private List<List<Integer>> outPais;
	private int winCount;
	private int lostCount;
	private int point;
	private int playPoint;
	private int cutCard;// 是否需要切牌
	private int[] infoArr = new int[4];
	private volatile boolean autoPlay = false;//托管
	private volatile long lastOperateTime = 0;//最后操作时间
	private volatile long lastCheckTime = 0;//最后检查时间
	private volatile long nextAutoDisCardTime = 0;
	private volatile long autoPlayTime = 0;//自动操作时间
	private int currentLs;//当前连胜
	private int maxLs;//最大连胜
	private volatile int autoLast=0;//自动最后手
	//斗地主参数
	private int jiaodizhu=-1;
	private int qiangdizhu=-1;
	private int selJaiBei =-1;
	private int cpnum =0;//出牌次数
	//游戏分
	private int gameFen=0;


	/**1不要，2要不起*/
	private int actionS=-1;
	/***/
	private int rank;//名次
//	全局总拿分
	private int zongfen=0;
	//是否明牌0=否 1=是
	private int isShowCards = 0;

	private List<Integer> chiFenCards = new ArrayList<>();


	//闷抓 地主 踢一脚 回一脚
	private int mengzhua=0;
	private int dizhu=0;
	private int t1j=0;
	private int h1j=0;
	private int lookCard=0;
	private int lookdp=0;
	private int passDz=0;
	private int beishu=1;
	private int menT=0;

	@Override
	public void initPlayInfo(String data) {
		if (!StringUtils.isBlank(data)) {
			int i = 0;
			String[] values = data.split(",");
			long duserId = StringUtil.getLongValue(values, i++);
			if (duserId != getUserId()) {
				return;
			}
			this.seat = StringUtil.getIntValue(values, i++);
			int stateVal = StringUtil.getIntValue(values, i++);
			this.state = SharedConstants.getPlayerState(stateVal);
			this.isEntryTable = StringUtil.getIntValue(values, i++);
			setTotalBoom(StringUtil.getIntValue(values, i++));
			this.winCount = StringUtil.getIntValue(values, i++);
			this.lostCount = StringUtil.getIntValue(values, i++);
			this.point = StringUtil.getIntValue(values, i++);
			setTotalPoint(StringUtil.getIntValue(values, i++));
			this.playPoint = StringUtil.getIntValue(values, i++);
			if (playPoint == 0 && this.point != 0) {
				playPoint = point;
			}
			zongfen =StringUtil.getIntValue(values, i++);
			this.actionS =  StringUtil.getIntValue(values, i++);
			gameFen=  StringUtil.getIntValue(values, i++);
			rank=  StringUtil.getIntValue(values, i++);
			isShowCards=  StringUtil.getIntValue(values, i++);
			selJaiBei=  StringUtil.getIntValue(values, i++);
			qiangdizhu=  StringUtil.getIntValue(values, i++);
			jiaodizhu=  StringUtil.getIntValue(values, i++);
			cpnum= StringUtil.getIntValue(values, i++);
			autoLast = StringUtil.getIntValue(values, i++);
			mengzhua = StringUtil.getIntValue(values, i++);
			dizhu = StringUtil.getIntValue(values, i++);
			t1j = StringUtil.getIntValue(values, i++);
			h1j = StringUtil.getIntValue(values, i++);
			lookCard = StringUtil.getIntValue(values, i++);
			beishu = StringUtil.getIntValue(values, i++);
			lookdp = StringUtil.getIntValue(values, i++);
			menT = StringUtil.getIntValue(values, i++);
		}
	}

	public String toInfoStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(getUserId()).append(",");
		sb.append(seat).append(",");
		int stateVal = 0;
		if (state != null) {
			stateVal = state.getId();
		}
		sb.append(stateVal).append(",");
		sb.append(isEntryTable).append(",");
		sb.append(getTotalBoom()).append(",");
		sb.append(winCount).append(",");
		sb.append(lostCount).append(",");
		sb.append(point).append(",");
		sb.append(loadScore()).append(",");
		sb.append(playPoint).append(",");
		sb.append(zongfen).append(",");
		sb.append(actionS).append(",");
		sb.append(gameFen).append(",");
		sb.append(rank).append(",");
		sb.append(isShowCards).append(",");
		sb.append(selJaiBei).append(",");
		sb.append(qiangdizhu).append(",");
		sb.append(jiaodizhu).append(",");
		sb.append(cpnum).append(",");
		sb.append(autoLast).append(",");
		sb.append(mengzhua).append(",");
		sb.append(dizhu).append(",");
		sb.append(t1j).append(",");
		sb.append(h1j).append(",");
		sb.append(lookCard).append(",");
		sb.append(beishu).append(",");
		sb.append(lookdp).append(",");
		sb.append(menT).append(",");
		return sb.toString();
	}

	@Override
	protected void loadFromDB0(RegInfo info) {
		if (UserDatasDao.getInstance().exists()){
			String val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId), CxxdzTable.GAME_CODE,"all","currentLs");
			if (CommonUtil.isPureNumber(val)){
				currentLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId), CxxdzTable.GAME_CODE,"all","currentLs","0");
			}

			val = UserDatasDao.getInstance().selectUserDataValue(String.valueOf(userId), CxxdzTable.GAME_CODE,"all","maxLs");
			if (CommonUtil.isPureNumber(val)){
				maxLs = Integer.parseInt(val);
			}else{
				UserDatasDao.getInstance().saveUserDatas(String.valueOf(userId), CxxdzTable.GAME_CODE,"all","maxLs","0");
			}
		}
	}

	public int getCurrentLs() {
		return currentLs;
	}

	public void setCurrentLs(int currentLs) {
		this.currentLs = currentLs;
	}

	public int getMaxLs() {
		return maxLs;
	}

	public void setMaxLs(int maxLs) {
		this.maxLs = maxLs;
	}

	public long getAutoPlayTime() {
		return autoPlayTime;
	}

	public void setAutoPlayTime(long autoPlayTime) {
		this.autoPlayTime = autoPlayTime;
	}

	public long getLastCheckTime() {
		return lastCheckTime;
	}

	public void setLastCheckTime(long lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}

	public boolean isAutoPlay() {
		return autoPlay;
	}

	public void setAutoPlay(boolean autoPlay,BaseTable table) {
		if (this.autoPlay != autoPlay && !isRobot()){
			ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(132, seat,autoPlay?1:0, (int)userId);
			GeneratedMessage msg = res.build();
			for (Map.Entry<Long,Player> kv:table.getPlayerMap().entrySet()){
				Player player=kv.getValue();
				if (player.getIsOnline() == 0) {
					continue;
				}
				player.writeSocket(msg);
			}
			CxxdzTable table2 = getPlayingTable(CxxdzTable.class);
			ArrayList<Integer> val = new ArrayList<>();
			if(autoPlay)
				val.add(1);
			else
				val.add(0);
        	table2.addPlayLog(table2.addCxxdzPlayLog(getSeat(), CxxdzConstants.action_tuoguan, null,val,false));
		}
		boolean addLog = this.autoPlay != autoPlay;
		this.autoPlay = autoPlay;
		if(!autoPlay){
			setAutoPlayCheckedTimeAdded(false);
		}

		if(addLog) {
			StringBuilder sb = new StringBuilder("Ddz");
			if (table != null) {
				sb.append("|").append(table.getId());
				sb.append("|").append(table.getPlayBureau());
			} else {
				sb.append("|").append(-1);
				sb.append("|").append(-1);
			}
			sb.append("|").append(this.getUserId());
			sb.append("|").append(this.getSeat());
			sb.append("|").append(this.isAutoPlay() ? 1 : 0);
			sb.append("|").append("setAutoPlay");
			sb.append("|").append(autoPlay);
			LogUtil.msgLog.info(sb.toString());
		}
	}

	public long getLastOperateTime() {
		return lastOperateTime;
	}

	public void setLastOperateTime(long lastOperateTime) {
		this.lastCheckTime = 0;
		this.lastOperateTime = lastOperateTime;
		this.autoPlayTime = 0;
	}


	public CxxdzPlayer() {
		handPais = new ArrayList<Integer>();
		outPais = new ArrayList<List<Integer>>();
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public void initPais(String hand, String out) {
		if (!StringUtils.isBlank(hand)) {
			this.handPais = StringUtil.explodeToIntList(hand);

		}
		if (!StringUtils.isBlank(out)) {
			this.outPais = StringUtil.explodeToLists(out);

		}
	}

	public void initPais(List<Integer> hand, List<List<Integer>> out) {
		if (hand != null) {
			this.handPais = hand;

		}
		if (out != null) {
			this.outPais = out;

		}
	}

	public void dealHandPais(List<Integer> pais, CxxdzTable table) {
		this.table = table;
		this.handPais = pais;

		Collections.sort(pais, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return DdzUtil.loadCardValue(o2) - DdzUtil.loadCardValue(o1);
			}
		});

		table.changeCards(seat);
	}

	public List<Integer> getHandPais() {
		return handPais;
	}
	
	public boolean addHandPais(List<Integer> list) {
		 handPais.addAll(list);
		 return true;
	}
	
	public boolean removeHandPais(List<Integer> list) {
		 handPais.removeAll(list);
		 return true;
	}
	

	public List<List<Integer>> getOutPais() {
		return outPais;
	}

	/**
	 * 是否出过牌用于结算
	 *
	 * @return
	 */
	public boolean isOutCards() {
		boolean isOut = false;
		for (List<Integer> list : outPais) {
			if (!list.isEmpty() && !list.contains(0)) {
				isOut = true;
			}

		}
		return isOut;
	}

	public void addOutPais(List<Integer> cards , CxxdzTable table) {
		this.table = table;
		//handPais.removeAll(cards);
		
		for(Integer id: cards){
			handPais.remove(id);
		}
		outPais.add(cards);
		table.changeCards(seat);
	}
	
	public void initExtend(String info) {
		  if (StringUtils.isBlank(info)) {
	            return;
	        }
	        int i = 0;
	        String[] values = info.split("\\|");
	        String val4 = StringUtil.getValue(values, i++);
	        if (!StringUtils.isBlank(val4)) {
	        	infoArr = StringUtil.explodeToIntArray(val4);
	        }
	}

	public String toExtendStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(StringUtil.implode(infoArr)).append("|");
		return sb.toString();

	}




	public player_state getState() {
		return state;
	}

	public void changeState(player_state state) {
		this.state = state;
		changeTableInfo();
	}

	public int getIsEntryTable() {
		return isEntryTable;
	}

	public void setIsEntryTable(int isEntryTable) {
		this.isEntryTable = isEntryTable;
		changeTableInfo();
	}

	public PlayerInTableRes.Builder buildPlayInTableInfo() {
		return buildPlayInTableInfo(0, false);
	}

	/**
	 * @param isrecover
	 *            是否重连
	 * @return
	 */
	public PlayerInTableRes.Builder buildPlayInTableInfo(long recUserId, boolean isrecover) {
		PlayerInTableRes.Builder res = PlayerInTableRes.newBuilder();
		res.setUserId(this.userId + "");
		if (!StringUtils.isBlank(ip)) {
			res.setIp(ip);
		} else {
			res.setIp("");
		}
		res.setName(name);
		res.setSeat(seat);
		res.setSex(sex);
		res.setShiZhongCard(isShowCards);//是否明牌 1是  0 否
		res.setPoint(getPlayPoint());//积分。
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		CxxdzTable table = getPlayingTable(CxxdzTable.class);
		if (table == null) {
			LogUtil.e("userId="+this.userId+"table null-->" + getPlayingTableId());
			return null;
		}
		
		List<Integer> outCards = getCurOutCard();
		if(outCards!=null) {
			if(!table.getTurnCards().isEmpty()){
				List<Integer> cards2 =new ArrayList<>(table.getNowDisCardIds());
				cards2.removeAll(outCards);
				if(cards2.isEmpty()){
					res.addAllOutCardIds(outCards);
				}
			}
			
		}
		
		
		if(!table.getTurnCards().isEmpty()&&actionS!=-1){
			if(table.getNowDisCardIds().size()==1&&!handPais.isEmpty()){
				for(Integer id: handPais){
					if(id==501||id==502){
						res.addOutedIds(id);
					}
				}
			}
		}
		res.setStatus(state.getId()-1);
		//res.setShiZhongCard(table.getTurnFirstSeat()==seat?1:0);

		if (isrecover) {
		}
		List<Integer> recover = new ArrayList<>();
		recover.add(isEntryTable);
		res.addAllRecover(recover);
		List<Integer> extList = new ArrayList<>();
		if(table.getMasterId() == userId){
			extList.add(1);//0

		}else{
			extList.add(0);
		}
		int totalScore = DdzUtil.getCpfen(DdzSfNew.intCardToStringCard(chiFenCards));
		res.addAllScoreCard(chiFenCards);
		//下标1
		if(table.isJiaofenMoshi()){
			if(table.getTableStatus()==5){//牌桌状态5= 加倍
				if(getSelJaiBei()==2){
					extList.add(4);
				}else if( getSelJaiBei()==1){
					extList.add(5);
				}else{
					extList.add(-1);
				}
			}else{
				extList.add(getJiaodizhu());//1  叫地主
			}
		}
		else{
			if(table.getTableStatus()==3){//牌桌状态3=叫地主
				extList.add(getJiaodizhu());//1  叫地主
			}
			else if(table.getTableStatus()==4){//牌桌状态4=抢地主
			    extList.add(getQiangdizhu());
			}
			else if(table.getTableStatus()==5){//牌桌状态5= 加倍
				if(getSelJaiBei()==2){
					extList.add(4);
				}else if( getSelJaiBei()==1){
					extList.add(5);
				}else{
					extList.add(-1);
				}
			}else{
				extList.add(-1);
			}
		}
		if(table.getTableStatus()>=CxxdzConstants.TABLE_STATUS_QDZ)
			extList.add(table.countBeiShuFD(seat));//2
		else
			extList.add(1);//2
		extList.add(isAutoPlay() && !isRobot() ? 1 : 0);//3
		extList.add(getGameFen());//4
		extList.add(actionS<0?0:actionS);// 5

		if(table.getMenzhua() ==1 && table.getTableStatus()>=15){
			CxxdzPlayer dizhu = (CxxdzPlayer) table.getSeatMap().get(table.getDizhuSeat());
			if(table.getDizhuSeat()!=getSeat() && dizhu.getMengzhua()==1){
				extList.add(0);//6
			}else{
				extList.add(1);//6
			}
		}else{
			extList.add(lookdp);//6
		}
		extList.add(rank);// 7排名
		extList.add(handPais.size());// 8
		extList.add(autoPlay ? 1 : 0);//9 是否托管状态
		extList.add(mengzhua);//10 是否闷抓
		extList.add(dizhu);//11 是否地主
		extList.add(t1j);//12 是否踢一脚
		extList.add(h1j);//13 是否回一脚
		extList.add(lookCard);//14 看牌
		extList.add(menT);//15 	闷T
		if(getHandPais().isEmpty()|| getHandPais().size()>2){
			extList.add(0);
		}else if( getHandPais().size()==1){
			extList.add(1);
		}else if( getHandPais().size()==2){
			extList.add(2);
		}
		//
		res.addAllExt(extList);

		//信用分
		if(table.isCreditTable()) {
			GroupUser gu = getGroupUser();
			String groupId = table.loadGroupId();
			if (gu == null || !groupId.equals(gu.getGroupId() + "")) {
				gu = GroupDao.getInstance().loadGroupUser(getUserId(), groupId);
			}
			res.setCredit(gu != null ? gu.getCredit() : 0);
		}
		return buildPlayInTableInfo1(res);
	}
	
	/**
	 * 获取当前轮的牌
	 * @param
	 * @return
	 */
	public List<Integer> getCurOutCard(){
		if(outPais==null||outPais.isEmpty()){
			return null;
		}
		return outPais.get(outPais.size()-1);
	}


	public int getWinCount() {
		return winCount;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	public int getLostCount() {
		return lostCount;
	}

	public void setLostCount(int lostCount) {
		this.lostCount = lostCount;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public int getPlayPoint() {
		return playPoint;
	}

	public void setPlayPoint(int playPoint) {
		this.playPoint = playPoint;
	}

	public void changePlayPoint(int playPoint) {
		this.playPoint += playPoint;
		changeTableInfo();
	}


    public void changeAction(int index, int val) {
        infoArr[index] += val;
//        if(index==0){
//        	addXifen(val);
//        }
        getPlayingTable().changeExtend();
    }
	public int getActionValue(int index){
		return infoArr[index];
	}
	public int[] getInfoArr(){
		return infoArr;
	}

	public void changePoint(int point,BaseTable table) {
		this.point += point;
		myExtend.changePoint(table.getPlayType(), point);
		myExtend.setPdkFengshen(FirstmythConstants.firstmyth_index0, point);
		changeTotalPoint(point);
		if (point > getMaxPoint()) {
			setMaxPoint(point);
		}
		changeTableInfo();
	}

	public int getMengzhua() {
		return mengzhua;
	}

	public int getDizhu() {
		return dizhu;
	}

	public int getT1j() {
		return t1j;
	}

	public int getMenT() {
		return menT;
	}

	public int getH1j() {
		return h1j;
	}

	public int getBeishu() {
		return beishu;
	}

	public void setBeishu(int beishu) {
		this.beishu = beishu;
	}

	public void setMengzhua(int mengzhua) {
		beishu*=2;
		this.mengzhua = mengzhua;
		changeTableInfo();
	}

	public void setDizhu(int dizhu) {
		this.dizhu = dizhu;
		changeTableInfo();
	}

	public void setT1j(int t1j) {
		this.t1j = t1j;
		beishu*=2;
		changeTableInfo();
	}
	public void setMenT(int menT) {
		this.menT = menT;
		beishu*=2;
		changeTableInfo();
	}


	public void setH1j(int h1j,CxxdzPlayer player) {
		this.h1j = h1j;
		player.setBeishu(player.getBeishu()*2);
		changeTableInfo();
	}

	public int getLookCard() {
		return lookCard;
	}

	public void setLookCard(int lookCard) {
		this.lookCard = lookCard;
		changeTableInfo();
	}

	public void lookdp(){
		setLookdp(1);
		writeComMessage(WebSocketMsgType.com_cxxdz_lookdp);
	}

	public void lookCard(){
		if(lookCard==0){
			setLookCard(1);
			writeComMessage(WebSocketMsgType.com_cxxdz_look);
		}
	}

	public void setLookdp(int lookdp) {
		this.lookdp = lookdp;
		changeTableInfo();
	}

	public int getPassDz() {
		return passDz;
	}

	public void setPassDz(int passDz) {
		this.passDz = passDz;
		changeTableInfo();
	}

	public void changeCutCard(int cutCard) {
		this.cutCard = cutCard;
		changeTableInfo();
	}

	public void clearTableInfo(BaseTable table,boolean save){
		boolean isCompetition = false;
		if (table != null && table.isCompetition()) {
			isCompetition = true;
			endCompetition();
		}
		setSeat(0);
		if (!isCompetition) {
			setPlayingTableId(0);
		}
		setIsEntryTable(0);
		changeIsLeave(0);
		getHandPais().clear();
		getOutPais().clear();
		setMaxPoint(0);
		setPlayPoint(0);
		changeState(null);
		setTotalBoom(0);
		setWinCount(0);
		setLostCount(0);
		setPoint(0);
		setTotalPoint(0);
		setCutCard(0);
		setRank(0);
		setZongfen(0);
		isShowCards=0;
		gameFen=0;
		rank=0;
		chiFenCards= new ArrayList<>();
		actionS=-1;
		infoArr = new int[4];
		jiaodizhu=-1;
		qiangdizhu=-1;
		selJaiBei=-1;
		cpnum=0;
		clearStart();
		setLastCheckTime(0);
		if(table.isAutoPlay() && this.autoPlay) {
			this.autoPlay = false;
			setAutoPlayCheckedTimeAdded(false);
		}
		setWinGold(0);
 		setAutoLast(0);
		if (save) {
			saveBaseInfo();
		}
	}

	public void clearTableInfo() {
		clearTableInfo(getPlayingTable(),true);
	}

	/**
	 * 单局详情
	 *
	 * @return
	 */
	public ClosingPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
		ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.addAllCards(handPais);
		res.setTotalPoint(getPlayPoint());
		res.setSeat(seat);
		CxxdzTable table = getPlayingTable(CxxdzTable.class);
		res.setBoom(table.getDizhuSeat()==seat?1:0);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}

		res.setSex(sex);
        res.addExt(table.isCt()+"");//0
		res.addExt(table.getBoomBeiShu()+"");// 1 炸弹倍数
		res.addExt(table.countBeiShuFD(seat)+""); // 2 总倍数
        res.addExt(mengzhua+""); // 3 总倍数
        res.addExt(t1j+""); // 4 总倍数
        res.addExt(h1j+""); // 5 总倍数
        res.addExt(menT+""); // 6 总倍数
		return res;
	}

	/**
	 * 总局详情
	 *
	 * @return
	 */
	public ClosingPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes() {
		ClosingPlayerInfoRes.Builder res = ClosingPlayerInfoRes.newBuilder();
		res.setUserId(userId + "");
		res.setName(name);
		res.setPoint(point);
		res.setLeftCardNum(handPais.size());
		res.setMaxPoint(getMaxPoint());
		res.setTotalBoom(getTotalBoom());
		res.setWinCount(getWinCount());
		res.setLostCount(getLostCount());
		res.setTotalPoint(getPlayPoint());
		res.addAllActionCounts(	Arrays.asList(ArrayUtils.toObject(infoArr)));
		res.addAllCards(handPais);
		res.setSeat(seat);
		if (!StringUtils.isBlank(getHeadimgurl())) {
			res.setIcon(getHeadimgurl());
		} else {
			res.setIcon("");
		}
		CxxdzTable table = getPlayingTable(CxxdzTable.class);
		res.setBoom(table.getDizhuSeat()==seat?1:0);
		res.setSex(sex);
		res.addExt(table.isCt()+"");//0
		res.addExt(table.getBoomBeiShu()+"");// 1 炸弹倍数
		res.addExt(table.countBeiShuFD(seat)+""); // 2 总倍数
        res.addExt(mengzhua+""); // 3 闷抓
        res.addExt(t1j+""); // 4 踢一脚
        res.addExt(h1j+""); // 5 回一脚
        res.addExt(menT+""); // 6 闷踢
		return res;
	}


	public void changeTableInfo() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changePlayers();
	}

	@Override
	public void initNext() {
		getHandPais().clear();
		getOutPais().clear();
		setPoint(0);
		changeState(player_state.entry);
		changeSeat();
		gameFen=0;
		actionS = 0;
		rank = 0;
		isShowCards=0;
		chiFenCards= new ArrayList<>();
		jiaodizhu=-1;
		qiangdizhu=-1;
		selJaiBei=-1;
		cpnum=0;
		setAutoLast(0);
		clearStart();
	}

	public void clearStart(){
		mengzhua=0;
		dizhu=0;
		t1j=0;
		h1j=0;
		lookCard=0;
		lookdp=0;
		passDz=0;
		beishu=1;
		menT=0;
		changeTableInfo();
	}

	public void changeSeat() {
		BaseTable table = getPlayingTable();
		if (table != null)
			table.changeCards(seat);
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
		changeTableInfo();
	}


	public int getGameFen() {
		return gameFen;
	}

	public void setGameFen(int gameFen) {
		this.gameFen = gameFen;
	}

	public void addGameFen(int fen) {
		CxxdzTable table2 = getPlayingTable(CxxdzTable.class);
//		if(table2==null ||table2.getBanker()!=0){
//			return;
//		}
		if(table2==null){
			return;
		}
		this.gameFen += fen;
		changeTableInfo();
	}

	
	public int getActionS() {
		return actionS;
	}

	public void setActionS(int actionS) {
		this.actionS = actionS;
		changeTableInfo();
	}

	@Override
	public void endCompetition1() {

	}

	public List<Integer> getChiFenCards() {
		return chiFenCards;
	}

	public void addChiFenCards(List<Integer> cards) {
		this.chiFenCards.addAll(cards);
		changeTableInfo();
	}

	public int getCutCard() {
		return cutCard;
	}

	public void setCutCard(int cutCard) {
		this.cutCard = cutCard;
	}

	public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_pk_cxxdz276);

	public static void loadWanfaPlayers(Class<? extends Player> cls){
		for (Integer integer:wanfaList){
			PlayerManager.wanfaPlayerTypesPut(integer,cls, CxxdzCommandProcessor.getInstance());
		}
	}



	public int getZongfen() {
		return zongfen;
	}
	public void setZongfen(int zongfen) {
		this.zongfen = zongfen;
		changeTableInfo();
	}

	public int isShowCards() {
		return isShowCards;
	}

	public void setShowCards(int showCards) {
		isShowCards = showCards;
		changeTableInfo();
	}

	public int getJiaodizhu() {
		return jiaodizhu;

	}

	public void setJiaodizhu(int jiaodizhu) {
		this.jiaodizhu = jiaodizhu;
		changeTableInfo();
	}

	public int getQiangdizhu() {
		return qiangdizhu;
	}

	public void setQiangdizhu(int qiangdizhu) {
		this.qiangdizhu = qiangdizhu;changeTableInfo();
	}

	public int getSelJaiBei() {
		return selJaiBei;
	}

	public void setSelJaiBei(int selJaiBei) {
		this.selJaiBei = selJaiBei;changeTableInfo();
	}
	public void changeCpNum() {
		 this.cpnum++;
	}
	public int getCpnum() {
		return cpnum;
	}

	public int getAutoLast() {
		return autoLast;
	}

	public void setAutoLast(int autoLast) {
		this.autoLast = autoLast;
		changeExtend();
	}
}
