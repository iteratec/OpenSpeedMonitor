(function () {
  var Pos = CodeMirror.Pos;

  function forEach(arr, f) {
    for (var i = 0, e = arr.length; i < e; ++i) f(arr[i]);
  }

  function arrayContains(arr, item) {
    if (!Array.prototype.indexOf) {
      var i = arr.length;
      while (i--) {
        if (arr[i] === item) {
          return true;
        }
      }
      return false;
    }
    return arr.indexOf(item) != -1;
  }

  function scriptHint(editor, keywords, getToken, options) {
    // Find the token at the cursor
    var cur = editor.getCursor(), token = getToken(editor, cur), tprop = token;
    if (/\b(?:string|comment)\b/.test(token.type)) return;
    token.state = CodeMirror.innerMode(editor.getMode(), token.state).state;

    // If it's not a 'word-style' token, ignore the token.
    if (!/^[\w$_]*$/.test(token.string)) {
      token = tprop = {start: cur.ch, end: cur.ch, string: "", state: token.state,
                       type: token.string == "." ? "property" : null};
    }
    // If it is a property, find out what it is a property of.
    while (tprop.type == "property") {
      tprop = getToken(editor, Pos(cur.line, tprop.start));
      if (tprop.string != ".") return;
      tprop = getToken(editor, Pos(cur.line, tprop.start));
      if (!context) var context = [];
      context.push(tprop);
    }
    return {list: getCompletions(token, getToken(editor, Pos(cur.line, token.start - 1)), keywords, options),
            from: Pos(cur.line, token.start),
            to: Pos(cur.line, token.end)};
  }

  function javascriptHint(editor, options) {
    return scriptHint(editor, javascriptKeywords,
                      function (e, cur) {return e.getTokenAt(cur);},
                      options);
  };
  //CodeMirror.javascriptHint = javascriptHint; // deprecated
  CodeMirror.registerHelper("hint", "pts", javascriptHint);

  var javascriptKeywords = ("navigate clickAndWait setInnerHTML setInnerText setValue submitForm exec execAndWait fileDialog requiredRequest setABM setDOMElement setDOMRequest setTimeout waitForComplete waitForJSDone false null true block setCookie setDns setDNSName setUserAgent addHeader setHeader resetHeaders combineSteps minInterval endInterval ignoreErrors logErrors loadFile loadVariables setEventName sleep logData").split(" ").sort();
  
  if (typeof String.prototype.startsWith != 'function') {
	    String.prototype.startsWith = function(prefix) {
	        return this.slice(0, prefix.length) == prefix;
	    };
	}
  
  function getCompletions(token, previousToken, keywords, options) {
    //return CodeMirror.measuredEvents;
	  //if (token.string == '')
	  //return keywords;
	  function suggest(prefix, words) {
		  if (prefix == '') {
			  return words;
		  } else {
			  return words.filter(function (keyword, index, array) { 
					 var str = (typeof keyword == 'string') ? keyword : keyword.text;
					 return str.startsWith(prefix); 
				  });
		  }
	  }
	  
	  if (previousToken.string == "setEventName") {
		  return suggest(token.string, CodeMirror.measuredEvents);
	  } else {
		  return suggest(token.string, keywords);
	  }
	  console.log(token.string);
  }
})();
