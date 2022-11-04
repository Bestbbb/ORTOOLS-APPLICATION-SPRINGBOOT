package demo.domain;

import com.google.ortools.sat.IntervalVar;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class Timeslot {

    private Integer shiftCount;
//    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private IntervalVar interval;





}
