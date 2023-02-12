package demo.controller;

import com.alibaba.fastjson2.JSONObject;
import com.google.ortools.Loader;
import demo.bootstrap.DataGenerator;
import demo.domain.*;
import demo.jsonUtils.LoadFile;
import demo.service.PhaseOneAssignedTaskService;
import demo.service.PhaseThreeAssignedTaskService;
import demo.service.PhaseTwoAssignedTaskService;
import demo.solver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
public class RequestSolutionController {
    @Autowired
    private MesConfig config;
    @Autowired
    private PhaseOneAssignedTaskService phaseOneAssignedTaskService;
    @Autowired
    private PhaseTwoAssignedTaskService phaseTwoAssignedTaskService;
    @Autowired
    private PhaseThreeAssignedTaskService phaseThreeAssignedTaskService;
    @Resource
    private DataSourceTransactionManager dataSourceTransactionManager;

    @PostMapping("/planner/requestSolution")
    public JSONObject requestSolution(String algorithmFileInputPath, String algorithmFileId) {
        System.out.println(config.getUrl());
        JSONObject jsonObject = new JSONObject();
        Input input = LoadFile.readJsonFile(algorithmFileInputPath);
        List<ResourceItem> resourceItems = DataGenerator.generateResources(input);
        LocalDateTime startTime = input.getPlanningPeriod().getStartTime();

        List<ManufacturerOrder> manufacturerOrders = DataGenerator.generateOrderList(input);

        List<List<ManufacturerOrder>> lists = DataGenerator.generateOrderListNew(manufacturerOrders);
        List<ManufacturerOrder> orders1 = lists.get(0);
        List<ManufacturerOrder> orders2 = lists.get(1);
        if(orders1.size()>0&&orders2.size()==0){
            List<Task> tasks1 = DataGenerator.generateTaskList(orders1);
            Loader.loadNativeLibraries();
            OrToolsJobApp orToolsJobApp = new OrToolsJobApp();
            orToolsJobApp.setTaskList(tasks1);
            orToolsJobApp.setResourceItems(resourceItems);
            orToolsJobApp.setManufacturerOrders(orders1);
//        orToolsJobApp.calculateHorizon();
//        orToolsJobApp.generateVariables();
//        orToolsJobApp.createConstraints();
////        orToolsJobApp.createRelatedLayerConstraints();
//        orToolsJobApp.createPrecedence();
//        orToolsJobApp.defineObjective();
            List<PhaseOneAssignedTask> phaseOneAssignedTasks = orToolsJobApp.solvePhaseOne();
            if (phaseOneAssignedTasks == null) {
                jsonObject.put("code", 500);
                jsonObject.put("message", "Error in phase 1");
                return jsonObject;
            }

//            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//            def.setName("requestSolutionTx");
//            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//            TransactionStatus status = dataSourceTransactionManager.getTransaction(def);

            List<PhaseTwoAssignedTask> phaseTwoAssignedTasks;
            List<PhaseThreeAssignedTask> phaseThreeAssignedTasks;
            try {
                phaseOneAssignedTaskService.saveOrUpdateBatch(phaseOneAssignedTasks);
//                List<SubPhaseOneTask> subPhaseOneTasks = PhaseOne.splitTask(phaseOneAssignedTasks);
            phaseTwoAssignedTasks = orToolsJobApp.solvePhaseTwo();
            if (phaseTwoAssignedTasks == null) {
                jsonObject.put("code", 500);
                jsonObject.put("message", "Error in phase 2");
                return jsonObject;
            }
            phaseTwoAssignedTaskService.saveOrUpdateBatch(phaseTwoAssignedTasks);
            phaseThreeAssignedTasks = orToolsJobApp.solvePhaseThree();
            if (phaseThreeAssignedTasks == null || phaseThreeAssignedTasks.size() == 0) {
                jsonObject.put("code", 500);
                jsonObject.put("message", "Error in phase 3");
                return jsonObject;
            }
            phaseThreeAssignedTaskService.saveOrUpdateBatch(phaseThreeAssignedTasks);
                List<AssignedTask> assignedTasks = new ArrayList<>();
                assignedTasks.addAll(phaseOneAssignedTasks);
                assignedTasks.addAll(phaseTwoAssignedTasks);
                assignedTasks.addAll(phaseThreeAssignedTasks);
                orToolsJobApp.setFirstAssignedTasks(assignedTasks);
                orToolsJobApp.output(algorithmFileId,startTime);

//                dataSourceTransactionManager.commit(status);
                jsonObject.put("code", 200);
            } catch (Exception e) {
                jsonObject.put("code", 500);
                jsonObject.put("message", e.getMessage());
            }

            return jsonObject;
        }
        else if(orders1.size()>0&&orders2.size()>0){
            List<Task> tasks1 = DataGenerator.generateTaskList(orders1);
            System.out.println("task1");
            tasks1.forEach(i->System.out.println(i.getId()+" orderIndex:"+i.getOrderIndex()+" stepIndex:"+i.getStepIndex()));
            List<Task> tasks2 = DataGenerator.generateTaskList(orders2);
            System.out.println("task1");
            tasks2.forEach(i->System.out.println(i.getId()+" orderIndex:"+i.getOrderIndex()+" stepIndex:"+i.getStepIndex()));

            Loader.loadNativeLibraries();
            OrToolsJobApp orToolsJobApp = new OrToolsJobApp();
            orToolsJobApp.setTaskList(tasks1);
            orToolsJobApp.setTaskList2(tasks2);
            orToolsJobApp.setResourceItems(resourceItems);
            orToolsJobApp.setManufacturerOrders(manufacturerOrders);
//            orToolsJobApp.setManufacturerOrders2(orders2);


            List<PhaseOneAssignedTask> phaseOneAssignedTasks = orToolsJobApp.solvePhaseOne();
            phaseOneAssignedTaskService.saveOrUpdateBatch(phaseOneAssignedTasks);
            List<PhaseOneAssignedTask> phaseOneAssignedTasks2 = orToolsJobApp.solvePhaseOneAnother();
            phaseOneAssignedTaskService.saveOrUpdateBatch(phaseOneAssignedTasks2);
            List<SubPhaseOneTask> subPhaseOneTasks = PhaseOne.splitTask(phaseOneAssignedTasks);
            List<SubPhaseOneTask> subPhaseOneTasks2 = PhaseOneAnother.splitTask(phaseOneAssignedTasks2);



            List<PhaseTwoAssignedTask> phaseTwoAssignedTasks = orToolsJobApp.solvePhaseTwo();
            phaseTwoAssignedTaskService.saveOrUpdateBatch(phaseTwoAssignedTasks);
            List<PhaseTwoAssignedTask> phaseTwoAssignedTasks2 = orToolsJobApp.solvePhaseTwoAnother();
            phaseTwoAssignedTaskService.saveOrUpdateBatch(phaseTwoAssignedTasks2);
            List<SubPhaseTwoTask> subPhaseTwoTasks = PhaseTwo.splitTask(phaseTwoAssignedTasks);
            List<SubPhaseTwoTask> subPhaseTwoTasks2 = PhaseTwoAnother.splitTask(phaseTwoAssignedTasks2);

            List<PhaseThreeAssignedTask> phaseThreeAssignedTasks = orToolsJobApp.solvePhaseThree();
            phaseThreeAssignedTaskService.saveOrUpdateBatch(phaseThreeAssignedTasks);
            List<PhaseThreeAssignedTask> phaseThreeAssignedTasks2 = orToolsJobApp.solvePhaseThreeAnother();
            phaseThreeAssignedTaskService.saveOrUpdateBatch(phaseThreeAssignedTasks2);
            List<SubPhaseThreeTask> subPhaseThreeTasks = PhaseThree.splitTask(phaseThreeAssignedTasks);
            List<SubPhaseThreeTask> subPhaseThreeTasks2 = PhaseThreeAnother.splitTask(phaseThreeAssignedTasks2);

            List<AssignedTask> assignedTasks = new ArrayList<>();
            assignedTasks.addAll(subPhaseOneTasks);
            assignedTasks.addAll(subPhaseOneTasks2);
            assignedTasks.addAll(subPhaseTwoTasks);
            assignedTasks.addAll(subPhaseTwoTasks2);
            assignedTasks.addAll(subPhaseThreeTasks);
            assignedTasks.addAll(subPhaseThreeTasks2);
            orToolsJobApp.setFirstAssignedTasks(assignedTasks);
            assignedTasks.forEach(i->System.out.println(i.getOriginalId() + " "+ i.getSubQuantity()));
            orToolsJobApp.output(algorithmFileId,startTime);
            jsonObject.put("code", 200);
            return jsonObject;

        }
//        List<Task> tasks1 = DataGenerator.generateTaskList(orders1);
//        List<Task> tasks2 = DataGenerator.generateTaskList(orders2);
//
//        Loader.loadNativeLibraries();
//        OrToolsJobApp orToolsJobApp = new OrToolsJobApp();
//        orToolsJobApp.setTaskList(tasks1);
//        orToolsJobApp.setTaskList2(tasks2);
//        orToolsJobApp.setResourceItems(resourceItems);
//        orToolsJobApp.setManufacturerOrders(orders1);
////        orToolsJobApp.calculateHorizon();
////        orToolsJobApp.generateVariables();
////        orToolsJobApp.createConstraints();
//////        orToolsJobApp.createRelatedLayerConstraints();
////        orToolsJobApp.createPrecedence();
////        orToolsJobApp.defineObjective();
//        List<PhaseOneAssignedTask> phaseOneAssignedTasks = orToolsJobApp.solvePhaseOne();
//        if (phaseOneAssignedTasks == null) {
//            jsonObject.put("code", 500);
//            jsonObject.put("message", "Error in phase 1");
//            return jsonObject;
//        }
//
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("requestSolutionTx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = dataSourceTransactionManager.getTransaction(def);
//
//        List<PhaseTwoAssignedTask> phaseTwoAssignedTasks;
//        List<PhaseThreeAssignedTask> phaseThreeAssignedTasks;
//        try {
//            phaseOneAssignedTaskService.saveOrUpdateBatch(phaseOneAssignedTasks);
//            List<SubPhaseOneTask> subPhaseOneTasks = PhaseOne.splitTask(phaseOneAssignedTasks);
////            phaseTwoAssignedTasks = orToolsJobApp.solvePhaseTwo();
////            if (phaseTwoAssignedTasks == null) {
////                jsonObject.put("code", 500);
////                jsonObject.put("message", "Error in phase 2");
////                return jsonObject;
////            }
////            phaseTwoAssignedTaskService.saveOrUpdateBatch(phaseTwoAssignedTasks);
////            phaseThreeAssignedTasks = orToolsJobApp.solvePhaseThree();
////            if (phaseThreeAssignedTasks == null || phaseThreeAssignedTasks.size() == 0) {
////                jsonObject.put("code", 500);
////                jsonObject.put("message", "Error in phase 3");
////                return jsonObject;
////            }
////            phaseThreeAssignedTaskService.saveOrUpdateBatch(phaseThreeAssignedTasks);
//            List<AssignedTask> assignedTasks = new ArrayList<>();
//            assignedTasks.addAll(subPhaseOneTasks);
////            assignedTasks.addAll(phaseTwoAssignedTasks);
////            assignedTasks.addAll(phaseThreeAssignedTasks);
////            orToolsJobApp.setFirstAssignedTasks(assignedTasks);
////            orToolsJobApp.output(algorithmFileId);
//
//            dataSourceTransactionManager.commit(status);
//            jsonObject.put("code", 200);
//        } catch (Exception e) {
//            jsonObject.put("code", 500);
//            jsonObject.put("message", e.getMessage());
//        }
        else{
            jsonObject.put("code", 500);
            jsonObject.put("message", "error");
            return jsonObject;
        }
    }

}
