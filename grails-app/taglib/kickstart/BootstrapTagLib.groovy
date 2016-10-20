package kickstart

import org.springframework.web.servlet.support.RequestContextUtils as RCU

import java.text.DateFormat;
import java.text.DateFormatSymbols
import org.springframework.context.i18n.LocaleContextHolder

class BootstrapTagLib {
	static namespace = "bs"

	def requestDataValueProcessor = null;

	def paginate = {
		attrs ->
		def writer = out
		if (attrs.total == null) {
			throwTagError("Tag [paginate] is missing required attribute [total]")
		}

		def messageSource	= grailsAttributes.messageSource
		def locale			= RCU.getLocale(request)

		def total			= attrs.int('total') ?: 0
		def action			= (attrs.action ? attrs.action : (params.action ? params.action : "list"))
		def offset			= params.int('offset') ?: 0
		def max				= params.int('max')
		def maxsteps		= (attrs.int('maxsteps') ?: 10)

		if (!offset)offset	= (attrs.int('offset') ?: 0)
		if (!max)	max		= (attrs.int('max') ?: 10)

		def linkParams = [:]
		if (attrs.params)	linkParams.putAll(attrs.params)
		linkParams.offset = offset - max
		linkParams.max = max
		if (params.sort)	linkParams.sort		= params.sort
		if (params.order)	linkParams.order	= params.order

		def linkTagAttrs = [action:action]
		if (attrs.controller)		linkTagAttrs.controller = attrs.controller
		if (attrs.id != null)		linkTagAttrs.id = attrs.id
		if (attrs.fragment != null)	linkTagAttrs.fragment = attrs.fragment
		linkTagAttrs.params = linkParams

		// determine paging variables
		def steps 		= maxsteps > 0
		int currentstep	= (offset / max) + 1
		int firststep	= 1
		int laststep	= Math.round(Math.ceil(total / max))

		// display previous link when not on firststep
		def disabledPrev = (currentstep > firststep) ? "" : "disabled"
		//		linkTagAttrs.class = 'prevLink'
		//		linkParams.offset = offset - max
		writer << "<ul>"
		writer << "<li class='prev ${disabledPrev}'>"
		writer << link(linkTagAttrs.clone()) {
			(attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
		}
		writer << "</li>"

		// display steps when steps are enabled and laststep is not firststep
		if (steps && laststep > firststep) {
			linkTagAttrs.class = 'step'

			// determine begin and endstep paging variables
			int beginstep	= currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
			int endstep		= currentstep + Math.round(maxsteps / 2) - 1
			if (beginstep < firststep) {
				beginstep = firststep
				endstep = maxsteps
			}
			if (endstep > laststep) {
				beginstep = laststep - maxsteps + 1
				if (beginstep < firststep) {
					beginstep = firststep
				}
				endstep = laststep
			}

			// display firststep link when beginstep is not firststep
			if (beginstep > firststep) {
				linkParams.offset = 0
				writer << "<li>"
				writer << link(linkTagAttrs.clone()) {firststep.toString()}
				writer << "</li>"
				writer << '<li class="disabled"><a href="#">…</a></li>'
			}

			// display paginate steps
			(beginstep..endstep).each { i ->
				if (currentstep == i) {
					writer << "<li class='active'><a href='#'>"+i.toString()+"</a></li>"
				}
				else {
					linkParams.offset = (i - 1) * max
					writer << "<li>"
					writer << link(linkTagAttrs.clone()) {i.toString()}
					writer << "</li>"
				}
			}

			// display laststep link when endstep is not laststep
			if (endstep < laststep) {
				linkParams.offset = (laststep -1) * max
				writer << '<li class="disabled"><a href="#">…</a></li>'
				writer << "<li>"
				writer << link(linkTagAttrs.clone()) { laststep.toString() }
				writer << "</li>"
			}
		}

		// display next link when not on laststep
		def disabledNext = (currentstep < laststep) ? "" : "disabled"
		linkParams.offset = (currentstep)*max
		writer << "<li class='next ${disabledNext}'>"
		writer << link(linkTagAttrs.clone()) {
			(attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
		}
		writer << "</li>"
		writer << "</ul>"
	}


	/**
	* A simple date picker that renders a date as selects.<br/>
	* This is just an initial hack - can be widely improved!
	* e.g. &lt;bs:datePicker name="myDate" value="${new Date()}" /&gt;
	*
	* @emptyTag
	*
	* @attr name REQUIRED The name of the date picker field set
	* @attr value The current value of the date picker; defaults to now if not specified
	* @attr precision The desired granularity of the date to be rendered
	* @attr noSelection A single-entry map detailing the key and value to use for the "no selection made" choice in the select box. If there is no current selection this will be shown as it is first in the list, and if submitted with this selected, the key that you provide will be submitted. Typically this will be blank.
	* @attr years A list or range of years to display, in the order specified. i.e. specify 2007..1900 for a reverse order list going back to 1900. If this attribute is not specified, a range of years from the current year - 100 to current year + 100 will be shown.
	* @attr relativeYears A range of int representing values relative to value. For example, a relativeYears of -2..7 and a value of today will render a list of 10 years starting with 2 years ago through 7 years in the future. This can be useful for things like credit card expiration dates or birthdates which should be bound relative to today.
	* @attr id the DOM element id
	* @attr disabled Makes the resulting inputs and selects to be disabled. Is treated as a Groovy Truth.
	* @attr readonly Makes the resulting inputs and selects to be made read only. Is treated as a Groovy Truth.
	*/
	Closure datePicker = { attrs ->
		def out = out // let x = x ?
		def xdefault = attrs['default']
		if (xdefault == null) {
			xdefault = new Date()
		}
		else if (xdefault.toString() != 'none') {
			if (xdefault instanceof String) {
				xdefault = DateFormat.getInstance().parse(xdefault)
			}
			else if (!(xdefault instanceof Date)) {
				throwTagError("Tag [datePicker] requires the default date to be a parseable String or a Date")
			}
		}
		else {
			xdefault = null
		}
		def years = attrs.years
		def relativeYears = attrs.relativeYears
		if (years != null && relativeYears != null) {
			throwTagError 'Tag [datePicker] does not allow both the years and relativeYears attributes to be used together.'
		}

		if (relativeYears != null) {
			if (!(relativeYears instanceof IntRange)) {
				// allow for a syntax like relativeYears="[-2..5]". The value there is a List containing an IntRage.
				if ((!(relativeYears instanceof List)) || (relativeYears.size() != 1) || (!(relativeYears[0] instanceof IntRange))){
					throwTagError 'The [datePicker] relativeYears attribute must be a range of int.'
				}
				relativeYears = relativeYears[0]
			}
		}
		def value = attrs.value
		if (value.toString() == 'none') {
			value = null
		} else if (!value) {
			value = xdefault
		}
		def name = attrs.name
		def id = attrs.id ?: name

		def noSelection = attrs.noSelection
		if (noSelection != null) {
			noSelection = noSelection.entrySet().iterator().next()
		}

		final PRECISION_RANKINGS = ["year": 0, "month": 10, "day": 20, "hour": 30, "minute": 40]
		def precision = (attrs.precision ? PRECISION_RANKINGS[attrs.precision] :
			(grailsApplication.config.grails.tags.datePicker.default.precision ?
				PRECISION_RANKINGS["${grailsApplication.config.grails.tags.datePicker.default.precision}"] :
				PRECISION_RANKINGS["minute"]))

		def day
		def month
		def year
		def hour
		def minute
		def dfs = new DateFormatSymbols(RCU.getLocale(request))

		def c = null
		if (value instanceof Calendar) {
			c = value
		}
		else if (value != null) {
			c = new GregorianCalendar()
			c.setTime(value)
		}

		if (c != null) {
			day = c.get(GregorianCalendar.DAY_OF_MONTH)
			month = c.get(GregorianCalendar.MONTH) + 1		// add one, as Java stores month from 0..11
			year = c.get(GregorianCalendar.YEAR)
			hour = c.get(GregorianCalendar.HOUR_OF_DAY)
			minute = c.get(GregorianCalendar.MINUTE)
		}

		if (years == null) {
			def tempyear
			if (year == null) {
				// If no year, we need to get current year to setup a default range... ugly
				def tempc = new GregorianCalendar()
				tempc.setTime(new Date())
				tempyear = tempc.get(GregorianCalendar.YEAR)
			}
			else {
				tempyear = year
			}
			if (relativeYears) {
				if (relativeYears.reverse) {
					years = (tempyear + relativeYears.toInt)..(tempyear + relativeYears.fromInt)
				} else {
					years = (tempyear + relativeYears.fromInt)..(tempyear + relativeYears.toInt)
				}
			} else {
				years = (tempyear - 100)..(tempyear + 100)
			}
		}

		booleanToAttribute(attrs, 'disabled')
		booleanToAttribute(attrs, 'readonly')

		// get the localized format for dates. NOTE: datepicker only uses Lowercase syntax and does not understand hours, seconds, etc. (it uses: dd, d, mm, m, yyyy, yy)
		def messageSource = grailsAttributes.getApplicationContext().getBean('messageSource')
		String dateFormat = messageSource.getMessage("default.date.datepicker.format",null,null,LocaleContextHolder.locale )
		if (!dateFormat) { // if date.datepicker.format is not used use date.format but remove characters not used by datepicker
			dateFormat = messageSource.getMessage("default.date.format",null,'mm/dd/yyyy',LocaleContextHolder.locale )\
				.replace('z', '').replace('Z', '')\
				.replace('h', '').replace('H', '')\
				.replace('k', '').replace('K', '')\
				.replace('w', '').replace('W', '')\
				.replace('s', '').replace('S', '')\
				.replace('m', '').replace('a', '').replace('D', '').replace('E', '').replace('F', '').replace('G', '').replace(':', '')\
				.replace('MMM', 'MM').replace('ddd', 'dd')\
				.trim()\
				.toLowerCase()
		}
		String formattedDate = g.formatDate(format: dateFormat.replace('m', 'M'), date: c?.getTime())
		out.println "	<input id=\"${id}\" name=\"${name}\" class=\"date\" size=\"16\" type=\"text\" value=\"${formattedDate}\" data-date-format=\"${dateFormat}\"/>"
	}

	/**
	* A helper tag for creating checkboxes.
	 * example: 	<bs:checkBox name="sendEmail" value="${false}" onLabel="On" offLabel="Off"/>
	 * @emptyTag
	 *
	 * @attr name REQUIRED the name of the checkbox
	 * @attr value the value of the checkbox
	 * @attr checked if evaluates to true sets to checkbox to checked
	 * @attr onLabel the I18N code (or the text itself if not defined) to label the On/Yes/True button
	 * @attr offLabel the I18N code (or the text itself if not defined) to label the Off/No/False button
	 * @attr disabled if evaluates to true sets to checkbox to disabled
	 * @attr readonly if evaluates to true, sets to checkbox to read only
	 * @attr id DOM element id; defaults to name
	 */
	 Closure checkBox = { attrs ->
		def messageSource	= grailsAttributes.messageSource
		def locale			= RCU.getLocale(request)

		def value		= attrs.remove('value')
		def name		= attrs.remove('name')
		def onLabel		= attrs.remove('onLabel')  ?: "checkbox.on.label"
		def offLabel	= attrs.remove('offLabel') ?: "checkbox.off.label"
		booleanToAttribute(attrs, 'disabled')
		booleanToAttribute(attrs, 'readonly')

		// Deal with the "checked" attribute. If it doesn't exist, we
		// default to a value of "true", otherwise we use Groovy Truth
		// to determine whether the HTML attribute should be displayed or not.
		def checked = true
		def checkedAttributeWasSpecified = false
		if (attrs.containsKey('checked')) {
			checkedAttributeWasSpecified = true
			checked = attrs.remove('checked')
		}

		if (checked instanceof String) checked = Boolean.valueOf(checked)

		if (value == null) value = false
		def hiddenValue = "";

		value = processFormFieldValueIfNecessary(name, value,"checkbox")
		hiddenValue = processFormFieldValueIfNecessary("_${name}", hiddenValue, "hidden")

//		out << """
//		<div>
//			<label for=\"_${name}\" class="control-label">
//				${messageSource.getMessage(name + '.label', null, '', locale)}
//			</label>
//
//			<div>
//"""

		out << "				<input type=\"hidden\" name=\"_${name}\"";
		if(hiddenValue != "") {
			out << " value=\"${hiddenValue}\"";
		}
		out << " />\n				<input class='hide pull-right' type=\"checkbox\" name=\"${name}\" "
		if (checkedAttributeWasSpecified) {
			if (checked) {
				out << 'checked="checked" '
			}
		}
		else if (value && value != "") {
			out << 'checked="checked" '
			checked = true
		}

		def outputValue = !(value instanceof Boolean || value?.class == boolean.class)
		if (outputValue) {
			out << "value=\"${value}\" "
		}
		// process remaining attributes
		outputAttributes(attrs, out)

		if (!attrs.containsKey('id')) {
			out << """id="${name}" """
		}

		// close the tag, with no body
		out << ' />'

		out << """
				<div id="btngroup" class="btn-group radiocheckbox" data-toggle="buttons-radio">
					<div class="btn btn-sm on   ${value ? 'active btn-primary' : 'btn-default'}">${messageSource.getMessage(onLabel, null, onLabel, locale)}</div>
					<div class="btn btn-sm off ${!value ? 'active btn-primary' : 'btn-default'}">${messageSource.getMessage(offLabel, null, offLabel, locale)}</div>
				</div>
		"""
	}

	 /**
	  * Dump out attributes in HTML compliant fashion.
	  */
	void outputAttributes(attrs, writer, boolean useNameAsIdIfIdDoesNotExist = false) {
		attrs.remove('tagName') // Just in case one is left
		attrs.each { k, v ->
			if(v != null) {
				writer << k
				writer << '="'
				writer << v.encodeAsHTML()
				writer << '" '
			}
		}
		if (useNameAsIdIfIdDoesNotExist) {
			outputNameAsIdIfIdDoesNotExist(attrs, writer)
		}
	}

	/**
	 * getter to obtain RequestDataValueProcessor from
	 */
    private getRequestDataValueProcessor() {
        if (requestDataValueProcessor == null && grailsAttributes.getApplicationContext().containsBean("requestDataValueProcessor")){
            requestDataValueProcessor = grailsAttributes.getApplicationContext().getBean("requestDataValueProcessor")
        }
        return requestDataValueProcessor;
    }

	 private processFormFieldValueIfNecessary(name, value, type) {
		 def requestDataValueProcessor = getRequestDataValueProcessor();
		 def processedValue = value;
		 if(requestDataValueProcessor != null) {
			 processedValue = requestDataValueProcessor.processFormFieldValue(request, name, "${value}", type);
		 }
		 return processedValue;
	 }

	/**
	* Some attributes can be defined as Boolean values, but the html specification
	* mandates the attribute must have the same value as its name. For example,
	* disabled, readonly and checked.
	*/
	private void booleanToAttribute(def attrs, String attrName) {
		def attrValue = attrs.remove(attrName)
		// If the value is the same as the name or if it is a boolean value,
		// reintroduce the attribute to the map according to the w3c rules, so it is output later
		if (Boolean.valueOf(attrValue) ||
		  (attrValue instanceof String && attrValue?.equalsIgnoreCase(attrName))) {
			attrs.put(attrName, attrName)
		} else if (attrValue instanceof String && !attrValue?.equalsIgnoreCase('false')) {
			// If the value is not the string 'false', then we should just pass it on to
			// keep compatibility with existing code
			attrs.put(attrName, attrValue)
		}
	}

}
