package fr.siroz.cariboustonks.core.mod.changelog;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single entry in the changelog
 */
public class ChangelogEntry {
	public String version;
	public String date;
	public List<String> notes = new ArrayList<>();
	public List<String> feature = new ArrayList<>();
	public List<String> improvement = new ArrayList<>();
	public List<String> fixed = new ArrayList<>();
	public List<String> backend = new ArrayList<>();

	public boolean isEmpty() {
		return feature.isEmpty() && improvement.isEmpty() && fixed.isEmpty() && backend.isEmpty();
	}
}
