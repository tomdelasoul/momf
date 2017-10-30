/**
 * KOL: retrieve historic stpck prices from finanzen.net
 * 
 * only works in safari (x-site validation off), issue in chrome
 */

$(document).ready(function() {
	// 
	// we have to get a key from holedaten.asp first and post that key
	$.get('http://www.finanzen.net/holedaten.asp?strFrag=vdBHTSService',
		function(secretKey) {
		// "BMW"
		// "Deutsche_Telekom"
		// "Deutsche_Bank"
		// "IMMOFINANZ/Wien"
		var aktie = "BMW";
		//
		$.ajax({
			url : "http://www.finanzen.net/historische-kurse/"+aktie,
			data : {
				inTag1 : 1,
				inMonat1 : 1,
				inJahr1 : 2017, // 2012
				inTag2 : 1,
				inMonat2 : 9,
				inJahr2 : 2017,
				strBoerse : "XETRA",
				pkBHTs : secretKey // key
			},
			type : "POST",
			dataType : "HTML",
		})
		.done(function(html) {
			// console.log("put done");
			// console.log(html);
			// now find the historic price table
			var priceTableBegin = '<tr><th>Datum</th><th>Er&ouml;ffnung</th><th>Schluss</th><th>Tageshoch</th><th>Tagestief</th>';
			var priceTableEnd = '</table></div></div><div class="spacer-20"></div></div><div class="col-sm-4"><!--CenterColumn_4-->';
			if((from = html.indexOf(priceTableBegin)) != -1){
			    // console.log(priceTableBegin + " begin at: "+html.indexOf(priceTableBegin));
			}			
			if((to = html.indexOf(priceTableEnd)) != -1) {
			    // console.log(priceTableEnd + " end at: "+html.indexOf(priceTableEnd));
			}
			table = html.substring(from, to);
			// insert table from html substring
			$("#results").append(table);
			$("#header").append(aktie);
			if(false) {
				// log all rows & cells of table to java console
			    $("#results tr").each(function(row) {
			    	var rowString = "";
			        $(this).find('td').each(function(col) {
			        	rowString += ($( this ).text()+"; ");
			        });
			        console.log(rowString);
			    });
			}
		})
		.fail(function(xhr, status, errorThrown) {
			console.log("put error: " + errorThrown);
			console.log("Status: " + status);
			console.dir(xhr);
		});
	}).done(function() {
		// console.log("get done ");
	}).fail(function(xhr, status, errorThrown) {
		console.log("get error status: " + status);
		console.log("Status: " + status);
		console.dir(xhr);
	});
});
