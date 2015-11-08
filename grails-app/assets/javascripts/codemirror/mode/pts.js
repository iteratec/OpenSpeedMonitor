CodeMirror.defineMode("pts", function() {
  function wordRegexp(words) {
    return new RegExp("^((" + words.join(")|(") + "))\\b");
  }

  var placeholders = new RegExp("^\\$\{[a-zA-Z0-9_]+\}");
  var identifiers = new RegExp("^[^\"'$ \t]+");
  var keywords = wordRegexp(("navigate clickAndWait setInnerHTML setInnerText setValue submitForm exec execAndWait fileDialog requiredRequest setABM setDOMElement setDOMRequest setTimeout waitForComplete waitForJSDone false null true block setCookie setDns setDNSName setUserAgent addHeader setHeader resetHeaders combineSteps minInterval endInterval ignoreErrors logErrors loadFile loadVariables setEventName sleep logData").split(" ").sort());

  function tokenize(stream, state) {
    // whitespaces
    if (stream.eatSpace()) return null;

    if (stream.match(/^\/\//) && stream.start == 0){
      stream.skipToEnd();
      return 'comment';
    }

    // Handle Strings
    if (stream.match(/^"([^"]|(""))*"/)) { return 'string'; } ;
    if (stream.match(/^'([^']|(''))*'/)) { return 'string'; } ;

    // Handle words
    if (stream.match(keywords)) { return 'keyword'; } ;
    if (stream.match(placeholders)) { return 'variable-2';  } ;
    if (stream.match(identifiers)) { return 'variable'; } ;

    // Handle non-detected items
    stream.next();
    return 'variable'; // 'error'
  };

  return {
    token: tokenize 
  };
});

CodeMirror.defineMIME("text/x-pts", "pts");