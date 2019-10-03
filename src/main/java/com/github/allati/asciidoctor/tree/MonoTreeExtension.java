package com.github.allati.asciidoctor.tree;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

public class MonoTreeExtension implements ExtensionRegistry {

	@Override
	public void register(Asciidoctor asciidoctor) {
		JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
		javaExtensionRegistry.block("tree", MonoTreeProcessor.class);
	}
}
