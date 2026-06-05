package com.androidperf.systrace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TraceFixExtension {
    public boolean enabled = true;
    public List<String> includedVariants = new ArrayList<>();
    public List<String> excludedVariants = new ArrayList<>();
    public List<String> includedClassPrefixes = new ArrayList<>();
    public List<String> excludedClassPrefixes = new ArrayList<>();
    public boolean traceConstructors = true;
    public boolean traceClassInitializers = true;
    public boolean traceSyntheticMethods = false;
    public boolean traceBridgeMethods = false;

    public boolean isVariantEnabled(String variantName) {
        if (!enabled) {
            return false;
        }
        String normalizedVariant = normalize(variantName);
        if (!includedVariants.isEmpty() && !containsVariant(includedVariants, normalizedVariant)) {
            return false;
        }
        return !containsVariant(excludedVariants, normalizedVariant);
    }

    private static boolean containsVariant(List<String> variants, String normalizedVariant) {
        for (String variant : variants) {
            if (normalize(variant).equals(normalizedVariant)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }
}
