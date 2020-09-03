package com.androidperf.systrace.retrace;

public interface MappingProcessor {
    /**
     * mapping the class name.
     *
     * @param className    the original class name.
     * @param newClassName the new class name.
     * @return whether the processor is interested in receiving mappings of the class members of
     * this class.
     */
    boolean processClassMapping(String className,
                                String newClassName);

    /**
     * mapping the method name.
     *
     * @param className          the original class name.
     * @param methodReturnType   the original external method return type.
     * @param methodName         the original external method name.
     * @param methodArguments    the original external method arguments.
     * @param newClassName       the new class name.
     * @param newMethodName      the new method name.
     */
    void processMethodMapping(String className,
                              String methodReturnType,
                              String methodName,
                              String methodArguments,
                              String newClassName,
                              String newMethodName);
}
