/* store tab activation */
function imgGift(){
	var gift = document.getElementById("gift");
	var couple = document.getElementById("couple");
	var party = document.getElementById("party");
	var giftfig = document.getElementById("giftfig");
	var couplefig = document.getElementById("couplefig");
	var partyfig = document.getElementById("partyfig");
	var giftProd = document.getElementsByClassName("GIFT");
	console.log(giftProd);
	var coupleProd = document.getElementsByClassName("COUPLE");
	var partyProd = document.getElementsByClassName("PARTY");
	var i;
	gift.src="/img/imgCommon/gift_color_163.png";
	giftfig.style.color = "#F247b8";
	couple.src="/img/imgCommon/couple_line_163.png";
	couplefig.style.color = "#000";
	party.src="/img/imgCommon/party_line_163.png";
	partyfig.style.color = "#000";
	for(i=0;i<giftProd.length;i++){
		giftProd[i].parentElement.style.display="inline-block";
	}
	for(i=0;i<coupleProd.length;i++){
		coupleProd[i].parentElement.style.display="none";
	}
	for(i=0;i<partyProd.length;i++){
		partyProd[i].parentElement.style.display="none";
	}
}

function imgCouple(){
	var gift = document.getElementById("gift");
	var couple = document.getElementById("couple");
	var party = document.getElementById("party");
	var giftfig = document.getElementById("giftfig");
	var couplefig = document.getElementById("couplefig");
	var partyfig = document.getElementById("partyfig");
	var giftProd = document.getElementsByClassName("GIFT");
	var coupleProd = document.getElementsByClassName("COUPLE");
	var partyProd = document.getElementsByClassName("PARTY");
	console.log(partyProd);
	gift.src="/img/imgCommon/gift_line_163.png";
	giftfig.style.color = "#000";
	couple.src="/img/imgCommon/couple_color_163.png";
	couplefig.style.color = "#F247b8";
	party.src="/img/imgCommon/party_line_163.png";
	partyfig.style.color = "#000";
	var i;
	for(i=0;i<giftProd.length;i++){
		giftProd[i].parentElement.style.display="none";
	}
	for(i=0;i<coupleProd.length;i++){
		coupleProd[i].parentElement.style.display="inline-block";
	}
	for(i=0;i<partyProd.length;i++){
		partyProd[i].parentElement.style.display="none";
	}
}

function imgParty(){
	var gift = document.getElementById("gift");
	var couple = document.getElementById("couple");
	var party = document.getElementById("party");
	var giftfig = document.getElementById("giftfig");
	var couplefig = document.getElementById("couplefig");
	var partyfig = document.getElementById("partyfig");
	var giftProd = document.getElementsByClassName("GIFT");
	var coupleProd = document.getElementsByClassName("COUPLE");
	var partyProd = document.getElementsByClassName("PARTY");
	console.log(partyProd);
	gift.src="/img/imgCommon/gift_line_163.png";
	giftfig.style.color = "#000";
	couple.src="/img/imgCommon/couple_line_163.png";
	couplefig.style.color = "#000";
	party.src="/img/imgCommon/party_color_163.png";
	partyfig.style.color = "#F247b8";
	for(i=0;i<giftProd.length;i++){
		giftProd[i].parentElement.style.display="none";
	}
	for(i=0;i<coupleProd.length;i++){
		coupleProd[i].parentElement.style.display="none";
	}
	for(i=0;i<partyProd.length;i++){
		partyProd[i].parentElement.style.display="inline-block";
	}
}