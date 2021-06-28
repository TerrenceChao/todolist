package com.example.todolist.config.db.rmdb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

@Slf4j
public abstract class BaseDataSource extends DataSourceProperties {

    abstract public Integer getSourceId();
    abstract public DataSourceProperties getSource();
}
