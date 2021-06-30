package com.sy599.game.qipai.dddz.tool;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.dddz.bean.DddzPlayer;
import com.sy599.game.qipai.dddz.constant.DddzConstants;
import com.sy599.game.qipai.dddz.util.CardType;
import com.sy599.game.qipai.dddz.util.CardUtils;
import com.sy599.game.util.StringUtil;

import java.util.*;

/**
 * @author lc
 */
public final class CardTool {
    /**
     * @param playerCount 人数
     * @param
     * @param zps
     * @return
     */
    public static List<List<Integer>> fapai(int playerCount, List<List<Integer>> zps) {
        //List<List<Integer>> zps = new ArrayList<>(zp);
        List<List<Integer>> list = new ArrayList<>();
        List<Integer> copy;
        copy = new ArrayList<>(DddzConstants.cardList);
        Collections.shuffle(copy);
        //19
        //八张底牌
        List<Integer> dipai = new ArrayList<Integer>();
        int dip = 2;
        if (playerCount < 5) {
            dip = 4;
        } else if (playerCount == 5) {
            dip = 2;
        }
        dipai.addAll(copy.subList(0, dip));

        copy = copy.subList(dip, copy.size());
        int maxCount = copy.size() / playerCount;
        List<Integer> pai = new ArrayList<>();
        if (GameServerConfig.isDebug()) {
            if (zps != null && !zps.isEmpty()) {
                return zps;
//				List<Integer> pai2 = new ArrayList<>();
//				List<Integer> copy2 =new ArrayList<>(DddzConstants.cardList);
//				 Collections.shuffle(copy2);
//				for (List<Integer> l: zps ) {
//					copy2.removeAll(l);
//				}

//				for (int m=0;m<zps.size()-1;m++) {
//					List<Integer> zp =zps.get(m);
//					if(playerCount==3){
//						//16
//							int su = 16-  zp.size();
//							if(su>0){
//								if(copy2.size()>=su){
//									zp.addAll(copy2.subList(0,su));
//									copy2=copy2.subList(su,copy2.size());
//								}
//							}
//					}else  if(playerCount==2){
//						//12
//						int su = 12-  zp.size();
//						if(su>0){
//							if(copy2.size()>=su){
//								zp.addAll(copy2.subList(0,su));
//								copy2=copy2.subList(su,copy2.size());
//							}
//						}
//					}else if(playerCount==5){
//						//10
//						int su = 10-  zp.size();
//						if(su>0){
//							if(copy2.size()>=su){
//								zp.addAll(copy2.subList(0,su));
//								copy2=copy2.subList(su,copy2.size());
//							}
//						}
//					}
//					pai2.addAll(zp);
//					//	list.add(findCardIIds(copy, zp, 0));
//				}
//				copy = new ArrayList<>(DddzConstants.cardList);
//				for (Integer id : pai2) {
//					copy.remove(id);
//				}
//				list.addAll(zps);
//				for (List<Integer> cards : list) {
//					//System.out.println(cards.size()+" catds ============" + cards);
//				}
//				return list;

            }
        }

        for (int i = 0; i < copy.size(); i++) {
            int card = copy.get(i);
            if (pai.size() < maxCount) {
                pai.add(card);
            } else {
                list.add(pai);
                pai = new ArrayList<>();
                pai.add(card);
            }

        }
        list.add(pai);


        list.add(dipai);
         //System.out.println("" + copy);

        for (List<Integer> cards : list) {
             //System.out.println(cards.size() + " catds ============" + cards);
        }
        return list;
    }


//	public static int checkFirstCard(List<Integer> hands,List<Integer> list ,int zhuColor,int disColor,boolean isFirst) {
//
//
//
//	}


    /***
     * 检查出的牌花色和类型 两位 10位是花色，个位类型
     * @param
     * @return
     */
    public static int checkCardValue(List<Integer> hands, List<Integer> list, int zhuColor, int disColor, boolean isFirst, int chulongpai,List<Integer> deskPai) {

        //一轮首次出牌
        if (isFirst) {
            CardType ct = getCardType(list, zhuColor, chulongpai);
            //甩牌
//            if (ct.getType() == CardType.SHUAIPAI2) {
//                //检查其他玩家是否报副
//                return -1;
//            }
            //副牌不能甩
//            if (!allZhu(list, zhuColor) && ct.getType() >= CardType.SHUAIPAI) {
//                return -1;
//            }
            int card = list.get(0);
            int cardColor = CardUtils.loadCardColor(card);
            int resColor = 0;
            if (CardUtils.isZhu(card, zhuColor) || isLongPai(list, chulongpai)) {
                resColor = zhuColor;//调主
            } else {
                resColor = cardColor;
            }

            int res = resColor * 10 + ct.getType();
            return res;

        }
        int color = disColor / 10;
        int type = disColor % 10;

        //调主
        if (color == zhuColor) {
            if (isLongPai(list, chulongpai)) {
                return 0;
            }
            List<Integer> zhuCards = CardUtils.getZhu(hands, zhuColor);
            //如果有主就有限制，无主了随便出
            if (zhuCards.size() >= list.size()) {
                if (!allZhu(list, zhuColor)) {
                    return -1;
                }
                return canChuPai(list, type, zhuCards,deskPai,zhuColor);
            } else {
                List<Integer> chuZhus = CardUtils.getZhu(list, zhuColor);
                //还有主没出完
                if (chuZhus.size() < zhuCards.size()) {
                    return -1;
                }
            }

        } else {
            //如果有这个花色就有限制，没了随便出
            List<Integer> cards = CardUtils.getColorCards(hands, color);
            if (cards.size() >= list.size()) {
                if (!CardUtils.isSameColor(list) || CardUtils.loadCardColor(list.get(0)) != CardUtils.loadCardColor(cards.get(0))) {
                    return -1;
                }
            } else {
                List<Integer> chuColors = CardUtils.getColorCards(list, color);
                if (chuColors.size() < cards.size()) {//出完
                    return -1;
                }
            }

            return canChuPai(list, type, cards,deskPai,zhuColor);

        }

        return 0;

    }

    //	public static boolean chencOurCardsCanKillNowDisCards(List<Integer> outhands,List<Integer> deskcards,int zhuColor){
//		List<Integer> hand = new ArrayList<>(deskcards);
//		List<List<Integer>> fenzuList = handFenZu(hand,zhuColor);
//
//	}
    public static Map<String, Object> checkShuaiPai(List<Integer> hands, Map<Integer, DddzPlayer> seatMap, int zhuColor, int chupaiSeat) {
        //大王小王 主2 副二 A K  Q J 10
        List<Integer> hand = new ArrayList<>(hands);
        List<Integer> res = new ArrayList<>(hands);
        Map<String, Object> result = new HashMap<>();
        result.put("score", 0);
        result.put("Cards", hand);
        List<List<Integer>> fenzuList = handFenZu(hand, zhuColor);
        //拖拉机
        List<Integer> chuPaiSeatTuoLaJi = checkShuaiPaiContainTuoLaJi(fenzuList, zhuColor);
        int tljlength = chuPaiSeatTuoLaJi.size() / 2;
        if (!chuPaiSeatTuoLaJi.isEmpty()) {
            for (DddzPlayer player : seatMap.values()) {
                if (chupaiSeat == player.getSeat()) {
                    continue;
                }
//                 System.out.println();
//                 System.out.println(player.getName() + "=======tlj=============" + player.getHandPais());
                boolean canOut = checkNextPlayersCanOutTuoLaJi(chuPaiSeatTuoLaJi, player.getHandPais(), zhuColor, tljlength);
                if (canOut) {
                    result.put("score", -20 * chuPaiSeatTuoLaJi.size() / 2);
                    result.put("Cards", chuPaiSeatTuoLaJi);
                    return result;
                }
            }

                //判断完拖拉机后移除拖拉机的牌
                res.removeAll(chuPaiSeatTuoLaJi);
            if(res.isEmpty()){
                return result;
            }else{
                fenzuList = handFenZu(res, zhuColor);
            }

        }

        // 对子
        List<Integer> chuPaiSeatDuiZi = checkShuaiPaiContainDuiZi(fenzuList, zhuColor);
        if (!chuPaiSeatDuiZi.isEmpty()) {
            for (DddzPlayer player : seatMap.values()) {
                if (chupaiSeat == player.getSeat()) {
                    continue;
                }
//                 System.out.println();
//                 System.out.println(player.getName() + "=====dui===============" + player.getHandPais());
                boolean canOut = checkNextPlayersCanOutDuiZi(chuPaiSeatDuiZi, player.getHandPais(), zhuColor);
                if (canOut) {
                    result.put("score", -20);
                    result.put("Cards", chuPaiSeatDuiZi);
                    return result;
                }
            }
                //判断完拖拉机后移除对子的牌
                res.removeAll(chuPaiSeatDuiZi);
                if(res.isEmpty()){
                    return result;
                }else{
                    fenzuList = handFenZu(res, zhuColor);
                }
        }

//         List<Integer> chuPaiSeatDan = checkShuaiPaiContainDan(fenzuList, zhuColor);
        if(!res.isEmpty()) {
            List<Integer> chuPaiSeatDan = new ArrayList<>();
            int min_ =CardUtils.getMinCard(new ArrayList<>(res),zhuColor);
            chuPaiSeatDan.add(min_);
            if (!chuPaiSeatDan.isEmpty()) {
                for (DddzPlayer player : seatMap.values()) {
                    if (chupaiSeat == player.getSeat()) {
                        continue;
                    }
//                    System.out.println();
//                    System.out.println(player.getName() + "=======dan=============" + player.getHandPais());
                    boolean canOut = checkNextPlayersCanOutDan(chuPaiSeatDan, player.getHandPais(), zhuColor);
                    if (canOut) {
                        result.put("score", -10);
                        result.put("Cards", chuPaiSeatDan);
                        return result;
                    }
                }
            }
        }
        return result;
    }


    /**
     * 下家手牌能否打得起拖拉机
     *
     * @param chuPaiSeatTuoLaJi 出的拖拉机
     * @param handPai
     * @param zhuColor
     * @param tljlength         拖拉机长度
     * @return
     */
    private static boolean checkNextPlayersCanOutTuoLaJi(List<Integer> chuPaiSeatTuoLaJi, List<Integer> handPai, int zhuColor, int tljlength) {
        int index0color = 0;
        if (CardUtils.isZhu(chuPaiSeatTuoLaJi.get(0), zhuColor)) {
            index0color = zhuColor;
        } else {
            index0color = CardUtils.loadCardColor(chuPaiSeatTuoLaJi.get(0));
        }
        List<Integer> chuPaiSeatTuoLaJiCopy = new ArrayList<>(chuPaiSeatTuoLaJi);
        List<Integer> handPais = new ArrayList<>(handPai);
        if (index0color == zhuColor) {
            //出牌为主拖拉机
            int[] chuPaiSeatAry = turnZhuPaiToAryTLJ(chuPaiSeatTuoLaJi, zhuColor);
            int chuPaiTljBeginIndex = 0;
            for (int i = 0; i < chuPaiSeatAry.length; i++) {
                if (chuPaiSeatAry[i] >= 2) {
                    chuPaiTljBeginIndex = i;
                    break;
                }
            }

            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
            // fenzulist.get(4); 对应主牌组
            List<Integer> zhulist = fenzuList.get(4);
            if (zhulist.size() < 4) {
                return false;
            }
            int[] nextPlaySeatAry = turnZhuPaiToAryTLJ(zhulist, zhuColor);//10,j q k A   副2  正2  小王 大王
             //System.out.println("主拖出牌：" + StringUtil.implode(chuPaiSeatAry));
             //System.out.println("主拖下家：" + StringUtil.implode(nextPlaySeatAry));
            for (int i = 0; i < nextPlaySeatAry.length - 1; i++) {
                if (nextPlaySeatAry[i] >= 2 && nextPlaySeatAry[i + 1] >= 2 && i > chuPaiTljBeginIndex) {
                    if (tljlength > 2) {
                        if (i + 2 < nextPlaySeatAry.length && nextPlaySeatAry[i + 2] >= 2) {
                            if (i + 3 < nextPlaySeatAry.length && nextPlaySeatAry[i + 3] >= 2) {
                                if (i + 4 < nextPlaySeatAry.length && nextPlaySeatAry[i + 4] >= 2) {
                                    if (i + 5 < nextPlaySeatAry.length && nextPlaySeatAry[i + 5] >= 2) {
                                        if (i + 6 < nextPlaySeatAry.length && nextPlaySeatAry[i + 7] >= 2) {
                                            if (i + 7 < nextPlaySeatAry.length && nextPlaySeatAry[i + 7] >= 2) {
                                                chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 16);
                                            }
                                        } else {
                                            chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 14);
                                        }
                                    } else {
                                        chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 12);
                                    }
                                } else {
                                    chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 10);
                                }
                            } else {
                                chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 8);
                            }
                        } else {
                            chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 6);
                        }
                    }
                    return true;
                }
            }
        } else {
            //出牌为副拖拉机
            int[] chuPaiSeatAry = turnFuPaiToAry(chuPaiSeatTuoLaJi, zhuColor);
            int chuPaiTljBeginIndex = 0;
            for (int i = 0; i < chuPaiSeatAry.length; i++) {
                if (chuPaiSeatAry[i] >= 2) {
                    chuPaiTljBeginIndex = i;
                    break;
                }
            }
            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
            int co = index0color - 1;
            List<Integer> fulist = fenzuList.get(co);//对应颜色的副牌
            if (fulist.size() < 4 && fulist.size() > 0) {
                return false;
            } else if (fulist.size() >= 4) {
                int[] nextPlaySeatAry = turnFuPaiToAry(fulist, zhuColor);//10,j q k A
                 //System.out.println("副拖出牌：" + StringUtil.implode(chuPaiSeatAry));
                 //System.out.println("副拖下家：" + StringUtil.implode(nextPlaySeatAry));
                for (int i = 0; i < nextPlaySeatAry.length - 1; i++) {
                    if (nextPlaySeatAry[i] >= 2 && nextPlaySeatAry[i + 1] >= 2 && i > chuPaiTljBeginIndex) {
                        if (tljlength > 2) {
                            if (i + 2 < nextPlaySeatAry.length && nextPlaySeatAry[i + 2] >= 2) {
                                if (i + 3 < nextPlaySeatAry.length && nextPlaySeatAry[i + 3] >= 2) {
                                    if (i + 4 < nextPlaySeatAry.length && nextPlaySeatAry[i + 4] >= 2) {
                                        if (i + 5 < nextPlaySeatAry.length && nextPlaySeatAry[i + 5] >= 2) {
                                            if (i + 6 < nextPlaySeatAry.length && nextPlaySeatAry[i + 7] >= 2) {
                                                if (i + 7 < nextPlaySeatAry.length && nextPlaySeatAry[i + 7] >= 2) {
                                                    chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 16);
                                                }
                                            } else {
                                                chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 14);
                                            }
                                        } else {
                                            chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 12);
                                        }
                                    } else {
                                        chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 10);
                                    }
                                } else {
                                    chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 8);
                                }
                            } else {
                                chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 6);
                            }
                        }
                        return true;
                    }
                }
            } else if (fulist.size() == 0) {
                //自己没副  出主能否打起
                return false;
//				fulist=fenzuList.get(4);
//				int[] nextPlaySeatAry =turnZhuPaiToAry(fulist,zhuColor);//10,j q k A
//				//System.out.println("副拖出牌："+ StringUtil.implode(chuPaiSeatAry));
//				//System.out.println("主拖下家："+ StringUtil.implode(nextPlaySeatAry));
//				for(int i=0;i<nextPlaySeatAry.length-1;i++){
//					if(nextPlaySeatAry[i]>=2 && nextPlaySeatAry[i+1]>=2){
//						if(tljlength>2){
//							if(i+2<nextPlaySeatAry.length && nextPlaySeatAry[i+2]>=2 ){
//								if(i+3<nextPlaySeatAry.length && nextPlaySeatAry[i+3]>=2 )
//									if(i+4<nextPlaySeatAry.length && nextPlaySeatAry[i+4]>=2 ){
//										if(i+5<nextPlaySeatAry.length && nextPlaySeatAry[i+5]>=2 ){
//											if(i+6<nextPlaySeatAry.length && nextPlaySeatAry[i+7]>=2 ){
//												if(i+7<nextPlaySeatAry.length && nextPlaySeatAry[i+7]>=2 ){
//													chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,16);
//												}
//											}else{
//												chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,14);
//											}
//										}else{
//											chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,12);
//										}
//									}else{
//										chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,10);
//									}
//								}else{
//									chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,8);
//								}
//							}else {
//								chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,6);
//							}
//						}
//						return true;
//					}
//				}
            } else {
                return false;
            }

        }
        return false;
    }



    /**
     * 下家手牌能否打得起  对子
     *
     * @param chuPaiSeatTuoLaJi
     * @param handPai
     * @param zhuColor
     * @return
     */
    private static boolean checkNextPlayersCanOutDuiZi(List<Integer> chuPaiSeatTuoLaJi, List<Integer> handPai, int zhuColor) {
        int index0color = 0;
        if (CardUtils.isZhu(chuPaiSeatTuoLaJi.get(0), zhuColor)) {
            index0color = zhuColor;
        } else {
            index0color = CardUtils.loadCardColor(chuPaiSeatTuoLaJi.get(0));
        }
        List<Integer> handPais = new ArrayList<>(handPai);
        if (index0color == zhuColor) {
            //出牌为主a
            int[] chuPaiSeatAry = turnZhuPaiToAryDuiZiAndDan(chuPaiSeatTuoLaJi, zhuColor);
            int chuPaiTljBeginIndex = 0;
            for (int i = 0; i < chuPaiSeatAry.length; i++) {
                if (chuPaiSeatAry[i] >= 2) {
                    chuPaiTljBeginIndex = i;
                    break;
                }
            }

            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
            // fenzulist.get(4); 对应主牌组
            List<Integer> zhulist = fenzuList.get(4);
            if (zhulist.size() < 2) {
                return false;
            }
            int[] nextPlaySeatAry = turnZhuPaiToAryDuiZiAndDan(zhulist, zhuColor);//10,j q k A   副2  正2  小王 大王
            //System.out.println("主对出牌：" + StringUtil.implode(chuPaiSeatAry));
            //System.out.println("主对下家：" + StringUtil.implode(nextPlaySeatAry));
            for (int i = 0; i < nextPlaySeatAry.length; i++) {
                if (nextPlaySeatAry[i] >= 2 && i > chuPaiTljBeginIndex) {
                    if(i==5){
                       List<Integer>  a = getFu2(zhulist,zhuColor);
                       if(null==a){
                           //没有对副2
                           continue;
                       }else{
                           return true;
                       }
                    } else{
                        return true;
                    }
                }
            }
        } else {
            //出牌为副对子
            int[] chuPaiSeatAry = turnFuPaiToAry(chuPaiSeatTuoLaJi, zhuColor);
            int chuPaiTljBeginIndex = 0;
            for (int i = 0; i < chuPaiSeatAry.length; i++) {
                if (chuPaiSeatAry[i] >= 2) {
                    chuPaiTljBeginIndex = i;
                    break;
                }
            }
            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
            int co = index0color - 1;
            List<Integer> fulist = fenzuList.get(co);//对应颜色的副牌
            if (fulist.size() < 2 && fulist.size() > 0) {
                return false;
            } else if (fulist.size() >= 2) {
                int[] nextPlaySeatAry = turnFuPaiToAry(fulist, zhuColor);//10,j q k A
                 //System.out.println("副对出牌：" + StringUtil.implode(chuPaiSeatAry));
                 //System.out.println("副对下家：" + StringUtil.implode(nextPlaySeatAry));
                for (int i = 0; i < nextPlaySeatAry.length; i++) {
                    if (nextPlaySeatAry[i] >= 2 && i > chuPaiTljBeginIndex) {
                        return true;
                    }
                }
            } else if (fulist.size() == 0) {
                //自己没副  出主能否打起
                return false;
//				fulist=fenzuList.get(4);
//				int[] nextPlaySeatAry =turnZhuPaiToAryDuiZiAndDan(fulist,zhuColor);//10,j q k A
//				//System.out.println("副对出牌："+ StringUtil.implode(chuPaiSeatAry));
//				//System.out.println("主对下家："+ StringUtil.implode(nextPlaySeatAry));
//				for(int i=0;i<nextPlaySeatAry.length;i++){
//					if(nextPlaySeatAry[i]>=2){
//						return true;
//					}
//				}
            } else {
                return false;
            }

        }
        return false;
    }

    private static void removeListParam(List<Integer> hands ,int num,int removenum){
        int ni=0;
        List<Integer> removelist = new ArrayList<>();
        for (int i=0;i<hands.size();i++ ) {
            if(hands.get(i)==num){
                removelist.add(hands.get(i));
                ni++;
                if(ni==removenum){
                   break;
                }
            }
        }
        hands.removeAll(removelist);
    }


    private static boolean checkNextPlayersCanOutDan(List<Integer> chuPaiSeatTuoLaJi, List<Integer> handPai, int zhuColor) {
        int index0color = 0;
        if (CardUtils.isZhu(chuPaiSeatTuoLaJi.get(0), zhuColor)) {
            index0color = zhuColor;
        } else {
            index0color = CardUtils.loadCardColor(chuPaiSeatTuoLaJi.get(0));
        }
        List<Integer> handPais = new ArrayList<>(handPai);
        if (index0color == zhuColor) {
            //出牌为主
            int[] chuPaiSeatAry = turnZhuPaiToAry(chuPaiSeatTuoLaJi, zhuColor);
            int chuPaiTljBeginIndex = 0;
            for (int i = 0; i < chuPaiSeatAry.length; i++) {
                if (chuPaiSeatAry[i] == 1) {
                    chuPaiTljBeginIndex = i;
                    break;
                }
            }

            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
            // fenzulist.get(4); 对应主牌组
            List<Integer> zhulist = fenzuList.get(4);
            if (zhulist.size() <= 0) {
                return false;
            }
            int[] nextPlaySeatAry = turnZhuPaiToAryDuiZiAndDan(zhulist, zhuColor);//10,j q k A   副2  正2  小王 大王
             //System.out.println("主单出牌：" + StringUtil.implode(chuPaiSeatAry));
             //System.out.println("主单下家：" + StringUtil.implode(nextPlaySeatAry));
            for (int i = 0; i < nextPlaySeatAry.length; i++) {
                if (nextPlaySeatAry[i] >= 1 && i > chuPaiTljBeginIndex) {
                    return true;
                }
            }
        } else {
            //出牌为副
            int[] chuPaiSeatAry = turnFuPaiToAry(chuPaiSeatTuoLaJi, zhuColor);
            int chuPaiTljBeginIndex = 0;
            for (int i = 0; i < chuPaiSeatAry.length; i++) {
                if (chuPaiSeatAry[i] == 1) {
                    chuPaiTljBeginIndex = i;
                    break;
                }
            }
            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
            int co = index0color - 1;
            List<Integer> fulist = fenzuList.get(co);//对应颜色的副牌
            if (fulist.size() >= 1) {
                int[] nextPlaySeatAry = turnFuPaiToAry(fulist, zhuColor);//10,j q k A
                 //System.out.println("副单出牌：" + StringUtil.implode(chuPaiSeatAry));
                 //System.out.println("副单下家：" + StringUtil.implode(nextPlaySeatAry));
                for (int i = 0; i < nextPlaySeatAry.length; i++) {
                    if (nextPlaySeatAry[i] >= 1 && i > chuPaiTljBeginIndex) {
                        return true;
                    }
                }
            } else if (fulist.size() == 0) {
                //自己没副  出主能否打起
                return false;
//				fulist=fenzuList.get(4);
//				int[] nextPlaySeatAry =turnZhuPaiToAryDuiZiAndDan(fulist,zhuColor);//10,j q k A
//				//System.out.println("副单出牌："+ StringUtil.implode(chuPaiSeatAry));
//				//System.out.println("主单下家："+ StringUtil.implode(nextPlaySeatAry));
//				for(int i=0;i<nextPlaySeatAry.length;i++){
//					if(nextPlaySeatAry[i]>=1){
//						return true;
//					}
//				}
            } else {
                return false;
            }

        }
        return false;
    }


    public static List<Integer> checkShuaiPaiContainTuoLaJi(List<List<Integer>> fenzuList, int zhuColor) {

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < fenzuList.size(); i++) {
            //方片 1 梅花2 红桃3 黑桃4  5王
            int color = i + 1;
            List<Integer> colorlist = fenzuList.get(i);
//            if(null == colorlist || colorlist.size()%2!=0){
//                continue;
//            }
            if (colorlist.size() >= 4 && color != zhuColor) {
                int[] ary = turnFuPaiToAry(colorlist, zhuColor);
                //int minPai = isContainTuoLaJi(ary);
                List<Integer> re = isContainTuoLaJi(ary);
                int minPai = re.get(0);
                int length = re.get(1);//拖拉机的长度
                if (minPai > 0) {
                    //从minpai值开的拖拉机
                    //返回这个拖拉机
                    result = getTuoLaJiFromParam(minPai, color, length, colorlist);
                }
            }
            if (i == 4) {
                //主 int[] ay ={0,0,0,0,0,0,0,0,0};//10,j q k A   副2  正2  小王 大王
                int[] ary = turnZhuPaiToAry(colorlist, zhuColor);
                List<Integer> re = isContainTuoLaJi(ary);
                int minPai = re.get(0);
                int length = re.get(1);//拖拉机的长度
                if (minPai == 0) {
                    return result;
                }
                result = getTuoLaJiFromParam(minPai, zhuColor, length, colorlist);
            }

        }
        return result;
    }

    private static List<Integer> getTuoLaJiFromParam(int beginIndex, int zhuColor, int length, List<Integer> colorlist) {
        List<Integer> tuolaji = new ArrayList<>();
        int i = beginIndex;
        do {
            if (i <= 14) {
                int cards = i + 100 * zhuColor;//10-A
                tuolaji.add(cards);
                tuolaji.add(cards);
            } else if (i == 15) {
                //副2
                List<Integer> fu2 = getFu2(colorlist, zhuColor);
                if(null!=fu2 && fu2.size()>0){
                    tuolaji.addAll(fu2);
                }
            } else if (i == 16) {
                //正2小王
                int cards = 15 + 100 * zhuColor;
                tuolaji.add(cards);
                tuolaji.add(cards);
            } else if (i == 17) {
                //小王
                tuolaji.add(501);
                tuolaji.add(501);

            } else if (i == 18) {
                // 大王
                tuolaji.add(502);
                tuolaji.add(502);
            }
            i++;
        } while (i < length + beginIndex);

        return tuolaji;
    }

    private static List<Integer> checkShuaiPaiContainDuiZi(List<List<Integer>> fenzuList, int zhuColor) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < fenzuList.size(); i++) {
            //方片 1 梅花2 红桃3 黑桃4  5王
            int color = i + 1;
            List<Integer> colorlist = fenzuList.get(i);
            if (colorlist.size() >= 2 && color != zhuColor) {
                int[] ary = turnFuPaiToAry(colorlist, zhuColor);
                int minPai = isContainDuiZi(ary);
                if (minPai > 0) {
                    //返回这一对
                    int cards = minPai + 100 * color;
                    result.add(cards);
                    result.add(cards);
                    return result;
                }
            }
            if (i == 4) {
                //主 int[] ay ={0,0,0,0,0,0,0,0,0};//10,j q k A   副2  正2  小王 大王
                int[] ary = turnZhuPaiToAry(colorlist, zhuColor);
                int minPai = isContainDuiZi(ary);
                if (minPai == 0) {
                    return result;
                }
                if (minPai == 15) {
                    //副2
                    List<Integer> fu2 = getFu2(colorlist, zhuColor);
                    if (null != fu2 && fu2.size() >= 2) {
                        result.addAll(fu2);
                    }
                    return result;
                } else if (minPai == 16) {
                    //正2
                    int cards = 15 + 100 * zhuColor;
                    result.add(cards);
                    result.add(cards);
                    return result;
                } else if (minPai == 17) {
                    //小王
                    result.add(501);
                    result.add(501);
                    return result;
                } else if (minPai == 18) {
                    //小王
                    result.add(502);
                    result.add(502);
                    return result;
                } else {
                    int cards = minPai + 100 * zhuColor;//10-A
                    result.add(cards);
                    result.add(cards);
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * 得到分组咧中最大的对子
     * @param fenzuList
     * @param zhuColor
     * @return
     */
    private static List<Integer> getShuaiPaiContainMaxDuiZi(List<List<Integer>> fenzuList, int zhuColor) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < fenzuList.size(); i++) {
            //方片 1 梅花2 红桃3 黑桃4  5王
            int color = i + 1;
            List<Integer> colorlist = fenzuList.get(i);
            if (colorlist.size() >= 2 && color != zhuColor) {
                int[] ary = turnFuPaiToAry(colorlist, zhuColor);
                int minPai = getContainMaxDuiZi(ary);
                if (minPai > 0) {
                    //返回这一对
                    int cards = minPai + 100 * color;
                    result.add(cards);
                    result.add(cards);
                   return result;
                }
            }
            if (i == 4) {
                //主 int[] ay ={0,0,0,0,0,0,0,0,0};//10,j q k A   副2  正2  小王 大王
                int[] ary = turnZhuPaiToAry(colorlist, zhuColor);
                int minPai = getContainMaxDuiZi(ary);
                if (minPai == 0) {
                    return result;
                }
                if (minPai == 15) {
                    //副2
                    List<Integer> fu2 = getFu2(colorlist, zhuColor);
                    if (null != fu2 && fu2.size() >= 2) {
                        result.addAll(fu2);
                    }
                    return result;
                } else if (minPai == 16) {
                    //正2
                    int cards = 15 + 100 * zhuColor;
                    result.add(cards);
                    result.add(cards);
                    return result;
                } else if (minPai == 17) {
                    //小王
                    result.add(501);
                    result.add(501);
                    return result;
                } else if (minPai == 18) {
                    //小王
                    result.add(502);
                    result.add(502);
                    return result;
                } else {
                    int cards = minPai + 100 * zhuColor;//10-A
                    result.add(cards);
                    result.add(cards);
                    return result;
                }
            }
        }
       return  result;

    }

    private static List<Integer> checkShuaiPaiContainDan(List<List<Integer>> fenzuList, int zhuColor) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < fenzuList.size(); i++) {
            //方片 1 梅花2 红桃3 黑桃4  5王
            int color = i + 1;
            List<Integer> colorlist = fenzuList.get(i);
            if (colorlist.size() >= 1 && color != zhuColor) {
                int[] ary = turnFuPaiToAry(colorlist, zhuColor);
                int minPai = isContainDan(ary);
                if (minPai > 0) {
                    //返回dan
                    int cards = minPai + 100 * color;
                    result.add(cards);
                    return result;
                }
            }
            if (i == 4) {
                //主 int[] ay ={0,0,0,0,0,0,0,0,0};//10,j q k A   副2  正2  小王 大王
                //	int[] ary =turnZhuPaiToAry(colorlist,zhuColor);//副2对子不计算
                int[] ary = RemoveDuiTurnZhuPaiToAry(colorlist, zhuColor);
                int minPai = isContainDan2(ary);
                if (minPai == 0) {
                    return result;
                }
                if (minPai == 15) {
                    //副2 返回一个副2
                    List<Integer> fu2 = getOneFu2(colorlist, zhuColor);
                    result.add(fu2.get(0));
                    return result;
                } else if (minPai == 16) {
                    //正2
                    int cards = 15 + 100 * zhuColor;
                    result.add(cards);
                    return result;
                } else if (minPai == 17) {
                    //小王
                    result.add(501);
                    return result;
                } else if (minPai == 18) {
                    //小王
                    result.add(502);
                    return result;
                } else {
                    int cards = minPai + 100 * zhuColor;//10-A
                    result.add(cards);
                    return result;
                }
            }
        }
        return result;
    }


    private static List<Integer> getFu2(List<Integer> colorlist, int zhuColor) {
        List<Integer> temp = new ArrayList<>(colorlist);
        List<Integer> re = new ArrayList<>();
        for (int p : colorlist) {
            if (CardUtils.loadCardValue(p) == 15 && CardUtils.loadCardColor(p) != zhuColor) {
                int num = 0;
                for (int p2 : temp) {
                    if (p2 == p) {
                        num++;
                    }
                    if (num == 2) {
                        re.add(p2);
                        re.add(p2);
                        return re;
                    }
                }
            }
        }
        return null;
    }

    private static List<Integer> getAllFu2(List<Integer> colorlist, int zhuColor) {
        List<Integer> temp = new ArrayList<>(colorlist);
        List<Integer> re = new ArrayList<>();
        for (int p : colorlist) {
            if (CardUtils.loadCardValue(p) == 15 && CardUtils.loadCardColor(p) != zhuColor) {
                re.add(p);
            }
        }
        return re;
    }

    private static List<Integer> getOneFu2(List<Integer> colorlist, int zhuColor) {
        List<Integer> temp = new ArrayList<>(colorlist);
        List<Integer> re = new ArrayList<>();
        for (int p : colorlist) {
            if (CardUtils.loadCardValue(p) == 15 && CardUtils.loadCardColor(p) != zhuColor) {
                re.add(p);
                return re;
            }
        }
        return null;
    }


    private static boolean checkNextPlayersCanKillShuaiTuoLaJi(List<Integer> chuPaiSeatTuoLaJi, List<Integer> handPai, int zhuColor, int tljlength) {
        int index0color = 0;
        if (CardUtils.isZhu(chuPaiSeatTuoLaJi.get(0), zhuColor)) {
            index0color = zhuColor;
        } else {
            index0color = CardUtils.loadCardColor(chuPaiSeatTuoLaJi.get(0));
        }
        List<Integer> chuPaiSeatTuoLaJiCopy = new ArrayList<>(chuPaiSeatTuoLaJi);
        List<Integer> handPais = new ArrayList<>(handPai);
        if (index0color == zhuColor) {
            //出牌为主拖拉机
            int[] chuPaiSeatAry = turnZhuPaiToAryTLJ(chuPaiSeatTuoLaJi, zhuColor);
            int chuPaiTljBeginIndex = 0;
            for (int i = 0; i < chuPaiSeatAry.length; i++) {
                if (chuPaiSeatAry[i] >= 2) {
                    chuPaiTljBeginIndex = i;
                    break;
                }
            }
            if(handPai.size()<4){
                //迭代剩余牌不足以继续
                return false;
            }
            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
            // fenzulist.get(4); 对应主牌组
            List<Integer> zhulist = fenzuList.get(4);
            if (zhulist.size() < 4) {
                return false;
            }
            int[] nextPlaySeatAry = turnZhuPaiToAryTLJ(zhulist, zhuColor);//10,j q k A   副2  正2  小王 大王
             //System.out.println("主拖出牌：" + StringUtil.implode(chuPaiSeatAry));
             //System.out.println("主拖下家：" + StringUtil.implode(nextPlaySeatAry));
            for (int i = 0; i < nextPlaySeatAry.length - 1; i++) {
                if (nextPlaySeatAry[i] >= 2 && nextPlaySeatAry[i + 1] >= 2 && i > chuPaiTljBeginIndex) {
                    if (tljlength > 2) {
                        if (i + 2 < nextPlaySeatAry.length && nextPlaySeatAry[i + 2] >= 2) {
                            if (i + 3 < nextPlaySeatAry.length && nextPlaySeatAry[i + 3] >= 2) {
                                if (i + 4 < nextPlaySeatAry.length && nextPlaySeatAry[i + 4] >= 2) {
                                    if (i + 5 < nextPlaySeatAry.length && nextPlaySeatAry[i + 5] >= 2) {
                                        if (i + 6 < nextPlaySeatAry.length && nextPlaySeatAry[i + 7] >= 2) {
                                            if (i + 7 < nextPlaySeatAry.length && nextPlaySeatAry[i + 7] >= 2) {
                                                chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 16);
                                            }
                                        } else {
                                            chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 14);
                                        }
                                    } else {
                                        chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 12);
                                    }
                                } else {
                                    chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 10);
                                }
                            } else {
                                chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,8);
                            }
                            handPai.removeAll( getTuoLaJiFromParam(i+10,zhuColor,chuPaiSeatTuoLaJi.size()/2,zhulist)) ;
                            return true;
                        }else {
                            List<Integer> tem = getTuoLaJiFromParam(i+10,zhuColor,chuPaiSeatTuoLaJi.size()/2,zhulist);
                            handPai.removeAll( tem) ;
                            return true;
                        }
                    }else {
                        return true;
                    }
                }
            }
        } else {
            //出牌为副拖拉机
            int[] chuPaiSeatAry = turnFuPaiToAry(chuPaiSeatTuoLaJi, zhuColor);
            int chuPaiTljBeginIndex = 0;
            for (int i = 0; i < chuPaiSeatAry.length; i++) {
                if (chuPaiSeatAry[i] >= 2) {
                    chuPaiTljBeginIndex = i;
                    break;
                }
            }
            if(handPai.size()<4){
                //迭代剩余牌不足以继续
                return false;
            }
            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
            int co = index0color - 1;
            List<Integer> fulist = fenzuList.get(co);//对应颜色的副牌
            if (fulist.size() < 4 && fulist.size() > 0) {
                return false;
            } else if (fulist.size() >= 4) {
                int[] nextPlaySeatAry = turnFuPaiToAry(fulist, zhuColor);//10,j q k A
                 //System.out.println("副拖出牌：" + StringUtil.implode(chuPaiSeatAry));
                 //System.out.println("副拖下家：" + StringUtil.implode(nextPlaySeatAry));
                for (int i = 0; i < nextPlaySeatAry.length - 1; i++) {
                    if (nextPlaySeatAry[i] >= 2 && nextPlaySeatAry[i + 1] >= 2 && i > chuPaiTljBeginIndex) {
                        if (tljlength > 2) {
                            if (i + 2 < nextPlaySeatAry.length && nextPlaySeatAry[i + 2] >= 2) {
                                if (i + 3 < nextPlaySeatAry.length && nextPlaySeatAry[i + 3] >= 2) {
                                    if (i + 4 < nextPlaySeatAry.length && nextPlaySeatAry[i + 4] >= 2) {
                                        if (i + 5 < nextPlaySeatAry.length && nextPlaySeatAry[i + 5] >= 2) {
                                            if (i + 6 < nextPlaySeatAry.length && nextPlaySeatAry[i + 7] >= 2) {
                                                if (i + 7 < nextPlaySeatAry.length && nextPlaySeatAry[i + 7] >= 2) {
                                                    chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 16);
                                                }
                                            } else {
                                                chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 14);
                                            }
                                        } else {
                                            chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 12);
                                        }
                                    } else {
                                        chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 10);
                                    }
                                } else {
                                    chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 8);
                                }
                            } else {
                                chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0, 6);
                            }
                        }
                        return true;
                    }
                }
            } else if (fulist.size() == 0) {
                //自己没副  出主能否打起
                fulist=fenzuList.get(4);
                int[] nextPlaySeatAry =turnZhuPaiToAryTLJ(fulist,zhuColor);//10,j q k A
                //System.out.println("副拖出牌："+ StringUtil.implode(chuPaiSeatAry));
                //System.out.println("主拖下家："+ StringUtil.implode(nextPlaySeatAry));
                for(int i=0;i<nextPlaySeatAry.length-1;i++){
                    if(nextPlaySeatAry[i]>=2 && nextPlaySeatAry[i+1]>=2){
                        if(tljlength>2){
                            if(i+2<nextPlaySeatAry.length && nextPlaySeatAry[i+2]>=2 ){
                                if(i+3<nextPlaySeatAry.length && nextPlaySeatAry[i+3]>=2 )
                                    if(i+4<nextPlaySeatAry.length && nextPlaySeatAry[i+4]>=2 ){
                                        if(i+5<nextPlaySeatAry.length && nextPlaySeatAry[i+5]>=2 ){
                                            if(i+6<nextPlaySeatAry.length && nextPlaySeatAry[i+7]>=2 ){
                                                if(i+7<nextPlaySeatAry.length && nextPlaySeatAry[i+7]>=2 ){
                                                    chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,16);
                                                }
                                            }else{
                                                chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,14);
                                            }
                                        }else{
                                            chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,12);
                                        }
                                    }else{
                                        chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,10);
                                    }
                            }else{
                                chuPaiSeatTuoLaJi = chuPaiSeatTuoLaJiCopy.subList(0,8);
                            }
                            List<Integer> a =  getTuoLaJiFromParam(i+10,zhuColor,chuPaiSeatTuoLaJi.size()/2,fulist);
                            List<Integer> hands2 = new ArrayList<>(handPai);
                            hands2.removeAll(a);
                            handPai=hands2;
                            return true;
                        }else {
                            List<Integer> tem = getTuoLaJiFromParam(i+10,zhuColor,chuPaiSeatTuoLaJi.size()/2,fulist);
                             List<Integer> hands2 = new ArrayList<>(handPai);
                            hands2.removeAll(tem);
                            handPai=hands2;
                            return true;
                        }
                    }

                }
            }
        }
        return false;
    }
    private static boolean checkNextPlayersCanKillShuaiDui(List<Integer> chuPaiSeatTuoLaJi, List<Integer> handPai, int zhuColor) {
        int index0color = 0;
        if (CardUtils.isZhu(chuPaiSeatTuoLaJi.get(0), zhuColor)) {
            index0color = zhuColor;
        } else {
            index0color = CardUtils.loadCardColor(chuPaiSeatTuoLaJi.get(0));
        }
        List<Integer> handPais = new ArrayList<>(handPai);
        if (index0color == zhuColor) {
            //出牌为主a
            int[] chuPaiSeatAry = turnZhuPaiToAryDuiZiAndDan(chuPaiSeatTuoLaJi, zhuColor);
            int chuPaiTljBeginIndex = 0;
            for (int i = 0; i < chuPaiSeatAry.length; i++) {
                if (chuPaiSeatAry[i] >= 2) {
                    chuPaiTljBeginIndex = i;
                    break;
                }
            }
            if(handPai.size()<2){
                //迭代剩余牌不足以继续
                return false;
            }
            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
            // fenzulist.get(4); 对应主牌组
            List<Integer> zhulist = fenzuList.get(4);
            if (zhulist.size() < 2) {
                return false;
            }
            int[] nextPlaySeatAry = turnZhuPaiToAryDuiZiAndDan(zhulist, zhuColor);//10,j q k A   副2  正2  小王 大王
//             System.out.println("主对出牌：" + StringUtil.implode(chuPaiSeatAry));
//             System.out.println("主对下家：" + StringUtil.implode(nextPlaySeatAry));
            for (int i = 0; i < nextPlaySeatAry.length; i++) {
                if (nextPlaySeatAry[i] >= 2 && i > chuPaiTljBeginIndex) {
                    if(i==5){
                        List<Integer> f2 = getFu2(zhulist,zhuColor);
                        if(null==f2|| f2.size()<0){
                            List<Integer>  allf2 =  getAllFu2(zhulist,zhuColor);
                            handPai.removeAll(allf2);
                            return false;
                        }else{
                            handPai.removeAll(f2);
                            return true;
                        }
                    }else if(i==6){
                        removeListParam(handPai,15+100*zhuColor,2);return true;
                    }else if(i==7){
                        removeListParam(handPai,501,2);return true;
                    }else if(i==8){
                        removeListParam(handPai,502,2);return true;
                    }else{
                        removeListParam(handPai,10+i+100*zhuColor,2);return true;
                    }
                }
            }
        } else {
            //出牌为副对子
            int[] chuPaiSeatAry = turnFuPaiToAry(chuPaiSeatTuoLaJi, zhuColor);
            int chuPaiTljBeginIndex = 0;
            for (int i = 0; i < chuPaiSeatAry.length; i++) {
                if (chuPaiSeatAry[i] >= 2) {
                    chuPaiTljBeginIndex = i;
                    break;
                }
            }
            if(handPai.size()<2){
                //迭代剩余牌不足以继续
                return false;
            }
            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
            int co = index0color - 1;
            List<Integer> fulist = fenzuList.get(co);//对应颜色的副牌
            if (fulist.size() < 2 && fulist.size() > 0) {
                return false;
            } else if (fulist.size() >= 2) {
                int[] nextPlaySeatAry = turnFuPaiToAry(fulist, zhuColor);//10,j q k A
                 //System.out.println("副对出牌：" + StringUtil.implode(chuPaiSeatAry));
                 //System.out.println("副对下家：" + StringUtil.implode(nextPlaySeatAry));
                for (int i = 0; i < nextPlaySeatAry.length; i++) {
                    if (nextPlaySeatAry[i] >= 2 && i > chuPaiTljBeginIndex) {

                        return true;
                    }
                }
            } else if (fulist.size() == 0) {
                //自己没副  出主能否打起
                fulist=fenzuList.get(4);
                int[] nextPlaySeatAry =turnZhuPaiToAryDuiZiAndDan(fulist,zhuColor);//10,j q k A
                //System.out.println("副对出牌："+ StringUtil.implode(chuPaiSeatAry));
                //System.out.println("主对下家："+ StringUtil.implode(nextPlaySeatAry));
                for(int i=0;i<nextPlaySeatAry.length;i++){
                    if(nextPlaySeatAry[i]>=2){
                        if(i==5){
                            List<Integer> f2 = getFu2(fulist,zhuColor);
                            if(null==f2|| f2.size()<0){
                                List<Integer>  allf2 =  getAllFu2(fulist,zhuColor);
                                handPai.removeAll(allf2);
                                return false;
                            }else{
                                handPai.removeAll(f2);
                                return true;
                            }
                        }else if(i==6){
                            removeListParam(handPai,15+100*zhuColor,2);return true;
                        }else if(i==7){
                            removeListParam(handPai,501,2);return true;
                        }else if(i==8){
                            removeListParam(handPai,502,2);return true;
                        }else{
                            removeListParam(handPai,10+i+100*zhuColor,2);
                            //System.out.println(handPai);
                            return true;
                        }
                    }
                }
            } else {
                return false;
            }

        }
        return false;
    }
//    private static boolean checkNextPlayersCanKillShuaiDan(List<Integer> chuPaiSeatTuoLaJi, List<Integer> handPai, int zhuColor) {
//        int index0color = 0;
//        if (CardUtils.isZhu(chuPaiSeatTuoLaJi.get(0), zhuColor)) {
//            index0color = zhuColor;
//        } else {
//            index0color = CardUtils.loadCardColor(chuPaiSeatTuoLaJi.get(0));
//        }
//        List<Integer> handPais = new ArrayList<>(handPai);
//        if (index0color == zhuColor) {
//            //出牌为主
//            int[] chuPaiSeatAry = turnZhuPaiToAry(chuPaiSeatTuoLaJi, zhuColor);
//            int chuPaiTljBeginIndex = 0;
//            for (int i = 0; i < chuPaiSeatAry.length; i++) {
//                if (chuPaiSeatAry[i] == 1) {
//                    chuPaiTljBeginIndex = i;
//                    break;
//                }
//            }
//
//            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
//            // fenzulist.get(4); 对应主牌组
//            List<Integer> zhulist = fenzuList.get(4);
//            if (zhulist.size() <= 0) {
//                return false;
//            }
//            int[] nextPlaySeatAry = turnZhuPaiToAryDuiZiAndDan(zhulist, zhuColor);//10,j q k A   副2  正2  小王 大王
//            //System.out.println("k主单出牌：" + StringUtil.implode(chuPaiSeatAry));
//            //System.out.println("k主单下家：" + StringUtil.implode(nextPlaySeatAry));
//            for (int i = 0; i < nextPlaySeatAry.length; i++) {
//                if (nextPlaySeatAry[i] >= 1 && i > chuPaiTljBeginIndex) {
//                    if(i==5){
//                        List<Integer> f2 = getOneFu2(zhulist,zhuColor);
//                        removeListParam(handPai,f2.get(0),1);return true;
//                    }else if(i==6){
//                        removeListParam(handPai,15+100*zhuColor,1);return true;
//                    }else if(i==7){
//                        removeListParam(handPai,501,1);return true;
//                    }else if(i==8){
//                        removeListParam(handPai,502,1);return true;
//                    }else{
//                        removeListParam(handPai,10+i+100*zhuColor,1);return true;
//                    }
//                }
//            }
//        } else {
//            //出牌为副
//            int[] chuPaiSeatAry = turnFuPaiToAry(chuPaiSeatTuoLaJi, zhuColor);
//            int chuPaiTljBeginIndex = 0;
//            for (int i = 0; i < chuPaiSeatAry.length; i++) {
//                if (chuPaiSeatAry[i] == 1) {
//                    chuPaiTljBeginIndex = i;
//                    break;
//                }
//            }
//            List<List<Integer>> fenzuList = handFenZu(handPais, zhuColor);
//            int co = index0color - 1;
//            List<Integer> fulist = fenzuList.get(co);//对应颜色的副牌
//            if (fulist.size() >= 1) {
//                int[] nextPlaySeatAry = turnFuPaiToAry(fulist, zhuColor);//10,j q k A
//                 //System.out.println("k副单出牌：" + StringUtil.implode(chuPaiSeatAry));
//                 //System.out.println("k副单下家：" + StringUtil.implode(nextPlaySeatAry));
//                for (int i = 0; i < nextPlaySeatAry.length; i++) {
//                    if (nextPlaySeatAry[i] >= 1 && i > chuPaiTljBeginIndex) {
//                        removeListParam(handPai,10+co*100,1);
//                        return true;
//                    }
//                }
//            } else if (fulist.size() == 0) {
//                //自己没副  出主能否打起
//                fulist=fenzuList.get(4);
//                int[] nextPlaySeatAry =turnZhuPaiToAryDuiZiAndDan(fulist,zhuColor);//10,j q k A   副2  正2  小王 大王
//                //System.out.println("副单出牌："+ StringUtil.implode(chuPaiSeatAry));
//                //System.out.println("主单下家："+ StringUtil.implode(nextPlaySeatAry));
//                for(int i=0;i<nextPlaySeatAry.length;i++){
//                    if(nextPlaySeatAry[i]>=1){
//                        removeListParam(handPai,fulist.get(0),1);
//                        return true;
//                    }
//                }
//            } else {
//                return false;
//            }
//
//        }
//        return false;
//    }


    /**
     * 把副牌转成数组判断拖拉机
     *
     * @param colorlist
     * @return
     */
    private static int[] turnFuPaiToAry(List<Integer> colorlist, int zhuColor) {
        int[] ay = {0, 0, 0, 0, 0};//10,j q k A
        for (int p : colorlist) {
            int val = CardUtils.loadCardValue(p);
            int col = CardUtils.loadCardColor(p);
            if (col != zhuColor && val != 1 && val != 2 && val != 15) {
                ay[val - 10] = ay[val - 10] + 1;
            }
        }
        return ay;
    }

    private static int[] RemoveDuiAndturnFuPaiToAry(List<Integer> colorlist, int zhuColor) {
        int[] ay = {0, 0, 0, 0, 0};//10,j q k A
        for (int p : colorlist) {
            int val = CardUtils.loadCardValue(p);
            int col = CardUtils.loadCardColor(p);
            if (col != zhuColor && val != 1 && val != 2 && val != 15) {
                ay[val - 10] = ay[val - 10] + 1;
            }
        }

        return ay;
    }

    /**
     * 把主牌转成数组判断拖拉机
     *
     * @param colorlist
     * @return
     */
    private static int[] turnZhuPaiToAry(List<Integer> colorlist, int zhuColor) {
        int[] ay = {0, 0, 0, 0, 0, 0, 0, 0, 0};//10,j q k A   副2  正2  小王 大王
        List<Integer> fu2 = new ArrayList<>();
        for (int p : colorlist) {
            int val = CardUtils.loadCardValue(p);
            int color = CardUtils.loadCardColor(p);
//			有主花色时,主A副2主2小王大王可组成姊妹对。无主时,只有大王和	小王可组成姊妹对。
            if (zhuColor > 0) {
                if (val == 15 && zhuColor == CardUtils.loadCardColor(p)) {
                    //主2下标
                    ay[6] = ay[6] + 1;
                } else if (val == 15 && (zhuColor != CardUtils.loadCardColor(p) || zhuColor == 0)) {
                    ay[5] = ay[5] + 1;//副2
                } else if (val == 2) {
                    ay[8] = ay[8] + 1;//大王
                } else if (val == 1) {
                    ay[7] = ay[7] + 1;//小王
                } else {
                    ay[val - 10] = ay[val - 10] + 1;
                }
            } else {
                if (val == 15 && zhuColor == CardUtils.loadCardColor(p)) {
                    //主2下标
                    ay[6] = ay[6] + 1;
                } else if (val == 15 && (zhuColor != CardUtils.loadCardColor(p) || zhuColor == 0)) {
                    ay[5] = ay[5] + 1;//副2
                }
            }
            if(val==15 && color!=zhuColor){
                fu2.add(p);
            }
        }
         //System.out.println(StringUtil.implode(ay,","));

        return ay;
    }
  private static int[] turnZhuPaiToAryTLJ(List<Integer> colorlist, int zhuColor) {
        int[] ay = {0, 0, 0, 0, 0, 0, 0, 0, 0};//10,j q k A   副2  正2  小王 大王
        List<Integer> fu2 = new ArrayList<>();
        for (int p : colorlist) {
            int val = CardUtils.loadCardValue(p);
            int color = CardUtils.loadCardColor(p);
//			有主花色时,主A副2主2小王大王可组成姊妹对。无主时,只有大王和	小王可组成姊妹对。
            if (zhuColor > 0) {
                if (val == 15 && zhuColor == CardUtils.loadCardColor(p)) {
                    //主2下标
                    ay[6] = ay[6] + 1;
                } else if (val == 15 && (zhuColor != CardUtils.loadCardColor(p) || zhuColor == 0)) {
                    ay[5] = ay[5] + 1;//副2
                } else if (val == 2) {
                    ay[8] = ay[8] + 1;//大王
                } else if (val == 1) {
                    ay[7] = ay[7] + 1;//小王
                } else {
                    ay[val - 10] = ay[val - 10] + 1;
                }
            } else {
                if (val == 15 && zhuColor == CardUtils.loadCardColor(p)) {
                    //主2下标
                    ay[6] = ay[6] + 1;
                } else if (val == 15 && (zhuColor != CardUtils.loadCardColor(p) || zhuColor == 0)) {
                    ay[5] = ay[5] + 1;//副2
                }
            }
            if(val==15 && color!=zhuColor){
                fu2.add(p);
            }
        }
         //System.out.println(StringUtil.implode(ay,","));
        //10,j q k A   副2  正2  小王 大王
        if(ay[5]>=2){
            //检查附二花色是否成对
            List<Integer> fu2Dui = CardUtils.getDuiCards(fu2);
            if(null==fu2Dui || fu2Dui.size()<=0){
                ay[5]=0;
            }
        }
        return ay;
    }

    private static int[] RemoveDuiTurnZhuPaiToAry(List<Integer> colorlist2, int zhuColor) {
        List<Integer> colorlist = new ArrayList<>(colorlist2);
        //移除副2对子 单独对子
        List<Integer> removelist = new ArrayList<>();
        for (int i : colorlist) {
            int num = getPnum(colorlist2, i);
            if (num == 2) {
                removelist.add(i);
            }
        }
        colorlist.removeAll(removelist);

        int[] ay = {0, 0, 0, 0, 0, 0, 0, 0, 0};//10,j q k A   副2  正2  小王 大王
        for (int p : colorlist) {
            int val = CardUtils.loadCardValue(p);
//			有主花色时,主A副2主2小王大王可组成姊妹对。无主时,只有大王和	小王可组成姊妹对。
            if (zhuColor > 0) {
                if (val == 15 && zhuColor == CardUtils.loadCardColor(p)) {
                    //主2下标
                    ay[6] = ay[6] + 1;
                } else if (val == 15 && (zhuColor != CardUtils.loadCardColor(p) || zhuColor == 0)) {
                    ay[5] = ay[5] + 1;//副2
                } else if (val == 2) {
                    ay[8] = ay[8] + 1;//大王
                } else if (val == 1) {
                    ay[7] = ay[7] + 1;//小王
                } else {
                    ay[val - 10] = ay[val - 10] + 1;
                }
            } else {
                if (val == 15 && zhuColor == CardUtils.loadCardColor(p)) {
                    //主2下标
                    ay[6] = ay[6] + 1;
                } else if (val == 15 && (zhuColor != CardUtils.loadCardColor(p) || zhuColor == 0)) {
                    ay[5] = ay[5] + 1;//副2
                }
            }
        }
         //System.out.println(StringUtil.implode(ay,","));
        return ay;
    }

    private static int getPnum(List<Integer> colorlist2, int i) {
        int m = 0;
        for (int n : colorlist2) {
            if (n == i) {
                m++;
            }
        }
        return m;
    }


    private static int[] turnZhuPaiToAryDuiZiAndDan(List<Integer> colorlist, int zhuColor) {
        int[] ay = {0, 0, 0, 0, 0, 0, 0, 0, 0};//10,j q k A   副2  正2  小王 大王
        for (int p : colorlist) {
            int val = CardUtils.loadCardValue(p);
            if (val == 15 && zhuColor == CardUtils.loadCardColor(p)) {
                //主2下标
                ay[6] = ay[6] + 1;
            } else if (val == 15 && (zhuColor != CardUtils.loadCardColor(p) || zhuColor == 0)) {
                ay[5] = ay[5] + 1;//副2
            } else if (val == 2) {
                ay[8] = ay[8] + 1;//大王
            } else if (val == 1) {
                ay[7] = ay[7] + 1;//小王
            } else {
                ay[val - 10] = ay[val - 10] + 1;
            }
        }
         //System.out.println(StringUtil.implode(ay,","));
        return ay;
    }

    /**
     * 甩牌分组
     *
     * @param hands
     * @param zhuColor
     * @return
     */
    public static List<List<Integer>> handFenZu(List<Integer> hands, int zhuColor) {
        //黑红梅方
        List<List<Integer>> list = new ArrayList<>();
        List<Integer> blist = new ArrayList<>();
        List<Integer> rlist = new ArrayList<>();
        List<Integer> mlist = new ArrayList<>();
        List<Integer> flist = new ArrayList<>();
        List<Integer> zlist = new ArrayList<>();
        //方片 1 梅花2 红桃3 黑桃4  5王
        if (zhuColor > 0) {
            for (int p : hands) {
                if (CardUtils.loadCardColor(p) == 1 && !CardUtils.isZhu(p, zhuColor)) {
                    flist.add(p);
                }
                if (CardUtils.loadCardColor(p) == 2 && !CardUtils.isZhu(p, zhuColor)) {
                    mlist.add(p);
                }
                if (CardUtils.loadCardColor(p) == 3 && !CardUtils.isZhu(p, zhuColor)) {
                    rlist.add(p);
                }
                if (CardUtils.loadCardColor(p) == 4 && !CardUtils.isZhu(p, zhuColor)) {
                    blist.add(p);
                }
                if (CardUtils.loadCardColor(p) == zhuColor || CardUtils.isZhu(p, zhuColor)) {
                    zlist.add(p);
                }
            }
        } else {
            for (int p : hands) {
                if (CardUtils.loadCardColor(p) == 1 && !CardUtils.isZhu(p, zhuColor)) {
                    flist.add(p);
                }
                if (CardUtils.loadCardColor(p) == 2 && !CardUtils.isZhu(p, zhuColor)) {
                    mlist.add(p);
                }
                if (CardUtils.loadCardColor(p) == 3 && !CardUtils.isZhu(p, zhuColor)) {
                    rlist.add(p);
                }
                if (CardUtils.loadCardColor(p) == 4 && !CardUtils.isZhu(p, zhuColor)) {
                    blist.add(p);
                }
//				if (CardUtils.loadCardColor(p) == zhuColor || CardUtils.loadCardColor(p) == 5 && zhuColor>0 ) {
//					zlist.add(p);
//				}
                //无主
                if (CardUtils.loadCardValue(p) == 15 || CardUtils.loadCardValue(p) == 1 || CardUtils.loadCardValue(p) == 2) {
                    zlist.add(p);
                }

            }
        }
        //方片 1 梅花2 红桃3 黑桃4  5王
        list.add(flist);
        list.add(mlist);
        list.add(rlist);
        list.add(blist);
        list.add(zlist);
        return list;


    }

    /**
     * 返回最小的拖拉机牌
     *
     * @param handAry
     * @return
     */
    public static List<Integer> isContainTuoLaJi(int[] handAry) {
        // 10 j q k a fu2  zheng2
//		for (int i =0; i < handAry.length-1; i++) {
//			if (handAry[i] >= 2 && handAry[i + 1] >= 2 ) {
//				return i + 10;
//			}
//		}
        List<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(0);
        for (int i = 0; i < handAry.length - 1; i++) {
            if (handAry[i] >= 2 && handAry[i + 1] >= 2) {
                list.set(0, i + 10);
                list.set(1, 2);
                if (i + 2 < handAry.length - 1 && handAry[i + 2] >= 2) {
                    list.set(1, 3);
                    if (i + 3 < handAry.length - 1 && handAry[i + 3] >= 2) {
                        list.set(1, 4);
                        if (i + 4 < handAry.length - 1 && handAry[i + 4] >= 2) {
                            list.set(1, 5);
                            if (i + 5 < handAry.length - 1 && handAry[i + 5] >= 2) {
                                list.set(1, 6);
                                if (i + 6 < handAry.length - 1 && handAry[i + 6] >= 2) {
                                    list.set(1, 7);
                                    return list;
                                } else {
                                    return list;
                                }
                            } else {
                                return list;
                            }
                        } else {
                            return list;
                        }
                    } else {
                        return list;
                    }
                } else {
                    return list;
                }
            }

        }
        return list;
    }

    public static int isContainDuiZi(int[] handAry) {
        // 10 j q k a fu2  zheng2
        //干掉拖拉机
        for (int i = 0; i < handAry.length - 1; i++) {
            if (handAry[i] >= 2 && handAry[i + 1] >= 2) {
                handAry[i] = 0;
                handAry[i + 1] = 0;
            }
        }
        for (int i = 0; i < handAry.length; i++) {
            if (handAry[i] >= 2) {
                return i + 10;
            }
        }
        return 0;
    }
    public static int getContainMaxDuiZi(int[] handAry) {
        // 10 j q k a fu2  zheng2
        //干掉拖拉机
//        for (int i = 0; i < handAry.length - 1; i++) {
//            if (handAry[i] >= 2 && handAry[i + 1] >= 2) {
//                handAry[i] = 0;
//                handAry[i + 1] = 0;
//            }
//        }
        for (int i = handAry.length-1; i >=0; i--) {
            if (handAry[i] >= 2) {
                return i+10;
            }
        }
        return 0;
    }

    public static int isContainDan(int[] handAry) {
        // 10 j q k a fu2  zheng2
        for (int i = 0; i < handAry.length; i++) {
            if (handAry[i] == 1) {
                return i + 10;
            }
        }
        return 0;
    }

    public static int isContainDan2(int[] handAry) {
        // 10 j q k a fu2  zheng2
        for (int i = 0; i < handAry.length; i++) {
            if (handAry[5] >= 1) {
                return 15;
            }
            if (handAry[i] == 1) {
                return i + 10;
            }
        }
        return 0;
    }

    public static int ContainTuoLaJiGetMinCard(int[] handAry) {
        //如果前一个index=2 后一个index=2 那么就存在拖拉机
        int re = 0;
        for (int i = handAry.length; i <= 1; i--) {
            if (handAry[i] >= 2 && handAry[i - 1] >= 2) {
                return i;
            }
        }
        return re;
    }

    //农民取最大的一个拖拉机
    public static int ContainTuoLaJiGetMaxCard(int[] handAry) {
        //如果前一个index=2 后一个index=2 那么就存在拖拉机
        int re = 0;
        for (int i = 1; i < handAry.length - 1; i--) {
            if (handAry[i] >= 2 && handAry[i + 1] >= 2) {
                return i + 1;
            }
        }
        return re;
    }


    public static int AllDuiGetMinCard(int[] handAry) {
        //对子中最小的对子
        int re = 0;
        for (int i = handAry.length; i <= 1; i--) {
            if (handAry[i] >= 2) {
                return i;
            }
        }
        return re;
    }

    /**
     * 是否可以出
     *
     * @param list
     * @param type
     * @param handCards
     * @return
     */
    private static int canChuPai(List<Integer> list, int type, List<Integer> handCards,List<Integer> deskPai,int zhuColor) {
        int hDui = CardUtils.hasDuiCount(handCards);
        int cDui = CardUtils.hasDuiCount(list);
        //有对不出对
        if (type == CardType.DUI) {
            if (hDui > 0 && cDui == 0) {
                return -1;
            }
        } else if (type == CardType.TUOLAJI) {
            int duiCount = list.size() / 2;
            int sDui = hDui > duiCount ? duiCount : hDui;
            if (sDui > cDui && hDui != 100) {
                return -1;
            }
        }else if(type >=5){
            //如果出牌带对子 垫牌手中有对必须出对
            List<Integer> deskPaiCopy = new ArrayList<>(deskPai);

            //桌牌对子数量
            int deskDuiNum = CardUtils.hasDuiNum(deskPaiCopy);

            List<Integer> dianpai = CardUtils.getDianCards3(handCards,zhuColor);
            int dianpaiDuiNum = CardUtils.hasDuiNum(dianpai);
            if(deskDuiNum>0 && dianpaiDuiNum>0 && cDui==0){
                // 情况1：甩牌带对子 自己垫牌有对不出对子
                return -1;
            }
            if(deskDuiNum>0 && cDui>0){
                if(dianpaiDuiNum >=deskDuiNum && cDui<deskDuiNum){
                    //情况2：首家甩中有2个对子 自己能垫2个 但是只垫了1个
                    return -1;
                }
            }

        }
        return 0;
    }

    public static void main(String args[]) {
        List<Integer> chu = Arrays.asList(314, 314, 313, 313, 311,311);//
        List<Integer> jie = Arrays.asList(315, 315, 415, 415, 310,310);//
        int zhu = 3;
      int a =  ComparaShuaiPai2(chu,jie,zhu,1,2,chu);
        System.err.println("a："+a);
//        int[] a1 = {2,0,0,0,1};
//


    }

    private static void test2AA() {
        {
            List<Integer> chuPaiSeatTuoLaJi = new ArrayList<>();
            chuPaiSeatTuoLaJi.add(215);
            chuPaiSeatTuoLaJi.add(214);
            chuPaiSeatTuoLaJi.add(214);


            //4321
            int zhuColor = 3;
            List<Integer> nextChuPaiSeatTuoLaJi = new ArrayList<>();

            nextChuPaiSeatTuoLaJi.add(215);
            nextChuPaiSeatTuoLaJi.add(213);
            nextChuPaiSeatTuoLaJi.add(210);

            //回合结束 能否毙掉上家甩牌
           // int a = ComparaShuaiPai(chuPaiSeatTuoLaJi,nextChuPaiSeatTuoLaJi,zhuColor,1,2);
            //System.out.println(" 1 test2AA winSeat = "+a);
        }
    }

    private static void testAAQQ() {
        List<Integer> chuPaiSeatTuoLaJi = new ArrayList<>();
        chuPaiSeatTuoLaJi.add(214);
        chuPaiSeatTuoLaJi.add(214);
        chuPaiSeatTuoLaJi.add(212);
        chuPaiSeatTuoLaJi.add(212);


        //4321
        int zhuColor = 4;
        List<Integer> nextChuPaiSeatTuoLaJi = new ArrayList<>();

        nextChuPaiSeatTuoLaJi.add(412);
        nextChuPaiSeatTuoLaJi.add(413);
        nextChuPaiSeatTuoLaJi.add(414);
        nextChuPaiSeatTuoLaJi.add(501);

        //回合结束 能否毙掉上家甩牌
        //int a = ComparaShuaiPai(chuPaiSeatTuoLaJi,nextChuPaiSeatTuoLaJi,zhuColor,1,2);
        //System.out.println(" 1 AAQQ winSeat = "+a);
    }
    private static void testAQ() {
        List<Integer> chuPaiSeatTuoLaJi = new ArrayList<>();
        chuPaiSeatTuoLaJi.add(414);
        chuPaiSeatTuoLaJi.add(412);


        //4321
        int zhuColor = 4;
        List<Integer> nextChuPaiSeatTuoLaJi = new ArrayList<>();

        nextChuPaiSeatTuoLaJi.add(411);
        nextChuPaiSeatTuoLaJi.add(501);

        //回合结束 能否毙掉上家甩牌
        //int a = ComparaShuaiPai(chuPaiSeatTuoLaJi,nextChuPaiSeatTuoLaJi,zhuColor,1,2);
        //System.out.println("2 AQ winSeat = "+a);
    }

    private static void test() {
        List<Integer> chuPaiSeatTuoLaJi = new ArrayList<>();
        chuPaiSeatTuoLaJi.add(210);
        chuPaiSeatTuoLaJi.add(210);
        chuPaiSeatTuoLaJi.add(211);
        chuPaiSeatTuoLaJi.add(211);

        chuPaiSeatTuoLaJi.add(214);
        chuPaiSeatTuoLaJi.add(214);
        chuPaiSeatTuoLaJi.add(213);
        chuPaiSeatTuoLaJi.add(213);
//
//
        //4321
        int zhuColor = 4;
        List<Integer> nextChuPaiSeatTuoLaJi = new ArrayList<>();
        nextChuPaiSeatTuoLaJi.add(411);
        nextChuPaiSeatTuoLaJi.add(411);
        nextChuPaiSeatTuoLaJi.add(412);
        nextChuPaiSeatTuoLaJi.add(412);

        nextChuPaiSeatTuoLaJi.add(413);
        nextChuPaiSeatTuoLaJi.add(413);
        nextChuPaiSeatTuoLaJi.add(414);
        nextChuPaiSeatTuoLaJi.add(501);

        //回合结束 能否毙掉上家甩牌
        //int a = ComparaShuaiPai(chuPaiSeatTuoLaJi,nextChuPaiSeatTuoLaJi,zhuColor,1,2);
        //System.out.println(" 1 winSeat = "+a);
    }

    private static  int  CompareShuaiPai( List<Integer> chu, List<Integer> jie,int zhuColor,int nextSeat,int winseat){

        CardType cardType =CardTool.getCardType(chu,zhuColor,1);
        // System.err.println(cardType.getType());
        List<Integer> chuDui =CardUtils.getDuiCards(chu);
        List<Integer> jieDui =CardUtils.getDuiCards(jie);
        if(chuDui.size()>=2){
            if(jieDui.size()>=2){
              int chuMinCard =  CardUtils.getMinCard(chuDui,zhuColor);
               int jieMaxCard = CardUtils.getMaxCard(jieDui,zhuColor);
                //接牌的人中。最大的对子能否打得起上家最小的对子
                boolean re = CardUtils.comCardValue(jieMaxCard,chuMinCard,zhuColor);
               if(re){
                   return nextSeat;
               }else{
                    return winseat;
               }
            }else{
                 return winseat;
            }
        }else{
            //单
            int chuMinCard =  CardUtils.getMinCard(chu,zhuColor);
            int jieMaxCard = CardUtils.getMaxCard(jie,zhuColor);
            boolean re = CardUtils.comCardValue(chuMinCard,jieMaxCard,zhuColor);
            if(re){
               return winseat ;
            }else{
                return nextSeat ;
            }
        }
    }
    public static CardType getCardType(List<Integer> list, int zhuColor, int chulongpai) {
        CardType ct;

        if (list.size() == 1) {
            ct = new CardType(CardType.DAN, list);
        } else if (list.size() == 8 && isLongPai(list, chulongpai)) {
            ct = new CardType(CardType.LONGPAI, list);
        } else {
            if (list.size() == 2) {
                ct = new CardType(CardType.DUI, list);
                int color1 = CardUtils.loadCardColor(list.get(0));
                int color2 = CardUtils.loadCardColor(list.get(1));
                if (color1 != color2 || !list.get(0).equals(list.get(1))) {
                    if (allZhu(list, zhuColor)) {
                        ct.setType(CardType.SHUAIPAI);
                    } else {
                        ct.setType(CardType.SHUAIPAI2);
                    }
                }
            } else {
                //拖拉机或者甩主
                ct = CardUtils.isTuoLaji(list, zhuColor);
                if (ct.getType() == CardType.TUOLAJI) {
                    ct.setType(CardType.TUOLAJI);
                }
                if (ct.getType() == CardType.SHUAIPAI) {
                    if (!allZhu(list, zhuColor)) {
                        ct.setType(CardType.SHUAIPAI2);
                    }
                }
            }

        }
        return ct;
    }

    /**
     * 是否为龙牌 8个相同点数
     *
     * @param list
     * @return
     */
    public static boolean isLongPai(List<Integer> list, int chulong) {
        if (0 == chulong) {
            return false;
        }
        if (list.size() != 8) {
            return false;
        }
        int index = CardUtils.loadCardValue(list.get(0));

        for (Integer id : list) {
            if (CardUtils.loadCardValue(id) != index) {
                return false;
            }
        }
        return true;
    }

    public static boolean allZhu(List<Integer> list, int zhuColor) {
        for (Integer id : list) {
            if (!CardUtils.isZhu(id, zhuColor)) {
                return false;
            }
        }
        return true;
    }

    public static boolean haveZhu(List<Integer> list, int zhuColor) {
        for (Integer id : list) {
            if (CardUtils.isZhu(id, zhuColor)) {
                return true;
            }
        }
        return false;
    }

    public static int getBaofuValue(int seat) {
        int res = 0;
        switch (seat) {
            case 1:
                res = 1;
                break;
            case 2:
                res = 10;
                break;
            case 3:
                res = 100;
                break;
            case 4:
                res = 1000;
                break;
            default:
                break;
        }
        return res;
    }


    /**
     * 获取叫分是几档
     *
     * @param jiaofen
     * @return
     */
    public static int getDang(int jiaofen) {
        if (jiaofen > 50) {
            return 1;
        }
        if (jiaofen > 30 && jiaofen <= 50) {
            return 2;
        }
        if (jiaofen <= 30) {
            return 3;
        }
        return 1;
    }


    public static CardType getTunWin(HashMap<Integer, CardType> map, int turnFirst, int zhuColor) {

        int winSeat = turnFirst;
        CardType winType = map.get(winSeat);
        List<Integer> fenCards = new ArrayList<Integer>();
        addScore(winType, fenCards);

        boolean isallDan = isallDan(winType.getCardIds());
        int size = map.size();
        int nextS = winSeat;

        for (int i = 0; i < size - 1; i++) {
            nextS += 1;
            if (nextS > size) {
                nextS = 1;
            }

            CardType ct = map.get(nextS);
            if (ct == null) {
                continue;
            }

            addScore(ct, fenCards);
            List<Integer> cards = ct.getCardIds();
            int color = CardUtils.loadCardColor(cards.get(0));
            int card = cards.get(cards.size() - 1);
            int card2 = cards.get(0);
            if (CardUtils.comCardValue(card2, card, zhuColor)) {
                card = card2;
            }
            int winCard = winType.getCardIds().get(winType.getCardIds().size() - 1);
            int winColor = CardUtils.loadCardColor(winType.getCardIds().get(0));

            //甩牌 不同牌型大小比较3,
            if(winType.getType()>=5){
                //都是散单牌
                if(isallDan){
                    //首家全是散单牌 最大
                    boolean winSameColor =CardUtils.isSameColor(winType.getCardIds());
                    boolean winTypeIsAllFu = !haveZhu(new ArrayList<>(winType.getCardIds()),zhuColor);
                    int WinMaxCard = CardUtils.getMaxCard(winType.getCardIds(),zhuColor);
                    boolean winTypeIsAllzhu = CardTool.allZhu(new ArrayList<>(winType.getCardIds()),zhuColor);
                    boolean ctTypeIsAllFu = !haveZhu(new ArrayList<>(ct.getCardIds()),zhuColor);
                    boolean ctSameColor =!CardUtils.isSameColor(ct.getCardIds());
                    boolean ctTypeIsAllzhu = CardTool.allZhu(new ArrayList<>(ct.getCardIds()),zhuColor);
                    //比较两个最小牌
                    //比较逻辑 先判断win是甩副 ct跟甩副 winSeat=nextS;
                    if(winTypeIsAllFu && ctTypeIsAllFu && winSameColor && ctSameColor && ctSameColor && color==winColor ){
                        //甩副接甩副
                         //System.out.println("winSeat:WinMinCard:"+WinMinCard);
                        int ctMaxCard = CardUtils.getMaxCard(ct.getCardIds(),zhuColor);
                         //System.out.println("nextS:ctMaxCard:"+ctMaxCard);
                        if(CardUtils.comCardValue(WinMaxCard, ctMaxCard, zhuColor)){
                            continue;
                        }else{
                            winSeat = nextS;
                            winType = ct;
                            continue;
                        }
                    }else if(ctTypeIsAllzhu && winTypeIsAllzhu){
                         //System.out.println("甩主接甩主");
                         //System.out.println("winSeat:WinMinCard:"+WinMinCard);
                        int ctMaxCard = CardUtils.getMaxCard(ct.getCardIds(),zhuColor);
                         //System.out.println("nextS:ctMaxCard:"+ctMaxCard);
                        if(CardUtils.comCardValue(WinMaxCard, ctMaxCard, zhuColor)){
                            continue;
                        }else{
                            winSeat = nextS;
                            winType = ct;
                            continue;
                        }
                    }else if(winTypeIsAllFu && ctTypeIsAllzhu) {
                        winSeat = nextS;
                        winType = ct;
                        continue;
                    }else {
                        //上家甩的全是主 下家主带副 =(winTypeIsAllzhu && !ctTypeIsAllzhu && !ctTypeIsAllFu)
                        continue;
                    }
                }else{

                    CardType ctcopy = new CardType(ct.getType(),ct.getCardIds());
                    int winSeat2 = ComparaShuaiPai(winType.getCardIds(),ct.getCardIds(),zhuColor,winSeat,nextS);
                    if(winSeat2!=winSeat){
                        winSeat = nextS;
                        winType = ctcopy;
                        // System.err.println("aaaaaa上甩副下甩主 主："+zhuColor+" 赢家座位号："+winSeat);
                        continue;
                    }else{
                        continue;//winseat无变化 继续循环判断
                    }
                }

            }

            //同牌型
            if (winType.getType() == ct.getType() && !CardUtils.comCardValue(winCard, card, zhuColor) && winType.getType()!=CardType.LONGPAI) {
                winSeat = nextS;
                winType = ct;
            }
            if (winType.getType() == CardType.LONGPAI && ct.getType() == CardType.LONGPAI) {
                if (CardUtils.loadCardValue(cards.get(0)) > CardUtils.loadCardValue(winType.getCardIds().get(0))) {
                    winSeat = nextS;
                    winType = ct;
                }
            }
        }
        CardType result = new CardType(winSeat, fenCards);
        return result;
    }

    /**
     *
     * @param map
     * @param turnFirst
     * @param zhuColor
     * @return CardType 获取当轮最大的牌的玩家位置和牌
     */
 public static CardType getTunWin2(HashMap<Integer, CardType> map, int turnFirst, int zhuColor) {

        int winSeat = turnFirst;
        CardType winType = map.get(winSeat);
        List<Integer> fenCards = new ArrayList<Integer>();
         List<Integer> firstCards = new ArrayList<>(winType.getCardIds());
        CardType firstType = map.get(winSeat);
        int size = map.size();
        int nextS = winSeat;
        addScore(winType,fenCards);
        for (int i = 0; i < size - 1; i++) {
            nextS += 1;
            if (nextS > size) {
                nextS = 1;
            }

            CardType ct = map.get(nextS);
            if (ct == null) {
                continue;
            }
            addScore(ct,fenCards);
            List<Integer> cards = ct.getCardIds();
            int color = CardUtils.loadCardColor(cards.get(0));
            int card = cards.get(cards.size() - 1);
            int card2 = cards.get(0);
            if (CardUtils.comCardValue(card2, card, zhuColor)) {
                card = card2;
            }

            int winCard = winType.getCardIds().get(winType.getCardIds().size() - 1);
            int winColor = CardUtils.loadCardColor(winType.getCardIds().get(0));
            // System.err.println(winSeat+ "上家："+CardUtils.toStringCards(winType.getCardIds()));
            // System.err.println(nextS+ "下家："+CardUtils.toStringCards(ct.getCardIds()));
            // System.err.println("主："+zhuColor);


            if (winType.getType()<5 && firstType.getType()==ct.getType() && winType.getType() == ct.getType() && !CardUtils.comCardValue(winCard, card, zhuColor) && winType.getType()!=CardType.LONGPAI) {
                winSeat = nextS;
                winType = ct;
                 //System.err.println(" 同牌比较 主："+zhuColor+" 赢家座位号："+winSeat);
                 continue;
            }else{
                //拖拉机比较
                if(firstType.getType() == CardType.TUOLAJI && ct.getType()!=CardType.TUOLAJI){
                    //第一家为拖拉机
                    //System.err.println("第一家为拖拉机：");
                    continue;
                }
                CardType ctcopy = new CardType(ct.getType(),ct.getCardIds());
                int winSeat2 = ComparaShuaiPai2(winType.getCardIds(),ct.getCardIds(),zhuColor,winSeat,nextS,firstCards);
                if(winSeat2!=winSeat){
                    winSeat = nextS;
                    winType = ctcopy;
                     //System.err.println(" 甩牌比较 主："+zhuColor+" 赢家座位号："+winSeat);
                    continue;
                }
            }

//            if (winType.getType() == CardType.LONGPAI && ct.getType() == CardType.LONGPAI) {
//                if (CardUtils.loadCardValue(cards.get(0)) > CardUtils.loadCardValue(winType.getCardIds().get(0))) {
//                    winSeat = nextS;
//                    winType = ct;
//                }
//            }
        }

//        fenCards =map.get(winSeat).getCardIds();
        CardType result = new CardType(winSeat, fenCards);
        // System.err.println("当前轮次最大牌玩家位置："+winSeat);
        // System.err.println("当前轮次最大牌玩家出牌："+CardUtils.toStringCards(map.get(winSeat).getCardIds()));
        return result;
    }

    /**
     *
     * @param shuaiCardIds
     * @param ctCardId
     * @param zhuColor
     * @param winSeat
     * @param nextS
     * @return 最大甩牌玩家座位号
     */
    private static int ComparaShuaiPai(List<Integer> shuaiCardIds, List<Integer> ctCardId, int zhuColor,int winSeat,int nextS) {
        if(shuaiCardIds.size()==0){
            return winSeat;
        }
        List<Integer>  ctCardIds   = new ArrayList<>(ctCardId);
        boolean ctTypeIsAllzhu = CardTool.allZhu(ctCardIds, zhuColor);
        boolean ctTypeIsAllFu = !haveZhu(new ArrayList<>(ctCardIds), zhuColor);
        //0  ct 接甩牌的人有主有副
        if (!ctTypeIsAllzhu && !ctTypeIsAllFu) {
            return winSeat;
        }
        List<Integer> hand = new ArrayList<>(shuaiCardIds);
        Map<String, Object> result = new HashMap<>();
        result.put("score", 0);
        result.put("Cards", hand);
        List<List<Integer>> fenzuList = handFenZu(hand, zhuColor);
        //拖拉机
        List<Integer> chuPaiSeatTuoLaJi = checkShuaiPaiContainTuoLaJi(fenzuList, zhuColor);
        int tljlength = chuPaiSeatTuoLaJi.size() / 2;
        boolean killtuolaji = true;
        if (chuPaiSeatTuoLaJi.size() >= 4) {
            killtuolaji = false;
            boolean canOut = checkNextPlayersCanKillShuaiTuoLaJi(chuPaiSeatTuoLaJi, ctCardIds, zhuColor, tljlength);
            if (canOut) {
                killtuolaji = true;
                shuaiCardIds.removeAll(chuPaiSeatTuoLaJi);
            }else{
                return winSeat;
            }
        }
        // 对子
        List<Integer> chuPaiSeatDuiZi = checkShuaiPaiContainDuiZi(fenzuList, zhuColor);
        boolean killdui = true;
        if (chuPaiSeatDuiZi.size() >= 2) {
            killdui = false;
            boolean canOut = checkNextPlayersCanKillShuaiDui(chuPaiSeatDuiZi, ctCardIds, zhuColor);
            if (canOut) {
                killdui = true;
            }else{
                return winSeat;
            }
            List<Integer> shuaiCardIds2= new ArrayList<>(shuaiCardIds);
            shuaiCardIds2.removeAll( chuPaiSeatDuiZi);
            shuaiCardIds=shuaiCardIds2;
        }

        // List<Integer> chuPaiSeatDan = checkShuaiPaiContainDan(fenzuList, zhuColor);
//        1、甩多个纯单牌：要毙掉必须满足甩牌数量相同的主牌（副和主混一起是毙不了的）
//        在毙牌中比较大小则只按最大单牌比较(毙牌的对子也相当于拆成单牌处理）
        boolean resDan =isallDan(shuaiCardIds);
        boolean ctresDan =isallDan(ctCardIds);
        boolean killDan = true;
        if(resDan && ctresDan){
            int winmax = CardUtils.getMaxCard(shuaiCardIds,zhuColor);
            int ctmin = CardUtils.getMinCard(ctCardIds,zhuColor);
            if(CardUtils.comCardValue(ctmin,winmax,zhuColor)){
                return nextS;
            }else{
                killDan = false;
            }
        }

//        List<Integer> chuPaiSeatDan = new ArrayList<>();
//        chuPaiSeatDan.add(max);
//
//        if (chuPaiSeatDan.size() >= 1) {
//            killDan = false;
//            boolean canOut = checkNextPlayersCanKillShuaiDan(chuPaiSeatDan, ctCardIds, zhuColor);
//            if (canOut) {
//                killDan = true;
//            }else{
//                return winSeat;
//            }
//            shuaiCardIds.removeAll(chuPaiSeatDan);
//        }
        if(shuaiCardIds.size()==0){
            if (killDan && killdui && killtuolaji) {
                return nextS;
            }else{
                return  winSeat;
            }
        }else{
            if(shuaiCardIds.size()>0 && ctCardId.size()>0){
                //BUG处理 上架501 501 502 最后一家消 115 115 315 ;zhuColor=0
                return CompareShuaiPai(shuaiCardIds,ctCardId,zhuColor,nextS,winSeat);
            }
            return  ComparaShuaiPai(shuaiCardIds, ctCardIds, zhuColor, winSeat, nextS);
        }

//        if(ctTypeIsAllzhu){
//            //甩纯对子  //接的也是对子
//            List<List<Integer>> winfenzuList = handFenZu(new ArrayList<>(winType.getCardIds()), zhuColor);
//            //甩牌中带 拖拉机
//            List<Integer> chuShuaiTuoLaJi = checkShuaiPaiContainTuoLaJi(winfenzuList, zhuColor);
//
//            List<List<Integer>> ctfenzuList = handFenZu(new ArrayList<>(ct.getCardIds()), zhuColor);
//            List<Integer> ctShuaiTuoLaJi = checkShuaiPaiContainTuoLaJi(ctfenzuList, zhuColor);
//            //接甩的人也带了拖拉机
//            if( chuShuaiTuoLaJi.size()>=4 && ctShuaiTuoLaJi.size()>=0){
//                int winMaxTlj = CardUtils.getMaxCard(chuShuaiTuoLaJi,zhuColor);
//                int ctMaxTlj = CardUtils.getMaxCard(chuShuaiTuoLaJi,zhuColor);
//                if(!CardUtils.comCardValue(winMaxTlj, ctMaxTlj, zhuColor)){
//                    winSeat = nextS;
//                    //是否还有剩余的甩牌 剩余的类型。
//                    List<Integer> syWin = new ArrayList<>(winType.getCardIds());
//                    syWin.removeAll(chuShuaiTuoLaJi);
//                    List<Integer> syCt=  new ArrayList<>(ct.getCardIds());
//                    syCt.removeAll(ctShuaiTuoLaJi);
//                    if(syWin.size()>0){
//
//                    }else{
//                        // meiyou 剩余 了、接甩的拖拉机能毙掉 出甩的人的拖拉机
//                    }
//
//                }
//            }
//        }
//
//        //比较两个最小牌
//        //比较逻辑 先判断win是甩副 ct跟甩副 winSeat=nextS;
//        if(winTypeIsAllFu && ctTypeIsAllFu && winSameColor && ctSameColor && ctSameColor && color==winColor ){
//            //甩副接甩副
//             //System.out.println("winSeat:WinMinCard:"+WinMinCard);
//            int ctMaxCard = CardUtils.getMaxCard(ct.getCardIds(),zhuColor);
//             //System.out.println("nextS:ctMaxCard:"+ctMaxCard);
//            if(CardUtils.comCardValue(WinMaxCard, ctMaxCard, zhuColor)){
//            }else{
//                winSeat = nextS;
//                winType = ct;
//            }
//        }else if(ctTypeIsAllzhu && winTypeIsAllzhu){
//             //System.out.println("甩主接甩主");
//             //System.out.println("winSeat:WinMinCard:"+WinMinCard);
//            int ctMaxCard = CardUtils.getMaxCard(ct.getCardIds(),zhuColor);
//             //System.out.println("nextS:ctMaxCard:"+ctMaxCard);
//            if(CardUtils.comCardValue(WinMaxCard, ctMaxCard, zhuColor)){
//                //continue;
//            }else{
//                winSeat = nextS;
//                winType = ct;
//            }
//        }else if(winTypeIsAllFu && ctTypeIsAllzhu) {
//            winSeat = nextS;
//            winType = ct;
//        }else {
//            //上家甩的全是主 下家主带副 =(winTypeIsAllzhu && !ctTypeIsAllzhu && !ctTypeIsAllFu)
//           // continue;
//        }

    }

    /**
     * 比较当前轮次甩牌大小。按照拖拉机。对子。单排的顺序比较。
     * 任一接牌的玩家按照拖拉机对子单排的顺序能大于上家桌牌。就为当前牌桌最大出牌者
     * @param shuaiCardIds 上家
     * @param ctCardId 下家
     * @param zhuColor
     * @param winSeat 上家座位号
     * @param nextS 下家座位号
     * @return 就为当前牌桌最大出牌者座位号
     */
    private static int ComparaShuaiPai2(List<Integer> shuaiCardIds, List<Integer> ctCardId, int zhuColor,int winSeat,int nextS,List<Integer> firstCards) {
        if(shuaiCardIds.size()==0){
            return winSeat;
        }
        List<Integer>  ctCardIds   = new ArrayList<>(ctCardId);
        boolean ctTypeIsAllzhu = CardTool.allZhu(ctCardIds, zhuColor);
        boolean ctTypeIsAllFu = !haveZhu(new ArrayList<>(ctCardIds), zhuColor);
        //0  ct 接甩牌的人有主有副
        if (!ctTypeIsAllzhu && !ctTypeIsAllFu) {
            return winSeat;
        }
        //轮次第一个人出牌情况

        List<List<Integer>> firstFenzuList = handFenZu(new ArrayList<>(firstCards), zhuColor);
        List<Integer> firstChuPaiSeatTuoLaJi = checkShuaiPaiContainTuoLaJi(firstFenzuList, zhuColor);
        List<Integer> firstChuPaiSeatDuiZi = getShuaiPaiContainMaxDuiZi(firstFenzuList, zhuColor);
        boolean firstIsAllDan = isallDan(new ArrayList<>(firstCards));
        boolean firstHaveTuoLaJi = firstChuPaiSeatTuoLaJi.size()>=4?true:false;
        boolean firstHaveDui = firstChuPaiSeatDuiZi.size()>=2?true:false;
        int fisrtDuiCount = (CardUtils.getDuiCards(new ArrayList<>(firstCards)).size());
        // System.err.println("首家：firstIsAllDan "+firstIsAllDan);
        // System.err.println("首家：firstHaveTuoLaJi "+firstHaveTuoLaJi);
        // System.err.println("首家：firstHaveDui "+firstHaveDui);
        // System.err.println("首家：对子数量 "+fisrtDuiCount);
        // System.err.println("首家：  "+CardUtils.toStringCards(firstCards));
        //
        //甩牌对子size
        int shuaiDuiCount = (CardUtils.getDuiCards(new ArrayList<>(shuaiCardIds)).size());
        //出牌对子size
        int ctDuiCount = (CardUtils.getDuiCards(new ArrayList<>(ctCardIds)).size());
        // System.err.println("shuaiDuiCount：对子数量 "+shuaiDuiCount);
        // System.err.println("ctDuiCount：对子数量 "+ctDuiCount);
        List<Integer> hand = new ArrayList<>(shuaiCardIds);
        List<List<Integer>> fenzuList = handFenZu(hand, zhuColor);
        //拖拉机
        List<Integer> chuPaiSeatTuoLaJi = checkShuaiPaiContainTuoLaJi(fenzuList, zhuColor);

        List<List<Integer>> ctFenzuList = handFenZu(new ArrayList<>(ctCardIds), zhuColor);
        List<Integer>  ctTuoLaJiSize =checkShuaiPaiContainTuoLaJi(ctFenzuList, zhuColor);
        int  ctTuoLaJiSize1 = ctTuoLaJiSize.size();
        int tljlength = chuPaiSeatTuoLaJi.size() / 2;
        int tljSize = chuPaiSeatTuoLaJi.size();
        if (firstHaveTuoLaJi && chuPaiSeatTuoLaJi.size() >= 4 ) {
            boolean canOut = checkNextPlayersCanKillShuaiTuoLaJi(chuPaiSeatTuoLaJi, ctCardIds, zhuColor, tljlength);
            if (canOut && ctTuoLaJiSize1 >= tljSize &&  ctDuiCount>=fisrtDuiCount) {
                //能盖住 且接的拖拉机长度大于出牌人的拖拉机长度。且对子数量必须相等
                //shuaiCardIds.removeAll(chuPaiSeatTuoLaJi);
                return nextS;
            }else{
                return winSeat;
            }
        }

        // 对子
        List<Integer> chuPaiSeatDuiZi = getShuaiPaiContainMaxDuiZi(fenzuList, zhuColor);
        if (firstHaveDui && chuPaiSeatDuiZi.size() >= 2) {
            boolean canOut = checkNextPlayersCanKillShuaiDui(chuPaiSeatDuiZi, ctCardIds, zhuColor);
            if (canOut && (shuaiDuiCount==ctDuiCount)) {
                //能毙且对子数量相等
                return nextS;
            }else {
                return winSeat;
            }
        }

        // List<Integer> chuPaiSeatDan = checkShuaiPaiContainDan(fenzuList, zhuColor);
//        1、甩多个纯单牌：要毙掉必须满足甩牌数量相同的主牌（副和主混一起是毙不了的）
//        在毙牌中比较大小则只按最大单牌比较(毙牌的对子也相当于拆成单牌处理）
            int winmax = CardUtils.getMaxCard(shuaiCardIds,zhuColor);
            int ctmax = CardUtils.getMaxCard(ctCardIds,zhuColor);
            if(firstIsAllDan &&!CardUtils.comCardValue(winmax,ctmax,zhuColor) ){
                return nextS;
            }
            return winSeat;
    }

    /**
     * 剩余拍是否都是单排
     * @param shuaiCardIds
     * @return
     */
    private static boolean isallDan(List<Integer> shuaiCardIds) {
        if(null==shuaiCardIds || shuaiCardIds.size()==0){
            return false;
        }
        List<Integer> cop = new ArrayList<>(shuaiCardIds);
        for (int i:shuaiCardIds  ) {
             if( getPnum(cop,i)>=2){
                 return false;
            }
        }
        return true;
    }


    public static CardType getTunKill(HashMap<Integer, CardType> map, int turnFirst, int zhuColor, int maxplay) {

        int winSeat = turnFirst;
        CardType winType = map.get(winSeat);
        List<Integer> fenCards = new ArrayList<Integer>();

        int size = maxplay;
        int nextS = winSeat;

        for (int i = 0; i < size - 1; i++) {
            nextS += 1;
            if (nextS > size) {
                nextS = 1;
            }

            CardType ct = map.get(nextS);
            if (ct == null) {
                continue;
            }

            List<Integer> cards = ct.getCardIds();
            int color = CardUtils.loadCardColor(cards.get(0));
            int card = cards.get(cards.size() - 1);
            int card2 = cards.get(0);

            if (CardUtils.comCardValue(card2, card, zhuColor)) {
                card = card2;
            }
            int winCard = winType.getCardIds().get(winType.getCardIds().size() - 1);
            int winColor = CardUtils.loadCardColor(winType.getCardIds().get(0));

            // System.err.println(winSeat+ "上家："+CardUtils.toStringCards(winType.getCardIds()));
            // System.err.println(nextS+ "下家："+CardUtils.toStringCards(ct.getCardIds()));
            // System.err.println("主："+zhuColor);
            // System.err.println(CardUtils.toStringCards(winType.getCardIds()));
            //同牌型
            if (winType.getType() == ct.getType() && !CardUtils.comCardValue(winCard, card, zhuColor) && winType.getType()!=CardType.LONGPAI) {
                winSeat = nextS;
                winType = ct;
            }
            if (winType.getType() == CardType.LONGPAI && ct.getType() == CardType.LONGPAI) {
                if (CardUtils.loadCardValue(cards.get(0)) > CardUtils.loadCardValue(winType.getCardIds().get(0))) {
                    winSeat = nextS;
                    winType = ct;
                }
            }
            if(winType.getType()==6 && ct.getType()==5){
                //上甩副下甩主
                int winSeat2 =  ComparaShuaiPai(winType.getCardIds(),ct.getCardIds(),zhuColor,winSeat,nextS);
                if(winSeat2==nextS){
                    //ct下家此次比较最大
                    winType=ct;
                    winSeat=winSeat2;
                    // System.err.println("上甩副下甩主 主："+zhuColor+" 赢家座位号："+winSeat);
                }
            }
        }
        CardType result = new CardType(winSeat, fenCards);
        return result;
    }


    private static void addScore(CardType winType, List<Integer> fenCards) {
        List<Integer> scoreCards = CardUtils.getScoreCards(winType.getCardIds());
        if (scoreCards.size() > 0) {
            fenCards.addAll(scoreCards);
        }
    }


    public static List<Integer> loadCards(List<Integer> list, int val) {
        List<Integer> ret = new ArrayList<>(4);
        for (Integer integer : list) {
            if (val == CardUtils.loadCardValue(integer.intValue())) {
                ret.add(integer);
            }
        }
        return ret;
    }


    public static Map<Integer, Integer> loadCards(List<Integer> list) {
        Map<Integer, Integer> map = new TreeMap<>();
        for (Integer integer : list) {
            int val = CardUtils.loadCardValue(integer.intValue());
            int count = map.getOrDefault(val, 0);
            count++;
            map.put(val, count);
        }
        return map;
    }

    public static List<Integer> findCardIIds(List<Integer> copy, List<Integer> vals, int cardNum) {
        List<Integer> pai = new ArrayList<>();
        if (!vals.isEmpty()) {
            int i = 1;
            for (int zpId : vals) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    int paiVal = card % 100;
                    if (paiVal == zpId) {
                        pai.add(card);
                        iterator.remove();
                        break;
                    }
                }
                if (cardNum != 0) {
                    if (i >= cardNum) {
                        break;
                    }
                    i++;
                }
            }
        }
        return pai;
    }


    public static boolean check3(List<List<Integer>> list) {

        List<Integer> l = list.get(2);
        for (int a : l) {
            if (a == 403)
                return true;
        }
        return false;
    }

}
