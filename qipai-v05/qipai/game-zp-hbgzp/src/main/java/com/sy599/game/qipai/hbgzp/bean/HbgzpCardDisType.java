package com.sy599.game.qipai.hbgzp.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards.Builder;
import com.sy599.game.qipai.hbgzp.rule.Hbgzp;
import com.sy599.game.qipai.hbgzp.tool.HbgzpTool;
import com.sy599.game.util.StringUtil;

public class HbgzpCardDisType {
	private int action;
	private List<Integer> cardIds;
	private int huxi;
	private int disSeat;
	private int disStatus; //0未报听，1报听
	private int qGang; //0未被抢，1被抢

	public int getDisStatus() {
		return disStatus;
	}

	public void setDisStatus(int disStatus) {
		this.disStatus = disStatus;
	}

	public int getqGang() {
		return qGang;
	}

	public void setqGang(int qGang) {
		this.qGang = qGang;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public List<Integer> getCardIds() {
		return cardIds;
	}

	public void setCardIds(List<Integer> cardIds) {
		this.cardIds = cardIds;
	}

	public void addCardId(int id) {
		if (this.cardIds == null) {
			this.cardIds = new ArrayList<>();
		}
		this.cardIds.add(id);
	}

	public int getDisSeat() {
		return disSeat;
	}

	public void setDisSeat(int disSeat) {
		this.disSeat = disSeat;
	}


	public boolean isHasCard(Hbgzp card) {
		return cardIds != null && cardIds.contains(card.getId());
	}


	public boolean isHasCardVal(int val) {
		if (cardIds != null) {
			List<Hbgzp> majiangs = HbgzpTool.toMajiang(cardIds);
			List<Integer> vals = HbgzpTool.toMajiangVals(majiangs);
			return vals.contains(val);
		}
		return false;

	}
	public boolean isHasCardVal(Hbgzp card) {
		if (cardIds != null) {
			List<Hbgzp> majiangs = HbgzpTool.toMajiang(cardIds);
			List<Integer> vals = HbgzpTool.toMajiangVals(majiangs);
			return vals.contains(card.getVal());
		}
		return false;

	}
	/**
	 * 删除某个值
	 * @param val
	 * @return
	 */
	public int removeCardVal(int val) {
		if (cardIds != null) {
			Iterator<Integer> iterator = cardIds.iterator();
			while (iterator.hasNext()) {
				int id = iterator.next();
				Hbgzp majiang = Hbgzp.getPaohzCard(id);
				if (majiang != null && majiang.getVal() == val) {
					iterator.remove();
					return id;
				}
			}
		}
		return 0;
	}

	public void init(String data) {
		if (!StringUtils.isBlank(data)) {
			String[] values = data.split("_");
			action = StringUtil.getIntValue(values, 0);
			String cards = StringUtil.getValue(values, 1);
			if (!StringUtils.isBlank(cards)) {
				cardIds = StringUtil.explodeToIntList(cards);
			}
			disSeat = StringUtil.getIntValue(values, 2);
			disStatus = StringUtil.getIntValue(values, 3);
			qGang = StringUtil.getIntValue(values, 4);
			huxi=StringUtil.getIntValue(values, 5);
		}
	}

	public String toStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(action).append("_");
		sb.append(StringUtil.implode(cardIds)).append("_");
		sb.append(disSeat).append("_");
		sb.append(disStatus).append("_");
		sb.append(qGang).append("_");
		sb.append(huxi).append("_");
		return sb.toString();
	}

	public Builder buildMsg() {
		return buildMsg(false);
	}

	public Builder buildMsg(boolean hideCards) {
		PhzHuCards.Builder msg = PhzHuCards.newBuilder();
		if (hideCards) {
			msg.addAllCards(toPhzCardZeroIds(cardIds));
		} else {
			msg.addAllCards(cardIds);
		}
		msg.setAction(action);
//		msg.setHuxi(disSeat); //TODO 原先的????
		msg.setHuxi(huxi);
		return msg;
	}
	/**
	 * 牌转化为Id
	 * 
	 * @param phzs
	 * @return
	 */
	public static List<Integer> toPhzCardZeroIds(List<?> phzs) {
		List<Integer> ids = new ArrayList<>();
		if (phzs == null) {
			return ids;
		}
		for (int i = 0; i < phzs.size(); i++) {
			ids.add(0);
		}
		return ids;
	}

	public int getHuxi() {
		return huxi;
	}

	public void setHuxi(int huxi) {
		this.huxi = huxi;
	}
	
}
