package com.androidperf.systrace.retrace;

public class MethodInfo {

    private final String originalClassName;

    public String originalType;
    public String originalArguments;
    public String originalName;
    public String desc;

    public MethodInfo(String originalClassName,
                      String originalType,
                      String originalName,
                      String originalArguments) {

        this.originalType = originalType;
        this.originalArguments = originalArguments;
        this.originalClassName = originalClassName;
        this.originalName = originalName;
    }

    public MethodInfo(MethodInfo methodInfo) {
        this.originalType = methodInfo.getOriginalType();
        this.originalArguments = methodInfo.getOriginalArguments();
        this.originalClassName = methodInfo.getOriginalClassName();
        this.originalName = methodInfo.getOriginalName();
        this.desc = methodInfo.getDesc();
    }

    public static MethodInfo deFault() {
        return new MethodInfo("", "", "", "");
    }

    public boolean matches(String originalType, String originalArguments) {
        boolean bool = (originalType == null || originalType.equals(this.originalType))
                && (originalArguments == null || originalArguments.equals(this.originalArguments));
        return bool;
    }

    public String getOriginalClassName() {
        return originalClassName;
    }

    public String getOriginalType() {
        return originalType;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getOriginalArguments() {
        return originalArguments;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void setOriginalArguments(String originalArguments) {
        this.originalArguments = originalArguments;
    }

    public void setOriginalType(String originalType) {
        this.originalType = originalType;
    }
}
