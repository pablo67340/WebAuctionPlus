<?php if(!defined('DEFINE_INDEX_FILE')){if(headers_sent()){echo '<header><meta http-equiv="refresh" content="0;url=../"></header>';}else{header('HTTP/1.0 301 Moved Permanently'); header('Location: ../');} die("<font size=+2>Access Denied!!</font>");}
// this class handles settings stored in the database
class SettingsClass{


public static function LoadSettings(){global $config, $db;
  if(!$db) ConnectDB();
  $result = mysql_query("SELECT `name`,`value` FROM `".$config['table prefix']."Settings`", $db);
  if(!$result) {echo '<p>Failed to load settings from the database! The plugin may not have been loaded for the first time yet.</p>'; exit();}
  if(mysql_num_rows($result) == 0) return;
  while(TRUE) {
  	$row = mysql_fetch_assoc($result);
  	if(!$row) break;
    $config['settings'][$row['name']] = array(
      'value'   => $row['value'],
      'changed' => FALSE,
    );
  }
}


public static function SaveSettings(){global $config;
  echo '<h1>SaveSettings function not finished!</h1>';
}


// set defaults / type
public static function setDefault($name, $default='', $setIfEmpty=TRUE){global $config;
  // set default
  if($setIfEmpty)
    if(empty($config['settings'][$name]['value']))
      $config['settings'][$name]['value'] = $default;
  else
    if(!isset($config['settings'][$name]['value']))
      $config['settings'][$name]['value'] = $default;
  // set type
  $type = gettype($default);
  if(    $type=='string' ) $config['settings'][$name]['value'] = (string)  $config['settings'][$name]['value'];
  elseif($type=='boolean') $config['settings'][$name]['value'] = toBoolean($config['settings'][$name]['value']);
  elseif($type=='integer') $config['settings'][$name]['value'] = (integer) $config['settings'][$name]['value'];
  elseif($type=='double' ) $config['settings'][$name]['value'] = (float)   $config['settings'][$name]['value'];
  $config['settings'][$name]['changed'] = (boolean)@$config['settings'][$name]['changed'];
}


// get setting
private static function getSetting($name){global $config;
  if(isset($config['settings'][$name]['value']))
    return($config['settings'][$name]['value']);
  else return(NULL);
}
public static function getString($name){
  $value = self::getSetting($name);
  if($value === NULL) return(NULL);
  else                return((string)$value);
}
public static function getBoolean($name){
  $value = self::getSetting($name);
  if($value === NULL) return(NULL);
  else                return(toBoolean($value));
}
public static function getInteger($name){
  $value = self::getSetting($name);
  if($value === NULL) return(NULL);
  else                return((integer)$value);
}
public static function getDouble($name){
  $value = self::getSetting($name);
  if($value === NULL) return(NULL);
  else                return((float)$value);
}


public static function setSetting($name, $value){global $config;
  $config['settings'][$name]['value']   = $value;
  $config['settings'][$name]['changed'] = TRUE;
}


}
?>
