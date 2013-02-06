<?php

  // Capture the arguments
  $classname = $argv[0];
  $infile = $argv[1];
  $outfile = $argv[2];

  // Load json from a file
  $filecontents = $file_get_contents($infile);
  
  // Deserialize to an object
  $parsed = json_decode($filecontents, true);
  $o = eval "$o = new #{classname}; $o->from_json($parsed);"
  
  // Serialize object to json
  file_put_contents($outfile, $o->to_json());

?>
