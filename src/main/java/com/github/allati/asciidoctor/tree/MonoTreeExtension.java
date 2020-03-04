package com.github.allati.asciidoctor.tree;

import com.uniqueck.asciidoctorj.extension.support.AbstractAsciidoctorjExtensionRegistry;
import org.asciidoctor.extension.JavaExtensionRegistry;

public class MonoTreeExtension extends AbstractAsciidoctorjExtensionRegistry {

	@Override
	protected void registerExtension(JavaExtensionRegistry javaExtensionRegistry) {
		javaExtensionRegistry.block(MonoTreeProcessor.class);
	}

}
