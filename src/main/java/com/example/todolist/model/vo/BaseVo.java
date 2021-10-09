package com.example.todolist.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class BaseVo implements Serializable {

//    public <S, T> T clone(S sourceObj, T targetObj) throws IllegalAccessException {
//        if (Objects.isNull(sourceObj)) {
//            return null;
//        }
//
//        for (Field field : sourceObj.getClass().getFields()) {
//            Object value = field.get(sourceObj);
//            field.set(targetObj, value);
//        }
//
//        return targetObj;
//    }
}
