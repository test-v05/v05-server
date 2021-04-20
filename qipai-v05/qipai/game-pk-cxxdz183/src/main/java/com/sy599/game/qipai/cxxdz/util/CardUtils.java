package com.sy599.game.qipai.cxxdz.util;

import com.sy599.game.qipai.cxxdz.bean.CxxdzPlayer;

import java.util.*;


public final class CardUtils {

    public static final CardValue EMPTY_CARD_VALUE = new CardValue(0, 0,0);
    public static final List<CardValue> EMPTY_CARD_VALUE_LIST = Arrays.asList(EMPTY_CARD_VALUE);




    
    
    
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
     * 计算牌值(A_14,2_15,3_3...,K_13)
     *
     * @param card
     * @return
     */
    public static int loadCardValue(int card) {
        return card % 100;
    }
    public static int loadCardValue1(int card) {
        if(card==501 ){
            return 16;
        }
        if(card==502 ){
            return 17;
        }
        return card % 100;
    }
    public static boolean isYingZhu(int card){
    	int value = loadCardValue(card);
    	
    	if(value==10||value==15||value==1||value==2) {
    		return true;
    	}
    	return false;
    }
    
    
    public static boolean isZhu(int card,int zhuColor){
    	int value = loadCardValue(card);
    	int color =loadCardColor(card);
    	if(value==10||value==15||value==1||value==2) {
    		return true;
    	}
    	
    	if(color==zhuColor){
    		return true;
    	}
    	return false;
    }
    
    
    
    public static boolean comCardValue(int card1,int card2,int zhuColor){
    	int color1 = loadCardColor(card1);
    	int color2 = loadCardColor(card2);
    	
    	int value1 = loadCardValue(card1);
    	int value2 = loadCardValue(card2);
    	
    	
    	value1 = changeCardValue(value1);
    	value2 = changeCardValue(value2);
    	
    	//同花色
    	if(color1==color2){
    		if(value1>=value2){
        		return true;
    		}
    	}else {
    		if(zhuColor>0){
    			//第一个是主牌,第二个如果不是硬主第一个大，如果第二个是硬主那比较大小即可
    			if(isZhu(card1, zhuColor)){
    				if(!isYingZhu(card2)||value1>value2){
    					return true;
    				}else if(value1==value2){//两个都是硬主谁是正的谁大,都是副的前面的大
    					if(color1 == zhuColor||color2!=zhuColor) {
    						return true;
    					}
    				}
        		}else{
        			//如果两个都不是主，那前面的大
        			if(!isZhu(card2, zhuColor)){
        				return true;
        			}
        		}
    		}else {
    			//无主 副牌 不同花色前面的大
    			if(value1<15&&value2<15){
    				return true;
    			}else if(value1>=value2){//一旦有硬主比较大小即可
    				return true;
    			}
    			
    		}
    	}
    	return false;
    }
    
    
    
    private static int changeCardValue(int value){
    	if(value==10){
    		value+=10;
    	}
    	if(value==1||value==2){
    		value +=20;
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


    
    
    
    /***
     * 拖拉机
     * @param cards
     */
    public static CardType  isTuoLaji(List<Integer> cards,int zhuColor) {
        CardType ct = new CardType(0, cards);
        if(cards.size()%2!=0) {
            ct.setType(CardType.SHUAIPAI);
            return ct;
        }
        CardUtils.sortCards(cards);
        List<Integer> newCardIds = new LinkedList<Integer>();
        newCardIds.addAll(cards);

        //是否全是对子len / 2 - 1
        if(hasDuiCount(newCardIds)==100&&(newCardIds.get(newCardIds.size()-1) -newCardIds.get(0)==newCardIds.size()/2-1)&&(isSameColor(cards)&&!isContainsYZ(cards))){//连对 拖拉机
            ct.setType(CardType.TUOLAJI);
            ct.setCardIds(newCardIds);
        }else {
            int dui = hasDuiCount(newCardIds);
            if(dui == 100) {//全是对子
                List<Integer> proList = new ArrayList<Integer>();
                for(int i=0;i<newCardIds.size()-1;i++) {
                    int a = newCardIds.get(i);
                    int b = newCardIds.get(i+1);
                    int val1 = getCardPro(a, zhuColor);
                    int val2 = getCardPro(b, zhuColor);
                    //相邻的两对不是连着的 不是拖拉机
                    if(a!=b){//&&(Math.abs(val2-val1)!=1)
                        if(!proList.contains(val1)){
                            proList.add(val1);
                        }
                        if(!proList.contains(val2)){
                            proList.add(val2);
                        }
//
//						break;
                    }
                }
                Collections.sort(proList);
                if(proList.size()<=1){
                    ct.setType(CardType.SHUAI_LIAN_DUI);
                }else {
                    for(int i=0;i<proList.size()-1;i++){
                        if(Math.abs(proList.get(i)-proList.get(i+1))!=1){
                            ct.setType(CardType.SHUAI_LIAN_DUI);
                            break;
                        }
                    }
                }



            }else {
                ct.setType(CardType.SHUAIPAI);
            }
        }

        //拖拉机
        if(ct.getType()==0){
            ct.setCardIds(newCardIds);
            ct.setType(CardType.TUOLAJI);
            System.out.println("tuolaji...................................... cards = " + newCardIds);
        }

        return ct;

    }
    public static Boolean  isTuoLaji2(List<Integer> cards,int zhuColor,boolean ischou6) {
        CardType ct = new CardType(0, cards);
        if(cards.size()%2!=0) {
            ct.setType(CardType.SHUAIPAI);
            return false;
        }
        if(!ischou6){
            return false;
        }
        List<Integer> cpcards = new ArrayList<>();
        for (int num:cards){
            if(loadCardValue(num)==5){
                cpcards.add(num+1);
            }else{
                cpcards.add(num);
            }
        }

        CardUtils.sortCards(cpcards);
        List<Integer> newCardIds = new LinkedList<Integer>();
        newCardIds.addAll(cpcards);

        //是否全是对子len / 2 - 1
        if(hasDuiCount(newCardIds)==100&&(newCardIds.get(newCardIds.size()-1) -newCardIds.get(0)==newCardIds.size()/2-1)&&(isSameColor(cards)&&!isContainsYZ(cards))){//连对 拖拉机
            ct.setType(CardType.TUOLAJI);
            ct.setCardIds(newCardIds);
        }else {
            int dui = hasDuiCount(newCardIds);
            if(dui == 100) {//全是对子
                List<Integer> proList = new ArrayList<Integer>();
                for(int i=0;i<newCardIds.size()-1;i++) {
                    int a = newCardIds.get(i);
                    int b = newCardIds.get(i+1);
                    int val1 = getCardPro2(a, zhuColor);
                    int val2 = getCardPro2(b, zhuColor);
                    //相邻的两对不是连着的 不是拖拉机
                    if(a!=b){//&&(Math.abs(val2-val1)!=1)
                        if(!proList.contains(val1)){
                            proList.add(val1);
                        }
                        if(!proList.contains(val2)){
                            proList.add(val2);
                        }
//						break;
                    }
                }
                Collections.sort(proList);
                if(proList.size()<=1){
                    ct.setType(CardType.SHUAI_LIAN_DUI);
                }else {
                    for(int i=0;i<proList.size()-1;i++){
                        if(Math.abs(proList.get(i)-proList.get(i+1))!=1){
                            ct.setType(CardType.SHUAI_LIAN_DUI);
                            break;
                        }
                    }
                }
            }else {
                ct.setType(CardType.SHUAIPAI);
            }
        }

        //拖拉机
        if(ct.getType()==0){
            ct.setCardIds(newCardIds);
            ct.setType(CardType.TUOLAJI);
            System.out.println("tuolaji...................................... cards = " + newCardIds);
        }
        if(ct.getType()==3){
            return true;
        }else{
            return false;
        }
    }
    public static boolean isContainsYZ(List<Integer> cards){
        for(Integer card: cards){
            boolean yz = isYingZhu(card);
            if(yz){
                return true;
            }

        }
        return false;
    }
    private static int getCardPro(int card,int zhuColor){
    	
    	int val = loadCardValue(card);
    	int color = loadCardColor(card);
    	
    	int priority = 0;
    	if(val==2){
    		priority = 1;
    	}else if(val==1){
    		priority = 2;
    	}else if(val==10&&color==zhuColor){
    		priority=3;
    	}else if(val==10){
    		priority=4;
    	}else if(val==15&&color==zhuColor){
    		priority = 5;
    	}else if(val==15){
    		priority = 6;
    	}else {//if(color==zhuColor)
    		priority = 21-val;//21-5=16 //21-7=14
    		if(val==6||val==5 ||val==7||val==8 ||val==9){
    			priority-=1;
    		}

    	}
    	return priority;
    	
    }
    private static int getCardPro2(int card,int zhuColor){

        int val = loadCardValue(card);
        int color = loadCardColor(card);

        int priority = 0;
        if(val==2){
            priority = 1;
        }else if(val==1){
            priority = 2;
        }else if(val==15&&color==zhuColor){
            priority = 5;
        }else if(val==15){
            priority = 6;
        }else {//if(color==zhuColor)
            priority = 21-val;
            if(val==6||val==5){
                priority-=1;
            }

        }
        return priority;

    }


    /**
	 * 获取主牌
	 * @param hands
	 * @param zhuColor
	 * @return
	 */
    public static List<Integer> getZhu(List<Integer> hands,int zhuColor){
		List<Integer> res = new ArrayList<Integer>();
		
		for(Integer card: hands){
			if(CardUtils.isZhu(card, zhuColor)){
				res.add(card);
			}
		}
		return res;
	}
	
	
	/**
	 * 获取花色牌
	 * @param hands
	 * @param color
	 * @return
	 */
	public static List<Integer> getColorCards(List<Integer> hands,int color){
		List<Integer> res = new ArrayList<Integer>();
		for(Integer card: hands){
			int cardColor = CardUtils.loadCardColor(card);
			if(!CardUtils.isYingZhu(card)&&cardColor==color){
				res.add(card);
			}
		}
		return res;
	}

	
	
	
	/**
	 * 是否同花色
	 * @param
	 * @param
	 * @return
	 */
	public static boolean isSameColor(List<Integer> cards){
		int color =-1;
		for(Integer card: cards){
			int cardColor = CardUtils.loadCardColor(card);
			if(color==-1){
				color =cardColor;
			}else if(cardColor!=color){
				return false;
			}
		}
		return true;
	}



	public static int hasDuiCount(List<Integer> hands){
		HashSet<Integer> set = new HashSet<Integer>();
		int dui=0;
		for(Integer card: hands){
			if(!set.contains(card)){
				set.add(card);
			}else{
				dui++;
			}
		}
		int size = set.size();
		if(size>1&&size*2==hands.size()) {
			//全是对子
			dui = 100;
		}
		
		return dui;
	}

    /**
     *  比较值，获取某一张牌的个数
     * @param handPais
     * @param val
     * @return
     */
    public static int getNumByVal(List<Integer> handPais, int val){
        int c2num =0;
        for (int c2:handPais) {
            if(val==loadCardValue(c2)){
                c2num++;
            }
        }
        return c2num;
    }









    private static double countBoom(String type,List<Integer> ids){
        double result=0;
        switch (type){
            case "jmBoom":
                Map<Integer, Integer> valAndNumById = DdzSfNew.getValAndNumById(ids,"desc");
                int maxVal=0;
                for (Map.Entry<Integer, Integer> entry:valAndNumById.entrySet()) {
                    maxVal=entry.getKey();
                    break;
                }
                double pow = Math.pow(10, valAndNumById.size());
                result=pow*maxVal;
                break;
            case "wboom":
                result=1;
                break;
            case "boom":
                int val = getCoreCardVal(ids, 4);
                result=Math.round(val)/100.0;
                break;
            default:
                result=0;
        }
        return result;

    }

    public static boolean canChuPai(List<Integer> cpdesk, List<Integer> myout, String deskType, String mytype) {
        boolean re = false;
        double deskCount=countBoom(deskType,cpdesk);
        double myCount=countBoom(mytype,myout);
        if(myCount>0){
            if(myCount>deskCount)
                return true;
        }

        if("danz".equals(deskType)
                || "duiz".equals(deskType)
                || "ld".equals(deskType)
                || "3z".equals(deskType)
                || "3d1".equals(deskType)
                || "3d2".equals(deskType)
                || "4d2".equals(deskType)
                || "shunzi".equals(deskType)
                || "fjd0".equals(deskType)
                || "fjddui".equals(deskType)
                || "fjddan".equals(deskType) ){
            if(mytype.equals("boom") || mytype.equals("wboom") ){
                return true;
            }
            if(cpdesk.size()!=myout.size()){
                return false;
            }
        }
        if("danz".equals(deskType)){
            if("danz".equals(mytype)){
                int deskval = loadCardValue1(cpdesk.get(0));
                int myval =  loadCardValue1(myout.get(0));
                if(myval>deskval){
                    return true;
                }
            }
        }else  if("duiz".equals(deskType)){
            if("duiz".equals(mytype)){
                int deskval = loadCardValue(cpdesk.get(0));
                int myval =  loadCardValue(myout.get(0));
                if(myval>deskval){
                    return true;
                }
            }
        }else  if("ld".equals(deskType)){
            if("ld".equals(mytype)){
                int deskval = getCoreCardValFeiJi(cpdesk);
                int myval =  getCoreCardValFeiJi(myout);
                if(myval>deskval){
                    return true;
                }
            }
        }else  if("3z".equals(deskType)){
            if("3z".equals(mytype)){
                int deskval = loadCardValue(cpdesk.get(0));
                int myval =  loadCardValue(myout.get(0));
                if(myval>deskval){
                    return true;
                }
            }
        }else  if("3d1".equals(deskType)){
            if("3d1".equals(mytype)){
                int deskval = getCoreCardVal(cpdesk,3);
                int myval =  getCoreCardVal(myout,3);
                if(myval>deskval){
                    return true;
                }
            }
        }else  if("3d2".equals(deskType)){
            if("3d2".equals(mytype)){
                int deskval = getCoreCardVal(cpdesk,3);
                int myval =  getCoreCardVal(myout,3);
                if(myval>deskval){
                    return true;
                }
            }
        }else  if("4d2".equals(deskType)){
            if("4d2".equals(mytype)){
                int deskval = getCoreCardVal(cpdesk,4);
                int myval =  getCoreCardVal(myout,4);
                if(myval>deskval){
                    return true;
                }
            }
        }else  if("shunzi".equals(deskType)){
            if("shunzi".equals(mytype)){
                int deskval = getCoreCardValShunzi(cpdesk);
                int myval =  getCoreCardValShunzi(myout);
                if(myval>deskval){
                    return true;
                }
            }
        }else  if("fjd0".equals(deskType)){
            if("fjd0".equals(mytype)){
                int deskval = getCoreCardValFeiJi(cpdesk);
                int myval =  getCoreCardValFeiJi(myout);
                if(myval>deskval){
                    return true;
                }
            }
        }else  if("fjddui".equals(deskType)){
            if("fjddui".equals(mytype)){
                int deskval = getCoreCardValFeiJi(cpdesk);
                int myval =  getCoreCardValFeiJi(myout);
                if(myval>deskval){
                    return true;
                }
            }
        }else  if("fjddan".equals(deskType)){
            if("fjddan".equals(mytype)){
                int deskval = getCoreCardValFeiJi(cpdesk);
                int myval =  getCoreCardValFeiJi(myout);
                if(myval>deskval){
                    return true;
                }
            }
        }
        return re;
    }


    private static int getCoreCardVal(List<Integer> cards,int num) {
        List<Integer> valList = new ArrayList<>();
        for (int p:cards) {
            valList.add(loadCardValue(p));
        }
        for (int p:valList) {
            int n = getNumByVal(valList,loadCardValue(p));
            if(n==num){
                return loadCardValue(p);
            };
        }
        return 0;
    }

    private static int getCoreCardValShunzi(List<Integer> cards ){
        List<Integer> valList = new ArrayList<>();
        for (int p:cards) {
            valList.add(loadCardValue(p));
        }
        Collections.sort(valList);
        return valList.get(0);
    }
    private static int getCoreCardValFeiJi(List<Integer> cards ){
        List<Integer> valList = new ArrayList<>();
        for (int p:cards) {
            valList.add(loadCardValue(p));
        }
        List<Integer> valList2 = new ArrayList<>();
        for (int p:valList) {
            int n = getNumByVal(valList,loadCardValue(p));
            if(n==3 && !valList.contains(loadCardValue(p))){
                valList2.add(loadCardValue(p));
            };
        }
        Collections.sort(valList2);
        return valList.get(0);
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

