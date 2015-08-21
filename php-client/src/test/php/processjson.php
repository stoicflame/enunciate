<?php

  include('enunciate.php');

  // Capture the arguments
  $classname = str_replace("::", "\\", $argv[1]);
  $infile = $argv[2];
  $outfile = $argv[3];

  // Load json from a file
  $filecontents = file_get_contents($infile);
  
  // Deserialize to an object
  $parsed = json_decode($filecontents, true);
  eval("\$o = new $classname();");
  $o->initFromArray($parsed);
  
  // Serialize object to json
  file_put_contents($outfile, $o->toJson());

?>