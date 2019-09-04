package cn.devcamp.cloud.oauth.repository;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Repository
public class RedisClientDetailsRepository extends JdbcClientDetailsService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String CLIENT_KEY = "client_details";

    public RedisClientDetailsRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws InvalidClientException {
        ClientDetails clientDetails = null;

        // 先从redis获取
        String value = (String) stringRedisTemplate.boundHashOps(CLIENT_KEY).get(clientId);
        if (StringUtils.isBlank(value)) {
            clientDetails = cacheAndGetClient(clientId);
        } else {
            clientDetails = JSONObject.parseObject(value, BaseClientDetails.class);
        }

        return clientDetails;
    }

    @Override
    public void updateClientDetails(ClientDetails clientDetails) throws NoSuchClientException {
        super.updateClientDetails(clientDetails);
        cacheAndGetClient(clientDetails.getClientId());
    }

    @Override
    public void updateClientSecret(String clientId, String secret) throws NoSuchClientException {
        super.updateClientSecret(clientId, secret);
        cacheAndGetClient(clientId);
    }

    @Override
    public void removeClientDetails(String clientId) throws NoSuchClientException {
        super.removeClientDetails(clientId);
        removeRedisCache(clientId);
    }

    private ClientDetails cacheAndGetClient(String clientId) {
        // 从数据库读取
        ClientDetails clientDetails = super.loadClientByClientId(clientId);
        if (clientDetails != null) {// 写入redis缓存
            stringRedisTemplate.boundHashOps(CLIENT_KEY).put(clientId, JSONObject.toJSONString(clientDetails));
            log.info("缓存clientId:{},{}", clientId, clientDetails);
        }

        return clientDetails;
    }

    private void removeRedisCache(String clientId) {
        stringRedisTemplate.boundHashOps(CLIENT_KEY).delete(clientId);
    }

    /**
     * 从数据库加载所有客户端信息到redis缓存中
     */
    public void loadAllClientToCache() {
        if (stringRedisTemplate.hasKey(CLIENT_KEY)) {
            return;
        }
        log.info("将oauth_client_details全表刷入redis");

        List<ClientDetails> list = super.listClientDetails();
        if (CollectionUtils.isEmpty(list)) {
            log.error("oauth_client_details表数据为空，请检查");
            return;
        }

        list.parallelStream().forEach(client -> stringRedisTemplate.boundHashOps(CLIENT_KEY).put(client.getClientId(), JSONObject.toJSONString(client)));
    }

}
