
/**
 * Represents a part available for purchase. Each part tracks its own quanitity.
 * 
 * @author JackSteketee
 *
 */
public class Part {

	String category;
	String name;
	int pkgQty;
	int quantity;
	double price;
	String simpleName;

	public Part(String category, String name, int pkgQty, int quantity, double price, String simpleName) {

		this.category = category;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
		this.simpleName = simpleName.toLowerCase();
		this.pkgQty = pkgQty;
	}

	public String toString() {
		String s = "" + this.quantity;
		int digits = String.valueOf(this.quantity).length();
		for (int i = 0; i < 4 - digits; i++)
			s += " ";
		s += "- " + this.name;
		for (int i = 0; i < 50 - this.name.length(); i++)
			s += " ";
		return s;
	}

	public String getPartPrintout(boolean showPrice) {
		String s = this.toString();
		String a;
		if (showPrice) {
			a = String.format("$%,.2f", this.price * this.quantity);
			s += a;
			for (int i = 0; i < 12 - a.length(); i++)
				s += " ";
			s += "(";
			s += String.format("$%,.2f", this.price) + " each)";
		}
		return s;
	}

	public String getInfo() {
		return this.quantity + " " + this.category + " " + this.name + " " + this.price + " \"" + this.simpleName + "\""
				+ " " + this.pkgQty;
	}
}