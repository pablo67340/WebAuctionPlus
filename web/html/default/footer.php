<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
global $html,$num_queries;
$output='';


//<div class="spacer"></div>
$output.="\n\n\n";
switch($html->getPageFrame()){
case 'default':
  $output.='
</div>
';
break;
case 'basic':
  $output.='
</td></tr>
<tr><td style="height: 1px;">
';
}
$output.='
<div id="footer" class="clear" style="text-align:center; padding:10px">
  <!-- Paste advert code here -->

  <!-- ====================== -->
  <p style="margin-bottom: 10px; font-size: large; color: #FFFFFF;">&nbsp;'.
    '<a href="http://website.rhythmpvp.com" '.
    'target="_blank" style="color: #FFFFFF;"><u>RhythmMarket</u> '.SettingsClass::getString('Version').'</a> By pablo67340 & lorenzop&nbsp;<br />'.

    '<b>&nbsp;Rendered page in '.GetRenderTime().' Seconds with '.((int)@$num_queries).' Queries&nbsp;</b></p>
  <p style="font-size: smaller; color: #FFFFFF;">'.
//    '<a href="http://validator.w3.org/#validate_by_input" target="_blank">'.
//    '<img src="{path=static}/valid-xhtml10.png" alt="Valid XHTML 1.0 Transitional" width="88" height="31" style="border-width: 0px;" /></a></p>
'
</div>
';
switch($html->getPageFrame()){
case 'basic':
  $output.='
</td></tr>
</table>
';
}
$output.='
</body>
</html>
';


return($output);
?>
