package com.example.todolist.service;

public interface TriggerService {

    void transformAsync(Long timestamp);

    /**
     * todo-list 最後一行的 next_created_at
     * @return
     */
    long getLastTimestamp();

    void setLastTimestamp(long timestamp);
}
