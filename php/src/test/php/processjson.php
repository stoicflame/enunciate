<?php

  include('enunciate.php');

  // Capture the arguments
  $classnames = split('::', $argv[1]);
  $classname = "FS\\".end($classnames);
  $infile = $argv[2];
  $outfile = $argv[3];

  // Load json from a file
  $filecontents = file_get_contents($infile);
  
  // Deserialize to an object
  $parsed = json_decode($filecontents, true);
  $eval_string = "\$o = new $classname();";
  echo $eval_string;
  eval($eval_string);
  $o->from_json($parsed);
  
  // Serialize object to json
  file_put_contents($outfile, $o->to_json());

?>