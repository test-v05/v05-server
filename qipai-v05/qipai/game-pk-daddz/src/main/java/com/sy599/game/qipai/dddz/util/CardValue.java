package com.sy599.game.qipai.dddz.util;

import java.util.Objects;

public class CardValue implements Comparable<CardValue> {
	private final int card;
	private final int value;
	// 方片 1 梅花2 红桃3 黑桃4  5王
	private final int color;

	public CardValue(int card) {
		this.card = card;
		this.value = CardUtils.loadCardValue(card);
		this.color = CardUtils.loadCardColor(card);
	}

	public CardValue(int card, int value, int color) {
		this.card = card;
		this.value = value;
		this.color = color;
	}

	public int getCard() {
		return card;
	}

	public int getValue() {
		return value;
	}

	public int getColor() {
		return color;
	}

	@Override
	public String toString() {
		return "{card=" + card + ",value=" + value + "}";
	}

	@Override
	public int compareTo(CardValue o) {
		return this.value - o.value;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof CardValue) && card == ((CardValue) obj).card;
	}

	public static void main(String[] args) {
		//payConfig
		String wanfa ="275";
		String[] paytype ={"0","1","3"};
		String[] jushu ={"5","8","10","15"};
		String[] renshu ={"4","3"};
		for (String p:paytype ) {
			for (String j:jushu ) {
				for (String r:renshu ) {
					String pay ="INSERT INTO `localtestbjdqplogin`.`t_resources_configs`\n" +
							" ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`)\n" +
							" VALUES ( 'PayConfig', 'pay_type"+wanfa+"_count"+j+"_player"+r+"_pay"+p+"', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-07-01 10:50:31');\n";
				System.out.println(pay);
				}
			}
		}
		
	}
}	
