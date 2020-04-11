
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * I created this solar ordering tool so that a SolarPV EPC can efficiently
 * create and verify part orders for individual residential solar projects. The
 * supplier that provides all of the parts would frequently send quotes with the
 * wrong part quantities and/or part pricing. Ordering using this tool allows us
 * to ensure 100% ordering and pricing accuracy.
 *
 * A .numbers spreadsheet contains all of the part information as well as a
 * template for inputting the details of the solar project. Once filled in, the
 * .numbers file is exported to a CSV.
 *
 * This class contains a main method that reads from the csv file and outputs a
 * material order request for a solar system. The main method performs the
 * following:
 *
 * 1. Reads the CSV files to import the parts list and solar system info. 2.
 * Performs logic on the solar system info to determine the quantity of specific
 * parts that need to be ordered. 3. Utilizes the part and PartsList class to
 * output formatted text that can be sent to the parts supplier. 4. Reports on
 * the cost of various part categories to better inform solar system pricing.
 *
 * @author JackSteketee
 *
 */
public class SolarOrderingTool {

	static boolean showExpectedCost = true;
	static boolean verbose = false;

	static String s;
	static String[] csvArray;
	static int a;
	static File CSV;

	static PartsList materialsToOrder = new PartsList(verbose);

	static String ballastRackingType1;
	static String ballastRackingType2;
	static boolean[][] ballastLayout1 = new boolean[10][14];
	static boolean[][] ballastLayout2 = new boolean[10][14];
	static int b1Extra;
	static int b2Extra;

	static String customerName;
	static String deliveryDate;
	static String projectType;
	static String address;

	static String systemType;
	static String panelType;
	static int panelWattage;
	static String panelLevelDeviceType;
	static String inverterType;
	static int inverterCount;
	static int cellCount;
	static int discoRating;
	static int fuseRating;
	static boolean consMonitor = false;
	static boolean fuseAdapter = false;

	static String attachmentType;
	static int tiltLeg;
	static int attachmentOverride;
	static double panelHeight;
	static double panelWidth;
	static int panelThickness;

	static int railCount;
	static int spliceBarCount;
	static int midClampCount;
	static int stopperSleeveCount;
	static int groundLugCount;
	static int attachmentCount;

	static int pitchedPanelCount;
	static int ballastedPanelCount;
	static int totalPanelCount;

	static int qCablePortrait;
	static int qCableLandscape;

	static int systemWattage;

	static String errorMessage = "\n\n***************************************************************\n"
			+ "Error:\nSolar orderding template csv not found. \nPerhaps you exported the "
			+ "Numbers file to your downloads folder? \nMake sure to export it to the same folder as the Numbers file.\n"
			+ "***************************************************************\n\n";

	@SuppressWarnings("resource")
	public static void main(String[] args) throws FileNotFoundException {

		if (args.length > 1) {
			if (args[0].contains("no"))
				showExpectedCost = false;
			if (args[1].contains("yes"))
				verbose = true;
		}

		// Import full parts list
		try {
			csvArray = new Scanner(
					new File("Solar Ordering Template/Ordering Template-Parts List.csv"))
							.useDelimiter("//A").next().split(",|\n");
		} catch (Exception e) {
			System.out.println(errorMessage);
			System.exit(1);
		}
		for (a = 0; a < csvArray.length; a++) {
			csvArray[a] = csvArray[a].trim();
		}

		int colnum = 8;
		for (int i = 2; i * colnum < csvArray.length; i++) {
			int rowStart = i * 8;
			if (!csvArray[rowStart].equals(""))
				materialsToOrder.loadPart(new Part(csvArray[rowStart], csvArray[rowStart + 1],
						Integer.parseInt(csvArray[rowStart + 2]),
						Integer.parseInt(csvArray[rowStart + 3]),
						Double.parseDouble(csvArray[rowStart + 4].replace("$", "")),
						csvArray[rowStart + 6]));
		}

		if (verbose) {
			System.out.println("\n\n Parts List:\n");
			for (Part p : materialsToOrder.partList) {
				System.out.println(p.getInfo());
			}
		}

		// Import project info + system type
		csvArray = new Scanner(
				new File("Solar Ordering Template/Ordering Template-System Info.csv"))
						.useDelimiter("//A").next().split(",|\n");
		for (a = 8; a < csvArray.length; a++) {
			csvArray[a] = csvArray[a].trim().toLowerCase();
		}

		a = 1;
		customerName = csvArray[a];
		deliveryDate = csvArray[a += 2];
		projectType = csvArray[a += 2];
		address = csvArray[a += 2];
		a += 2;
		systemType = csvArray[a += 2];
		panelType = csvArray[a += 2];
		panelWattage = Integer.parseInt(csvArray[a += 2]);
		panelLevelDeviceType = csvArray[a += 2];
		inverterType = csvArray[a += 2];
		inverterCount = Integer.parseInt(csvArray[a += 2]);
		cellCount = Integer.parseInt(csvArray[a += 2]);
		discoRating = Integer.parseInt(csvArray[a += 2]);
		fuseRating = Integer.parseInt(csvArray[a += 2]);
		consMonitor = (csvArray[a += 2].toLowerCase().contains("yes"));
		fuseAdapter = (fuseRating > discoRating);

		if (verbose) {
			System.out.println("\n\nSystem Info:\n");
			for (int i = 0; i < csvArray.length; i += 2) {
				System.out.println(csvArray[i] + " " + csvArray[i + 1]);
			}
			System.out.println("\n");
		}

		// Load in pitched roof racking info
		csvArray = new Scanner(
				new File("Solar Ordering Template/Ordering Template-Rail Layout.csv"))
						.useDelimiter("//A").next().split(",|\n");
		for (a = 0; a < csvArray.length; a++) {
			csvArray[a] = csvArray[a].trim().toLowerCase();
		}

		a = 1;
		attachmentType = csvArray[a];
		tiltLeg = Integer.parseInt(csvArray[a += 18].replace("\"", ""));
		attachmentOverride = Integer.parseInt(csvArray[a += 18]);
		panelHeight = Double.parseDouble((csvArray[a += 18].replace("mm", "")));
		panelWidth = Double.parseDouble((csvArray[a += 18].replace("mm", "")));
		panelThickness = Integer.parseInt(((csvArray[a += 18].replace("mm", ""))));

		qCablePortrait = Integer.parseInt(csvArray[196]);
		if (qCablePortrait > 0)
			qCablePortrait += 2;
		qCableLandscape = Integer.parseInt(csvArray[538]);

		if (qCableLandscape > 0)
			qCableLandscape += 2;

		// Load in pitched roof racking count's:
		csvArray = new Scanner(
				new File("Solar Ordering Template/Ordering Template-Rail Racking Count.csv"))
						.useDelimiter("//A").next().split(",|\n");
		for (a = 0; a < csvArray.length; a++) {
			csvArray[a] = csvArray[a].trim().toLowerCase();
		}

		pitchedPanelCount = Integer.parseInt(csvArray[7]);
		railCount = Integer.parseInt(csvArray[11]);
		spliceBarCount = Integer.parseInt(csvArray[15]);
		midClampCount = Integer.parseInt(csvArray[19]);
		stopperSleeveCount = Integer.parseInt(csvArray[23]);
		groundLugCount = Integer.parseInt(csvArray[27]);
		attachmentCount = Integer.parseInt(csvArray[31]);
		if (attachmentOverride > -1)
			attachmentCount = attachmentOverride;

		if (verbose) {
			System.out.println("\n\nRail Parts Count:\n");
			for (int i = 4; i < csvArray.length; i += 4) {
				System.out.println(csvArray[i + 1] + " - " + csvArray[i]);
			}
		}

		// Load in ballast layout boolean[][] ballastArray1 = new
		// boolean[10][14];
		csvArray = new Scanner(
				new File("Solar Ordering Template/Ordering Template-Flat Layout.csv"))
						.useDelimiter("//A").next().split(",|\n");
		for (a = 0; a < csvArray.length; a++) {
			csvArray[a] = csvArray[a].trim().toLowerCase();
		}

		ballastRackingType1 = csvArray[0];
		b1Extra = Integer.parseInt(csvArray[4]);
		ballastRackingType2 = csvArray[154];
		b2Extra = Integer.parseInt(csvArray[158]);

		if (verbose)
			System.out.println("\n\nBallast layout 1:\n");

		boolean t = false;
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j <= 13; j++) {
				if (csvArray[(i + 1) * 14 + j].contains("true")) {
					ballastLayout1[i][j] = true;
					t = true;
					ballastedPanelCount++; // counts ballast panels
				} else
					ballastLayout1[i][j] = false;
				if (verbose)
					System.out.print(ballastLayout1[i][j] + " ");
			}
			if (verbose)
				System.out.println("");
		}
		if (t)
			qCableLandscape += 2;
		t = false;

		if (verbose)
			System.out.println("\n\nBallast layout 2:\n");

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j <= 13; j++) {
				if (csvArray[(i + 12) * 14 + j].contains("true")) {
					ballastLayout2[i][j] = true;
					t = true;
					ballastedPanelCount++; // counts ballast panels
				} else
					ballastLayout2[i][j] = false;
				if (verbose)
					System.out.print(ballastLayout1[i][j] + " ");
			}
			if (verbose)
				System.out.println("");
		}
		if (verbose)
			System.out.println("\n");

		if (t)
			qCableLandscape += 2;
		qCableLandscape += ballastedPanelCount;

		totalPanelCount = pitchedPanelCount + ballastedPanelCount;
		systemWattage = totalPanelCount * panelWattage;

		int breakerCount;
		if (panelLevelDeviceType.contains("+")) {
			breakerCount = (int) Math.ceil((double) totalPanelCount / 13);
		} else if (panelLevelDeviceType.toLowerCase().contains("x")) {
			breakerCount = (int) Math.ceil((double) totalPanelCount / 12);
		} else {
			breakerCount = (int) Math.ceil((double) totalPanelCount / 16);
		}

		// ***** Add all the parts to the order list that we need to order ******
		String category = "";
		if (totalPanelCount > 0) {

			category = "modules";
			materialsToOrder.addPart(category, panelType, totalPanelCount);

			if (systemType.equals("solaredge")) {
				category = "solaredge";
				materialsToOrder.addPart(category, inverterType, inverterCount);
				materialsToOrder.addPart(category, panelLevelDeviceType, totalPanelCount);
				materialsToOrder.addPart(category, "cell kit", cellCount);
				if (consMonitor) {
					materialsToOrder.addPart(category, "CT", 2);
					materialsToOrder.addPart(category, "energy meter", 1);
				}
			}
			if (systemType.contains("enphase")) {
				category = "enphase";

				materialsToOrder.addPart(category, panelLevelDeviceType, totalPanelCount);
				materialsToOrder.addPart(category, "cell kit", cellCount);
				materialsToOrder.addPart(category, "Qcable Portrait", qCablePortrait);
				materialsToOrder.addPart(category, "Qcable Landscape", qCableLandscape);
				materialsToOrder.addPart(category, "sealing cap",
						qCablePortrait + qCableLandscape - totalPanelCount);
				materialsToOrder.addPart(category, "terminator cap", breakerCount + 1);

				if (systemType.contains("iq combiner")) {
					materialsToOrder.addPart(category, "combiner", 1);
					materialsToOrder.addPart(category, "solar breaker", breakerCount);
				} else {
					materialsToOrder.addPart(category, "envoy", 1);
				}
				if (consMonitor)
					materialsToOrder.addPart(category, "ct", 2);
			}

			category = "Fuses and Disconnects";
			materialsToOrder.addPart(category, discoRating + "A Disconnect", 1);
			materialsToOrder.addPart(category, fuseRating + "A Fuse", 2);
			if (fuseAdapter)
				materialsToOrder.addPart(category, "reducer", 2);

			if (pitchedPanelCount > 0) {
				category = "IronRidge";
				String railSize = "10";
				if (attachmentType.contains("curb"))
					railSize = "100";

				materialsToOrder.addPart(category, "rail bolt", attachmentCount);
				materialsToOrder.addPart(category, "XR" + railSize, railCount);
				materialsToOrder.addPart(category, "XR" + railSize + " splice", spliceBarCount);
				materialsToOrder.addPart(category, "UFO", midClampCount);
				materialsToOrder.addPart(category, "sleeve " + panelThickness, stopperSleeveCount);
				materialsToOrder.addPart(category, "lug", groundLugCount);
				materialsToOrder.addPart(category, "T Bolt",
						(int) Math.ceil(pitchedPanelCount * 1.3));

				if (attachmentType.contains("quickmount")) {
					category = "quickmount";
					materialsToOrder.addPart(category, "QM Flashing Kit", attachmentCount);

				}
				if (attachmentType.contains("roof tech")) {
					category = "roof tech";
					materialsToOrder.addPart(category, "base", attachmentCount);
					materialsToOrder.addPart(category, "bolt", attachmentCount);
					if (attachmentType.contains("rafter"))
						materialsToOrder.addPart(category, "screw", attachmentCount * 2);
					else
						materialsToOrder.addPart(category, "screw", attachmentCount * 5);
					materialsToOrder.addPart(category, "LFoot", attachmentCount);
				}
				if (attachmentType.contains("curb")) {
					category = "curb attachment";
					materialsToOrder.addPart(category, "curb kit", attachmentCount);
					materialsToOrder.addPart(category, "standoff", attachmentCount);
					materialsToOrder.addPart(category, "LFoot", attachmentCount);
					materialsToOrder.addPart(category, "Lag Screw", attachmentCount * 2);

					if (tiltLeg > 0)
						materialsToOrder.addPart(category, tiltLeg + "\" tilt leg kit", 10000);
				}
				if (attachmentType.contains("s5")) {
					category = "S5";
					materialsToOrder.addPart(category, "S5", attachmentCount);
					materialsToOrder.addPart(category, "Lfoot", attachmentCount);
				}
			}
			if (ballastedPanelCount > 0) {
				if (ballastRackingType1.contains("ddome"))
					addDDomeParts(ballastLayout1, b1Extra);
				if (ballastRackingType1.contains("ecofoot2"))
					addEco2Parts(ballastLayout1, b1Extra, ballastRackingType1);
				if (ballastRackingType1.contains("ecofoot5"))
					addEco5DParts(ballastLayout1, b1Extra);

				if (ballastRackingType2.contains("ddome"))
					addDDomeParts(ballastLayout2, b2Extra);
				if (ballastRackingType2.contains("ecofoot2"))
					addEco2Parts(ballastLayout2, b2Extra, ballastRackingType2);
				if (ballastRackingType2.contains("ecofoot5"))
					addEco5DParts(ballastLayout2, b2Extra);
			}
		}
		displayPartsList();
	}

	private static void displayPartsList() throws FileNotFoundException {

		String custInfo = formatCustomerInfo();
		String partsOrderLong = materialsToOrder.partListReport(true, systemWattage);
		String partsOrderShort = materialsToOrder.partListReport(false, systemWattage);

		if (showExpectedCost)
			System.out.println("\n\n" + custInfo + partsOrderLong);
		else
			System.out.println("\n\n" + custInfo + partsOrderShort);

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		DateTimeFormatter dtfFileName = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		LocalDateTime now = LocalDateTime.now();

		File file = new File("Order_History/");
		if (!file.isDirectory()) {
			file.mkdir();
		}

		String fileName = "Order_History/" + dtfFileName.format(now) + "_"
				+ customerName.trim().replace(" ", "_") + ".txt";
		PrintWriter out = new PrintWriter(fileName);
		out.println("Solar Parts Order Generated on " + dtf.format(now) + "\n" + custInfo
				+ partsOrderShort);
		out.close();
		out = new PrintWriter(fileName.replace(".txt", "_Cost.txt"));
		out.println("Solar Parts Order Generated on " + dtf.format(now) + "\n" + custInfo
				+ partsOrderLong);
		out.close();

	}

	private static String formatCustomerInfo() {
		int spacing = 25;
		return format("Project Name:", customerName, spacing) + "\n"
				+ format("Lead Source:", projectType, spacing) + "\n"
				+ format("Estimated Delivery:", deliveryDate, spacing) + "\n"
				+ format("Address:", address, spacing) + "\n";
	}

	public static String format(String a, String b, int spacing) {
		if (a.length() > spacing)
			a = a.substring(0, spacing - 3) + ".";

		String s = "";
		for (int i = 0; i < spacing - a.length(); i++)
			s += " ";
		return a + s + b;
	}

	private static void addEco5DParts(boolean[][] layout, int extra) {

		String category = "EcoFoot5D";
		int base = 0;
		int clamp = 0;
		int deflector = 0;
		int ballastTray = 0;
		int midSupport = 0;
		int groundLug = 0;
		int panelClip = 0;
		int panelCount = 0;

		for (int i = 0; i < layout.length; i++) {
			for (int j = 0; j < layout[0].length; j++) {

				if (layout[i][j]) {

					panelClip++;
					panelCount++;
					deflector++;
					midSupport++;
					ballastTray++;

					boolean isLeft = (j != 0 && layout[i][j - 1]);
					boolean isTop = (i != 0 && layout[i - 1][j]);

					if (isLeft && isTop) {
						base += 1;
						clamp += 2;
					} else if (isTop && !isLeft) {
						base += 2;
						clamp += 4;
					} else if (isLeft && !isTop) {
						ballastTray++;
						base += 2;
						clamp += 2;
					} else if (!isLeft && !isTop) {
						ballastTray++;
						base += 4;
						clamp += 4;
						groundLug += 1;
					}
				}
			}
		}

		int extraFactor = 1;

		base = (int) (Math.round(base * extraFactor) + extra);
		clamp = (int) (Math.round(clamp * extraFactor) + extra);
		deflector = (int) (Math.round(deflector * extraFactor) + extra);
		ballastTray = (int) (Math.round(ballastTray * extraFactor) + extra);
		panelClip = (int) (Math.round(panelClip * extraFactor) + extra);
		groundLug = (int) (Math.round(groundLug * extraFactor) + extra);
		midSupport = ballastTray;

		if (panelCount == 0)
			return;

		materialsToOrder.addPart(category, "clip", panelClip);
		materialsToOrder.addPart(category, "base", base);
		materialsToOrder.addPart(category, "clamp", clamp);
		materialsToOrder.addPart(category, "deflector", deflector);
		materialsToOrder.addPart(category, "tray", ballastTray);
		materialsToOrder.addPart(category, "mid support", midSupport);
		materialsToOrder.addPart(category, "ground lug", groundLug);

	}

	private static void addEco2Parts(boolean[][] layout, int extra, String orientation) {

		String category = "ecofoot2+";
		int base = 0;
		int clamp = 0;
		int deflector = 0;
		int groundLug = 0;
		int panelClip = 0;
		int panelCount = 0;

		for (int i = 0; i < layout.length; i++) {
			for (int j = 0; j < layout[0].length; j++) {

				if (layout[i][j]) {

					panelClip++;
					panelCount++;
					deflector++;

					boolean isLeft = (j != 0 && layout[i][j - 1]);
					boolean isTop = (i != 0 && layout[i - 1][j]);

					if (isLeft && isTop) {
						base += 1;
						clamp += 2;
					} else if (isTop && !isLeft) {
						base += 2;
						clamp += 4;
						groundLug += 1;
					} else if (isLeft && !isTop) {
						base += 2;
						clamp += 2;
					} else if (!isLeft && !isTop) {
						base += 4;
						clamp += 4;
						groundLug += 1;
					}
				}
			}
		}

		int extraFactor = 1;

		base = (int) (Math.round(base * extraFactor) + extra);
		clamp = (int) (Math.round(clamp * extraFactor) + extra);
		deflector = (int) (Math.round(deflector * extraFactor) + extra);
		panelClip = (int) (Math.round(panelClip * extraFactor) + extra);
		groundLug = (int) (Math.round(groundLug * extraFactor) + extra);

		if (panelCount == 0)
			return;

		materialsToOrder.addPart(category, "clip", panelClip);
		materialsToOrder.addPart(category, "base", base);
		materialsToOrder.addPart(category, "clamp", clamp);
		if (orientation.contains("landscape"))
			materialsToOrder.addPart(category, "deflector landscape", deflector);
		else
			materialsToOrder.addPart(category, "deflector portrait", deflector);
		materialsToOrder.addPart(category, "ground lug", groundLug);
	}

	public static void addDDomeParts(boolean[][] layout, int extraCount) {

		String category = "DDome";

		int panelCount = 0;
		int peak = 0;
		int base = 0;
		int protectionMat = 0;
		int groundLug = 0;
		int endClamp = 0;
		int midClamp = 0;
		int cornerStrutKit = 0;
		int ballastPorter = 0;
		int spacerMat = 4;
		int panelClip = 0;

		for (int i = 0; i < layout.length; i += 2) {
			for (int j = 0; j < layout[0].length; j++) {

				if (layout[i][j]) {

					ballastPorter += 2;
					panelClip += 2;
					panelCount += 2;

					boolean isLeft = (j != 0 && layout[i][j - 1]);
					boolean isTop = (i != 0 && layout[i - 1][j]);

					if (isLeft && isTop) {
						base += 1;
						midClamp += 4;
						peak += 1;
						cornerStrutKit += 2;
					} else if (!isLeft && isTop) {
						base += 2;
						endClamp += 8;
						peak += 2;
						cornerStrutKit += 4;
					} else if (isLeft && !isTop) {
						base += 2;
						midClamp += 4;
						peak += 1;
						cornerStrutKit += 2;
					} else if (!isLeft && !isTop) {
						groundLug += 2;
						base += 4;
						endClamp += 8;
						peak += 2;
						cornerStrutKit += 4;
					}
				}
			}
		}

		protectionMat = peak + base;

		if (panelCount == 0)
			return;

		int extraFactor = 1;
		extraCount = 0;

		peak = (int) (Math.round(peak * extraFactor) + extraCount);
		base = (int) (Math.round(base * extraFactor) + extraCount);
		protectionMat = (int) (Math.round(protectionMat * extraFactor) + extraCount);
		groundLug = (int) (Math.round(groundLug * extraFactor) + extraCount);
		endClamp = (int) (Math.round(endClamp * extraFactor) + extraCount);
		midClamp = (int) Math.round(midClamp * extraFactor) + extraCount;
		cornerStrutKit = (int) Math.round(cornerStrutKit * extraFactor) + extraCount;
		ballastPorter = (int) Math.round(ballastPorter * extraFactor) + extraCount;
		panelClip = (int) Math.round(panelClip * extraFactor) + extraCount;

		String clampSize = "DDome clamp incompatible with panel width";
		if (panelWidth > 32 && panelWidth < 34) {
			clampSize = "33";
		} else if (panelWidth > 39 && panelWidth < 42) {
			clampSize = "40";
		}

		materialsToOrder.addPart(category, "clip", panelClip);
		materialsToOrder.addPart(category, "peak", peak);
		materialsToOrder.addPart(category, "base", base);
		materialsToOrder.addPart(category, "mat", protectionMat);
		materialsToOrder.addPart(category, "spacer pad", spacerMat);
		materialsToOrder.addPart(category, "mid " + clampSize, midClamp);
		materialsToOrder.addPart(category, "end " + clampSize, endClamp);
		materialsToOrder.addPart(category, "ballast porter", ballastPorter);
		materialsToOrder.addPart(category, "corner strut", cornerStrutKit);
		materialsToOrder.addPart(category, "ground lug", groundLug);
		materialsToOrder.addPart(category, "weeb", endClamp + midClamp);

	}
}
