package com.commercium.inventory.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisStockLockManager {

    private final RedissonClient redissonClient;

    /**
     * 분산 락 획득 시도
     */
    public boolean tryLock(String lockKey, Duration timeout) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(timeout.toSeconds(), 30, TimeUnit.SECONDS);

            if (acquired) {
                log.debug("분산 락 획득 성공: lockKey={}", lockKey);
            } else {
                log.warn("분산 락 획득 실패: lockKey={}, timeout={}초", lockKey, timeout.toSeconds());
            }

            return acquired;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("분산 락 획득 중단됨: lockKey={}", lockKey, e);
            return false;
        }
    }

    /**
     * 분산 락 해제
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("분산 락 해제 성공: lockKey={}", lockKey);
            } else {
                log.warn("현재 스레드가 소유하지 않은 락 해제 시도: lockKey={}", lockKey);
            }
        } catch (Exception e) {
            log.error("분산 락 해제 실패: lockKey={}", lockKey, e);
        }
    }

    /**
     * Lua 스크립트를 사용한 원자적 재고 차감 (대안 방식)
     */
    public boolean decreaseStockAtomic(String productId, Integer quantity) {
        String script =
                "local current = tonumber(redis.call('GET', KEYS[1]) or 0) " +
                        "if current >= tonumber(ARGV[1]) then " +
                        "    redis.call('DECRBY', KEYS[1], ARGV[1]) " +
                        "    return 1 " +
                        "else " +
                        "    return 0 " +
                        "end";

        String stockKey = "stock:available:" + productId;

        try {
            Object result = redissonClient.getScript().eval(
                    org.redisson.client.codec.LongCodec.INSTANCE,
                    script,
                    org.redisson.api.RScript.ReturnType.INTEGER,
                    java.util.List.of(stockKey),
                    quantity
            );

            boolean success = Integer.valueOf(1).equals(result);
            log.debug("원자적 재고 차감: productId={}, quantity={}, success={}",
                    productId, quantity, success);

            return success;

        } catch (Exception e) {
            log.error("원자적 재고 차감 실패: productId={}", productId, e);
            return false;
        }
    }
}
