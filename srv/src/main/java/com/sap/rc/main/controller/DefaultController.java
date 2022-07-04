package com.sap.rc.main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/")
@RestController
public class DefaultController {

	 @GetMapping("/check")
	 // http://localhost:8080/v4/api/v1/check
	 // http://localhost:8080/v4/odata/RCService
    public String get() {
        return "Default controller works fine. oData service is provided through /odata/RCService/." ;
    }
}
