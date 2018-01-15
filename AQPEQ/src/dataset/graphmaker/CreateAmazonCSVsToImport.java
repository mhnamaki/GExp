package dataset.graphmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class CreateAmazonCSVsToImport {

	private static String rawFile = "amazon-meta.txt";

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-rawFile")) {
				rawFile = args[++i];
			}

		}
		String path = rawFile;
		ArrayList<String> list = CreateAmazonCSVsToImport.readData(path);
		ArrayList<AmazonProduct> productList = CreateAmazonCSVsToImport.amazonProduct(list);
		long tem = CreateAmazonCSVsToImport.writeIntoCSV(productList);
		// CreateAmazonCSVsToImport.customerCSVRefine(productList);
		CreateAmazonCSVsToImport.productCSVRefine(productList, tem);
	}

	public static ArrayList<String> readData(String path) {
		ArrayList<String> dataList = new ArrayList<String>();
		File filename = new File(path);
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(filename));
			BufferedReader br = new BufferedReader(reader);
			String line = "";
			String product = "";
			int count = 0;
			while ((line = br.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					product += line + "\n";
				} else {
					dataList.add(product);
					count++;
					product = "";
					 if ((count % 100000) == 0) {
					 System.out.println(count);
					 }
//					if ((count % 10000) == 0) {
//						break;
//					}
				}
			}

			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataList;
	}

	public static ArrayList<AmazonProduct> amazonProduct(ArrayList<String> list) {
		System.out.println("AmazonProduct");
		ArrayList<AmazonProduct> productList = new ArrayList<AmazonProduct>();
		for (int i = 0; i < list.size(); i++) {
			String[] product = list.get(i).trim().split("\n");
			String id = "";
			String asin = "";
			String title = "";
			String group = "";
			String salesrank = "";
			String similar = "";
			String categoryNum = "";
			String reviewNum = "";
			String reviewAvgRating = "";
			ArrayList<String> similarPro = new ArrayList<String>();
			ArrayList<String> categoryVal = new ArrayList<String>();
			ArrayList<String> customer = new ArrayList<String>();
			ArrayList<String> reviewVal = new ArrayList<String>();
			AmazonCategory category = new AmazonCategory(categoryNum, categoryVal);
			AmazonReview review = new AmazonReview(reviewNum, reviewAvgRating, customer, reviewVal);

			HashSet<String> cList = new HashSet<String>();

			for (int j = 0; j < product.length; j++) {
				String line = product[j].trim();
				line = line.trim().toLowerCase();
				line = line.trim();
				String[] splittedValues = line.split(":");
				if (line.contains("id:")) {
					// splittedValues[0]: Id, splittedValues[1]: 0
					id = splittedValues[1].trim();
				} else if (line.contains("asin:")) {
					// splittedValues[0]: ASIN, splittedValues[1]: 0486220125
					asin = splittedValues[1].trim();
				} else if (line.contains("title:")) {
					String titleTem = "";
					for (int s = 1; s < splittedValues.length; s++) {
						titleTem += splittedValues[s].trim();
						if (s < (splittedValues.length - 1)) {
							titleTem += ": ";
						}
					}
					title = titleTem.replace(",", "#");
				} else if (line.contains("group:")) {
					group = splittedValues[1].trim();
				} else if (line.contains("salesrank:")) {
					salesrank = splittedValues[1].trim();
				} else if (line.contains("similar:")) {
					similarPro.clear();
					String[] similarValues = splittedValues[1].trim().split(" ");
					similar = similarValues[0].trim();
					int tem = Integer.parseInt(similarValues[0].trim());
					if (tem == 0) {
						similarPro.add("");
					}
					for (int s = 1; s < similarValues.length; s++) {
						if (!similarValues[s].isEmpty()) {
							similarPro.add(similarValues[s]);
						}
					}
				} else if (line.contains("categories:")) {
					categoryVal.clear();
					categoryNum = splittedValues[1].trim();
					int categoryNumTem = Integer.parseInt(splittedValues[1].trim());
					if (categoryNumTem == 0) {
						categoryVal.add("");
					} else {
						for (int s = 0; s < categoryNumTem; s++) {
							// line = br.readLine().trim();
							line = product[j + 1 + s].trim();
							String tem = line.replace(",", "#");
							categoryVal.add(tem);
						}
					}
					category = new AmazonCategory(categoryNum, categoryVal);
				} else if (line.contains("reviews:")) {
					// total: 12 downloaded: 12 avg rating: 4.5
					// reviewsValues[0]: reviews(total)
					// reviewsValues[2]: avgRating
					customer.clear();
					reviewVal.clear();
					cList.clear();
					String[] reviewsValues = splittedValues[3].trim().split(" ");
					reviewNum = reviewsValues[0].trim();
					int reviewNumTem = Integer.parseInt(reviewsValues[0].trim());
					reviewAvgRating = splittedValues[4].trim();
					if (reviewNumTem == 0) {
						reviewVal.add("");
					} else {
						for (int s = 0; s < reviewNumTem; s++) {
							line = product[j + 1 + s].trim();
							reviewVal.add(line);
							// 2001-12-16 cutomer: A11NCO6YTE4BTJ rating: 5
							// votes: 5
							// helpful: 4
							String[] reviewTem = line.split(":");
							String[] customerTem = reviewTem[1].trim().split(" ");
							customer.add(customerTem[0]);
							cList.add(customerTem[0]);
						}
					}
					review = new AmazonReview(reviewNum, reviewAvgRating, customer, reviewVal);
				} else if (line.contains("discontinued product")) {
					title = "";
					salesrank = "";
					categoryVal.add("");
					similarPro.add("");
					customer.add("");
					reviewVal.add("");
					category = new AmazonCategory(categoryNum, categoryVal);
					review = new AmazonReview(reviewNum, reviewAvgRating, customer, reviewVal);
				}
			}
			AmazonProduct amaPro = new AmazonProduct(id, asin, title, group, salesrank, similar, similarPro, category,
					review, cList);
			productList.add(amaPro);
			 if ((i % 100000) == 0) {
			 System.out.println(i);
			 }
//			if ((i % 1000) == 0) {
//				System.out.println(i);
//			}
		}
		System.out.println("finish AmazonProduct");
		return productList;
	}

	public static long writeIntoCSV(ArrayList<AmazonProduct> list) throws Exception {
		System.out.println("writing");
		File product = new File("Product.csv");
		FileOutputStream fosProduct = new FileOutputStream(product);
		BufferedWriter bwProduct = new BufferedWriter(new OutputStreamWriter(fosProduct));
		bwProduct.write("ASIN:ID,title,salesrank,similar,categories,reviews,avgRating,:LABEL\n");

		// Category Node
		File categoryF = new File("Category.csv");
		FileOutputStream fosCategory = new FileOutputStream(categoryF);
		BufferedWriter bwCategory = new BufferedWriter(new OutputStreamWriter(fosCategory));
		bwCategory.write("CategoryID:ID,:LABEL\n");

		// Customer Node
		File customerF = new File("Customer.csv");
		FileOutputStream fosCustomer = new FileOutputStream(customerF);
		BufferedWriter bwCustomer = new BufferedWriter(new OutputStreamWriter(fosCustomer));
		bwCustomer.write("CustomerID:ID,:LABEL\n");

		// Review Relation
		// For each review, we can let the rating score be the label
		File reviewF = new File("Product_Review.csv");
		FileOutputStream fosReview = new FileOutputStream(reviewF);
		BufferedWriter bwReview = new BufferedWriter(new OutputStreamWriter(fosReview));
		bwReview.write(":Start_ID,date,:End_ID,votes,helpful,:TYPE\n");

		// Similar Relation
		File similarF = new File("Product_Product.csv");
		FileOutputStream fosSimilar = new FileOutputStream(similarF);
		BufferedWriter bwSimilar = new BufferedWriter(new OutputStreamWriter(fosSimilar));
		bwSimilar.write(":Start_ID,:End_ID\n");

		// Product_Category
		File productCategory = new File("Product_Category.csv");
		FileOutputStream fosProductCategory = new FileOutputStream(productCategory);
		BufferedWriter bwProductCategory = new BufferedWriter(new OutputStreamWriter(fosProductCategory));
		bwProductCategory.write(":Start_ID,:End_ID\n");

		// generate for category.csv
		// generate for customer.csv
		HashSet<String> categorySet = new HashSet<String>();
		HashSet<String> productSet = new HashSet<String>();
		long bRank = 0;
		long sRank = 5392;
		// generate for customer.csv
		HashMap<String, HashSet<String>> customerMap = new HashMap<String, HashSet<String>>();

		for (int i = 0; i < list.size(); i++) {
			AmazonProduct pro = list.get(i);
			bwProduct.write(pro.asin + "," + pro.title + "," + pro.salesrank + "," + pro.similar + ","
					+ pro.category.categoryNum + "," + pro.review.reviewNum + "," + pro.review.reviewAvgRating + ","
					+ "Product;" + pro.group + "\n");
			productSet.add(pro.asin);
			if (pro.salesrank.equals("") || pro.salesrank.equals("0") || pro.salesrank.equals("-1")) {
				continue;
			} else {
				long tem = 0;
				if (pro.salesrank.equals("-1")) {
					tem = 0;
				} else {
					tem = Long.parseLong(pro.salesrank);
				}
				if (tem > bRank) {
					bRank = tem;
				}
				if (tem < sRank) {
					sRank = tem;
				}
			}

			// add category
			for (int j = 0; j < pro.category.categoryVal.size(); j++) {
				if (!pro.category.categoryNum.equals("") && !pro.category.categoryNum.equals("0")) {
					categorySet.add(pro.category.categoryVal.get(j));
					bwProductCategory.write(pro.asin + "," + pro.category.categoryVal.get(j) + "\n");
				}
				if (pro.asin.equals("0787951978")) {
					System.out.println(pro.category.categoryVal.get(j));
				}
			}

			// add review
			for (int n = 0; n < pro.review.reviewVal.size(); n++) {
				// reviewMap.put(asin, review.reviewVal.get(n));
				// bwReview.write(":Start_ASIN,date,:End_CustomerID,votes,helpful,:Label\n");
				String[] revTem = pro.review.reviewVal.get(n).trim().split(" ");
				String date = revTem[0];
				String customerID = "";
				Integer vote = 0;
				Integer helpful = 0;
				double rating = 0.0;
				for (int k = 0; k < revTem.length; k++) {
					int j = k;
					if (revTem[k].contains("rating")) {
						customerID = revTem[j - 2];
					} else if (revTem[k].contains("votes")) {
						rating = Double.parseDouble(revTem[j - 2]);
					} else if (revTem[k].contains("helpful")) {
						vote = Integer.parseInt(revTem[j - 2]);
						j = revTem.length - 1;
						helpful = Integer.parseInt(revTem[j]);

					}
				}
				if (customerID != "") {
					if (vote == 0) {
						bwReview.write(pro.asin + "," + date + "," + customerID + "," + vote + "," + helpful + ","
								+ rating + "\n");
					} else {
						double reliability = ((double) helpful) / vote;
						if (reliability > 0.75) {
							bwReview.write(pro.asin + "," + date + "," + customerID + "," + vote + "," + helpful + ","
									+ rating + ";high reliability" + "\n");
						} else {
							if (reliability < 0.40) {
								bwReview.write(pro.asin + "," + date + "," + customerID + "," + vote + "," + helpful
										+ "," + rating + ";low reliability" + "\n");
							} else {
								bwReview.write(pro.asin + "," + date + "," + customerID + "," + vote + "," + helpful
										+ "," + rating + ";medium reliability" + "\n");
							}
						}
					}
				}
			}

			// add customer buy product information
			// customerMap
			HashSet<String> cList = pro.cList;
			Iterator itCL = cList.iterator();
			while (itCL.hasNext()) {
				String cID = (String) itCL.next();
				customerMap.putIfAbsent(cID, new HashSet<String>());
				customerMap.get(cID).add(pro.group);
			}
		}
		// bwCategory.write(":CategoryID,:Label\n");
		// categorySet
		Iterator itCat = categorySet.iterator();
		while (itCat.hasNext()) {
			String tem = (String) itCat.next();
			if (tem.equals("")) {
				continue;
			}
			String[] categoryTemVal = tem.trim().split("\\|");
			bwCategory.write(tem + ",");
			for (int j = 0; j < categoryTemVal.length; j++) {
				if (!categoryTemVal[j].isEmpty()) {
					String s = categoryTemVal[j].replaceAll("[^A-Za-z]", "");
					if (s.equals("")) {
						continue;
					} else {
						bwCategory.write(s + ";");
					}
				}
			}
			bwCategory.write("\n");
		}

		Iterator itCus = customerMap.entrySet().iterator();
		while (itCus.hasNext()) {
			HashMap.Entry<String, HashSet<String>> entry = (java.util.Map.Entry<String, HashSet<String>>) itCus.next();
			String cID = entry.getKey();
			HashSet<String> proASIN = entry.getValue();
			bwCustomer.write(cID + ",");
			bwCustomer.write("Customer");
			Iterator itProA = proASIN.iterator();
			while (itProA.hasNext()) {
				bwCustomer.write(";" + itProA.next() + " buyer");
			}
			bwCustomer.write("\n");
		}

		for (int i = 0; i < list.size(); i++) {
			AmazonProduct pro = list.get(i);
			// bwSimilar.write(":Start_ASIN,:End_ASIN\n");
			for (int m = 0; m < pro.similarPro.size(); m++) {
				if (pro.similarPro.get(m).equals("") || !productSet.contains(pro.similarPro.get(m))) {
					continue;
				} else {
					bwSimilar.write(pro.asin + "," + pro.similarPro.get(m) + "\n");
				}
			}
		}

		bwProduct.close();
		bwSimilar.close();
		bwCategory.close();
		bwCustomer.close();
		bwProductCategory.close();
		bwReview.close();
		System.out.println("finish writing");
		long tem = (bRank - sRank) / 5;
		return tem;
	}

	public static void customerCSVRefine(ArrayList<AmazonProduct> list) throws IOException {
		System.out.println("customer refine.");
		File filename = new File("Customer.csv");
		InputStreamReader reader;

		// Customer Node
		File customerF = new File("CustomerRefine.csv");
		FileOutputStream fosCustomer = new FileOutputStream(customerF);
		BufferedWriter bwCustomer = new BufferedWriter(new OutputStreamWriter(fosCustomer));
		bwCustomer.write(":CustomerID,:LABEL\n");

		// read customer ID from Customer.SCV
		// get cList from ArrayList<AmazonProduct> list
		try {
			reader = new InputStreamReader(new FileInputStream(filename));
			BufferedReader br = new BufferedReader(reader);
			String line = "";
			ArrayList<AmazonCustomer> customerList = new ArrayList<AmazonCustomer>();
			while ((line = br.readLine()) != null) {
				String[] splittedValues = line.split(",");
				String cID = splittedValues[0];
				HashSet<String> boughtProduct = new HashSet<String>();
				for (int i = 0; i < list.size(); i++) {
					AmazonProduct proTem = list.get(i);
					String group = proTem.group.trim().toLowerCase();
					HashSet<String> cList = proTem.cList;
					// ArrayList<String> product = new ArrayList<String>();
					if (cList.contains(cID)) {
						boughtProduct.add(group);
					}
					// for (int j = 0; j < cList.size(); j++) {
					// String temID = cList.get(j);
					// if (temID.equals(cID)) {
					//
					// }
					// }
				}
				if (cID.contains("Cus")) {
					continue;
				} else {
					bwCustomer.write(cID + ",");
					bwCustomer.write("Customer");
					Iterator itP = boughtProduct.iterator();
					while (itP.hasNext()) {
						bwCustomer.write(";" + itP.next() + " buyer");
					}
					bwCustomer.write("\n");
				}
			}

			br.close();
			bwCustomer.close();
			System.out.println("finish customer refine.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void productCSVRefine(ArrayList<AmazonProduct> list, long tem) throws IOException {
		System.out.println("product refine.");
		File filename = new File("Product.csv");
		InputStreamReader reader;

		// Product Node
		File product = new File("ProductRefine.csv");
		FileOutputStream fosProduct = new FileOutputStream(product);
		BufferedWriter bwProduct = new BufferedWriter(new OutputStreamWriter(fosProduct));
		bwProduct.write("ASIN:ID,title,salesrank,salesgrade,similar,categories,reviews,avgRating,:LABEL\n");

		// add salesrank ranking
		try {
			reader = new InputStreamReader(new FileInputStream(filename));
			BufferedReader br = new BufferedReader(reader);
			String line = "";

			while ((line = br.readLine()) != null) {
				if (line.contains("ASIN")) {
					line = br.readLine();
				}
				String[] splittedValues = line.split(",");
				String asin = splittedValues[0];
				String title = splittedValues[1].replace("\"", "").replace("'", "");
				String salesrank = splittedValues[2];
				String similar = splittedValues[3];
				String category = splittedValues[4];
				String review = splittedValues[5];
				String avgRating = splittedValues[6];
				String label = splittedValues[7];
				if (salesrank.contains("salesrank")) {
					continue;
				} else {
					if (salesrank.equals("") || salesrank.equals("0") || salesrank.equals("-1")) {
						bwProduct.write(asin + "," + title + "," + salesrank + ", ," + similar + "," + category + ","
								+ review + "," + avgRating + "," + label + "\n");
					} else {
					//	System.out.println(salesrank);
						long proRank = Long.parseLong(salesrank);
						// very high
						if (proRank < tem) {
							bwProduct.write(asin + "," + title + "," + salesrank + "," + "very high salesrank" + ","
									+ similar + "," + category + "," + review + "," + avgRating + "," + label + "\n");
						} else {
							// high
							if (proRank < (tem * 2)) {
								bwProduct.write(asin + "," + title + "," + salesrank + "," + "high salesrank" + ","
										+ similar + "," + category + "," + review + "," + avgRating + "," + label + "\n");
							} else {
								// medium
								if (proRank < (tem * 3)) {
									bwProduct.write(
											asin + "," + title + "," + salesrank + "," + "medium salesrank" + "," + similar
													+ "," + category + "," + review + "," + avgRating + "," + label + "\n");
								} else {
									// low
									if (proRank < (tem * 4)) {
										bwProduct.write(asin + "," + title + "," + salesrank + "," + "low salesrank" + ","
												+ similar + "," + category + "," + review + "," + avgRating + "," + label
												+ "\n");
									} else {
										// very low
										bwProduct.write(asin + "," + title + "," + salesrank + "," + "very low salesrank"
												+ "," + similar + "," + category + "," + review + "," + avgRating + ","
												+ label + "\n");
									}
								}
							}
						}
					}
				}
			}

			br.close();
			bwProduct.close();
			System.out.println("finish product refine");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
