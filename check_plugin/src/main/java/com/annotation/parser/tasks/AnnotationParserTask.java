package com.annotation.parser.tasks;

import com.ITaskFlowInstruction;
import com.TaskBeanContract;
import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.annotation.parser.IAnnotationParser;
import com.annotation.parser.asm.AptClassVisitor;
import com.nullpointer.analysis.bean.output.SimpleTaskOutput;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 针对注解编译生成的class文件名，解析其中需要检测的class文件的路径和文件名，根据该信息找到对应的class文件
 *
 * @author guizhihong
 */
public class AnnotationParserTask implements ITaskFlowInstruction.IAnnotationParser<TaskBeanContract.ISimpleTaskInput, TaskBeanContract.ISimpleTaskOutput>, IAnnotationParser {
    private Listener<TaskBeanContract.ISimpleTaskOutput> mListener;
    private File mTransformFile;
    private Collection<File> mCheckFiles;

    @Override
    public void start(@NonNull TaskBeanContract.ISimpleTaskInput iSimpleTaskInput, @NonNull Listener<TaskBeanContract.ISimpleTaskOutput> listener) {
        mListener = listener;
        mTransformFile = iSimpleTaskInput.getTransformFile();
        mCheckFiles = new ArrayList<>();
        checkToStart();
    }

    private void checkToStart() {
        if (mTransformFile == null) {
            end();
            return;
        }

        File dirFile = new File(mTransformFile, IAnnotationParser.PKG_DIR.replace(IAnnotationParser.DOT_CHAR, File.separatorChar));
        File generatedFile = FileUtils.getFile(dirFile, IAnnotationParser.GN_CLASS_FILE);

        try {
            ClassReader classReader = new ClassReader(FileUtils.readFileToByteArray(generatedFile));
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            AptClassVisitor classVisitor = new AptClassVisitor(classWriter, this);
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseCheckFiles(List<String> checkClasses) {
        if (checkClasses == null || checkClasses.isEmpty()) {
            return;
        }

        for (String checkClass : checkClasses) {
            String[] classInfo = parse(checkClass);
            String pkgName = classInfo[0];
            String fileName = classInfo[1];
            if (pkgName == null || fileName == null) {
                continue;
            }

            File dirFile = new File(mTransformFile, pkgName.replace(IAnnotationParser.DOT_CHAR, File.separatorChar));
            mCheckFiles.addAll(FileUtils.listFiles(dirFile, new NameFileFilter(fileName + SdkConstants.DOT_CLASS), TrueFileFilter.INSTANCE));
        }
    }

    /**
     * 通过全路径名解析出文件名和路径名，数组第一个是路径名，第二个是文件名
     *
     * @param pathClass class路径名
     */
    private String[] parse(String pathClass) {
        String[] classInfo = new String[2];
        if (pathClass == null || pathClass.isEmpty()) {
            return classInfo;
        }

        String[] dotSplits = Pattern.compile("[.]").split(pathClass);
        String fileName = dotSplits[dotSplits.length - 1];

        String[] fileNameSplits = pathClass.split(IAnnotationParser.DOT + fileName);
        String packageName = fileNameSplits[0];

        classInfo[0] = packageName;
        classInfo[1] = fileName;
        return classInfo;
    }

    private @NonNull
    TaskBeanContract.ISimpleTaskOutput buildOutput() {
        return new SimpleTaskOutput.Builder()
                .checkFiles(mCheckFiles)
                .build();
    }

    @Override
    public void end() {
        if (mListener != null) {
            mListener.notifyEnd(buildOutput());
        }
    }

    @Override
    public void parseNPCheckClasses(List<String> checkClasses) {
        parseCheckFiles(checkClasses);
        end();
    }
}
