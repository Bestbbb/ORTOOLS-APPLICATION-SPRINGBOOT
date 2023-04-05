package demo.bootstrap;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import demo.domain.*;
import demo.domain.DTO.NextWorkDayDto;
import demo.service.HolidayService;
import demo.service.PhaseOneAssignedTaskService;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
@Component
public class DateUtil {
    static HolidayService holidayService = ContextUtil.getBean(HolidayService.class);

    static String nextDayURL = "http://timor.tech/api/holiday/workday/next/";
    static String isHolidayURL = "http://timor.tech/api/holiday/info/";
    static String rollURL = "https://www.mxnzp.com/api/holiday/single/";
    static String APPID_AND_APPSECRET  = "?app_id=jke8cmppk1lklsjo&app_secret=WHBkUzJtNGdOSjg0ZXpoWGdSQjE1UT09";
//    {
//        "code": 0,              // 0服务正常。-1服务出错
//     "type": {
//        "type": enum(0, 1, 2, 3), // 节假日类型，分别表示 工作日、周末、节日、调休。
//        "name": "周六",         // 节假日类型中文名，可能值为 周一 至 周日、假期的名字、某某调休。
//                "week": enum(1 - 7)    // 一周中的第几天。值为 1 - 7，分别表示 周一 至 周日。
//    },
//        "holiday": {
//        "holiday": false,     // true表示是节假日，false表示是调休
//                "name": "国庆前调休",  // 节假日的中文名。如果是调休，则是调休的中文名，例如'国庆前调休'
//                "wage": 1,            // 薪资倍数，1表示是1倍工资
//                "after": false,       // 只在调休下有该字段。true表示放完假后调休，false表示先调休再放假
//                "target": '国庆节'     // 只在调休下有该字段。表示调休的节假日
//    }
//    }
    //api版本获取日期
    public static JSONObject getNextWorkDay(String startDate){
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> map= new LinkedMultiValueMap();
        String res = restTemplate.getForObject(nextDayURL + startDate, String.class);


        return JSONObject.parseObject(res).getJSONObject("workday");
    }

    //数据库版本获取日期
    public static NextWorkDayDto getNextWorkDayNew(String startDate){
        LocalDate today = LocalDate.parse(startDate);
        NextWorkDayDto nextWorkDayDto = new NextWorkDayDto();
        LambdaQueryWrapper<Holiday> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Holiday::getHolidayDate,startDate);
        wrapper.eq(Holiday::getType,0);
        wrapper.last("limit 1");
        Holiday holiday = holidayService.getOne(wrapper);
        LocalDate holidayDate = holiday.getHolidayDate();
        long rest = holidayDate.toEpochDay()-today.toEpochDay();
        nextWorkDayDto.setDate(holidayDate.toString());
        nextWorkDayDto.setRest(rest);
        return nextWorkDayDto;
    }

    //旧版api
    public static Boolean getIsHoliday(String startDate){
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> map= new LinkedMultiValueMap();
        String res = restTemplate.getForObject(isHolidayURL + startDate, String.class);
        JSONObject type = JSONObject.parseObject(res).getJSONObject("type");
        //"type": enum(0, 1, 2, 3), // 节假日类型，分别表示 工作日、周末、节日、调休。
        Integer typeNumber = (int) type.get("type");
        if(typeNumber==1 || typeNumber==2){
            return true;
        }
        return false;
    }
    //新版数据库版
    public static Boolean getIsHolidayNew(String startDate){
        LambdaQueryWrapper<Holiday> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Holiday::getHolidayDate,startDate);
        Holiday holiday = holidayService.getOne(wrapper);

        Integer typeNumber = holiday.getType();
        if(typeNumber==1 || typeNumber==2){
            return true;
        }
        return false;
    }

    public static  Boolean getIsHolidayRoll(String startDate){
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> map= new LinkedMultiValueMap();
        String res = restTemplate.getForObject(rollURL + startDate + APPID_AND_APPSECRET, String.class);
        JSONObject data = JSONObject.parseObject(res).getJSONObject("data");
        //"type": enum(0, 1, 2, 3), // 节假日类型，分别表示 工作日、周末、节日、调休。
        Integer typeNumber = (int) data.get("type");

        if(typeNumber==1 || typeNumber==2){
            return true;
        }
        return false;
    }




    public static void setOutputDate(List<AssignedTask> assignedTasks){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter df2 = DateTimeFormatter.ofPattern("yyyyMMdd");

        AtomicReference<Integer> rest  = new AtomicReference<>(0);
//        for(int i=0;i<assignedTasks.size();i++){
//            AssignedTask task = assignedTasks.get(i);
//            LocalDate runTime = task.getRunTime();
//            String dateStr = runTime.format(df);
//            Boolean isHoliday = getIsHoliday(dateStr);
//            if (isHoliday) {
//                JSONObject nextWorkDay = getNextWorkDay(dateStr);
//                String newDate = nextWorkDay.get("date")+"";
//                rest.set((Integer) nextWorkDay.get("rest"));
//                LocalDate newRunTime = LocalDate.parse(newDate, df);
//                task.setRunTime(newRunTime);
//                List<AssignedTask> collect = assignedTasks.stream().
//                        filter(assignedTask -> assignedTask.getRunTime().isAfter(runTime)).collect(Collectors.toList());
//                collect.forEach(item->{
//                    LocalDate originalRunTime = item.getRunTime();
//                    LocalDate plusDays = originalRunTime.plusDays(rest.get());
//                    item.setRunTime(plusDays);
//                });
//
//            }
//        }
        Map<LocalDate, List<AssignedTask>> dateToTaskMap = assignedTasks.parallelStream().collect(Collectors.groupingBy(AssignedTask::getRunTime));
        Map<LocalDate,List<AssignedTask>> sortedMap = dateToTaskMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        sortedMap.forEach((date,assignedTaskList)->{
            System.out.println(date);


            LocalDate runTime = assignedTaskList.get(0).getRunTime();
//            assignedTaskList.forEach(i->i.setRunTime(LocalDate.of(2022,12,1)));
            String dateStr = runTime.format(df);
            String getNextDateStr = runTime.format(df);
            Boolean isHoliday = getIsHolidayNew(dateStr);
            if (isHoliday) {
                //旧的接口型写法
//                JSONObject nextWorkDay = getNextWorkDay(getNextDateStr);
//                String newDate = nextWorkDay.get("date")+"";
//                rest.set((Integer) nextWorkDay.get("rest"));
                NextWorkDayDto nextWorkDayDto = getNextWorkDayNew(getNextDateStr);
                String newDate = nextWorkDayDto.getDate();
                rest.set((int) nextWorkDayDto.getRest());
                LocalDate newRunTime = LocalDate.parse(newDate, df);
                List<AssignedTask> collect = assignedTasks.stream().
                        filter(assignedTask -> assignedTask.getRunTime().isAfter(runTime)).collect(Collectors.toList());
                collect.forEach(i->{
                    LocalDate originalRunTime = i.getRunTime();
                    LocalDate plusDays = originalRunTime.plusDays(rest.get());
                    i.setRunTime(plusDays);
                });
                assignedTaskList.forEach(i->i.setRunTime(newRunTime));

            }
//            try {
//                Thread.sleep(3000);//单位：毫秒
//
//            }catch (Exception e){
//
//            }


        });
        for (AssignedTask assignedTask : assignedTasks) {
            System.out.println("runTime:"+assignedTask.getRunTime());
        }
    }

    public static void main(String[] args) {
//        System.out.println(DateUtil.getIsHoliday("2022-12-31"));
//        JSONObject nextWorkDay = DateUtil.getNextWorkDay("2022-12-27");
//        System.out.println(nextWorkDay.get("date"));
//        LocalDate date1 = LocalDate.of(2022,12,22);
//        LocalDate date2 = LocalDate.of(2022,12,22);
//        boolean after = date1.equals(date2);
//        System.out.println(after);
//
//        Boolean isHolidayRoll = getIsHolidayRoll("20221229");
//        System.out.println(isHolidayRoll);
        getNextWorkDayNew("2023-03-14");
    }
}
