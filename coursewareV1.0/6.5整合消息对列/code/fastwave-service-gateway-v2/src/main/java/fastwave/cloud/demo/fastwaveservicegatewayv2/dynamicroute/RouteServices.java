package fastwave.cloud.demo.fastwaveservicegatewayv2.dynamicroute;

import com.alibaba.fastjson.JSON;
import fastwave.cloud.demo.fastwavelibcommon.R;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

@Service
public class RouteServices implements ApplicationEventPublisherAware {

    @Resource
    private RouteDefinitionWriter routeDefinitionWriter;
    private ApplicationEventPublisher publisher;

    //public static final String KEY_GATEWAY_ROUTES = "KEY_GATEWAY_ROUTES";

    @Resource
    StringRedisTemplate stringRedisTemplate;

    //增加路由
    public R add(RouteDefinition definition) {
        stringRedisTemplate.opsForHash().put(RedisRepository.KEY_GATEWAY_ROUTES, definition.getId(), JSON.toJSONString(definition));

        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        return R.ok("添加动态路由成功");
    }
    //更新路由
    public R update(RouteDefinition definition) {
        stringRedisTemplate.opsForHash().put(RedisRepository.KEY_GATEWAY_ROUTES, definition.getId(), JSON.toJSONString(definition));
        try {
            this.routeDefinitionWriter.delete(Mono.just(definition.getId()));
        } catch (Exception e) {
            return R.error("更新动态路由失败，routeId: "+definition.getId());
        }
        try {
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            this.publisher.publishEvent(new RefreshRoutesEvent(this));
            return R.error("更新动态路由成功");
        } catch (Exception e) {
            return R.error("更新动态路由失败");
        }
    }
    //删除路由
    public Mono<ResponseEntity<Object>> delete(String id) {
        stringRedisTemplate.opsForHash().delete(RedisRepository.KEY_GATEWAY_ROUTES, id);
        return this.routeDefinitionWriter.delete(Mono.just(id))
                .then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
                .onErrorResume(t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));
    }
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
