package com.sy599.game.qipai.cxxdz.util;



import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DdzSfNew {


	/**
	 * 牌转短int数组
	 * @param pai
	 * @return
	 */
	public static int[] paiToShortAry(List<String> pai){
		int[] ary = {0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		for (int i = 0; i < pai.size(); i++) {
			int p = getNumbers(pai.get(i));
			ary[p-3]++;
		}
		return ary;
	}

	/**
	 * 截取数值
	 * 
	 * @param content
	 * @return
	 */
	public static Integer getNumbers(String content) {
		Integer i = 0;
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			i = Integer.parseInt(matcher.group(0));
		}
		return i;
	}

	/**
	 * 截取花色 返回对应花色的值，方便排序
	 * @param content
	 * @return 大王6 小王5  黑红梅方  4321
	 */
	public static Integer getColor(String content) {
		String color = String.valueOf(content.charAt(0));
		if (color.equals("W")) {
			return 6;
		} else if (color.equals("w")) {
			return 5;
		} else if (color.equals("B")) {
			return 4;
		} else if (color.equals("R")) {
			return 3;
		} else if (color.equals("M")) {
			return 2;
		} else {
			return 1;
		}
	}

	/**
	 *
	 * @param pai
	 * @param type asc desc
	 * @return
	 */
	public static List<String> sortList(List<String> pai, String type) {
		for (int i = 0; i < pai.size() - 1; i++) {
			for (int j = 0; j < pai.size() - i - 1; j++) {
				if ("asc".equals(type)) {
					if (comparePai(pai.get(j), pai.get(j + 1)) > 0) { // 把小的值交换到后面
						String temp = pai.get(j);
						pai.set(j, pai.get(j + 1));
						pai.set(j + 1, temp);
					}
				} else if ("desc".equals(type)) {
					if (comparePai(pai.get(j), pai.get(j + 1)) < 0) { // 把小的值交换到后面
						String temp = pai.get(j);
						pai.set(j, pai.get(j + 1));
						pai.set(j + 1, temp);
					}
				}
			}
		}
		return pai;
	}

	/**
	 * 自定义牌比较大小
	 * 
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public static int comparePai(String arg0, String arg1) {
		if (arg0.equals(arg1)) {
			return 0;
		} else {
			if (getNumbers(arg0) > getNumbers(arg1)) {
				return 1;
			} else if (getNumbers(arg0) < getNumbers(arg1)) {
				return -1;
			} else {// 两者数值相等，判断花色
				if (getColor(arg0) > getColor(arg1)) {
					return 1;
				} else if (getColor(arg0) < getColor(arg1)) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}
	
	public static boolean is8zha(List<String> pai) {
		boolean bl = false;
		if(pai.size()==8) {
			int temp = 0;
			int flag = 0;
			for(String p : pai) {
				int ip = getNumbers(p);
				if(temp != ip) {
					temp = ip;
					flag++;
				}
			}
			if(flag==1) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean is7zha(List<String> pai) {
		boolean bl = false;
		if(pai.size()==7) {
			int temp = 0;
			int flag = 0;
			for(String p : pai) {
				int ip = getNumbers(p);
				if(temp != ip) {
					temp = ip;
					flag++;
				}
			}
			if(flag==1) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean is6zha(List<String> pai) {
		boolean bl = false;
		if(pai.size()==6) {
			int temp = 0;
			int flag = 0;
			for(String p : pai) {
				int ip = getNumbers(p);
				if(temp != ip) {
					temp = ip;
					flag++;
				}
			}
			if(flag==1) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean is5zha(List<String> pai) {
		boolean bl = false;
		if(pai.size()==5) {
			int temp = 0;
			int flag = 0;
			for(String p : pai) {
				int ip = getNumbers(p);
				if(temp != ip) {
					temp = ip;
					flag++;
				}
			}
			if(flag==1) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean is4zhang(List<String> pai) {
		boolean bl = false;
		if(pai.size()==4) {
			if(getNumbers(pai.get(0)) == getNumbers(pai.get(1)) && getNumbers(pai.get(0)) == getNumbers(pai.get(2)) && getNumbers(pai.get(0)) == getNumbers(pai.get(3))) {
				bl = true;
			}
			for(String p : pai) {
				int ip = getNumbers(p);
				if(ip == 16) {
					bl = false;
					break;
				}
			}
		}
		return bl;
	}
	
	public static boolean is3zhang(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getNumbers(pai.get(0)) == getNumbers(pai.get(1))  && getNumbers(pai.get(0)) == getNumbers(pai.get(2))) {
				bl = true;
			}
			for(String p : pai) {
				int ip = getNumbers(p);
				if(ip == 16) {
					bl = false;
					break;
				}
			}
		}
		return bl;
	}
	
	public static boolean is510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			pai = sortList(pai, "asc");
			if(getNumbers(pai.get(0)) == 5 && getNumbers(pai.get(1)) == 10 && getNumbers(pai.get(2)) == 13) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean isHt510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getColor(pai.get(0)) == 4 && getColor(pai.get(1)) == 4 && getColor(pai.get(2)) == 4 && is510k(pai)) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean isHx510k(List<String> pai) {
		boolean bl = false;//B5 B10 B13
		if(pai.size()==3) {
			if(getColor(pai.get(0)) == 3 && getColor(pai.get(1)) == 3 && getColor(pai.get(2)) == 3 && is510k(pai)) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean isMh510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getColor(pai.get(0)) == 2 && getColor(pai.get(1)) == 2 && getColor(pai.get(2)) == 2 && is510k(pai)) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean isFk510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getColor(pai.get(0)) == 1 && getColor(pai.get(1)) == 1 && getColor(pai.get(2)) == 1 && is510k(pai)) {
				bl = true;
			}
		}
		return bl;
	}
	
	public static boolean isMin510k(List<String> pai) {
		boolean bl = false;
		if(pai.size()==3) {
			if(getColor(pai.get(0)) != getColor(pai.get(1)) || getColor(pai.get(0)) != getColor(pai.get(2)) || getColor(pai.get(1)) != getColor(pai.get(2))) {
				if(is510k(pai)) {
					bl = true;
				}
			}
		}
		return bl;
	}
	
	public static boolean is4wang(List<String> pai) {
		boolean bl = true;
		if(pai.size()==4) {
			for(String p : pai) {
				if(getNumbers(p) != 16) {
					bl = false;
					break;
				}
			}
		} else {
			bl = false;
		}
		return bl;
	}


	public static String isShunzi(List<String> pai) {
		if(pai.size()==0) {
			return "false";
		}

		pai = sortList(pai, "asc");
		String bool = "false";;
		if(pai.size() < 5) {
			bool = "false";;
			return "false";
		}
		List<String> ls1 = new ArrayList<>();
		for (int i = 0; i < pai.size(); i++) {
			 int num = getNumbers(pai.get(i));
			 ls1.add(pai.get(i));
		}
		sortList(ls1, "asc");
		 boolean  flag = true;
			for (int i=1;i<ls1.size();i++){
				 int index1 =getNumbers(ls1.get(i-1))+1;
				 int index2 = getNumbers(ls1.get(i));
				 if( index2>=15){
					 flag= false;
					 break;
				 }
				if((index1!=index2)){
					flag= false;
					break;
				}
			}
			if(flag){
				bool = "shunzi1";
			}


		if("shunzi1".equals(bool)){
				return "shunzi";
		}
		return bool;
	}
	public static boolean is3wang(List<String> pai) {
		boolean bl = true;
		if(pai.size()==3) {
			for(String p : pai) {
				if(getNumbers(p) != 16) {
					bl = false;
					break;
				}
			}
		} else {
			bl = false;
		}
		return bl;
	}
	/**
	 * 判断一组牌是否为四带2
	 * @param paistr
	 * @return
	 */
	public static boolean isSiDaiEr(List<String> paistr) {
		if(paistr.size()!=6){
			return false;
		}
		String[] pai = new String[]{paistr.get(0),paistr.get(1),paistr.get(2),paistr.get(3),paistr.get(4),paistr.get(5)};
		pai = sortAry(pai, "asc");
		int[] ar =	paiToShortIntAry(pai);
		String patStr = paiToString(ar);
		//正则表达式
		String pa1 ="^[^4]*4[^4]*$";
		Pattern p = Pattern.compile(pa1);
		Matcher matcher = p.matcher(patStr);
		boolean m3 = matcher.matches();
		if(m3){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 牌转短int数组
	 * @param pai
	 * @return
	 */
	public static int[] paiToShortIntAry(String[] pai){
		int[] ary = {0,0,0,0,0,0,0,0, 0, 0, 0, 0, 0,0,0,0,0};
		for (int i = 0; i < pai.length; i++) {
			int p = getNumbers(pai[i]);
			ary[p-3]++;
		}
		return ary;
	}

	public static boolean is2dawang(List<String> pai) {
		boolean bl = false;
		if(pai.size()==2) {
			if(getColor(pai.get(0)) == 6 && getColor(pai.get(1)) == 6) {
				bl = true;
			}
		} else {
			bl = false;
		}
		return bl;
	}
	
	public static boolean is2xiaowang(List<String> pai) {
		boolean bl = false;
		if(pai.size()==2) {
			if(getColor(pai.get(0)) == 5 && getColor(pai.get(1)) == 5) {
				bl = true;
			}
		} else {
			bl = false;
		}
		return bl;
	}
	
	public static boolean is1dw1xw(List<String> pai) {
		boolean bl = false;
		if(pai.size()==2) {
			if((getColor(pai.get(0)) == 6 && getColor(pai.get(1)) == 5) || (getColor(pai.get(0)) == 5 && getColor(pai.get(1)) == 6)) {
				bl = true;
			}
		} else {
			bl = false;
		}
		return bl;
	}
	
	public static boolean isDanzhang(List<String> pai) {
		if(pai.size()==1) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isDuizi(List<String> pai) {
		if(pai.size()==2) {
			if(getNumbers(pai.get(0)) == getNumbers(pai.get(1)) &&getNumbers(pai.get(0))!=16) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * 是否连对
	 * @param pai
	 * @return
	 */
	public static boolean isLiandui(List<String> pai) {
		boolean bl = true;
		if(pai.size()%2 == 0 && pai.size() >=4) {
			List<Integer> xblist = new ArrayList<Integer>();
			int[] ary = paiToShortAry(pai);
			if(ary[13] != 0 || ary[12] != 0) {//若连对里包含王
				bl = false;
			} else {
				for (int i = 0; i < ary.length; i++) {
					if(ary[i] != 2 && ary[i] != 0) {//若牌型里有不等于2张的
						bl = false;
						break;
					}
					if(ary[i] == 2) {
						xblist.add(i);
					}
				}
				
				for (int i = 0; i < xblist.size()-1; i++) {
					if(xblist.get(i+1) - xblist.get(i) != 1) {//若连对的下标不连续
						bl = false;
						break;
					}
				}
				
			}
		} else {
			bl = false;
		}
		return bl;
	}

	public static boolean isLiandui2(List<String> pai) {
		boolean bl = true;
		if(pai.size()%2 == 0 && pai.size() >=6) {
			List<Integer> xblist = new ArrayList<Integer>();
			int[] ary = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			for (int i = 0; i < pai.size(); i++) {
				int p = getNumbers(pai.get(i));
				ary[p]++;
			}
			if(ary[16] != 0 || ary[15] != 0) {//若连对里包含王
				bl = false;
			} else {
				for (int i = 0; i < ary.length; i++) {
					if(ary[i] != 2 && ary[i] != 0) {//若牌型里有不等于2张的
						bl = false;
						break;
					}
					if(ary[i] == 2) {
						xblist.add(i);
					}
				}

				for (int i = 0; i < xblist.size()-1; i++) {
					if(xblist.get(i+1) - xblist.get(i) != 1) {//若连对的下标不连续
						bl = false;
						break;
					}
				}

			}
		} else {
			bl = false;
		}
		return bl;
	}
	public static boolean isLiandui3(List<String> pai) {
		boolean bool = false;
		List<String> ls1 = new ArrayList<>();
		List<String> ls2 = new ArrayList<>();
		if(pai.isEmpty() || pai.size()<=4){
			return false;
		}
		for (int i = 0; i < pai.size(); i++) {
			int num = getNumbers(pai.get(i));

			ls2.add(pai.get(i));
		}
		if(isLiandui2(ls2)){
			bool  = true;
		}else{
			bool = false;
		}
		return bool;
	}
	/**
	 * 是否连三张
	 * @param pai
	 * @return
	 */
	public static boolean isLian3zhang(List<String> pai) {
		boolean bl = true;
		if(pai.size()%3 == 0 && pai.size() >=6) {
			List<Integer> xblist = new ArrayList<Integer>();
			int[] ary = paiToShortAry(pai);
			if(ary[13] != 0) {//若连对里包含王
				bl = false;
			} else {
				for (int i = 0; i < ary.length; i++) {
					if(ary[i] != 3 && ary[i] != 0) {//若牌型里有不等于2张的
						bl = false;
						break;
					}
					if(ary[i] == 3) {
						xblist.add(i);
					}
				}
				
				for (int i = 0; i < xblist.size()-1; i++) {
					if(xblist.get(i+1) - xblist.get(i) != 1) {//若连对的下标不连续
						bl = false;
						break;
					}
				}
				
			}
		} else {
			bl = false;
		}
		return bl;
	}
	/**
	 * 是否连四张
	 * @param pai
	 * @return
	 */
	public static boolean isLian4zhang(List<String> pai) {
		boolean bl = true;
		if(pai.size()%4 == 0 && pai.size() >=8) {
			List<Integer> xblist = new ArrayList<Integer>();
			int[] ary = paiToShortAry(pai);
			if(ary[13] != 0) {//若连对里包含王
				bl = false;
			} else {
				for (int i = 0; i < ary.length; i++) {
					if(ary[i] != 4 && ary[i] != 0) {//若牌型里有不等于2张的
						bl = false;
						break;
					}
					if(ary[i] == 4) {
						xblist.add(i);
					}
				}
				
				for (int i = 0; i < xblist.size()-1; i++) {
					if(xblist.get(i+1) - xblist.get(i) != 1) {//若连对的下标不连续
						bl = false;
						break;
					}
				}
			}
		} else {
			bl = false;
		}
		return bl;
	}


	
	/**
	 * 获取对子提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsDuizi(List<String> sjp, List<String> zjp) {
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		String[] zjpary = new String[zjp.size()];
		zjp.toArray(zjpary);
		combinationDz(zjpary, 2, list, 2);
		int snum = getNumbers(sjp.get(0));
		for (int i = 0; i < list.size(); i++) {
			List<String> l = list.get(i);
			if(getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
				all.add(l);
			}
		}
		return all;
	}

	
	/**
	 * 获取三张提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs3zhang(List<String> sjp, List<String> zjp) {
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 3) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination3zhang(zjpary, 3, list, 3);
		int snum = getNumbers(sjp.get(0));
		for (int i = 0; i < list.size(); i++) {
			List<String> l = list.get(i);
			if(getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
				all.add(l);
			}
		}
		return all;
	}
	

	
	/**
	 * 获取4张提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs4zhang(List<String> sjp, List<String> zjp) {
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 4) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination4zhang(zjpary, 4, list, 4);
		int snum = getNumbers(sjp.get(0));
		for (int i = 0; i < list.size(); i++) {
			List<String> l = list.get(i);
			if(getNumbers(l.get(0)) > snum && !isHave1(l, all)) {
				all.add(l);
			}
		}
		return all;
	}
	



	
	/**
	 * 获取副50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsMin50k(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combinationMin50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取方块50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsFk50k(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combinationFk50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取梅花50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsMh50k(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combinationMh50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取红心50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsHx50k(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combinationHx50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取黑桃50K提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTsHt50k(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(2);xblist.add(7);xblist.add(10);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combinationHt50k(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取2w提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs2XiaoWang(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> ts = new ArrayList<String>();
		if(zjp1.contains("w16")) {
			ts.add("w16");
			zjp1.remove("w16");
		}
		if(zjp1.contains("w16")) {
			ts.add("w16");
			zjp1.remove("w16");
		}
		if(ts.size() == 2) {
			list.add(ts);
		}
		return list;
	}
	
	/**
	 * 获取1dw1xw提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs1dw1xw(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> ts = new ArrayList<String>();
		if(zjp1.contains("W16")) {
			ts.add("W16");
			zjp1.remove("W16");
		}
		if(zjp1.contains("w16")) {
			ts.add("w16");
			zjp1.remove("w16");
		}
		if(ts.size() == 2) {
			list.add(ts);
		}
		return list;
	}
	
	/**
	 * 获取2W提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs2DaWang(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> ts = new ArrayList<String>();
		if(zjp1.contains("W16")) {
			ts.add("W16");
			zjp1.remove("W16");
		}
		if(zjp1.contains("W16")) {
			ts.add("W16");
			zjp1.remove("W16");
		}
		if(ts.size() == 2) {
			list.add(ts);
		}
		return list;
	}
	
	/**
	 * 获取3W提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs3Wang(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		xblist.add(13);
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination3Wang(zjpary, 3, list, 3);
		return list;
	}
	
	/**
	 * 获取4W提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs4Wang(List<String> sjp, List<String> zjp) {
		
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> ts = new ArrayList<String>();
		if(zjp1.contains("W16")) {
			ts.add("W16");
			zjp1.remove("W16");
		}
		if(zjp1.contains("W16")) {
			ts.add("W16");
			zjp1.remove("W16");
		}
		if(zjp1.contains("w16")) {
			ts.add("w16");
			zjp1.remove("w16");
		}
		if(zjp1.contains("w16")) {
			ts.add("w16");
			zjp1.remove("w16");
		}
		if(ts.size() == 4) {
			list.add(ts);
		}
		return list;
	}
	
	/**
	 * 获取5炸提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs5zha(List<String> sjp, List<String> zjp) {
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 5) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination5zha(zjpary, 5, list, 5);
		if(is5zha(sjp)) {
			int snum = getNumbers(sjp.get(0));
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(getNumbers(l.get(0)) > snum  && !isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
		} else {
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(!isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
			//return list;
		}
	}
	
	/**
	 * 获取6炸提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs6zha(List<String> sjp, List<String> zjp) {
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 6) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination6zha(zjpary, 6, list, 6);
		
		if(is6zha(sjp)) {
			int snum = getNumbers(sjp.get(0));
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(getNumbers(l.get(0)) > snum  && !isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
		} else {
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(!isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
		}
	}
	
	/**
	 * 获取7炸提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs7zha(List<String> sjp, List<String> zjp) {
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 7) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination7zha(zjpary, 7, list, 7);
		if(is7zha(sjp)) {
			int snum = getNumbers(sjp.get(0));
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(getNumbers(l.get(0)) > snum  && !isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
		} else {
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(!isHave1(l, all)) {
					all.add(l);
				}
			}
			return all;
		}
	}
	
	/**
	 * 获取8炸提示
	 * @param sjp
	 * @param zjp
	 * @return
	 */
	public static List<List<String>> getTs8zha(List<String> sjp, List<String> zjp) {
		
		List<List<String>> all = new ArrayList<List<String>>();
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> zjp1 = new ArrayList<String>();
		zjp1.addAll(zjp);
		List<String> zjp2 = new ArrayList<String>();
		zjp2.addAll(zjp);
		
		List<Integer> xblist = new ArrayList<Integer>();
		int[] ary = paiToShortAry(zjp);
		for (int i = 0; i < ary.length; i++) {
			if(ary[i] >= 8) {
				xblist.add(i);
			}
		}
		for (int i = 0; i < zjp1.size(); i++) {
			int num = getNumbers(zjp1.get(i));
			if(!xblist.contains(num - 3)) {
				zjp2.remove(zjp1.get(i));
			}
		}
		
		String[] zjpary = new String[zjp2.size()];
		zjp2.toArray(zjpary);
		combination8zha(zjpary, 8, list, 8);
		if(is8zha(sjp)) {
			int snum = getNumbers(sjp.get(0));
			for (int i = 0; i < list.size(); i++) {
				List<String> l = list.get(i);
				if(getNumbers(l.get(0)) > snum) {
					all.add(l);
				}
			}
			return all;
		} else {
			return list;
		}
	}



	public static List<String> copyList(List<String> ts){
		List<String> l = new ArrayList<String>();
		for (String pai :ts ) {
			l.add(pai);
		}
		return l;
	}


	/**
	 * 获取出牌类型
	 * @param pai
	 * @return
	 */
	public static String getCpType2(List<String> pai,int is3dai2,int sidai2dui,int fjkbd) {
		String type = "";
		if(isDanzhang(pai)) {
			type = "danz";
		} else if(isDuizi(pai)) {
			type = "duiz";
		} else if(isLiandui3(pai)) {
			type = "ld";
		} else if(is3zhang(pai) ) {
			type = "3z";
		} else if(is3dai1(pai)) {
			type = "3d1";
		} else if(is3dai1dui(pai) && is3dai2==1) {
			type = "3d2";
		}else if(is4zhang(pai)) {
			type = "boom";
		}else if(isSiDaiEr(pai)) {
			type = "4d2";
		}else if(isSiDai2dui(pai)&&sidai2dui==1) {
			type = "4d2dui";
		}else if(isJMBoom(pai)) {
			return "jmBoom";
		} else if(is1dw1xw(pai)) {
			type = "wboom";
		}
		String shunzitype = isShunzi(pai);
		String[] p2 = listToStringAry(pai);
		if(!shunzitype.equals("false")){
			type = shunzitype;
		}
		String fjtype = isFJ(pai);
		if("fjd0".equals(fjtype)&&fjkbd==1){
			type = "fjd0";//飞机不带 连三张
		}else if("fjddui".equals(fjtype) &&is3dai2==1){
		 	type = "fjddui";//飞机带对
		}else if("fjddan".equals(fjtype) && !isBoom(p2)){
		 	type = "fjddan";//飞机带单
		}
		return type;
	}

	public static boolean isFeiJi(String[] pai) {
		if(pai.length==0) {
			return false;
		}
		boolean bool = false;

		pai = sortAry(pai, "asc");
		//找出里面所有3个相同的
		//判断三个相同的是否连续
		//没连续则false
		//有几个连续则需要几个翅膀，若翅膀数小于剩余牌数，则false
		//若翅膀数大于剩余牌数，一、若没牌了true，二、若有牌则false
		List<List<String>> planeList = new ArrayList<List<String>>();
		for (int i = 0; i < pai.length-2; i++) {
			List<String> plane = new ArrayList<String>();
			int m = getNumbers(pai[i]);
			int n = getNumbers(pai[i+1]);
			int k = getNumbers(pai[i+2]);
			if(m==n && m==k && m!=15) {
				plane.add(pai[i]);
				plane.add(pai[i+1]);
				plane.add(pai[i+2]);
				planeList.add(plane);
			}
		}

		List<List<String>> planeList1 = new ArrayList<List<String>>();
		List<List<String>> planeList2 = new ArrayList<List<String>>();
		planeList1.addAll(planeList);
		for(List<String> l : planeList1) {
			if(!isHave(l, planeList2)) {
				planeList2.add(l);
			}
		}
		planeList = new ArrayList<List<String>>();
		planeList.addAll(planeList2);
		int psize = planeList.size();
		int pailength = pai.length;
		if(psize > 1) {//至少有2个三张相同的
			//判断连续的有几个，并把连续的保存下来
			int lxgs = 1;
			int lxgs1 = 1;
			// 取出改牌组里三同连续个数最大的num
			for (int i = 0; i < planeList.size() - 1; i++) {
				int m = getNumbers(planeList.get(i).get(0));
				int n = getNumbers(planeList.get(i + 1).get(0));
				if (n - m == 1) {
					lxgs++;
				}
			}
			//飞机不带翅膀 3*n
			if(pailength == 3*lxgs){
//				 System.out.println("3n");
				bool = true;
			}
			//飞机带单翅膀3*n +n

			if(pailength > 3*lxgs && (pailength- 3*lxgs==lxgs)){
				//n单张
				bool = false;
			}
			if(pailength > 3*lxgs && (pailength- 3*lxgs==2*lxgs)){
				// n对子
				String str =planeList.toString();
				List<String> syplist = new ArrayList<String>();
				for (int i = 0; i < pai.length; i++) {
					if(!str.contains(pai[i])){
						syplist.add(pai[i]);
					}
				}
				if(syplist.size()==2*lxgs){
					if(isLiandui(syplist)){
						bool  = true;
					}
				}
			}
			//飞机带双翅膀3*n+2*n
		}
		return bool;
	}
	public static String isFeiJi2(List<String> pai ) {
		if(pai.size()==0) {
			return "";
		}
		String bool = "";
		pai = sortPai(pai, "asc");
		List<String> new_pai = new ArrayList<>(pai);
		//找出里面所有3个相同的
		//判断三个相同的是否连续
		//没连续则false
		//有几个连续则需要几个翅膀，若翅膀数小于剩余牌数，则false
		//若翅膀数大于剩余牌数，一、若没牌了true，二、若有牌则false
		List<List<String>> planeList = new ArrayList<List<String>>();
		for (int i = 0; i < pai.size()-2; i++) {
			List<String> plane = new ArrayList<String>();
			int m = getNumbers(pai.get(i));
			int n = getNumbers(pai.get(i+1));
			int k = getNumbers(pai.get(i+2));
			if(m==n && m==k && m<15) {
				plane.add(pai.get(i));
				plane.add(pai.get(i+1));
				plane.add(pai.get(i+2));
				planeList.add(plane);
			}
		}
		List<List<String>> planeList1 = new ArrayList<List<String>>();
		List<List<String>> planeList2 = new ArrayList<List<String>>();
		planeList1.addAll(planeList);
		for(List<String> l : planeList1) {
			if(!isHave(l, planeList2)) {
				planeList2.add(l);
			}
		}
		planeList = new ArrayList<List<String>>();
		planeList.addAll(planeList2);
		int psize = planeList.size();
		int pailength = pai.size();
		if(psize > 1) {//至少有2个三张相同的
			//判断连续的有几个，并把连续的保存下来
			int lxgs = 1;
			// 取出改牌组里三同连续个数最大的num
			for (int i = 0; i < planeList.size() - 1; i++) {
				int m = getNumbers(planeList.get(i).get(0));
				int n = getNumbers(planeList.get(i + 1).get(0));
				if (Math.abs(m - n )== 1) {
					lxgs++;
				}
			}
			//飞机不带翅膀 3*n
			if(pailength == 3*lxgs){
				bool = "3n";
				return bool;
			}
			//飞机带单翅膀3*n +n
			if(pailength > 3*lxgs && (pailength- 3*lxgs==lxgs)){
				//n单张
				bool = "3n1";
				return  bool;
			}
			if(pailength > 3*lxgs && (pailength- 3*lxgs==2*lxgs)){
				// n对子
				String str =planeList.toString();
				List<String> syplist = new ArrayList<String>();
				for (int i = 0; i < pai.size(); i++) {
					if(!str.contains(pai.get(i))){
						syplist.add(pai.get(i));
					}
				}
				sortList(syplist,"asc");
				if(syplist.size()==2*lxgs){
				 	 if(isAllDuizi(syplist)){
				 		return "3n2n";
					 }
				}
			}

			//带牌中含炸弹
			 if(haveBoom(pai)){
				 List<Integer> p_list = isHavePlaneInLists(new_pai);
				 if(!p_list.isEmpty() && p_list.size()>=2){
					 Collections.sort(p_list);
					 if(p_list.size()==2){
						 //2连飞机
						 if(p_list.get(0)+1==p_list.get(1)){
							 //剩余牌是2个对子或者炸弹
							 List<String> restpai =getRestPai(new_pai,p_list);
							 String[]  ar=listToStringAry(restpai);
							 if(isBoom(ar)){
								 return "3n2n";
							 }
						 }
					 }else if(p_list.size()==3){
						 if(p_list.get(0)+1==p_list.get(1) && p_list.get(0)+2==p_list.get(2)){
							 List<String> restpai =getRestPai(new_pai,p_list);
							 int[] intrest =paiToShortAry(restpai);
							 int he =0;
							 boolean restDuizi = true;
							 for (int s :intrest){
								 he+=s;

							 }
							 for (int s :intrest){
								 if(s==0 || s==2 || s==4){
									 continue;
								 }else{
									 restDuizi = false;
									 break;
								 }
							 }
							 if(he==6 && restDuizi){
								 return "3n2n";
							 }
						 }
					 }
				 }
				 //飞机带双翅膀3*n+2*n
			 }
		}
		return bool;
	}
	private static  boolean haveBoom(List<String> pai){
		List<String> ls =  new ArrayList<>(pai);
		for (String p : pai) {
			int num = getNumbers(p);
			int count =getNumInPai(num,ls);
			if(count==4){
				return true;
			}
		}
		return  false;
	}
	private static List<String> getRestPai(List<String> new_pai, List<Integer> p_list) {
		List<String> pai = new ArrayList<>(new_pai);
		List<String> pai2 = new ArrayList<>(new_pai);
		for (String str:pai){
			int num = getNumbers(str);
			if(p_list.contains(num)){
				pai2.remove(str);
			}
		}
		return pai2;
	}

	private  static  List<Integer> isHavePlaneInLists(List<String> pai){
		List<String> pai3 = new ArrayList<>(pai);
		List<Integer> boomlist = new ArrayList<>();
		for (String str:pai){
			int pai2 = getNumbers(str);
			int num =	getNumInPai(pai2,pai3);
			if(num==3 && !boomlist.contains(pai2)){
				boomlist.add(pai2);
			}
		}
		return boomlist;
	}

	private static int getNumInPai(int pai2, List<String> pai3) {
		int num=0;
		for (String str : pai3) {
			if(getNumbers(str)==pai2){
				num++;
			}
		}
		return num;
	}

	private static boolean isAllDuizi(List<String> pai) {
            boolean bl = true;
            if(pai.size()%2 == 0 && pai.size() >=4) {
                List<Integer> xblist = new ArrayList<Integer>();
                int[] ary = paiToShortAry(pai);
                //if (ary[13] != 0 || ary[12] != 0) {//若连对里包含王
				if (ary[13] != 0){
						bl = false;
                } else {
                    for (int i:ary ) {
                        if(i!=0 && i!=2){
                            return false;
                        }
                    }
                }
            }
            return bl;
    }

    /**
	 * 是否为三带一对子
	 * @param pai
	 * @return
	 */
	public static boolean is3dai1dui(List<String> pai) {
		if(pai.size()==0) {
				return false;
		}

		boolean bool = false;
		if(is5zha(pai)) {
			return false;
		}

		if(pai.size() < 3 || pai.size() > 5) {
				 bool = false;
				 return bool;
		}
		int[] ar =	paiToShortAry(pai);
		String patStr = paiToString(ar);
		//正则表达式
		String pa1 ="^[^3]*3[^3]*$";
		Pattern p = Pattern.compile(pa1);
		Matcher matcher = p.matcher(patStr);
		boolean m3 = matcher.matches();
		String pa2 ="^[^2]*2[^2]*$";
		Pattern p2 = Pattern.compile(pa2);
		Matcher matcher2 = p2.matcher(patStr);
		boolean m2 = matcher2.matches();
		if(m2 && m3){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 判断一组牌是否为炸弹
	 * @param pai
	 * @return
	 */
	public static boolean isBoom(String[] pai) {
		if(pai.length==0) {
			return false;
		}
		if(pai.length == 2) {
			if(pai[0].equals("w16") && pai[1].equals("W16")){
				return true;
			}else if(pai[1].equals("w16") && pai[0].equals("W16")){
				return true;
			}else{
				return false;
			}
		}
		boolean bool = true;
		if(pai.length != 4) {
			bool = false;
			return bool;
		}
		int tmp = 0;
		for (int i = 0; i < pai.length; i++) {
			int m = getNumbers(pai[i]);
			if(tmp != 0) {
				if(m != tmp) {
					bool = false;
					return bool;
				}
			}
			tmp = m;
		}

		return bool;
	}
	/**
	 * 是否为三带一
	 * @param pai
	 * @return
	 */
	public static boolean is3dai1(List<String> pai) {
		if(pai.size()==0) {
			return false;
		}

		boolean bool = false;

		if(pai.size() !=4) {
			bool = false;
			return bool;
		}
		String[] paarr = new String[]{pai.get(0),pai.get(1),pai.get(2),pai.get(3)};
		if(isBoom(paarr)) {
			return false;
		}

		int[] ar =	paiToShortAry(pai);
		String patStr = paiToString(ar);
		//正则表达式
		String pa1 ="^[^3]*3[^3]*$";
		Pattern p = Pattern.compile(pa1);
		Matcher matcher = p.matcher(patStr);
		boolean m3 = matcher.matches();
		String pa2 ="^[^1]*1[^1]*$";
		Pattern p2 = Pattern.compile(pa2);
		Matcher matcher2 = p2.matcher(patStr);
		boolean m2 = matcher2.matches();
		if(m2 && m3){
			return true;
		}else{
			return false;
		}
	}
	public static String paiToString(int[] pai){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<pai.length;i++){
			sb.append(String.valueOf(pai[i]));
		}
		return sb.toString();
	}


	/////////////
	public static boolean isZhadan(List<String> pai) {
		boolean bl = false;
		if(isMin510k(pai) || isFk510k(pai) || isMh510k(pai) || isHx510k(pai) || isHt510k(pai)
				|| is2dawang(pai) || is2xiaowang(pai) || is1dw1xw(pai) || is5zha(pai) || is3wang(pai)
				|| is4wang(pai) || is6zha(pai) || is7zha(pai) || is8zha(pai)) {
			bl = true;
		}
		return bl;
	}
	
	/**
	 * 判断该组合在所有组合中是否已经存在，存在返回true，不存在返回false
	 * @param list
	 * @param all
	 * @return
	 */
	public static boolean isHave(List<String> list, List<List<String>> all) {
		for (int i = 0; i < all.size(); i++) {
			List<String> a1 = all.get(i);
			if(a1.size()==list.size() && getListSum(a1) == getListSum(list)) {
				return true;
			}
		}
		return false;
	}
	
	public static int getListSum(List<String> list) {
		int sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += getNumbers(list.get(i)) + getColor(list.get(i)) * 100;
		}
		return sum;
	}
	
	public static boolean isHave2(List<String> list, List<List<String>> all) {
		for (int i = 0; i < all.size(); i++) {
			List<String> a1 = all.get(i);
			if(a1.size()==list.size() && getListSum2(a1) == getListSum2(list)) {
				return true;
			}
		}
		return false;
	}
	
	public static int getListSum2(List<String> list) {
		int sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += getNumbers(list.get(i));
		}
		return sum;
	}
	
	public static boolean isHave1(List<String> list, List<List<String>> all) {
//		for (int i = 0; i < all.size(); i++) {
//			List<String> a1 = all.get(i);
//			if(a1.size()==list.size() && getListSum1(a1) == getListSum1(list)) {
//				return true;
//			}
//		}
//		return false;
		return false;
	}
	
	public static int getListSum1(List<String> list) {
		int sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += getNumbers(list.get(i));
		}
		return sum;
	}
	
	public static void combinationDz(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationDz("", ia, n, list, dpnum);
    }

    public static void combinationDz(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isDuizi(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationDz(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination3zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination3zhang("", ia, n, list, dpnum);
    }

    public static void combination3zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is3zhang(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination3zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination4zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination4zhang("", ia, n, list, dpnum);
    }

    public static void combination4zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is4zhang(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination4zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationLiandui(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationLiandui("", ia, n, list, dpnum);
    }

    public static void combinationLiandui(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isLiandui(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationLiandui(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationLian3zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationLian3zhang("", ia, n, list, dpnum);
    }

    public static void combinationLian3zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isLian3zhang(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationLian3zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationLian4zhang(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationLian4zhang("", ia, n, list, dpnum);
    }

    public static void combinationLian4zhang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isLian4zhang(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationLian4zhang(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationMin50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationMin50k("", ia, n, list, dpnum);
    }

    public static void combinationMin50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isMin510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationMin50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination50k("", ia, n, list, dpnum);
    }

    public static void combination50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationFk50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationFk50k("", ia, n, list, dpnum);
    }

    public static void combinationFk50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isFk510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationFk50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationMh50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationMh50k("", ia, n, list, dpnum);
    }

    public static void combinationMh50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isMh510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationMh50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationHx50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationHx50k("", ia, n, list, dpnum);
    }

    public static void combinationHx50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isHx510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationHx50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationHt50k(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combinationHt50k("", ia, n, list, dpnum);
    }

    public static void combinationHt50k(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(isHt510k(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationHt50k(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination3Wang(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination3Wang("", ia, n, list, dpnum);
    }

    public static void combination3Wang(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is3wang(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination3Wang(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    
    public static void combination6zha(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination6zha("", ia, n, list, dpnum);
    }

    public static void combination6zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is6zha(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination6zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination7zha(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination7zha("", ia, n, list, dpnum);
    }

    public static void combination7zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is7zha(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination7zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination8zha(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination8zha("", ia, n, list, dpnum);
    }

    public static void combination8zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is8zha(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination8zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combination5zha(String[] ia, int n, List<List<String>> list, int dpnum) {
    	combination5zha("", ia, n, list, dpnum);
    }

    public static void combination5zha(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                List<String> pai = Arrays.asList(iary);
                if(is5zha(pai)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combination5zha(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    
    public static void combinationl3z(List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
    	List<String> l = new ArrayList<String>();
    	combinationl3z(l, ia, n, list, dpnum);
    }

    public static void combinationl3z(List<String> s, List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.size(); i++) {
                List<String> totalStr = new ArrayList<String>();
                totalStr.addAll(s); totalStr.addAll(ia.get(i));
                String[] iary = new String[totalStr.size()];
                for (int j = 0; j < iary.length; j++) {
                	iary[j] = totalStr.get(j);
				}
                List<String> pai = Arrays.asList(iary);
                if(isLian3zhang(pai) && iary.length==dpnum*3) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum*3; j++) {
	                	list1.add(iary[j]);
					}
	                list.add(list1);
                }
            }
        } else {
            for (int i = 0; i < ia.size() - (n - 1); i++) {
            	List<String> ss = new ArrayList<String>();
            	ss.addAll(s);
            	ss.addAll(ia.get(i));
                List<List<String>> ii = new ArrayList<List<String>>();
                for (int j = 0; j < ia.size() - i - 1; j++) {
                	ii.add(ia.get(i + j + 1));
                }
                combinationl3z(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static void combinationl4z(List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
    	List<String> l = new ArrayList<String>();
    	combinationl4z(l, ia, n, list, dpnum);
    }

    public static void combinationl4z(List<String> s, List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.size(); i++) {
                List<String> totalStr = new ArrayList<String>();
                totalStr.addAll(s); totalStr.addAll(ia.get(i));
                String[] iary = new String[totalStr.size()];
                for (int j = 0; j < iary.length; j++) {
                	iary[j] = totalStr.get(j);
				}
                List<String> pai = Arrays.asList(iary);
                if(isLian4zhang(pai) && iary.length==dpnum*4) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum*4; j++) {
	                	list1.add(iary[j]);
					}
	                list.add(list1);
                }
            }
        } else {
            for (int i = 0; i < ia.size() - (n - 1); i++) {
            	List<String> ss = new ArrayList<String>();
            	ss.addAll(s);
            	ss.addAll(ia.get(i));
                List<List<String>> ii = new ArrayList<List<String>>();
                for (int j = 0; j < ia.size() - i - 1; j++) {
                	ii.add(ia.get(i + j + 1));
                }
                combinationl4z(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    public static Map<String, Object> isHaveXiqian(List<String> pai) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	List<String> sjp = new ArrayList<String>();
    	List<List<String>> _8list = getTs8zha(sjp, pai);
    	List<List<String>> _7list = getTs7zha(sjp, pai);
    	List<List<String>> _6list = getTs6zha(sjp, pai);
    	List<List<String>> _4wlist = getTs4Wang(sjp, pai);
    	List<List<String>> all = new ArrayList<List<String>>();
    	all.addAll(_8list);
    	all.addAll(_4wlist);
    	for(List<String> list : _7list) {
			int pnum = getNumbers(list.get(0));
			if(!isHaveInLists(pnum, _8list) && !isHaveInLists(pnum, all)) {
				all.add(list);
			}
		}
    	for(List<String> list : _6list) {
			int pnum = getNumbers(list.get(0));
			if(!isHaveInLists(pnum, _7list) && !isHaveInLists(pnum, all)) {
				all.add(list);
			}
		}
    	
    	if(_8list.size() + _7list.size() + _6list.size() + _4wlist.size() > 0) {
    		map.put("have", "1");
    		map.put("pai", all);
    	} else {
    		map.put("have", "0");
    		map.put("pai", all);
    	}
    	
    	return map;
    }
    
    public static boolean isHaveInLists(int pnum, List<List<String>> pai) {
    	boolean bl = false;
    	for(List<String> list : pai) {
    		int plnum = getNumbers(list.get(0));
    		if(pnum == plnum) {
    			bl = true;
    			break;
    		}
    	}
    	return bl;
    }
    
    /**
     * 排序
     * @param pai
     * @return
     */
    public static List<String> sortPai(List<String> pai, String type) {
    	List<String> slist = new ArrayList<String>();
    	List<String> sjp = new ArrayList<String>();
    	sjp.add("B0");
    	//pai = sortList(pai, "desc");
    	//获取所有炸弹，往左边排，从大到小获取一个remove一个
    	List<List<String>> _8zhalist = getTs8zha(sjp, pai);
    	for (int i = 0; i < _8zhalist.size(); i++) {
    		List<String> l = _8zhalist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _7zhalist = getTs7zha(sjp, pai);
    	for (int i = 0; i < _7zhalist.size(); i++) {
    		List<String> l = _7zhalist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _4wlist = getTs4Wang(sjp, pai);
    	for (int i = 0; i < _4wlist.size(); i++) {
    		List<String> l = _4wlist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _6zhalist = getTs6zha(sjp, pai);
    	for (int i = 0; i < _6zhalist.size(); i++) {
    		List<String> l = _6zhalist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _3wlist = getTs3Wang(sjp, pai);
    	for (int i = 0; i < _3wlist.size(); i++) {
    		List<String> l = _3wlist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _5zhalist = getTs5zha(sjp, pai);
    	for (int i = 0; i < _5zhalist.size(); i++) {
    		List<String> l = _5zhalist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _2Wlist = getTs2DaWang(sjp, pai);
    	for (int i = 0; i < _2Wlist.size(); i++) {
    		List<String> l = _2Wlist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
			slist.addAll(l);
		}
    	List<List<String>> _1dw1xwlist = getTs1dw1xw(sjp, pai);
    	for (int i = 0; i < _1dw1xwlist.size(); i++) {
    		List<String> l = _1dw1xwlist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
			slist.addAll(l);
		}
    	List<List<String>> _2wlist = getTs2XiaoWang(sjp, pai);
    	for (int i = 0; i < _2wlist.size(); i++) {
    		List<String> l = _2wlist.get(i);
    		//pai.removeAll(l);
    		pai = myRemoveAll(pai, l);
			slist.addAll(l);
		}

    	List<List<String>> _ht50k = getTsHt50k(sjp, pai);
    	List<String> _ht50kslist = new ArrayList<String>();
    	while(_ht50k.size() > 0) {
    		List<String> l = _ht50k.get(0);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
    		_ht50kslist.addAll(l);
    		_ht50k = getTsHt50k(sjp, pai);
    	}
		slist.addAll(_ht50kslist);
		
		List<List<String>> _hx50k = getTsHx50k(sjp, pai);
    	List<String> _hx50kslist = new ArrayList<String>();
    	while(_hx50k.size() > 0) {
    		List<String> l = _hx50k.get(0);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
    		_hx50kslist.addAll(l);
    		_hx50k = getTsHx50k(sjp, pai);
    	}
		slist.addAll(_hx50kslist);
		
    	List<List<String>> _mh50k = getTsMh50k(sjp, pai);
    	List<String> _mh50kslist = new ArrayList<String>();
    	while(_mh50k.size() > 0) {
    		List<String> l = _mh50k.get(0);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
    		_mh50kslist.addAll(l);
    		_mh50k = getTsMh50k(sjp, pai);
    	}
		slist.addAll(_mh50kslist);
    	

    	
    	List<List<String>> _fk50k = getTsFk50k(sjp, pai);
    	List<String> _fk50kslist = new ArrayList<String>();
    	while(_fk50k.size() > 0) {
    		List<String> l = _fk50k.get(0);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
    		_fk50kslist.addAll(l);
    		_fk50k = getTsFk50k(sjp, pai);
    	}
		slist.addAll(_fk50kslist);
    	
    	List<List<String>> _min50k = getTsMin50k(sjp, pai);
    	List<String> _min50kslist = new ArrayList<String>();
    	while(_min50k.size() > 0) {
    		List<String> l = _min50k.get(0);
    		pai = myRemoveAll(pai, l);
    		l = sortList(l, "desc");
    		_min50kslist.addAll(l);
    		_min50k = getTsMin50k(sjp, pai);
    	}
		slist.addAll(_min50kslist);
    	
    	
		if(type.equals("1")) {
			pai = sortList(pai, "desc");
			slist.addAll(pai);
    	} else {
    		List<List<String>> _4zhang = getTs4zhang(sjp, pai);
    		List<String> _4slist = new ArrayList<String>();
    		while(_4zhang.size() > 0) {
        		List<String> l = _4zhang.get(0);
        		pai = myRemoveAll(pai, l);
        		l = sortList(l, "desc");
        		_4slist.addAll(l);
    			_4zhang = getTs4zhang(sjp, pai);
        	}
    		_4slist = sortList(_4slist, "desc");
    		slist.addAll(_4slist);

    		List<List<String>> _3zhang = getTs3zhang(sjp, pai);
        	List<String> _3slist = new ArrayList<String>();
        	while(_3zhang.size() > 0) {
        		List<String> l = _3zhang.get(0);
        		pai = myRemoveAll(pai, l);
        		l = sortList(l, "desc");
        		_3slist.addAll(l);
    			_3zhang = getTs3zhang(sjp, pai);
        	}
        	_3slist = sortList(_3slist, "desc");
    		slist.addAll(_3slist);

        	List<List<String>> _duizi = getTsDuizi(sjp, pai);
        	List<String> _dzslist = new ArrayList<String>();
        	while(_duizi.size() > 0) {
        		List<String> l = _duizi.get(0);
        		pai = myRemoveAll(pai, l);
        		l = sortList(l, "desc");
        		_dzslist.addAll(l);
    			_duizi = getTsDuizi(sjp, pai);
        	}
        	_dzslist = sortList(_dzslist, "desc");
    		slist.addAll(_dzslist);

        	pai = sortList(pai, "desc");
			slist.addAll(pai);
    	}
    	return slist;
    }
    
    public static List<String> myRemoveAll(List<String> pai, List<String> l) {
    	for (String p : l) {
			if(pai.contains(p)) {
				pai.remove(p);
			}
		}
    	return pai;
    }


	public static void combination(String[] ia, int n, List<List<String>> list, int dpnum) {
		combination("", ia, n, list, dpnum);
	}

	public static void combination(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
		if (n == 1) {
			for (int i = 0; i < ia.length; i++) {
				String totalStr = s + ia[i];
				String[] iary = totalStr.split(",");
				List<String> list1 = new ArrayList<String>();
				for (int j = 0; j < dpnum; j++) {
					list1.add(iary[j]);
				}
				list.add(list1);
			}
		} else {
			for (int i = 0; i < ia.length - (n - 1); i++) {
				String ss = "";
				ss = s + ia[i] + ",";
				// 建立从i开始的子数组
				String[] ii = new String[ia.length - i - 1];
				for (int j = 0; j < ia.length - i - 1; j++) {
					ii[j] = ia[i + j + 1];
				}
				combination(ss, ii, n - 1, list, dpnum);
			}
		}
	}


	public static void combinationFj(List<String> s, List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
		if (n == 1) {
			for (int i = 0; i < ia.size(); i++) {
				List<String> totalStr = new ArrayList<String>();
				totalStr.addAll(s); totalStr.addAll(ia.get(i));
				String[] iary = new String[totalStr.size()];
				for (int j = 0; j < iary.length; j++) {
					iary[j] = totalStr.get(j);
				}
				//if(isLiandui(iary) && iary.length==dpnum*2) {
				if(isFeiJi(iary) && iary.length==dpnum*3) {
					List<String> list1 = new ArrayList<String>();
					for (int j = 0; j < dpnum*3; j++) {
						list1.add(iary[j]);
					}
					list.add(list1);
				}
			}
		} else {
			for (int i = 0; i < ia.size() - (n - 1); i++) {
				List<String> ss = new ArrayList<String>();
				ss.addAll(s);
				ss.addAll(ia.get(i));
				List<List<String>> ii = new ArrayList<List<String>>();
				for (int j = 0; j < ia.size() - i - 1; j++) {
					ii.add(ia.get(i + j + 1));
				}
				combinationFj(ss, ii, n - 1, list, dpnum);
			}
		}
	}


	public static boolean isSt(String[] pai) {
		if(pai.length==0) {
			return false;
		}
		pai = sortAry(pai, "asc");
		boolean bool = false;
		if(pai.length==3) {
			int m = getNumbers(pai[0]);
			int n = getNumbers(pai[1]);
			int k = getNumbers(pai[2]);
			if(m == n && m == k && m!=15) {
				bool = true;
			}
		}
		return bool;
	}

	public static void combinationSt(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
		if (n == 1) {
			for (int i = 0; i < ia.length; i++) {
				String totalStr = s + ia[i];
				String[] iary = totalStr.split(",");
				if(isSt(iary)) {
					List<String> list1 = new ArrayList<String>();
					for (int j = 0; j < dpnum; j++) {
						list1.add(iary[j]);
					}
					if(!isHave(list1, list)) {
						list.add(list1);
					}
				}
			}
		} else {
			for (int i = 0; i < ia.length - (n - 1); i++) {
				String ss = "";
				ss = s + ia[i] + ",";
				// 建立从i开始的子数组
				String[] ii = new String[ia.length - i - 1];
				for (int j = 0; j < ia.length - i - 1; j++) {
					ii[j] = ia[i + j + 1];
				}
				combinationSt(ss, ii, n - 1, list, dpnum);
			}
		}
	}

    public static void combinationDz1(String[] ia, int n, List<List<String>> list, int dpnum) {
        combinationDz1("", ia, n, list, dpnum);
    }

    public static void combinationDz1(String s, String[] ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.length; i++) {
                String totalStr = s + ia[i];
                String[] iary = totalStr.split(",");
                if(isDuizi(iary)) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum; j++) {
	                	list1.add(iary[j]);
					}
	                if(!isHave(list1, list)) {
	                	list.add(list1);
	                }
                }
            }
        } else {
            for (int i = 0; i < ia.length - (n - 1); i++) {
                String ss = "";
                ss = s + ia[i] + ",";
                // 建立从i开始的子数组
                String[] ii = new String[ia.length - i - 1];
                for (int j = 0; j < ia.length - i - 1; j++) {
                    ii[j] = ia[i + j + 1];
                }
                combinationDz(ss, ii, n - 1, list, dpnum);
            }
        }
    }
    
    
    public static void combinationLd(List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
    	List<String> l = new ArrayList<String>();
    	combinationLd(l, ia, n, list, dpnum);
    }

    public static void combinationLd(List<String> s, List<List<String>> ia, int n, List<List<String>> list, int dpnum) {
        if (n == 1) {
            for (int i = 0; i < ia.size(); i++) {
                List<String> totalStr = new ArrayList<String>();
                totalStr.addAll(s); totalStr.addAll(ia.get(i));
                String[] iary = new String[totalStr.size()];
                for (int j = 0; j < iary.length; j++) {
                	iary[j] = totalStr.get(j);
				}
                //if(isLiandui(iary) && iary.length==dpnum*2) {
                if(isLiandui(iary) && iary.length==dpnum*2) {
	                List<String> list1 = new ArrayList<String>();
	                for (int j = 0; j < dpnum*2; j++) {
	                	list1.add(iary[j]);
					}
	                list.add(list1);
                }
            }
        } else {
            for (int i = 0; i < ia.size() - (n - 1); i++) {
            	List<String> ss = new ArrayList<String>();
            	ss.addAll(s);
            	ss.addAll(ia.get(i));
                List<List<String>> ii = new ArrayList<List<String>>();
                for (int j = 0; j < ia.size() - i - 1; j++) {
                	ii.add(ia.get(i + j + 1));
                }
                combinationLd(ss, ii, n - 1, list, dpnum);
            }
        }
    }


	/**
	 * 判断一组牌是否为连对
	 * @param pai
	 * @return
	 */
	public static boolean isLiandui(String[] pai) {
		if(pai.length==0) {
			return false;
		}
		pai = sortAry(pai, "asc");
		boolean bool = true;
		if(pai.length%2 != 0 || pai.length < 4) {
			bool = false;
			return bool;
		}
		for (int i = 0; i < pai.length-2; i++) {
			if(getNumbers(pai[i+2]) - getNumbers(pai[i]) !=1 || (getNumbers(pai[i]) - getNumbers(pai[i+1]) !=0 && i%2==0)) {
				bool = false;
				return bool;
			}
		}
		
		return bool;
	}
	
	public static String[] sortAry(String[] pai, String type) {
		for (int i = 0; i < pai.length-1; i++) {
			for (int j = 0; j < pai.length-i-1; j++) {
				if("asc".equals(type)) {
					if(comparePai(pai[j], pai[j+1]) > 0){    //把小的值交换到后面
						String temp = pai[j];
						pai[j] =  pai[j+1];
						pai[j+1] = temp;
					}
				} else if("desc".equals(type)){
					if(comparePai(pai[j], pai[j+1]) < 0){    //把小的值交换到后面
						String temp = pai[j];
						pai[j] =  pai[j+1];
						pai[j+1] = temp;
					}
				}
				
			}
		}
		return pai;
	}
	
	/**
	 * 判断一组牌是否对子
	 * @param pai
	 * @return
	 */
	public static boolean isDuizi(String[] pai) {
		if(pai.length==0) {
			return false;
		}
		pai = sortAry(pai, "asc");
		boolean bool = false;
		if(pai.length==2) {
			int m = getNumbers(pai[0]);
			int n = getNumbers(pai[1]);
			if(m == n && m!=16) {
				bool = true;
			}
		}
		return bool;
	}

	public static void main(String[] args) {
		long  s = System.currentTimeMillis();
		List<String> pai = new ArrayList<>();
		pai.add("B13");
		pai.add("B13");
		pai.add("R14");
		pai.add("R14");
		pai.add("B12");
		pai.add("B12");
		System.out.println("---");
//		System.out.println("type::" +getCpType2(pai,1,1,1));
		long  e = System.currentTimeMillis();
		System.out.println("runtime:"+(e-s));
	}
	public static String[] listToStringAry(List<String> pai) {
		String[] a = new String[pai.size()];
		int i=0;
		for(String pa : pai){
			a[i] = pa;
			i++;
		}
		return a;
	}
	public static  List<String> intCardToStringCard(List<Integer> cardList){
		List<String> ls = new ArrayList<>();
		for (Integer in :cardList){
		//			方片 1 梅花2 洪涛3 黑桃4 5王
		//		B  R  M F
			if(in>100 && in <200){
				int p = in-100;
				ls.add("F"+p);
			}
			if(in>200 && in <300){
				int p = in-200;
				ls.add("M"+p);
			}
			if(in>300 && in <400){
				int p = in-300;
				ls.add("R"+p);
			}
			if(in>400 && in <500){
				int p = in-400;
				ls.add("B"+p);
			}
			if(501==in){
				ls.add("w16");
			}
			if(502==in){
				ls.add("W16");
			}
		}
		return ls;
	}
	public static  List<Integer> stringCardToIntCard(List<String> cardList){
		List<Integer> ls = new ArrayList<>();
		for (int  i=0;i<cardList.size();i++){
			String s =cardList.get(i);
			if(s.startsWith("F")){
				ls.add(getNumbers(s)+100);
			}
			if(s.startsWith("M")){
				ls.add(getNumbers(s)+200);
			}
			if(s.startsWith("R")){
				ls.add(getNumbers(s)+300);
			}
			if(s.startsWith("B")){
				ls.add(getNumbers(s)+400);
			}
			if(s.startsWith("w")){
				ls.add(501);
			}
			if(s.startsWith("W")){
				ls.add(502);
			}
		}
		return ls;
	}

	private static boolean isSiDai2dui(List<String> paistr){
		if(paistr.size()!=8)
			return false;
		Map<Integer, Integer> valAndNum = getValAndNum(paistr);

		int siNum=0;
		int duiNum=0;
		for (Integer num:valAndNum.values()) {
			if(num==4)
				siNum++;
			else if(num==2)
				duiNum++;
		}
		if(siNum==1&&duiNum==2)
			return true;
		return false;
	}

	private static boolean isJMBoom(List<String> paistr){
		if(paistr.size()<8||paistr.size()%4!=0)
			return false;
		Map<Integer, Integer> valAndNum = getValAndNum(paistr);

		int nowVal=0;
		for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
			if(entry.getValue()!=4||entry.getKey()==15)
				return false;
			if(nowVal==0)
				nowVal=entry.getKey();
			else {
				if(entry.getKey()-nowVal!=1)
					return false;
				else
					nowVal=entry.getKey();
			}
		}
		return true;
	}

	private static String isFJ(List<String> paistr){
		Map<Integer, Integer> valAndNum = getValAndNumById(stringCardToIntCard(paistr),"");
		int nowVal=0;
		int otherNum=0;
		int fjNum=0;
		int duiNum=0;
		boolean checkFJstart=false;
		boolean checkFJend=false;
		for (Map.Entry<Integer,Integer> entry:valAndNum.entrySet()) {
			Integer valueNum = entry.getValue();
			if(!checkFJend&&valueNum>=3){
				checkFJstart=true;
				fjNum++;
				if(valueNum==4)
					otherNum++;
				if(nowVal==0)
					nowVal=entry.getKey();
				else {
					if(entry.getKey()-nowVal!=1)
						return "";
					else
						nowVal=entry.getKey();
				}
			}else {
				if(checkFJstart)
					checkFJend=true;
				otherNum+=valueNum;
				if(valueNum==2)
					duiNum++;
			}
		}
		if(fjNum>1){
			if(otherNum==0){
				return "fjd0";
			}else if(otherNum==fjNum){
				return "fjddan";
			}else if(otherNum==2*fjNum&&fjNum==duiNum){
				return "fjddui";
			}
		}
		return "";
	}


	public static Map<Integer, Integer> getValAndNum(List<String> paistr){
		List<Integer> ids = stringCardToIntCard(paistr);
		Map<Integer,Integer> valAndNum=new TreeMap<>();
		for (Integer id:ids) {
			int val=id%100;
			if(valAndNum.containsKey(val))
				valAndNum.put(val,valAndNum.get(val)+1);
			else
				valAndNum.put(val,1);
		}
		return valAndNum;
	}

	public static Map<Integer, Integer> getValAndNumById(List<Integer> ids,String order){
		Map<Integer,Integer> valAndNum;
		if("desc".equals(order)){
			valAndNum=new TreeMap<>(new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o2-o1;
				}
			});
		}else {
			valAndNum=new TreeMap<>();
		}
		for (Integer id:ids) {
			int val=id%100;
			if(valAndNum.containsKey(val))
				valAndNum.put(val,valAndNum.get(val)+1);
			else
				valAndNum.put(val,1);
		}
		return valAndNum;
	}
	public static int getBoomNum(List<Integer> ids){
		Map<Integer, Integer> valAndNum = getValAndNumById(ids, "");
		int resultNum=0;
		for (int num:valAndNum.values()) {
			if(num==4)
				resultNum++;
		}
		return resultNum;
	}
}
