package com.example.todolist.db.rmdb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.todolist.db.rmdb.entity.Course;
import org.apache.ibatis.annotations.Select;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
//@MapperScan("com.example.todolist.db.rmdb.mapper")
public interface CourseMapper extends BaseMapper<Course> {

//    @Select("select * from course;")
//    List getList();
}
