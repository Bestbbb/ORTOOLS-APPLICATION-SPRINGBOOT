package demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import demo.domain.AssignedTask;
import demo.mapper.AssignedTaskMapper;
import demo.service.AssignedTaskService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Liubin
 * @since 2022-11-20
 */
@Service
public class AssignedTaskServiceImpl extends ServiceImpl<AssignedTaskMapper, AssignedTask> implements AssignedTaskService {

}
