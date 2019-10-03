package com.github.allati.asciidoctor.tree;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Reader;

public class MonoTreeProcessor extends BlockProcessor {

	public static final String OPT_SYMBOL_EMPTY = "symbol_empty";
	public static final String OPT_SYMBOL_JUNCTION = "symbol_junction";
	public static final String OPT_SYMBOL_PASSTHROUGH = "symbol_passthrough";
	public static final String OPT_SYMBOL_TERMINAL = "symbol_terminal";
	public static final String OPT_SYMBOL_EMPTY_SH = "e";
	public static final String OPT_SYMBOL_JUNCTION_SH = "j";
	public static final String OPT_SYMBOL_PASSTHROUGH_SH = "p";
	public static final String OPT_SYMBOL_TERMINAL_SH = "t";

	public static final String OPT_SYMBOLS = "symbols";

	public static final String SYMBOL_SET_FANCY = "fancy";
	public static final String SYMBOL_SET_SIMPLE = "simple";

	private final Map<String, Map<LaneState, String>> definedSymbolSets;

	public MonoTreeProcessor(String name, Map<String, Object> config) {
		super(name, appendConfig(config));
		definedSymbolSets = new HashMap<>();

		try (
			InputStream is = getClass().getClassLoader().getResourceAsStream("com/github/allati/asciidoctor/tree/symbolsets.properties");
		) {
			Properties props = new Properties();
			props.load(is);

			definedSymbolSets.put(SYMBOL_SET_FANCY, loadSymbolSet(props, SYMBOL_SET_FANCY));
			definedSymbolSets.put(SYMBOL_SET_SIMPLE, loadSymbolSet(props, SYMBOL_SET_SIMPLE));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Map<String, Object> appendConfig(Map<String, Object> config) {
		config.put("contexts", Arrays.asList(":listing"));
		return config;
	}

	private Map<LaneState, String> loadSymbolSet(Properties props, String setName) {
		Map<LaneState, String> enumMap = new EnumMap<>(LaneState.class);
		for (LaneState state : LaneState.values()) {
			enumMap.put(state, props.getProperty(setName + "." + state.name()));
		}
		return enumMap;
	}

	@Override
	public Map<String, Object> getConfig() {
		return super.getConfig();
	}

	private Map<LaneState, String> createSymbolSet(Map<String, Object> attributes) {
		Map<LaneState, String> style = new EnumMap<>(LaneState.class);

		String symbolSetName = Objects.toString(attributes.remove(OPT_SYMBOLS), SYMBOL_SET_FANCY);
		Map<LaneState, String> symbolSet = definedSymbolSets.get(symbolSetName);
		if (symbolSet != null) {
			style.putAll(symbolSet);
		}

		overrideSymbolIfDefined(style, attributes, LaneState.EMPTY, OPT_SYMBOL_EMPTY);
		overrideSymbolIfDefined(style, attributes, LaneState.PASSTHROUGH, OPT_SYMBOL_PASSTHROUGH);
		overrideSymbolIfDefined(style, attributes, LaneState.JUNCTION, OPT_SYMBOL_JUNCTION);
		overrideSymbolIfDefined(style, attributes, LaneState.TERMINAL, OPT_SYMBOL_TERMINAL);

		overrideSymbolIfDefined(style, attributes, LaneState.EMPTY, OPT_SYMBOL_EMPTY_SH);
		overrideSymbolIfDefined(style, attributes, LaneState.PASSTHROUGH, OPT_SYMBOL_PASSTHROUGH_SH);
		overrideSymbolIfDefined(style, attributes, LaneState.JUNCTION, OPT_SYMBOL_JUNCTION_SH);
		overrideSymbolIfDefined(style, attributes, LaneState.TERMINAL, OPT_SYMBOL_TERMINAL_SH);

		return style;
	}

	private static void overrideSymbolIfDefined(Map<LaneState, String> style, Map<String, Object> attributes, LaneState state, String attrName) {
		Object symbol = attributes.remove(attrName);
		if (symbol != null) {
			style.put(state, symbol.toString());
		}
	}

	@Override
	public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
		List<TreeLine> treeLines = new ArrayList<>();
		for (String line : reader.readLines()) {
			int offset = computeLevel(line);
			treeLines.add(new TreeLine(offset - 1, line.substring(offset + 1)));
		}

		Map<LaneState, String> symbolSet = createSymbolSet(attributes);
		LaneState[][] computeLanes = computeLanes(treeLines);

		List<String> tree = new ArrayList<String>(treeLines.size());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < computeLanes.length; i++) {
			for (int j = 0; j < computeLanes[i].length; j++) {
				sb.append(symbolSet.get(computeLanes[i][j]));
			}
			sb.append(treeLines.get(i).getText());
			tree.add(sb.toString());
			sb.setLength(0);
		}

		return createBlock(parent, "listing", tree, attributes, new HashMap<>());
	}

	private static int computeLevel(String line) {
		int i = 0;
		while (line.charAt(i) == '>' && i < line.length()) {
			i++;
		}
		return i;
	}

	public static LaneState[][] computeLanes(List<TreeLine> lines) {
		LaneState[][] lanes = new LaneState[lines.size()][];
		LaneState[] prevLine = new LaneState[0];
		for (int i = lanes.length - 1; i >= 0; i--) {
			int nesting = lines.get(i).getNesting();
			LaneState[] line  = new LaneState[nesting];
			for (int j = 0; j < nesting; j++) {
				if (j < prevLine.length && prevLine[j] != LaneState.EMPTY) {
					line[j] = (j < nesting - 1 ? LaneState.PASSTHROUGH : LaneState.JUNCTION); ;
				} else {
					line[j] = (j < nesting - 1 ? LaneState.EMPTY : LaneState.TERMINAL);
				}
			}
			lanes[i] = line;
			prevLine = line;
		}
		return lanes;
	}

	private static class TreeLine {
		private int nesting;
		private String text;

		public TreeLine(int nesting, String text) {
			this.nesting = nesting;
			this.text = text;
		}

		public int getNesting() {
			return nesting;
		}

		public String getText() {
			return text;
		}
	}
}