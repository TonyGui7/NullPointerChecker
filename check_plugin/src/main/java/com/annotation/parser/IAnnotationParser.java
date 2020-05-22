package com.annotation.parser;

import com.android.SdkConstants;

import java.util.List;

public interface IAnnotationParser {
    String PKG_DIR = "com.npcheck.compiler.generated";

    String GN_CLASS_NAME = "NPCheckInfoManager";

    String GN_METHOD_NAME = "initNPCheckInfo";

    String GN_CLASS_FILE = GN_CLASS_NAME + SdkConstants.DOT_CLASS;

    String DOT = ".";

    char DOT_CHAR = '.';

    void parseNPCheckClasses(List<String> checkClasses);
}
