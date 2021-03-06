package com.sy599.game.qipai.cqxzmj.tool;

import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.qipai.cqxzmj.constant.CqxzMj;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.StringUtil;

public class MjHelper {

    /**
     * 麻将val的个数
     *
     * @param majiangs
     * @param majiangVal
     * @return
     */
    public static int getMajiangCount(List<CqxzMj> majiangs, int majiangVal) {
        int count = 0;
        for (CqxzMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal) {
                count++;
            }
        }
        return count;
    }

    /**
     * 麻将val的List
     *
     * @param majiangs
     * @param majiangVal
     * @return
     */
    public static List<CqxzMj> getMajiangList(List<CqxzMj> majiangs, int majiangVal) {
        List<CqxzMj> list = new ArrayList<>();
        for (CqxzMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal) {
                list.add(majiang);
            }
        }
        return list;
    }

    public static List<CqxzMj> getGangMajiangList(List<CqxzMj> majiangs, int majiangVal) {
        int num=0;
        List<CqxzMj> list = new ArrayList<>();
        for (CqxzMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal&&num<4) {
                list.add(majiang);
                num++;
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
    public static List<Integer> toMajiangIds(List<CqxzMj> majiangs) {
        List<Integer> majiangIds = new ArrayList<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (CqxzMj majiang : majiangs) {
            majiangIds.add(majiang.getId());
        }
        return majiangIds;
    }

    /**
     * 麻将id转化为majiangIds
     *
     * @param ids
     * @return
     */
    public static List<Integer> toMajiangValsById(List<Integer> ids) {
        List<Integer> majiangIds = new ArrayList<>();
        if (ids == null) {
            return majiangIds;
        }
        for (Integer id : ids) {
            majiangIds.add(CqxzMj.getMajang(id).getVal());
        }
        return majiangIds;
    }

    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static String toMajiangStrs(List<CqxzMj> majiangs) {
        StringBuffer sb = new StringBuffer();
        if (majiangs == null) {
            return sb.toString();
        }
        for (CqxzMj majiang : majiangs) {
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
    public static List<Integer> toMajiangVals(List<CqxzMj> majiangs) {
        List<Integer> majiangIds = new ArrayList<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (CqxzMj majiang : majiangs) {
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
    public static Map<Integer, Integer> toMajiangValMap(List<CqxzMj> majiangs) {
        Map<Integer, Integer> majiangIds = new HashMap<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (CqxzMj majiang : majiangs) {
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
    public static List<CqxzMj> toMajiang(List<Integer> majiangIds) {
        if (majiangIds == null) {
            return new ArrayList<>();
        }
        List<CqxzMj> majiangs = new ArrayList<>();
        for (int majiangId : majiangIds) {
            if (majiangId == 0) {
                continue;
            }
            majiangs.add(CqxzMj.getMajang(majiangId));
        }
        return majiangs;
    }




    public static List<CqxzMj> find(List<Integer> copy, List<Integer> valList) {
        List<CqxzMj> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    CqxzMj majiang = CqxzMj.getMajang(card);
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

    public static List<Integer> find(List<CqxzMj> copy, int  val) {
        List<Integer> pai = new ArrayList<>();
        Iterator<CqxzMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            CqxzMj majiang = iterator.next();
            if (majiang.getVal() == val) {
                pai.add(majiang.getId());
                break;
            }
        }
        return pai;
    }


    public static Map<Integer,List<Integer>> getGangList(List<CqxzMj> mjs){
        Map<Integer,List<Integer>> map=new HashMap<>();
        for (CqxzMj mj:mjs) {
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
            Integer val = CqxzMj.getMajang(id).getVal();
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



    public static List<Integer> dropValById(List<Integer> ids,int val){
        int dropNum=0;
        List<Integer> list=new ArrayList<>();
        for (Integer id:ids) {
            if(CqxzMj.getMajang(id).getVal()!=val||dropNum>=4){
                list.add(id);
            }else {
                dropNum++;
            }
        }
        return list;
    }

}
