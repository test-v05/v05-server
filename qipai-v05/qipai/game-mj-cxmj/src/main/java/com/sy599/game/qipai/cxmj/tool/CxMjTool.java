package com.sy599.game.qipai.cxmj.tool;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.cxmj.constant.CxMj;
import com.sy599.game.qipai.cxmj.rule.CxMjIndex;
import com.sy599.game.qipai.cxmj.rule.CxMjIndexArr;
import com.sy599.game.qipai.cxmj.tool.ting.TingTool;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.TingResouce;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author lc
 */
public class CxMjTool {


    public static synchronized List<List<CxMj>> fapai(List<Integer> copy, List<List<Integer>> t) {
        List<List<CxMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<CxMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                list.add(CxMjHelper.find(copy, zp));
                testcount += zp.size();
            }

            if (list.size() == 4) {
                list.add(CxMjHelper.toMajiang(copy));
                System.out.println(JacksonUtil.writeValueAsString(list));
                return list;
            } else if (list.size() == 5) {
                return list;
            }
        }

        List<Integer> copy2 = new ArrayList<>(copy);
        int fapaiCount = 13 * 4 + 1 - testcount;
        if (pai.size() >= 14) {
            list.add(pai);
            pai = new ArrayList<>();
        }

        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }

        for (int i = 0; i < fapaiCount; i++) {
            // 发牌张数=13*4+1 正好第一个发牌的人14张其他人13张
            CxMj majiang = CxMj.getMajang(copy.get(i));
            copy2.remove((Object) copy.get(i));
            if (test) {
                if (i < j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            } else {
                if (i <= j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            }

        }
        list.add(pai);
        List<CxMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(CxMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    public static synchronized List<List<CxMj>> fapai(List<Integer> copy, int playerCount) {
        List<List<CxMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<CxMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(CxMj.getMajang(id));
        }
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                list.add(new ArrayList<>(allMjs.subList(0, 14)));
            } else {
                list.add(new ArrayList<>(allMjs.subList(14 + (i - 1) * 13, 14 + (i - 1) * 13 + 13)));
            }
            if (i == playerCount - 1) {
                list.add(new ArrayList<>(allMjs.subList(14 + (i) * 13, allMjs.size())));
            }

        }
        return list;
    }

    public static synchronized List<List<CxMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        if(t.size()<playerCount)
            return null;
        List<List<CxMj>> list = new ArrayList<>();
        for (List<Integer> zp : t) {
            list.add(find(copy, zp));
        }
        Collections.shuffle(copy);
        if(true){
            //不需要补张

        }else {
            //补张
            for (int i = 0; i < playerCount; i++) {
                List<CxMj> ahmjs = list.get(i);
                if(i==0){
                    for (int j = ahmjs.size(); j < 14; j++) {
                        ahmjs.add(CxMj.getMajang(copy.get(j)));
                        copy.remove(j);
                    }
                }else {
                    for (int j = ahmjs.size(); j < 13; j++) {
                        ahmjs.add(CxMj.getMajang(copy.get(j)));
                        copy.remove(j);
                    }
                }
            }
        }

        if(list.size()>playerCount){
        }else if(list.size()==playerCount){
            List<CxMj> l=new ArrayList<>();
            for (int i = 0; i < copy.size(); i++) {
                l.add(CxMj.getMajang(copy.get(i)));
            }
            list.add(l);
        }

        return list;
    }


    public static boolean isPingHu(List<CxMj> majiangIds) {
        return isPingHu(majiangIds, true);

    }

    public static boolean isPingHu(List<CxMj> majiangIds, boolean needJiang258) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        if (majiangIds.size() % 3 != 2) {
            System.out.println("%3！=2");
            return false;

        }

        // 先去掉红中
        List<CxMj> copy = new ArrayList<>(majiangIds);
        List<CxMj> hongzhongList = dropHongzhong(copy);

        CxMjIndexArr card_index = new CxMjIndexArr();
        CxMjQipaiTool.getMax(card_index, copy);
        // 拆将
        if (chaijiang(card_index, copy, hongzhongList.size(), needJiang258)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 红中麻将没有7小对，所以不用红中补
     *
     * @param majiangIds
     * @param card_index
     */
    public static boolean check7duizi(List<CxMj> majiangIds, CxMjIndexArr card_index, int hongzhongNum) {
        if (majiangIds.size() == 14) {
            // 7小对
            int duizi = card_index.getDuiziNum();
            if (duizi == 7) {
                return true;
            }

        } else if (majiangIds.size() + hongzhongNum == 14) {
            if (hongzhongNum == 0) {
                return false;
            }

            CxMjIndex index0 = card_index.getMajiangIndex(0);
            CxMjIndex index2 = card_index.getMajiangIndex(2);
            int lackNum = index0 != null ? index0.getLength() : 0;
            lackNum += index2 != null ? index2.getLength() : 0;

            if (lackNum <= hongzhongNum) {
                return true;
            }

            if (lackNum == 0) {
                lackNum = 14 - majiangIds.size();
                if (lackNum == hongzhongNum) {
                    return true;
                }
            }

        }
        return false;
    }

    // 拆将
    public static boolean chaijiang(CxMjIndexArr card_index, List<CxMj> hasPais, int hongzhongnum, boolean needJiang258) {
        Map<Integer, List<CxMj>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<CxMj>> valEntry : jiangMap.entrySet()) {
            List<CxMj> copy = new ArrayList<>(hasPais);
            CxMjHuLack lack = new CxMjHuLack(hongzhongnum);
            List<CxMj> list = valEntry.getValue();
            int i = 0;
            for (CxMj majiang : list) {
                i++;
                copy.remove(majiang);
                if (i >= 2) {
                    break;
                }
            }
            lack.setHasJiang(true);
            boolean hu = chaipai(lack, copy, needJiang258);
            if (hu) {
                return hu;
            }
        }

        if (hongzhongnum > 0) {
            // 只剩下红中
            if (hasPais.isEmpty()) {
                return true;
            }
            // 没有将
            for (CxMj majiang : hasPais) {
                List<CxMj> copy = new ArrayList<>(hasPais);
                CxMjHuLack lack = new CxMjHuLack(hongzhongnum);
                boolean isJiang = false;
                if (!needJiang258) {
                    // 不需要将
                    isJiang = true;

                } else {
                    // 需要258做将
                    if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                        isJiang = true;
                    }

                }
                if (isJiang) {
                    lack.setHasJiang(true);
                    lack.changeHongzhong(-1);
                    lack.addLack(majiang.getVal());
                    copy.remove(majiang);
                }

                boolean hu = chaipai(lack, copy, needJiang258);
                if (lack.isHasJiang() && hu) {
                    return true;
                }
                if (!lack.isHasJiang() && hu) {
                    if (lack.getHongzhongNum() == 2) {
                        // 红中做将
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // 拆牌
    public static boolean chaipai(CxMjHuLack lack, List<CxMj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<CxMj> hasPais) {
        Collections.sort(hasPais, new Comparator<CxMj>() {

            @Override
            public int compare(CxMj o1, CxMj o2) {
                if (o1.getPai() < o2.getPai()) {
                    return -1;
                }
                if (o1.getPai() > o2.getPai()) {
                    return 1;
                }
                return 0;
            }

        });
    }

    /**
     * 拆顺
     *
     * @param hasPais
     * @return
     */
    public static boolean chaishun(CxMjHuLack lack, List<CxMj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        CxMj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<CxMj> minList = CxMjQipaiTool.getVal(hasPais, minVal);
        if (minList.size() >= 3) {
            // 先拆坎子
            removeAllPai(hasPais, minList.subList(0, 3));
            //hasPais.removeAll(minList.subList(0, 3));
            return chaipai(lack, hasPais, needJiang258);
        }

        // 做顺子
        int pai1 = minVal;
        int pai2 = 0;
        int pai3 = 0;
        if (pai1 % 10 == 9) {
            pai1 = pai1 - 2;

        } else if (pai1 % 10 == 8) {
            pai1 = pai1 - 1;
        }
        pai2 = pai1 + 1;
        pai3 = pai2 + 1;

        List<Integer> lackList = new ArrayList<>();
        List<CxMj> num1 = CxMjQipaiTool.getVal(hasPais, pai1);
        List<CxMj> num2 = CxMjQipaiTool.getVal(hasPais, pai2);
        List<CxMj> num3 = CxMjQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<CxMj> hasMajiangList = new ArrayList<>();
        if (!num1.isEmpty()) {
            hasMajiangList.add(num1.get(0));
        }
        if (!num2.isEmpty()) {
            hasMajiangList.add(num2.get(0));
        }
        if (!num3.isEmpty()) {
            hasMajiangList.add(num3.get(0));
        }

        // 一句话缺少的麻将
        if (num1.isEmpty()) {
            lackList.add(pai1);
        }
        if (num2.isEmpty()) {
            lackList.add(pai2);
        }
        if (num3.isEmpty()) {
            lackList.add(pai3);
        }

        int lackNum = lackList.size();
        if (lackNum > 0) {
            if (lack.getHongzhongNum() <= 0) {
                return false;
            }

            // 做成一句话缺少2张以上的，没有将优先做将
            if (lackNum >= 2) {
                // 补坎子
                List<CxMj> count = CxMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() >= 3) {
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);

                } else if (count.size() == 2) {
                    if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258)) {
                        // 没有将做将
                        lack.setHasJiang(true);
                        removeAllPai(hasPais, count);
                        //hasPais.removeAll(count);
                        return chaipai(lack, hasPais, needJiang258);
                    }

                    // 拿一张红中补坎子
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }

                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    lack.addLack(count.get(0).getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }
            } else if (lackNum == 1) {
                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(minMajiang, needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    hasPais.remove(minMajiang);
                    lack.addLack(minMajiang.getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }

                List<CxMj> count = CxMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }
            }

            // 如果有红中则补上
            if (lack.getHongzhongNum() >= lackNum) {
                lack.changeHongzhong(-lackNum);
                removeAllPai(hasPais, hasMajiangList);
                //hasPais.removeAll(hasMajiangList);
                lack.addAllLack(lackList);

            } else {
                return false;
            }
        } else {
            // 可以一句话
            if (lack.getHongzhongNum() > 0) {
                List<CxMj> count1 = CxMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<CxMj> count2 = CxMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<CxMj> count3 = CxMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<CxMj> copy = new ArrayList<>(hasPais);
                    removeAllPai(copy, count1);
                    //copy.removeAll(count1);
                    CxMjHuLack copyLack = lack.copy();
                    copyLack.changeHongzhong(-1);

                    copyLack.addLack(hasMajiangList.get(0).getVal());
                    if (chaipai(copyLack, copy, needJiang258)) {
                        return true;
                    }
                }
            }
            removeAllPai(hasPais, hasMajiangList);
            //hasPais.removeAll(hasMajiangList);
        }
        return chaipai(lack, hasPais, needJiang258);
    }

    public static void removeAllPai(List<CxMj> hasPais, List<CxMj> remPai) {
        for (CxMj majiang : remPai) {
            hasPais.remove(majiang);
        }
    }

    public static boolean isCanAsJiang(CxMj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    public static List<CxMj> checkChi(List<CxMj> majiangs, CxMj dismajiang) {
        return checkChi(majiangs, dismajiang, null);
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<CxMj> checkChi(List<CxMj> majiangs, CxMj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = CxMjHelper.toMajiangVals(majiangs);
        if (wangValList == null || !checkWang(chi1, wangValList)) {
            if (majiangIds.containsAll(chi1)) {
                return findMajiangByVals(majiangs, chi1);
            }
        }
        if (wangValList == null || !checkWang(chi2, wangValList)) {
            if (majiangIds.containsAll(chi2)) {
                return findMajiangByVals(majiangs, chi2);
            }
        }
        if (wangValList == null || !checkWang(chi3, wangValList)) {
            if (majiangIds.containsAll(chi3)) {
                return findMajiangByVals(majiangs, chi3);
            }
        }
        return new ArrayList<CxMj>();
    }

    public static List<CxMj> findMajiangByVals(List<CxMj> majiangs, List<Integer> vals) {
        List<CxMj> result = new ArrayList<>();
        for (int val : vals) {
            for (CxMj majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 去掉红中
     *
     * @param copy
     * @return
     */
    public static List<CxMj> dropHongzhong(List<CxMj> copy) {
        List<CxMj> hongzhong = new ArrayList<>();
        Iterator<CxMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            CxMj majiang = iterator.next();
            if (majiang.getVal() > 200) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
        if (majiangs instanceof List) {
            List list = (List) majiangs;
            for (Object majiang : list) {
                int val = 0;
                if (majiang instanceof CxMj) {
                    val = ((CxMj) majiang).getVal();
                } else {
                    val = (int) majiang;
                }
                if (wangValList.contains(val)) {
                    return true;
                }
            }
        }

        return false;

    }



    private static List<CxMj> find(List<Integer> copy, List<Integer> valList) {
        List<CxMj> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    CxMj mj = CxMj.getMajang(card);
                    if (mj.getVal() == zpId) {
                        pai.add(mj);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return pai;
    }


    /**
     *
     * @param ver
     * @param handPais
     * @param pengMap key_val val=count
     * @param addId
     * @return
     */
    public static Map<Integer,List<Integer>> checkGang(int ver,List<Integer> handPais,Map<Integer, Integer> pengMap,int addId) {
        Map<Integer,List<Integer>> result=new HashMap<>();
        //最多连杠两次
        if(ver<2){
            //获取所有组成4张的可能
            Map<Integer, List<Integer>> gangList = CxMjHelper.getGangListById(handPais);
            for (Integer gangV:gangList.keySet()) {
                List<Integer> gangIds = gangList.get(gangV);
                List<Integer> copy=new ArrayList<>(handPais);
                //移除杠的牌，
                List<Integer> ids=CxMjHelper.dropValById(copy, gangV);
                checkG(ver,ids,pengMap,result,gangIds.get(0),1004,addId);
                checkG(ver,ids,pengMap,result,gangIds.get(0),1005,addId);
            }

            for (Integer id : handPais) {
                if (pengMap.containsKey(CxMj.getMajang(id).getVal())) {
                    List<Integer> copy=new ArrayList<>(handPais);
                    copy.remove(id);
                    checkG(ver,copy,pengMap,result,id,1004,addId);
                    checkG(ver,copy,pengMap,result,id,1005,addId);
                }
            }
        }

        if(TingTool.isHu(handPais))
            result.put(0,new ArrayList<>());
        return result;
    }
//    hand [1万, 1万, 1万, 1万, 8万, 8万, 8万, 5条, 5条, 4筒, 4筒, 4筒, 5筒, 5筒]
//    hand ar [19, 46, 73, 100, 26, 53, 80, 5, 32, 13, 40, 67, 14, 41]
//    peng []
//    peng ar []
//    gangMj null
    public static void main(String[] args) {
        TingResouce.init();
//        List<Integer> hand =Arrays.asList(14, 41, 68, 15, 16, 18, 45, 93);
//        List<Integer> peng =Arrays.asList(12, 39, 66, 13, 40, 67);
//        checkShuangGang(1,hand,peng,CxMj.getMajang(93),true);

//        List<Integer> hand =Arrays.asList(25, 52, 14, 41, 68, 15, 16, 5, 32, 59, 95);
//        List<Integer> peng =Arrays.asList(13, 40, 67);
//        checkShuangGang(1,hand,peng,CxMj.getMajang(95),true);

        //
//        hand [7万, 7万, 5筒, 5筒, 5筒, 6筒, 7筒, 5条, 5条, 5条, 5筒]
//        hand ar [25, 52, 14, 41, 68, 15, 16, 5, 32, 59, 95]
//        peng [4筒, 4筒, 4筒]
//        peng ar [13, 40, 67]
//        gangMj 5筒

//       hand [5筒, 5筒, 5筒, 4筒, 4筒, 4筒, 7筒, 5筒]
//hand ar [14, 41, 68, 13, 40, 67, 97, 95]
//peng [7筒, 7筒, 7筒, 6筒, 6筒, 6筒]
//peng ar [16, 43, 70, 15, 42, 69]
//gangMj 5筒
//        List<Integer> hand =Arrays.asList(14, 41, 68, 13, 40, 67, 97, 95);
//        List<Integer> peng =Arrays.asList(16, 43, 70, 15, 42, 69);
//       System.err.println( checkShuangGang(1,hand,peng,CxMj.getMajang(95),true).toString());;

//        hand [2条, 2条, 3筒, 3筒, 3筒, 4筒, 4筒, 4筒, 4筒, 3万, 3筒]
//        hand ar [2, 29, 12, 39, 66, 13, 40, 67, 94, 75, 93]
//        peng [3万, 3万, 3万]
//        peng ar [21, 48, 102]
//        gangMj 3筒
//        List<Integer> hand =Arrays.asList(2, 29, 12, 39, 66, 13, 40, 67, 94, 75, 93);
//        List<Integer> peng =Arrays.asList(21, 48, 102);
//        System.err.println( checkShuangGang(1,hand,peng,CxMj.getMajang(93),true).toString());;

//        List<Integer> list = Arrays.asList(11,28,55,82,12,29,56,83,13,30,57,84,38,65);
//        System.err.println( checkShuangGang(1,list,new ArrayList<>(),CxMj.getMajang(65),true).toString());
        //
        List<Integer> hand =Arrays.asList(101,97, 5,32,59,6,33);
        List<Integer> peng =Arrays.asList(21, 48, 102, 16,43,70);//86,
        System.err.println( checkShuangGang(1,hand,peng,CxMj.getMajang(86),false).toString());


    }

    /**
     *
     * @param ver 无效参数
     * @param handPais 手牌
     * @param peng 碰牌
     * @param gangMj 摸的牌或者别人打的牌
     * @param ismo 是否已摸牌
     * @return 能否双杠直接胡 map
     */
    public static   HashMap<Object,Object> checkShuangGang(int ver, List<Integer> handPais, List<Integer> peng, CxMj gangMj, boolean ismo) {
        HashMap<Object,Object> result = new HashMap<>();
        result.put("hu", false);
        if (null == gangMj && handPais.size()!=14) {
            return result;
        }

        try {
            System.out.println("hand " + CxMjHelper.toMajiang(handPais));
            System.out.println("hand ar " + handPais);
            System.out.println("peng " + CxMjHelper.toMajiang(peng));
            System.out.println("peng ar " + peng);
            System.out.println("gangMj " + gangMj );

            //自摸 hand 58 7 34 14 90 peng 21 48 75 9 36 63 4 31 85

            //fangpao
            //        hand [3万, 3万, 3万, 2筒, 2筒, 2筒, 2筒, 5筒, 1万, 1万, 1万, 4筒, 4筒]
            //        hand ar [21, 48, 75, 11, 38, 65, 92, 14, 19, 46, 73, 13, 40]
            //最多连杠两次
            if (ver < 2) {
                //获取所有组成4张的可能
                List<Integer> ary = new ArrayList<>(handPais);
                ary.addAll(new ArrayList<>(peng));
                if (!ismo&& gangMj!=null) {
                    ary.add(gangMj.getId());
                }
                if(handPais.size()==14){
                    //起手
                   ary = new ArrayList<>(handPais);
                }

                Map<Integer, List<Integer>> gangList = CxMjHelper.getGangListById(ary);
                Map<Integer, List<Integer>> gangSortList = new HashMap<>();
                //
                int g1val = 0;
                int g2Val = 0;
                List<Integer> g1 =new ArrayList<>();
                List<Integer> g2 =new ArrayList<>();
                boolean falg = false;
                for (Entry<Integer,List<Integer>> e:gangList.entrySet()  ) {
                     if(gangMj!=null && e.getKey()==gangMj.getVal()){
                         g1val=e.getKey();
                         g1.addAll(e.getValue());
                         falg = true;
                     }else{
                         g2Val=e.getKey();
                         g2.addAll(e.getValue());
                     }
                }
                if(falg && gangList.size()==2){
                    gangSortList.putIfAbsent(g1val,g1);
                    gangSortList.putIfAbsent(g2Val,g2);
                    gangList = gangSortList;
                }
                int tong4num = CxMjHelper.getMajiangCount(CxMjHelper.toMajiang(ary),24);
                int tong5num = CxMjHelper.getMajiangCount(CxMjHelper.toMajiang(ary),25);
                List<Integer> ids = new ArrayList<>();
                if (gangList.size() == 2) {
                    // 以下情况 手牌不包含3个4筒或者5筒 摸45筒共计2个双杠胡
                    List<Integer> copy = new ArrayList<>(handPais);
                    List<Integer> copy2 = new ArrayList<>(handPais);
                    List<Integer> gang1 = new ArrayList<>();
                    List<Integer> gang2 = new ArrayList<>();
                    int i=0;
                    boolean qishouShuangGangHu = false;
                    List<Integer> gangList3 = new ArrayList<>();
                    for (Integer gangV : gangList.keySet()) {
                        gangList3.add(gangV);
                        List<Integer> gangIds = gangList.get(gangV);
                        if(gangMj==null){
                            //起手双杠胡特俗处理
                            if(i==0){
                                gang1.addAll(gangIds);
                                i++;
                            }else{
                                gang2.addAll(gangIds);
                                qishouShuangGangHu = true;
                            }
                            //移除杠的牌，
                            ids = CxMjHelper.dropValById(copy, gangV);
                            copy = ids;
                            continue;
                        }
                        if (i==0) {
                            gang1.addAll(gangIds);
                            i++;
                        } else {
                            gang2.addAll(gangIds);
                        }

                        //移除杠的牌，
                        ids = CxMjHelper.dropValById(copy, gangV);
                        copy = ids;
                        System.out.println("ids " + CxMjHelper.toMajiang(ids));
                    }
                    List<Integer> handPais2 = new ArrayList<>(ids);
//                    handPais2.add(1005);
//                    handPais2.add(1005);
////                    System.out.println("==>");
////                    System.out.println(CxMjHelper.toMajiang(handPais2));
//                    if (TingTool.isHu(handPais2)) {
//                        result.put("hu", true);
//                        result.put("gang1", CxMjHelper.toMajiang(gang1));
//                        result.put("mo1", 1005);
//                        result.put("gang2", CxMjHelper.toMajiang(gang2));
//                        result.put("mo2", 1005);
//                        result.put("hand", CxMjHelper.toMajiang(handPais2));
//                        //result.put("qishouShuangGangHu",  qishouShuangGangHu);
//                        return result;
//                    }

                    handPais2 = new ArrayList<>(ids);
                    handPais2.add(1004);
                    handPais2.add(1005);
                    if (TingTool.isHu(handPais2)) {
                        result.put("hu", true);
                        result.put("gang1", CxMjHelper.toMajiang(gang1));
                        result.put("mo1", 1004);
                        result.put("gang2", CxMjHelper.toMajiang(gang2));
                        result.put("mo2", 1005);
                        result.put("hand", CxMjHelper.toMajiang(handPais2));
                        //result.put("qishouShuangGangHu",  qishouShuangGangHu);
                        return result;
                    }
                    //peng [7筒, 7筒, 7筒, 6筒, 6筒, 6筒]
                    //hand [5筒, 5筒, 5筒, 4筒, 4筒, 4筒, 7筒, 5筒]
                    //gangMj 5筒
                    //mo=true
                    // 下面这个循环 处理 。摸5筒 杠7 再摸4筒 杠4筒 再摸5筒 胡。
                    System.err.println("gang1 "+ g1val+"  gang2 "+  g2Val);
                    for (int val:gangList3 ) {
                        List<Integer> copy3 = new ArrayList<>(copy2);
                        copy3 = CxMjHelper.dropValById(copy3, val);
                        copy3.add(1004);
                        List<CxMj> _g =CxMjHelper.canGang(CxMjHelper.toMajiang(copy3),24);
                        if(_g.size()==4){
                             copy3 = CxMjHelper.dropValById(copy3, 24);
                             copy3.add(1005);
                            if (TingTool.isHu(copy3)) {
                                System.err.println(CxMjHelper.toMajiang(copy3));
                                System.err.println("hu");
                                result.put("hu", true);
                                result.put("gang1", CxMjHelper.toMajiang(gangList.get(val)));
                                result.put("mo1", 1004);
                                result.put("gang2", _g);
                                result.put("mo2", 1005);
                                result.put("hand", CxMjHelper.toMajiang(copy3));
                                return result;
                                //gang1 =val
                                //gang2 ==24
                            }
                        }

                        List<Integer> copy4 = new ArrayList<>(copy2);
                        copy4 = CxMjHelper.dropValById(copy4, val);
                        copy4.add(1005);
                        List<CxMj> _g2=CxMjHelper.canGang(CxMjHelper.toMajiang(copy3),25);
                        if(_g2.size()==4){
                            copy4 = CxMjHelper.dropValById(copy4, 25);
                            copy4.add(1004);
                            if (TingTool.isHu(copy4)) {
                                System.err.println(CxMjHelper.toMajiang(copy4));
                                System.err.println("hu");
                                result.put("hu", true);
                                result.put("gang1",  CxMjHelper.toMajiang(gangList.get(val)));
                                result.put("mo1", 1005);
                                result.put("gang2", _g2);
                                result.put("mo2", 1004);
                                result.put("hand", CxMjHelper.toMajiang(copy4));
                                return result;
                                //gang1 =val
                                //gang2 ==25
                            }
                        }
                    }

                }
                else if((gangList.size()==1 && tong4num==3 && (tong5num<3 ||tong5num==4)) ){
                    ary.add(1004);
                    ary.add(1005);
                    List<Integer> cop = new ArrayList<>(ary);
                    Map<Integer, List<Integer>> gang2List = CxMjHelper.getGangListById(ary);
                    List<Integer> copy = new ArrayList<>(cop);
                    List<Integer> gang1 = new ArrayList<>();
                    List<Integer> gang2 = new ArrayList<>();//后杠的4或5筒
                    int _g1Vals=0;//优先杠的
                    for (Integer gangV : gangList.keySet()) {
                        List<Integer> gangIds = gang2List.get(gangV);
                        gang1.addAll(gangIds);
                        _g1Vals =gangV;
                    }

                    for (Integer gangV : gang2List.keySet()) {
                        List<Integer> gangIds = gang2List.get(gangV);
                        if (gangV!=_g1Vals) {
                            gang2.addAll(gangIds);
                        }
                        //移除杠的牌，
                        ids = CxMjHelper.dropValById(copy, gangV);
                        copy = ids;
                          System.out.println("ids " + CxMjHelper.toMajiang(ids));

                    }
                    if (TingTool.isHu(copy)){
                        result.put("hu", true);
                        result.put("gang1", CxMjHelper.toMajiang(gang1));
                        result.put("mo1", 1004);
                        result.put("gang2", CxMjHelper.toMajiang(gang2));
                        result.put("mo2", 1005);
                        result.put("hand", CxMjHelper.toMajiang(copy));
                    }
                }
                else if(gangList.size()==1 && tong5num==3 && (tong4num<3||tong4num==4)){
                    ary.add(1005);
                    ary.add(1004);
                    List<Integer> cop = new ArrayList<>(ary);
                    Map<Integer, List<Integer>> gang2List = CxMjHelper.getGangListById(ary);
                    List<Integer> copy = new ArrayList<>(cop);
                    List<Integer> gang1 = new ArrayList<>();//
                    List<Integer> gang2 = new ArrayList<>();//后杠的4或5筒
                    int _g1Vals=0;//优先杠的排
                    for (Integer gangV : gangList.keySet()) {
                        List<Integer> gangIds = gang2List.get(gangV);
                        gang1.addAll(gangIds);
                        _g1Vals =gangV;
                    }

                    for (Integer gangV : gang2List.keySet()) {
                        List<Integer> gangIds = gang2List.get(gangV);
                        if (gangV!=_g1Vals) {
                            gang2.addAll(gangIds);
                        }
                        //移除杠的牌，
                        ids = CxMjHelper.dropValById(copy, gangV);
                        copy = ids;

                    }
                    if (TingTool.isHu(copy)){
                        result.put("hu", true);
                        result.put("gang1", CxMjHelper.toMajiang(gang1));
                        result.put("mo1", 1005);
                        result.put("gang2", CxMjHelper.toMajiang(gang2));
                        result.put("mo2", 1004);
                        result.put("hand", CxMjHelper.toMajiang(copy));
                    }
                }
                else if(gangList.size()==1 && tong4num==3 && tong5num==3) {
                    //shoupai 3万, 9条, 4条, 6万, 7筒, 8筒, 9筒, 1条, 1条, 7条, 2条, 8条, 1筒, 3万
                    // peng 3筒, 3筒, 3筒, 4筒, 4筒, 4筒
                    // gangmj =93 3筒
                    ary.add(1005);
                    ary.add(1004);
                    List<Integer> cop = new ArrayList<>(ary);
                    Map<Integer, List<Integer>> gang2List = CxMjHelper.getGangListById(ary);
                    if(gang2List.size()==3){
                        List<Integer> _gangVal = new ArrayList<>();
                        for (Integer gangV : gang2List.keySet()) {
                            _gangVal.add(gangV);
                        }
                        for (int v:_gangVal  ) {
                            if(gangMj!=null && v!=gangMj.getVal()) {
                                 //System.out.println("==>"+CxMjHelper.toMajiang(cop));
                                //System.out.println(v);
                                List<Integer> copy_hand = new ArrayList<>(cop);
                                copy_hand=CxMjHelper.dropValById(copy_hand,v);
                                copy_hand=CxMjHelper.dropValById(copy_hand,gangMj.getVal());
//
                                //System.out.println("==>"+CxMjHelper.toMajiang(copy_hand));
                                if( TingTool.isHu(copy_hand)){
                                    result.put("hu", true);
                                    result.put("gang1", CxMjHelper.toMajiang(gang2List.get(gangMj.getVal())));
                                    result.put("mo1", 1004);
                                    result.put("gang2", CxMjHelper.toMajiang(gang2List.get(v)));
                                    result.put("mo2", 1005);
                                    result.put("hand", CxMjHelper.toMajiang(copy_hand));
                                    return result;
                                }
                            }else{
                                //
                                List<Integer> _gangVal2 = new ArrayList<>(_gangVal);
                                //起手情况下 gangMj==null
                                _gangVal2.remove((Object)v);
                                for (int v2:_gangVal2 ) {
                               // System.out.println("==>"+CxMjHelper.toMajiang(cop));
//                                System.out.println(v);
                                    List<Integer> copy_hand = new ArrayList<>(cop);
                                    copy_hand=CxMjHelper.dropValById(copy_hand,v);//
                                    copy_hand=CxMjHelper.dropValById(copy_hand,v2);

                                   // System.out.println("==>"+CxMjHelper.toMajiang(copy_hand));
                                    if( TingTool.isHu(copy_hand)){
                                        result.put("hu", true);
                                        result.put("gang1", CxMjHelper.toMajiang(gang2List.get(v)));
                                        result.put("mo1", 1004);
                                        result.put("gang2", CxMjHelper.toMajiang(gang2List.get(v2)));
                                        result.put("mo2", 1005);
                                        result.put("hand", CxMjHelper.toMajiang(copy_hand));
                                        return result;
                                    }
                                }

                            }
                        }
                    }
                }
                else if(gangList.size()==3){
                    List<Integer> _gangVal = new ArrayList<>();
                    for (Integer gangV : gangList.keySet()) {
                        _gangVal.add(gangV);
                    }
                    //38 23 24 //93=23
                    for (int v:_gangVal  ) {
                        if(gangMj!=null && v!=gangMj.getVal()) {
                            //System.out.println("==>"+CxMjHelper.toMajiang(handPais));
                            //System.out.println(v);
                            List<Integer> copy_hand = new ArrayList<>(handPais);
                            copy_hand=CxMjHelper.dropValById(copy_hand,v);
                            copy_hand=CxMjHelper.dropValById(copy_hand,gangMj.getVal());

                            copy_hand.add(1004);
                            copy_hand.add(1005);
                            //System.out.println("==>"+CxMjHelper.toMajiang(copy_hand));
                           if( TingTool.isHu(copy_hand)){
                               result.put("hu", true);
                               result.put("gang1", CxMjHelper.toMajiang(gangList.get(gangMj.getVal())));
                               result.put("mo1", 1004);
                               result.put("gang2", CxMjHelper.toMajiang(gangList.get(v)));
                               result.put("mo2", 1005);
                               result.put("hand", CxMjHelper.toMajiang(copy_hand));
                               return result;
                           }

                        }
                        else{
                            //
                            List<Integer> _gangVal2 = new ArrayList<>(_gangVal);
                            //起手情况下 gangMj==null
                            _gangVal2.remove((Object)v);
                            for (int v2:_gangVal2 ) {
//                                System.out.println("==>"+CxMjHelper.toMajiang(handPais));
//                                System.out.println(v);
                                List<Integer> copy_hand = new ArrayList<>(handPais);
                                copy_hand=CxMjHelper.dropValById(copy_hand,v);//
                                copy_hand=CxMjHelper.dropValById(copy_hand,v2);
                                copy_hand.add(1004);
                                copy_hand.add(1005);
                                //System.out.println("==>"+CxMjHelper.toMajiang(copy_hand));
                                if( TingTool.isHu(copy_hand)){
                                    result.put("hu", true);
                                    result.put("gang1", CxMjHelper.toMajiang(gangList.get(v)));
                                    result.put("mo1", 1004);
                                    result.put("gang2", CxMjHelper.toMajiang(gangList.get(v2)));
                                    result.put("mo2", 1005);
                                    result.put("hand", CxMjHelper.toMajiang(copy_hand));
                                    return result;
                                }
                            }

                        }
                    }

                    //2021年5月28日 10:38:02 BUG修复
                    //牌型
//                    hand [2条, 2条, 3筒, 3筒, 3筒, 4筒, 4筒, 4筒, 4筒, 3万, 3筒]
//                    hand ar [2, 29, 12, 39, 66, 13, 40, 67, 94, 75, 93]
//                    peng [3万, 3万, 3万]
//                    peng ar [21, 48, 102]
//                    gangMj 3筒 ismo=true 摸3筒
                    // 01 12  02 穷举组合 List<Integer> _gangVal
                   result= reCheck3Gang(gangList,handPais,_gangVal.get(0),_gangVal.get(1));
                  if(result!=null && result.size()>2){
                      return  result;
                  }
                    result= reCheck3Gang(gangList,handPais,_gangVal.get(1),_gangVal.get(2));
                  if(result!=null &&result.size()>2){
                      return  result;
                  }
                    result= reCheck3Gang(gangList,handPais,_gangVal.get(0),_gangVal.get(2));
                  if(result!=null && result.size()>2){
                      return  result;
                  }
                }

            }
            if(result!=null && result.size()>3){
                //特殊牌型处理      List<Integer> hand =Arrays.asList(25, 52, 14, 41, 68, 15, 16, 5, 32, 59, 95);
                //        List<Integer> peng =Arrays.asList(13, 40, 67);
                //        checkShuangGang(1,hand,peng,CxMj.getMajang(95),true);
                // 杠的list 把全部同牌放进去了 做下处理
                List<CxMj>  rg1 = (List<CxMj>) result.get("gang1");
                List<CxMj> rg2 = (List<CxMj>) result.get("gang2");
                if(rg1.size()>=5){
                    rg1=rg1.subList(0,4);
                    result.put("gang1",rg1);
                } if(rg2.size()>=5){
                    rg2=rg2.subList(0,4);
                    result.put("gang2",rg2);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("hu",false);
            return result;
        }
    }

    public static HashMap<Object,Object> reCheck3Gang(   Map<Integer, List<Integer>> gangList,List<Integer> handPais,int gangVal1,int gangVal2){
        HashMap<Object,Object> result = new HashMap<>();
        List<Integer> copy_hand = new ArrayList<>(handPais);
        copy_hand=CxMjHelper.dropValById(copy_hand,gangVal1);
        copy_hand=CxMjHelper.dropValById(copy_hand,gangVal2);

        copy_hand.add(1004);
        copy_hand.add(1005);
         System.out.println("==>"+CxMjHelper.toMajiang(copy_hand));
        if( TingTool.isHu(copy_hand)){
            result.put("hu", true);
            result.put("gang1", CxMjHelper.toMajiang(gangList.get(gangVal1)));
            result.put("mo1", 1004);
            result.put("gang2", CxMjHelper.toMajiang(gangList.get(gangVal2)));
            result.put("mo2", 1005);
            result.put("hand", CxMjHelper.toMajiang(copy_hand));
            return result;
        }else{
            return null;
        }
    }

    public static void checkG(int ver,List<Integer> cards,Map<Integer, Integer> pengMap,Map<Integer,List<Integer>> result,int gangId,int checkBuId,int addId){
        if(addId!=checkBuId){
            List<Integer> copy=new ArrayList<>(cards);
            copy.add(checkBuId);
            Map<Integer, List<Integer>> r = checkGang(ver + 1, copy, pengMap, checkBuId);
            if(r.size()>0||TingTool.isHu(copy)){
                List<Integer> defL = result.getOrDefault(checkBuId, new ArrayList<>());
                defL.add(gangId);
                result.put(checkBuId,defL);
            }
        }
    }
}
