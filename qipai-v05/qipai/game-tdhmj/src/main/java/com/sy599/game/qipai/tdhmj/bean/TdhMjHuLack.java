package com.sy599.game.qipai.tdhmj.bean;

import java.util.ArrayList;
import java.util.List;

public class TdhMjHuLack {
	private boolean isHasJiang;
	private List<Integer> lackVal;
	private int hongzhongNum;

	public TdhMjHuLack(int hongzhongNum) {
		lackVal = new ArrayList<>();
		this.hongzhongNum = hongzhongNum;
	}

	public void addLack(int val) {
		lackVal.add(val);
	}

	public void addAllLack(List<Integer> vallist) {
		lackVal.addAll(vallist);
	}

	public boolean isHasJiang() {
		return isHasJiang;
	}

	public void setHasJiang(boolean isHasJiang) {
		this.isHasJiang = isHasJiang;
	}

	public List<Integer> getLackVal() {
		return lackVal;
	}

	public void setLackVal(List<Integer> lackVal) {
		this.lackVal = lackVal;
	}

	public int getHongzhongNum() {
		return hongzhongNum;
	}

	public void changeHongzhong(int count) {
		hongzhongNum += count;
	}

	public void setHongzhongNum(int hongzhongNum) {
		this.hongzhongNum = hongzhongNum;
	}

	public TdhMjHuLack copy() {
		TdhMjHuLack copy = new TdhMjHuLack(this.hongzhongNum);
		copy.setHasJiang(isHasJiang);
		if (lackVal != null && !lackVal.isEmpty()) {
			copy.setLackVal(new ArrayList<>(lackVal));
		}
		return copy;
	}

}
