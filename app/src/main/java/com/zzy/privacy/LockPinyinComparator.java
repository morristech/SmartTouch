package com.zzy.privacy;

import java.util.Comparator;

import com.zzy.privacy.PrivacyAdpter.LockAppSt;
import com.zzy.smarttouch.MainClickAdpter.ProgrameSt;


public class LockPinyinComparator implements Comparator<LockAppSt> {  
	  
    public int compare(LockAppSt o1, LockAppSt o2) {  
        //这里主要是用来对ListView里面的数据根据ABCDEFG...来排序  
        if (o2.sortLetters.equals("#")) {  
            return -1;  
        } else if (o1.sortLetters.equals("#")) {  
            return 1;  
        } else {  
            return o1.sortLetters.compareTo(o2.sortLetters);  
        }  
    }  
}  
