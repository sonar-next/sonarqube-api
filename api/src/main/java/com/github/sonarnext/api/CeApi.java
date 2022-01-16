package com.github.sonarnext.api;

import com.github.sonarnext.api.models.CeTask;

import javax.ws.rs.core.Response;

public class CeApi extends AbstractApi {

    public CeApi(SonarApi sonarApi) {
        super(sonarApi);
    }

    public CeTask getCeTask(String id, String additionalFields) throws SonarApiException {

        if (id == null) {
            throw new RuntimeException("ID cannot be null");
        }

        SonarApiForm sonarApiForm = new SonarApiForm();
        sonarApiForm.withParam("additionalFields", additionalFields);
        sonarApiForm.withParam("id", id);
        Response response = get(Response.Status.OK, sonarApiForm.asMap(), "api/ce/task");
        return (response.readEntity(CeTask.class));
    }

}
