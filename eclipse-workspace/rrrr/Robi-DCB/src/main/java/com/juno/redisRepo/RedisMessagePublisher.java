package com.juno.redisRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.juno.util.CommonHelper;

@Service
public class RedisMessagePublisher implements MessagePublisher {
	private RedisTemplate<String, String> StringredisTemplate;
	
    @Autowired
    private ChannelTopic topic;
    
	@Autowired
	CommonHelper commonHelper;
 
    public RedisMessagePublisher() {
    }
    	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Autowired
    public RedisMessagePublisher(RedisTemplate redisTemplate) {
        this.StringredisTemplate = redisTemplate;
    }
 
    public RedisMessagePublisher(RedisTemplate<String, String> redisTemplate, ChannelTopic topic) {
      this.StringredisTemplate = redisTemplate;
      this.topic = topic;
    }
 
    @Override
    public void publish(String message) {
        StringredisTemplate.convertAndSend(topic.getTopic(), message);
        //System.out.println("inside queue publish , message to queue : "+message+" publish-time : "+commonHelper.getDate());
    }
}