package fr.siroz.cariboustonks.core.skyblock.tablist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a {@code Widget} from the client TabList
 */
public class TabWidget {
	private final TabLine header;
	private final List<TabLine> lines = new ArrayList<>();

	public TabWidget(TabLine header) {
		this.header = header;
	}

	public void addLine(TabLine line) {
		lines.add(line);
	}

	/**
	 * "Jacob's Contest: 11m left" → "Jacob's Contest"
	 */
	public String getName() {
		int colon = header.text().indexOf(':');
		return colon >= 0 ? header.text().substring(0, colon).trim() : header.text().trim();
	}

	public TabLine getHeader() {
		return header;
	}

	public List<TabLine> getLines() {
		return Collections.unmodifiableList(lines);
	}
}
