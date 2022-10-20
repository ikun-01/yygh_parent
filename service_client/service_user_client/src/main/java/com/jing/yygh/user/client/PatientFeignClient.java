package com.jing.yygh.user.client;

import com.jing.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-user")
public interface PatientFeignClient {
    @GetMapping("/api/user/patient/inner/get/{id}")
    public Patient getPatientById(@PathVariable("id") Long id);
}
