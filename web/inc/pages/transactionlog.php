<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// transaction log page


//function RenderPage_transactionlog(){global $config; $output='';
//  $config['title'] = 'Transaction Log';
//  $output.='<h1 style="text-align: center;">** Under Construction **</h1>';
//  return($output);


//session_start();
//if(!isset($_SESSION['User'])){
//  header('Location: login.php');
//}
//$user=$_SESSION['User'];
//require('scripts/config.php');
//require('scripts/itemInfo.php');
//require('scripts/updateTables.php');
//$isAdmin=$_SESSION['Admin'];
//$queryAuctions=mysql_query("SELECT * FROM WA_Auctions");
//if($useMySQLiConomy){
//  $queryiConomy=mysql_query("SELECT `balance` FROM $iConTableName WHERE username='$user'");
//  $iConRow = mysql_fetch_assoc($queryiConomy);
//}
//$queryMySales    =mysql_query("SELECT `id`,`name`,`damage`,UNIX_TIMESTAMP(`time`) AS `time`,`quantity`,`price`,`seller`,`buyer` FROM WA_SellPrice WHERE seller='$user'");
//$queryMyPurchases=mysql_query("SELECT `id`,`name`,`damage`,UNIX_TIMESTAMP(`time`) AS `time`,`quantity`,`price`,`seller`,`buyer` FROM WA_SellPrice WHERE buyer='$user'");

//$playerQuery=mysql_query("SELECT * FROM WA_Players WHERE name='$user'");
//$playerRow=mysql_fetch_row($playerQuery);
//$mailQuery=mysql_query("SELECT * FROM WA_Mail WHERE player='$user'");
//$mailCount=mysql_num_rows($mailQuery);

//? >
//<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
//<html>
//  <head>
//    <meta http-equiv="content-type" content="text/html; charset=utf-8" />    
//    <title>WebAuction</title>
//    <link rel="icon" type="image/x-icon" href="images/favicon.ico" />
//    <style type="text/css" title="currentStyle">
//      @import "css/table_jui.css";
//      @import "css/< ?php echo $uiPack? >/jquery-ui-1.8.18.custom.css";
//    </style>
//    <link rel="stylesheet" type="text/css" href="css/< ?php echo $cssFile? >.css" />
//    <script type="text/javascript" language="javascript" src="js/jquery-1.7.2.min.js"></script>
//    <script type="text/javascript" language="javascript" src="js/jquery.dataTables-1.9.0.min.js"></script>
//< ?php
////    <script type="text/javascript" language="javascript" src="js/dataTables.Sort.js"></script>
//? >
//    <script type="text/javascript" charset="utf-8">
//      $(document).ready(function() {
//        oTable=$('#example').dataTable({
//          "bJQueryUI": true,
//          "sPaginationType": "full_numbers",
//          "aoColumns": [
//            null,
//            { "sType": "html" },
//            { "sType": "html" },
//            null,
//            null,
//            null,
//            null
//          ]
//        });
//        oTable=$('#example2').dataTable({
//          "bJQueryUI": true,
//          "sPaginationType": "full_numbers",
//          "aoColumns": [
//            null,
//            { "sType": "html" },
//            { "sType": "html" },
//            null,
//            null,
//            null,
//            null
//          ]
//        });
//      } );
//    </script>
//  </head>
//  <div id="holder">
//    < ?php include("topBoxes.php"); ? >
//    <h1>Web Auction</h1>
//    <br/>
//    <h2>My Items Bought</h2>
        
      
//<div class="demo_jui">
//<table cellpadding="0" cellspacing="0" border="0" class="display" id="example">
//  <thead>
//    <tr>
//      <th>Time</th>
//      <th>Item</th>
//      <th>Seller</th>
//            <th>Quantity</th>
//            <th>Price (Each)</th>
//      <th>Price (Total)</th>  
//      <th>% of Market Price</th>
//    </tr>
//  </thead>
//  <tbody>
//< ?php

//while(list($id, $name, $damage, $time, $quantity, $price, $seller, $buyer)=mysql_fetch_row($queryMyPurchases)){ 
//  $marketPrice=getMarketPrice($id, 3);
//    $timeFormat=date('jS M Y H:i:s', $time);  
//    if($marketPrice>0){
//      $marketPercent=round((($price/$marketPrice)*100),1);
//    }else{
//      $marketPercent='N/A';
//    }
//    if($marketPercent=='0'){
//      $grade='gradeU';
//    }elseif($marketPercent<=50){
//      $grade='gradeA';
//    }elseif($marketPercent<=150){
//      $grade='gradeC';
//    }else{
//      $grade='gradeX';
//    }
//    echo '  <tr class="'.$grade.'">'."\n";
//    echo '    <td>'.$timeFormat.'</td>'."\n";
//    // alt="'.getItemName($name, $damage).'"
//    echo '    <td><a href="graph.php?name='.$name.'&damage='.$damage.'"><img src="'.getItemImage($name, $damage).'" /><br/>'.getItemName($name, $damage);
//    $queryEnchantLinks=mysql_query("SELECT enchId FROM WA_EnchantLinks WHERE itemId='".((int)$id)."' AND itemTableId=3");
//    while(list($enchId)=mysql_fetch_row($queryEnchantLinks)){
//      $queryEnchants=mysql_query("SELECT * FROM WA_Enchantments WHERE id='".$enchId."'");
//      while(list($idj, $enchName, $enchantId, $level)=mysql_fetch_row($queryEnchants)){
//        echo '<br />'.getEnchName($enchantId).' '.numberToRoman($level);
//      }
//    }
//    echo '</a></td>'."\n";
//    echo '    <td><img width="32px" src="http://minotar.net/avatar/'.$seller.'" /><br/>'.$seller.'</td>'."\n";
//    echo '    <td>'.number_format($quantity,0).'</td>'."\n";
//    echo '    <td class="center">$ '.number_format($price,2).'</td>'."\n";
//    echo '    <td class="center">$ '.number_format($price*$quantity,2).'</td>'."\n";
//    echo '    <td class="center">'.($marketPercent=='N/A'?'N/A':number_format($marketPercent,1).' %').'</td>'."\n";
//    echo '  </tr>'."\n";
//  }
//  echo '</tbody>'."\n";
//  echo '</table>'."\n";
//  echo '<h2>My Items Sold</h2>'."\n";
//  echo '</div>'."\n";
//? >

//<div class="demo_jui">
//<table cellpadding="0" cellspacing="0" border="0" class="display" id="example2">
//  <thead>
//    <tr>
//      <th>Date</th>
//      <th>Item</th>
//      <th>Buyer</th>
//      <th>Quantity</th>
//      <th>Price (Each)</th>
//      <th>Price (Total)</th>  
//      <th>% of Market Price</th>
//    </tr>
//  </thead>
//  <tbody>

//< ?php
//while(list($id, $name, $damage, $time, $quantity, $price, $seller, $buyer)=mysql_fetch_row($queryMySales)){
//  $marketPrice=getMarketPrice($id, 3);
//  $timeFormat=date('jS M Y H:i:s', $time);
//  if($marketPrice>0){
//    $marketPercent=number_format((($price/$marketPrice)*100),1);
//  }else{
//    $marketPercent='N/A';
//  }
//  if($marketPercent=="0"){
//    $grade='gradeU';
//  }elseif($marketPercent<=50){
//    $grade='gradeA';
//  }elseif($marketPercent<=150){
//    $grade='gradeC';
//  }else{
//    $grade='gradeX';
//  }
//  echo '  <tr class="'.$grade.'">'."\n";
//  echo '    <td>'.$timeFormat.'</td>'."\n";
//  // alt="'.getItemName($name, $damage).'"
//  echo '    <td><a href="graph.php?name='.$name.'&damage='.$damage.'"><img src="'.getItemImage($name, $damage).'" /><br/>'.getItemName($name, $damage);
//  $queryEnchantLinks=mysql_query("SELECT enchId FROM WA_EnchantLinks WHERE itemId='$id' AND itemTableId=3");
//  while(list($enchId)=mysql_fetch_row($queryEnchantLinks)){
//    $queryEnchants=mysql_query("SELECT * FROM WA_Enchantments WHERE id='$enchId'");
//    while(list($idj, $enchName, $enchantId, $level)=mysql_fetch_row($queryEnchants)){
//      echo '<br/>'.getEnchName($enchantId).' '.numberToRoman($level);
//    }
//  }
//  echo '</a></td>'."\n";
//  echo '    <td><img width="32px" src="http://minotar.net/avatar/'.$buyer.'" /><br/>'.$buyer.'</td>'."\n";
//  echo '    <td>'.number_format($quantity,0).'</td>'."\n";
//  echo '    <td class="center">$ '.number_format($price,2).'</td>'."\n";
//  echo '    <td class="center">$ '.number_format($price*$quantity,2).'</td>'."\n";
//  echo '    <td class="center">'.$marketPercent.($marketPercent=='N/A'?'':' %').'</td>'."\n";
//  echo '  </tr>'."\n";
//}
//echo '</tbody>'."\n";
//echo '</table>'."\n";
//echo '</div>'."\n";
//echo '<div class="spacer"></div>'."\n";
//include('footer.php');
//echo '</div>'."\n";
//echo '</body>'."\n";
//echo '</html>'."\n";


//}


?>
