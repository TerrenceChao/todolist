package com.example.todolist.mq.rabbit.consumer;


import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.service.HistoryListService;
import com.example.todolist.service.TriggerService;
import com.example.todolist.util.ByteUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Date;


@Slf4j
@Primary
@Component("transformConsumerA")
public class TransformConsumerA extends BaseConsumer<Long> {

    @Autowired
    private ByteUtil byteUtil;

    @Autowired
    private HistoryListService historyListService;

    @Autowired
    @Qualifier("triggerServiceA")
    private TriggerService triggerService;

    @Value("${todo-task.max}")
    private String maxTask;

    @Override
    protected Long transformMsg(byte[] msgBody, long deliveryTag) throws Exception {
        log.info("A) 觸發轉換機制 (todo-task transfer into todo-list) > transformMsg msgBody: {}, deliveryTag: {}", msgBody, deliveryTag);
        return byteUtil.bytesToLong(msgBody);
    }

    @Override
    protected void businessProcess(Long previousTime, long deliveryTag) throws Exception {

        log.info("A) 觸發轉換機制 (todo-task transfer into todo-list) > \npreviousTime: {}, deliveryTag: {}", previousTime, deliveryTag);

        int limit = Integer.parseInt(maxTask);
        BatchVo vo = historyListService.transform(new Date(previousTime), limit);
        if (! vo.getList().isEmpty()) {
            TodoTaskVo firstOne = (TodoTaskVo) vo.getList().get(0);
            triggerService.setLastTimestamp(firstOne.getCreatedAt().getTime());
        } else {
            log.info("A) 觸發轉換機制 (todo-task transfer into todo-list) > 不滿足 K 個 todo-task，無需轉換  K = {}, deliveryTag: {}", limit, deliveryTag);
        }
    }

//    @RabbitListener(
//            queues = "${mq.transform.queue}",
//            containerFactory = "singleListenerContainer"
//    )
    public void transferFromTaskIntoList(@Payload Message message, Channel channel) throws Exception {
        consumeMessage(message, channel);
    }
}
