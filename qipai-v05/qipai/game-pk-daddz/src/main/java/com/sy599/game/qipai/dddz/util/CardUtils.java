package com.sy599.game.qipai.dddz.util;

import com.sy599.game.qipai.dddz.tool.CardTool;

import java.util.*;


public final class CardUtils {

    public static final CardValue EMPTY_CARD_VALUE = new CardValue(0, 0, 0);
    public static final List<CardValue> EMPTY_CARD_VALUE_LIST = Arrays.asList(EMPTY_CARD_VALUE);


    /**
     * 计算所有牌的得分值
     *
     * @param cards
     * @return
     */
    public static int loadCardScore(int... cards) {
        int total = 0;
        for (int card : cards) {
            int val = loadCardValue(card);
            if (val == 5 || val == 10) {
                total += val;
            } else if (val == 13) {
                total += 10;
            }
        }
        return total;
    }

    /**
     * 计算所有牌的得分值
     *
     * @param cards
     * @return
     */
    public static int loadCardValueScore(CardValue... cards) {
        int total = 0;
        for (CardValue card : cards) {
            int val = card.getValue();
            if (val == 5 || val == 10) {
                total += val;
            } else if (val == 13) {
                total += 10;
            }
        }
        return total;
    }

    /**
     * 计算所有牌的得分值
     *
     * @param cards
     * @return
     */
    public static int loadCardScore(List<Integer> cards) {
        if(null==cards){
            return 0;
        }
        int total = 0;
        for (int card : cards) {
            int val = loadCardValue(card);
            if (val == 5 || val == 10) {
                total += val;
            } else if (val == 13) {
                total += 10;
            }
        }
        return total;
    }

    /**
     * 计算所有牌的得分值
     *
     * @param cards
     * @return
     */
    public static int loadCardValueScore(List<CardValue> cards) {
        int total = 0;
        for (CardValue card : cards) {
            int val = card.getValue();
            if (val == 5 || val == 10) {
                total += val;
            } else if (val == 13) {
                total += 10;
            }
        }
        return total;
    }

    /**
     * 添加分牌
     *
     * @param cards
     * @return
     */
    public static int filterCardValueScore(List<CardValue> cards, List<Integer> scoreCards) {
        int total = 0;
        for (CardValue card : cards) {
            int val = card.getValue();
            if (val == 5 || val == 10) {
                total += val;
                scoreCards.add(card.getCard());
            } else if (val == 13) {
                total += 10;
                scoreCards.add(card.getCard());
            }
        }
        return total;
    }


    public static List<Integer> getScoreCards(List<Integer> cardIds) {
        List<Integer> scoreCards = new ArrayList<Integer>();

        for (Integer id : cardIds) {
            int val = loadCardValue(id);
            if (val == 5 || val == 10) {
                scoreCards.add(id);
            } else if (val == 13) {
                scoreCards.add(id);
            }
        }
        return scoreCards;
    }


    /**
     * 计算牌型的得分值
     *
     * @param result
     * @param min    牌的个数
     * @param base   最低分
     * @param rule   记分规则
     * @return
     */
    public static int loadResultScore(Result result, int min, int base, int rule) {
        if (result.type == 100) {
            int temp = result.count - min;
            if (temp == 0) {
                return base;
            } else if (temp > 0) {
                if (rule == 1) {
                    return base + (result.count - min) * base;
                } else if (rule == 2) {
                    int ratio = 1;
                    for (int i = 0; i < temp; i++) {
                        ratio *= 2;
                    }
                    return base * ratio;
                } else {
                    return base;
                }
            }
        }
        return 0;
    }

    /**
     * 检查vals中是否包含val
     *
     * @param val
     * @param vals
     * @return
     */
    public static boolean contains(int val, int... vals) {
        for (int v : vals) {
            if (v == val) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据牌id获取CardValue
     *
     * @param card
     * @return
     */
    public static CardValue initCardValue(int card) {
        return new CardValue(card, loadCardValue(card), loadCardColor(card));
    }

    /**
     * 根据牌id获取CardValue
     *
     * @param cards
     * @return
     */
    public static List<CardValue> loadCards(List<Integer> cards) {
        List<CardValue> cardValues = new ArrayList<>(cards.size());
        for (Integer card : cards) {
            cardValues.add(initCardValue(card.intValue()));
        }
        return cardValues;
    }

    /**
     * 根据牌id获取CardValue
     *
     * @param cards
     * @return
     */
    public static List<CardValue> loadCardValues(int... cards) {
        List<CardValue> cardValues = new ArrayList<>(cards.length);
        for (int card : cards) {
            cardValues.add(initCardValue(card));
        }
        return cardValues;
    }

    /**
     * 根据牌CardValue获取id
     *
     * @param cards
     * @return
     */
    public static List<Integer> loadCardIds(List<CardValue> cards) {
        List<Integer> ids = new ArrayList<>(cards.size());
        for (CardValue card : cards) {
            ids.add(card.getCard());
        }
        return ids;
    }

    /**
     * 计算牌值(A_14,2_15,3_3...,K_13)
     *
     * @param card
     * @return
     */
    public static int loadCardValue(int card) {
        return card % 100;
    }

    public static boolean isYingZhu(int card) {
        int value = loadCardValue(card);

        if (  value == 15 || value == 1 || value == 2) {
            return true;
        }
        return false;
    }


    public static boolean isZhu(int card, int zhuColor) {
        int value = loadCardValue(card);
        int color = loadCardColor(card);
        if ( value == 15 || value == 1 || value == 2) {
            return true;
        }

        if (color == zhuColor) {
            return true;
        }


        return false;
    }

    public static  boolean isAllZhu(List<Integer> list,int zhuColor){
        if(null==list || list.isEmpty()){
            return false;
        }else{
            for (int val:list
                 ) {
                if(!isZhu(val,zhuColor)){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * @param card1
     * @param card2
     * @param zhuColor
     * @return card1>card2 返回true
     */
    public static boolean comCardValue(int card1, int card2, int zhuColor) {
        int color1 = loadCardColor(card1);
        int color2 = loadCardColor(card2);

        int value1 = loadCardValue(card1);
        int value2 = loadCardValue(card2);


        value1 = changeCardValue(value1);
        value2 = changeCardValue(value2);

        //同花色
        if (color1 == color2) {
            if (value1 >= value2) {
                return true;
            }
        } else {
            if (zhuColor > 0) {
                //第一个是主牌,第二个如果不是硬主第一个大，如果第二个是硬主那比较大小即可
                if (isZhu(card1, zhuColor)) {
                    if (!isYingZhu(card2) || value1 > value2) {
                        return true;
                    } else if (value1 == value2) {//两个都是硬主谁是正的谁大,都是副的前面的大
                        if (color1 == zhuColor || color2 != zhuColor) {
                            return true;
                        }
                    }
                } else {
                    //如果两个都不是主，那前面的大
                    if (!isZhu(card2, zhuColor)) {
                        return true;
                    }
                }
            } else {
                //无主 副牌 不同花色前面的大
                if (value1 < 15 && value2 < 15) {
                    return true;
                } else if (value1 >= value2) {//一旦有硬主比较大小即可
                    return true;
                }

            }
        }
        return false;
    }


    private static int changeCardValue(int value) {
        if (value == 1 || value == 2) {
            value += 20;
        }
        return value;

    }

    /**
     * 计算牌花色方片 1 梅花2 洪涛3 黑桃4  王5
     *
     * @param card
     * @return
     */
    public static int loadCardColor(int card) {
        return card / 100;
    }


    /**
     * 计算相同牌值的数量
     *
     * @param cardValues
     * @return
     */
    public static int countSameCards(List<CardValue> cardValues, int val) {
        int count = 0;
        for (CardValue cardValue : cardValues) {
            if (cardValue.getValue() == val) {
                count++;
            }
        }
        return count;
    }

    /**
     * 根据牌值查找牌
     *
     * @param cardValues
     * @param val        牌值
     * @param count      数量
     * @return
     */
    public static List<CardValue> searchCardValues(List<CardValue> cardValues, int val, int count) {
        List<CardValue> list = new ArrayList<>(count);
        for (CardValue cardValue : cardValues) {
            if (cardValue.getValue() == val) {
                list.add(cardValue);
                if (list.size() >= count) {
                    break;
                }
            }
        }
        return list;
    }


    /**
     * 统计相同牌值的个数
     *
     * @param cardValues
     * @return
     */
    public static final Map<Integer, Integer> countValue(List<CardValue> cardValues) {
        Map<Integer, Integer> map = new HashMap<>();
        for (CardValue cv : cardValues) {
            Integer val = map.getOrDefault(cv.getValue(), 0);
            map.put(cv.getValue(), val + 1);
        }
        return map;
    }

    public static void sortCards(List<Integer> cards) {
        Collections.sort(cards, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int val1 = loadCardValue(o1);
                val1 = changeCardValue(val1);
                int val2 = loadCardValue(o2);
                val2 = changeCardValue(val2);
                return val1 - val2;
            }
        });
    }

    public static void sortCardValues(List<CardValue> cards) {
        Collections.sort(cards, new Comparator<CardValue>() {
            @Override
            public int compare(CardValue o1, CardValue o2) {
                return o1.getValue() - o2.getValue();
            }
        });
    }


    /***
     * 拖拉机
     * @param cards
     */
    public static CardType isTuoLaji(List<Integer> cards, int zhuColor) {
        CardType ct = new CardType(0, cards);
        if (cards.size() % 2 != 0) {
            ct.setType(CardType.SHUAIPAI);
            return ct;
        }
        CardUtils.sortCards(cards);
        List<Integer> newCardIds = new LinkedList<Integer>();
        newCardIds.addAll(cards);

        //是否全是对子len / 2 - 1
        if (hasDuiCount(newCardIds) == 100 && (newCardIds.get(newCardIds.size() - 1) - newCardIds.get(0) == newCardIds.size() / 2 - 1) && (isSameColor(cards) && !isContainsYZ(cards))) {//连对 拖拉机
            ct.setType(CardType.TUOLAJI);
            ct.setCardIds(newCardIds);
        } else {
            int dui = hasDuiCount(newCardIds);
            if (dui == 100) {//全是对子
                List<Integer> proList = new ArrayList<Integer>();
                for (int i = 0; i < newCardIds.size() - 1; i++) {
                    int a = newCardIds.get(i);
                    int b = newCardIds.get(i + 1);
                    int val1 = getCardPro(a, zhuColor);
                    int val2 = getCardPro(b, zhuColor);
                    //相邻的两对不是连着的 不是拖拉机
                    if (a != b) {//&&(Math.abs(val2-val1)!=1)
                        if (!proList.contains(val1)) {
                            proList.add(val1);
                        }
                        if (!proList.contains(val2)) {
                            proList.add(val2);
                        }
//
//						break;
                    }
                }
                Collections.sort(proList);
                if (proList.size() <= 1) {
                    ct.setType(CardType.SHUAI_LIAN_DUI);
                } else {
                    for (int i = 0; i < proList.size() - 1; i++) {
                        if (Math.abs(proList.get(i) - proList.get(i + 1)) != 1) {
                            ct.setType(CardType.SHUAI_LIAN_DUI);
                            break;
                        }
                    }
                }
            } else {
                ct.setType(CardType.SHUAIPAI);
            }
        }

        //拖拉机
        if (ct.getType() == 0 && CardTool.allZhu(cards, zhuColor)) {
//            ct.setCardIds(newCardIds);
//            ct.setType(CardType.TUOLAJI);
//                BUG修复 在此处114, 114, 315, 315, 415, 415 主=1 判定为拖拉机
            List<List<Integer>> firstFenzuList = CardTool.handFenZu(new ArrayList<>(cards), zhuColor);
            List<Integer> firstChuPaiSeatTuoLaJi =  CardTool.checkShuaiPaiContainTuoLaJi(firstFenzuList, zhuColor);
            if(firstChuPaiSeatTuoLaJi.size()>=4 && firstChuPaiSeatTuoLaJi.size()==cards.size()){
                ct.setCardIds(newCardIds);
                ct.setType(CardType.TUOLAJI);
            }else{
                ct.setCardIds(newCardIds);
                ct.setType(CardType.SHUAIPAI);
            }

        }

        return ct;

    }

    public static int getCardPro(int card, int zhuColor) {
        int val = loadCardValue(card);
        int color = loadCardColor(card);
        //有主花色时,主A副2主2小王大王可组成姊妹对。无主时,只有大王和	小王可组成姊妹对
        int priority = 0;
        if(zhuColor!=0){
            if (val == 2) {
                priority = 1;//502
            } else if (val == 1) {
                priority = 2;//501
            }  else if (val == 15 && color == zhuColor) {
                priority = 3;//正2
            } else if (val == 15&& color != zhuColor) {
                priority = 4;//fu2
            } else {//if(color==zhuColor)
                priority = 19-val;
            }
        }else{
            if (val == 2) {
                priority = 1;//502
            } else if (val == 1) {
                priority = 2;//501
            } else if (val == 15 && 0 == zhuColor) {
                priority = -3;//
            }else {//if(color==zhuColor)
                priority = 19-val;
            }
        }

        return priority;

    }


    public static boolean isContainsYZ(List<Integer> cards) {
        for (Integer card : cards) {
            boolean yz = isYingZhu(card);
            if (yz) {
                return true;
            }

        }
        return false;
    }


    /**
     * 获取主牌
     *
     * @param hands
     * @param zhuColor
     * @return
     */
    public static List<Integer> getZhu(List<Integer> hands, int zhuColor) {
        List<Integer> res = new ArrayList<Integer>();

        for (Integer card : hands) {
            if (CardUtils.isZhu(card, zhuColor)) {
                res.add(card);
            }
        }
        return res;
    }


    /**
     * 获取花色牌
     *
     * @param hands
     * @param color
     * @return
     */
    public static List<Integer> getColorCards(List<Integer> hands, int color) {
        List<Integer> res = new ArrayList<Integer>();
        for (Integer card : hands) {
            int cardColor = CardUtils.loadCardColor(card);
            if (!CardUtils.isYingZhu(card) && cardColor == color) {
                res.add(card);
            }
        }
        return res;
    }

    /**
     *
     * @param hands
     * @param color 主色
     * @return
     */
    public static List<Integer> getDianCards2(List<Integer> hands, int color) {
        List<Integer> res = new ArrayList<Integer>();
        for (Integer card : hands) {
            if(color==0){
                if(CardUtils.isZhu(card,0)){
                    res.add(card);
                }
            }else if(color>=1 && color<=4){
                //常规主色
                if(CardUtils.isZhu(card,color)){
                    res.add(card);
                }
            }else{
                int cardColor = CardUtils.loadCardColor(card);
                if (!CardUtils.isYingZhu(card) && cardColor == color) {
                    res.add(card);
                }
            }


        }
        return res;
    }
public static List<Integer> getDianCards3(List<Integer> hands, int color) {
        List<Integer> res = new ArrayList<Integer>();
        for (Integer card : hands) {
            if(color>=0 && color<=4){
                if(CardUtils.isZhu(card,color)){
                    res.add(card);
                }
            } else{
                int cardColor = CardUtils.loadCardColor(card);
                if (!CardUtils.isYingZhu(card) && cardColor == color) {
                    res.add(card);
                }
            }


        }
        return res;
    }


    public static List<Integer> getDuiCards(List<Integer> hands, int count) {
        HashSet<Integer> set = new HashSet<Integer>();
        List<Integer> duis = new ArrayList<Integer>();
        int dui = 0;
        for (Integer card : hands) {
            if (!set.contains(card)) {
                set.add(card);
            } else {
                dui++;
                duis.add(card);
                duis.add(card);
                if (dui == count) {
                    break;
                }
            }
        }
        return duis;
    }
    public static List<Integer> getDuiCards(List<Integer> hands) {
        HashSet<Integer> set = new HashSet<Integer>();
        List<Integer> duis = new ArrayList<Integer>();
        int dui = 0;
        for (Integer card : hands) {
            if (!set.contains(card)) {
                set.add(card);
            } else {
                dui++;
                duis.add(card);
                duis.add(card);
            }
        }
        return duis;
    }


    /**
     * 是否同花色
     *
     * @param
     * @param
     * @return
     */
    public static boolean isSameColor(List<Integer> cards) {
        int color = -1;
        for (Integer card : cards) {
            int cardColor = CardUtils.loadCardColor(card);
            if (color == -1) {
                color = cardColor;
            } else if (cardColor != color) {
                return false;
            }
        }
        return true;
    }

    public static int hasDuiCount(List<Integer> hands) {
        HashSet<Integer> set = new HashSet<Integer>();
        int dui = 0;
        for (Integer card : hands) {
            if (!set.contains(card)) {
                set.add(card);
            } else {
                dui++;
            }
        }
        int size = set.size();
        if (size > 1 && size * 2 == hands.size()) {
            //全是对子
            dui = 100;
        }

        return dui;
    }
    public static boolean isAllDui(List<Integer> hands) {
        HashSet<Integer> set = new HashSet<Integer>();
        int dui = 0;
        for (Integer card : hands) {
            if (!set.contains(card)) {
                set.add(card);
            } else {
                dui++;
            }
        }
        int size = set.size();
        if (size > 1 && size * 2 == hands.size()) {
            //全是对子
            dui = 100;
            return true;
        }

        return false;
    }

    public static List<String> toStringCards(List<Integer> cards){
        List<String> list = new ArrayList<>();
        for (int val:cards) {
           int pai =  val%100;
           int huase = val/100;
           String  strPai ="";
           String  strPai2 ="";
           switch ( huase){
               case 4:
                   strPai ="黑桃";  break;
                   case 3:
                   strPai ="红桃";  break;
                   case 2:
                   strPai ="梅花";  break;
                   case 1:
                   strPai ="方块";  break;
           }
           switch (pai){
               case 1:
                   strPai2 ="小王";  break;
                   case 2:
                       strPai2 ="大王";  break;

                   case 10:
                       strPai2 ="10";  break;
                   case 11:
                       strPai2 ="J";  break;
                       case 12:
                       strPai2 ="Q";  break;
               case 13:
                   strPai2 ="K";  break;
               case 14:
                   strPai2 ="A";
                   break;
                   case 15:
                   strPai2 ="2";
                   break;
               default:
                   strPai2 =""+pai;
                   break;
           }
            list.add(strPai+strPai2+"|"+val);
        }
        return list;
    }

    public static void main(String[] args) {

        List<Integer> newCardIds = new LinkedList<Integer>();
        newCardIds.add(105);
        newCardIds.add(108);
        newCardIds.add(105);
        newCardIds.add(108);
        System.err.println(toStringCards(newCardIds));
    }

    /**
     *
     * @param cardIds
     * @param zhuColor
     * @return 出牌最小牌
     */
    public static int getMinCard(List<Integer> cardIds, int zhuColor) {
        List<Integer> cardId = new ArrayList<>(cardIds);
        int mincard =cardId.get(0);
        for (int i=1;i<cardId.size();i++  ) {
            int card2 = cardId.get(i);
          if(comCardValue(mincard,card2,zhuColor)){
              mincard=card2;
          }
        }
       return mincard;
    }

    /**
     *
     * @param cardIds
     * @param zhuColor
     * @return 出牌最大牌
     */
    public static int getMaxCard(List<Integer> cardIds, int zhuColor) {
            if(null==cardIds ||cardIds.size()==0){
                return 0;
            }
            List<Integer> cardId = new ArrayList<>(cardIds);
            int maxcard =cardId.get(0);
            for (int i=1;i<cardId.size();i++  ) {
                int card2 = cardId.get(i);
                if(!comCardValue(maxcard,card2,zhuColor)){
                    maxcard=card2;
                }
            }
            return maxcard;
    }


    /**
     * 组合牌结果
     */
    public static class Result implements Comparable<Result> {
        /**
         * 0无效，1单张，2对子，3三飘，4四带，11单顺子，22双顺子，33飞机，100炸弹，1000：3条A
         */
        private final int type;
        private final int count;
        private final int max;

        public Result(int type, int count, int max) {
            this.type = type;
            this.count = count;
            this.max = max;
        }

        public int getType() {
            return type;
        }

        public int getCount() {
            return count;
        }

        public int getMax() {
            return max;
        }

        @Override
        public String toString() {
            return new StringBuilder(32).append("{type=").append(type).append(",count=").append(count).append(",max=").append(max).append("}").toString();
        }

        /**
         * -100不能比较，大：正数，小：负数，相等：零
         *
         * @param o
         * @return
         */
        @Override
        public int compareTo(Result o) {
            if (this.type <= 0 || o.type <= 0) {
                return -100;
            } else if (this.type == 1000) {
                return 1;
            } else if (o.type == 1000) {
                return -1;
            } else if (this.type == o.type) {
                switch (this.type) {
                    case 100:
                        if (this.count > o.count) {
                            return 1;
                        } else if (this.count < o.count) {
                            return -1;
                        } else {
                            return this.max - o.max;
                        }
                    case 1:
                        return this.max - o.max;
                    case 2:
                        return this.max - o.max;
                    case 3:
                        return this.max - o.max;
                    case 4:
                        return this.max - o.max;
                    case 22:
                        if (this.count == o.count) {
                            return this.max - o.max;
                        } else {
                            return -100;
                        }
                    case 33:
                        if (this.count == o.count) {
                            return this.max - o.max;
                        } else {
                            return -100;
                        }
                    case 11:
                        if (this.count == o.count) {
                            return this.max - o.max;
                        } else {
                            return -100;
                        }
                    default:
                        return -100;
                }
            } else if (this.type == 100) {
                return 1;
            } else if (o.type == 100) {
                return -1;
            } else {
                return -100;
            }
        }
    }
}

