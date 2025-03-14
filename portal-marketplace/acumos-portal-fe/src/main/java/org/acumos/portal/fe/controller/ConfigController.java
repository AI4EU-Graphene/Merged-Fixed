package org.acumos.portal.fe.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acumos.portal.fe.AcumosPortalApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

import static org.acumos.portal.fe.AcumosPortalApplication.CONFIG_ENV_VAR_NAME;

@RestController
@RequestMapping("/frontend")
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    private String uiSystemProperties;

    @GetMapping(path = "/ui-system-config.js")
    public String getUISystemConfig() {

        String springApplicationJson = System.getenv(CONFIG_ENV_VAR_NAME);
        if(uiSystemProperties == null) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                JsonNode uiSystemPropertiesNode = mapper.readTree(springApplicationJson).get("ui_system_config");
                uiSystemProperties = "window.ui_system_config="+mapper.writeValueAsString(uiSystemPropertiesNode);
                logger.info("successfully read configuration from environment {}", uiSystemProperties);
            } catch (Exception x) {
                logger.warn("no configuration found in environment {}", CONFIG_ENV_VAR_NAME);
                uiSystemProperties = "{\"error\": \"could not read application properties\"}";
            }
        }
        return uiSystemProperties;
    }
}