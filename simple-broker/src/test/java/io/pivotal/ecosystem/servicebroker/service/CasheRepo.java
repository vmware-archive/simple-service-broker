package io.pivotal.ecosystem.servicebroker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CasheRepo {

    @Autowired
    private RedisTemplate<String, Object> template;

    public Object getObject(final String key) {
        return template.opsForValue().get(key);
    }
}
