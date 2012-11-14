package crs;

import java.io.IOException;
import java.util.List;

public class Challenge {
	
	/** Challenge Entry Point */
	public static void main(String[] args) throws IOException {
		
		// testing args
		args = new String[] {
			"C:\\Users\\Chris Stephenson\\git\\repository\\sortable-challenge\\data\\products.txt",
			"C:\\Users\\Chris Stephenson\\git\\repository\\sortable-challenge\\data\\listings.txt",
			"f:\fail"
		};
		
		// simple usage message
		if(args.length != 3)
		{
			System.out.println("Usage is: java [-options] " + Challenge.class.getName() + " [product file] [listing file] [output file]");
			return;
		}
		
		// load data
		List<Product> products = Product.loadProducts(args[0]);
		
		
		System.out.println("hiya");
	}
}
