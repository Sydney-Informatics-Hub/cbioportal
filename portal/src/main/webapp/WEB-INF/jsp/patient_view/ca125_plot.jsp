<object data="<%=ca125PlotUrl%>" type="application/pdf" width="100%" height="600px">
 
  <p>It appears you don't have a PDF plugin for this browser.</p>
  <p id="missing-pdf-plugin">You can <a href="<%=ca125PlotUrl%>">click here to
  download the CA125 plot PDF file</a>.</p>
  
</object>

<script type="text/javascript">
$(document).ready(function(){
    if (cbio.util.browser.mozilla) { // firefox
        var msg = "You can <ul><li>either install a PDF plugin such as <a href='https://addons.mozilla.org/en-US/firefox/addon/pdfjs/'>this one</a> and refresh this page,</li>\n\
                   <li>or <a href='<%=ca125PlotUrl%>'>click here to download the CA125 plot PDF file</a>.</li></ul>";
        $("#missing-pdf-plugin").html(msg);
    }
});

</script>
