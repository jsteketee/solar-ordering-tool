
import java.util.ArrayList;

/**
 * Represents the list of parts available for purchase. Contains methods for
 * reporting this list in formatted text. Only parts with a positive quantity
 * are reported.
 * 
 * @author JackSteketee
 *
 */
public class PartsList {
	ArrayList<Part> partList = new ArrayList<Part>();
	ArrayList<String> categoryList = new ArrayList<String>();
	ArrayList<Double> categoryCost = new ArrayList<Double>();
	boolean verbose;

	// initialize part list
	public PartsList(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Populates the list of available parts from a csv file. Most part
	 * quantities are zero except for those being manually ordered.
	 * 
	 * @param p - the part to be added to the part list.
	 */
	public void loadPart(Part p) {
		partList.add(p);
		if (categoryList.isEmpty() || !categoryList.contains(p.category)) {
			categoryList.add(p.category);
			categoryCost.add(p.quantity * p.price);
		} else if (p.quantity > 0) {
			categoryCost.set(categoryList.indexOf(p.category),
					(double) p.price * p.quantity);
		}
	}

	/**
	 * Main method that increments the quantity of parts that need to be
	 * ordered. Searching by category and simple name allows for some
	 * flexibility as specific part names change.
	 * 
	 * @param category   - the category of the part
	 * @param simpleName - the common name of the part
	 * @param qty        - the amount by which to increment the part quantity.
	 */
	public void addPart(String category, String simpleName, int qty) {
		boolean partFound = false;
		String s = "";
		double catCost;

		for (Part p : partList) {
			if (p.category.toLowerCase().contains(category.toLowerCase())) {
				if (p.simpleName.toLowerCase()
						.contains(simpleName.toLowerCase())) {
					if (p.pkgQty > 1) {
						p.quantity += Math.ceil((double) qty / p.pkgQty);

					} else {
						p.quantity += qty;
					}
					catCost = categoryCost
							.get(categoryList.indexOf(p.category));
					categoryCost.set(categoryList.indexOf(p.category),
							catCost + p.price * p.quantity);
					s = "Part Added: " + p.toString();
					partFound = true;
					break;
				}
			}
		}
		if (!partFound)
			System.out
					.println(category + " - " + simpleName + " Part not found");
		if (verbose)
			System.out.println(s);
	}

	/**
	 * Iterates through the part list and returns a String containing the
	 * formatted part order. While doing so it sums the total order cost as well
	 * as category specific costs.
	 * 
	 * @param displayCost - causes method to include the cost estimates in its
	 *                    output.
	 * @param Wattage     - the total power (in Watts) of the system.
	 * @return
	 */
	public String partListReport(boolean displayCost, int Wattage) {

		double totalCost = 0;
		String curCategory = "";
		String toReturn = "";
		String a;

		for (Part p : partList) {
			if (p.quantity > 0) {
				if (!p.category.contentEquals(curCategory)) {
					curCategory = p.category;
					toReturn += "\n\n" + curCategory + ":" + "";
				}
				toReturn += "\n" + p.getPartPrintout(displayCost);
			}
		}

		if (displayCost) {
			toReturn += "\n\n\n";
			double curCatCost;
			for (String curCat : categoryList) {
				curCatCost = categoryCost.get(categoryList.indexOf(curCat));
				if (curCatCost > 0) {
					totalCost += curCatCost;
					toReturn += curCat;
					for (int i = 0; i < 56 - curCat.length(); i++)
						toReturn += " ";
					a = String.format("$%,.2f", curCatCost);
					toReturn += a;
					for (int i = 0; i < 12 - a.length(); i++)
						toReturn += " ";
					toReturn += "(ppw = "
							+ String.format("$%,.2f", curCatCost / Wattage)
							+ ")\n";
				}
			}

			toReturn += "\n\nTotal Cost: " + String.format("$%,.2f", totalCost);

			toReturn += "\nTotal ppW:  "
					+ String.format("$%,.2f", totalCost / Wattage) + "\n";
		}
		return toReturn;
	}
}
