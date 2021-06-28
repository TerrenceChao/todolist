package com.example.todolist.config;

import org.springframework.context.annotation.Configuration;


/**
 * 寫一個Config類(+@Config)，裡面創建ThreadPool，會針對這個創建的ThreadPool做一些設定
 * 像是Policy跟worker容量等等
 * 之後在異步方法+@Async(name=你創的ThreadPool名稱)
 */
@Configuration
public class ThreadConfig {

//    private ThreadLocal<> threadPool =
}
