package com.github.vitallium.rubylsp.lsp

import com.intellij.ide.util.PropertiesComponent

class RubyLspConfigProperties(private val propertiesComponent: PropertiesComponent) {
    var pluginEnabled: Boolean
        get() = propertiesComponent.getBoolean(PLUGIN_ENABLED_KEY, true)
        set(enabled) {
            propertiesComponent.setValue(PLUGIN_ENABLED_KEY, enabled, true)
        }

    var experimentsEnabled: Boolean
        get() = propertiesComponent.getBoolean(EXPERIMENTS_ENABLED_KEY, false)
        set(enabled) {
            propertiesComponent.setValue(EXPERIMENTS_ENABLED_KEY, enabled, false)
        }

    var formattingEnabled: Boolean
        get() = propertiesComponent.getBoolean(FORMATTING_ENABLED_KEY, false)
        set(enabled) {
            propertiesComponent.setValue(FORMATTING_ENABLED_KEY, enabled, false)
        }

    companion object {
        private val PLUGIN_ENABLED_KEY
            get() = RubyLspConfigProperties::class.java.getPackageName() + ".enabled"
        private val EXPERIMENTS_ENABLED_KEY
            get() = RubyLspConfigProperties::class.java.getPackageName() + ".enableExperiments"
        private val FORMATTING_ENABLED_KEY
            get() = RubyLspConfigProperties::class.java.getPackageName() + ".enableFormatting"
    }
}
