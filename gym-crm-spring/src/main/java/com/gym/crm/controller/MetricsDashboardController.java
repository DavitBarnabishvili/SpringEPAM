package com.gym.crm.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.TimeUnit;

@Controller
@Profile({"local", "dev"})
public class MetricsDashboardController {

    private final MeterRegistry meterRegistry;

    public MetricsDashboardController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/metrics-dashboard")
    public String metricsDashboard(Model model) {
        double traineeRegistrations = getCounterValue("gym.trainee.registrations");
        double trainerRegistrations = getCounterValue("gym.trainer.registrations");
        double trainingsCreated = getCounterValue("gym.training.created");
        double loginAttempts = getCounterValue("gym.login.attempts");
        double loginSuccess = getCounterValue("gym.login.success");
        double authTime = getTimerValue();

        double successRate = loginAttempts > 0 ? (loginSuccess / loginAttempts * 100) : 0;

        double memoryUsed = getGaugeValue("jvm.memory.used") / (1024 * 1024);
        double memoryMax = getGaugeValue("jvm.memory.max") / (1024 * 1024);
        double cpuUsage = getGaugeValue("process.cpu.usage") * 100;

        model.addAttribute("traineeRegistrations", traineeRegistrations);
        model.addAttribute("trainerRegistrations", trainerRegistrations);
        model.addAttribute("trainingsCreated", trainingsCreated);
        model.addAttribute("loginAttempts", loginAttempts);
        model.addAttribute("loginSuccess", loginSuccess);
        model.addAttribute("loginSuccessRate", successRate);
        model.addAttribute("authTime", authTime);
        model.addAttribute("memoryUsed", memoryUsed);
        model.addAttribute("memoryMax", memoryMax);
        model.addAttribute("cpuUsage", cpuUsage);
        model.addAttribute("timestamp", new java.util.Date());

        return "metrics-dashboard";
    }

    private double getCounterValue(String name) {
        Counter counter = meterRegistry.find(name).counter();
        return counter != null ? counter.count() : 0.0;
    }

    private double getGaugeValue(String name) {
        Gauge gauge = meterRegistry.find(name).gauge();
        return gauge != null ? gauge.value() : 0.0;
    }

    private double getTimerValue() {
        Timer timer = meterRegistry.find("gym.authentication.time").timer();
        return timer != null ? timer.mean(TimeUnit.MILLISECONDS) : 0.0;
    }
}
