package com.sy599.game.qipai.nanxmj.bean;

import java.util.*;

import com.sy599.game.qipai.nanxmj.tool.*;
import com.sy599.game.qipai.nanxmj.tool.huTool.HuTool;
import com.sy599.game.qipai.nanxmj.tool.huTool.TingTool;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.TableMjResMsg.*;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TableRes.PlayerInTableRes;
import com.sy599.game.qipai.nanxmj.command.MjCommandProcessor;
import com.sy599.game.qipai.nanxmj.constant.NxMjConstants;
import com.sy599.game.qipai.nanxmj.constant.NxMj;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class NanxMjPlayer extends Player {
    // 座位id
    private int seat;
    // 状态
    private player_state state;// 1进入 2已准备 3正在玩 4已结束
    /**
     * 牌局是否在线 1离线 2在线
     */
    private int isEntryTable;
    private List<NxMj> handPais;
    private List<Integer> outPais;
    private List<NxMj> peng;
    private List<NxMj> aGang;
    private List<NxMj> mGang;
    private int winCount;
    private int lostCount;
    /**
     * 局分
     */
    private int lostPoint;
    /**
     * 大局分
     */
    private int point;
    private int[] actionArr = new int[5];
    private int[] actionTotalArr = new int[5];
    private List<Integer> passMajiangVal;
    //过杠操作
    private List<Integer> passGangValList;
    /**
     * 出牌对应操作信息
     */
    private List<MjCardDisType> cardTypes;
    //胡牌类型
    private List<Integer> huType;

    private volatile boolean autoPlay = false;//托管
    private volatile boolean autoPlaySelf = false;//托管
    private volatile long lastCheckTime = 0;//最后检查时间
    private volatile long autoPlayTime = 0;//自动操作时间
    private volatile boolean checkAutoPlay = false; //是否是牌桌上的焦点
    private volatile long sendAutoTime = 0;//发送倒计时间
    private int huNum=0;//自摸次数
    private int jiePaoNum=0;//接炮次数
    private int dianPaoNum=0;//点炮次数
    private int moNum=0;//摸排数
    private int gangNum=0;
    private int dianGangNum=0;
    //0胡牌分 1 明杠 2暗杠 3接杠
    private int pointArr[]=new int[4];

    private int piaoFen=-1;
    //输赢飘分
    private int winLostPiaoFen=0;
    //过胡之后，直到自己的回合为止都不能胡。
    private int passHu=0;
    //过碰之后，直到自己的回合为止都不能碰该牌。
    private int passPengVal=0;
    //可碰
    private boolean canPeng=true;
    //可杠牌
    private List<Integer> canGang=new ArrayList<>();
    //报听
    private int baoTing=0;
    public NanxMjPlayer() {
        handPais = new ArrayList<NxMj>();
        outPais = new ArrayList<Integer>();
        peng = new ArrayList<>();
        aGang = new ArrayList<>();
        mGang = new ArrayList<>();
        passGangValList = new ArrayList<>();
        passMajiangVal = new ArrayList<>();
        cardTypes = new ArrayList<>();
        huType = new ArrayList<>();
        autoPlaySelf = false;
        autoPlay = false;
        autoPlayTime = 0;
        checkAutoPlay = false;
        lastCheckTime = System.currentTimeMillis();
    }

    public String toInfoStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(getUserId()).append(",");
        sb.append(seat).append(",");
        int stateVal = 0;
        if (state != null) {
            stateVal = state.getId();
        }
        sb.append(stateVal).append(",");
        sb.append(isEntryTable).append(",");
        sb.append(winCount).append(",");
        sb.append(lostCount).append(",");
        sb.append(point).append(",");
        sb.append(getTotalPoint()).append(",");
        sb.append(lostPoint).append(",");
        sb.append(autoPlay ? 1 : 0).append(",");
        sb.append(autoPlaySelf ? 1 : 0).append(",");
        sb.append(autoPlayTime).append(",");
        sb.append(lastCheckTime).append(",");
        sb.append(piaoFen).append(",");
        sb.append(huNum).append(",");
        sb.append(jiePaoNum).append(",");
        sb.append(dianPaoNum).append(",");
        sb.append(canPeng?1:0).append(",");
        sb.append(StringUtil.implode(canGang, "_")).append(",");
        sb.append(gangNum).append(",");
        sb.append(dianGangNum).append(",");
        sb.append(baoTing).append(",");
        return sb.toString();
    }

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
            this.winCount = StringUtil.getIntValue(values, i++);
            this.lostCount = StringUtil.getIntValue(values, i++);
            this.point = StringUtil.getIntValue(values, i++);
            setTotalPoint(StringUtil.getIntValue(values, i++));
            this.lostPoint = StringUtil.getIntValue(values, i++);
            this.autoPlay = StringUtil.getIntValue(values, i++) == 1;
            this.autoPlaySelf = StringUtil.getIntValue(values, i++) == 1;
            this.autoPlayTime = StringUtil.getLongValue(values, i++);
            this.lastCheckTime = StringUtil.getLongValue(values, i++);
            this.piaoFen = StringUtil.getIntValue(values, i++);
            this.huNum = StringUtil.getIntValue(values, i++);
            this.jiePaoNum = StringUtil.getIntValue(values, i++);
            this.dianPaoNum = StringUtil.getIntValue(values, i++);
            this.canPeng = StringUtil.getIntValue(values, i++)==1?true:false;
            String virtualGangStr = StringUtil.getValue(values, i++);
            if (!StringUtils.isBlank(virtualGangStr)) {
                this.canGang = StringUtil.explodeToIntList(virtualGangStr, "_");
            }
            this.gangNum = StringUtil.getIntValue(values, i++);
            this.dianGangNum = StringUtil.getIntValue(values, i++);
            this.baoTing = StringUtil.getIntValue(values, i++);
        }
    }

    public List<Integer> getHuType() {
        return huType;
    }

    public void setHuType(List<Integer> huType) {
        this.huType = huType;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public void setPassHu(int passHu) {
        this.passHu = passHu;
    }

    public int getPassHu() {
        return passHu;
    }

    public void setPassPengVal(int passPengVal) {
        this.passPengVal = passPengVal;
    }

    public boolean isCanPeng() {
        return canPeng;
    }

    public void setCanPeng(boolean canPeng) {
        this.canPeng = canPeng;
    }

    public int[] getPointArr() {
        return pointArr;
    }

    public void changePointArr(int index, int num) {
        pointArr[index]+=num;
    }


    public int calcGangFen(){
        int point=0;
        for (int i = 0; i < pointArr.length; i++) {
            point+=pointArr[i];
        }
        return point;
    }

    public int getWinLostPiaoFen() {
        return winLostPiaoFen;
    }

    public void setWinLostPiaoFen(int winLostPiaoFen) {
        this.winLostPiaoFen = winLostPiaoFen;
    }

    public void dealHandPais(List<NxMj> pais) {
        this.handPais = pais;
        getPlayingTable().changeCards(seat);
    }

    public List<NxMj> getHandMajiang() {
        return handPais;
    }

    public boolean haveHongzhong() {
        for (NxMj majiang : handPais) {
            if (majiang.isHongzhong()) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getHandPais() {
        return MjHelper.toMajiangIds(handPais);
    }

    public List<Integer> getHandPaisWhithOutMo(boolean isLiuJu) {
        List<Integer> ids = new ArrayList<>(getHandPais());
        if(!isLiuJu&&ids.size()%3==2)
            ids.remove(ids.size()-1);
        return ids;
    }

    public List<Integer> getOutPais() {
        return outPais;
    }

    public void moMajiang(NxMj majiang) {
        passMajiangVal.clear();
        handPais.add(majiang);
        getPlayingTable().changeCards(seat);
    }

    /**
     * 增加玩家明面上的牌
     *
     * @param action
     * @param disSeat 碰或杠的出牌人
     */
    public void addOutPais(List<NxMj> cards, int action, int disSeat) {
        handPais.removeAll(cards);
        if (action == MjDisAction.action_chupai) {
            outPais.addAll(MjHelper.toMajiangIds(cards));
        } else {
            if (action == MjDisAction.action_peng) {
                peng.addAll(cards);
            } else if (action == MjDisAction.action_minggang) {
                mGang.addAll(cards);
                if (cards.size() != 1) {
                } else {
                    NxMj pengMajiang = cards.get(0);
                    Iterator<NxMj> iterator = peng.iterator();
                    while (iterator.hasNext()) {
                        NxMj majiang = iterator.next();
                        if (majiang.getVal() == pengMajiang.getVal()) {
                            mGang.add(majiang);
                            iterator.remove();
                        }
                    }
                }
                changeAction(NxMjConstants.ACTION_COUNT_INDEX_MINGGANG, 1);
            } else if (action == MjDisAction.action_angang) {
                aGang.addAll(cards);
                changeAction(NxMjConstants.ACTION_COUNT_INDEX_ANGANG, 1);
            }
            getPlayingTable().changeExtend();
            setPassHu(0);
            setPassPengVal(0);
            addCardType(action, cards, disSeat, 0);
        }
        getPlayingTable().changeCards(seat);
    }

    /**
     * 添加牌型，例如将碰变成明杠等
     *
     * @param action
     * @param disCardList
     */
    public void addCardType(int action, List<NxMj> disCardList, int disSeat, int disStatus) {
        if (action != 0) {
            if (action == MjDisAction.action_minggang && disCardList.size() == 1) {
                NxMj majiang = disCardList.get(0);
                for (MjCardDisType disType : cardTypes) {
                    if (disType.getAction() == MjDisAction.action_peng) {
                        if (disType.isHasCardVal(majiang)) {
                            disType.setAction(MjDisAction.action_minggang);
                            disType.addCardId(majiang.getId());
                            disType.setDisSeat(seat);
                            break;
                        }
                    }
                }
            } else {
                MjCardDisType type = new MjCardDisType();
                type.setAction(action);
                type.setCardIds(MjQipaiTool.toMajiangIds(disCardList));
                type.setDisSeat(disSeat);
                type.setDisStatus(disStatus);
                cardTypes.add(type);
            }

        }
    }

    /**
     * 已经摸过牌了
     *
     * @return
     */
    public boolean isAlreadyMoMajiang() {
        return !handPais.isEmpty() && handPais.size() % 3 == 2;
    }

    public void removeOutPais(List<NxMj> cards) {
        boolean remove = outPais.removeAll(MjHelper.toMajiangIds(cards));
        if (remove) {
            getPlayingTable().changeCards(seat);
        }
    }

    public String toExtendStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtil.implode(actionArr)).append("|");
        sb.append(StringUtil.implode(actionTotalArr)).append("|");
        sb.append(StringUtil.implode(passGangValList, ",")).append("|");
        sb.append(StringUtil.implode(huType, ",")).append("|");
        sb.append(StringUtil.implode(passMajiangVal, ",")).append("|");
        sb.append(StringUtil.implode(pointArr)).append("|");
        return sb.toString();
    }

    public void initExtend(String info) {
        if (StringUtils.isBlank(info)) {
            return;
        }
        int i = 0;
        String[] values = info.split("\\|");
        String val4 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val4)) {
            actionArr = StringUtil.explodeToIntArray(val4);
        }
        String val5 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val5)) {
            actionTotalArr = StringUtil.explodeToIntArray(val5);
        }
        String val6 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val6)) {
            passGangValList = StringUtil.explodeToIntList(val6);
        }
        String val7 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val7)) {
            huType = StringUtil.explodeToIntList(val7);
        }
        String val8 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val8)) {
            passMajiangVal = StringUtil.explodeToIntList(val8);
        }

        String val9 = StringUtil.getValue(values, i++);
        if (!StringUtils.isBlank(val9)) {
            pointArr = StringUtil.explodeToIntArray(val9);
        }
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
        changeTbaleInfo();
    }

    public PlayerInTableRes.Builder buildPlayInTableInfo() {
        return buildPlayInTableInfo(false);
    }

    /**
     * 吃碰杠过的牌
     *
     * @return
     */
    public List<Integer> getMoldIds() {
        List<Integer> list = new ArrayList<>();
        if (!peng.isEmpty()) {
            list.addAll(MjHelper.toMajiangIds(peng));
        }
        if (!mGang.isEmpty()) {
            list.addAll(MjHelper.toMajiangIds(mGang));
        }
        if (!aGang.isEmpty()) {
            list.addAll(MjHelper.toMajiangIds(aGang));
        }
        return list;
    }



    public List<PhzHuCards> buildDisCards(long lookUid) {
        return buildDisCards(lookUid, true);
    }

    public List<PhzHuCards> buildDisCards(long lookUid, boolean hide) {
        List<PhzHuCards> list = new ArrayList<>();
        NanxMjTable table = getPlayingTable(NanxMjTable.class);
        for (MjCardDisType type : cardTypes) {
            if ((lookUid != this.userId && type.getAction() == MjDisAction.action_angang)) {
                // 不是本人并且是栽
                list.add(type.buildMsg(true).build());
            } else {
                list.add(type.buildMsg().build());
            }
        }
        return list;
    }

    /**
     * @param isrecover 是否重连
     * @return
     */
    public PlayerInTableRes.Builder buildPlayInTableInfo(boolean isrecover) {
        NanxMjTable table = getPlayingTable(NanxMjTable.class);

        PlayerInTableRes.Builder res = PlayerInTableRes.newBuilder();
        res.setUserId(userId + "");
        if (!StringUtils.isBlank(ip)) {
            res.setIp(ip);
        } else {
            res.setIp("");
        }
        res.setName(name);
        res.setSeat(seat);
        res.setSex(sex);
        res.setPoint(getTotalPoint() + getLostPoint());
        res.addAllOutedIds(getOutPais());
        res.addAllMoldIds(getMoldIds());
        res.addAllAngangIds(MjHelper.toMajiangIds(aGang));
        res.addAllMoldCards(buildDisCards(userId));
        List<NxMj> gangList = getGang();
        // 是否杠过
        res.addExt(gangList.isEmpty() ? 0 : 1);    //0
        // 现在是否自己摸的牌
        res.addExt((isAlreadyMoMajiang()) ? 1 : 0);//1
        res.addExt(handPais != null ? handPais.size() : 0);//2
        res.addExt(autoPlay ? 1 : 0);              //3
        res.addExt(autoPlaySelf ? 1 : 0);          //4
        res.addExt(piaoFen);                       //5
        res.addExt(0);                             //6
        res.addExt(baoTing);                       //7
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        if (state == player_state.ready || state == player_state.play) {
            // 玩家装备已经准备和正在玩的状态时通知前台已准备
            res.setStatus(SharedConstants.state_player_ready);
        } else {
            res.setStatus(0);
        }
        if (isrecover) {
        }
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

    public List<NxMj> getGang() {
        List<NxMj> gang = new ArrayList<>();
        gang.addAll(aGang);
        gang.addAll(mGang);
        return gang;
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

    public void calcLost(int lostCount, int point) {
        this.lostCount += lostCount;
        changePoint(point);

    }

    public void calcWin(int winCount, int point) {
        this.winCount += winCount;
        changePoint(point);

    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public void setLostPoint(int lostPoint) {
        this.lostPoint = lostPoint;
        changeTbaleInfo();
    }

    public void changeLostPoint(int point) {
        this.lostPoint += point;
        changeTbaleInfo();
    }

    public int getLostPoint() {
        return lostPoint;
    }

    public void changePoint(int point) {
        this.point += point;
        myExtend.changePoint(getPlayingTable().getPlayType(), point);
        //myExtend.setMjFengshen(FirstmythConstants.firstmyth_index7, point);
        changeTotalPoint(point);
        if (point > getMaxPoint()) {
            setMaxPoint(point);
        }
        changeTbaleInfo();
    }

    public int getBaoTing() {
        return baoTing;
    }

    public void setBaoTing(int baoTing) {
        this.baoTing = baoTing;
    }

    public void clearTableInfo() {
        BaseTable table = getPlayingTable();
        boolean isCompetition = false;
        if (table != null && table.isCompetition()) {
            isCompetition = true;
            endCompetition();
        }
        setIsEntryTable(0);
        changeIsLeave(0);
        getHandMajiang().clear();
        getOutPais().clear();
        changeState(null);
        actionArr = new int[5];
        actionTotalArr = new int[5];
        peng.clear();
        aGang.clear();
        mGang.clear();
        cardTypes.clear();
        huType.clear();
        passMajiangVal.clear();
        clearPassGangVal();
        setWinCount(0);
        setLostCount(0);
        setPoint(0);
        setLostPoint(0);
        setTotalPoint(0);
        setSeat(0);
        if (!isCompetition) {
            setPlayingTableId(0);
        }
        autoPlaySelf = false;
        autoPlay = false;
        lastCheckTime = System.currentTimeMillis();
        checkAutoPlay = false;
        huNum=0;//胡牌次数
        jiePaoNum=0;
        dianPaoNum=0;//点炮次数
        saveBaseInfo();
        setPiaoFen(-1);
        setWinLostPiaoFen(0);
        moNum=0;
        canPeng=true;
        passHu=0;
        pointArr=new int[4];
        passPengVal=0;
        gangNum=0;
        dianGangNum=0;
        baoTing=0;
    }

    /**
     * 单局详情
     *
     * @return
     */
    public ClosingMjPlayerInfoRes.Builder bulidOneClosingPlayerInfoRes() {
        ClosingMjPlayerInfoRes.Builder res = ClosingMjPlayerInfoRes.newBuilder();
        NanxMjTable table = getPlayingTable(NanxMjTable.class);
        res.setUserId(userId + "");
        res.setName(name);
        res.setPoint(point);
        res.setTotalPoint(getTotalPoint());
        res.setSeat(seat);
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
        res.addAllHandPais(getHandPaisWhithOutMo(table.isLiuJu()==1));
        List<PhzHuCards> list = new ArrayList<>();
        for (MjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
        res.addExt(piaoFen);
        return res;
    }

    /**
     * 总局详情
     *
     * @return
     */
    public ClosingMjPlayerInfoRes.Builder bulidTotalClosingPlayerInfoRes() {
        ClosingMjPlayerInfoRes.Builder res = ClosingMjPlayerInfoRes.newBuilder();
        NanxMjTable table = getPlayingTable(NanxMjTable.class);
        res.setUserId(userId + "");
        res.setName(name);
        res.setPoint(point);
        res.addAllActionCount(Arrays.asList(ArrayUtils.toObject(actionTotalArr)));
        res.setTotalPoint(getTotalPoint());
        res.setSeat(seat);
        if (!StringUtils.isBlank(getHeadimgurl())) {
            res.setIcon(getHeadimgurl());
        } else {
            res.setIcon("");
        }
        res.setSex(sex);
        res.addAllHandPais(getHandPaisWhithOutMo(table.isLiuJu()==1));
        List<PhzHuCards> list = new ArrayList<>();
        for (MjCardDisType type : cardTypes) {
            list.add(type.buildMsg().build());
        }
        res.addAllMoldPais(list);
        res.addAllDahus(huType);
        res.addAllPointArr(Arrays.asList(ArrayUtils.toObject(pointArr)));
        res.addExt(piaoFen);
        res.setActionCount(0,huNum);
        res.setActionCount(1,dianPaoNum);
        res.setActionCount(2,gangNum);
        res.setActionCount(3,dianGangNum);
        return res;
    }

    /**
     * TODO 设置大结算的胜负次数
     */
//	public void setBaseCount(){
//		this.winCnt=winCount;
//		this.loseCnt=lostCount;
//	}
    public void changeTbaleInfo() {
        BaseTable table = getPlayingTable();
        if (table != null)
            table.changePlayers();
    }

    @Override
    public void initNext() {
        getHandMajiang().clear();
        getOutPais().clear();
        setPoint(0);
        passMajiangVal.clear();
        setLostPoint(0);
        clearPassGangVal();
        peng.clear();
        aGang.clear();
        mGang.clear();
        cardTypes.clear();
        actionArr = new int[5];
        huType.clear();
        getPlayingTable().changeExtend();
        getPlayingTable().changeCards(seat);
        changeState(player_state.entry);
        if (autoPlaySelf) {
            autoPlaySelf = false;
            autoPlay = false;
            checkAutoPlay = false;
            lastCheckTime = System.currentTimeMillis();
        }
        if (!autoPlay) {
            checkAutoPlay = false;
            lastCheckTime = System.currentTimeMillis();
        }
        NanxMjTable table = getPlayingTable(NanxMjTable.class);
        if(table.getPiaoFenType()!=2)
            setPiaoFen(-1);
        setWinLostPiaoFen(0);
        moNum=0;
        canPeng=true;
        passHu=0;
        pointArr=new int[4];
        passPengVal=0;
        baoTing=0;
    }

    @Override
    public void initPais(String handPai, String outPai) {
        if (!StringUtils.isBlank(handPai)) {
            List<Integer> list = StringUtil.explodeToIntList(handPai);
            this.handPais = MjHelper.toMajiang(list);
        }
        if (!StringUtils.isBlank(outPai)) {
            String[] values = outPai.split(";");
            int i = -1;
            for (String value : values) {
                i++;
                if (i == 0) {
                    List<Integer> list = StringUtil.explodeToIntList(value);
                    if(list!=null)
                        this.outPais = list;
                }else {
                    MjCardDisType type = new MjCardDisType();
                    type.init(value);
                    cardTypes.add(type);
                    List<NxMj> majiangs = MjHelper.toMajiang(type.getCardIds());
                    if (type.getAction() == MjDisAction.action_angang) {
                        aGang.addAll(majiangs);
                    } else if (type.getAction() == MjDisAction.action_minggang) {
                        mGang.addAll(majiangs);
                    } else if (type.getAction() == MjDisAction.action_peng) {
                        peng.addAll(majiangs);
                    }
                }
            }
        }
    }

    /**
     * 出牌
     *
     * @return
     */
    public String buildOutPaiStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtil.implode(getOutPais())).append(";");
        for (MjCardDisType huxi : cardTypes) {
            sb.append(huxi.toStr()).append(";");
        }
        return sb.toString();
    }

    /**
     * 别人出牌可做的操作
     *
     * @param majiang
     * @param canCheckHu 是否能点炮 或 抢杠胡
     * @return 0胡 1碰 2明刚 3暗杠(暗杠后来不需要了 暗杠也用3标记)
     */
    public List<Integer> checkDisMajiang(NxMj majiang, boolean canCheckHu) {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        NanxMjTable table = getPlayingTable(NanxMjTable.class);
        // 转转麻将别人出牌可以胡
        if (passHu==0&&canCheckHu && !passMajiangVal.contains(majiang.getVal())) {
            // 没有出现漏炮的情况
            List<NxMj> copy = new ArrayList<>(handPais);
            copy.add(majiang);
            boolean hu = HuTool.isHu(MjHelper.toMajiangIds(copy),false,this);
            if (hu) {
                arr[NxMjConstants.ACTION_INDEX_HU] = 1;
            }
        }
        int count = MjHelper.getMajiangCount(handPais, majiang.getVal());
        if (baoTing==0&&count >= 2&&isCanPeng()&&passPengVal==0) {
            arr[NxMjConstants.ACTION_INDEX_PENG] = 1;// 可以碰
        }
        if (baoTing==0&&count == 3&&!passGangValList.contains(majiang.getVal())) {
            arr[NxMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
            canGang.addAll(MjHelper.find(new ArrayList<>(handPais),majiang.getVal()));
            sendGangMsg();
        }
        for (int val : arr) {
            list.add(val);
        }
        if (list.contains(1)) {
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 自己出牌可做的操作
     * @return 0胡 1碰 2明刚 3暗杠
     */
    public List<Integer> checkMo() {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        NanxMjTable table=getPlayingTable(NanxMjTable.class);
        if (HuTool.isHu(MjHelper.toMajiangIds(handPais),true,this)) {
            arr[NxMjConstants.ACTION_INDEX_HU] = 1;
        }
        if (isAlreadyMoMajiang()&&table.getLeftMajiangCount()>table.getMaxPlayerCount()) {
            if(baoTing==0){
                canGang.clear();
                Map<Integer, Integer> pengMap = MjHelper.toMajiangValMap(peng);
                for (NxMj handMajiang : handPais) {
                    if (pengMap.containsKey(handMajiang.getVal())) {
                        // 有碰过
                        arr[NxMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
                        canGang.add(handMajiang.getId());
                    }
                }
            }

            List<NxMj> copy = new ArrayList<>(handPais);
            Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(copy);
            if (handMap.containsValue(4)) {
                arr[NxMjConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
                List<Integer> anGangVal=new ArrayList<>();
                for (Map.Entry<Integer,Integer> entry:handMap.entrySet()) {
                    if(entry.getValue()==4){
                        anGangVal.add(entry.getKey());
                    }
                }
                List<NxMj> tcdpMjs = MjHelper.find(MjHelper.toMajiangIds(copy), anGangVal);
                canGang.addAll(MjHelper.toMajiangIds(tcdpMjs));
            }
            sendGangMsg();
        }
        for (int val : arr) {
            list.add(val);
        }
        if (list.contains(1)) {
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 自己出牌可做的操作
     * @return 0胡 1碰 2明刚 3暗杠
     */
    public List<Integer> checkGang() {
        List<Integer> list = new ArrayList<>();
        int[] arr = new int[6];
        NanxMjTable table=getPlayingTable(NanxMjTable.class);
        if (isAlreadyMoMajiang()&&table.getLeftMajiangCount()>table.getMaxPlayerCount()) {
            if(baoTing==0){
                canGang.clear();
                Map<Integer, Integer> pengMap = MjHelper.toMajiangValMap(peng);
                for (NxMj handMajiang : handPais) {
                    if (pengMap.containsKey(handMajiang.getVal())) {
                        // 有碰过
                        arr[NxMjConstants.ACTION_INDEX_MINGGANG] = 1;// 可以杠
                        canGang.add(handMajiang.getId());
                    }
                }
            }
            List<NxMj> copy = new ArrayList<>(handPais);
            Map<Integer, Integer> handMap = MjHelper.toMajiangValMap(copy);
            if (handMap.containsValue(4)) {
                arr[NxMjConstants.ACTION_INDEX_ANGANG] = 1;// 可以暗杠
                List<Integer> anGangVal=new ArrayList<>();
                for (Map.Entry<Integer,Integer> entry:handMap.entrySet()) {
                    if(entry.getValue()==4){
                        anGangVal.add(entry.getKey());
                    }
                }
                List<NxMj> tcdpMjs = MjHelper.find(MjHelper.toMajiangIds(copy), anGangVal);
                canGang.addAll(MjHelper.toMajiangIds(tcdpMjs));
            }
            sendGangMsg();
        }
        for (int val : arr) {
            list.add(val);
        }
        if (list.contains(1)) {
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    public List<Integer> checkBaoTing(){
        List<Integer> ting = TingTool.getTing(getHandPais(),this);
        if(ting.size()>0){
            int[] arr = new int[6];
            arr[NxMjConstants.ACTION_INDEX_BAOTING] = 1;
            List<Integer> list = new ArrayList<>();
            for (int val : arr) {
                list.add(val);
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * 获取可以杠的数组
     */
    public void sendGangMsg(){
        if(canGang.isEmpty())
            return;
        StringBuilder sb=new StringBuilder();
        for (Integer id:canGang) {
            if(sb.length()>0)
                sb.append(";");
            List<NxMj> mjs = MjHelper.getGangMajiangList(handPais, NxMj.getMajang(id).getVal());
            if(!mjs.isEmpty())
                sb.append(StringUtil.implode(MjHelper.toMajiangIds(mjs), ","));
            if(!MjHelper.toMajiangIds(handPais).contains(id)){
                if(sb.length()>0)
                    sb.append(",");
                sb.append(id);
            }
        }
        if(sb.length()>0)
            writeComMessage(WebSocketMsgType.res_code_cxmj_gangMsg, sb.toString());
    }

    public List<Integer> removeGangId(List<NxMj> mjs, int removeVal){
        List<Integer> ids=new ArrayList<>();
        for (NxMj mj:mjs) {
            if(mj.getVal()!=removeVal)
                ids.add(mj.getId());
        }
        return ids;
    }

    /**
     * 操作
     *
     * @param index 0点炮1点杠2明杠3暗杠4被碰5被杠6胡7自摸
     * @param val
     */
    public void changeAction(int index, int val) {
        actionArr[index] += val;
        actionTotalArr[index] += val;
        getPlayingTable().changeExtend();
    }

    public List<NxMj> getPeng() {
        return peng;
    }

    public List<NxMj> getaGang() {
        return aGang;
    }

    public List<NxMj> getmGang() {
        return mGang;
    }

    public void setPassGangValList(List<Integer> passGangValList) {
        this.passGangValList = passGangValList;
    }

    public void addGangNum() {
        this.gangNum++;
    }

    public void addDianGangNum() {
        this.dianGangNum++;
    }

    /**
     * 漏炮
     *
     * @param passVal
     */
    public void addPassMajiangVal(int passVal) {
        if (!passMajiangVal.contains(passVal)) {
            passMajiangVal.add(passVal);
            changeTbaleInfo();
        }
    }

    /**
     * 可以碰可以杠的牌 选择了碰 再杠不算分
     *
     * @param majiang
     * @return
     */
    public boolean isPassGang(NxMj majiang) {
        return passGangValList.contains(majiang.getVal());
    }

    public List<Integer> getPassGangValList() {
        return passGangValList;
    }

    /**
     * 可以碰可以杠的牌 选择了碰 再杠不算分
     *
     * @param passGangVal
     */
    public void addPassGangVal(int passGangVal) {
        if (!this.passGangValList.contains(passGangVal)) {
            this.passGangValList.add(passGangVal);
            getPlayingTable().changeExtend();
        }
    }

    public void clearPassGangVal() {
        this.passGangValList.clear();
        BaseTable table = getPlayingTable();
        if (table != null)
            table.changeExtend();
    }

    @Override
    public void endCompetition1() {
        // TODO Auto-generated method stub

    }

    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.game_type_nanxmj);

    public static void loadWanfaPlayers(Class<? extends Player> cls) {
        for (Integer integer : wanfaList) {
            PlayerManager.wanfaPlayerTypesPut(integer, cls, MjCommandProcessor.getInstance());
        }
    }

    /**
     * 检查托管
     *
     * @param autoType  0打牌，1准备，2胡牌
     * @param forceSend 立即推送倒计时，用于断线重连
     * @return
     */
    public boolean checkAutoPlay(int autoType, boolean forceSend) {
        NanxMjTable table = getPlayingTable(NanxMjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.getIsAutoPlay() >= 1) {
            //检查玩家是否进入系统托管状态
            if (!checkAutoPlay) {
                if (isAlreadyMoMajiang() || table.getActionSeatMap().containsKey(seat)) {
                    setCheckAutoPlay(true);
                } else {
                    setCheckAutoPlay(false);
                    return false;
                }
            }

            int timeOut = (int) (now - getLastCheckTime()) / 1000;
            //CxMjConstants.AUTO_TIMEOUT + CxMjConstants.AUTO_CHECK_TIMEOUT
            if (timeOut >=table.getIsAutoPlay()) {
                //进入托管状态
                auto = true;
                setAutoPlay(true, false);
            } else if (timeOut >= NxMjConstants.AUTO_CHECK_TIMEOUT) {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now - NxMjConstants.AUTO_CHECK_TIMEOUT * 1000);
                    timeOut = (int) (now - getLastCheckTime()) / 1000;
                }
                int timeSecond = table.getIsAutoPlay() - timeOut;
                if ((timeOut % 5 == 0 && isOnline()) || forceSend) {
                    //推送即将进入托管状态的倒计时
                    ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, (autoPlay ? 1 : 0), timeSecond, autoPlaySelf ? 1 : 0);
                    GeneratedMessage msg = res.build();
                    table.broadMsg(msg);
                }
            }
        }
        if (auto) {
            if (getAutoPlayTime() == 0L) {
                setAutoPlayTime(now);
            } else {
                int timeOut = (int) (now - getAutoPlayTime()) / 1000;
                if (autoType == 1) {
                    if (timeOut >= table.getIsAutoPlay()) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else if (autoType == 2) {
                    if (timeOut >= NxMjConstants.AUTO_HU_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                } else {
                    if (timeOut >= NxMjConstants.AUTO_PLAY_TIME) {
                        setAutoPlayTime(0);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查托管(飘分)
     * @return
     */
    public boolean checkAutoPiaoFen() {
        NanxMjTable table = getPlayingTable(NanxMjTable.class);
        long now = System.currentTimeMillis();
        boolean auto = isAutoPlay();
        if (!auto && table.getIsAutoPlay() >= 1) {
            //检查玩家是否进入系统托管状态
            if (!checkAutoPlay) {
                setCheckAutoPlay(true);
            }

            int timeOut = (int) (now - getLastCheckTime()) / 1000;
            if (timeOut >=table.getIsAutoPlay()) {
                //进入托管状态
                auto = true;
                setAutoPlay(true, false);
            } else if (timeOut >= NxMjConstants.AUTO_CHECK_TIMEOUT) {
                if (sendAutoTime == 0) {
                    sendAutoTime = now;
                    setLastCheckTime(now - NxMjConstants.AUTO_CHECK_TIMEOUT * 1000);
                    timeOut = (int) (now - getLastCheckTime()) / 1000;
                }
                int timeSecond = table.getIsAutoPlay() - timeOut;
                if ((timeOut % 5 == 0 && isOnline())) {
                    //推送即将进入托管状态的倒计时
                    ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, (autoPlay ? 1 : 0), timeSecond, autoPlaySelf ? 1 : 0);
                    GeneratedMessage msg = res.build();
                    table.broadMsg(msg);
                }
            }
        }
        if (auto) {
            setAutoPlayTime(now);
            return true;
        }
        return false;
    }

    public boolean isAutoPlaySelf() {
        return autoPlaySelf;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public int getPiaoFen() {
        return piaoFen;
    }

    public void setPiaoFen(int piaoFen) {
        this.piaoFen = piaoFen;
    }

    public void setAutoPlay(boolean autoPlay, boolean isSelf) {
        NanxMjTable table = getPlayingTable(NanxMjTable.class);
        if (this.autoPlay != autoPlay || autoPlaySelf != isSelf) {
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tuoguan, seat, autoPlay ? 1 : 0, 0, isSelf ? 1 : 0);
            GeneratedMessage msg = res.build();
            if (table != null) {
                table.broadMsgToAll(msg);
            }
        	if (!getHandPais().isEmpty()) {
            table.addPlayLog(table.getDisCardRound() + "_" +getSeat() + "_" + NxMjConstants.action_tuoguan + "_" +(autoPlay?1:0)+ getExtraPlayLog());
        	}

            LogUtil.msg("setAutoPlay|" + (table == null ? -1 : table.getIsAutoPlay()) + "|" + getSeat() + "|" + getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + (isSelf ? 1 : 0));
        }
        this.autoPlay = autoPlay;
        this.autoPlaySelf = autoPlay && isSelf;
        this.checkAutoPlay = autoPlay;
        setLastCheckTime(System.currentTimeMillis());
        if (table != null) {
            table.changeExtend(); 
        }
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
        if (getPlayingTable() != null) {
            getPlayingTable().changeExtend();
        }
    }

    public int getDianPaoNum() {
        return dianPaoNum;
    }

    public int getMoNum() {
        return moNum;
    }

    public void setMoNum(int moNum) {
        this.moNum = moNum;
    }

    public List<MjCardDisType> getCardTypes() {
        return cardTypes;
    }

    public void addDianPaoNum(int num) {
        this.dianPaoNum += num;
    }

    public void addJiePaoNum(int num) {
        this.jiePaoNum += num;
    }

    public void addHuNum() {
        this.huNum++;
    }

    public long getAutoPlayTime() {
        return autoPlayTime;
    }

    public void setAutoPlayTime(long autoPlayTime) {
        this.autoPlayTime = autoPlayTime;
//		getPlayingTable().changeExtend();
    }



    public boolean isCheckAutoPlay() {
        return checkAutoPlay;
    }

    public void setCheckAutoPlay(boolean checkAutoPlay) {
        this.checkAutoPlay = checkAutoPlay;
        this.lastCheckTime = System.currentTimeMillis();
        this.sendAutoTime = 0;
        if (getPlayingTable() != null) {
            getPlayingTable().changeExtend();
        }
    }

    /**
     * 回放日志的额外信息
     *
     * @return
     */
    public String getExtraPlayLog() {
        return "_" + (isAutoPlay() ? 1 : 0) + "," + (isAutoPlaySelf() ? 1 : 0);
    }
}
