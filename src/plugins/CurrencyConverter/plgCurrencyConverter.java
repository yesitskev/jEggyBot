// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								CURRENCY CONVERTER
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package plugins.CurrencyConverter;

import jeggybot.*;

import jeggybot.util.CachedResult;
import jeggybot.util.Utils;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;

import java.text.NumberFormat;
import java.text.DecimalFormat;

public class plgCurrencyConverter extends Plugin
{	
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private static HashMap<String, String> currencies;		// list of currency codes
	private HashMap<Object, CachedResult> results;			// cached results
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************
    
	public plgCurrencyConverter()
	{
		// initialisation
		results = new HashMap<Object, CachedResult>();
		// set plugin information
		name = "Currency Converter";
		author = "Matthew Goslett";
		version = "1.0";
		// register plugin commands
		ph.register("!cc", "cmdCC", ACCESS.GLOBAL, LEVEL.USER, this);
		ph.register("!ccsearch", "cmdCCSearch", ACCESS.GLOBAL, LEVEL.USER, this);
	}
    
    static
    {
		// initialisation
		currencies = new HashMap<String, String>();
		// load currency codes into vector
		currencies.put("afghanistan", "AFN");
		currencies.put("albania", "ALL");
		currencies.put("algeria", "DZD");
		currencies.put("america", "USD");
		currencies.put("argentina", "ARS");
		currencies.put("australia", "AUD");
		currencies.put("austria", "EUR");
		currencies.put("bahamas", "BSD");
		currencies.put("bahrain", "BHD");
		currencies.put("bangladesh", "BDT");
		currencies.put("barbados", "BBD");
		currencies.put("belgium", "EUR");
		currencies.put("bermuda", "BMD");
		currencies.put("brazil", "BRL");
		currencies.put("bulgaria", "BGN");
		currencies.put("canada", "CAD");
		currencies.put("chile", "CLP");
		currencies.put("china", "CNY");
		currencies.put("colombia", "COP");
		currencies.put("costa rica", "CRC");
		currencies.put("croatia", "HRK");
		currencies.put("cyprus", "CYP");
		currencies.put("czech republic", "CZK");
		currencies.put("denmark", "DKK");
		currencies.put("dominican republic", "DOP");
		currencies.put("eastern caribbean", "XCD");
		currencies.put("egypt", "EGP");
		currencies.put("estonia", "EEK");
		currencies.put("fiji", "FJD");
		currencies.put("finland", "EUR");
		currencies.put("france", "EUR");
		currencies.put("germany", "EUR");
		currencies.put("greece", "EUR");
		currencies.put("holland", "EUR");
		currencies.put("hong kong", "HKD");
		currencies.put("hungary", "HUF");
		currencies.put("iceland", "ISK");
		currencies.put("india", "INR");
		currencies.put("indonesia", "IDR");
		currencies.put("iran", "IRR");
		currencies.put("iraq", "IQD");
		currencies.put("ireland", "EUR");
		currencies.put("israel", "ILS");
		currencies.put("italy", "EUR");
		currencies.put("jamaica", "JMD");
		currencies.put("japan", "JPY");
		currencies.put("jordan", "JOD");
		currencies.put("kenya", "KES");
		currencies.put("korea", "KRW");
		currencies.put("kuwait", "KWD");
		currencies.put("lebanon", "LBP");
		currencies.put("luxembourg", "EUR");
		currencies.put("malaysia", "MYR");
		currencies.put("malta", "MTL");
		currencies.put("mauritius", "MUR");
		currencies.put("mexico", "MXN");
		currencies.put("morocco", "MAD");
		currencies.put("netherlands", "EUR");
		currencies.put("new zealand", "EUR");
		currencies.put("norway", "NOK");
		currencies.put("oman", "OMR");
		currencies.put("pakistan", "PKR");
		currencies.put("peru", "PEN");
		currencies.put("philippines", "PHP");
		currencies.put("poland", "PLN");
		currencies.put("portugal", "EUR");
		currencies.put("qatar", "QAR");
		currencies.put("romania", "RON");
		currencies.put("russia", "RUB");
		currencies.put("saudi arabia", "SAR");
		currencies.put("singapore", "SGD");
		currencies.put("slovakia", "SKK");
		currencies.put("slovenia", "SIT");
		currencies.put("south africa", "ZAR");
		currencies.put("south korea", "KRW");
		currencies.put("spain", "EUR");
		currencies.put("sri lanka", "LKR");
		currencies.put("sudan", "SDD");
		currencies.put("sweden", "SEK");
		currencies.put("switzerland", "CHF");
		currencies.put("taiwan", "TWD");
		currencies.put("thailand", "THB");
		currencies.put("trinidad and tobago", "TTD");
		currencies.put("tunisia", "TND");
		currencies.put("turkey", "TRY");
		currencies.put("united arab emirates", "AED");
		currencies.put("united kingdom", "GBP");
		currencies.put("united states", "USD");
		currencies.put("venezuela", "VEB");
		currencies.put("vietnam", "VND");
		currencies.put("zambia", "ZMK");
    }

	// ********************************************************************************
	//			PRIVATE METHODS
	// ********************************************************************************
    
    private boolean isCurrency(String currency)
    {
    	return currencies.containsValue(currency.toUpperCase());
    }
    
    private CurrencyFetcher getCurrencyFetcher(String currency1, String currency2)
    {
    	// define result key
    	String key = (currency1 + currency2).toLowerCase();
    	// determine if result is in cache list
    	if (results.containsKey(key)) {
    		// check if result is older than 40 minutes
    		CachedResult cr = results.get(key);
    		if (cr.getTimeDifference() < 2400) {
    			// return cached result
    			return (CurrencyFetcher) cr.getResult();
    		}
    		else {
    			// remove result from cache list
    			results.remove(key);
    		}
    	}
		// fetch new result
    	try {
    		// get conversion rate from xe.com
    		CurrencyFetcher cf = new CurrencyFetcher(currency1, currency2);
			// cache result
			results.put(key, new CachedResult(key, cf));
			return cf;
    	}
    	catch (IOException e) {
    		return null;
    	}
    }
    
	// ********************************************************************************
	//			BOT COMMANDS
	// ********************************************************************************
    
    public void cmdCC(final MessageInfo m, final String[] p)
    {
		new Thread() {
			public void run() {
		    	if (p.length == 3) {
		    		double amount;
		    		String currency1 = p[1];
		    		String currency2 = p[2];
		    		try {
		    			amount = Double.parseDouble(p[0]);
		    		}
		    		catch (NumberFormatException e) {
		    			bot.notice(m.getNickname(), "You must enter a valid currency amount");
		    			return;
		    		}
		    		if ((amount >= 0) && (amount <= 999000000)) {
			    		if ((isCurrency(currency1)) && (isCurrency(currency2))) {
							CurrencyFetcher cf = getCurrencyFetcher(currency1, currency2);
							if (cf != null) {
								if (cf.isValid()) {
									// calculate converted amount
									double total = amount * cf.getRate();
									// round figures off to 4 decimal places
									NumberFormat nf = new DecimalFormat("#.#####");
									// message response
									bot.notice(m.getNickname(), nf.format(amount) + " " + cf.getCurrency1() + " = " + nf.format(total) + " " + cf.getCurrency2() + "");
								}
								else {
									bot.notice(m.getNickname(), "I was unable to perform this currency conversion.");
								}
							}
							else {
								bot.notice(m.getNickname(), "I was unable to perform this currency conversion.");
							}
			    		}
			    		else {
			    			bot.notice(m.getNickname(), "You have entered an invalid currency code(s)");
			    			bot.notice(m.getNickname(), "Type !ccsearch (country) to obtain a specific country's currency code");
			    		}
		    		}
		    		else {
		    			bot.notice(m.getNickname(), "You must enter an amount between 0 and 999000000");
		    		}
		    	}
		    	else {
		    		bot.notice(m.getNickname(), "Syntax: !cc (amount) (original currency) (target currency)");
		    	}
			}
		}.start();
    }
    
    public void cmdCCSearch(final MessageInfo m, final String[] p)
    {
    	if (p.length >= 1) {
    		String country = Utils.arrayToString(p);
    		if (currencies.containsKey(country.toLowerCase())) {
    			bot.notice(m.getNickname(), "The currency code for " + country + " is " + currencies.get(country.toLowerCase()) + "");
    		}
    		else {
    			bot.notice(m.getNickname(), "I do not know the currency code for " + country + "");
    		}
    	}
    	else {
    		bot.notice(m.getNickname(), "Syntax: !ccsearch (country)");
    	}
    }
}