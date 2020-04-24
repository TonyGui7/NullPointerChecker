package com.example.pluginexplore

import com.android.annotations.NonNull
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ddmlib.Log
import com.example.gui.AsmUtil
import org.apache.commons.io.FileUtils

import java.util.function.Consumer

class ExploreTransform extends Transform{

    @Override
    public String getName() {
        return "ExploreTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(@NonNull TransformInvocation invocation)
            throws TransformException, InterruptedException, IOException {
        super.transform(invocation)
        Collection<TransformInput> inputs = invocation.getInputs();
        for (TransformInput input : inputs) {
            Collection<DirectoryInput> directoryInputCollection = input.getDirectoryInputs();
            input.getDirectoryInputs().parallelStream().forEach(new Consumer<DirectoryInput>() {
                @Override
                public void accept(DirectoryInput directoryInput) {
                    File dest = invocation.getOutputProvider().getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                    AsmUtil.getInstance().handleClassFile(directoryInput.getFile(), dest);
                    try {
                        FileUtils.copyDirectory(directoryInput.getFile(), dest);
                    }catch (Exception e) {
                        Log.d("Transform", "test")
                        e.printStackTrace();
                    }
                }
            });

            input.getJarInputs().parallelStream().forEach(new Consumer<JarInput>() {
                @Override
                public void accept(JarInput jarInput) {
                    File dest = invocation.getOutputProvider().getContentLocation(jarInput.getFile().getAbsolutePath(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                    try{
                        FileUtils.copyFile(jarInput.getFile(), dest);
                    } catch (Exception e) {
                        Log.d("Transform", "test")
                        e.printStackTrace();
                    }
                }
            })
        }
    }
}