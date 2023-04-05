package demo.controller;

import demo.bootstrap.DateUtil;
import demo.domain.DTO.NextWorkDayDto;
import demo.domain.Holiday;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolidayTestController {
    @PostMapping("/getNextWorkDay")
    public NextWorkDayDto getNextWorkDay(String date){
        return DateUtil.getNextWorkDayNew(date);
    }
}
