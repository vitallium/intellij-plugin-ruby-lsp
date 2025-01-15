package com.github.vitallium.rubylsp

import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.roots.ModuleRootModel
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy
import com.intellij.openapi.vfs.impl.LightFilePointer
import com.intellij.openapi.vfs.pointers.VirtualFilePointer

class RubyLspDirectoryIndexExcludePolicy : DirectoryIndexExcludePolicy {
    override fun getExcludeRootsForModule(rootModel: ModuleRootModel): Array<VirtualFilePointer> {
        return rootModel.module.guessModuleDir()?.findChild(".ruby-lsp")?.let{ arrayOf(LightFilePointer(it)) }
            ?: emptyArray()
    }
}
