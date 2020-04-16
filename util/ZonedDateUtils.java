package com.inspur.bss.waf.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspur.bss.waf.common.constant.DatePartternConstant;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hexinyu
 * @create 2019/12/13 10:05
 */
public class ZonedDateUtils {

    public static final DateTimeFormatter DF_DATETIME = DateTimeFormatter.ofPattern(DatePartternConstant.yyyyMMddHHmmss_EN);
    public static final DateTimeFormatter DF_DATE = DateTimeFormatter.ofPattern(DatePartternConstant.yyyyMMdd_EN);
    public static final DateTimeFormatter DF_ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static final ZoneId ZONE_UTC8 = ZoneId.of("UTC+8");
    public static final ZoneId ZONE_UTC = ZoneId.of("UTC");


    /**
     * 获取当前时间
     *
     * @return Date 时间戳
     */
    public static Date now() {
        return Date.from(Instant.now());
    }

    /**
     * 获取当前时间
     *
     * @param zoneId 时区id
     * @return ZonedDateTime 带时区的DateTime类型
     */
    public static ZonedDateTime now(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId);
    }

    /**
     * 解析时间
     *
     * @param dateTimeStr 时间字符串
     * @param df          formatter
     * @return ZonedDateTime 带时区的DateTime类型
     */
    public static ZonedDateTime parse(String dateTimeStr, DateTimeFormatter df) {
        try{
            return ZonedDateTime.parse(dateTimeStr, df);
        }catch (Exception e1){
            try{
                return ZonedDateTime.of(LocalDate.parse(dateTimeStr, df),LocalTime.MIN,df.getZone());
            }
            catch (Exception e2){
                return ZonedDateTime.of(LocalDate.ofEpochDay(0),LocalTime.parse(dateTimeStr, df),df.getZone());
            }
        }
    }

    /**
     * 格式化时间
     *
     * @param zonedDateTime 带时间的DateTime
     * @param df            formatter
     * @return 时间字符串
     */
    public static String format(ZonedDateTime zonedDateTime, DateTimeFormatter df) {
        return df.format(zonedDateTime);
    }

    /**
     * 解析ISO标准时间字符串
     *
     * @param dateTimeStrZoned 时间字符串ISO标准
     *                         如: 2017-01-01T00:00:00Z 2017-01-01T00:00:00+08:00
     * @return Date 时间戳
     */
    public static Date parseIso(String dateTimeStrZoned) {
        return zonedDate2Date(parseIsoZoned(dateTimeStrZoned));
    }

    /**
     * 解析yyyy-MM-dd HH:mm:ss型的时间字符串
     *
     * @param dateTimeStrNoZoned 形如"yyyy-MM-dd HH:mm:ss"的时间字符串
     * @param zoneId             时区id
     * @return Date 时间戳
     */
    public static Date parseDateTime(String dateTimeStrNoZoned, ZoneId zoneId) {
        return zonedDate2Date(parseDateTimeZoned(dateTimeStrNoZoned, zoneId));
    }

    /**
     * 解析yyyy-MM-dd型的时间字符串
     *
     * @param dateTimeStrNoZoned 形如"yyyy-MM-dd"的时间字符串
     * @param zoneId             时区id
     * @return Date 时间戳
     */
    public static Date parseDate(String dateTimeStrNoZoned, ZoneId zoneId) {
        return zonedDate2Date(parseDateZoned(dateTimeStrNoZoned, zoneId));
    }

    /**
     * 解析{@param pattern}(自定义)的时间字符串
     *
     * @param dateTimeStrNoZoned 形如{@param pattern}的时间字符串
     * @param zoneId             时区id
     * @param pattern            自定义时间解析模式,见{@link DatePartternConstant}
     * @return Date 时间戳
     */
    public static Date parseByPattern(String dateTimeStrNoZoned, ZoneId zoneId, String pattern) {
        return zonedDate2Date(parseZonedByPattern(dateTimeStrNoZoned, zoneId, pattern));
    }

    /**
     * 把java Date类型转换为ISO时间字符串
     *
     * @param date   日期
     * @param zoneId 时区
     * @return ISO时间字符串
     */
    public static String format2Iso(Date date, ZoneId zoneId) {
        return format2Iso(date2ZonedDate(date, zoneId));
    }

    /**
     * 把java Date类型转化为形如"yyyy-MM-dd HH:mm:ss"的日期字符串
     *
     * @param date   日期
     * @param zoneId 时区
     * @return 形如"yyyy-MM-dd HH:mm:ss"的日期字符串
     */
    public static String format2DateTime(Date date, ZoneId zoneId) {
        return format2DateTime(date2ZonedDate(date, zoneId));
    }

    /**
     * 把java Date类型转化为形如"yyyy-MM-dd"的日期字符串
     *
     * @param date   日期
     * @param zoneId 时区
     * @return 形如"yyyy-MM-dd"的日期字符串
     */
    public static String format2Date(Date date, ZoneId zoneId) {
        return format2Date(date2ZonedDate(date, zoneId));
    }

    /**
     * 把java Date类型转化为自定义字符串{@param pattern}的日期字符串
     *
     * @param date    日期
     * @param zoneId  时区
     * @param pattern 自定义时间字符串,见{@link DatePartternConstant}
     * @return 自定义时间字符串 {@param pattern}
     */
    public static String format2Pattern(Date date, ZoneId zoneId, String pattern) {
        return format2Pattern(date2ZonedDate(date, zoneId), pattern);
    }

    /**
     * 解析ISO标准时间字符串
     *
     * @param dateTimeStrZoned 时间字符串ISO标准
     *                         如: 2017-01-01T00:00:00Z 2017-01-01T00:00:00.000Z
     *                         2017-01-01T00:00:00+08:00
     * @return ZonedDateTime
     */
    public static ZonedDateTime parseIsoZoned(String dateTimeStrZoned) {
        return parse(dateTimeStrZoned, DF_ISO);
    }

    /**
     * 解析yyyy-MM-dd HH:mm:ss型的时间字符串
     *
     * @param dateTimeStrNoZoned 形如"yyyy-MM-dd HH:mm:ss"的时间字符串
     * @param zoneId             时区id
     * @return ZonedDateTime
     */
    public static ZonedDateTime parseDateTimeZoned(String dateTimeStrNoZoned, ZoneId zoneId) {
        return parse(dateTimeStrNoZoned, DF_DATETIME.withZone(zoneId));
    }

    /**
     * 解析yyyy-MM-dd型的时间字符串
     *
     * @param dateTimeStrNoZoned 形如"yyyy-MM-dd"的时间字符串
     * @param zoneId             时区id
     * @return ZonedDateTime 时间戳
     */
    public static ZonedDateTime parseDateZoned(String dateTimeStrNoZoned, ZoneId zoneId) {
        return parse(dateTimeStrNoZoned + " 00:00:00", DF_DATETIME.withZone(zoneId));
    }

    /**
     * 解析{@param pattern}(自定义)的时间字符串
     *
     * @param dateTimeStrNoZoned 形如{@param pattern}的时间字符串
     * @param zoneId             时区id
     * @param pattern            自定义时间解析模式,见{@link DatePartternConstant}
     * @return ZonedDateTime
     */
    public static ZonedDateTime parseZonedByPattern(String dateTimeStrNoZoned, ZoneId zoneId, String pattern) {
        return parse(dateTimeStrNoZoned, DateTimeFormatter.ofPattern(pattern).withZone(zoneId));
    }

    /**
     * 把java Instant 类型转换为ISO时间字符串
     *
     * @param zonedDateTime
     * @return ISO时间字符串
     */
    public static String format2Iso(ZonedDateTime zonedDateTime) {
        return format(zonedDateTime, DF_ISO);
    }

    /**
     * 把java Instant 类型转化为形如"yyyy-MM-dd HH:mm:ss"的日期字符串
     *
     * @param zonedDateTime
     * @return 形如"yyyy-MM-dd HH:mm:ss"的日期字符串
     */
    public static String format2DateTime(ZonedDateTime zonedDateTime) {
        return format(zonedDateTime, DF_DATETIME);
    }

    /**
     * 把java Instant 类型转化为形如"yyyy-MM-dd"的日期字符串
     *
     * @param zonedDateTime
     * @return 形如"yyyy-MM-dd"的日期字符串
     */
    public static String format2Date(ZonedDateTime zonedDateTime) {
        return format(zonedDateTime, DF_DATE);
    }

    /**
     * 把java Date类型转化为自定义字符串{@param pattern}的日期字符串
     *
     * @param zonedDateTime
     * @param pattern       自定义时间字符串,见{@link DatePartternConstant}
     * @return 自定义时间字符串 {@param pattern}
     */
    public static String format2Pattern(ZonedDateTime zonedDateTime, String pattern) {
        return format(zonedDateTime, DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * 获取当月的第一天时间 零时区
     *
     * @return
     */
    public static Date getFirstDayOfMonth() {
        return getFirstDayOfMonth(ZONE_UTC);
    }

    /**
     * 获取当月的第一天时间
     *
     * @param zoneId 时区id
     * @return
     */
    public static Date getFirstDayOfMonth(ZoneId zoneId) {
        return zonedDate2Date(getFirstDayOfMonth(ZonedDateTime.now(zoneId)));
    }

    /**
     * 获取指定时间的月份的第一天时间
     *
     * @param date   指定的时间
     * @param zoneId 时区id
     * @return
     */
    public static Date getFirstDayOfMonth(Date date, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = date2ZonedDate(date, zoneId);
        return zonedDate2Date(getFirstDayOfMonth(zonedDateTime));
    }

    public static ZonedDateTime getFirstDayOfMonth(ZonedDateTime zonedDateTime) {
        return zonedDateTime.with(TemporalAdjusters.firstDayOfMonth())
                .with(LocalTime.MIN);
    }

    /**
     * 获取当月的最后时间 零时区
     *
     * @return
     */
    public static Date getLastDayOfMonth() {
        return getLastDayOfMonth(ZONE_UTC);
    }

    /**
     * 获取当月的最后时间
     *
     * @param zoneId 时区id
     * @return
     */
    public static Date getLastDayOfMonth(ZoneId zoneId) {
        return zonedDate2Date(getLastDayOfMonth(ZonedDateTime.now(zoneId)));
    }

    /**
     * 获取指定时间所在月份的最后时间
     *
     * @param date   时间戳
     * @param zoneId 时区
     * @return
     */
    public static Date getLastDayOfMonth(Date date, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = date2ZonedDate(date, zoneId);
        return zonedDate2Date(getLastDayOfMonth(zonedDateTime));
    }

    public static ZonedDateTime getLastDayOfMonth(ZonedDateTime zonedDateTime) {
        return zonedDateTime.with(TemporalAdjusters.lastDayOfMonth())
                .with(LocalTime.MAX);
    }

    /**
     * 获取指定时间所在天的最后时间 零时区
     *
     * @param date
     * @return
     */
    public static Date getEndOfDay(Date date) {
        return getEndOfDay(date, ZONE_UTC);
    }

    /**
     * 获取指定时间所在天的最后时间
     *
     * @param date
     * @param zoneId 时区id
     * @return
     */
    public static Date getEndOfDay(Date date, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = date2ZonedDate(date, zoneId);
        return zonedDate2Date(getEndOfDay(zonedDateTime));
    }

    public static ZonedDateTime getEndOfDay(ZonedDateTime zonedDateTime) {
        return zonedDateTime.with(LocalTime.MAX);
    }

    /**
     * 获取指定时间所在天的开始 零时区
     *
     * @param date
     * @return
     */
    public static Date getBeginOfDay(Date date) {
        return getBeginOfDay(date, ZONE_UTC);
    }

    /**
     * 获取指定时间所在天的最后时间
     *
     * @param date
     * @param zoneId 时区id
     * @return
     */
    public static Date getBeginOfDay(Date date, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = date2ZonedDate(date, zoneId);
        return zonedDate2Date(getBeginOfDay(zonedDateTime));
    }

    public static ZonedDateTime getBeginOfDay(ZonedDateTime zonedDateTime) {
        return zonedDateTime.with(LocalTime.MIN);
    }

    /**
     * Date 转 ZonedDateTime
     *
     * @param zonedDateTime
     * @return
     */
    public static Date zonedDate2Date(ZonedDateTime zonedDateTime) {
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * ZonedDateTime 转 Date
     *
     * @param date
     * @param zoneId 时区id
     * @return
     */
    public static ZonedDateTime date2ZonedDate(Date date, ZoneId zoneId) {
        return ZonedDateTime.ofInstant(date.toInstant(), zoneId);
    }

    /**
     * 获取 [startTimeStr,endTimeStr]中的时间,间隔为1天
     * @param startTimeStr
     * @param endTimeStr
     * @param fromZone
     * @return
     */
    public static List<String> splitDateIntervalByDay(String startTimeStr, String endTimeStr, ZoneId fromZone, ZoneId toZone){
        return splitDateIntervalByMinute(parseDateTime(startTimeStr,fromZone),parseDateTime(endTimeStr,fromZone),60 * 24)
                .stream()
                .map(d -> format2DateTime(d,toZone))
                .collect(Collectors.toList());
    }

    public static List<Date> splitDateIntervalByMinute(Date startTime, Date endTime, int diffMinute) {
        List<Date> rList = new ArrayList<>();

        if (startTime.compareTo(endTime) > 0) {
            return rList;
        }
        final int minute2millisecond = 1000 * 60;
        long diffTimestamp1 = 0L;
        long quotient1 = diffTimestamp1 / (diffMinute * minute2millisecond);
        Date minuteBegin = new Date(startTime.getTime() + quotient1 * diffMinute * minute2millisecond);

        long diffTimestamp2 = 0L;
        long quotient2 = diffTimestamp2 / (diffMinute * minute2millisecond);
        Date minuteEnd = new Date(endTime.getTime() + quotient2 * diffMinute * minute2millisecond);


        Date tempDate = new Date(minuteBegin.getTime());
        while (tempDate.compareTo(minuteEnd) <= 0) {
            rList.add(new Date(tempDate.getTime()));
            tempDate.setTime(tempDate.getTime() + diffMinute * minute2millisecond);
        }

        return rList;
    }
    /**
     * 时间字符串的时区进行转换
     * @param dateStr 形如"yyyy-MM-dd HH:mm:ss"
     * @param fromZone 转换前的时区
     * @param toZone 转换后的时区
     * @return 形如"yyyy-MM-dd HH:mm:ss"
     */
    public static String transfterZoneOfDateTimeStr(String dateStr,ZoneId fromZone,ZoneId toZone){
        return format2DateTime(parseDateTimeZoned(dateStr, fromZone).withZoneSameInstant(toZone));
    }

    /**
     * 把形如{@param pattern}的时间字符串的时区进行转换
     * @param dateStr 形如"{@param pattern}
     * @param fromZone 转换前的时区
     * @param toZone 转换后的时区
     * @param pattern 见{@link DatePartternConstant}
     * @return 形如"{@param pattern}
     */
    public static String transfterZoneOfPatternStr(String dateStr,ZoneId fromZone,ZoneId toZone,String pattern){
        return format2Date(parseZonedByPattern(dateStr, fromZone,pattern).withZoneSameInstant(toZone));
    }

    /**
     * 获取 {@param zonedDateTime} 偏移{@param offsetDay}天的开始时间
     * @param zonedDateTime  ZonedDateTime
     * @param offsetDay 偏移天数
     * @return ZonedDateTime
     */
    public static ZonedDateTime getBeginOfDayOffsetByDay(ZonedDateTime zonedDateTime,int offsetDay){
        return getBeginOfDay(zonedDateTime.plusDays(offsetDay));
    }

    /**
     * 获取 {@param zonedDateTime} 偏移{@param offsetMonth}月的开始时间
     * @param zonedDateTime  ZonedDateTime
     * @param offsetMonth 偏移月数
     * @return ZonedDateTime
     */
    public static ZonedDateTime getBeginOfDayOffsetByMonth(ZonedDateTime zonedDateTime,int offsetMonth){
        return getBeginOfDay(getOffsetByMonth(zonedDateTime, offsetMonth));
    }

    public static ZonedDateTime getOffsetByMonth(ZonedDateTime zonedDateTime, int offsetMonth){
        return zonedDateTime.plusMonths(offsetMonth);
    }

    /**
     *
     * 获取 {@param zonedDateTimeNoZone} 偏移{@param offsetDay}天的开始时间字符串（形如{@param pattern}）
     * @param zonedDateTimeNoZone 不带时区的时间字符串
     * @param offsetDay 偏移天数
     * @param zoneId 时区id
     * @param pattern 见{@link DatePartternConstant}
     * @return String
     */
    public static String getBeginOfDayOffsetByDay(String zonedDateTimeNoZone,int offsetDay,ZoneId zoneId,String pattern){
        return format2Pattern(getBeginOfDayOffsetByDay(parseZonedByPattern(zonedDateTimeNoZone,zoneId,pattern), offsetDay),pattern);
    }

    /**
     *  获取 {@param date} 偏移{@param offsetDay}天的开始时间
     * @param date Date
     * @param offsetDay 偏移天数
     * @param zoneId 时区id
     * @return Date
     */
    public static Date getBeginOfDayOffsetByDay(Date date,int offsetDay,ZoneId zoneId){
        return zonedDate2Date(getBeginOfDayOffsetByDay(date2ZonedDate(date,zoneId), offsetDay));
    }

    /**
     *  获取 {@param date} 偏移{@param offsetMonth}月的开始时间
     * @param date Date
     * @param offsetMonth 偏移月数
     * @param zoneId 时区id
     * @return Date
     */
    public static Date getBeginOfDayOffsetByMonth(Date date,int offsetMonth,ZoneId zoneId){
        return zonedDate2Date(getBeginOfDayOffsetByMonth(date2ZonedDate(date,zoneId), offsetMonth));
    }

    public static Date getOffsetByMonth(Date date,int offsetMonth,ZoneId zoneId){
        return zonedDate2Date(getOffsetByMonth(date2ZonedDate(date,zoneId), offsetMonth));
    }

    /**
     *  获取 {@param zonedDateTime} 偏移{@param offsetDay}天的结束时间
     * @param zonedDateTime ZonedDateTime
     * @param offsetDay 偏移天数
     * @return ZonedDateTime
     */
    public static ZonedDateTime getEndOfDayOffsetByDay(ZonedDateTime zonedDateTime,int offsetDay){
        return getEndOfDay(zonedDateTime.plusDays(offsetDay));
    }

    /**
     * 获取 {@param zonedDateTimeNoZone} 偏移{@param offsetDay}天的结束时间字符串（形如{@param pattern}）
     * @param zonedDateTimeNoZone 不带时区的时间字符串
     * @param offsetDay 偏移天数
     * @param zoneId 时区id
     * @param pattern 见{@link DatePartternConstant}
     * @return String 时间字符串形如{@param pattern}
     */
    public static String getEndOfDayStrOffsetByDay(String zonedDateTimeNoZone, int offsetDay, ZoneId zoneId, String pattern){
        return format2Pattern(getEndOfDayOffsetByDay(parseZonedByPattern(zonedDateTimeNoZone,zoneId,pattern), offsetDay),pattern);
    }

    /**
     *  获取 {@param zonedDateTimeNoZone} 偏移{@param offsetDay}天的结束时间
     * @param zonedDateTimeNoZone 不带时区的时间字符串
     * @param offsetDay 偏移天数
     * @param zoneId 时区id
     * @param pattern 见{@link DatePartternConstant}
     * @return Date
     */
    public static Date getEndOfDayOffsetByDay(String zonedDateTimeNoZone, int offsetDay,ZoneId zoneId,String pattern){
        return zonedDate2Date(getEndOfDayOffsetByDay(parseZonedByPattern(zonedDateTimeNoZone,zoneId,pattern), offsetDay));
    }

    /**
     *  获取 {@param date} 偏移{@param offsetDay}天的结束时间
     * @param date 不带时区的时间字符串
     * @param offsetDay 偏移天数
     * @param zoneId 时区id
     * @param pattern 见{@link DatePartternConstant}
     * @return String 形如
     */
    public static String getEndOfDayStrOffsetByDay(Date date, int offsetDay, ZoneId zoneId, String pattern){
        return format2Pattern(getEndOfDayOffsetByDay(date2ZonedDate(date,zoneId), offsetDay),pattern);
    }

    /**
     *  获取 {@param date} 偏移{@param offsetDay}天的结束时间
     * @param date Date
     * @param offsetDay 偏移天数
     * @param zoneId 时区id
     * @return Date
     */
    public static Date getEndOfDayOffsetByDay(Date date,int offsetDay,ZoneId zoneId){
        return zonedDate2Date(getEndOfDayOffsetByDay(date2ZonedDate(date,zoneId), offsetDay));
    }

    /**
     * 获取 {@param zonedDateTime} 偏移{@param offsetDay}小时的开始时间
     * @param zonedDateTime  ZonedDateTime
     * @param offsetHour 偏移小时
     * @return ZonedDateTime
     */
    public static ZonedDateTime getBeginOfDayOffsetByHour(ZonedDateTime zonedDateTime, int offsetHour){
        return getBeginOfDay(zonedDateTime).plusHours(offsetHour);
    }

    public static Date getBeginOfDayOffsetByHour(Date date, int offsetHour ,ZoneId zoneId){
        return zonedDate2Date(getBeginOfDayOffsetByHour(date2ZonedDate(date,zoneId),offsetHour));
    }

    public static void main(String[] args) throws JsonProcessingException {
        String pattern = DatePartternConstant.yyyyMMdd_CN;
        ZoneId z = ZONE_UTC8;
        //String to Date
        System.out.println("String to Date:");
        System.out.println(parseIso("2019-01-01T11:00:00.03Z"));
        System.out.println(parseDateTime("2019-01-01 12:00:00",z));
        System.out.println(parseDate("2019-01-11",z));
        System.out.println(parseByPattern("2019年01月22日",z,pattern));
        System.out.println(parseByPattern("00:00:00",z,DatePartternConstant.HHmmss_EN));
        System.out.println();

        format2DateTime(parseDateTime("2019-01-01 12:00:00",ZONE_UTC), ZONE_UTC8);

        //Date to String
        Date date1 = parseIso("2019-01-01T16:01:01Z");
        System.out.println("Date to String:");
        System.out.println(format2DateTime(date1,z));
        System.out.println(format2Date(date1,z));
        System.out.println(format2Iso(date1,z));
        System.out.println(format2Pattern(date1,z,pattern));
        System.out.println();
        // splitDateIntervalByDay
        System.out.println("splitDateIntervalByDay:");
        String startT = "2019-01-01 00:00:00";
        String endT = "2019-01-04 00:00:00";
        ZonedDateUtils.splitDateIntervalByDay(startT,endT,ZonedDateUtils.ZONE_UTC8,ZONE_UTC)
                .forEach(System.out::println);
        System.out.println();
        //getLastDayOfMonth
//        String s = format2Iso(parseDateTime("", ZONE_PLUS_8), ZONE_UTC);
        System.out.println("getLastDayOfMonth:");
        Date testDate = parseIso("2019-01-01T00:00:00Z");
        System.out.println(getLastDayOfMonth());
        System.out.println(getLastDayOfMonth(testDate, z));
        System.out.println(getLastDayOfMonth(z));
        System.out.println();
        //getFirstDayOfMonth
        Date testDate1 = parseIso("2019-01-01T00:00:00Z");
        System.out.println(getFirstDayOfMonth());
        System.out.println(getFirstDayOfMonth(testDate1, z));
        System.out.println(getFirstDayOfMonth(z));
        System.out.println();
        //getBeginOfDay
        System.out.println("get");
        Date testDate2 = parseIso("2019-01-01T00:00:00Z");
        System.out.println(getBeginOfDay(new Date()));
        System.out.println(getBeginOfDay(testDate2));
        System.out.println(getBeginOfDay(testDate2,z));

        HashMap<String, Object> map = new HashMap<>();
        map.put("date",new Date());
        System.out.println(map.get("date"));

        String json0 = JSON.toJSONString(map,SerializerFeature.UseISO8601DateFormat);
        System.out.println(json0);
        JSON.defaultTimeZone = TimeZone.getTimeZone(ZONE_UTC);
        String json1 = JSON.toJSONString(map,SerializerFeature.UseISO8601DateFormat);
        System.out.println(json1);
        JSON.defaultTimeZone = TimeZone.getTimeZone(ZONE_UTC);
        String json2 = JSON.toJSONString(map,SerializerFeature.WriteDateUseDateFormat);
        System.out.println(json2);

        Map<String, Date> stringDateMap = JSON.parseObject(json1, new TypeReference<Map<String, Date>>() {
        });
        System.out.println(JSON.toJSONString(stringDateMap,SerializerFeature.UseISO8601DateFormat));

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatePartternConstant.yyyyMMddHHmmss_EN);
        simpleDateFormat.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("+8")));
        objectMapper.setDateFormat(simpleDateFormat);
        System.out.println();
        System.out.println(objectMapper.writeValueAsString(map));
        System.out.println();
        System.out.println(ZONE_UTC8.getId());
        System.out.println(TimeZone.getTimeZone("UTC+8"));
        ZonedDateTime beginOfDay =  ZonedDateTime.now(ZONE_UTC8).plusDays(-1);
        System.out.println(format2Iso(beginOfDay));
        beginOfDay = getBeginOfDay(beginOfDay);
        System.out.println(format2Iso(beginOfDay));

        System.out.println("----------------------");
        String dateStr = "2019-01-01 16:00:00";
        Date date = parseDateTime(dateStr, ZONE_UTC8);
        System.out.println(getEndOfDayOffsetByDay(date,0, ZONE_UTC8).getTime());
        System.out.println(format2Iso(getEndOfDayOffsetByDay(dateStr,0, ZONE_UTC8,DatePartternConstant.yyyyMMddHHmmss_EN),ZONE_UTC));
    }
}


