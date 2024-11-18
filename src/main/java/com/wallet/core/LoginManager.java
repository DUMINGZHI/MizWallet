package com.wallet.core;

import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class LoginManager {
    // 存储登录信息 (wid -> 上次心跳时间)
    private final Map<String, Long> heartMap = new ConcurrentHashMap<>();

    // 存储用户登录信息 (wid -> 钱包连接)
    private final Map<String, Credentials> credentialsMap = new ConcurrentHashMap<>();


    // 登录方法
    public String login(Credentials value) {
        String wid = UUID.randomUUID().toString(); // 生成唯一 wid
        heartMap.put(wid, System.currentTimeMillis());
        credentialsMap.put(wid, value);
        return wid;
    }

    // 获取钱包凭证方法
    public Credentials get(String wid) {
        return credentialsMap.get(wid);
    }

    // 心跳更新
    public boolean updateHeartbeat(String wid) {
        if (heartMap.containsKey(wid)) {
            heartMap.put(wid, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    // 检查登录状态
    public boolean isLoggedIn(String wid) {
        return heartMap.containsKey(wid);
    }

    // 定时清理超时登录信息
    public void cleanup() {
        long now = System.currentTimeMillis();
        heartMap.entrySet().removeIf(entry -> now - entry.getValue() > 10 * 60 * 1000); // 超过 10 分钟
        credentialsMap.entrySet().removeIf(entry -> !heartMap.containsKey(entry.getKey())); // 清除钱包连接
    }

    // 定时任务启动
    @PostConstruct
    public void startCleanupTask() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
    }
}
