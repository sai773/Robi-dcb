package com.juno.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.juno.redisRepo.PurchaseDetailValidationRepositoryImpl;
import com.juno.redisRepo.RedisMessageSubscriber;

@Configuration
public class RedisConfiguration {

	@Value("${redis.host.name}")
	String redisHost;
	
	@Value("${redis.host.port}")
	int redisPort;
	
	@Value("${CdrSessionTimeout}")
	int sessiontimer;
	
	/*@Value("${redis.host.cred}")
	String redisCred;*/
	
	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
	    JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
	    jedisConFactory.setHostName(redisHost);
	    jedisConFactory.setPort(redisPort);
	    //jedisConFactory.setPassword(redisCred);
	    return jedisConFactory;
	}
	
	@Bean
	StringRedisSerializer stringRedisSerializer() {
		return new StringRedisSerializer();
	}
	
	@Bean
	public PurchaseDetailValidationRepositoryImpl purchaseDetailValRepo() {
		return new PurchaseDetailValidationRepositoryImpl(redisTemplate());
	}
	
	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
	    RedisTemplate<String, Object> template = new RedisTemplate<>();
	    template.setConnectionFactory(jedisConnectionFactory());
	    template.setKeySerializer(stringRedisSerializer());
	    return template;
	}
	
	@Bean
	public RedisTemplate<String, Integer> sessionTemplate() {
	    RedisTemplate<String, Integer> template = new RedisTemplate<>();
	    template.setConnectionFactory(jedisConnectionFactory());
	    template.setKeySerializer(stringRedisSerializer());
	    return template;
	}
	
	//Pub/Sub Configuration
	@Bean
    ChannelTopic topic() {
        return new ChannelTopic("asRobi-Queue");
    }

    @Autowired
    RedisMessageSubscriber redisMessageSubscriber;

    @Bean
    MessageListenerAdapter messageListener( ) {
        return new MessageListenerAdapter(redisMessageSubscriber);
    }

    @Bean
    RedisMessageListenerContainer redisContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(jedisConnectionFactory());
        container.addMessageListener(messageListener(), topic());
        container.setMaxSubscriptionRegistrationWaitingTime(sessiontimer * 60 * 1000);
        return container;
    }	
}
