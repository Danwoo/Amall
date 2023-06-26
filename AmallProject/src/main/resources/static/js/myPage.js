function openUpdateProfileModal() {
	window.scrollTo(0, 0);
	document.querySelector(".checkForGoUpdateProfileModal").style.display = "block";
	document.body.style.overflow = "hidden";
}
function closeUpdateProfileModal() {
	document.querySelector(".checkForGoUpdateProfileModal").style.display = "none";
	document.body.style.removeProperty("overflow");
}