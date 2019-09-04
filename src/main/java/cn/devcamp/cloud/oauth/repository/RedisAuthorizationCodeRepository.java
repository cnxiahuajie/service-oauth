package cn.devcamp.cloud.oauth.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RedisAuthorizationCodeRepository extends RandomValueAuthorizationCodeServices {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        redisTemplate.execute((RedisConnection connection) -> {
            connection.set(codeKey(code).getBytes(), SerializationUtils.serialize(authentication),
                    Expiration.from(10, TimeUnit.MINUTES), RedisStringCommands.SetOption.UPSERT);
            return 1L;
        });
    }

    @Override
    protected OAuth2Authentication remove(String code) {
        OAuth2Authentication oAuth2Authentication = redisTemplate.execute((RedisConnection connection) -> {
            byte[] keyByte = codeKey(code).getBytes();
            byte[] valueByte = connection.get(keyByte);

            if (valueByte != null) {
                connection.del(keyByte);
                return SerializationUtils.deserialize(valueByte);
            }
            return null;
        });
        return oAuth2Authentication;
    }

    private String codeKey(String code) {
        return "oauth2:codes:" + code;
    }

}
