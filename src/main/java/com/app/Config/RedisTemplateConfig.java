package com.app.Config;

import com.app.Model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisTemplateConfig {

  private <T> RedisTemplate<String, T> createRedisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, T> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new JdkSerializationRedisSerializer());
    return template;
  }

  @Bean
  @Primary
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    return createRedisTemplate(connectionFactory);
  }

  @Bean
  public RedisTemplate<String, Comment> commentRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    return createRedisTemplate(connectionFactory);
  }

  @Bean
  public RedisTemplate<String, SubComment> subCommentRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    return createRedisTemplate(connectionFactory);
  }

  @Bean
  public RedisTemplate<String, Pin> pinRedisTemplate(RedisConnectionFactory connectionFactory) {
    return createRedisTemplate(connectionFactory);
  }

  @Bean
  public RedisTemplate<String, Board> boardRedisTemplate(RedisConnectionFactory connectionFactory) {
    return createRedisTemplate(connectionFactory);
  }

  @Bean
  public RedisTemplate<String, Media> mediaRedisTemplate(RedisConnectionFactory connectionFactory) {
    return createRedisTemplate(connectionFactory);
  }
}
