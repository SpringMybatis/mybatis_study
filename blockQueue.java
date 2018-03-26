package com.pingan.hf.log4j2.queue;

import java.util.List;

public interface TaskService<E> {

    public boolean putTask(E e);

    public E getTask();

    public E peekTask();

    public void initTaskQueue(List<E> es);
    
    public Integer getTaskSize();

}


package com.pingan.hf.log4j2.queue.impl;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.pingan.hf.log4j2.queue.MessageDTO;
import com.pingan.hf.log4j2.queue.TaskService;

public class TaskServiceImpl implements TaskService<MessageDTO> {

    /**
     * 任务队列
     */
    private static Queue<MessageDTO> taskQueue = new LinkedBlockingQueue<MessageDTO>();

    @Override
    public boolean putTask(MessageDTO messageDTO) {
        return taskQueue.offer(messageDTO);
    }

    @Override
    public MessageDTO getTask() {
        return taskQueue.poll();
    }

    @Override
    public MessageDTO peekTask() {
        return taskQueue.peek();
    }

    @Override
    public void initTaskQueue(List<MessageDTO> es) {
        if (null == es || es.isEmpty()) {
            return;
        }
        int size = es.size();
        for (int i = 0; i < size; i++) {
            MessageDTO messageDTO = es.get(i);
            boolean flag = putTask(messageDTO);
            if (!flag) {
                taskQueue.clear();
                throw new RuntimeException("-- init queue exception, queue is full --");
            }
        }
    }

    @Override
    public Integer getTaskSize() {
        return taskQueue.size();
    }

}


package com.pingan.hf.log4j2.queue;

public class MessageDTO {

    private Long id;

    private String from;

    private String to;

    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageDTO() {
        super();
    }

    public MessageDTO(Long id, String from, String to, String content) {
        super();
        this.id = id;
        this.from = from;
        this.to = to;
        this.content = content;
    }

}


package com.pingan.hf.log4j2.queue;

import com.pingan.hf.log4j2.queue.impl.TaskServiceImpl;

public class LinkedQueueTest {

    public static void main(String[] args) {
        TaskService<MessageDTO> taskService = new TaskServiceImpl();
        for (int i = 0; i < 200; i++) {
            MessageDTO message = new MessageDTO();
            message.setId((long) i);

            taskService.putTask(message);
        }

        for (int i = 0; i < 205; i++) {
            MessageDTO task = taskService.getTask();
            if (task == null) {
                System.out.println(i);
            }
        }

    }

}

