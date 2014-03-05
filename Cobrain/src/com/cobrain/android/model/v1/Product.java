package com.cobrain.android.model.v1;

import java.text.NumberFormat;

public class Product {
	int id;
	String name;
	int price;
	Integer salePrice;
	String buyURL;
	String imageURL;
	Merchant merchant;
	int rank;
	private String salePriceFormatted;
	private String priceFormatted;
	boolean onSale;
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getPrice() {
		return price;
	}
	public Integer getSalePrice() {
		return salePrice;
	}
	public String getBuyURL() {
		return buyURL;
	}
	public String getImageURL() {
		return imageURL;
	}
	public Merchant getMerchant() {
		return merchant;
	}
	public int getRank() {
		return rank;
	}

	public CharSequence getPriceFormatted() {
		if (priceFormatted == null)
			priceFormatted = formatPrice(price);
		return priceFormatted;
	}

	public CharSequence getSalePriceFormatted() {
		if (salePriceFormatted == null)
			salePriceFormatted = formatPrice(salePrice);
		return salePriceFormatted;
	}

	String formatPrice(Integer price) {
		if (price == null) {
			return "N/A";
		}
		else {
			NumberFormat formatter = NumberFormat.getCurrencyInstance();
			double amt = price / (double)100;
			return formatter.format(amt);
		}
	}

	public void setSalePrice(int price) {
		salePrice = price;
	}
	public String getPriceLabel() {
		if (isOnSale()) {
			return formatPrice(salePrice);
		}
		else return formatPrice(price);
	}
	public boolean isOnSale() {
		return /* FIXME: onSale == true ||*/ (salePrice != null && salePrice > 0 && salePrice < price);
	}

	
	/*
	{
	  "products": [
	 
	        {
	            "id": 506366638,
	            "name": "Nautica Men's Sleepwear, Anchor Pajama Shorts",
	            "price": 3000,
	            "salePrice": 3000,
	            "buyURL": "http://click.linksynergy.com/link?id=J7fouUe6AiE&offerid=206960.731517856070&type=15&murl=http%3A%2F%2Fwww1.macys.com%2Fshop%2Fproduct%2Fnautica-mens-sleepwear-anchor-pajama-shorts%3FID%3D534297%26PartnerID%3DLINKSHARE%26cm_mmc%3DLINKSHARE-_-5-_-58-_-MP558",
	            "detailURL": "http://click.linksynergy.com/link?id=J7fouUe6AiE&offerid=206960.731517856070&type=15&murl=http%3A%2F%2Fwww1.macys.com%2Fshop%2Fproduct%2Fnautica-mens-sleepwear-anchor-pajama-shorts%3FID%3D534297%26PartnerID%3DLINKSHARE%26cm_mmc%3DLINKSHARE-_-5-_-58-_-MP558",
	            "imageURL": "http://slimages.macys.com/is/image/MCY/products/6/optimized/872046_fpx.tif?wid=300&fmt=jpeg&qlt=100",
	            "merchant": {
	                "name": "Macy's",
	                "url": "http://www.macys.com/"
	            },
	            "rank": 1
	        },
	        {
	            "id": 506391088,
	            "name": "Cutter & Buck Pleated Drytec Shorts",
	            "price": null,
	            "salePrice": null,
	            "buyURL": "http://www.avantlink.com/click.php?p=145609&pw=132915&pt=3&pri=959&tt=df",
	            "detailURL": null,
	            "imageURL": "http://i1.avlws.com/2169/l959.png",
	            "merchant": {
	                "name": "Patrick James",
	                "url": "http://patrickjames.com/Default.aspx"
	            },
	            "rank": 2
	        },
	        {
	            "id": 506331682,
	            "name": "Polo Ralph Lauren Shorts, Vintage Chino Pleated Tyler Short",
	            "price": 3499,
	            "salePrice": 2799,
	            "buyURL": "http://click.linksynergy.com/link?id=J7fouUe6AiE&offerid=206960.887436399065&type=15&murl=http%3A%2F%2Fwww1.macys.com%2Fshop%2Fproduct%2Fpolo-ralph-lauren-shorts-vintage-chino-pleated-tyler-short%3FID%3D812300%26PartnerID%3DLINKSHARE%26cm_mmc%3DLINKSHARE-_-5-_-62-_-MP562",
	            "detailURL": "http://click.linksynergy.com/link?id=J7fouUe6AiE&offerid=206960.887436399065&type=15&murl=http%3A%2F%2Fwww1.macys.com%2Fshop%2Fproduct%2Fpolo-ralph-lauren-shorts-vintage-chino-pleated-tyler-short%3FID%3D812300%26PartnerID%3DLINKSHARE%26cm_mmc%3DLINKSHARE-_-5-_-62-_-MP562",
	            "imageURL": "http://slimages.macys.com/is/image/MCY/products/3/optimized/1510724_fpx.tif?wid=300&fmt=jpeg&qlt=100",
	            "merchant": {
	                "name": "Macy's",
	                "url": "http://www.macys.com/"
	            },
	            "rank": 3
	        }
	    ],
	    "page": 1,
	    "perPage": 3,
	    "count": 3,
	    "total": 78
	}
	*/
}
