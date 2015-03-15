package com.github.allati.asciidoctor.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.Reader;
import org.jruby.Ruby;

public class AsciidoctorTree {
	public static void main(String[] args) {
		Asciidoctor asciidoctor = Asciidoctor.Factory.create();
		JavaExtensionRegistry jer = asciidoctor.javaExtensionRegistry();
		HashMap<String, Object> config = new HashMap<String, Object>();
		config.put("contexts", Arrays.asList(Ruby.getGlobalRuntime().newSymbol("listing")));
		jer.block("tree", new MonoTreeProcessor("tree", config));
		asciidoctor.renderFile(new File("test.adoc"), OptionsBuilder.options().safe(SafeMode.UNSAFE).get());
	}

	private static class MonoTreeProcessor extends BlockProcessor {

		public MonoTreeProcessor() {
			super("tree");
		}
		
		public MonoTreeProcessor(String name) {
			super(name);
		}

		public MonoTreeProcessor(String name, Map<String, Object> config) {
			super(name, config);
		}

		@Override
		public Map<Object, Object> getConfig() {
			return super.getConfig();
		}

		@Override
		public Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
			List<String> lines = reader.readLines();
			int[] levels = new int[lines.size()];
			for (int i = 0; i < levels.length; i++) {
				levels[i] = computeLevel(lines.get(i)) - 1;
			}

			LaneState[][] computeLanes = computeLanes(levels);

			List<String> tree = new ArrayList<String>(levels.length);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < computeLanes.length; i++) {
				for (int j = 0; j < computeLanes[i].length; j++) {
					switch (computeLanes[i][j]) {
						case EMPTY:
							sb.append("    ");
							break;
							
						case JUNCTION:
							sb.append("├── ");
							break;
							
						case PASSTHROUGH:
							sb.append("│   ");
							break;
							
						case TERMINAL:
							sb.append("└── ");
							break;
					}
				}
				sb.append(lines.get(i).substring(levels[i] + 2));
				tree.add(sb.toString());
				sb.setLength(0);
			}
			
			return createBlock(parent, "listing", tree, attributes, new HashMap<Object, Object>());
		}

		private static int computeLevel(String line) {
			int i = 0;
			while (line.charAt(i) == '>' && i < line.length()) {
				i++;
			}
			return i;
		}

		public static LaneState[][] computeLanes(int[] levels) {
			LaneState[][] lanes = new LaneState[levels.length][];
			LaneState[] prevLine = new LaneState[0];
			for (int i = levels.length - 1; i >= 0; i--) {
				lanes[i] = new LaneState[levels[i]];
				for (int j = 0; j < levels[i]; j++) {
					if (j < prevLine.length && prevLine[j] != LaneState.EMPTY) {
						lanes[i][j] = (j < levels[i] - 1 ? LaneState.PASSTHROUGH : LaneState.JUNCTION); ;
					} else {
						lanes[i][j] = (j < levels[i] - 1 ? LaneState.EMPTY : LaneState.TERMINAL);
					}
				}
				prevLine = lanes[i];
			}
			return lanes;
		}
	}

	private enum LaneState {
		PASSTHROUGH,
		JUNCTION,
		EMPTY,
		TERMINAL,
	}
}
