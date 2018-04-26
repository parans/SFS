package com.company;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main (String[] args) throws java.lang.Exception
    {
        // your code goes here
        Map<String, String> testMap = new ConcurrentHashMap<String, String>();

        testMap.put("shata", "Teri");
        testMap.put("Laude", "KeBaal");
        testMap.remove("shata");
        //String item = null;
        //try {
            String item = testMap.get("shata");
            if(item == null) {
                System.out.println("Its null bro");
            }
       // } catch(Exception e) {
         //   System.out.println(e.getMessage());
        //}
        System.out.println(item);
    }

}
