package demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class Holiday {
    @TableId(value = "holiday_id",type = IdType.INPUT)
    private String holidayId;
    private LocalDate holidayDate;
    private Integer year;
    private Integer month;
    private Integer day;
    private Integer type;
    private LocalDate createDate;
    private LocalDate updateDate;

}
