package demo.bootstrap;

import demo.domain.Input;
import demo.domain.ManufacturerOrder;
import demo.domain.Step;
import demo.jsonUtils.LoadFile;

import java.util.List;

//做一些更新数据的操作
public class Update {
    public static void main(String[] args) {
//        String algorithmFileInputPath =
//                "D:\\文档\\Idea Projects\\ORTOOLS-APPLICATION\\src\\main\\resources\\json\\test-copy.json";
//        String outputPath = "D:\\文档\\Idea Projects\\ORTOOLS-APPLICATION\\src\\main\\resources\\json\\test-copy-1.json";
//        Input input = LoadFile.readJsonFile(algorithmFileInputPath);
//        List<ManufacturerOrder> manufacturerOrderList = input.getManufacturerOrderList();
//        for(ManufacturerOrder order: manufacturerOrderList){
//            List<Step> stepList = order.getProduct().getStepList();
//            stepList.forEach(i->i.setShiftType("1"));
//        }
//        DataGenerator.writeObjectToFile(input,outputPath);
        int num = 49;
        if (num % 24 == 0) {
            System.out.println(num + "是24的倍数");
        } else {
            int nextMultiple = (num / 24 + 1) * 24;
            System.out.println(num + "不是24的倍数，离他最近的比他大的24的倍数是" + nextMultiple);
        }

    }
    
    

}
