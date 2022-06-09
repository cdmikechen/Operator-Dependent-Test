package com.pesco.operator.common.utils;

import java.util.*;

/**
 * @Title collection相关工具类
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.utils
 * @Description
 * @Date 2022/6/9 11:32 上午
 * @Version V1.0
 */
public class CollectionUtils {

    /**
     * 处理空列表的情况
     */
    public static <T> List<T> filterEmptyCollections(List<T> collection) {
        if (collection == null || collection.isEmpty()) return Collections.emptyList();
        return collection;
    }

    /**
     * Returns {@code true} iff the given {@link Collection}s contain
     * exactly the same elements with exactly the same cardinalities.
     * <p>
     * That is, iff the cardinality of <i>e</i> in <i>a</i> is
     * equal to the cardinality of <i>e</i> in <i>b</i>,
     * for each element <i>e</i> in <i>a</i> or <i>b</i>.
     *
     * @param a the first collection, must not be null  两个参数都不能为空
     * @param b the second collection, must not be null
     * @return <code>true</code> iff the collections contain the same elements with the same cardinalities.
     * 注意这句它说明了该方法的实现原理：集合包含相同的元素且元素的基数相同
     */
    public static boolean isEqualCollection(Collection a, Collection b) {
        if (a.size() != b.size())
            return false;
        Map mapa = getCardinalityMap(a);
        Map mapb = getCardinalityMap(b);
        if (mapa.size() != mapb.size())
            return false;
        for (Iterator it = mapa.keySet().iterator(); it.hasNext(); ) {
            Object obj = it.next();
            if (getFreq(obj, mapa) != getFreq(obj, mapb))
                return false;
        }

        return true;
    }

    private static Integer INTEGER_ONE = new Integer(1);

    public static Map getCardinalityMap(Collection coll) {
        Map count = new HashMap();
        for (Iterator it = coll.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            Integer c = (Integer) count.get(obj);
            if (c == null)
                count.put(obj, INTEGER_ONE);
            else
                count.put(obj, new Integer(c.intValue() + 1));
        }

        return count;
    }

    private static final int getFreq(Object obj, Map freqMap) {
        Integer count = (Integer) freqMap.get(obj);
        if (count != null)
            return count.intValue();
        else
            return 0;
    }

    /**
     * 过滤空的map信息
     */
    public static <K, V> Map<K, V> filterEmptyMap(Map<K, V> map) {
        if (map == null) return Collections.emptyMap();
        return map;
    }

    /**
     * 两个map是否相同值
     */
    public static <K, V> boolean isEqualMap(Map<K, V> map1, Map<K, V> map2) {
        //同一对象相等
        if (map1 == map2)
            return true;
        //元素数量不同不相等
        if (map1.size() != map2.size())
            return false;
        //遍历
        try {
            for (Map.Entry<K, V> e : map2.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                //空值特殊处理
                if (value == null) {
                    if (!(map1.get(key) == null && map1.containsKey(key)))
                        return false;
                } else {
                    //调用元素的equals比较
                    if (!value.equals(map1.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

}
