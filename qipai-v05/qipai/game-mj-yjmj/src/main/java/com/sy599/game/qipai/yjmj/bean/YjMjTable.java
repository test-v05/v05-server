package com.sy599.game.qipai.yjmj.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.FirstmythConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.TableInf;
import com.sy599.game.db.bean.UserGroupPlaylog;
import com.sy599.game.db.bean.UserPlaylog;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.TableLogDao;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangMoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.GangPlayMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.MoMajiangRes;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.msg.serverPacket.TableMjResMsg;
import com.sy599.game.msg.serverPacket.TableRes.*;
import com.sy599.game.qipai.yjmj.constant.YjMjConstants;
import com.sy599.game.qipai.yjmj.rule.MajiangHelper;
import com.sy599.game.qipai.yjmj.rule.RobotAI;
import com.sy599.game.qipai.yjmj.rule.YjMj;
import com.sy599.game.qipai.yjmj.tool.YjMjResTool;
import com.sy599.game.qipai.yjmj.tool.YjMjTool;
import com.sy599.game.qipai.yjmj.tool.YjMjQipaiTool;
import com.sy599.game.qipai.yjmj.tool.YjMjHelper;
import com.sy599.game.qipai.yjmj.tool.HuUtil;
import com.sy599.game.udplog.UdpLogger;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ????????????????????????
 */
public class YjMjTable extends BaseTable {
    /**
     * ????????????????????????
     */
    private List<YjMj> nowDisCardIds = new ArrayList<>();
    /**
     * 0??? 1??? 2?????? 3?????? 4?????? 5?????? 6??????
     */
    private Map<Integer, List<Integer>> actionSeatMap = new ConcurrentHashMap<>();
    /**
     * 0??? 1??? 2?????? 3?????? 4?????? 5?????? 6??????
     */
    private Map<Integer, Map<Integer, List<Integer>>> gangSeatMap = new ConcurrentHashMap<>();
    /**
     * ??????????????????????????????
     */
    private int maxPlayerCount = 4;
    /**
     * ????????????????????????????????????
     */
    private List<YjMj> leftMajiangs = new ArrayList<>();
    /**
     * ??????????????????????????????map
     */
    private Map<Long, YjMjPlayer> playerMap = new ConcurrentHashMap<Long, YjMjPlayer>();
    /**
     * ???????????????????????????MAP
     */
    private Map<Integer, YjMjPlayer> seatMap = new ConcurrentHashMap<Integer, YjMjPlayer>();
    /**
     * ???????????????
     */
    private Map<Integer, Integer> huConfirmMap = new HashMap<>();
    /**
     * ??????
     */
    private int birdNum;
    /**
     * ???????????? (1???????????? 2????????????)
     */
    private int isCalcBanker;
    /**
     * ?????????????????? 1?????? 2??????
     */
    private int calcBird;

    /**
     * ???????????? 0?????????  ??????24???  ???????????????????????????YJMajiangConstants
     */
    private int fanshuLimit;
    /**
     * ??????????????? 0????????? 1?????????
     */
    private int hasMenQing;
    /**
     * ??????????????????????????????  0????????????  1?????????
     */
    private int menQingJiangJiangHu;
    /**
     * ??????????????? ??????  0??????  1??????
     */
    private int yizhiqiao;
    /**
     * ?????? ?????????????????? ????????????????????????????????????????????????????????????????????? ?????????
     */
    private int kaqiao;
    /**
     * ????????????seat
     */
    private int moMajiangSeat;
    /**
     * ???????????????
     */
    private YjMj moGang;
    /**
     * ??????????????????
     */
    private YjMj gangMajiang;
    /**
     * ?????????
     */
    private List<Integer> moGangHuList = new ArrayList<>();
    /**
     * ?????????????????????
     */
    private List<YjMj> gangDisMajiangs = new ArrayList<>();
    /**
     * ????????????????????????????????????????????????
     */
    private int moLastMajiangSeat;
    /**
     * ?????????????????????---??????pass??? 0?????? 1pass???
     */
    private Map<Integer, Integer> baotingSeat = new HashMap<>();
    /**
     * ??????????????????
     */
    private YjMj lastMajiang;
    /**
     * ????????????action
     */
    private int disEventAction;
    /**
     * ??????????????? ??????????????????????????????  ?????????????????? ????????????????????????
     */
    private Map<Integer, List<Integer>> pengPassMap = new HashMap<>();
    /**
     * ??????????????? ?????????????????????????????????  ?????????????????? ???????????????
     */
    private List<Integer> huPassList = new ArrayList<>();

    /**
     * ?????????
     */
    private List<YjMj> birdPaiList = new ArrayList<>();

    private long groupPaylogId = 0;  //?????????????????????id

    private int readyTime = 0;
    /**
     * ??????1????????????2?????????
     */
    private int autoPlayGlob;
    private int autoTableCount;
    // ????????????
    private int autoTime;
    private int tableStatus;//???????????? 1??????

    // ???????????????0??????1???
    private int jiaBei;
    // ?????????????????????xx???????????????
    private int jiaBeiFen;
    // ????????????????????????
    private int jiaBeiShu;
    // ?????? below ??? belowAdd ???
    private int belowAdd = 0;
    private int below = 0;

    // ?????????0???????????????13???26?????????????????????
    private int chouPai = 0;
    // ????????????
    List<Integer> chouCards = new ArrayList<>();

    // ???????????????
    private int maMaHu = 0;

    public List<YjMj> getBirdPaiList() {
        return birdPaiList;
    }

    public void setBirdPaiList(List<YjMj> birdPaiList) {
        this.birdPaiList = birdPaiList;
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
        List<Integer> winList = new ArrayList<>(huConfirmMap.keySet());
        boolean selfMo = false;
        int[] birdMjIds = null;
        int[] seatBirds = null;
        Map<Integer, Integer> seatBirdMap = new HashMap<>();
        boolean flow = false;
        int startseat = 0;
        int fangPaoSeat = 0;
        int[] jiePaoSeat = null;
        if (winList.size() == 0) { // ??????
            flow = true;
        } else { // ??????????????????????????????
            YjMjPlayer winPlayer = null;
            if (winList.size() == 1) {
                winPlayer = seatMap.get(winList.get(0));
                if ((winPlayer.isAlreadyMoMajiang() || winPlayer.isGangshangHua()) && winList.get(0) == moMajiangSeat) {
                    if (moGang == null)
                        selfMo = true;
                }
            }
            // ??????????????????????????????????????? ???????????????????????????
            if (isCalcBanker == 1) { // ??????????????????
                startseat = winList != null && winList.size() > 1 ? disCardSeat : winList.get(0);
            } else {// ????????????????????????
                startseat = lastWinSeat;
            }
            if (!winList.isEmpty() && selfMo) { // ???????????????
                pickBird();// ?????????
                seatBirds = birdToSeat(startseat);// ??????????????????
            }

            if (selfMo) {
                int winSeat = winList.get(0);
                int winBirdNum = calcBirdNum(seatBirds, winSeat);
                seatBirdMap.put(winList.get(0), winBirdNum);
                int loseTotalPoint = 0;
                for (int seat : seatMap.keySet()) {
                    if (!winList.contains(seat)) {// ????????????????????????
                        YjMjPlayer player = seatMap.get(seat);
                        int losePoint = player.getLostPoint();
                        int birdCount = calcBirdNum(seatBirds, seat);
                        if (birdCount != 0) {
                            seatBirdMap.put(seat, birdCount);
                        }
                        losePoint = calcBirdPoint(losePoint, winBirdNum + birdCount);// ?????????
                        loseTotalPoint += losePoint;
                        player.setLostPoint(losePoint);
                    }
                }
                for (int seat : winList) {
                    YjMjPlayer player = seatMap.get(seat);
                    player.changeAction(0, 1);
                    player.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index8, 1);
                    player.setLostPoint(-loseTotalPoint);
                }
            } else {
                // ???????????????
                YjMjPlayer losePlayer = seatMap.get(disCardSeat);
                losePlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index10, winList.size());
                int totalLosePoint = 0;
                jiePaoSeat = new int[winList.size()];
                int i = 0;
                for (int seat : winList) {// ??????
                    winPlayer = seatMap.get(seat);
                    int point = winPlayer.getLostPoint();
                    point *= 2;// ????????????
                    if (getFanshuLimit() > 0 && Math.abs(point) >= YjMjConstants.fanshuLimit) {// ????????????????????????
                        point = YjMjConstants.fanshuLimit;
                    }
                    winPlayer.changeAction(1, 1);
                    totalLosePoint += point;
                    winPlayer.setLostPoint(point);
                    jiePaoSeat[i++] = seat;
                }
                losePlayer.changeAction(2, 1);
                losePlayer.setLostPoint(-totalLosePoint);
                fangPaoSeat = losePlayer.getSeat();
            }
        }
        // ?????????????????????
        calcExtraPoint(winList);// ??????????????????????????????
        for (YjMjPlayer player : seatMap.values()) {//??????????????????point
            player.changePoint(player.getLostPoint());
            logHuPoint(player);
        }

        boolean over = playBureau >= totalBureau;
        if (autoPlayGlob > 0) {
            //????????????
            boolean diss = false;
            if (autoPlayGlob == 1) {
                for (YjMjPlayer seat : seatMap.values()) {
                    if (seat.isAutoPlay()) {
                        diss = true;
                        break;
                    }
                }
            } else if (autoPlayGlob == 3) {
                diss = checkAuto3();
            }
            if (diss) {
                autoPlayDiss = true;
                over = true;
            }
        }

        YjClosingInfoRes.Builder res = sendAccountsMsg(over, selfMo, winList, birdMjIds, seatBirds, seatBirdMap, false, startseat, fangPaoSeat, jiePaoSeat);
        if (!flow) {// ????????????
            if (winList.size() > 1) {// ???????????????????????????????????????
                setLastWinSeat(disCardSeat);
            } else {
                setLastWinSeat(winList.get(0));
            }
        } else {
            if (moLastMajiangSeat != 0) {// ???????????? ???????????????????????????
                setLastWinSeat(moLastMajiangSeat);
            }
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

        for (YjMjPlayer player : seatMap.values()) {
            if (player.isAutoPlaySelf()) {
                player.setAutoPlay(false, false);
            }
        }
        for (Player player : seatMap.values()) {
            player.saveBaseInfo();
        }
    }

    private boolean checkAuto3() {
        boolean diss = false;
        boolean diss2 = false;
        for (YjMjPlayer seat : seatMap.values()) {
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
        return diss;
    }

    /**
     * ?????????????????????????????????
     */
    private void calcExtraPoint(List<Integer> winSeats) {
        if (getYizhiqiao() > 0) {// ?????????????????????
            for (int seat : seatMap.keySet()) {
                YjMjPlayer player = seatMap.get(seat);
                if (player.getHandPais().size() <= 2) {
                    for (int tempSeat : seatMap.keySet()) {
                        if (tempSeat != seat) {// ?????????????????????
                            YjMjPlayer tempPlayer = seatMap.get(tempSeat);
                            tempPlayer.changeLostPoint(-YjMjHu.xiaoMengZiBasePoint * 2);
                        }
                    }
                    player.changeLostPoint(2 * YjMjHu.xiaoMengZiBasePoint * (maxPlayerCount - 1));
                }
            }
        }
        for (int seat : seatMap.keySet()) {// ???????????? ?????????????????????????????????, ????????????????????? ?????????2????????????, ???????????????2????????????
            YjMjPlayer player = seatMap.get(seat);
            int aGangNum = player.getGangInfos()[0];// 0????????????
            int mGangNum = player.getGangInfos()[1];// 1????????????
            int jGangNum = player.getGangInfos()[2];// 2????????????
            int fGangNum = player.getGangInfos()[3];// 3????????????
            if (aGangNum > 0 || mGangNum > 0) {
                int gangWinPoint = 0;
                for (int tempSeat : seatMap.keySet()) {
                    if (seat != tempSeat) {
                        YjMjPlayer tempPlayer = seatMap.get(tempSeat);
                        int gangLossPoint = (aGangNum * 2 + mGangNum) * YjMjHu.xiaoMengZiBasePoint;
                        gangWinPoint += gangLossPoint;
                        tempPlayer.changeLostPoint(-gangLossPoint);
                    }
                }
                player.changeLostPoint(gangWinPoint);
            }
            if (jGangNum > 0) {
                player.changeLostPoint(jGangNum * 2 * YjMjHu.xiaoMengZiBasePoint);
            }
            if (fGangNum > 0) {
                player.changeLostPoint(-fGangNum * 2 * YjMjHu.xiaoMengZiBasePoint);
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param point
     * @param bird
     * @return
     */
    private int calcBirdPoint(int point, int bird) {
        if (bird > 0) {
            if (calcBird == 1) {// ???
                point = point * (bird + 1);
            } else if (calcBird == 2) {// ??? ?????????2???2??????
                point = (int) (point * (Math.pow(2, bird)));
            } else {// ???1
                point = point - bird;
            }
        }
        if (getFanshuLimit() > 0 && Math.abs(point) >= YjMjConstants.fanshuLimit) {
            return -YjMjConstants.fanshuLimit;
        }
        return point;
    }

    public void saveLog(boolean over, long winId, Object resObject) {
        LogUtil.d_msg("tableId:" + id + " play:" + playBureau + " over:" + resObject);
        YjClosingInfoRes res = (YjClosingInfoRes) resObject;
        String logRes = JacksonUtil.writeValueAsString(YjMjTool.buildClosingInfoResLog(res));
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
        userLog.setUserId(creatorId);
        userLog.setExtend(logOtherRes);
        long logId = TableLogDao.getInstance().save(userLog);
        if (isGroupRoom()) {
            UserGroupPlaylog userGroupLog = new UserGroupPlaylog();
            userGroupLog.setTableid(id);
            userGroupLog.setUserid(creatorId);
            userGroupLog.setCount(playBureau);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            userGroupLog.setCreattime(sdf.format(createTime));
            String players = "";
            String score = "";
            String diFenScore = "";
            for (int i = 1; i <= seatMap.size(); i++) {
                if (i == seatMap.size()) {
                    players += seatMap.get(i).getUserId();
                    score += seatMap.get(i).getTotalPoint();
                    diFenScore += seatMap.get(i).getTotalPoint();
                } else {
                    players += seatMap.get(i).getUserId() + ",";
                    score += seatMap.get(i).getTotalPoint() + ",";
                    diFenScore += seatMap.get(i).getTotalPoint() + ",";
                }
            }
            userGroupLog.setPlayers(players);
            userGroupLog.setScore(score);
            userGroupLog.setDiFenScore(diFenScore);
            userGroupLog.setDiFen(1 + "");
            userGroupLog.setOvertime(sdf.format(now));
            userGroupLog.setPlayercount(maxPlayerCount);
            String groupId = isGroupRoom() ? loadGroupId() : 0 + "";
            userGroupLog.setGroupid(Long.parseLong(groupId));
            userGroupLog.setGamename("????????????");
            userGroupLog.setTotalCount(totalBureau);
            if (playBureau == 1) {
                groupPaylogId = TableLogDao.getInstance().saveGroupPlayLog(userGroupLog);
            } else if (playBureau > 1 && groupPaylogId != 0) {
                userGroupLog.setId(groupPaylogId);
                TableLogDao.getInstance().updateGroupPlayLog(userGroupLog);
            }
        }
        saveTableRecord(logId, over, playBureau);
        for (YjMjPlayer player : playerMap.values()) {
            player.addRecord(logId, playBureau);
        }
        UdpLogger.getInstance().sendSnapshotLog(masterId, playLog, logRes);
    }

    /**
     * ????????????
     *
     * @param seatBirdArr
     * @param seat
     * @return
     */
    private int calcBirdNum(int[] seatBirdArr, int seat) {
        if (seatBirdArr == null) {
            return 0;
        }
        int point = 0;
        for (int seatBird : seatBirdArr) {
            if (seat == seatBird) {
                point++;
            }
        }
        return point;
    }

    /**
     * ??????
     *
     * @return
     */
    private void pickBird() {
        birdPaiList.clear();
        if (getLeftMajiangCount() > 0) {
            // ?????????????????????
            YjMj last = null;
            for (int i = 0; i < birdNum; i++) {
                YjMj birdMj = getLeftMajiang();
                if (birdMj != null) {
                    // ??????????????????????????????????????????????????????????????????
                    last = birdMj;
                }
                birdPaiList.add(last);
            }
        } else {
            // ???????????????
            for (int i = 0; i < this.birdNum; i++) {
                birdPaiList.add(this.getLastMajiang());
            }
        }
    }

    /**
     * ??????
     * ??????????????????
     *
     * @param winSeat
     * @return
     */
    private int[] birdToSeat(int winSeat) {
        int birdSize = birdPaiList.size();
        if (birdSize == 0) {
            return null;
        }
        int[] seatArr = new int[birdSize];
        for (int i = 0; i < birdSize; i++) {
            YjMj birdMj = birdPaiList.get(i);
            int birdMjPai = birdMj.getPai();
            if (getMaxPlayerCount() == 2) {
                // -----------?????????????????????159????????????26?????????--------------
                int loseSeat = winSeat == 1 ? 2 : 1;
                if (birdMjPai == 1 || birdMjPai == 5 || birdMjPai == 9) {
                    seatArr[i] = winSeat;
                } else if (birdMjPai == 2 || birdMjPai == 6) {
                    seatArr[i] = loseSeat;
                }
            } else {
                birdMjPai = (birdMjPai - 1) % 4;// ?????????????????? ?????????1
                int birdSeat = birdMjPai + winSeat > 4 ? birdMjPai + winSeat - 4 : birdMjPai + winSeat;
                seatArr[i] = birdSeat;
            }
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
                tempMap.put("nowDisCardIds", StringUtil.implode(MajiangHelper.toMajiangIds(nowDisCardIds), ","));
            }
            if (tempMap.containsKey("leftPais")) {
                tempMap.put("leftPais", StringUtil.implode(MajiangHelper.toMajiangIds(leftMajiangs), ","));
            }
            if (tempMap.containsKey("nowAction")) {
                tempMap.put("nowAction", buildNowAction());
            }
            if (tempMap.containsKey("extend")) {
                tempMap.put("extend", buildExtend());
            }
            //            TableDao.getInstance().save(tempMap);
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
    protected void sendDealMsg(long userId) {// ?????????????????? ??????
        boolean hasBaoTing = false;
        logFaPaiTable();
        DealInfoRes.Builder bankRes = null;
        for (YjMjPlayer tablePlayer : seatMap.values()) {
            DealInfoRes.Builder res = DealInfoRes.newBuilder();
            List<Integer> actionList = tablePlayer.checkMo(null, true, false);
            if (!actionList.isEmpty()) {
                if (actionList.get(YjMjConstants.ACTION_INDEX_BAOTING) == 1) {
                    hasBaoTing = true;
                }
                addActionSeat(tablePlayer.getSeat(), actionList);
                res.addAllSelfAct(actionList);
            }
            res.addAllHandCardIds(tablePlayer.getHandPais());
            res.setNextSeat(getNextDisCardSeat());
            res.setGameType(getWanFa());
            res.setRemain(leftMajiangs.size());
            res.setBanker(lastWinSeat);
            logFaPaiPlayer(tablePlayer, actionList);
            if (tablePlayer.getSeat() == lastWinSeat) {
                bankRes = res;
                continue;
            }
            tablePlayer.writeSocket(res.build());
            sendTingInfo(tablePlayer);
            if (tablePlayer.isAutoPlay()) {
                addPlayLog(getDisCardRound() + "_" + tablePlayer.getSeat() + "_" + YjMjDisAction.action_tuoguan + "_" + 1 + tablePlayer.getExtraPlayLog());
            }
        }
        YjMjPlayer bankPlayer = seatMap.get(lastWinSeat);
        bankRes.setBaoting(hasBaoTing ? 1 : 0);
        bankPlayer.writeSocket(bankRes.build());
        sendTingInfo(bankPlayer);
    }

    /**
     * ??????
     *
     * @param player
     * @param isGangMo ??????????????????
     */
    public void moMajiang(YjMjPlayer player, boolean isGangMo) {
        if (state != table_state.play) {
            return;
        }
        if (leftMajiangs.size() == 0) {
            calcOver();
            return;
        }
        if (leftMajiangs.size() <= maxPlayerCount) {
            setMoLastMajiangSeat(player.getSeat());
        }
        // ??????
        YjMj majiang = null;
        boolean isZp = false;
        if (disCardRound != 0) {
            if (player.isAlreadyMoMajiang()) {
                return;
            }
            if (GameServerConfig.isDeveloper() && !player.isRobot()) {
                if (zpMap.containsKey(player.getUserId()) && zpMap.get(player.getUserId()) > 0) {
                    majiang = MajiangHelper.findMajiangByVal(leftMajiangs, zpMap.get(player.getUserId()));
                    if (majiang != null) {
                        zpMap.remove(player.getUserId());
                        leftMajiangs.remove(majiang);
                        isZp = true;
                    } else {
                        if (leftMajiangs.size() <= maxPlayerCount) {
                            majiang = getLeftMajiang();
                            majiang = YjMj.getMajiangByValue(zpMap.get(player.getUserId()));
                            isZp = true;
                        }
                    }
                }
            }
            if (isZp == false) {
                majiang = getLeftMajiang();
            }
        }
        if (majiang != null) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + YjMjDisAction.action_moMjiang + "_" + majiang.getId());
            player.moMajiang(majiang);
            if (leftMajiangs.size() < maxPlayerCount) {
                player.setHaidiMajiang(majiang.getId());
                setLastMajiang(majiang);
            }
        }
        // ????????????
        clearActionSeatMap();
        if (disCardRound == 0) {
            return;
        }
        setMoMajiangSeat(player.getSeat());
        List<Integer> arr = player.checkMo(majiang, false, isGangMo);
        if (!arr.isEmpty()) {
            coverAddActionSeat(player.getSeat(), arr);
        }
        logMoMj(player, majiang, arr);
        MoMajiangRes.Builder res = MoMajiangRes.newBuilder();
        res.setUserId(player.getUserId() + "");
        res.setRemain(getLeftMajiangCount());
        res.setSeat(player.getSeat());
        boolean disMajiang = baotingSeat.containsKey(player.getSeat()) && arr.isEmpty() ? true : false;
        for (YjMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                if (true) {
                    MoMajiangRes.Builder copy = res.clone();
                    copy.addAllSelfAct(arr);
                    if (majiang != null) {
                        copy.setMajiangId(majiang.getId());
                    }
                    seat.writeSocket(copy.build());
                }
            } else {
                seat.writeSocket(res.build());
            }
        }
        if (disMajiang && leftMajiangs.size() >= maxPlayerCount) {// ??????????????????
            List<YjMj> disMjiang = new ArrayList<>();
            disMjiang.add(majiang);
//            chuPai(player, disMjiang, 0);
        }
        sendTingInfo(player);
    }

    private void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<YjMj> majiangs) {
        YjMjResTool.buildPlayRes(builder, player, action, majiangs);
        buildPlayRes1(builder);
    }

    private void buildPlayRes1(PlayMajiangRes.Builder builder) {
        // builder
    }

    /**
     * ???????????????
     *
     * @param player
     * @param majiangs
     */
    private void hu(YjMjPlayer player, List<YjMj> majiangs, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (huConfirmMap.containsKey(player.getSeat())) {
            return;
        }
        boolean zimo = player.isAlreadyMoMajiang();// ????????????
        YjMj disMajiang = null;
        if (!zimo) { // ????????????
            if (moGangHuList.contains(player.getSeat())) {
                disMajiang = moGang;// ?????????
            } else {
                if (!nowDisCardIds.isEmpty()) {
                    disMajiang = nowDisCardIds.get(0);
                }
            }
        }
        if (lastMajiang != null && moGangHuList.contains(player.getSeat())) {
            disMajiang = moGang;// ?????????
            zimo = false;
        }

        YjMjHu huBean = player.checkHu(disMajiang, nowDisCardIds == null || nowDisCardIds.isEmpty() ? true : false);
        if (actionSeatMap.get(player.getSeat()).get(5) == 1) {// ?????????
            huBean.setGangBao(true);
            huBean.initDahuList();
        }
        if (huBean.isHu() && lastMajiang != null) {
            huBean.setHaidilao(true);
            huBean.initDahuList();
        }
        if (!huBean.isHu()) {
            return;
        }
        int fromSeat = 0;
        if (moGangHuList.contains(player.getSeat())) { // ?????????????????????
            if (disEventAction == YjMjDisAction.action_minggang) {// ?????????
                huBean.setQiangGangHu(true);
                if (huBean.isHaidilao()) {// ????????????????????????
                    huBean.setHaidilao(false);
                }
                huBean.initDahuList();
            }
            YjMjPlayer moGangPlayer = getPlayerByHasMajiang(moGang);
            if (moGangPlayer == null) {
                moGangPlayer = seatMap.get(moMajiangSeat);
            }
            List<YjMj> moGangMajiangs = new ArrayList<>();
            moGangMajiangs.add(moGang);
            moGangPlayer.addOutPais(moGangMajiangs, -1, 0);// ?????????????????? ??????????????????????????????
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();// ???????????????
            buildPlayRes(disBuilder, moGangPlayer, 0, moGangMajiangs);
            fromSeat = moGangPlayer.getSeat();
            sendDisMajiangAction(disBuilder);
            recordDisMajiang(moGangMajiangs, moGangPlayer);
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + 0 + "_" + MajiangHelper.toMajiangStrs(majiangs));
        }
        if (baotingSeat.containsKey(player.getSeat())) { // ???????????????
            if (huBean.isQiangGangHu() && baotingSeat.get(player.getSeat()) == 1) {// ????????????????????? ??????????????????????????????  ?????????????????????
                huBean.setBaoting(false);
            } else {
                huBean.setBaoting(true);
            }
            huBean.initDahuList();
        }
        if (huBean.getDahuPoint() > 0) { // ??????
            player.setDahu(huBean.getDahuList());
            if (zimo) { // ??????
                int point = 0;
                for (YjMjPlayer seatPlayer : seatMap.values()) {
                    if (seatPlayer.getSeat() != player.getSeat()) {
                        int dahuPoint = huBean.getDahuPoint();
                        if (getFanshuLimit() > 0 && dahuPoint > YjMjConstants.fanshuLimit) {// ???????????????
                            dahuPoint = YjMjConstants.fanshuLimit;
                        }
                        point += dahuPoint;
                        seatPlayer.changeLostPoint(-huBean.getDahuPoint());
                    }
                }
                player.changeLostPoint(point);
            } else {
                player.changeLostPoint(huBean.getDahuPoint());
                seatMap.get(disCardSeat).changeLostPoint(-huBean.getDahuPoint());
            }
        } else { // ????????????
            if (zimo) {
                int point = 0;
                for (YjMjPlayer seatPlayer : seatMap.values()) {
                    if (seatPlayer.getSeat() != player.getSeat()) {
                        point += YjMjHu.xiaoMengZiBasePoint;
                        seatPlayer.changeLostPoint(-YjMjHu.xiaoMengZiBasePoint);
                    }
                }
                player.changeLostPoint(point);
            }
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, huBean.getShowMajiangs());
        if (fromSeat > 0) {
            builder.setFromSeat(fromSeat);
        }
        builder.addAllHuArray(player.getDahu());
        if (zimo) {
            builder.setZimo(1);
        }
        for (YjMjPlayer seat : seatMap.values()) {// ???????????????
            seat.writeSocket(builder.build());
        }
        logActionHu(player, majiangs);
        addHuList(player.getSeat(), disMajiang == null ? 0 : disMajiang.getId());// ??????????????????
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_");
        if (isCalcOver()) { // ?????????????????? ???????????????????????????
            calcOver();
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param majiang
     * @return
     */
    private YjMjPlayer getPlayerByHasMajiang(YjMj majiang) {
        for (YjMjPlayer player : seatMap.values()) {
            if (player.getmGang() != null && player.getmGang().contains(majiang)) {
                return player;
            }
        }
        for (YjMjPlayer player : seatMap.values()) {
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
            YjMjPlayer moGangPlayer = null;
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
            YjMjPlayer disYJMajiangPlayer = seatMap.get(disCardSeat);
            for (int huseat : huActionList) {
                if (huConfirmMap.containsKey(huseat)) {
                    if (nowDisCardIds == null || nowDisCardIds.isEmpty() ? true : false) {// ??????
                        removeActionSeat(huseat);
                    }
                    continue;
                }
                PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
                YjMjPlayer seatPlayer = seatMap.get(huseat);
                buildPlayRes(disBuilder, disYJMajiangPlayer, 0, null);
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
     * ??????
     *
     * @param player
     * @param mjs    ????????? ??? ?????????
     * @param action
     */
    private void chiPengGang(YjMjPlayer player, List<YjMj> mjs, int action) {
        logAction(player, action, mjs, null);

        if (mjs == null || mjs.size() == 0) {
            LogUtil.msgLog.info("YjMj|chiPengGang|error|" + getId() + "|" + player.getUserId() + "|" + player.getUserId() + "|" + action + "|" + player.getHandMajiang());
            return;
        }

        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        List<Integer> huList = getHuSeatByActionMap();
        huList.remove((Object) player.getSeat());//????????????action
        if (!huList.isEmpty()) {// ?????????????????????  ??????????????????????????????
            return;
        }
        if (!checkAction(player, action)) {
            return;
        }

        List<YjMj> handMajiang = new ArrayList<>(player.getHandMajiang());
        YjMj disMajiang = null;
        if (isHasGangAction()) {// ???????????????
            List<Integer> majiangIds = MajiangHelper.toMajiangIds(mjs);
            for (int majiangId : gangSeatMap.keySet()) {
                if (majiangIds.contains(majiangId)) {
                    disMajiang = YjMj.getMajang(majiangId);
                    handMajiang.add(disMajiang);
                    if (mjs.size() > 1) {
                        mjs.remove(disMajiang);
                    }
                    break;
                }
            }
            if (disMajiang == null) {// ?????????????????????
                return;
            }
        } else {
            if (!nowDisCardIds.isEmpty()) {
                disMajiang = nowDisCardIds.get(0);
            }
        }
        int sameCount = 0;
        if (mjs.size() > 0) {
            sameCount = MajiangHelper.getMajiangCount(mjs, mjs.get(0).getVal());
        }
        if (action == YjMjDisAction.action_jiegang) { // ??????
            mjs = MajiangHelper.getMajiangList(handMajiang, mjs.get(0).getVal());
            sameCount = mjs.size();
        } else if (action == YjMjDisAction.action_minggang || action == YjMjDisAction.action_angang) {// ???????????? ????????????????????????????????????
            mjs = MajiangHelper.getMajiangList(handMajiang, mjs.get(0).getVal());
            sameCount = mjs.size();
            if (sameCount == 4) {// ???4????????????????????????
                action = YjMjDisAction.action_angang;
            }
        }
        // ??? ?????????
        boolean hasQGangHu = false;
        if (action == YjMjDisAction.action_peng) {
            int curSize = MajiangHelper.getMajiangList(handMajiang, mjs.get(0).getVal()).size();
            if (curSize == 3) {// ??????????????????????????? ?????????????????????
                player.removeUncheckmGang(mjs.get(0).getVal());
            }
            sameCount = 2;
            boolean can = canPeng(player, mjs, sameCount, disMajiang);
            if (!can) {
                return;
            }
        } else if (action == YjMjDisAction.action_angang) {
            boolean can = canAnGang(player, mjs, sameCount);
            if (!can) {
                return;
            }
            player.updateGangInfo(0, 1);
        } else if (action == YjMjDisAction.action_minggang) {
            boolean can = canMingGang(player, handMajiang, mjs, sameCount, disMajiang);
            if (!can) {
                return;
            }
            //???????????????
            if (!player.getUncheckmGangs().contains(mjs.get(0).getVal())) {
            	return;
            }
            
            if (sameCount == 1 && canGangHu()) {// ???????????????????????????????????????????????????
                if (checkQGangHu(player, mjs, action)) {
                    hasQGangHu = true;
                    LogUtil.monitor_i("???????????????????????????");
                }
            }
            if (!hasQGangHu)
                player.updateGangInfo(1, 1);
        } else if (action == YjMjDisAction.action_jiegang) {
            boolean can = canMingGang(player, handMajiang, mjs, sameCount, disMajiang);
            if (!can) {
                return;
            }
            if (disCardSeat != player.getSeat()) {
                YjMjPlayer disPlayer = this.seatMap.get(disCardSeat);
                disPlayer.updateGangInfo(3, 1);
                player.updateGangInfo(2, 1);
            }
        } else {
            return;
        }
        calcPoint(player, action, sameCount, mjs);
        boolean disMajiangMove = false;
        if (disMajiang != null) {// ????????????
            if ((action != YjMjDisAction.action_minggang && action != YjMjDisAction.action_angang && action != YjMjDisAction.action_jiegang)
                    || (action == YjMjDisAction.action_minggang && sameCount != 1) || (action == YjMjDisAction.action_jiegang && sameCount != 1 && sameCount != 4)) {
                disMajiangMove = true;
            }
        }
        if (disMajiangMove) {
            if (action == YjMjDisAction.action_chi) {
                // ?????????????????????
                mjs.add(1, disMajiang);
            } else {
                mjs.add(disMajiang);
            }
            builder.setFromSeat(disCardSeat);
            List<YjMj> disMajiangs = new ArrayList<>();
            disMajiangs.add(disMajiang);
            seatMap.get(disCardSeat).removeOutPais(disMajiangs, action);
        }
        chiPengGang(builder, player, mjs, action, hasQGangHu);
    }

    private void chiPengGang(PlayMajiangRes.Builder builder, YjMjPlayer player, List<YjMj> majiangs, int action, boolean hasQGangHu) {
        if (action == YjMjDisAction.action_peng) {// ????????????????????????action
            int curSize = MajiangHelper.getMajiangList(player.getHandMajiang(), majiangs.get(0).getVal()).size();
            if (curSize == 3) {
                action = YjMjDisAction.action_gangPeng;
            }
        }
        player.addOutPais(majiangs, action, disCardSeat);
        buildPlayRes(builder, player, action, majiangs);
        removeActionSeat(player.getSeat());
        clearGangActionMap();
        if (!hasQGangHu) {
            clearActionSeatMap();
        }
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
        // ??????????????????
        setNowYjDisCardSeat(player.getSeat(), false);
        if (action == YjMjDisAction.action_peng || action == YjMjDisAction.action_gangPeng) {// ????????????
            List<Integer> arr = player.checkMo(null, false, false);
            if (!arr.isEmpty()) {
                addActionSeat(player.getSeat(), arr);
            }
        }
        for (YjMjPlayer seatPlayer : seatMap.values()) {
            // ????????????
            PlayMajiangRes.Builder copy = builder.clone();
            if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                copy.addAllSelfAct(actionSeatMap.get(seatPlayer.getSeat()));
            }
            seatPlayer.writeSocket(copy.build());
        }
        if (action == YjMjDisAction.action_chi || action == YjMjDisAction.action_peng) {
            sendTingInfo(player);
        }
        player.setPassMajiangVal(0);// ????????????
        if (action == YjMjDisAction.action_minggang || action == YjMjDisAction.action_angang || action == YjMjDisAction.action_jiegang) {
            if (!hasQGangHu) {// ???????????????????????????????????????
                if (getLeftMajiangCount() > maxPlayerCount)// ?????????????????? ???????????????
                    moMajiang(player, true);
                else {// ???????????? ???????????????  ?????????????????????
                    Map<Integer, Integer> handMap = MajiangHelper.toMajiangValMap(player.getHandMajiang());
                    if (handMap.containsValue(4)) {// ?????????????????? ???????????????
                        List<Integer> list = new ArrayList<>();
                        int[] arr = new int[7];
                        arr[3] = 1;
                        for (int val : arr) {
                            list.add(val);
                        }
                        addActionSeat(player.getSeat(), list);
                        logAction(player, action, majiangs, list);
                        PlayMajiangRes.Builder anGangBuilder = PlayMajiangRes.newBuilder();
                        anGangBuilder.setFromSeat(disCardSeat);
                        buildPlayRes(anGangBuilder, player, YjMjDisAction.action_haidi_angang, null);
                        anGangBuilder.addAllSelfAct(actionSeatMap.get(player.getSeat()));
                        player.writeSocket(anGangBuilder.build());
                    } else {
                        if (getLeftMajiangCount() < maxPlayerCount) {
                            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + YjMjDisAction.action_haodilaoPass + "_");
                            PlayMajiangRes.Builder haidilaoGuoBuilder = PlayMajiangRes.newBuilder();
                            buildPlayRes(haidilaoGuoBuilder, player, YjMjDisAction.action_haodilaoPass, null);
                            for (YjMjPlayer seat : seatMap.values()) {
                                seat.writeSocket(haidilaoGuoBuilder.build());
                            }
                        }
                        setNowYjDisCardSeat(player.getSeat(), true);
                        YjMjPlayer next = seatMap.get(nowDisCardSeat);
                        moMajiang(next, false);// ??????????????????????????????
                    }
                }
            }
        }
        setDisEventAction(action);
        robotDealAction();
    }

    /**
     * ???????????????
     *
     * @param gangPlayer
     * @param majiangs
     * @param action
     * @return
     */
    private boolean checkQGangHu(YjMjPlayer gangPlayer, List<YjMj> majiangs, int action) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        Map<Integer, List<Integer>> huMap = new HashMap<>();
        for (YjMjPlayer player : seatMap.values()) {
            if (player.getUserId() == gangPlayer.getUserId()) {
                continue;
            }
            // ????????????
            List<Integer> actList = player.checkDisMj(majiangs.get(0), true);
            if (!actList.isEmpty() && actList.get(YjMjConstants.ACTION_INDEX_HU) == 1) {
                addActionSeat(player.getSeat(), actList);
                huMap.put(player.getSeat(), actList);
                logQiangGangHu(player, majiangs, actList);
            }
        }
        if (huMap.isEmpty()) {
            return false;
        }
        // ????????????
        // ???????????????????????? ???????????? ????????????????????????????????????????????????
        setDisEventAction(action);
        setMoGang(majiangs.get(0), new ArrayList<>(huMap.keySet()));
        buildPlayRes(builder, gangPlayer, action, majiangs);
        for (Entry<Integer, List<Integer>> entry : huMap.entrySet()) {// ????????????????????????
            PlayMajiangRes.Builder copy = builder.clone();
            YjMjPlayer seatPlayer = seatMap.get(entry.getKey());
            copy.addAllSelfAct(entry.getValue());
            seatPlayer.writeSocket(copy.build());
            LogUtil.monitor_i("????????????????????????:" + seatPlayer.getName() + "?????????:" + entry.getKey());
        }
        return true;

    }

    public void checkSendGangRes(Player player) {
        if (isHasGangAction(player.getSeat()) && actionSeatMap.containsKey(player.getSeat())) {
            YjMjPlayer disPlayer = seatMap.get(disCardSeat);
            GangMoMajiangRes.Builder gangbuilder = GangMoMajiangRes.newBuilder();
            gangbuilder.setGangId(gangMajiang.getId());
            gangbuilder.setUserId(disPlayer.getUserId() + "");
            gangbuilder.setName(disPlayer.getName() + "");
            gangbuilder.setSeat(disPlayer.getSeat());
            gangbuilder.setRemain(leftMajiangs.size());
            gangbuilder.setReconnect(1);

            for (int majiangId : gangSeatMap.keySet()) {
                GangPlayMajiangRes.Builder playBuilder = GangPlayMajiangRes.newBuilder();
                playBuilder.setMajiangId(majiangId);
                Map<Integer, List<Integer>> seatActionList = gangSeatMap.get(majiangId);
                if (seatActionList == null) {
                    continue;
                }
                if (seatActionList.containsKey(player.getSeat())) {
                    playBuilder.addAllSelfAct(seatActionList.get(player.getSeat()));
                }
                gangbuilder.addGangActs(playBuilder);
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
    private void chuPai(YjMjPlayer player, List<YjMj> majiangs, int action) {
        if (majiangs.size() != 1) {
            return;
        }
        if (!player.isAlreadyMoMajiang()) {
            // ???????????????
            LogUtil.errorLog.error("????????????????????????" + player.getUserId());
            return;
        }
        if (!actionSeatMap.isEmpty()) {
            return;
        }
        if (getBaotingSeat().containsKey(player.getSeat())) { // ???????????????????????????????????????
            if (majiangs.get(0).getId() != player.getHandMajiang().get(player.getHandMajiang().size() - 1).getId()) {
                return;
            }
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        clearActionSeatMap();
        clearGangActionMap();
        setNowYjDisCardSeat(player.getSeat(), true);
        recordDisMajiang(majiangs, player);
        player.addOutPais(majiangs, action, 0);

        // ???????????????????????????????????????
        if (player.getSeat() == getLastWinSeat()
                && hadNotMoMj()
                && player.getHandPais().size() >= 13
                && player.isTingPai(0)) {

            int[] arr = new int[YjMjConstants.ACTION_INDEX_SIZE];
            arr[YjMjConstants.ACTION_INDEX_BAOTING] = 1;
            List<Integer> list = new ArrayList<>();
            for (int val : arr) {
                list.add(val);
            }
            addActionSeat(player.getSeat(), list);
            setDisEventAction(action);

            builder.addAllSelfAct(list);
            player.writeSocket(builder.build());
            logAction(player, action, majiangs, list);
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
            return;
        }

        logAction(player, action, majiangs, null);

        // ????????????
        for (YjMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                continue;
            }
            List<Integer> list = seat.checkDisMj(majiangs.get(0), false);
            if (list.contains(1)) {
                addActionSeat(seat.getSeat(), list);
                logChuPaiActList(seat, majiangs.get(0), list);
            }
        }

        setDisEventAction(action);
        sendDisMajiangAction(builder);
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
        checkMo();// ??????????????????
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public boolean hadNotMoMj() {
        return getLeftMajiangCount() == (108 - (maxPlayerCount * 13 + 1 + chouPai));
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

    //???action?????? ?????????????????????????????????
    public List<Integer> getHuSeatByActionMap() {
        List<Integer> huList = new ArrayList<>();
        for (int seat : actionSeatMap.keySet()) {
            List<Integer> actionList = actionSeatMap.get(seat);
            if (actionList.get(YjMjConstants.ACTION_INDEX_HU) == 1) {// ???
                huList.add(seat);
            }
        }
        return huList;
    }

    /**
     * ?????????????????? ?????????????????????
     *
     * @param builder
     */
    private void sendDisMajiangAction(PlayMajiangRes.Builder builder) {
        // ????????????????????? ?????????
        // ??????????????????
        buildPlayRes1(builder);
        List<Integer> huList = getHuSeatByActionMap();
        if (huList.size() > 0) {
            // ?????????,?????????
            for (YjMjPlayer seatPlayer : seatMap.values()) {
                PlayMajiangRes.Builder copy = builder.clone();
                List<Integer> actionList;
                // ???????????????????????????????????????????????????????????????????????????????????????
                if (actionSeatMap.containsKey(seatPlayer.getSeat())) {
                    actionList = actionSeatMap.get(seatPlayer.getSeat());
                } else {// ?????????????????????
                    actionList = new ArrayList<>();
                }
                copy.addAllSelfAct(actionList);
                seatPlayer.writeSocket(copy.build());
            }

        } else {
            // ??????????????????????????????
            for (YjMjPlayer seat : seatMap.values()) {
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

    private void err(YjMjPlayer player, int action, String errMsg) {
        LogUtil.e("play:tableId-->" + id + " playerId-->" + player.getUserId() + " action-->" + action + " err:" + errMsg);
    }

    /**
     * ??????
     *
     * @param player
     * @param majiangs
     * @param action
     */
    public synchronized void playCommand(YjMjPlayer player, List<YjMj> majiangs, int action) {
        // ???????????????
        if (!moGangHuList.isEmpty()) {
            if (!moGangHuList.contains(player.getSeat())) {
                return;
            }
        }

        if (YjMjDisAction.action_hu == action) {
            hu(player, majiangs, action);
            return;
        }

        if (getLeftMajiangCount() == (108 - (maxPlayerCount * 13 + 1 + birdNum)) && lastWinSeat == player.getSeat()) {// ????????????????????????????????????  ?????????????????????????????????????????????
            for (int seat : actionSeatMap.keySet()) {
                if (seat != player.getSeat() && actionSeatMap.containsKey(seat) && actionSeatMap.get(seat).get(6) == 1) {
                    return;
                }
            }
        }

        // ???????????????????????????
        if (!isHasGangAction() && action != YjMjDisAction.action_minggang && action != YjMjDisAction.action_jiegang && action != YjMjDisAction.action_peng)
            if (majiangs == null || !player.getHandMajiang().containsAll(majiangs)) {
                err(player, action, "?????????????????????" + majiangs);
                return;
            }
        changeDisCardRound(1);
        if (action == YjMjDisAction.action_pass) {
            guo(player, majiangs, action);
        } else if (action == YjMjDisAction.action_haodilaoPass) {//????????????????????? ?????????
            moLast4Majiang(player, YjMjDisAction.action_haodilaoPass);
        } else if (action == YjMjDisAction.action_baoting) {// ????????????
            baoTing(player, action);
        } else if (action == YjMjDisAction.action_moMjiang) {
        } else if (action != 0) {
            chiPengGang(player, majiangs, action);
        } else {
            chuPai(player, majiangs, action);
        }

    }

    /**
     * ????????????????????? ???????????????
     *
     * @param player
     * @param action
     */
    public synchronized void moLast4Majiang(YjMjPlayer player, int action) {
        if (!moGangHuList.isEmpty() && moGangHuList.contains(player.getSeat())) {// ?????????????????????????????? ?????????
            List<YjMj> majiangs = new ArrayList<>();
            majiangs.add(moGang);
            passMoHu(player, majiangs, action);
            return;
        }
        List<Integer> actList = removeActionSeat(player.getSeat());
        logAction(player, action, null, actList);
        if (!actionSeatMap.isEmpty()) {
            return;
        }
        if (state != table_state.play) {
            return;
        }
        if (getDisCardRound() <= 0) {
            return;
        }
        if (action == YjMjDisAction.action_haodilaoPass) {
            addPlayLog(disCardRound + "_" + player.getSeat() + "_" + YjMjDisAction.action_haodilaoPass + "_");

            PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
            buildPlayRes(builder, player, action, null);
            for (YjMjPlayer seat : seatMap.values()) {
                seat.writeSocket(builder.build());
            }
            if (getLeftMajiangCount() == 0) {
                calcOver();
                return;
            }
            YjMjPlayer next = seatMap.get(calcNextSeat(player.getSeat()));
            if (next.isAlreadyMoMajiang()) {// ??????????????? ????????????
                return;
            }
            setNowYjDisCardSeat(moLastMajiangSeat, true);
            next = seatMap.get(nowDisCardSeat);
            moMajiang(next, false);
            robotDealAction();
        }
    }

    /**
     * ?????????????????? ??????????????????
     *
     * @param leisurePlayer
     */
    public synchronized boolean leisureBaotingfinish(YjMjPlayer leisurePlayer) {
        for (int actionSeat : actionSeatMap.keySet()) {
            if (actionSeat != lastWinSeat && actionSeatMap.get(actionSeat).get(YjMjConstants.ACTION_INDEX_BAOTING) == 1) {// ???????????????????????????
                return false;
            }
        }
        // ????????????????????????????????????
        Player bankPlayer = seatMap.get(lastWinSeat);
        ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_ask_dismajiang);
        bankPlayer.writeSocket(com.build());
        return true;
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param action
     */
    public synchronized void baoTing(YjMjPlayer player, int action) {
        if (actionSeatMap.isEmpty() || !actionSeatMap.containsKey(player.getSeat()) || actionSeatMap.get(player.getSeat()).get(YjMjConstants.ACTION_INDEX_BAOTING) == 0) {// ?????????????????????
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, null);
        builder.setSeat(player.getSeat());
        for (YjMjPlayer seatPlayer : seatMap.values()) {// ???????????????????????????
            seatPlayer.writeSocket(builder.build());
        }
        removeActionSeat(player.getSeat());
        addBaotingSeat(player.getSeat());// ???????????????????????????
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + YjMjDisAction.action_baoting + "_");
        logAction(player, action, null, null);
        if (player.getSeat() != lastWinSeat) {
            leisureBaotingfinish(player);
            return;
        }
        // ????????????
        PlayMajiangRes.Builder builder2 = PlayMajiangRes.newBuilder();
        buildPlayRes(builder2, player, 0, nowDisCardIds);
        builder2.setSeat(player.getSeat());
        for (YjMjPlayer seat : seatMap.values()) {
            if (seat.getUserId() == player.getUserId()) {
                continue;
            }
            List<Integer> list = seat.checkDisMj(nowDisCardIds.get(0), false);
            if (list.contains(1)) {
                addActionSeat(seat.getSeat(), list);
            }
        }
        setDisEventAction(action);
        sendDisMajiangAction(builder2);
        player.setPassMajiangVal(0);// ????????????
        setNowYjDisCardSeat(player.getSeat(), true);
//		addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(nowDisCardIds));
        checkMo();// ??????????????????
    }

    private void passMoHu(YjMjPlayer player, List<YjMj> majiangs, int action) {
        if (!moGangHuList.contains(player.getSeat())) {
            return;
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        removeActionSeat(player.getSeat());
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));
        if (isCalcOver()) {
            calcOver();
            return;
        }
        player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
        YjMjPlayer moGangPlayer = seatMap.get(moMajiangSeat);
        if (moGangHuList.isEmpty()) {
            moGangPlayer.updateGangInfo(1, 1);// ??????????????????
            majiangs = new ArrayList<>();
            majiangs.add(moGang);
            calcPoint(moGangPlayer, YjMjDisAction.action_minggang, 1, majiangs);
            builder = PlayMajiangRes.newBuilder();
            clearMoGang();// ?????????????????????
            // ????????????????????????
            if (getLeftMajiangCount() >= maxPlayerCount) // ????????????????????????
                moMajiang(moGangPlayer, true);
            else {// ???????????? ?????? ???????????????  ?????????????????????  ?????????????????? ???????????????
                setNowYjDisCardSeat(moGangPlayer.getSeat(), true);
                YjMjPlayer next = seatMap.get(nowDisCardSeat);
                moMajiang(next, true);
            }
            //chiPengGang(builder, moGangPlayer, majiangs, YjMjDisAction.action_minggang);
        }
    }

    /**
     * pass
     *
     * @param player
     * @param majiangs
     * @param action
     */
    private void guo(YjMjPlayer player, List<YjMj> majiangs, int action) {
        if (!actionSeatMap.containsKey(player.getSeat())) {
            return;
        }
        if (!moGangHuList.isEmpty()) {// ???????????????????????????
            passMoHu(player, majiangs, action);
            return;
        }
        if (actionSeatMap.get(player.getSeat()).get(2) == 1) {// ????????????
            Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(player.getPeng());
            for (YjMj handMajiang : player.getHandMajiang()) {// ????????????????????????
                if (pengMap.containsKey(handMajiang.getVal())) {
                    if (player.getUncheckmGangs().contains(handMajiang.getVal())) {
                        player.removeUncheckmGang(handMajiang.getVal());// ??????????????????  ??????????????????
                    }
                }
            }
        }
        if (actionSeatMap.get(player.getSeat()).get(0) == 1) {// ?????????
            if (baotingSeat.containsKey(player.getSeat()) && baotingSeat.get(player.getSeat()) == 0) {// ????????????????????????????????? ?????????????????????
                baotingSeat.put(player.getSeat(), 1);
            }
            if (nowDisCardSeat != player.getSeat()) {
                addHuPassSeat(player.getSeat());
            }
        }
        if (actionSeatMap.get(player.getSeat()).get(1) == 1) {// ?????????
            if (nowDisCardSeat != player.getSeat()) {
                addPengPassSeat(player.getSeat(), nowDisCardIds.get(0).getVal());
            }
        }
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        buildPlayRes(builder, player, action, majiangs);
        builder.setSeat(nowDisCardSeat);
        player.writeSocket(builder.build());
        addPlayLog(disCardRound + "_" + player.getSeat() + "_" + action + "_" + MajiangHelper.toMajiangStrs(majiangs));

        List<Integer> removeActionList = removeActionSeat(player.getSeat());
        logAction(player, action, majiangs, removeActionList);

        if (removeActionList != null && removeActionList.get(YjMjConstants.ACTION_INDEX_BAOTING) == 1) {// ???????????????
            if (player.getSeat() != lastWinSeat) {// ?????????????????????
                leisureBaotingfinish(player);
                return;
            } else {// ????????????????????? ???????????????????????????????????????
                PlayMajiangRes.Builder builder2 = PlayMajiangRes.newBuilder();
                buildPlayRes(builder2, player, YjMjDisAction.action_chupai, nowDisCardIds);
                builder2.setSeat(player.getSeat());
                for (YjMjPlayer seat : seatMap.values()) {
                    if (seat.getUserId() == player.getUserId()) {
                        continue;
                    }
                    List<Integer> list = seat.checkDisMj(nowDisCardIds.get(0), false);
                    if (list.contains(1)) {
                        addActionSeat(seat.getSeat(), list);
                        logChuPaiActList(seat, nowDisCardIds.get(0), list);
                    }
                }
                if (!actionSeatMap.isEmpty()) {
                    sendDisMajiangAction(builder2);
                    return;
                }
            }
        }

        if (isCalcOver()) {
            calcOver();
            return;
        }

        // ??????
        if (removeActionList != null && removeActionList.get(YjMjConstants.ACTION_INDEX_HU) == 1) {
            if (player.isAlreadyMoMajiang()) {
                // ??????
                player.setPassMajiangVal(player.getHandMajiang().get(player.getHandMajiang().size() - 1).getVal());
            } else if (disCardSeat != player.getSeat() && nowDisCardIds.size() == 1) {
                // ??????
                player.setPassMajiangVal(nowDisCardIds.get(0).getVal());
            }
        }

        if (!actionSeatMap.isEmpty()) {
            YjMjPlayer disPlayer = seatMap.get(disCardSeat);
            PlayMajiangRes.Builder disBuilder = PlayMajiangRes.newBuilder();
            buildPlayRes(disBuilder, disPlayer, 0, null);
            for (int seat : actionSeatMap.keySet()) {
                List<Integer> actionList = actionSeatMap.get(seat);
                if (actionList.get(YjMjConstants.ACTION_INDEX_HU) == 1) {// ?????????????????????????????????????????????????????????????????????
                    continue;
                }
                if (isHasGangAction(seat)) {
                    continue;
                }
                PlayMajiangRes.Builder copy = disBuilder.clone();
                copy.addAllSelfAct(actionList);
                YjMjPlayer seatPlayer = seatMap.get(seat);
                seatPlayer.writeSocket(copy.build());
            }
        }
        if (player.isAlreadyMoMajiang()) {
            sendTingInfo(player);
        }
        checkMo();
    }

    private void calcPoint(YjMjPlayer player, int action, int sameCount, List<YjMj> majiangs) {
        if (sameCount == 3) {
            YjMjPlayer disPlayer = seatMap.get(disCardSeat);
            disPlayer.getMyExtend().setMjFengshen(FirstmythConstants.firstmyth_index13, 1);
        }
    }

    private void recordDisMajiang(List<YjMj> majiangs, YjMjPlayer player) {
        setNowDisCardIds(majiangs);
        String disCardStr = "";
        for (YjMj cards : nowDisCardIds) {
            disCardStr += (cards.toString() + ",");
        }
        setDisCardSeat(player.getSeat());
    }

    public List<YjMj> getNowDisCardIds() {
        return nowDisCardIds;
    }

    public void setDisEventAction(int disAction) {
        this.disEventAction = disAction;
        changeExtend();
    }

    public void setNowDisCardIds(List<YjMj> nowDisCardIds) {
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
                YjMjPlayer player = seatMap.get(nowDisCardSeat);
                moMajiang(player, false);
            }
            robotDealAction();
        } else {
            for (int seat : actionSeatMap.keySet()) {
                YjMjPlayer player = seatMap.get(seat);
                if (player != null && player.isRobot()) {
                    // ????????????????????????????????????
                    List<Integer> actionList = actionSeatMap.get(seat);
                    if (actionList == null) {
                        continue;
                    }
                    List<YjMj> list = new ArrayList<>();
                    if (!nowDisCardIds.isEmpty()) {
                        list = YjMjQipaiTool.getVal(player.getHandMajiang(), nowDisCardIds.get(0).getVal());
                    }
                    //0??? 1??? 2?????? 3?????? 4?????? 6??????
                    if (actionList.get(0) == 1) {// ???
                        playCommand(player, new ArrayList<YjMj>(), YjMjDisAction.action_hu);
                    } else if (actionList.get(6) == 1) {
                        baoTing(player, YjMjDisAction.action_baoting);
                    } else if (actionList.get(2) == 1) {
                        playCommand(player, list, YjMjDisAction.action_minggang);
                    } else if (actionList.get(3) == 1) {
                        playCommand(player, list, YjMjDisAction.action_angang);
                    } else if (actionList.get(4) == 1) {
                        playCommand(player, list, YjMjDisAction.action_jiegang);
                    } else if (actionList.get(1) == 1) {
                        playCommand(player, list, YjMjDisAction.action_peng);
                    } else {
                        System.out.println("---------->" + JacksonUtil.writeValueAsString(actionList));
                    }
                }
            }

        }
    }

    @Override
    protected void robotDealAction() {
        if (isTest()) {
            int nextseat = getNextActionSeat();
            YjMjPlayer next = seatMap.get(nextseat);
            if (next != null && next.isRobot()) {
                List<Integer> actionList = actionSeatMap.get(next.getSeat());
                if (actionList != null) {// 0??? 1??? 2?????? 3?????? 4??????
                    List<YjMj> list = null;
                    if (actionList.get(0) == 1) {// ???
                        playCommand(next, new ArrayList<YjMj>(), YjMjDisAction.action_hu);
                    } else if (actionList.get(6) == 1) {// 2??????
                        baoTing(next, YjMjDisAction.action_baoting);
                    } else if (actionList.get(2) == 1) {// 2??????
                        Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(next.getPeng());
                        for (YjMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {// ?????????
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, YjMjDisAction.action_minggang);
                                break;
                            }
                        }
                    } else if (actionList.get(3) == 1) {// 3??????
                        Map<Integer, Integer> handMap = MajiangHelper.toMajiangValMap(next.getHandMajiang());
                        for (Entry<Integer, Integer> entry : handMap.entrySet()) {
                            if (entry.getValue() == 4) {// ????????????
                                list = MajiangHelper.getMajiangList(next.getHandMajiang(), entry.getKey());
                            }
                        }
                        playCommand(next, list, YjMjDisAction.action_angang);
                    } else if (actionList.get(4) == 1) {// 4??????
                        Map<Integer, Integer> pengMap = MajiangHelper.toMajiangValMap(next.getPeng());
                        for (YjMj handMajiang : next.getHandMajiang()) {
                            if (pengMap.containsKey(handMajiang.getVal())) {
                                list = new ArrayList<>();
                                list.add(handMajiang);
                                playCommand(next, list, YjMjDisAction.action_jiegang);
                                break;
                            }
                        }
                    } else if (actionList.get(1) == 1) {
                        playCommand(next, list, YjMjDisAction.action_peng);
                    } else {
                        System.out.println("!!!!!!!!!!" + JacksonUtil.writeValueAsString(actionList));
                    }
                } else {
                    if (getLeftMajiangCount() >= maxPlayerCount) {
                        int maJiangId = RobotAI.getInstance().outPaiHandle(0, next.getHandPais(), new ArrayList<Integer>());
                        List<YjMj> majiangList = MajiangHelper.toMajiang(Arrays.asList(maJiangId));
                        playCommand(next, majiangList, 0);
                    } else {// ?????????????????? ????????? ?????????
                        moLast4Majiang(next, YjMjDisAction.action_haodilaoPass);
                    }
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
        setNowYjDisCardSeat(lastWinSeat, false);
        setMoMajiangSeat(lastWinSeat);
        List<List<YjMj>> list = faPai();
        int i = 1;
        for (YjMjPlayer player : playerMap.values()) {
            player.changeState(player_state.play);
            if (player.getSeat() == lastWinSeat) {
                player.dealHandPais(list.get(0));
                continue;
            }
            player.dealHandPais(list.get(i));
            i++;
        }
        List<YjMj> lefts = list.get(getMaxPlayerCount());

        // ?????????
//        List<YjMj> birds = new ArrayList<>();
//        for (int j = 0; j < birdNum; j++) {
//            birds.add(lefts.get(0));
//            lefts.remove(0);
//        }
//        setBirdPaiList(birds);

        // ??????
        int leftSize = lefts.size();
        if (chouPai > 0 && leftSize > chouPai) {
            List<YjMj> chuPaiList = lefts.subList(leftSize - chouPai, leftSize);
            chouCards = YjMjTool.toIds(chuPaiList);
            lefts = lefts.subList(0, lefts.size() - chouPai);

            StringBuilder sb = new StringBuilder("YjMj");
            sb.append("|").append(getId());
            sb.append("|").append(getPlayBureau());
            sb.append("|").append("chouPai");
            sb.append("|").append(chuPaiList);
            LogUtil.msgLog.info(sb.toString());
        }
        // ??????????????????
        setLeftMajiangs(lefts);

    }

    /**
     * ??????
     *
     * @return
     */
    private List<List<YjMj>> faPai() {
        List<Integer> copy = new ArrayList<>(YjMjConstants.yuanjiang_mjList);
        List<List<YjMj>> list;
        // ??????????????????  ???????????????
        if (zp != null) {
            list = YjMjTool.fapai(copy, maxPlayerCount, zp);
        } else {
            list = YjMjTool.fapai(copy, maxPlayerCount);
        }
        int checkTime = 0;
        while (checkTime < 10) { // ????????????10???
            boolean isForbidHu = false;
            Iterator<List<YjMj>> iterator = list.iterator();
            while (iterator.hasNext()) {
                List<YjMj> next = iterator.next();
                if (isLargeForbidHu(next)) {
                    isForbidHu = true;
                    break;
                }
            }
            if (isForbidHu == true) {// ????????????
                copy = new ArrayList<>(YjMjConstants.yuanjiang_mjList);
                if (zp != null) {
                    list = YjMjTool.fapai(copy, maxPlayerCount, zp);
                } else {
                    list = YjMjTool.fapai(copy, maxPlayerCount);
                }
            } else
                break;
            checkTime++;
        }
        return list;
    }

    /**
     * ????????????  ???????????????????????????
     *
     * @param yjMajiangs
     * @return
     */
    private boolean isLargeForbidHu(List<YjMj> yjMajiangs) {
        List<YjMj> gang = new ArrayList<>();
        List<YjMj> aGangs = new ArrayList<>();
        List<YjMj> peng = new ArrayList<>();
        boolean isMenQing = canMenQing();
        boolean isMaMaHu = canMaMaHu();
        List<YjMj> copy = new ArrayList<>(yjMajiangs);
        if (copy.size() == 14) {// ?????????????????????????????????
            YjMjHu hu = YjMjTool.isHuYuanjiang(copy, gang, aGangs, peng, true, false, isMenQing, isMaMaHu);
            if (hu.isHu())
                return true;
            else {// ????????????
                for (int index = 0; index < 14; index++) {// ??????????????????14???
                    List<YjMj> tempMjs = new ArrayList<>(copy);
                    tempMjs.remove(index);
                    if (isLargeForbidHu(tempMjs))// ??????????????????????????????
                        return true;
                }
                return false;
            }
        }
        if (copy.size() == 13) {
            if (copy.size() % 3 != 2) {
                copy.add(YjMj.getMajang(201));// ???????????????
            }
            YjMjHu hu = YjMjTool.isHuYuanjiang(copy, gang, aGangs, peng, false, false, isMenQing, isMaMaHu);
            hu.initDahuList();
            if (hu.getDahuList().size() >= 3 || hu.isShuang7xiaodui() || hu.isSan7xiaodui()) {// ?????????7?????? ??? ?????????7?????????????????????
                return true;
            } else
                return false;
        }
        return false;
    }

    /**
     * ???????????????????????????
     *
     * @param leftMajiangs
     */
    public void setLeftMajiangs(List<YjMj> leftMajiangs) {
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
    public YjMj getLeftMajiang() {
        if (this.leftMajiangs.size() > 0) {
            YjMj majiang = this.leftMajiangs.remove(0);
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
            // 0??? 1??? 2?????? 3?????? 4??????
            for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                if (seat == 0) {
                    seat = entry.getKey();
                }
                if (entry.getValue().get(0) == 1) {// ???
                    return entry.getKey();
                }
                if (entry.getValue().get(2) == 1) {// ??????
                    return entry.getKey();
                }
                if (entry.getValue().get(3) == 1) {// ??????
                    return entry.getKey();
                }
                if (entry.getValue().get(4) == 1) {// ??????
                    return entry.getKey();
                }
                if (entry.getValue().get(1) == 1) {// ???
                    return entry.getKey();
                }
            }
            return seat;
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

    @SuppressWarnings("unchecked")
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
        res.addExt(birdNum);                // 0
        res.addExt(getConifg(0));     // 1
        res.addExt(isCalcBanker);           // 2
        res.addExt(calcBird);               // 3
        // 4?????? 5???????????? 6??????????????? 7?????????????????????????????? 8???????????????
        res.addExt(payType);                // 4??????
        res.addExt(fanshuLimit);            // 5????????????
        res.addExt(hasMenQing);             // 6???????????????
        res.addExt(menQingJiangJiangHu);    // 7??????????????????????????????
        res.addExt(yizhiqiao);              // 8???????????????
        res.addExt(kaqiao);                 // 9??????
        res.addExt(jiaBei);                 // 10??????
        res.addExt(jiaBeiFen);              // 11?????????
        res.addExt(jiaBeiShu);              // 12?????????
        res.addExt(autoPlayGlob);           // 13??????
        res.addExt(autoTime);               // 14????????????
        res.addExt(below);                  // 15 ?????? below ??? belowAdd ???
        res.addExt(belowAdd);               // 16

        res.setRenshu(maxPlayerCount);
        if (leftMajiangs != null) {
            res.setRemain(leftMajiangs.size());
        } else {
            res.setRemain(0);
        }
        List<PlayerInTableRes> players = new ArrayList<>();
        for (YjMjPlayer player : playerMap.values()) {
            PlayerInTableRes.Builder playerRes = player.buildPlayInTableInfo(isrecover);
            if (player.getUserId() == userId) {
                playerRes.addAllHandCardIds(player.getHandPais());
            }
            if (player.getSeat() == disCardSeat && nowDisCardIds != null) {
                playerRes.addAllOutCardIds(MajiangHelper.toMajiangIds(nowDisCardIds));
            }
            playerRes.addRecover(player.getSeat() == lastWinSeat ? 1 : 0);
            if (!isHasGangAction(player.getSeat()) && actionSeatMap.containsKey(player.getSeat()) && !huConfirmMap.containsKey(player.getSeat())) {
                playerRes.addAllRecover(actionSeatMap.get(player.getSeat()));
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

    public void setMaxPlayerCount(int playerCount) {
        this.maxPlayerCount = playerCount;
        changeExtend();
    }

    @SuppressWarnings("unchecked")
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
        setLeftMajiangs(null);
        setNowDisCardIds(null);
        clearMoGang();
        clearGangDisMajiangs();
        setMoLastMajiangSeat(0);
        setDisEventAction(0);
        setLastMajiang(null);
        clearBaotingSeat();
        clearHuPass();
        clearPengPass();
        birdPaiList.clear();
        readyTime = 0;
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, Object... objects) throws Exception {
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

    public void addGangActionSeat(int majiang, int seat, List<Integer> actionList) {
        Map<Integer, List<Integer>> actionMap;
        if (gangSeatMap.containsKey(majiang)) {
            actionMap = gangSeatMap.get(majiang);
        } else {
            actionMap = new HashMap<>();
            gangSeatMap.put(majiang, actionMap);
        }
        if (!actionList.isEmpty()) {
            actionMap.put(seat, actionList);

        }
        saveActionSeatMap();
    }

    public void clearGangActionMap() {
        if (!gangSeatMap.isEmpty()) {
            gangSeatMap.clear();
            saveActionSeatMap();
        }
    }

    public void coverAddActionSeat(int seat, List<Integer> actionlist) {
        actionSeatMap.put(seat, actionlist);
        addPlayLog(disCardRound + "_" + seat + "_" + YjMjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist));
        saveActionSeatMap();
    }

    public void addActionSeat(int seat, List<Integer> actionlist) {
        if (actionSeatMap.containsKey(seat)) {
            List<Integer> a = actionSeatMap.get(seat);
            DataMapUtil.appendList(a, actionlist);
            addPlayLog(disCardRound + "_" + seat + "_" + YjMjDisAction.action_hasAction + "_" + StringUtil.implode(a));
        } else {
            actionSeatMap.put(seat, actionlist);
            addPlayLog(disCardRound + "_" + seat + "_" + YjMjDisAction.action_hasAction + "_" + StringUtil.implode(actionlist));
        }
        saveActionSeatMap();
    }

    public void clearActionSeatMap() {
        if (!actionSeatMap.isEmpty()) {
            actionSeatMap.clear();
            saveActionSeatMap();
        }
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
            nowDisCardIds = MajiangHelper.toMajiang(StringUtil.explodeToIntList(info.getNowDisCardIds()));
        }

        if (!StringUtils.isBlank(info.getLeftPais())) {
            leftMajiangs = MajiangHelper.toMajiang(StringUtil.explodeToIntList(info.getLeftPais()));
        }

    }

    /**
     * ????????????
     *
     * @param player
     * @param majiangs
     * @return
     */
    @SuppressWarnings("unused")
    private boolean canChi(YjMjPlayer player, List<YjMj> handMajiang, List<YjMj> majiangs, YjMj disMajiang) {
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

        if (disMajiang == null) {
            return false;
        }

        if (!handMajiang.containsAll(majiangs)) {
            return false;
        }

        List<YjMj> chi = YjMjTool.checkChi(majiangs, disMajiang);
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
    private boolean canPeng(YjMjPlayer player, List<YjMj> majiangs, int sameCount, YjMj disMajiang) {
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
    private boolean canAnGang(YjMjPlayer player, List<YjMj> majiangs, int sameCount) {
        if (sameCount != 4) {
            return false;
        }
        if (player.getSeat() != getNextDisCardSeat()) {
            return false;
        }
        return true;
    }

    /**
     * ????????????????????????????????? ????????????????????????????????????????????????????????????
     * ???????????????????????????
     *
     * @param player
     * @param action
     * @return
     */
    public boolean checkAction(YjMjPlayer player, int action) {
        List<Integer> stopActionList = YjMjDisAction.findPriorityAction(action);
        for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
            if (player.getSeat() != entry.getKey()) {// ??????
                boolean can = YjMjDisAction.canDisMajiang(stopActionList, entry.getValue());
                if (!can) {
                    return false;
                }
                List<Integer> disActionList = YjMjDisAction.parseToDisActionList(entry.getValue());
                if (disActionList.contains(action)) {// ??????????????????????????? ????????????????????????
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
    private boolean canMingGang(YjMjPlayer player, List<YjMj> handMajiang, List<YjMj> majiangs, int sameCount, YjMj disMajiang) {
        List<Integer> pengList = MajiangHelper.toMajiangVals(player.getPeng());
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

    public void setMoMajiangSeat(int moMajiangSeat) {
        this.moMajiangSeat = moMajiangSeat;
        changeExtend();
    }

    public void setLastMajiang(YjMj lastMajiang) {
        this.lastMajiang = lastMajiang;
        changeExtend();
    }

    public YjMj getLastMajiang() {
        return this.lastMajiang;
    }

    public void setMoLastMajiangSeat(int moLastMajiangSeat) {
        this.moLastMajiangSeat = moLastMajiangSeat;
        changeExtend();
    }

    public void setGangMajiang(YjMj gangMajiang) {
        this.gangMajiang = gangMajiang;
        changeExtend();
    }

    public void addBaotingSeat(int seat) {
        this.baotingSeat.put(seat, 0);
        changeExtend();
    }

    public void clearBaotingSeat() {
        this.baotingSeat.clear();
        changeExtend();
    }

    public Map<Integer, Integer> getBaotingSeat() {
        return baotingSeat;
    }

    /**
     * ?????????????????????
     *
     * @param moGang           ?????????
     * @param moGangHuSeatList ????????????????????????list
     */
    public void setMoGang(YjMj moGang, List<Integer> moGangHuSeatList) {
        this.moGang = moGang;
        this.moGangHuList = moGangHuSeatList;
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

    public void setGangDisMajiangs(List<YjMj> gangDisMajiangs) {
        this.gangDisMajiangs = gangDisMajiangs;
        changeExtend();
    }

    /**
     * ?????????????????????
     */
    public void clearGangDisMajiangs() {
        this.gangMajiang = null;
        this.gangDisMajiangs.clear();
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

    public List<Integer> getMoGangHuSeats() {
        return moGangHuList;
    }

    public int getMoMajiangSeat() {
        return moMajiangSeat;
    }

    @Override
    protected String buildNowAction() {
        JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putString(1, DataMapUtil.explodeListMap(actionSeatMap));
        wrapper.putString(2, DataMapUtil.explodeListMapMap(gangSeatMap));
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
     * ???????????????
     *
     * @return
     */
    public boolean canGangHu() {
//		if (getConifg(0) == 1) {
//			return true;
//		}
        return true;
    }

    public YjClosingInfoRes.Builder sendAccountsMsg(boolean over, boolean selfMo, List<Integer> winList, int[] prickBirdMajiangIds, int[] seatBirds, Map<Integer, Integer> seatBridMap, boolean isBreak, int bankerSeat, int fangPaoSeat, int[] jiePaoSeat) {

        if (isBreak) {
            // ?????????????????????
            calcExtraPoint(winList);// ??????????????????????????????
            for (YjMjPlayer player : seatMap.values()) {//??????????????????point
                player.changePoint(player.getLostPoint());
                logHuPoint(player);
            }
        }

        //????????????????????????
        if (over && jiaBei == 1) {
            int jiaBeiPoint = 0;
            int loserCount = 0;
            for (YjMjPlayer player : seatMap.values()) {
                if (player.getTotalPoint() > 0 && player.getTotalPoint() < jiaBeiFen) {
                    jiaBeiPoint += player.getTotalPoint() * (jiaBeiShu - 1);
                    player.setTotalPoint(player.getTotalPoint() * jiaBeiShu);
                } else if (player.getTotalPoint() < 0) {
                    loserCount++;
                }
            }
            if (jiaBeiPoint > 0) {
                for (YjMjPlayer player : seatMap.values()) {
                    if (player.getTotalPoint() < 0) {
                        player.setTotalPoint(player.getTotalPoint() - (jiaBeiPoint / loserCount));
                    }
                }
            }
        }

        //???????????????below???+belowAdd???
        if (over && belowAdd > 0 && playerMap.size() == 2) {
            for (YjMjPlayer player : seatMap.values()) {
                int totalPoint = player.getTotalPoint();
                if (totalPoint > -below && totalPoint < 0) {
                    player.setTotalPoint(player.getTotalPoint() - belowAdd);
                } else if (totalPoint < below && totalPoint > 0) {
                    player.setTotalPoint(player.getTotalPoint() + belowAdd);
                }
            }
        }

        List<YjClosingPlayerInfoRes.Builder> builderList = new ArrayList<>();
        List<YjClosingPlayerInfoRes> list = new ArrayList<>();
        for (YjMjPlayer player : seatMap.values()) {
            YjClosingPlayerInfoRes.Builder build = null;
            if (over) {
                build = player.bulidTotalClosingPlayerInfoRes();
            } else {
                build = player.bulidOneClosingPlayerInfoRes();
            }
            if (seatBridMap != null && seatBridMap.containsKey(player.getSeat())) {
                build.addActionCounts(seatBridMap.get(player.getSeat()));
            } else {
                build.addActionCounts(0);
            }
            if (winList != null && winList.contains(player.getSeat())) {
                YjMj huMajiang = null;
                if (isHasGangAction()) {
                    huMajiang = getGangHuMajiang(player.getSeat());
                }
                if (!selfMo) {
                    // ????????????
                    if (huMajiang == null) {
                        huMajiang = nowDisCardIds.get(0);
                    }
                    if (!build.getCardsList().contains(huMajiang.getId())) {
                        build.addCards(huMajiang.getId());
                    }
                    build.setIsHu(huMajiang.getId());
                } else {
                    if (huMajiang == null) {
                        huMajiang = player.getLastMoMajiang();
                    }
                    if (!build.getCardsList().contains(huMajiang.getId())) {
                        build.addCards(huMajiang.getId());
                    }
                    build.setIsHu(huMajiang.getId());
                }
            }
            if (winList != null && winList.contains(player.getSeat())) {
                // ?????????????????????????????????????????????
                builderList.add(0, build);
            } else {
                builderList.add(build);
            }

            //?????????
            if (isCreditTable()) {
                player.setWinLoseCredit(player.getTotalPoint() * creditDifen);
            }
        }

        //???????????????
        if (isCreditTable()) {
            //??????????????????
            calcNegativeCredit();
            long dyjCredit = 0;
            for (YjMjPlayer player : seatMap.values()) {
                if (player.getWinLoseCredit() > dyjCredit) {
                    dyjCredit = player.getWinLoseCredit();
                }
            }
            for (YjClosingPlayerInfoRes.Builder builder : builderList) {
                YjMjPlayer player = seatMap.get(builder.getSeat());
                calcCommissionCredit(player, dyjCredit);
                builder.setWinLoseCredit(player.getWinLoseCredit());
                builder.setCommissionCredit(player.getCommissionCredit());
            }
        } else if (isGroupTableGoldRoom()) {
            // -----------??????????????????---------------------------------
            for (YjMjPlayer player : seatMap.values()) {
                player.setWinGold(player.getTotalPoint() * gtgDifen);
            }
            calcGroupTableGoldRoomWinLimit();
            for (YjClosingPlayerInfoRes.Builder builder : builderList) {
                YjMjPlayer player = seatMap.get(builder.getSeat());
                builder.setWinLoseCredit(player.getWinGold());
            }
        }

        for (YjClosingPlayerInfoRes.Builder builder : builderList) {
            list.add(builder.build());
        }

        YjClosingInfoRes.Builder res = YjClosingInfoRes.newBuilder();
        res.addAllClosingPlayers(list);
        res.setIsBreak(isBreak ? 1 : 0);
        res.setWanfa(getWanFa());
        res.setGroupLogId((int) groupPaylogId);
        res.addAllExt(buildAccountsExt(bankerSeat, over));
        res.addAllLeftCards(MajiangHelper.toMajiangIds(leftMajiangs));
        res.setFangPaoSeat(fangPaoSeat);
        res.addCreditConfig(creditMode);                         //0
        res.addCreditConfig(creditJoinLimit);                    //1
        res.addCreditConfig(creditDissLimit);                    //2
        res.addCreditConfig(creditDifen);                        //3
        res.addCreditConfig(creditCommission);                   //4
        res.addCreditConfig(creditCommissionMode1);              //5
        res.addCreditConfig(creditCommissionMode2);              //6
        res.addCreditConfig(creditCommissionLimit);              //7
        res.addAllIntParams(intParams);
        res.addAllChouCards(chouCards);
        if (jiePaoSeat != null)
            res.addAllJiePaoSeat(DataMapUtil.toList(jiePaoSeat));
        if (seatBirds != null) {
            res.addAllBirdSeat(DataMapUtil.toList(seatBirds));
        }
        if (birdPaiList != null) {
            res.addAllBird(YjMjQipaiTool.toMajiangIds(birdPaiList));
        }
        for (YjMjPlayer player : seatMap.values()) {
            player.writeSocket(res.build());
        }
        return res;

    }

    /**
     * ?????????????????????
     *
     * @return
     */
    public YjMj getGangHuMajiang(int seat) {
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
        return YjMj.getMajang(majiangId);

    }

    /**
     * ??????ID ??????ID ??????????????????  ??????  ?????????????????? ????????? ???????????? ??????????????????  ???????????????  ????????? ???????????? ??????????????? ?????????????????????????????? ???????????????
     *
     * @param bankerSeat
     * @return
     */
    public List<String> buildAccountsExt(int bankerSeat, boolean isOver) {
        List<String> ext = new ArrayList<>();
        ext.add(id + "");                               // 0??????ID
        ext.add(masterId + "");                         // 1??????ID
        ext.add(TimeUtil.formatTime(TimeUtil.now()));   // 2??????????????????
        ext.add(playType + "");                         // 3??????
        ext.add(getConifg(0) + "");               // 4??????????????????
        ext.add(birdNum + "");                          // 5?????????
        ext.add(isCalcBanker + "");                     // 6????????????
        ext.add(calcBird + "");                         // 7??????????????????
        ext.add(bankerSeat + "");                       // 8???????????????
        ext.add(totalBureau + "");                      // 9?????????
        ext.add(fanshuLimit + "");                      // 10????????????
        ext.add(hasMenQing + "");                       // 11???????????????
        ext.add(menQingJiangJiangHu + "");              // 12??????????????????????????????
        ext.add(yizhiqiao + "");                        // 13???????????????
        ext.add(kaqiao + "");                           // 14??????
        ext.add(isGroupRoom() ? loadGroupId() : "0");   // 15
        ext.add(maxPlayerCount + "");                   // 16
        ext.add(isOver ? "1" : "0");                    // 17
        return ext;
    }

    @Override
    public void sendAccountsMsg() {
        YjClosingInfoRes.Builder builder = sendAccountsMsg(true, false, null, null, null, null, true, 0, 0, null);
        saveLog(true, 0l, builder.build());
    }

    public Class<? extends Player> getPlayerClass() {
        return YjMjPlayer.class;
    }

    @Override
    public int getWanFa() {
        return GameUtil.play_type_yuanjiang;
    }

    @Override
    public boolean isTest() {
        return YjMjConstants.isTest;
    }

    @Override
    public void checkReconnect(Player player) {
        seatMap.get(player.getSeat()).checkSendActionRes();
        checkSendGangRes(player);
        if (actionSeatMap.isEmpty()) {// ????????????????????????????????????
            if (player instanceof YjMjPlayer) {
                YjMjPlayer csMjPlayer = (YjMjPlayer) player;
                if (csMjPlayer != null) {
                    if (csMjPlayer.isAlreadyMoMajiang()) {
                        if (baotingSeat.containsKey(csMjPlayer.getSeat())) {// ??????????????????
                            List<YjMj> disMajiangs = new ArrayList<>();
                            disMajiangs.add(csMjPlayer.getLastMoMajiang());
                            chuPai(csMjPlayer, disMajiangs, 0);
                        }
                    }
                }
            }
        }
        if (state == table_state.play) {
            YjMjPlayer player1 = (YjMjPlayer) player;
            if (player1.getHandPais() != null && player1.getHandPais().size() > 0) {
                sendTingInfo(player1);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(Math.pow(3, 2));
    }

    @SuppressWarnings({"unused", "static-access"})
    private YjClosingInfoRes.Builder test() {
        String[] str = new String[]{
                "{\"dahus\":[1,8],\"icon\":\"http://wx.qlogo.cn/mmopen/25FRchib0VdljibYZe4WsZN0pbBQQECYc0B4V9Bjn5HlDzqeM4JLfp3SIWRCKDDl5VlNibBKViam8xYibFiaWe7Fibm3Ihu8pWWTiaMY/0\",\"point\":-108,\"leftCardNum\":0,\"sex\":1,\"totalPoint\":-153,\"seat\":1,\"name\":\"????????????\",\"userId\":\"103596\",\"actionCounts\":[1,1,0,1],\"isHu\":77,\"cards\":[65,5,35,56,44,14,53,101,20,47,23,104,50,77]}",
                "{\"icon\":\"http://wx.qlogo.cn/mmopen/SOkBQWIHibUbEabTknxaXHYMQMZFMKyoHmuG3LNKOFLvxTQegZwa3UFHOR5Uy3feibDDHnMnd9cErXcG7tgdc8icicOVooGzjypia/0\",\"point\":-12,\"leftCardNum\":0,\"sex\":1,\"totalPoint\":55,\"seat\":2,\"name\":\"GG GL HF\",\"userId\":\"103614\",\"actionCounts\":[0,0,0,0],\"cards\":[98,29,84,15,37,64,2,22,103,76,11,93,13]}",
                "{\"icon\":\"http://wx.qlogo.cn/mmopen/g9RQicMD01M2MfibJkibYic3OAuv4cwTgPfdBBmy6ImlJCWNNJN6IMJbHtKQugt4EPOHzybcY7Sh7MvojoKSQp3s8l0MxDJmFprH/0\",\"point\":-12,\"leftCardNum\":0,\"sex\":1,\"totalPoint\":-6,\"seat\":3,\"name\":\"justin -wan\",\"userId\":\"103592\",\"actionCounts\":[0,0,0,0],\"cards\":[91,78,51,105,7,61,34,12,39,66,85,30,59]}",
                "{\"icon\":\"http://wx.qlogo.cn/mmopen/SOkBQWIHibUbffmgIJic7USO8hIz91ica5ESiankd2YPSTlfbMItaFeKmqnVdSS21IOP1FWBanzbZDraFRncfo8BXg/0\",\"point\":-12,\"leftCardNum\":0,\"sex\":2,\"totalPoint\":-40,\"seat\":4,\"name\":\"??????\",\"userId\":\"103630\",\"actionCounts\":[0,0,0,0],\"cards\":[95,80,63,9,107,90,41,73,46,100,6,60,33]}"

        };

        YjClosingInfoRes.Builder infolist = YjClosingInfoRes.newBuilder();
        for (String resValue : str) {
            YjClosingPlayerInfoRes.Builder info = YjClosingPlayerInfoRes.newBuilder();
            Map<String, Object> resMap = JacksonUtil.readValue(resValue, new TypeReference<Map<String, Object>>() {
            });
            for (Object o : resMap.keySet()) {
                String key = o.toString();
                List<FieldDescriptor> list = info.getDescriptor().getFields();
                for (FieldDescriptor field : list) {

                    if (field.getName().equals(key)) {
                        info.setField(field, resMap.get(key));

                    }
                }
            }

            infolist.addClosingPlayers(info);
            infolist.setWanfa(getWanFa());
            infolist.addAllExt(buildAccountsExt(0, false));
        }

        for (YjMjPlayer player : seatMap.values()) {
            player.writeSocket(infolist.build());
        }
        return infolist;
    }

    @Override
    public boolean consumeCards() {
        return SharedConstants.consumecards;
    }


    @Override
    public void checkAutoPlay() {
        if (getSendDissTime() > 0) {
            for (YjMjPlayer player : seatMap.values()) {
                if (player.getLastCheckTime() > 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                }
            }
            return;
        }

        // ??????????????????
        if (baotingSeat.containsKey(nowDisCardSeat)) {
            YjMjPlayer player = seatMap.get(nowDisCardSeat);
            if (player != null && !actionSeatMap.containsKey(nowDisCardSeat) && !player.hadMoHaiDi()) {
                autoChuPai(player);
                return;
            }
        }

        if (!autoPlay) {
            return;
        }

        if (isAutoPlayOff()) {
            // ????????????
            for (int seat : seatMap.keySet()) {
                YjMjPlayer player = seatMap.get(seat);
                player.setAutoPlay(false, false);
                player.setCheckAutoPlay(false);
            }
            return;
        }

        if (getTableStatus() == YjMjConstants.TABLE_STATUS_PIAO) {
            for (int seat : seatMap.keySet()) {
                YjMjPlayer player = seatMap.get(seat);
                if (player.getLastCheckTime() > 0 && player.getPiaoPoint() >= 0) {
                    player.setLastCheckTime(player.getLastCheckTime() + 1 * 1000);
                    continue;
                }
                player.checkAutoPlay(2, false);
                if (!player.isAutoPlay()) {
                    continue;
                }
                autoPiao(player);
            }
            boolean piao = true;
            for (int seat : seatMap.keySet()) {
                YjMjPlayer player = seatMap.get(seat);
                if (player.getPiaoPoint() < 0) {
                    piao = false;
                }

            }
            if (piao) {
                setTableStatus(YjMjConstants.AUTO_PLAY_TIME);
            }

        } else if (state == table_state.play) {
            autoPlay();
        } else {
            if (getPlayedBureau() == 0) {
                return;
            }
            readyTime++;
            //????????????????????????xx???????????????????????????
            for (YjMjPlayer player : seatMap.values()) {
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
                //???????????????
                for (int seat : huSeatList) {
                    YjMjPlayer player = seatMap.get(seat);
                    if (player == null) {
                        continue;
                    }
                    if (!player.checkAutoPlay(2, false)) {
                        continue;
                    }
                    playCommand(player, new ArrayList<>(), YjMjDisAction.action_hu);
                }
                return;
            } else {
                int action, seat;
                for (Entry<Integer, List<Integer>> entry : actionSeatMap.entrySet()) {
                    List<Integer> actList = YjMjDisAction.parseToDisActionList(entry.getValue());
                    if (actList == null) {
                        continue;
                    }
                    seat = entry.getKey();
                    action = YjMjDisAction.getAutoMaxPriorityAction(actList);
                    YjMjPlayer player = seatMap.get(seat);
                    if (!player.checkAutoPlay(0, false)) {
                        continue;
                    }
                    boolean chuPai = false;
                    if (player.isAlreadyMoMajiang() && !player.hadMoHaiDi()) {
                        chuPai = true;
                    }
                    if (action == YjMjDisAction.action_peng) {
                        if (player.isAutoPlaySelf()) {
                            //???????????????????????????
                            playCommand(player, new ArrayList<>(), YjMjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        } else {
                            if (nowDisCardIds != null && !nowDisCardIds.isEmpty()) {
                                YjMj mj = nowDisCardIds.get(0);
                                List<YjMj> mjList = new ArrayList<>();
                                for (YjMj handMj : player.getHandMajiang()) {
                                    if (handMj.getVal() == mj.getVal()) {
                                        mjList.add(handMj);
                                        if (mjList.size() == 2) {
                                            break;
                                        }
                                    }
                                }
                                playCommand(player, mjList, YjMjDisAction.action_peng);
                            }
                        }
                    } else {
                        if (player.hadMoHaiDi()) {
                            playCommand(player, new ArrayList<>(), YjMjDisAction.action_haodilaoPass);
                        } else {
                            playCommand(player, new ArrayList<>(), YjMjDisAction.action_pass);
                            if (chuPai) {
                                autoChuPai(player);
                            }
                        }
                    }
                }
            }
        } else {
            YjMjPlayer player = seatMap.get(nowDisCardSeat);
            if (player == null || !player.checkAutoPlay(0, false)) {
                return;
            }
            if (player.hadMoHaiDi()) {
                playCommand(player, new ArrayList<>(), YjMjDisAction.action_haodilaoPass);
            } else {
                autoChuPai(player);
            }
        }
    }

    public void autoChuPai(YjMjPlayer player) {

        if (!player.isAlreadyMoMajiang()) {
            return;
        } else if (player.hadMoHaiDi()) {
            // ??????????????????
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
        YjMj mj = YjMj.getMajang(mjId);
        if (mjId != -1) {
            List<YjMj> mjList = YjMjHelper.toMajiang(Arrays.asList(mjId));
            playCommand(player, mjList, YjMjDisAction.action_chupai);
        }
    }

    public boolean IsCalcBankerPoint() {
        return true;
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

    public int getFanshuLimit() {
        return fanshuLimit;
    }

    public void setFanshuLimit(int fanshuLimit) {
        this.fanshuLimit = fanshuLimit;
    }

    public boolean canMenQing() {
        return hasMenQing == 1;
    }

    public void setHasMenQing(int hasMenQing) {
        this.hasMenQing = hasMenQing;
    }

    public int getMenQingJiangJiangHu() {
        return menQingJiangJiangHu;
    }

    public void setMenQingJiangJiangHu(int menQingJiangJiangHu) {
        this.menQingJiangJiangHu = menQingJiangJiangHu;
    }

    public int getYizhiqiao() {
        return yizhiqiao;
    }

    public void setYizhiqiao(int yizhiqiao) {
        this.yizhiqiao = yizhiqiao;
    }

    public int getKaqiao() {
        return kaqiao;
    }

    public void setKaqiao(int kaqiao) {
        this.kaqiao = kaqiao;
    }

    /**
     * ???????????????????????? ???????????????
     *
     * @param seat
     */
    public void removeHuPengPass(int seat) {
        boolean update = false;
        if (huPassList.contains((Object) seat)) {
            huPassList.remove((Object) seat);// ???????????????
            update = true;
        }
        if (pengPassMap.containsKey((Object) seat)) {
            pengPassMap.remove((Object) seat);// ???????????????
            update = true;
        }
        if (update)
            changeExtend();
    }

    /**
     * ?????????????????????????????????
     *
     * @param seat
     * @param calcNextSeat ????????????????????????
     */
    public void setNowYjDisCardSeat(int seat, boolean calcNextSeat) {
        removeHuPengPass(seat);
        if (calcNextSeat) {
            int nextSeat = calcNextSeat(seat);
            setNowDisCardSeat(nextSeat);
        } else {
            setNowDisCardSeat(seat);
        }
    }

    public void clearHuPass() {
        huPassList.clear();
        changeExtend();
    }

    public void clearPengPass() {
        pengPassMap.clear();
        changeExtend();
    }

    public List<Integer> getHuPassSeat() {
        return huPassList;
    }

    public Map<Integer, List<Integer>> getPengPassSeat() {
        return pengPassMap;
    }

    /**
     * ??????????????????(?????????)
     *
     * @param seat
     * @param majiangValue
     * @return
     */
    public boolean inPengPassSeat(int seat, int majiangValue) {
        if (pengPassMap.containsKey((Object) seat) && pengPassMap.get((Object) seat).contains((Object) majiangValue))
            return true;
        else
            return false;
    }

    public void addHuPassSeat(int seat) {
        if (!huPassList.contains(seat)) {
            huPassList.add(seat);
            changeExtend();
        }
    }

    public void addPengPassSeat(int seat, int pengMajiangValue) {
        if (pengPassMap.containsKey(seat)) {
            pengPassMap.get(seat).add(pengMajiangValue);
        } else {
            List<Integer> pengMajiangs = new ArrayList<>();
            pengMajiangs.add(pengMajiangValue);
            pengPassMap.put(seat, pengMajiangs);
        }
        changeExtend();
    }

    @Override
    public void initExtend0(JsonWrapper extend) {
        for (YjMjPlayer player : seatMap.values()) {
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
            moGang = YjMj.getMajang(moGangMajiangId);
        }
        String moGangHu = extend.getString(9);
        if (!StringUtils.isBlank(moGangHu)) {
            moGangHuList = StringUtil.explodeToIntList(moGangHu);
        }
        String gangDisMajiangstr = extend.getString(10);
        if (!StringUtils.isBlank(gangDisMajiangstr)) {
            gangDisMajiangs = MajiangHelper.explodeMajiang(gangDisMajiangstr, ",");
        }
        int gangMajiang = extend.getInt(11, 0);
        if (gangMajiang != 0) {
            this.gangMajiang = YjMj.getMajang(gangMajiang);
        }
        //12????????????
        moLastMajiangSeat = extend.getInt(13, 0);
        int lastMajiangId = extend.getInt(14, 0);
        if (lastMajiangId != 0) {
            this.lastMajiang = YjMj.getMajang(lastMajiangId);
        }
        String baotingSeatStr = extend.getString(15);
        if (!StringUtils.isBlank(baotingSeatStr)) {
            baotingSeat = DataMapUtil.implode(baotingSeatStr);
        }
        disEventAction = extend.getInt(16, 0);
        isCalcBanker = extend.getInt(17, 1);
        calcBird = extend.getInt(18, 1);
        String huPassStr = extend.getString(19);
        if (!StringUtils.isBlank(huPassStr)) {
            huPassList = StringUtil.explodeToIntList(huPassStr);
        }
        String pengPassStr = extend.getString(20);
        if (!StringUtils.isBlank(pengPassStr)) {
            pengPassMap = DataMapUtil.toListMap(pengPassStr);
        }
        payType = extend.getInt(21, 0);
        fanshuLimit = extend.getInt(22, 0);
        hasMenQing = extend.getInt(23, 0);
        menQingJiangJiangHu = extend.getInt(24, 0);
        yizhiqiao = extend.getInt(25, 0);
        kaqiao = extend.getInt(26, 0);
        isAAConsume = Boolean.parseBoolean(extend.getString(27));
        maxPlayerCount = extend.getInt(28, 4);
        if (maxPlayerCount <= 0) {
            maxPlayerCount = 4;
        }
        String birdPaiListStr = extend.getString(29);
        if (!StringUtils.isBlank(birdPaiListStr)) {
            birdPaiList = MajiangHelper.explodeMajiang(birdPaiListStr, ",");
        }
        groupPaylogId = extend.getLong(30, 0);
        autoPlayGlob = extend.getInt(31, 0);
        autoTime = extend.getInt(32, 0);
        tableStatus = extend.getInt(33, 0);
        jiaBei = extend.getInt(34, 0);
        jiaBeiFen = extend.getInt(35, 0);
        jiaBeiShu = extend.getInt(36, 0);
        below = extend.getInt(37, 0);
        belowAdd = extend.getInt(38, 0);
        this.chouPai = extend.getInt(39, 0);
        String chouCardsStr = extend.getString(40);
        if (StringUtils.isNotBlank(chouCardsStr)) {
            this.chouCards = StringUtil.explodeToIntList(chouCardsStr);
        }
        this.maMaHu = extend.getInt(41, 0);
    }

    @Override
    public JsonWrapper buildExtend0(JsonWrapper extend) {
        for (YjMjPlayer player : seatMap.values()) {
            extend.putString(player.getSeat(), player.toExtendStr());
        }
        extend.putString(5, DataMapUtil.explode(huConfirmMap));
        extend.putInt(6, birdNum);
        extend.putInt(7, moMajiangSeat);
        if (moGang != null) {
            extend.putInt(8, moGang.getId());
        } else {
            extend.putInt(8, 0);
        }
        extend.putString(9, StringUtil.implode(moGangHuList, ","));
        extend.putString(10, MajiangHelper.implodeMajiang(gangDisMajiangs, ","));
        if (gangMajiang != null) {
            extend.putInt(11, gangMajiang.getId());
        } else {
            extend.putInt(11, 0);
        }
        //12????????????
        extend.putInt(13, moLastMajiangSeat);
        if (lastMajiang != null) {
            extend.putInt(14, lastMajiang.getId());
        } else {
            extend.putInt(14, 0);
        }
        extend.putString(15, DataMapUtil.explode(baotingSeat));
        extend.putInt(16, disEventAction);
        extend.putInt(17, isCalcBanker);
        extend.putInt(18, calcBird);
        extend.putString(19, StringUtil.implode(huPassList, ","));
        extend.putString(20, DataMapUtil.explodeListMap(pengPassMap));
        extend.putInt(21, payType);
        extend.putInt(22, fanshuLimit);
        extend.putInt(23, hasMenQing);
        extend.putInt(24, menQingJiangJiangHu);
        extend.putInt(25, yizhiqiao);
        extend.putInt(26, kaqiao);
        extend.putString(27, Boolean.toString(isAAConsume));
        extend.putInt(28, maxPlayerCount);
        extend.putString(29, MajiangHelper.implodeMajiang(birdPaiList, ","));
        extend.putLong(30, groupPaylogId);
        extend.putInt(31, autoPlayGlob);
        extend.putInt(32, autoTime);
        extend.putInt(33, tableStatus);
        extend.putInt(34, jiaBei);
        extend.putInt(35, jiaBeiFen);
        extend.putInt(36, jiaBeiShu);
        extend.putInt(37, below);
        extend.putInt(38, belowAdd);
        extend.putInt(39, this.chouPai);
        extend.putString(40, StringUtil.implode(this.chouCards, ","));
        extend.putInt(41, this.maMaHu);
        return extend;
    }

    @Override
    public void createTable(Player player, int play, int bureauCount, List<Integer> params, List<String> strParams, Object... objects) throws Exception {
        //0 ??????  1??????   2 ????????????  3??????????????? 4??????????????????????????????  5?????????????????????  7??????  10??????
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
        info.setConfig(String.valueOf(0));
        info.setExtend(buildExtend());
        TableDao.getInstance().save(info);
        loadFromDB(info);
        setIsCalcBanker(1);// ??????????????????
        setCalcBird(1);// ???????????? ??????
        this.fanshuLimit = StringUtil.getIntValue(params, 2, 0);// ????????????  ???????????????
        this.hasMenQing = StringUtil.getIntValue(params, 3, 0);// ??????????????? ???????????????
        this.birdNum = StringUtil.getIntValue(params, 4, 1); // ?????????????????? 1 5 9??????????????????    3 7????????????  2 6????????????  4 8????????????
        this.yizhiqiao = StringUtil.getIntValue(params, 5, 0);// ?????????????????????  ????????????
        this.kaqiao = StringUtil.getIntValue(params, 6, 0);// ??????
        this.maxPlayerCount = StringUtil.getIntValue(params, 7, 4);// ????????????
        if (this.yizhiqiao == 0) {
            this.kaqiao = 0;
        }
        int payType = StringUtil.getIntValue(params, 10, 0);// ????????????
        setPayType(payType);
        if (payType == 1) {// 1AA??????  2????????????
            setAAConsume(true);
        }

        this.autoTime = StringUtil.getIntValue(params, 11, 0);     // ????????????
        this.autoPlayGlob = StringUtil.getIntValue(params, 12, 0); // ????????????
        if (autoTime > 0) {
            this.autoPlay = true;
        }

        this.jiaBei = StringUtil.getIntValue(params, 13, 0);        // ??????
        this.jiaBeiFen = StringUtil.getIntValue(params, 14, 0);     // ?????????
        this.jiaBeiShu = StringUtil.getIntValue(params, 15, 0);     // ?????????

        if (this.maxPlayerCount == 2) {
            int belowAdd = StringUtil.getIntValue(params, 16, 0);   // ?????? below ??? belowAdd ???
            if (belowAdd <= 100 && belowAdd >= 0) {
                this.belowAdd = belowAdd;
            }
            int below = StringUtil.getIntValue(params, 17, 0);
            if (below <= 100 && below >= 0) {
                this.below = below;
                if (belowAdd > 0 && below == 0) {
                    this.below = 10;
                }
            }
        }

        this.chouPai = StringUtil.getIntValue(params, 18, 0);
        if (this.chouPai != 0 && this.chouPai != 13 && this.chouPai != 26) {
            this.chouPai = 13;
        }
        if (this.maxPlayerCount != 2) { // 2?????????????????????
            this.chouPai = 0;
        }

        this.maMaHu = StringUtil.getIntValue(params, 19, 1);
    }


    public static final List<Integer> wanfaList = Arrays.asList(GameUtil.play_type_yuanjiang);

    public static void loadWanfaTables(Class<? extends BaseTable> cls) {
        for (Integer integer : wanfaList) {
            TableManager.wanfaTableTypesPut(integer, cls);
        }
        HuUtil.init();
    }

    public String getTableMsg() {

        Map<String, Object> json = new HashMap<>();
        json.put("wanFa", "????????????");
        if (isGroupRoom()) {
            json.put("roomName", getRoomName());
        }
        json.put("playerCount", getPlayerCount());
        json.put("count", getTotalBureau());
        if (autoPlay) {
            json.put("autoTime", autoTime / 1000);
            if (autoPlayGlob == 1) {
                json.put("autoName", "??????");
            } else {
                json.put("autoName", "??????");
            }
        }
        return JSON.toJSONString(json);
    }

    public int getAutoPlayGlob() {
        return autoPlayGlob;
    }

    public void setAutoPlayGlob(int autoPlayGlob) {
        this.autoPlayGlob = autoPlayGlob;
    }

    public int getAutoTableCount() {
        return autoTableCount;
    }

    public void setAutoTableCount(int autoTableCount) {
        this.autoTableCount = autoTableCount;
    }

    public int getAutoTime() {
        return autoTime;
    }

    public void setAutoTime(int autoTime) {
        this.autoTime = autoTime;
    }


    public void setTableStatus(int tableStatus) {
        this.tableStatus = tableStatus;
    }

    public int getTableStatus() {
        return tableStatus;
    }

    public void autoPiao(YjMjPlayer player) {
        int piaoPoint = 0;
        if (getTableStatus() != YjMjConstants.TABLE_STATUS_PIAO) {
            return;
        }
        if (player.getPiaoPoint() < 0) {
            player.setPiaoPoint(piaoPoint);
        } else {
            return;
        }
        sendPiaoPoint(player, piaoPoint);
        checkDeal(player.getUserId());
    }

    private void sendPiaoPoint(YjMjPlayer player, int piaoPoint) {
        ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(), piaoPoint);
        broadMsg(build.build());
        broadMsgRoomPlayer(build.build());
    }

    public int getJiaBei() {
        return jiaBei;
    }

    public void setJiaBei(int jiaBei) {
        this.jiaBei = jiaBei;
    }

    public int getJiaBeiFen() {
        return jiaBeiFen;
    }

    public void setJiaBeiFen(int jiaBeiFen) {
        this.jiaBeiFen = jiaBeiFen;
    }

    public int getJiaBeiShu() {
        return jiaBeiShu;
    }

    public void setJiaBeiShu(int jiaBeiShu) {
        this.jiaBeiShu = jiaBeiShu;
    }

    public void sendTingInfo(YjMjPlayer player) {
        long start = System.currentTimeMillis();
        boolean jjHu = player.allOutMjIsJiang();
        boolean mmHu = this.maMaHu == 1 && player.allOutMjIsMa();
        if (player.isAlreadyMoMajiang()) {
            if (actionSeatMap.containsKey(player.getSeat())) {
                return;
            }
            PlayCardResMsg.DaPaiTingPaiRes.Builder tingInfo = PlayCardResMsg.DaPaiTingPaiRes.newBuilder();
            List<YjMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = YjMjTool.dropHongzhong(cards).size();
            int[] cardArr = HuUtil.toCardArray(cards);
            Map<Integer, List<YjMj>> checked = new HashMap<>();
            for (YjMj card : cards) {
                if (card.isHongzhong()) {
                    continue;
                }
                List<YjMj> lackPaiList;
                if (checked.containsKey(card.getVal())) {
                    lackPaiList = checked.get(card.getVal());
                } else {
                    int cardIndex = HuUtil.getMjIndex(card);
                    cardArr[cardIndex] = cardArr[cardIndex] - 1;
                    lackPaiList = YjMjTool.getLackList(cardArr, hzCount, true, jjHu, mmHu);
                    cardArr[cardIndex] = cardArr[cardIndex] + 1;
                    if (lackPaiList.size() > 0) {
                        checked.put(card.getVal(), lackPaiList);
                    } else {
                        continue;
                    }
                }

                PlayCardResMsg.DaPaiTingPaiInfo.Builder ting = PlayCardResMsg.DaPaiTingPaiInfo.newBuilder();
                ting.setMajiangId(card.getId());
                if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
                    //?????????
                    ting.addTingMajiangIds(YjMj.mj201.getId());
                } else {
                    for (YjMj lackPai : lackPaiList) {
                        ting.addTingMajiangIds(lackPai.getId());
                    }
                }
                tingInfo.addInfo(ting.build());
            }
            if (tingInfo.getInfoCount() > 0) {
                player.writeSocket(tingInfo.build());
            }
        } else {
            List<YjMj> cards = new ArrayList<>(player.getHandMajiang());
            int hzCount = YjMjTool.dropHongzhong(cards).size();
            int[] cardArr = HuUtil.toCardArray(cards);
            List<YjMj> lackPaiList = YjMjTool.getLackList(cardArr, hzCount, true, jjHu, mmHu);
            if (lackPaiList == null || lackPaiList.size() == 0) {
                return;
            }
            PlayCardResMsg.TingPaiRes.Builder ting = PlayCardResMsg.TingPaiRes.newBuilder();
            if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
                //?????????
                ting.addMajiangIds(YjMj.mj201.getId());
            } else {
                for (YjMj lackPai : lackPaiList) {
                    ting.addMajiangIds(lackPai.getId());
                }
            }
            player.writeSocket(ting.build());
        }
        long timeUse = (System.currentTimeMillis() - start);
        if (timeUse > 50) {
            StringBuilder sb = new StringBuilder("sendTingInfo");
            sb.append("|").append(timeUse);
            sb.append("|").append(getId());
            sb.append("|").append(getPlayedBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.getHandMajiang());
            LogUtil.msgLog.info(sb.toString());
        }
    }

    public void logFaPaiTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append("faPai");
        sb.append("|").append(playType);
        sb.append("|").append(maxPlayerCount);
        sb.append("|").append(getPayType());
        sb.append("|").append(lastWinSeat);
        LogUtil.msg(sb.toString());
    }

    public void logFaPaiPlayer(YjMjPlayer player, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("faPai");
        sb.append("|").append(player.getHandMajiang());
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logAction(YjMjPlayer player, int action, List<YjMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        String actStr = "unKnown-" + action;
        switch (action) {
            case YjMjDisAction.action_peng:
                actStr = "peng";
                break;
            case YjMjDisAction.action_minggang:
                actStr = "mingGang";
                break;
            case YjMjDisAction.action_chupai:
                actStr = "chuPai";
                break;
            case YjMjDisAction.action_pass:
                actStr = "guo";
                break;
            case YjMjDisAction.action_angang:
                actStr = "anGang";
                break;
            case YjMjDisAction.action_chi:
                actStr = "chi";
                break;
            case YjMjDisAction.action_jiegang:
                actStr = "jieGang";
                break;
            case YjMjDisAction.action_baoting:
                actStr = "baoTing";
                break;
            case YjMjDisAction.action_haodilaoPass:
                actStr = "guoHaiDi";
                break;
        }
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(actStr);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        LogUtil.msg(sb.toString());
    }

    public void logMoMj(YjMjPlayer player, YjMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
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
        sb.append("|").append(player.getHandPais());
        LogUtil.msg(sb.toString());
    }

    public void logChuPaiActList(YjMjPlayer player, YjMj mj, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("chuPaiActList");
        sb.append("|").append(mj);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandPais());
        LogUtil.msg(sb.toString());
    }

    public void logActionHu(YjMjPlayer player, List<YjMj> mjs) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("huPai");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(mjs);
        sb.append("|").append(player.getDaHuNames());
        LogUtil.msg(sb.toString());
    }

    public void logHuPoint(YjMjPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("huPoint");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(player.getPoint());
        sb.append("|").append(player.getTotalPoint());
        sb.append("|").append(player.getHandPais());
        LogUtil.msg(sb.toString());
    }

    public void logQiangGangHu(YjMjPlayer player, List<YjMj> mjs, List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        sb.append("YjMj");
        sb.append("|").append(getId());
        sb.append("|").append(getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append("qiangGangHu");
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append(player.isAutoPlaySelf() ? 1 : 0);
        sb.append("|").append(mjs);
        sb.append("|").append(actListToString(actList));
        sb.append("|").append(player.getHandMajiang());
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
                switch (i) {
                    case YjMjConstants.ACTION_INDEX_HU:
                        sb.append("hu");
                        break;
                    case YjMjConstants.ACTION_INDEX_PENG:
                        sb.append("peng");
                        break;
                    case YjMjConstants.ACTION_INDEX_MINGGANG:
                        sb.append("mingGang");
                        break;
                    case YjMjConstants.ACTION_INDEX_ANGANG:
                        sb.append("anGang");
                        break;
                    case YjMjConstants.ACTION_INDEX_JIEGANG:
                        sb.append("jieGang");
                        break;
                    case YjMjConstants.ACTION_INDEX_GANGBAO:
                        sb.append("gangBao");
                        break;
                    case YjMjConstants.ACTION_INDEX_BAOTING:
                        sb.append("baoTing");
                        break;
                    default:
                        sb.append(i);
                        break;
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public int getChouPai() {
        return chouPai;
    }

    public void setChouPai(int chouPai) {
        this.chouPai = chouPai;
    }

    public List<Integer> getChouCards() {
        return chouCards;
    }

    public void setChouCards(List<Integer> chouCards) {
        this.chouCards = chouCards;
    }

    public boolean canMaMaHu() {
        return maMaHu == 1;
    }
}