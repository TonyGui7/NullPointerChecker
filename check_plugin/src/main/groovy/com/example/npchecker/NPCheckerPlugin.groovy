package com.example.npchecker

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class NPCheckerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
       project.getExtensions().findByType(AppExtension.class).registerTransform(new ByteCodeReaderTransform());
    }
}