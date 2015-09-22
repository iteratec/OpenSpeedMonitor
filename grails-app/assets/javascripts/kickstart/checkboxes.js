if (typeof jQuery !== 'undefined') {
	$(document).ready(function() {
		/**
		 * A checkbox using Bootstrap CSS's radio buttons
		 */
		$('.radiocheckbox .btn.on').click(function() {
			var $checkbox = $(this).parent().siblings("input[type=checkbox]")
			$checkbox.attr('checked', true);
			$(this).addClass("btn-primary")
			$(this).siblings().removeClass("btn-primary")
		})
		$('.radiocheckbox .btn.off').click(function() {
			var $checkbox = $(this).parent().siblings("input[type=checkbox]")
			$checkbox.attr('checked', false);
			$(this).addClass("btn-primary")
			$(this).siblings().removeClass("btn-primary")
		})
		
		/**
		 * A checkbox using Bootstrap CSS's buttons
		 */
		$('.buttoncheckbox').click(function() {
			var $text = $(this).text()
			$(this).text($(this).attr('data-complete-text'))
			$(this).attr('data-complete-text', $text)
			$(this).toggleClass("btn-primary")
			$(this).removeClass("active")
			var $checkbox = $(this).siblings("input[type=checkbox]")
			$checkbox.attr('checked', !$checkbox.attr('checked'));
		})
	});
}
