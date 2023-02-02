package demo.domain.DTO;

import demo.domain.Task;
import lombok.Data;

import java.util.List;
@Data
public class OrderIdAndTaskDto {
    private String orderId;
    private List<Task> taskList;

}
