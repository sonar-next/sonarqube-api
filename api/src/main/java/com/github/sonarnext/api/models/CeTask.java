package com.github.sonarnext.api.models;

public class CeTask {

    private Task task;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public static class Task {
        private String organization;
        private String id;
        private String type;
        private String componentId;
        private String componentKey;
        private String componentName;
        private String componentQualifier;
        private String analysisId;
        private String status;
        private String submittedAt;
        private String startedAt;
        private String executedAt;
        private Long executionTimeMs;
        private String errorMessage;
        private Boolean logs;
        private Boolean hasErrorStacktrace;
        private String errorStacktrace;
        private String scannerContext;
        private Boolean hasScannerContext;

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getComponentId() {
            return componentId;
        }

        public void setComponentId(String componentId) {
            this.componentId = componentId;
        }

        public String getComponentKey() {
            return componentKey;
        }

        public void setComponentKey(String componentKey) {
            this.componentKey = componentKey;
        }

        public String getComponentName() {
            return componentName;
        }

        public void setComponentName(String componentName) {
            this.componentName = componentName;
        }

        public String getComponentQualifier() {
            return componentQualifier;
        }

        public void setComponentQualifier(String componentQualifier) {
            this.componentQualifier = componentQualifier;
        }

        public String getAnalysisId() {
            return analysisId;
        }

        public void setAnalysisId(String analysisId) {
            this.analysisId = analysisId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(String submittedAt) {
            this.submittedAt = submittedAt;
        }

        public String getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(String startedAt) {
            this.startedAt = startedAt;
        }

        public String getExecutedAt() {
            return executedAt;
        }

        public void setExecutedAt(String executedAt) {
            this.executedAt = executedAt;
        }

        public Long getExecutionTimeMs() {
            return executionTimeMs;
        }

        public void setExecutionTimeMs(Long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Boolean getLogs() {
            return logs;
        }

        public void setLogs(Boolean logs) {
            this.logs = logs;
        }

        public Boolean getHasErrorStacktrace() {
            return hasErrorStacktrace;
        }

        public void setHasErrorStacktrace(Boolean hasErrorStacktrace) {
            this.hasErrorStacktrace = hasErrorStacktrace;
        }

        public String getErrorStacktrace() {
            return errorStacktrace;
        }

        public void setErrorStacktrace(String errorStacktrace) {
            this.errorStacktrace = errorStacktrace;
        }

        public String getScannerContext() {
            return scannerContext;
        }

        public void setScannerContext(String scannerContext) {
            this.scannerContext = scannerContext;
        }

        public Boolean getHasScannerContext() {
            return hasScannerContext;
        }

        public void setHasScannerContext(Boolean hasScannerContext) {
            this.hasScannerContext = hasScannerContext;
        }
    }
}
