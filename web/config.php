<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}


// Database config
function ConnectDB(){global $db,$config;


  $host     = 'localhost';
  $port     = 3306;
  $username = '';
  $password = '';
  $database = '';
  $config['table prefix'] = 'WA_';


  $db=@mysql_pconnect($host.($port==0?'':':'.((int)$port)),$username,$password);
  if(!$db || !@mysql_select_db($database,$db)){echo '<p>MySQL Error: '.mysql_error().'</p>'; exit();}
  mysql_query("SET names UTF8");
}


// iConomy config
$config['iConomy']['use']   = 'auto';    // ( true / false / 'auto' )  you you have iConomy data in another table in the same database?
$config['iConomy']['table'] = 'iConomy'; // 'iConomy' is the default table name when using MySQL with iConomy


?>
