package com.example.todolist;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.todolist.db.rmdb.entity.Course;
import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.entity.User;
import com.example.todolist.db.rmdb.mapper.CourseMapper;
import com.example.todolist.db.rmdb.mapper.TodoTaskMapper;
import com.example.todolist.db.rmdb.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
class TodolistApplicationTests {

    @Autowired
    private TodoTaskMapper todoTaskMapper;

    /**
     * test sharding
     */
    @Test
    void addTodoTaskDb() {
        int cnt = 20;
        long start = System.currentTimeMillis();
        while (cnt-- > 0) {
            Long t = System.currentTimeMillis() % 1000;
            TodoTask task = new TodoTask()
                    .setTitle("todo " + cnt)
                    .setContent(UUID.randomUUID().toString())
                    .setWeekOfYear(t.intValue())
                    .setCreatedAt(new Date());
            todoTaskMapper.insert(task);

            // TODO 透過這方式來找到 tid !!
            log.info("來看看 SNOWFLAKE, tid: {}", task.getTid());
        }
        long cost = System.currentTimeMillis() - start;
        System.out.println(cost + " ms");
    }

    @Test
    void findOneTodoTaskDb() {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
        wrapper.eq("tid", 616029305360613376L);
        wrapper.eq("week_of_year", 668);
        List<TodoTask> tasks = todoTaskMapper.selectList(wrapper);
        tasks.forEach(t -> log.info("就一筆清單! {}", t));
    }

    @Test
    void findTodoTasksDb() {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
        wrapper.ge("tid", 616029305360613376L);
        wrapper.last(" limit 3");
        List<TodoTask> tasks = todoTaskMapper.selectList(wrapper);
        log.info("清單列表吧! {}", tasks.toString());
    }


    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    public void addUserDb() {
        User user = new User()
                .setUsername("lucy")
                .setUstatus("a");
        userMapper.insert(user);
    }

    @Test
    void findUserDb() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", 615588507267629057L);
        User user = userMapper.selectOne(wrapper);
        System.out.println(user);
    }


    /**
     * test sharding
     */
    @Test
    void addCourseDb() {
        int cnt = 20;
        while (cnt-- > 0) {
            Long t = System.currentTimeMillis();
            Course course = new Course()
                    .setCname("java demo " + cnt)
                    .setUserId(100L + t % 10)
                    .setCstatus("Sharding");
            courseMapper.insert(course);
        }
    }

    @Test
    void findCourseDb() {
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        wrapper.ge("cid", 615697769641803777L);
        wrapper.last(" limit 3");
        List<Course> courses = courseMapper.selectList(wrapper);
        courses.forEach(c -> log.info("來吧! {}", c));
    }




    /**
     * test partition
     */
    @Test
    void addCourse() {
        int cnt = 10;
        while (cnt-- > 0) {
            Course course = new Course()
                    .setCname("java " + cnt)
                    .setUserId(100L)
                    .setCstatus("Normal " + cnt);
            courseMapper.insert(course);
        }
    }

    @Test
    void findCourse() {
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        wrapper.eq("cid", 614958500048535553L);
        Course course = courseMapper.selectOne(wrapper);
        System.out.println(course);
    }
}
