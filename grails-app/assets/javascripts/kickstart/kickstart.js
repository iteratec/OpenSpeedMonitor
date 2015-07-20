if (typeof jQuery !== 'undefined') {
	$(document).ready(function() {
		/**
		 * Activate Datepicker for Bootstrap
		 */
		//$(".date").datepicker();

		/**
		 * Close Dropdown menus when user clicks outside a menu (on the body)
		 */
		$("body").bind("click", function (e) {
			$('.menu').parent("li").removeClass("open");
		});

		/**
		 * Toggle Dropdown menus when user clicks on the menu's "switch"
		 */
		$(".dropdown-toggle, .menu").click(function (e) {
			var $li = $(this).parent("li").toggleClass('open');
			return false;
		});

		/**y
		 * Close other Dropdown menus that are open when user opens a menu
		 */
	    $('.dropdown-toggle').each(function(){
	        $(this).on("click", function () {
	        	$(this).parent().parent().siblings().each(function(){
	        		$(this).find('.dropdown').removeClass('open');
	        	});
	        });
	    });

	});
}
