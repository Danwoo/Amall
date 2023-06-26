$("#chkAll").click(function() {
	if ($('#chkAll').prop('checked')) {
		$("input[name=chk]").attr("checked", "checked");
		$("input[name=chk]").prop("checked", true);
	} else {
		$("input[name=chk]").removeAttr("checked", "");
		$("input[name=chk]").prop("checked", false);
	}
	$("input[name=chk]").click(function() {
		if ($("input[name=chk]").length == $("input[name=chk]:checked").length) {
			$("#chkAll").attr("checked", "checked");
			$("#chkAll").prop("checked", true);
		} else {
			$("#chkAll").removeAttr("checked", "");
			$("#chkAll").prop("checked", false);
		}
	});
});