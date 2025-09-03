package fun.luqing.Utils.Else;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberToChineseConverter {

    private static final String[] CHINESE_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] CHINESE_UNITS = {"", "十", "百", "千"};
    private static final String[] CHINESE_BIG_UNITS = {"", "万", "亿", "兆"};
    private static final Map<String, String> SPECIAL_FORMATS = new HashMap<>();

    static {
        SPECIAL_FORMATS.put("年", "年");
        SPECIAL_FORMATS.put("月", "月");
        SPECIAL_FORMATS.put("日", "日");
        SPECIAL_FORMATS.put("号", "号");
        SPECIAL_FORMATS.put("时", "时");
        SPECIAL_FORMATS.put("分", "分");
        SPECIAL_FORMATS.put("秒", "秒");
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+\\.?\\d*)([年月日号时分秒]?)");

    public static String convertNumbersToChinese(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String numberStr = matcher.group(1);
            String suffix = matcher.group(2);

            String chineseNumber = suffix.isEmpty() || !SPECIAL_FORMATS.containsKey(suffix)
                    ? convertToChinese(numberStr)
                    : convertToChineseSpecial(numberStr, suffix);

            matcher.appendReplacement(result, chineseNumber);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String convertToChinese(String numberStr) {
        if (numberStr.contains(".")) {
            return convertDecimalToChinese(numberStr);
        }

        try {
            long number = Long.parseLong(numberStr);
            if (number == 0) {
                return CHINESE_NUMBERS[0];
            }

            return convertIntegerToChinese(number);
        } catch (NumberFormatException e) {
            return numberStr; // 如果不是有效数字，返回原字符串
        }
    }

    private static String convertIntegerToChinese(long number) {
        StringBuilder result = new StringBuilder();
        int unitPos = 0;

        while (number > 0) {
            int segment = (int) (number % 10000);
            if (segment > 0 || result.length() > 0) {
                result.insert(0, convertSegment(segment) + CHINESE_BIG_UNITS[unitPos]);
            }
            number /= 10000;
            unitPos++;
        }

        String res = result.toString().replaceAll("零+", "零").replaceAll("零+$", "");
        return res.startsWith("一十") ? res.substring(1) : res;
    }

    private static String convertDecimalToChinese(String numberStr) {
        String[] parts = numberStr.split("\\.");
        String integerPart = convertToChinese(parts[0]);
        StringBuilder decimalPart = new StringBuilder("点");

        for (char c : parts[1].toCharArray()) {
            decimalPart.append(CHINESE_NUMBERS[Character.getNumericValue(c)]);
        }

        return integerPart + decimalPart;
    }

    private static String convertSegment(int segment) {
        if (segment == 0) {
            return CHINESE_NUMBERS[0];
        }

        StringBuilder result = new StringBuilder();
        boolean zeroFlag = false;

        for (int i = 0; i < 4; i++) {
            int digit = (segment / (int) Math.pow(10, 3 - i)) % 10;

            if (digit != 0) {
                if (zeroFlag) {
                    result.append(CHINESE_NUMBERS[0]);
                    zeroFlag = false;
                }
                result.append(CHINESE_NUMBERS[digit]);
                result.append(CHINESE_UNITS[3 - i]);
            } else if (!zeroFlag && segment % (int) Math.pow(10, 4 - i) != 0) {
                zeroFlag = true;
            }
        }

        return result.toString();
    }

    private static String convertToChineseSpecial(String numberStr, String suffix) {
        if (numberStr.contains(".")) {
            numberStr = numberStr.split("\\.")[0];
        }

        try {
            int number = Integer.parseInt(numberStr);

            if ("月".equals(suffix) || "号".equals(suffix) || "日".equals(suffix)) {
                return convertMonthOrDay(number) + SPECIAL_FORMATS.get(suffix);
            } else if ("时".equals(suffix)) {
                return convertHour(number) + SPECIAL_FORMATS.get(suffix);
            } else if ("分".equals(suffix) || "秒".equals(suffix)) {
                return convertMinuteOrSecond(number) + SPECIAL_FORMATS.get(suffix);
            }

            return convertToChinese(numberStr) + SPECIAL_FORMATS.get(suffix);
        } catch (NumberFormatException e) {
            return numberStr + suffix;
        }
    }

    private static String convertMonthOrDay(int number) {
        if (number <= 10) return "十";
        if (number <= 20) return "二十";
        if (number <= 30) return "三十";
        return CHINESE_NUMBERS[number / 10] + "十" + (number % 10 != 0 ? CHINESE_NUMBERS[number % 10] : "");
    }

    private static String convertHour(int number) {
        if (number == 0) return "零";
        if (number == 10) return "十";
        if (number == 20) return "二十";
        return (number < 10 ? CHINESE_NUMBERS[number] : "二十" + CHINESE_NUMBERS[number % 10]);
    }

    private static String convertMinuteOrSecond(int number) {
        return (number == 0) ? "零" : CHINESE_NUMBERS[number / 10] + "十" + (number % 10 != 0 ? CHINESE_NUMBERS[number % 10] : "");
    }
}
