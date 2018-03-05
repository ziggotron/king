package com.ziggy.king;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class FakUpgrades {

	public static final String SCIENCE_SOURCE = "science-redux.txt";

	public static final List<String> potatoes = new ArrayList<>(Arrays.asList(new String[] { "FARMERS", "COMMUNES", "COLLECTIVES", "PLANTATIONS", "HIVES" }));
	public static final List<String> land = new ArrayList<>(Arrays.asList(new String[] { "WORKERS", "BLASTING", "CLEARCUT", "ROADS", "HIGHWAYS" }));
	public static final List<String> ores = new ArrayList<>(Arrays.asList(new String[] { "MINERS", "MINES", "EXCAVATORS", "MEGAMINES", "BORES" }));
	public static final List<String> weapons = new ArrayList<>(Arrays.asList(new String[] { "SOLDIERS", "FIRETEAMS", "SQUADS", "PLATOONS", "DIVISIONS" }));
	public static final List<String> medicine = new ArrayList<>(Arrays.asList(new String[] { "NURSES", "AMBULANCES", "FIELDHOS", "CLINICS", "HOSPITALS" }));

	public static ObjectMapper mapper = new ObjectMapper();

	// public static final List<Set<Upgrade>> burned = new ArrayList<>();

	public static void main(String[] args) throws IOException, URISyntaxException {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		final String valueItem = "BTN-LND";
		final Boolean includeAlternatives = true;
		final Integer budget = 230;
		final Integer printSize = 1;
		final Boolean minimalPrint = false;
		final List<Integer> excludedIds = new ArrayList<>(Arrays.asList(new Integer[] { 23, 30, 38, 65, 87, 90, 99, 122, 136, 137, 197 }));

		FakUpgrades util = new FakUpgrades();
		List<Upgrade> upgrades = util.loadUpgrades();

		Map<String, Integer> cntmp = new HashMap<>();
		upgrades.forEach(u -> cntmp.put(u.getIndustry(), cntmp.get(u.getIndustry()) == null ? 1 : cntmp.get(u.getIndustry()) + 1));
		for (String k : cntmp.keySet()) {
			System.out.println(k + " x " + cntmp.get(k));
		}

		List<Upgrade> filtered = util.findByItem(upgrades, includeAlternatives, valueItem);
		System.out.println(filtered.size() + " potential upgrades found\n");

		List<Upgrade> temp = new ArrayList<>();
		filtered.forEach(u -> {
			if (!excludedIds.contains(u.getId()))
				temp.add(u);
		});
		filtered = temp;
		System.out.println(filtered.size() + " potential upgrades after exclusion\n");

		util.printCombo(filtered, valueItem, includeAlternatives, true);

		System.out.println("\n\nOptimizing for best combo\n\n");

		List<Set<Upgrade>> bestCombos = util.getBest(filtered, includeAlternatives, valueItem, budget, printSize);
		System.out.println(bestCombos.size() + " combos found");
		util.printCombos(bestCombos, valueItem, includeAlternatives, printSize, minimalPrint);

		// Collections.sort(burned, new Comparator<Set<Upgrade>>() {
		// @Override
		// public int compare(Set<Upgrade> o1, Set<Upgrade> o2) {
		// BigInteger multi = util.getMultiplier(o1, valueItem, includeAlternatives);
		// BigInteger multi2 = util.getMultiplier(o2, valueItem, includeAlternatives);
		//
		// Integer cost = util.getCost(o1);
		// Integer cost2 = util.getCost(o2);
		// int c = multi2.compareTo(multi);
		// if (c == 0) {
		// c = cost.compareTo(cost2);
		// }
		//
		// return c;
		// }
		// });
		//
		// for (int i = 0; i < 10 && i < burned.size(); ++i) {
		// Set<Upgrade> set = burned.get(i);
		// System.out.println(String.format("Burned %d with %s multiplier ",
		// util.getCost(set), util.getMultiplier(upgrades, valueItem,
		// includeAlternatives).toString()));
		// util.printCombo(set, valueItem, includeAlternatives, true);
		// }

		if (excludedIds.size() > 0) {
			System.out.println("\n\n\n----------------------------------------\n\n\nYou got exclusions, homie\n\n\n----------------------------------------\n\n\n");
		}
	}

	public List<Set<Upgrade>> getBest(List<Upgrade> upgrades, Boolean includeAlternatives, String valueItem, Integer budget, Integer count) {
		Set<Set<Upgrade>> carry = new HashSet<>();
		for (Upgrade upgrade : upgrades) {
			if (Integer.parseInt(upgrade.getCost()) <= budget) {
				Set<Upgrade> seed = new HashSet<>();
				seed.add(upgrade);
				carry.add(seed);
			}
		}
		getAll(carry, upgrades, budget, 1);

		List<Set<Upgrade>> sortable = new ArrayList<>(carry);
		Collections.sort(sortable, new ValueComparator(valueItem, includeAlternatives));

		return sortable;
	}

	public void getAll(Set<Set<Upgrade>> carry, List<Upgrade> upgrades, int budget, int size) {
		List<Set<Upgrade>> existing = new ArrayList<>(carry);
		Set<Set<Upgrade>> hypotheticals = new HashSet<>();
		Set<Set<Upgrade>> toBurn = new HashSet<>();

		for (Set<Upgrade> set : existing) {
			boolean burned = false;
			if (set.size() == size) {

				for (Upgrade upgrade : upgrades) {
					if (!set.contains(upgrade)) {

						Set<Upgrade> hypothetical = new HashSet<>(set);
						hypothetical.add(upgrade);

						Integer cost = getCost(hypothetical);

						if (budget >= cost) {
							hypotheticals.add(hypothetical);
							if (!burned) {
								toBurn.add(set);
								burned = true;
							}
						}

					}

				}
			}
		}

		// trimSets(carry, hypotheticals);
		carry.removeAll(toBurn);
		// burned.addAll(toBurn);
		carry.addAll(hypotheticals);

		if (hypotheticals.size() > 0) {
			System.out.println(carry.size() + " hypotheticals at size " + size);
			getAll(carry, upgrades, budget, size + 1);
		}

	}

	public static void trimSets(Set<Set<Upgrade>> set, Set<Set<Upgrade>> hypotheticals) {
		System.out.println("trimming " + hypotheticals.size() + " hypotheticals from " + set.size() + " existing");
		Set<Set<Upgrade>> removal = new HashSet<>();
		for (Set<Upgrade> u : set) {
			for (Set<Upgrade> v : hypotheticals) {
				if (u.size() < v.size() && v.containsAll(u)) {
					removal.add(u);
					continue;
				}
			}
		}
		set.removeAll(removal);
	}

	public Integer getCost(Collection<Upgrade> upgrades) {
		Integer cost = 0;
		for (Upgrade u : upgrades) {
			cost += Integer.parseInt(u.getCost());
		}
		return cost;
	}

	public BigInteger getMultiplier(Collection<Upgrade> upgrades, String highlighItem, boolean includeAlternatives) {
		BigInteger multi = new BigInteger("1");
		for (Upgrade u : upgrades) {
			for (UpgradeItem ui : u.getPositive()) {
				if (itemMatches(highlighItem, includeAlternatives, ui.getItem())) {
					multi = multi.multiply(new BigInteger("" + ui.getMultiplier()));
				}
			}
		}
		return multi;
	}

	public void printCombos(List<Set<Upgrade>> upgrades, String valueItem, boolean includeAlternatives, int print, boolean minimal) {
		int c = 0;
		for (Set<Upgrade> s : upgrades) {
			System.out.println("\n\n\n----------=================------------\n\n\n");
			c++;

			printCombo(s, valueItem, includeAlternatives, minimal);

			if (c >= print || print <= 0) {
				break;
			}
		}
	}

	public void printCombo(Collection<Upgrade> upgrades, String valueItem, boolean includeAlternatives, boolean minimal) {
		List<Upgrade> ul = new ArrayList<>(upgrades);
		Collections.sort(ul, new IdComparator());
		System.out.println(String.format("x%s (%d science) - %d upgrades:", Fak.p(new BigInteger(getMultiplier(upgrades, valueItem, includeAlternatives).toString())),
				getCost(upgrades), upgrades.size()));

		printUpgrades(ul, valueItem, minimal);
	}

	public void printUpgrades(List<Upgrade> upgrades, String valueItem, boolean minimal) {
		BigInteger mmulti = new BigInteger("1");
		if (minimal) {
			for (Upgrade u : upgrades) {
				for (UpgradeItem ui : u.getPositive()) {
					if (itemMatches(valueItem, true, ui.getItem())) {
						mmulti = mmulti.multiply(new BigInteger("" + ui.getMultiplier()));
						System.out.println(String.format("%s %4d / %3d %0$-15s %s", u.getIndustry(), Integer.parseInt(u.getCost()), ui.getMultiplier(), ui.getItem(), mmulti.toString()));
					}
				}
			}
		} else {
			System.out.println(Arrays.toString(upgrades.toArray()));
		}
		List<Integer> ids = new ArrayList<>();
		upgrades.forEach(u -> ids.add(u.getId()));
		System.out.println(Arrays.toString(ids.toArray()));
	}

	public void printFrequencies(List<Upgrade> upgrades) {
		Map<String, Integer> m = new HashMap<>();
		upgrades.forEach(u -> u.getPositive().forEach(p -> m.put(p.getItem(), m.get(p.getItem()) == null ? 1 : m.get(p.getItem()) + 1)));
		List<String> sl = new ArrayList<>(m.keySet());
		Collections.sort(sl);
		for (String u : sl) {
			System.out.println(u + " x " + m.get(u));
		}
	}

	public static boolean itemMatches(String valueItem, boolean includeAlternatives, String item) {
		if (valueItem.equals(item)) {
			return true;
		}

		if (includeAlternatives) {
			if (potatoes.contains(valueItem) && (item.equals("IND-POT") || item.equals("IND-ALL"))) {
				return true;
			} else if (land.contains(valueItem) && (item.equals("IND-LND") || item.equals("IND-ALL"))) {
				return true;
			} else if (ores.contains(valueItem) && (item.equals("IND-ORE") || item.equals("IND-ALL"))) {
				return true;
			} else if (weapons.contains(valueItem) && (item.equals("IND-WPN") || item.equals("IND-ALL"))) {
				return true;
			} else if (medicine.contains(valueItem) && (item.equals("IND-MED") || item.equals("IND-ALL"))) {
				return true;
			}

			if (valueItem.startsWith("IND") && item.equals("IND-ALL")) {
				return true;
			}

			if (valueItem.startsWith("BTN")) {
				if (item.equals("IND-ALL") || item.equals("BTN-ALL")) {
					return true;
				} else {
					String btnInd = valueItem.split("-")[1];
					if (item.startsWith("IND-")) {
						String indInd = item.split("-")[1];
						if (btnInd.equals(indInd)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public List<Upgrade> findByItem(List<Upgrade> upgrades, boolean includeAlternatives, String... items) {
		List<Upgrade> res = new ArrayList<>();
		for (Upgrade up : upgrades) {
			for (UpgradeItem i : up.getPositive()) {
				for (String item : items) {
					if (itemMatches(item, includeAlternatives, i.getItem())) {
						res.add(up);
						break;
					}
				}
			}
		}
		return res;
	}

	public List<Upgrade> loadUpgrades() throws IOException, URISyntaxException {
		List<Upgrade> res = new ArrayList<>();
		List<String> lines = Files.readAllLines(Paths.get(new File(SCIENCE_SOURCE).toURI()));

		String hostIndustry = "";
		Upgrade upgrade = null;
		Integer id = 0;
		for (String line : lines) {
			if (line.length() == 0) {
				continue;
			}
			if (line.startsWith("===")) {
				hostIndustry = line.substring(3);
				continue;
			}

			String[] split = line.split(" ");
			if (split.length == 1) {
				upgrade = new Upgrade();
				upgrade.setId(id++);
				upgrade.setCost(line);
				upgrade.setIndustry(hostIndustry);
				res.add(upgrade);
			} else {

				String item = split[0];
				Integer effect = Integer.parseInt(split[1]);

				if (effect > 0) {
					UpgradeItem ui = new UpgradeItem();
					ui.setItem(item);
					ui.setMultiplier(effect);
					upgrade.getPositive().add(ui);
				} else {
					DowngradeItem di = new DowngradeItem();
					di.setItem(item);
					di.setPenalty(effect);
					upgrade.getNegative().add(di);
				}

			}
		}

		return res;
	}

	public static class Upgrade {
		private Integer id;
		private String industry;
		private String cost;
		private List<UpgradeItem> positive = new ArrayList<>();
		private List<DowngradeItem> negative = new ArrayList<>();

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public List<UpgradeItem> getPositive() {
			return positive;
		}

		public void setPositive(List<UpgradeItem> positive) {
			this.positive = positive;
		}

		public List<DowngradeItem> getNegative() {
			return negative;
		}

		public void setNegative(List<DowngradeItem> negative) {
			this.negative = negative;
		}

		public String getIndustry() {
			return industry;
		}

		public void setIndustry(String industry) {
			this.industry = industry;
		}

		public String getCost() {
			return cost;
		}

		public void setCost(String cost) {
			this.cost = cost;
		}

		public boolean equals(Object o) {
			Upgrade u = (Upgrade) o;
			return u.getId().equals(getId());
		}

		public int hashCode() {
			return getId().hashCode();
		}

		public String toString() {
			String s = String.format("\n-----\n%s - %d\n%s science\n", getIndustry(), getId(), getCost());
			for (UpgradeItem pos : getPositive()) {
				s += String.format("x%d %s\n", pos.getMultiplier(), pos.getItem());
			}

			for (DowngradeItem neg : getNegative()) {
				s += String.format("%d%% %s\n", neg.getPenalty(), neg.getItem());
			}
			s += "-----\n";
			return s;
		}

	}

	public static class DowngradeItem {
		private String item;
		private Integer penalty;

		public String getItem() {
			return item;
		}

		public void setItem(String item) {
			this.item = item;
		}

		public Integer getPenalty() {
			return penalty;
		}

		public void setPenalty(Integer penalty) {
			this.penalty = penalty;
		}

	}

	public static class UpgradeItem {
		private String item;
		private Integer multiplier;

		public String getItem() {
			return item;
		}

		public void setItem(String item) {
			this.item = item;
		}

		public Integer getMultiplier() {
			return multiplier;
		}

		public void setMultiplier(Integer multiplier) {
			this.multiplier = multiplier;
		}

	}

	public String pad(String s, int length) {
		while (s.length() < length) {
			s += " ";
		}
		return s;
	}

	public static class CostComparator implements Comparator<Upgrade> {

		@Override
		public int compare(Upgrade o1, Upgrade o2) {
			return Integer.valueOf(Integer.parseInt(o1.getCost())).compareTo(Integer.parseInt(o2.getCost()));
		}

	}

	public static class IdComparator implements Comparator<Upgrade> {

		@Override
		public int compare(Upgrade o1, Upgrade o2) {
			return o1.getId().compareTo(o2.getId());
		}

	}

	public static class ValueComparator implements Comparator<Set<Upgrade>> {

		private String valueItem;
		private Boolean includeAlternatives;

		public ValueComparator(String valueItem, Boolean includeAlternatives) {
			this.valueItem = valueItem;
			this.includeAlternatives = includeAlternatives;
		}

		@Override
		public int compare(Set<Upgrade> o1, Set<Upgrade> o2) {
			BigInteger multiplier1 = new FakUpgrades().getMultiplier(o1, valueItem, includeAlternatives);
			BigInteger multiplier2 = new FakUpgrades().getMultiplier(o2, valueItem, includeAlternatives);

			int c = multiplier2.compareTo(multiplier1);
			if (c == 0) {
				Integer co1 = 0;
				Integer co2 = 0;
				for (Upgrade u : o1) {
					co1 += Integer.parseInt(u.getCost());
				}
				for (Upgrade u : o2) {
					co2 += Integer.parseInt(u.getCost());
				}
				c = co1.compareTo(co2);
			}
			return c;
		}
	}
}
