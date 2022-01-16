package com.github.sonarnext.sonarqube.api.example;

import com.github.sonarnext.api.CeApi;
import com.github.sonarnext.api.SonarApi;
import com.github.sonarnext.api.SonarApiException;

public class CeApiExample {

    public static void main(String[] args) throws SonarApiException {
        try {
            SonarApi sonarApi = new SonarApi("https://sonarcloud.io", "", null);
            sonarApi.getCeApi().getCeTask("111", "componentKey");
        } catch (SonarApiException e) {
        }


    }
}
