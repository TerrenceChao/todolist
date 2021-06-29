package com.example.todolist.util;

import com.example.todolist.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class DatetimeUtil {

    public Integer getWeekOfYear(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constant.WEEK_OF_YEAR_FORMAT_V2);
        String weekOfYear = sdf.format(date);
        return Integer.valueOf(weekOfYear);
    }


    /**
     * simple test...
     * @param args
     */
    public static void main(String[] args) {
        DatetimeUtil datetimeUtil = new DatetimeUtil();
        System.out.println(datetimeUtil.getWeekOfYear(new Date()));
    }
}
