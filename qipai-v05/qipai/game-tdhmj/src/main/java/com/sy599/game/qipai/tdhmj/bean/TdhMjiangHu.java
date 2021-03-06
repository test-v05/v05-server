package com.sy599.game.qipai.tdhmj.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sy599.game.qipai.tdhmj.rule.TdhMj;
import com.sy599.game.util.DataMapUtil;

public class TdhMjiangHu {
	/**
	 * 是否平胡
	 */
	private boolean pingHu;
	//------------------------------大胡----------------------------
	/**
	 * 碰碰胡
	 */
	private boolean pengpengHu;
	/**
	 * 将将胡
	 */
	private boolean jiangjiangHu;
	/**
	 * 清一色
	 */
	private boolean qingyiseHu;
	/**
	 * 双豪华7小对
	 */
	private boolean shuang7xiaodui;
	/**
	 * 豪华7小对
	 */
	private boolean hao7xiaodui;
	/**
	 * 7小对
	 */
	private boolean xiaodui;
	/**
	 * 全求人
	 */
	private boolean quanqiuren;
	/**
	 * 抢杠胡
	 */
	private boolean isQGangHu;
	/**
	 * 杠上炮
	 */
	private boolean isGangShangPao;
	/**
	 * 杠上花
	 */
	private boolean isGangShangHua;
	/**
	 * 海底捞
	 */
	private boolean haidilaoyue;
	/**
	 * 海底炮
	 */
	private boolean haidipao;
	/**
	 * 天胡
	 */
	private boolean tianhu;
	/**
	 * 地胡
	 */
	private boolean dihu;
	
	private boolean menqing;


	/**
	 * 是否胡
	 */
	private boolean isHu;
	/**
	 * 是否大胡
	 */
	private boolean isDahu;
	
	
	/**
	 * 大胡列表：0碰碰胡 1将将胡 2清一色 3海底月 4海底炮 5:7小对 6 豪华7小对 7杠上花 8抢杠胡9 杠上炮 10全求人 11双豪华7小对
	 */
	private List<Integer> dahuList;
	
	/**
	 *
	 */
	private List<TdhMj> showMajiangs;
	/**
	 * 胡的分数
	 */
	private int dahuPoint;

	/**
	 * @return 0：碰碰胡
	 * 		   1：将将胡
	 * 		   2：清一色
	 * 		   3：海底月
	 * 		   4：海底炮
	 * 		   5：7小对
	 * 		   6：豪华7小对
	 * 		   7：杠上花
	 * 		   8：抢杠胡
	 * 		   9：杠上炮
	 * 		   10：全求人
	 *         11：双豪华7小对
	 *         12：天胡
	 *         13：地胡
	 *         14:门清
	 */
	public List<Integer> buildDahuList() {
		int[] arr = new int[15];
		if (pengpengHu) {
			arr[0] = 1;
		}
		if (jiangjiangHu) {
			arr[1] = 1;
		}
		if (qingyiseHu) {
			arr[2] = 1;
		}
		if (haidilaoyue) {
			arr[3] = 1;
		}
		if (haidipao) {
			arr[4] = 1;
		}
		if (xiaodui) {
			arr[5] = 1;
		}
		if (hao7xiaodui) {
			arr[6] = 1;
		}
		if (isGangShangHua) {
			arr[7] = 1;
		}
		if (isQGangHu) {
			arr[8] = 1;
		}
		if (isGangShangPao) {
			arr[9] = 1;
		}
		if (quanqiuren) {
			arr[10] = 1;
		}
		if (shuang7xiaodui) {
			arr[11] = 1;
		}
		if (tianhu) {
			arr[12] = 1;
		}
		if (dihu) {
			arr[13] = 1;
		}
		if(menqing) {
			arr[14] = 1;
		}

		List<Integer> dahu = DataMapUtil.indexToValList(arr);
		if (!dahu.isEmpty()) {
			return dahu;
		}
		return Collections.EMPTY_LIST;
	}

	public int getDahuPointByList() {
		int point = 0;
		for (int dahu : dahuList) {
			if (dahu == 11) {
				point += 18;
			} else if (dahu == 6) {
				point += 12;
			} else {
				point += 6;
			}
		}
		return point;
	}

	public boolean isPingHu() {
		return pingHu;
	}
	
	

	public boolean isMenqing() {
		return menqing;
	}

	public void setMenqing(boolean menqing) {
		this.menqing = menqing;
	}

	public void setPingHu(boolean pingHu) {
		this.pingHu = pingHu;
	}

	public boolean isPengpengHu() {
		return pengpengHu;
	}

	public void setPengpengHu(boolean pengpengHu) {
		this.pengpengHu = pengpengHu;
		dahuPoint += 6;
	}

	public boolean isJiangjiangHu() {
		return jiangjiangHu;
	}

	public void setJiangjiangHu(boolean jiangjiangHu) {
		this.jiangjiangHu = jiangjiangHu;
		dahuPoint += 6;
	}

	public boolean isQingyiseHu() {
		return qingyiseHu;
	}

	public void setQingyiseHu(boolean qingyiseHu) {
		this.qingyiseHu = qingyiseHu;
		dahuPoint += 6;
	}

	public boolean isShuang7xiaodui() {
		return shuang7xiaodui;
	}

	public void setShuang7xiaodui(boolean shuang7xiaodui) {
		this.shuang7xiaodui = shuang7xiaodui;
		dahuPoint += 18;
	}

	public boolean isHao7xiaodui() {
		return hao7xiaodui;
	}

	public void setHao7xiaodui(boolean hao7xiaodui) {
		this.hao7xiaodui = hao7xiaodui;
		dahuPoint += 12;
	}

	public boolean is7Xiaodui() {
		return xiaodui;
	}

	public void set7Xiaodui(boolean xiaodui) {
		this.xiaodui = xiaodui;
		dahuPoint += 6;
	}

	public boolean isQuanqiuren() {
		return quanqiuren;
	}

	public void setQuanqiuren(boolean quanqiuren) {
		this.quanqiuren = quanqiuren;
		dahuPoint += 6;
	}

	
	public boolean isHu() {
		return isHu;
	}

	public void setHu(boolean isHu) {
		this.isHu = isHu;
	}


	public boolean isDahu() {
		return isDahu;
	}

	public void setDahu(boolean isDahu) {
		this.isDahu = isDahu;
	}


	public List<Integer> getDahuList() {
		return dahuList;
	}

	public void setDahuList(List<Integer> dahuList) {
		this.dahuList = dahuList;
	}

	public void initDahuList() {
		this.dahuList = buildDahuList();
		if (!this.dahuList.isEmpty()) {
			setDahuPoint(getDahuPointByList());
			isDahu = true;
		}
	}
	
	public int getDahuSize() {
		if(dahuList==null) {
			return 0;
		}
		int gangsp= 0;
		for(Integer id :dahuList){
			if(id==9) {
				gangsp+=1;
			}
		}
		if(gangsp==2) {
			return dahuList.size()-1;
		}else {
			return dahuList.size();
		}
		
	}

	public int getDahuPoint() {
		return dahuPoint;
	}

	public void setDahuPoint(int dahuPoint) {
		this.dahuPoint = dahuPoint;
	}

	public void addToDahu(List<Integer> dahuList) {
		if (this.dahuList == null) {
			this.initDahuList();
		}
		for (int dahu : dahuList) {
//			if (dahu == 7 || dahu == 9) {
				// 杠上花和杠上炮可以算两次
				this.dahuList.add(dahu);
//				continue;
//			}
//			if (!this.dahuList.contains(dahu)) {
//				this.dahuList.add(dahu);
//			}

		}

		setDahuPoint(getDahuPointByList());
	}

	public boolean isQGangHu() {
		return isQGangHu;
	}

	public void setQGangHu(boolean isQGangHu) {
		this.isQGangHu = isQGangHu;
		dahuPoint += 6;
	}

	public boolean isGangShangPao() {
		return isGangShangPao;
	}

	public void setGangShangPao(boolean isGangShangPao) {
		this.isGangShangPao = isGangShangPao;
		dahuPoint += 6;
	}

	public void setHaidilaoyue(boolean haidilaoyue) {
		this.haidilaoyue = haidilaoyue;
		dahuPoint += 6;
	}

	public void setHaidipao(boolean haidipao) {
		this.haidipao = haidipao;
		dahuPoint += 6;
	}

	public boolean isGangShangHua() {
		return isGangShangHua;
	}

	public void setGangShangHua(boolean isGangShangHua) {
		this.isGangShangHua = isGangShangHua;
		dahuPoint += 6;
	}


	public boolean isTianhu() {
		return tianhu;
	}

	public void setTianhu(boolean tianhu) {
		this.tianhu = tianhu;
		dahuPoint += 6;
	}

	public boolean isDihu() {
		return dihu;
	}

	public void setDihu(boolean dihu) {
		this.dihu = dihu;
		dahuPoint += 6;
	}
	
	public void setShowMajiangs(List<TdhMj> showMajiangs) {
		this.showMajiangs = showMajiangs;
	}

	public List<TdhMj> getShowMajiangs() {
		return showMajiangs;
	}


	public static int getDaHuPointCount(List<Integer> daHuList){
		if(daHuList == null || daHuList.size()==0){
			return 0;
		}
		int count = 0;
		int gangshanghua = 0;
		for (int dahu : daHuList) {
			if(dahu==7) {
				gangshanghua +=1;
			}
			
//			if(gangshanghua==2) {
//				continue;
//			}
//			
			if (dahu == 11) {
				count += 3;
			} else if (dahu == 6) {
				count += 2;
			} else {
				//天胡地胡不算大胡20180803
				count += 1;
			}
		}
		return count;
	}
	
	
	
	
	public static List<Integer> getDaHuPointCount2(List<Integer> daHuList){
		
		List<Integer> res = new ArrayList<>();
		if(daHuList == null || daHuList.size()==0){
			return res;
		}
		int count = 0;
		for (int dahu : daHuList) {
			if (dahu == 11) {
				count += 3;
			} else if (dahu == 6) {
				count += 2;
			} else {
				//天胡地胡不算大胡20180803
				count += 1;
			}
			if(dahu==7) {
				res.add(count);
				count =0;
			}
		}
		return res;
	}
	
	
	
	

}
