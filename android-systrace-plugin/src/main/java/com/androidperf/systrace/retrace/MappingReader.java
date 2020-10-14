package com.androidperf.systrace.retrace;

import com.androidperf.systrace.tools.LogTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class MappingReader {
    private final static String TAG = "MappingReader";
    private final static String SPLIT = ":";
    private final static String SPACE = " ";
    private final static String ARROW = "->";
    private final static String LEFT_PUNC = "(";
    private final static String RIGHT_PUNC = ")";
    private final static String DOT = ".";
    private final File proguardMappingFile;

    public MappingReader(File proguardMappingFile) {
        this.proguardMappingFile = proguardMappingFile;
    }

    public void read(MappingProcessor mappingProcessor) throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(proguardMappingFile)))) {
            String className = null;
            // Read the class and class member mappings.
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (!line.startsWith("#")) {
                    // a class mapping
                    if (line.endsWith(SPLIT)) {
                        className = parseClassMapping(line, mappingProcessor);
                    } else if (className != null) { // a class member mapping
                        parseClassMemberMapping(className, line, mappingProcessor);
                    }
                } else {
                    LogTools.i(TAG, "comment:# %s", line);
                }
            }
        } catch (IOException err) {
            throw new IOException("Can't read mapping file", err);
        }
        // do nothing
    }

    /**
     * @param line             read content
     * @param mappingProcessor
     * @return
     */
    private String parseClassMapping(String line, MappingProcessor mappingProcessor) {

        int leftIndex = line.indexOf(ARROW);
        if (leftIndex < 0) {
            return null;
        }
        int offset = 2;
        int rightIndex = line.indexOf(SPLIT, leftIndex + offset);
        if (rightIndex < 0) {
            return null;
        }

        // trim the elements.
        String className = line.substring(0, leftIndex).trim();
        String newClassName = line.substring(leftIndex + offset, rightIndex).trim();

        // Process this class name mapping.
        boolean ret = mappingProcessor.processClassMapping(className, newClassName);

        return ret ? className : null;
    }

    /**
     * Parses the a class member mapping
     *
     * @param className
     * @param line
     * @param mappingProcessor parse line such as
     *                         ___ ___ -> ___
     *                         ___:___:___ ___(___) -> ___
     *                         ___:___:___ ___(___):___ -> ___
     *                         ___:___:___ ___(___):___:___ -> ___
     */
    private void parseClassMemberMapping(String className, String line, MappingProcessor mappingProcessor) {
        int leftIndex1 = line.indexOf(SPLIT);
        int leftIndex2 = leftIndex1 < 0 ? -1 : line.indexOf(SPLIT, leftIndex1 + 1);
        int spaceIndex = line.indexOf(SPACE, leftIndex2 + 2);
        int argIndex1 = line.indexOf(LEFT_PUNC, spaceIndex + 1);
        int argIndex2 = argIndex1 < 0 ? -1 : line.indexOf(RIGHT_PUNC, argIndex1 + 1);
        int leftIndex3 = argIndex2 < 0 ? -1 : line.indexOf(SPLIT, argIndex2 + 1);
        int leftIndex4 = leftIndex3 < 0 ? -1 : line.indexOf(SPLIT, leftIndex3 + 1);
        int rightIndex = line.indexOf(ARROW, (leftIndex4 >= 0 ? leftIndex4 : leftIndex3 >= 0
                ? leftIndex3 : argIndex2 >= 0 ? argIndex2 : spaceIndex) + 1);
        if (spaceIndex < 0 || rightIndex < 0) {
            return;
        }

        // trim the elements.
        String type = line.substring(leftIndex2 + 1, spaceIndex).trim();
        String name = line.substring(spaceIndex + 1, argIndex1 >= 0 ? argIndex1 : rightIndex).trim();
        String newName = line.substring(rightIndex + 2).trim();

        String newClassName = className;
        int dotIndex = name.lastIndexOf(DOT);
        if (dotIndex >= 0) {
            className = name.substring(0, dotIndex);
            name = name.substring(dotIndex + 1);
        }

        // parse class member mapping.
        if (type.length() > 0 && name.length() > 0 && newName.length() > 0 && argIndex2 >= 0) {
            String arguments = line.substring(argIndex1 + 1, argIndex2).trim();
            mappingProcessor.processMethodMapping(className, type, name, arguments, newClassName, newName);
        }
    }
}
