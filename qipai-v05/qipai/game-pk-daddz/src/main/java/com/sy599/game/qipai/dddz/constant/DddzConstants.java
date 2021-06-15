package com.sy599.game.qipai.dddz.constant;

import java.util.ArrayList;
import java.util.List;

public class DddzConstants {

	/*** 桌状态叫分 */
    public static final int TABLE_STATUS_JIAOFEN = 1;

    /*** 选主 */
    public static final int TABLE_STATUS_XUANZHU = 2;
    
    /*** 埋牌 */
    public static final int TABLE_STATUS_MAIPAI = 3;
    
    /*** 打牌 */
    public static final int TABLE_STATUS_PLAY = 4;
    
    
    /*** 留守 */
    public static final int TABLE_LIUSHOU_PLAY = 5;
    
    
    /*** 扣底 */
    public static final int TABLE_KOUDI = 6;
    
    /*** 定庄 */
    public static final int TABLE_DINGZHUANG = 7;

    //add
	/*** 飘分 */
	public static final int TABLE_STATUS_PIAOFEN = 8;

	/*** 扣底 */
	public static final int TABLE_STATUS_KOUDI = 9;

	/*** 选择是否半路打春天 */
	public static final int TABLE_STATUS_SELZTCT = 11;

	/*** 选择是否投降、地主过叫分的一半之后,如果整个牌面剩余分数不足以打够地主叫分,地主可	以选择直接投降。(可在创建房间时勾选是否带此玩法) */
	public static final int TABLE_STATUS_SELDZTX= 12;

	public static final int TABLE_STATUS_KOUDIPAI = 14 ;


	/**埋牌*/
    public static final int REQ_MAIPAI=100;
    
    public static final int RES_KOUDI=200;
    
    
    
    /**托管**/
    public static final int action_tuoguan = 100;
	public static final int TABLE_REPLAY_SHUAIPAI =13 ;
	public static final int TABLE_REPLAY_CallBackPai =15;

	// public static List<Integer> cardList_16 = new ArrayList<>(52);
	public static List<Integer> cardList = new ArrayList<>(52);
	static {
		// 方片 1 梅花2 红3 黑桃4 5王
		//  玩法10 - A 2
		for (int n = 0; n < 2; n++) {
			for (int i = 1; i <= 4; i++) {
				for (int j = 10; j <= 15; j++) {
					int card = i * 100 + j;
					cardList.add(card);
				}
			}
			cardList.add(501);//小王
			cardList.add(502);//大王
		}

	}
//		1、游戏人数3-5人,共分两个阵营,1个地主,其他都是农民。
//			2、使用2副扑克牌,只取10、J、Q、K、A、2、大小王,共52张;其中2和大	小王为常主。
//			3、3人玩法:每人16张牌,4张底牌,80分起叫。
//			4、4人玩法:每人12张牌,4张底牌,80分起叫。
//			5、5人玩法:每人10张牌,2张底牌,40分起叫。
	public static void main(String[] args) {
		 List<Integer>	copy = cardList.subList(4,cardList.size());
		int maxCount = copy.size() /3;
		List<Integer> pai = new ArrayList<>();
		List<List<Integer>> list = new ArrayList<>();

		int j=1;
		for (int i = 0; i < copy.size(); i++) {
			int card = copy.get(i);
			if (i < j*maxCount) {
				pai.add(card);
			} else {
				list.add(pai);
				pai = new ArrayList<>();
				pai.add(card);
				j++;
			}
			
		}
		list.add(pai);
		list.add(cardList.subList(0, 8));
		System.out.println(list);
		System.out.println("===========================");
		initlongpai();
	}

	private static void initlongpai() {
	}

}
