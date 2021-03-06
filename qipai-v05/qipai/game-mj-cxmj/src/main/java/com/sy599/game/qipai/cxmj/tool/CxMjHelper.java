package com.sy599.game.qipai.cxmj.tool;

import java.util.*;

import com.sy599.game.qipai.cxmj.constant.CxMj;
import org.apache.commons.lang.StringUtils;

public class CxMjHelper {

    /**
     * 麻将val的个数
     *
     * @param majiangs
     * @param majiangVal
     * @return
     */
    public static int getMajiangCount(List<CxMj> majiangs, int majiangVal) {
        int count = 0;
        for (CxMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取手里面最后一个牌值的麻将
     * @param majiangs
     * @param majiangVal
     * @return
     */
    public static CxMj getOneMajiang(List<CxMj> majiangs, int majiangVal) {
        for (CxMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal) {
               return majiang;
            }
        }
        return null;
    }

    /**
     * 麻将val的List
     *
     * @param majiangs
     * @param majiangVal
     * @return
     */
    public static List<CxMj> getMajiangList(List<CxMj> majiangs, int majiangVal) {
        List<CxMj> list = new ArrayList<>();
        for (CxMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal) {
                list.add(majiang);
            }
        }
        return list;
    }

    public static List<CxMj> getGangMajiangList(List<CxMj> majiangs, int majiangVal) {
        int num=0;
        List<CxMj> list = new ArrayList<>();
        for (CxMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal&&num<4) {
                list.add(majiang);
                num++;
            }
        }
        return list;
    }

    public  static List<CxMj>   canGang(List<CxMj> majiangs, int majiangVal) {
        int num=0;
        List<CxMj> list = new ArrayList<>();
        for (CxMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal) {
                list.add(majiang);
            }
        }
        return list;
    }


    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static List<Integer> toMajiangIds(List<CxMj> majiangs) {
        List<Integer> majiangIds = new ArrayList<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (CxMj majiang : majiangs) {
            majiangIds.add(majiang.getId());
        }
        return majiangIds;
    }

    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static String toMajiangStrs(List<CxMj> majiangs) {
        StringBuffer sb = new StringBuffer();
        if (majiangs == null) {
            return sb.toString();
        }
        for (CxMj majiang : majiangs) {
            sb.append(majiang.getId()).append(",");

        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static List<Integer> toMajiangVals(List<CxMj> majiangs) {
        List<Integer> majiangIds = new ArrayList<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (CxMj majiang : majiangs) {
            majiangIds.add(majiang.getVal());
        }
        return majiangIds;
    }


    /**
     * 麻将转化为Map<val,valNum>
     *
     * @param majiangs
     * @return
     */
    public static Map<Integer, Integer> toMajiangValMap(List<CxMj> majiangs) {
        Map<Integer, Integer> majiangIds = new HashMap<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (CxMj majiang : majiangs) {
            if (majiangIds.containsKey(majiang.getVal())) {
                majiangIds.put(majiang.getVal(), majiangIds.get(majiang.getVal()) + 1);
            } else {
                majiangIds.put(majiang.getVal(), 1);
            }
        }
        return majiangIds;
    }

    /**
     * 麻将Id转化为麻将
     *
     * @param majiangIds
     * @return
     */
    public static List<CxMj> toMajiang(List<Integer> majiangIds) {
        if (majiangIds == null) {
            return new ArrayList<>();
        }
        List<CxMj> majiangs = new ArrayList<>();
        for (int majiangId : majiangIds) {
            if (majiangId == 0) {
                continue;
            }
            majiangs.add(CxMj.getMajang(majiangId));
        }
        return majiangs;
    }




    public static List<CxMj> find(List<Integer> copy, List<Integer> valList) {
        List<CxMj> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    CxMj majiang = CxMj.getMajang(card);
                    if (majiang.getVal() == zpId) {
                        pai.add(majiang);
                        iterator.remove();
                        break;
                    }
                }
            }

        }
        return pai;
    }


    public static Map<Integer,List<Integer>> getGangList(List<CxMj> mjs){
        Map<Integer,List<Integer>> map=new HashMap<>();
        for (CxMj mj:mjs) {
            Integer val = mj.getVal();
            List<Integer> list = map.get(val);
            if(list==null){
                list=new ArrayList<>();
                map.put(val,list);
            }
            list.add(mj.getId());
        }

        Map<Integer,List<Integer>> rMap=new HashMap<>();
        for (Map.Entry<Integer,List<Integer>> entry:map.entrySet()){
            if(entry.getValue().size()==4)
                rMap.put(entry.getKey(),entry.getValue());
        }
        return rMap;
    }

    public static Map<Integer,List<Integer>> getGangListById(List<Integer> ids){
        Map<Integer,List<Integer>> map=new HashMap<>();
        for (Integer id:ids) {
            Integer val = CxMj.getMajang(id).getVal();
            List<Integer> list = map.get(val);
            if(list==null){
                list=new ArrayList<>();
                map.put(val,list);
            }
            list.add(id);
        }

        Map<Integer,List<Integer>> rMap=new HashMap<>();
        for (Map.Entry<Integer,List<Integer>> entry:map.entrySet()){
            if(entry.getValue().size()>=4)
                rMap.put(entry.getKey(),entry.getValue());
        }
        return rMap;
    }

    public static List<Integer> dropVal(List<CxMj> mjs,int val){
        List<Integer> list=new ArrayList<>();
        for (CxMj mj:mjs) {
            if(mj.getVal()!=val)
                list.add(mj.getId());
        }
        return list;
    }

    public static List<Integer> dropValById(List<Integer> ids,int val){
        int dropNum=0;
        List<Integer> list=new ArrayList<>();
        for (Integer id:ids) {
            if(CxMj.getMajang(id).getVal()!=val||dropNum>=4){
                list.add(id);
            }else {
                dropNum++;
            }
        }
        return list;
    }

    /**
     * 移除牌型ids中 val的牌
     * @param ids
     * @param val
     * @param num 移除张数
     * @return
     */
    public static List<Integer> dropValById(List<Integer> ids,int val,int num){
        List<Integer> list=new ArrayList<>();
        int count =0;
        for (Integer id:ids) {
            if(count>num){
                list.add(id);
            }
            if(CxMj.getMajang(id).getVal()==val && count<num){
                count++;
                continue;
            }else{
                list.add(id);
            }
        }
        return  list;
    }

//    public static void main(String[] args) {
//       List<Integer> as = Arrays.asList(21, 22, 23, 1, 28, 55, 14, 41, 68, 67,13, 40, 94, 1004);
//       System.out.println("==>");
//       System.err.println(CxMjHelper.toMajiang(as));
//        System.err.println(CxMjHelper.toMajiang(dropValById(as,24,4)));
//    }
}
