package com.daybreak.clock;
import org.junit.*;
import static org.junit.Assert.*;
import org.json.*;
import java.util.*;

public class AlarmScheduleTest {
    private TimeZone previous;
    @Before public void setup(){previous=TimeZone.getDefault();TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));}
    @After public void cleanup(){TimeZone.setDefault(previous);}
    private long date(int month,int day,int hour,int minute){Calendar c=Calendar.getInstance();c.clear();c.set(2026,month-1,day,hour,minute,0);return c.getTimeInMillis();}
    private JSONObject alarm(String time,int...days)throws Exception{JSONArray d=new JSONArray();for(int day:days)d.put(day);return new JSONObject().put("time",time).put("days",d);}
    @Test public void upcomingOnceUsesToday()throws Exception{assertEquals(date(11,21,8,30),AlarmStore.next(alarm("08:30"),date(11,21,7,0)));}
    @Test public void passedOnceUsesTomorrow()throws Exception{assertEquals(date(11,22,7,0),AlarmStore.next(alarm("07:00"),date(11,21,8,0)));}
    @Test public void weekdaysSkipWeekend()throws Exception{assertEquals(date(11,23,7,0),AlarmStore.next(alarm("07:00",1,2,3,4,5),date(11,21,6,0)));}
    @Test public void exactMinuteDoesNotRefire()throws Exception{assertEquals(date(11,28,7,0),AlarmStore.next(alarm("07:00",6),date(11,21,7,0)));}
    @Test public void springDstKeepsWallClockTime()throws Exception{assertEquals(date(3,29,7,0),AlarmStore.next(alarm("07:00"),date(3,28,23,0)));}
    @Test public void autumnDstKeepsWallClockTime()throws Exception{assertEquals(date(10,25,7,0),AlarmStore.next(alarm("07:00"),date(10,24,23,0)));}
}
