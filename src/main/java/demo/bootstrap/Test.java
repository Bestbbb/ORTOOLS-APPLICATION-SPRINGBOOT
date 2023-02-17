package demo.bootstrap;

import demo.domain.SubPhaseOneTask;
import org.springframework.beans.BeanUtils;

import java.time.Duration;

public class Test {
    public static void main(String[] args) {


       int x =  (int) (Math.floorDiv(15 * 3, 4));
       System.out.println(x);
        int start = 8;
        int end = 24;
        int duration =17;
        int remainderStart = start%16;
        int epochStart = start/16;
        int remainderEnd = end%16;
        int epochEnd = end/16;
        end = 24*epochEnd+remainderEnd;
        start = 24*epochStart+remainderStart;
        System.out.println("real start"+start);
        System.out.println("real end"+end);
        int re = end -start;
        int l = re - duration;
        for(int i =0;i<l+1;i++){
            //new something
        }
        int start_ = 5;
        int end_ = 32;
        int duration_ = 27;
        int remainderStart_ = start_%8;
        int epochStart_ = start_/8;
        int remainderEnd_ = end_%8;
        int epochEnd_ = end_/8;
        int realEnd = 24*epochEnd_+remainderEnd_+16;
        int realStart = 24*epochStart_+remainderStart_+16;
        System.out.println("real start"+realStart);
        System.out.println("real end"+realEnd);
        int re2 = realEnd-realStart;
        int l2 = re2-duration_;
        int e = l2/16;
        for(int i =0;i<e+1;i++){
            int tempStart = 0;
            int tempEnd = 0;
            if(i==0){
                tempStart = realStart;
                tempEnd = 24*(epochStart_+1);
                System.out.println("tempStart"+tempStart);
                System.out.println("tempEnd"+tempEnd);

            }else if(i==e){
                tempStart = 24*(epochEnd_)+16;
                tempEnd = realEnd;
                if(tempStart!=tempEnd){
                    System.out.println("tempStart"+tempStart);
                    System.out.println("tempEnd"+tempEnd);
                }
            }else{
                tempStart = 24*(epochStart_+i)+16;
                tempEnd = 24*(epochStart_+i+1);
                System.out.println("tempStart"+tempStart);
                System.out.println("tempEnd"+tempEnd);

            }
        }
    }
}
