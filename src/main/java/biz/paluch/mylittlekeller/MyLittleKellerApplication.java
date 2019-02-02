/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.paluch.mylittlekeller;

import biz.paluch.mylittlekeller.descriptor.CodecheckClient;
import biz.paluch.mylittlekeller.descriptor.ItemDescriptorClient;
import biz.paluch.mylittlekeller.descriptor.OpengtindbClient;
import biz.paluch.mylittlekeller.descriptor.RedisCachingItemDescriptorClient;
import biz.paluch.mylittlekeller.events.EventProcessor;
import biz.paluch.mylittlekeller.usecases.Constants;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * Web application to manage grocery stocks.
 */
@SpringBootApplication
@EnableScheduling
public class MyLittleKellerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyLittleKellerApplication.class, args);
	}

	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {

		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setDefaultSerializer(StringRedisSerializer.UTF_8);

		return redisTemplate;
	}

	@Bean
	GenericJackson2JsonRedisSerializer serializer() {

		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, "_class");
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		mapper.registerModule(new JavaTimeModule());
		mapper.registerModule(new ParameterNamesModule());

		return new GenericJackson2JsonRedisSerializer(mapper);
	}

	@Bean
	@Qualifier("jsonRedisTemplate")
	public RedisTemplate<String, Object> jsonRedisTemplate(GenericJackson2JsonRedisSerializer json,
			RedisConnectionFactory connectionFactory) {

		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(StringRedisSerializer.UTF_8);
		redisTemplate.setValueSerializer(json);
		redisTemplate.setHashKeySerializer(StringRedisSerializer.UTF_8);
		redisTemplate.setHashValueSerializer(json);

		return redisTemplate;
	}

	@Bean
	public ItemDescriptorClient articleDescirptorClient(RestTemplateBuilder restTemplateBuilder,
			RedisTemplate<String, String> redisTemplate) {
		return new RedisCachingItemDescriptorClient(redisTemplate, new OpengtindbClient(restTemplateBuilder),
				new CodecheckClient(restTemplateBuilder));
	}

	@Bean
	public RedisMessageListenerContainer messageListenerContainer(RedisConnectionFactory connectionFactory) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.start();
		return container;
	}

	@Bean
	public MessageListenerAdapter messageListenerAdapter(GenericJackson2JsonRedisSerializer json,
			RedisMessageListenerContainer container, EventProcessor eventProcessor) {

		MessageListenerAdapter adapter = new MessageListenerAdapter(eventProcessor);
		adapter.setSerializer(json);

		container.addMessageListener(adapter, new ChannelTopic(Constants.WAREHOUSE_CHANNEL));

		return adapter;
	}
}
