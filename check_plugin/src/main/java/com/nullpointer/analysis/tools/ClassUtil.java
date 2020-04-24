package com.nullpointer.analysis.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class工具
 *
 * @author guizhihong
 */
public class ClassUtil {
    public final static String OVERALL_TAG = "[()]";

    private final static int ARGUMENT_INDEX = 1;
    private final static int RETURN_INDEX = 2;

    public final static String ARGUMENTS_TAG = "[L;]";
    public final static String RETURN_TAG = "[L;]";

    public static List<String> parseArguments(String descriptor) {
        Pattern pattern = Pattern.compile(OVERALL_TAG);
        String[] patternList = pattern.split(descriptor);

        List<String> result = new ArrayList<>();
        String[] temp = Pattern.compile(ARGUMENTS_TAG).split(patternList[ARGUMENT_INDEX]);
        if (temp != null) {
            for (int index = 0; index < temp.length; index++) {
                if (temp[index] == null || temp[index].isEmpty()) {
                    continue;
                }

                result.add(temp[index]);
            }
        }

        return result;
    }

    public static String parseReturnType(String descriptor) {

        Pattern pattern = Pattern.compile(OVERALL_TAG);
        String[] patternList = pattern.split(descriptor);

        String[] returnPatternList = Pattern.compile(RETURN_TAG).split(patternList[RETURN_INDEX]);

        return returnPatternList[returnPatternList.length - 1];
    }
}
