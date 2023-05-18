package com.example.demo;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AutometricsAspect {

    @Value("${git.commit.id}")
    private String commitId;

    private final PrometheusMeterRegistry registry;
    private final Gauge gauge;

    public AutometricsAspect(PrometheusMeterRegistry registry, Environment environment) {
        this.registry = registry;
        this.gauge = Gauge.builder("build_info", () -> 1.0)
                .tags("version", environment.getProperty("app.version"))
                .register(registry);
    }

    @Around("@annotation(Autometrics)")
    public Object methodCallCount(ProceedingJoinPoint joinPoint) throws Throwable {
        String function = joinPoint.getSignature().getName();
        String module = joinPoint.getSignature().getDeclaringType().getPackageName();
        String caller = "";
        try {
            Object proceed = joinPoint.proceed();
            registry.counter( "function.calls.count","function", function, "module", module, "result", "ok", "caller", caller).increment();
            return proceed;
        } catch (Throwable throwable) {
            registry.counter("function.calls.count", "function", function, "module", module, "result", "error", "caller", caller).increment();
            throw throwable;
        }
    }

    @Around("@annotation(Autometrics)")
    public Object methodCallDuration(ProceedingJoinPoint joinPoint) {
        String function = joinPoint.getSignature().getName();
        String module = joinPoint.getSignature().getDeclaringType().getPackageName();
        Timer timer = registry.timer("function.calls.duration", "function", function, "module", module);
        return timer.record(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
    }
}