package demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@NoArgsConstructor
@Data
@AllArgsConstructor
public class ScheduleDate {
    private LocalDateTime localDateTime;

    private Timeslot timeslot;

}
