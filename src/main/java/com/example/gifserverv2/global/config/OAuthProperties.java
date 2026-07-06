package com.example.gifserverv2.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {
    private Datagsm datagsm = new Datagsm();
    private Google google = new Google();

    public Datagsm getDatagsm() { return datagsm; }
    public void setDatagsm(Datagsm datagsm) { this.datagsm = datagsm; }

    public Google getGoogle() { return google; }
    public void setGoogle(Google google) { this.google = google; }

    public static class Datagsm {
        private String redirectUris = "";
        public String getRedirectUris() { return redirectUris; }
        public void setRedirectUris(String redirectUris) { this.redirectUris = redirectUris; }
    }

    public static class Google {
        private String redirectUris = "";
        public String getRedirectUris() { return redirectUris; }
        public void setRedirectUris(String redirectUris) { this.redirectUris = redirectUris; }
    }
}
