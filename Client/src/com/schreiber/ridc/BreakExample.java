package com.schreiber.ridc;

public class BreakExample {  
public static void main(String[] args) {  
    
    String i = "Foldername|aa";
    
     String jj[] = i.split("\\|");
     
    System.out.println(jj.length);
    
    if(jj.length == 1 || jj.length == 0 )
        System.out.println(i);  
    
}  
}  