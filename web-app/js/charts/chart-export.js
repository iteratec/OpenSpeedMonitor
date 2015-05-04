  function constructFileName(fileType) {
    var currentdate = new Date(); 
    var datetime = "" + currentdate.getFullYear() + "-"  
                    + (currentdate.getMonth()+1)  + "-" 
                    + currentdate.getDate() + "_"
                    + currentdate.getHours() + "-"  
                    + currentdate.getMinutes() + "-" 
                    + currentdate.getSeconds() + "";
    
    var curAreaName = (window.location.pathname.indexOf("eventResultDashboard") > -1) ? "event" : "csi";
    return ("osm_" + curAreaName + "_" + datetime + "." + fileType + "");
  }

  function prepareNewBlankCanvas(modelCanvasId, reduceHeightBy) {
    if (typeof(reduceHeightBy)==='undefined') reduceHeightBy = 10;
    var canvas = document.createElement('canvas');
    canvas.setAttribute('id', 'canvas_everything_merged');
    canvas.setAttribute('style', "display:none");
    canvas.width = 3000;
    canvas.height = 5000;
    document.body.appendChild(canvas);
    var ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, canvas.width, canvas.height);
  
    ctx.canvas.width  = $( modelCanvasId ).first().width();
    ctx.canvas.height = $( modelCanvasId ).first().height() - reduceHeightBy;
    ctx.globalCompositeOperation = "destination-under";
    ctx.fillStyle = '#fff';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    return {
      canvas: canvas,
      ctx: ctx
    };
  }

  function removeObjectFromDom(objectId) {
    useMe = document.querySelector(objectId);
    useMe.parentNode.removeChild(useMe);
  }

  function downloadCanvas(canvas, fileType) { // currently, most browsers only support toDataURL with mimeTypes 'jpeg' and 'png'
    var canvasdata = canvas.toDataURL("image/" + fileType + "");      
    var pngimg = '<img src="'+canvasdata+'">';
    
    newA = document.createElement('a');
    newA.setAttribute('href', '');
    newA.setAttribute('id', 'converteddataurl');
    newA.setAttribute('style', "display:none");
    document.body.appendChild(newA);
    
    var a = document.getElementById("converteddataurl");
    
    var newFileName = constructFileName(fileType);
    a.download = newFileName;
    a.href = canvasdata;
    a.click();
    removeObjectFromDom("#converteddataurl");
  }