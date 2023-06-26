/*썸네일 이미지 교체*/ 
function updatePhotoOnClick() {
  var photo = document.getElementById("productPhoto");
  var thumbnail = document.querySelectorAll("#gallery > li > img");

  for (var i = 0; i < thumbnail.length; i++) {
    thumbnail[i].onclick = function() {
      photo.setAttribute("src", this.getAttribute("src"));
    };
  }
}

$(function(){
	$("#gallery>li>img").click(function(){
		$("#gallery>li>img").css("border","none");
		$(this).css("border","1px solid black");
	});
});

/*후기 scroll*/
function scrollToQA() {
  const div = document.getElementById("QA");
  const rect = div.getBoundingClientRect();
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  window.scrollTo({
    top: rect.top + scrollTop,
    behavior: "smooth"
  });
}
function scrollToDetail() {
  const div = document.getElementById("detail");
  const rect = div.getBoundingClientRect();
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  window.scrollTo({
    top: rect.top + scrollTop,
    behavior: "smooth"
  });
}
function scrollToReview() {
  const div = document.getElementById("review");
  const rect = div.getBoundingClientRect();
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  window.scrollTo({
    top: rect.top + scrollTop,
    behavior: "smooth"
  });
}
function scrollToPurchaseDetail() {
  const div = document.getElementById("purchaseDetail");
  const rect = div.getBoundingClientRect();
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  window.scrollTo({
    top: rect.top + scrollTop,
    behavior: "smooth"
  });
}