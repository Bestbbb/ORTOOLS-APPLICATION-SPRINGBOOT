package demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Period {

    // 生产计划就绪时间
    private LocalDateTime startTime;
    // 生产计划要求完成时间
    private LocalDateTime endTime;
    //持续时间
    private Integer requiredDuration;
}
