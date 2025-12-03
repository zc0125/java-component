package com.feng.module.util.number;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 基于线性同余的数字加密（伪随机，但极难破解）
 * 决定加密结果的信息需要用户自定义
 *
 * @author zc
 * Date 2025/12/3 18:54
 * Version 1.0
 */


public abstract class NumberLCGEncryptor {

    private final String[] keys;
    private final Map<String, Integer> keyMapIndex;
    private final int radix;
    private final int minLength;
    private final long prefix;
    private final int reallyLength;
    private final long maxNumber;


    NumberLCGEncryptor() {
        if (defineMinLength() <= 0 || definePrefix() < 0) {
            throw new IllegalArgumentException("Min-length and prefix must be greater than 0.");
        }
        if (defineKeys().length < 2) {
            throw new IllegalArgumentException("Radix must be greater than 2.");
        }
        this.keys = defineKeys();
        this.minLength = defineMinLength();
        this.prefix = definePrefix();
        this.maxNumber = Long.MAX_VALUE - this.prefix;
        this.radix = this.keys.length;
        this.reallyLength = this.keys[0].length();
        keyMapIndex = new HashMap<>();
        for (int i = 0; i < this.keys.length; i++) {
            if (keys[i].length() != this.reallyLength) {
                throw new IllegalArgumentException("key lengths don't match");
            }
            keyMapIndex.put(keys[i], i);
        }
    }

    /**
     * 定义混淆key(每个key的长度应是一致的)
     */
    abstract String[] defineKeys();

    /**
     * 定义生成组数(最终加密结果长度为：return * 单个key的长度)
     */
    abstract int defineMinLength();

    /**
     * 定义前缀值(前缀值+加密数字不能大于Long.MAX_VALUE)
     */
    abstract long definePrefix();

    /**
     * 获取key值
     *
     * @param index 坐标
     * @return key
     */
    private String getKeyValue(int index) {
        return this.keys[index];
    }

    /**
     * 获取key坐标
     *
     * @param key key
     * @return 坐标
     */
    private int getKeyIndex(String key) {
        return this.keyMapIndex.getOrDefault(key, -1);
    }

    /**
     * 加密前置数字处理
     *
     * @param target 加密的数字
     * @return 加前缀值的混淆结果
     */
    private long prefixEncryption(long target) {
        if (target <= this.maxNumber) {
            return target + this.prefix;
        }
        throw new IllegalArgumentException("target must be greater than " + this.maxNumber + ".");
    }

    /**
     * 解密后置数字处理
     *
     * @param target 解密中的数字
     * @return 去掉前缀值混淆的结果
     */
    private Long afterDecrypt(long target) {
        if (target >= this.prefix) {
            return target - this.prefix;
        }
        return null;
    }

    /**
     * 加密公式（同余处理）
     *
     * @param value  加密值
     * @param random 随机值
     * @return 加密结果
     */
    private int encryptionFormula(int value, int random) {
        return (value + random) % radix;
    }

    /**
     * 解密公式（同余处理）
     *
     * @param value  解密值
     * @param random 随机值
     * @return 解密结果
     */
    private int decryptFormula(int value, int random) {
        return (value - (random % radix) + radix) % radix;
    }

    /**
     * 生成唯一随机值（hash）
     * 子类可选择重写hash逻辑，确保输入能得到固定的输出
     *
     * @param targetInt 目标值
     * @return 唯一随机值
     */
    protected int genRandom(int targetInt, int random) {
        return targetInt * (random + 1) + 1;
    }

    /**
     * 将数字转换为指定进制，返回进制数字列表
     *
     * @param number 要转换的非负整数
     * @return 进制转换后的数字列表（高位在前）
     */
    private int[] convertToRadix(long number) {
        // 校验参数合法性
        if (number < 0) {
            throw new IllegalArgumentException("Number must be a non-negative integer.");
        }
        List<Integer> result = new ArrayList<>();
        // 迭代取余，收集进制位
        while (number > 0) {
            int remainder = (int) (number % radix);
            result.add(remainder);
            number = number / radix;
        }
        for (int i = result.size(); i < minLength; i++) {
            result.add(0);
        }
        // 反转列表，转为高位在前
        Collections.reverse(result);
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * 按固定长度分割字符串
     *
     * @param str    字符串
     * @param length 分割长度
     * @return 分割后的列表
     */
    private static List<String> splitByLength(String str, int length) {
        if (str == null || str.isEmpty() || length <= 0) {
            return new ArrayList<>();
        }

        return IntStream.range(0, (str.length() + length - 1) / length)
                .mapToObj(i -> str.substring(i * length, Math.min((i + 1) * length, str.length())))
                .collect(Collectors.toList());
    }

    /**
     * 加密数字（非负数）
     *
     * @param target 要加密的数字
     * @return 加密后的结果
     */
    public String encryption(long target) {
        //进制转换
        int[] radixList = convertToRadix(prefixEncryption(target));
        //线性同余随机转换
        List<Integer> randomIndexList = new ArrayList<>();

        // 初始值的随机数因子(进制和)，线性同余初始值
        int randomSeed = Arrays.stream(radixList).sum() - radixList[0];

        // 使用随机因子对原列表进行随机转换
        for (int i = 0; i < radixList.length; i++) {
            int index = encryptionFormula(radixList[i], genRandom(randomSeed, i));
            randomIndexList.add(index);
            randomSeed = index;
        }

        // 混淆
        StringBuilder result = new StringBuilder();
        for (int index : randomIndexList) {
            result.append(getKeyValue(index));
        }
        return result.toString();
    }


    /**
     * 解密成数字（Null为失败）
     *
     * @param encryptionTarget 加密值
     * @return 解密后的数字
     */
    public Long decrypt(String encryptionTarget) {

        // 过滤不符合条件的数据
        if (encryptionTarget.length() < reallyLength * minLength) {
            return null;
        }
        // 还原key值
        List<String> randomKeyList = splitByLength(encryptionTarget, reallyLength);

        // 还原随机值
        List<Integer> randomIndexList = new ArrayList<>();
        for (String value : randomKeyList) {
            int idx = getKeyIndex(value);
            if (idx == -1) {
                return null; // 如果找不到对应的值，返回null
            }
            randomIndexList.add(idx);
        }
        // 还原进制值
        int[] radixList = new int[randomIndexList.size()];
        int randomSeed;
        for (int i = randomIndexList.size() - 1; i > 0; i--) {
            randomSeed = randomIndexList.get(i - 1);
            // 线性同余还原
            int radixValue = decryptFormula(randomIndexList.get(i), genRandom(randomSeed, i));
            radixList[i] = radixValue;
        }
        // 计算第一个值的随机种子
        randomSeed = Arrays.stream(radixList).sum();

        radixList[0] = decryptFormula(randomIndexList.get(0), genRandom(randomSeed, 0));

        // 再次过滤，确保是按照加密规则生成，确保值是唯一对应的
        if (radixList.length > minLength && radixList[0] == 0) {
            return null;
        }

        long target = 0;
        for (int digit : radixList) {
            // 累乘进制
            target = target * radix + digit;
            // 溢出检查
            if (target < 0) {
                return null;
            }
        }
        return afterDecrypt(target);
    }
}

